/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import net.minecraft.command.CommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the CommandAliases Reassign Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.3.0
 */
public abstract class AbstractReassignCommandBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
    protected final CommandAlias command;
    private final Field literalCommandNodeLiteralField;
    protected final Map<String, String> reassignCommandMap;

    public AbstractReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, Map<String, String> reassignCommandMap) {
        this.command = command;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
        this.reassignCommandMap = reassignCommandMap;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        return this.reassignCommand(dispatcher);
    }

    protected abstract LiteralArgumentBuilder<S> reassignCommand(CommandDispatcher<S> dispatcher);

    /**
     * Try to reassign a command name to another command name.
     *
     * @param cmd        Command Alias
     * @param dispatcher CommandDispatcher
     * @return If {@code true} then it was successful, else if {@code false} failed.
     */
    protected boolean reassignCommand(CommandAlias cmd, CommandDispatcher<S> dispatcher) {
        if (cmd.getReassignCommand() == null) {
            CommandAliasesMod.getLogger().error("[{}] {} - Skipping reassignment, missing declaration!", cmd.getCommandType(), cmd.getCommandMode());
            return false;
        }

        String command = cmd.getReassignCommand().getCommand().trim();
        String reassignTo = cmd.getReassignCommand().getReassignTo().trim();

        if (command.contains(" ")) {
            CommandAliasesMod.getLogger().error("[{}] {} - \"command\" field must not contain spaces, skipping \"{}\".", cmd.getCommandType(), cmd.getCommandMode(), command);
            return false;
        }

        if (reassignTo.contains(" ")) {
            CommandAliasesMod.getLogger().error("[{}] {} - \"reassignTo\" field must not contain spaces, skipping \"{}\".", cmd.getCommandType(), cmd.getCommandMode(), reassignTo);
            return false;
        }

        CommandNode<S> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(command)).findFirst().orElse(null);

        CommandNode<S> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(reassignTo)).findFirst().orElse(null);

        if (commandNode != null && commandReassignNode == null) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(command));

            try {
                this.literalCommandNodeLiteralField.set(commandNode, reassignTo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                CommandAliasesMod.getLogger().error("[{}] {} - Failed to modify command literal \"{}\", skipping.", cmd.getCommandType(), cmd.getCommandMode(), command);
                return false;
            }

            dispatcher.getRoot().addChild(commandNode);

            CommandAliasesMod.getLogger().info("[{}] {} - Command \"{}\" has been reassigned to \"{}\"", cmd.getCommandType(), cmd.getCommandMode(), command, reassignTo);
            return true;
        }
        return false;
    }
}
