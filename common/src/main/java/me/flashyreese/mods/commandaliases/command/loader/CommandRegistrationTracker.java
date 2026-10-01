package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Records only our changes to one dispatcher, so unloading preserves other registrations. */
public final class CommandRegistrationTracker<S> {
    private static final Field CHILDREN = field(CommandNode.class, "children");
    private static final Field LITERALS = field(CommandNode.class, "literals");
    private static final Field ARGUMENTS = field(CommandNode.class, "arguments");
    private static final Field COMMAND = field(CommandNode.class, "command");
    private static final Field REDIRECT = field(CommandNode.class, "redirect");
    private static final Field MODIFIER = field(CommandNode.class, "modifier");
    private static final Field FORKS = field(CommandNode.class, "forks");
    private static final Field LITERAL_LOWER_CASE = field(LiteralCommandNode.class, "literalLowerCase");

    private final CommandNode<S> root;
    private final Field literalField;
    private final List<Change> changes = new ArrayList<>();

    public CommandRegistrationTracker(CommandDispatcher<S> dispatcher, Field literalField) {
        this.root = dispatcher.getRoot();
        this.literalField = literalField;
    }

    public boolean belongsTo(CommandDispatcher<S> dispatcher) {
        return this.root == dispatcher.getRoot();
    }

    public boolean isEmpty() {
        return this.changes.isEmpty();
    }

    public void register(LiteralArgumentBuilder<S> builder) {
        this.merge(this.root, builder.build());
    }

    private void merge(CommandNode<S> parent, CommandNode<S> candidate) {
        CommandNode<S> existing = parent.getChild(candidate.getName());
        if (existing == null) {
            parent.addChild(candidate);
            this.recordAddedTree(parent, candidate, Collections.newSetFromMap(new IdentityHashMap<>()));
            return;
        }

        // Match CommandNode.addChild: merge descendants and replace a non-null executor only.
        // Requirements, argument types, redirects and the original node identity stay intact.
        if (candidate.getCommand() != null && candidate.getCommand() != existing.getCommand()) {
            this.changes.add(new ReplacedCommand<>(existing, existing.getCommand(), candidate.getCommand()));
            set(COMMAND, existing, candidate.getCommand());
        }
        for (CommandNode<S> child : candidate.getChildren()) {
            this.merge(existing, child);
        }
    }

    private void recordAddedTree(CommandNode<S> parent, CommandNode<S> node, Set<CommandNode<S>> visited) {
        this.changes.add(new AddedNode<>(parent, node.getName(), node, node.getCommand(), node.getRedirect()));
        if (visited.add(node)) {
            for (CommandNode<S> child : node.getChildren()) {
                this.recordAddedTree(node, child, visited);
            }
        }
    }

    public boolean reassign(String from, String to) {
        CommandNode<S> node = this.root.getChild(from);
        if (!(node instanceof LiteralCommandNode<S> literal) || this.root.getChild(to) != null) {
            return false;
        }
        rename(literal, to, this.literalField);
        removeChild(this.root, from, literal);
        this.root.addChild(literal);
        this.changes.add(new RenamedNode<>(this.root, literal, from, to, this.literalField));
        return true;
    }

    /** Undo in reverse registration order. Conflicting reassignments remain available for retry. */
    public void unload() {
        for (int i = this.changes.size() - 1; i >= 0; i--) {
            if (this.changes.get(i).undo()) {
                this.changes.remove(i);
            }
        }
    }

    public Map<String, String> pendingReassignments() {
        Map<String, String> pending = new LinkedHashMap<>();
        for (Change change : this.changes) {
            if (change instanceof RenamedNode<?> rename) {
                pending.put(rename.from(), rename.to());
            }
        }
        return pending;
    }

    private interface Change {
        boolean undo();
    }

    private record ReplacedCommand<S>(CommandNode<S> node, Command<S> previous, Command<S> installed) implements Change {
        @Override
        public boolean undo() {
            if (this.node.getCommand() == this.installed) {
                set(COMMAND, this.node, this.previous);
            }
            return true;
        }
    }

    private record AddedNode<S>(CommandNode<S> parent, String name, CommandNode<S> node,
                                Command<S> installed, CommandNode<S> redirect) implements Change {
        @Override
        public boolean undo() {
            if (this.parent.getChild(this.name) != this.node) {
                return true;
            }
            if (this.node.getCommand() == this.installed) {
                set(COMMAND, this.node, null);
            }
            if (this.redirect != null && this.node.getRedirect() == this.redirect) {
                set(REDIRECT, this.node, null);
                set(MODIFIER, this.node, null);
                set(FORKS, this.node, false);
            }
            // Later registrations may have added descendants or replaced the executor.
            // Retain their path and its permission requirement, stripping only our contribution.
            if (this.node.getChildren().isEmpty() && this.node.getCommand() == null && this.node.getRedirect() == null) {
                removeChild(this.parent, this.name, this.node);
            }
            return true;
        }
    }

    private record RenamedNode<S>(CommandNode<S> parent, LiteralCommandNode<S> node, String from,
                                  String to, Field literalField) implements Change {
        @Override
        public boolean undo() {
            CommandNode<S> occupied = this.parent.getChild(this.from);
            if (occupied == this.node && this.node.getName().equals(this.from)) {
                return true;
            }
            if (occupied != null || !this.node.getName().equals(this.to)) {
                CommandAliasesMod.logger().warn(
                        "Cannot restore reassigned command '{}' from '{}': another registration conflicts with it. "
                                + "Keeping both commands; retry unload after resolving the conflict.", this.from, this.to);
                return false;
            }
            rename(this.node, this.from, this.literalField);
            removeChild(this.parent, this.to, this.node);
            this.parent.addChild(this.node);
            return true;
        }
    }

    private static void removeChild(CommandNode<?> parent, String name, CommandNode<?> expected) {
        for (Field field : List.of(CHILDREN, LITERALS, ARGUMENTS)) {
            Map<?, ?> index = (Map<?, ?>) get(field, parent);
            // Brigadier node.equals recursively compares subtrees; identity is intentional here.
            if (index.get(name) == expected) {
                index.remove(name);
            }
        }
    }

    private static void rename(LiteralCommandNode<?> node, String name, Field literalField) {
        if (literalField == null) {
            throw new IllegalStateException("Brigadier literal field is unavailable; cannot reassign a command");
        }
        String previous = node.getLiteral();
        try {
            literalField.set(node, name);
            LITERAL_LOWER_CASE.set(node, name.toLowerCase(Locale.ROOT));
        } catch (IllegalAccessException exception) {
            set(literalField, node, previous);
            set(LITERAL_LOWER_CASE, node, previous.toLowerCase(Locale.ROOT));
            throw new IllegalStateException("Could not rename Brigadier literal " + previous, exception);
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static Object get(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not read Brigadier field " + field.getName(), exception);
        }
    }

    private static void set(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not update Brigadier field " + field.getName(), exception);
        }
    }
}
