/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builders;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the CommandAliases Reassign Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.4.0
 * @since 0.3.0
 */
public class CommandReassignBuilder {
    private final CommandAlias command;
    private final Field literalCommandNodeLiteralField;


    public CommandReassignBuilder(CommandAlias command, Field literalCommandNodeLiteralField) {
        this.command = command;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher         CommandDispatcher
     * @param reassignCommandMap The map of reassigned commands
     * @return Command
     */
    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher, Map<String, String> reassignCommandMap) {
        if (!this.reassignCommand(this.command, dispatcher)) {
            return null;
        }
        String original = this.command.getCommand().contains(" ") ? this.command.getCommand().split(" ")[0] : this.command.getCommand();
        reassignCommandMap.put(original, this.command.getReassignTo());
        if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS) {
            return new CommandAliasesBuilder(this.command).buildCommand(dispatcher);
        } else if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
            return new CommandBuilder(this.command.getCustomCommand()).buildCommand(dispatcher);
        }
        return null;
    }

    /**
     * Try to reassign a command name to another command name.
     *
     * @param cmd        Command Alias
     * @param dispatcher CommandDispatcher
     * @return If {@code true} then it was successful, else if {@code false} failed.
     */
    private boolean reassignCommand(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) {
        if (cmd.getReassignTo().contains(" ")) {
            CommandAliasesMod.getLogger().error("Skipping \"{}\", \"{}\" must not contain spaces", cmd.getCommand(), cmd.getReassignTo());
            return false;
        }

        CommandNode<ServerCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(cmd.getCommand())).findFirst().orElse(null);

        CommandNode<ServerCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(cmd.getReassignTo())).findFirst().orElse(null);

        if (commandNode != null && commandReassignNode == null) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd.getCommand()));

            try {
                this.literalCommandNodeLiteralField.set(commandNode, cmd.getReassignTo());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                CommandAliasesMod.getLogger().error("Skipping \"{}\", couldn't modify command literal", cmd.getCommand());
                return false;
            }

            dispatcher.getRoot().addChild(commandNode);
            CommandAliasesMod.getLogger().info("Command \"{}\" has been reassigned to \"{}\"", cmd.getCommand(), cmd.getReassignTo());
            return true;
        }
        return false;
    }
}
