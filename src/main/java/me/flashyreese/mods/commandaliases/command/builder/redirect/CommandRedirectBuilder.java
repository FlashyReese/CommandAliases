/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.redirect;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final CommandAlias command;
    private final CommandType commandType;

    public CommandRedirectBuilder(CommandAlias command, CommandType commandType) {
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
    private LiteralArgumentBuilder<S> parseCommand(CommandAlias cmd, CommandDispatcher<S> dispatcher) {
        if (cmd.getRedirectCommand() == null) {
            CommandAliasesMod.getLogger().error("[{}] {} - Skipping redirection, missing declaration!", this.commandType, cmd.getCommandMode());
            return null;
        }

        LiteralArgumentBuilder<S> commandBuilder = null;

        String command = cmd.getRedirectCommand().getCommand().trim();
        String redirectTo = cmd.getRedirectCommand().getRedirectTo().trim();

        CommandNode<S> redirect = dispatcher.findNode(Lists.newArrayList(redirectTo.split(" ")));
        if (redirect == null) {
            CommandAliasesMod.getLogger().error("[{}] {} - Could not find existing command \"{}\".", this.commandType, cmd.getCommandMode(), redirectTo);
            return null;
        }

        List<String> literals = Arrays.asList(command.split(" "));
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
