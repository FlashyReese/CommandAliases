package me.flashyreese.mods.commandaliases.command.builder.redirect;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;
import net.minecraft.command.CommandSource;

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
public class CommandRedirectBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
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
            CommandAliasesMod.logger().error("[{}] {} - Empty command/redirect field.", this.commandType, cmd.getCommandMode());
            return null;
        }

        CommandNode<S> redirect = dispatcher.findNode(Lists.newArrayList(redirectTo.split(" ")));
        if (redirect == null) {
            CommandAliasesMod.logger().error("[{}] {} - Could not find existing command \"{}\".", this.commandType, cmd.getCommandMode(), redirectTo);
            return null;
        }

        List<String> literals = Arrays.asList(command.split(" "));

        Optional<String> topLevelCommand = literals.stream().findFirst();
        if (topLevelCommand.isPresent() && dispatcher.findNode(List.of(topLevelCommand.get())) != null) {
            CommandAliasesMod.logger().error("[{}] {} - Existing top level command \"{}\".", this.commandType, cmd.getCommandMode(), command);
            return null;
        }

        Collections.reverse(literals);
        for (String literal : literals) {
            if (commandBuilder != null) {
                commandBuilder = this.literal(literal).then(commandBuilder);
            } else {
                if (this.command.getCommandMode() == CommandMode.COMMAND_REDIRECT) {
                    commandBuilder = this.literal(literal).redirect(redirect);
                } else if (this.command.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    commandBuilder = this.literal(literal).executes(redirect.getCommand());
                }
            }
        }
        return commandBuilder;
    }
}
