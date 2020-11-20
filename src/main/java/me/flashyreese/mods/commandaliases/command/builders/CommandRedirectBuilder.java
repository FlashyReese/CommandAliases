/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builders;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the CommandAliases Redirect Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.3.0
 * @since 0.3.0
 */
public class CommandRedirectBuilder {
    private final CommandAlias command;

    public CommandRedirectBuilder(CommandAlias command) {
        this.command = command;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        return this.parseCommand(this.command, dispatcher);
    }

    /**
     * Parses and builds command using CommandAlias
     *
     * @param command    CommandAlias
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    private LiteralArgumentBuilder<ServerCommandSource> parseCommand(CommandAlias command, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = null;
        CommandNode<ServerCommandSource> redirect = dispatcher.findNode(Lists.newArrayList(command.getRedirectTo().trim().split(" ")));
        if (redirect == null) {
            CommandAliasesMod.getLogger().error("Could not find existing command \"{}\"", command.getRedirectTo().trim());
            return null;
        }

        List<String> literals = Arrays.asList(command.getCommand().trim().split(" "));
        Collections.reverse(literals);
        for (String literal : literals) {
            if (commandBuilder != null) {
                commandBuilder = CommandManager.literal(literal).then(commandBuilder);
            } else {
                if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN){
                    commandBuilder = CommandManager.literal(literal).redirect(redirect);
                }else{
                    commandBuilder = CommandManager.literal(literal).executes(redirect.getCommand());
                }
            }
        }
        return commandBuilder;
    }
}
