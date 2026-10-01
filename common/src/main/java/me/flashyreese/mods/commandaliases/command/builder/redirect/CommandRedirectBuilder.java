package me.flashyreese.mods.commandaliases.command.builder.redirect;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.IntegerSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/** Builds a parsing tree for an alias; execution goes through the original command dispatcher. */
public class CommandRedirectBuilder<S extends SharedSuggestionProvider> implements CommandBuilderDelegate<S> {
    private final String filePath;
    private final RedirectCommand command;
    private final CommandType commandType;
    private LiteralCommandNode<S> aliasLeaf;
    private ParsingTree parsingTree;

    public CommandRedirectBuilder(String filePath, RedirectCommand command, CommandType commandType) {
        this.filePath = filePath;
        this.command = command;
        this.commandType = commandType;
    }

    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        String alias = this.command.getCommand().trim();
        String target = this.command.getRedirectTo().trim();
        Match<S> match = this.findNode(dispatcher, dispatcher.getRoot(), new StringReader(target), null);
        boolean noArguments = this.command.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG;
        if (match == null || (noArguments && match.node().getCommand() == null)) {
            CommandAliasesMod.logger().error("[{}] {} - Invalid or incomplete redirect target \"{}\": {}",
                    this.commandType, this.command.getCommandMode(), target, this.filePath);
            return null;
        }

        List<String> literals = List.of(alias.split("\\s+"));
        if (dispatcher.findNode(literals) != null) {
            CommandAliasesMod.logger().error("[{}] {} - Existing command \"{}\": {}",
                    this.commandType, this.command.getCommandMode(), alias, this.filePath);
            return null;
        }

