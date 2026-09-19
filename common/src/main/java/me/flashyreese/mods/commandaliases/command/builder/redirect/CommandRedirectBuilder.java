package me.flashyreese.mods.commandaliases.command.builder.redirect;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents the CommandAliases Redirect Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.3.0
 */
public class CommandRedirectBuilder<S extends SharedSuggestionProvider> implements CommandBuilderDelegate<S> {
    private final String filePath;
    private final RedirectCommand command;
    private final CommandType commandType;

    public CommandRedirectBuilder(String filePath, RedirectCommand command, CommandType commandType) {
        this.filePath = filePath;
        this.command = command;
        this.commandType = commandType;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        return this.parseCommand(this.command, dispatcher);
    }

    /**
     * Parses and builds command using CommandAlias
     *
     * @param cmd        CommandAlias
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    private LiteralArgumentBuilder<S> parseCommand(RedirectCommand cmd, CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> commandBuilder = null;

        String command = cmd.getCommand().trim();
        String redirectTo = cmd.getRedirectTo().trim();

        if (command.isEmpty() || redirectTo.isEmpty()) {
            CommandAliasesMod.logger().error("[{}] {} - Empty command/redirect field: {}", this.commandType, cmd.getCommandMode(), this.filePath);
            return null;
        }

        CommandNode<S> redirect = this.findRedirectNode(dispatcher, redirectTo);
        if (redirect == null) {
            CommandAliasesMod.logger().error("[{}] {} - Could not find existing command \"{}\": {}", this.commandType, cmd.getCommandMode(), redirectTo, this.filePath);
            return null;
        }

        List<String> literals = Arrays.asList(command.split(" "));

        Optional<String> topLevelCommand = literals.stream().findFirst();
        if (topLevelCommand.isPresent() && dispatcher.findNode(List.of(topLevelCommand.get())) != null && literals.size() == 1) {
            CommandAliasesMod.logger().error("[{}] {} - Existing top level command \"{}\": {}", this.commandType, cmd.getCommandMode(), command, this.filePath);
            return null;
        }

        Collections.reverse(literals);
        for (String literal : literals) {
            if (commandBuilder != null) {
                commandBuilder = this.literal(literal).then(commandBuilder);
            } else {
                if (this.command.getCommandMode() == CommandMode.COMMAND_REDIRECT) {
                    LiteralArgumentBuilder<S> builder = this.literal(literal)
                            .executes(context -> this.execute(dispatcher, redirectTo, context));

                    // Copy children from redirect node to maintain tab completion
                    redirect.getChildren().forEach(child -> builder.then(child.createBuilder().executes(context -> {
                        String input = context.getInput();
                        String args = input.substring(input.indexOf(' ') + 1);
                        String fullCommand = redirectTo + " " + args;
                        return this.execute(dispatcher, fullCommand, context);
                    })));

                    commandBuilder = builder;
                } else if (this.command.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    commandBuilder = this.literal(literal).executes(context -> this.execute(dispatcher, redirectTo, context));
                }
            }
        }
        return commandBuilder;
    }

    private CommandNode<S> findRedirectNode(CommandDispatcher<S> dispatcher, String redirectTo) {
        CommandNode<S> current = dispatcher.getRoot();
        for (String token : redirectTo.split("\\s+")) {
            CommandNode<S> literal = current.getChild(token);
            if (literal != null) {
                current = literal;
                continue;
            }

            CommandNode<S> argument = null;
            for (CommandNode<S> child : current.getChildren()) {
                if (child instanceof ArgumentCommandNode<?, ?> argumentNode && this.accepts(argumentNode, token)) {
                    argument = child;
                    break;
                }
            }
            if (argument == null) {
                return null;
            }
            current = argument;
        }
        return current == dispatcher.getRoot() ? null : current;
    }

    private boolean accepts(ArgumentCommandNode<?, ?> argumentNode, String token) {
        StringReader reader = new StringReader(token);
        try {
            argumentNode.getType().parse(reader);
            return !reader.canRead();
        } catch (CommandSyntaxException e) {
            CommandAliasesMod.logger().debug(
                    "[{}] {} - Redirect token '{}' did not match argument node '{}' ({}) while resolving '{}': {}",
                    this.commandType,
                    this.command.getCommandMode(),
                    token,
                    argumentNode.getName(),
                    argumentNode.getType().getClass().getSimpleName(),
                    this.filePath,
                    e.getMessage()
            );
            return false;
        }
    }

    private int execute(CommandDispatcher<S> dispatcher, String command, CommandContext<S> context) throws CommandSyntaxException {
        // Minecraft 26.3 executes server commands through its command execution queue.
        // The platform adapter keeps client command sources on their native dispatcher.
        return CommandAliasesMod.platform().executeCommand(dispatcher, command, context);
    }
}