        LiteralArgumentBuilder<S> builder = new LiteralArgumentBuilder<>(literals.getLast()) {
            @Override
            public LiteralCommandNode<S> build() {
                // Keep the actual node so forwarding can locate this alias even inside execute ... run.
                aliasLeaf = super.build();
                return aliasLeaf;
            }
        };
        if (match.node().getCommand() != null) {
            builder.executes(context -> this.execute(dispatcher, target, context));
        }
        if (!noArguments) {
            ParsingTree tree = new ParsingTree(dispatcher, target);
            this.parsingTree = tree;
            if (match.node().getRedirect() != null) {
                builder.redirect(tree.continuation(match.node().getRedirect()));
            } else {
                for (CommandNode<S> child : match.node().getChildren()) builder.then(tree.copy(child));
            }
        }
        for (int i = literals.size() - 2; i >= 0; i--) {
            builder = this.literal(literals.get(i)).then(builder);
        }
        return builder;
    }

    /** Finish redirect continuations after every alias has been added to this dispatcher. */
    public void completeRegistration() {
        if (this.parsingTree != null) this.parsingTree.completeRedirects();
    }

    /**
     * Resolve syntax without a live source or loaded datapack resources. A target can end at an
     * incomplete prefix, but every supplied value must parse. Literal priority and argument
     * backtracking follow Brigadier; an argument's internal node name is never a literal match.
     * When wanted is supplied, stop at that node even if later input is incomplete (completion).
     */
    private Match<S> findNode(CommandDispatcher<S> dispatcher, CommandNode<S> parent, StringReader input, CommandNode<S> wanted) {
        // During registration, earlier aliases may still have unfilled continuation shells.
        if (wanted == null) parent = original(parent);
        for (CommandNode<S> child : parent.getRelevantNodes(input)) {
            StringReader reader = new StringReader(input);
            int start = reader.getCursor();
            try {
                if (child instanceof ArgumentCommandNode<S, ?> argument) {
                    argument.getType().parse(reader);
                } else {
                    child.parse(reader, new CommandContextBuilder<>(dispatcher, null, parent, start));
                }
                if (reader.getCursor() == start || (reader.canRead() && reader.peek() != ' ')) continue;
                if (child == wanted || (!reader.canRead() && wanted == null)) {
                    return new Match<>(child, reader.getCursor());
                }
                if (reader.canRead()) {
                    reader.skip();
                    CommandNode<S> next = child.getRedirect() == null ? child : child.getRedirect();
                    Match<S> match = this.findNode(dispatcher, next, reader, wanted);
                    if (match != null) return match;
                }
            } catch (CommandSyntaxException | IllegalArgumentException e) {
                CommandAliasesMod.logger().debug("[{}] Redirect target did not match node '{}': {} ({})",
                        this.commandType, child.getName(), this.filePath, e.getMessage());
            }
        }
        return null;
    }

    private int aliasEnd(CommandDispatcher<S> dispatcher, CommandContext<S> context) throws CommandSyntaxException {
        for (var node : context.getNodes()) {
            if (node.getNode() == this.aliasLeaf) return node.getRange().getEnd();
        }
        // A native redirect starts a child context, so its executable node no longer has the
        // alias in getNodes(). Locate the alias in the full input without executing modifiers.
        Match<S> match = this.findNode(dispatcher, dispatcher.getRoot(), new StringReader(context.getInput()), this.aliasLeaf);
        if (match != null) return match.end();
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
    }

    private int execute(CommandDispatcher<S> dispatcher, String target, CommandContext<S> context) throws CommandSyntaxException {
        String input = target + context.getInput().substring(this.aliasEnd(dispatcher, context));
        // Reparse with the invoker's source: fixed arguments, permissions, redirects and forks
        // belong to the original command and must be applied exactly once.
        return CommandAliasesMod.platform().executeCommand(dispatcher, input, context);
    }

    private CompletableFuture<Suggestions> suggest(CommandDispatcher<S> dispatcher, String target, CommandContext<S> context, String input) throws CommandSyntaxException {
        int end = this.aliasEnd(dispatcher, context);
        String expanded = target + input.substring(end);
        int offset = end - target.length();
        return dispatcher.getCompletionSuggestions(dispatcher.parse(expanded, context.getSource())).thenApply(suggestions -> {
            List<Suggestion> translated = new ArrayList<>();
            for (Suggestion suggestion : suggestions.getList()) {
                // A completion cannot edit the fixed part of the target through the alias.
                if (suggestion.getRange().getStart() < target.length()) continue;
                StringRange range = StringRange.between(suggestion.getRange().getStart() + offset, suggestion.getRange().getEnd() + offset);
                translated.add(suggestion instanceof IntegerSuggestion integer
                        ? new IntegerSuggestion(range, integer.getValue(), suggestion.getTooltip())
                        : new Suggestion(range, suggestion.getText(), suggestion.getTooltip()));
            }
            return Suggestions.create(input, translated);
        });
    }

    private record Match<S>(CommandNode<S> node, int end) { }

    private interface ParsingNode<S> {
        CommandNode<S> original();
    }

    @SuppressWarnings("unchecked")
    private static <S> CommandNode<S> original(CommandNode<S> node) {
        // Only our copies implement this interface, with the same source type as their origin.
        return node instanceof ParsingNode<?> copy ? (CommandNode<S>) copy.original() : node;
    }

    private static final class ParsingLiteral<S> extends LiteralCommandNode<S> implements ParsingNode<S> {
        private final LiteralCommandNode<S> original;

        private ParsingLiteral(LiteralCommandNode<S> original, Command<S> command, CommandNode<S> redirect) {
            super(original.getLiteral(), command, original.getRequirement(), redirect, null, false);
            this.original = original;
        }

        @Override public CommandNode<S> original() { return this.original; }
    }

    private static final class ParsingArgument<S, T> extends ArgumentCommandNode<S, T> implements ParsingNode<S> {
        private final ArgumentCommandNode<S, T> original;

        private ParsingArgument(ArgumentCommandNode<S, T> original, Command<S> command, CommandNode<S> redirect, SuggestionProvider<S> suggestions) {
            super(original.getName(), original.getType(), command, original.getRequirement(), redirect, null, false, suggestions);
            this.original = original;
        }

        @Override public CommandNode<S> original() { return this.original; }
    }

    private static final class ParsingRoot<S> extends RootCommandNode<S> implements ParsingNode<S> {
        private final CommandNode<S> original;

        private ParsingRoot(CommandNode<S> original) { this.original = original; }

        @Override public CommandNode<S> original() { return this.original; }
    }

    /** Copies syntax and requirements, without running native redirect/fork modifiers twice. */
    private final class ParsingTree {
        private final CommandDispatcher<S> dispatcher;
        private final String target;
        private final Map<CommandNode<S>, CommandNode<S>> copies = new IdentityHashMap<>();
        private final Map<CommandNode<S>, RootCommandNode<S>> continuations = new IdentityHashMap<>();
        private final Queue<CommandNode<S>> pendingRedirects = new ArrayDeque<>();

        private ParsingTree(CommandDispatcher<S> dispatcher, String target) {
            this.dispatcher = dispatcher;
            this.target = target;
        }

        private CommandNode<S> copy(CommandNode<S> node) {
            // Multiple aliases of execute/root redirects must share grammar identities rather
            // than repeatedly copying all earlier aliases' copies (exponential tree growth).
            node = original(node);
            CommandNode<S> existing = this.copies.get(node);
            if (existing != null) return existing;
            Command<S> executor = node.getCommand() == null ? null : context -> execute(this.dispatcher, this.target, context);
            CommandNode<S> redirect = node.getRedirect() == null ? null : this.continuation(node.getRedirect());
            CommandNode<S> result;
            if (node instanceof ArgumentCommandNode<S, ?> argumentNode) {
                result = new ParsingArgument<>(argumentNode, executor, redirect,
                        (context, suggestions) -> suggest(this.dispatcher, this.target, context, suggestions.getInput()));
            } else if (node instanceof LiteralCommandNode<S> literalNode) {
                result = new ParsingLiteral<>(literalNode, executor, redirect);
            } else {
                result = node.createBuilder().forward(redirect, null, false).executes(executor).build();
            }
            this.copies.put(node, result);
            for (CommandNode<S> child : node.getChildren()) result.addChild(this.copy(child));
            return result;
        }

        private RootCommandNode<S> continuation(CommandNode<S> targetNode) {
            targetNode = original(targetNode);
            RootCommandNode<S> result = this.continuations.get(targetNode);
            if (result == null) {
                result = new ParsingRoot<>(targetNode);
                this.continuations.put(targetNode, result);
                this.pendingRedirects.add(targetNode);
            }
            return result;
        }

        private void completeRedirects() {
            // Fill shells after constructing nodes so redirects back to an ancestor/root remain
            // redirect edges, rather than recursive child trees that cannot be serialized safely.
            while (!this.pendingRedirects.isEmpty()) {
                CommandNode<S> targetNode = this.pendingRedirects.remove();
                RootCommandNode<S> continuation = this.continuations.get(targetNode);
                for (CommandNode<S> child : targetNode.getChildren()) continuation.addChild(this.copy(child));
            }
        }
    }
}
