/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandChild;
import me.flashyreese.mods.commandaliases.command.impl.FunctionProcessor;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Represents the Server Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.6.0
 * @since 0.5.0
 */
public class ServerCustomCommandBuilder extends AbstractCustomCommandBuilder<ServerCommandSource> {
    private final FunctionProcessor functionProcessor;
    public ServerCustomCommandBuilder(CustomCommand commandAliasParent, CommandRegistryAccess registryAccess, AbstractDatabase<byte[], byte[]> database) {
        super(commandAliasParent, registryAccess, database);
        this.functionProcessor = new FunctionProcessor(database);
    }

    /**
     * Executes command action
     *
     * @param actions          List of command actions
     * @param message          Message
     * @param dispatcher       The command dispatcher
     * @param context          The command context
     * @param currentInputList User input list
     * @return Command execution state
     */
    @Override
    protected int executeAction(List<CustomCommandAction> actions, String message, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, List<String> currentInputList) {
        if (actions == null || actions.isEmpty()) {
            String formatString = this.formatString(context, currentInputList, message);
            context.getSource().sendFeedback(Text.literal(formatString), CommandAliasesMod.options().debugSettings.broadcastToOps);
            return Command.SINGLE_SUCCESS;
        } else {
            if (message == null || message.isEmpty()) {
                return this.executeCommand(actions, dispatcher, context, currentInputList);
            } else {
                int state = this.executeCommand(actions, dispatcher, context, currentInputList);
                String formatString = this.formatString(context, currentInputList, message);
                context.getSource().sendFeedback(Text.literal(formatString), CommandAliasesMod.options().debugSettings.broadcastToOps);
                return state;
            }
        }
    }

    /**
     * Executes command in command action
     *
     * @param actions          List of command actions
     * @param dispatcher       The command dispatcher
     * @param context          The command context
     * @param currentInputList User input list
     * @return Command execution state
     */
    @Override
    protected int executeCommand(List<CustomCommandAction> actions, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, List<String> currentInputList) {
        AtomicInteger executeState = new AtomicInteger();
        Thread thread = new Thread(() -> {
            try {
                if (actions != null) {
                    executeState.set(this.processActions(actions, dispatcher, context, currentInputList));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                String output = e.getLocalizedMessage();
                context.getSource().sendFeedback(Text.literal(output), CommandAliasesMod.options().debugSettings.broadcastToOps);
            }
        });
        thread.setName("Command Aliases");
        thread.start();
        return executeState.get();
    }

    public int processActions(List<CustomCommandAction> actions, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, List<String> currentInputList) throws InterruptedException {
        int state = 0;
        long start = System.nanoTime();
        for (CustomCommandAction action : actions) {
            if (action.getCommand() != null && action.getCommandType() != null) {
                long startFormat = System.nanoTime();
                String actionCommand = this.formatString(context, currentInputList, action.getCommand());
                long endFormat = System.nanoTime();
                if (CommandAliasesMod.options().debugSettings.showProcessingTime) {
                    CommandAliasesMod.getLogger().info("======================================================");
                    CommandAliasesMod.getLogger().info("Original Action Command: {}", action.getCommand());
                    CommandAliasesMod.getLogger().info("Original Action Command Type: {}", action.getCommandType());
                    CommandAliasesMod.getLogger().info("Post Processed Action Command: {}", actionCommand);
                    CommandAliasesMod.getLogger().info("Process time: " + (endFormat-startFormat) + "ns");
                    CommandAliasesMod.getLogger().info("======================================================");
                }
                try {
                    if (action.getCommandType() == CommandType.CLIENT) {
                        state = dispatcher.execute(actionCommand, context.getSource());
                    } else if (action.getCommandType() == CommandType.SERVER) {
                        state = dispatcher.execute(actionCommand, context.getSource().getServer().getCommandSource());
                    }
                } catch (CommandSyntaxException e) {
                    if (CommandAliasesMod.options().debugSettings.debugMode) {
                        CommandAliasesMod.getLogger().error("Failed to process command");
                        CommandAliasesMod.getLogger().error("Original Action Command: {}", action.getCommand());
                        CommandAliasesMod.getLogger().error("Original Action Command Type: {}", action.getCommandType());
                        CommandAliasesMod.getLogger().error("Post Processed Action Command: {}", actionCommand);
                        String output = e.getLocalizedMessage();
                        context.getSource().sendFeedback(Text.literal(output), CommandAliasesMod.options().debugSettings.broadcastToOps);
                    }
                    e.printStackTrace();
                }
                if (state != Command.SINGLE_SUCCESS) {
                    if (action.getUnsuccessfulMessage() != null) {
                        String message = this.formatString(context, currentInputList, action.getUnsuccessfulMessage());
                        context.getSource().sendFeedback(Text.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
                    }
                    if (action.getUnsuccessfulActions() != null && !action.getUnsuccessfulActions().isEmpty()) {
                        state = this.processActions(action.getUnsuccessfulActions(), dispatcher, context, currentInputList);
                    }
                    if (action.isRequireSuccess()) {
                        break;
                    }
                } else {
                    if (action.getSuccessfulMessage() != null) {
                        String message = this.formatString(context, currentInputList, action.getSuccessfulMessage());
                        context.getSource().sendFeedback(Text.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
                    }
                }
            }
            if (action.getMessage() != null) {
                String message = this.formatString(context, currentInputList, action.getMessage());
                context.getSource().sendFeedback(Text.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
            }
            if (action.getSleep() != null) {
                String formattedTime = this.formatString(context, currentInputList, action.getSleep());
                int time = Integer.parseInt(formattedTime);
                Thread.sleep(time);
            }
        }
        long end = System.nanoTime();
        if (CommandAliasesMod.options().debugSettings.showProcessingTime) {
            CommandAliasesMod.getLogger().info("======================================================");
            CommandAliasesMod.getLogger().info("Command Actions");
            CommandAliasesMod.getLogger().info("Process time: " + (end-start) + "ns");
            CommandAliasesMod.getLogger().info("======================================================");
        }
        return state;
    }

    /**
     * Method to format string(command or messages) with user input map.
     *
     * @param context          The command context
     * @param currentInputList User input map with functions
     * @param string           Input string
     * @return Formatted string
     */
    @Override
    protected String formatString(CommandContext<ServerCommandSource> context, List<String> currentInputList, String string) {
        Map<String, String> resolvedInputMap = new Object2ObjectOpenHashMap<>();
        //Todo: valid if getInputString returns null and if it does catch it :>
        currentInputList.forEach(input -> resolvedInputMap.put(input, this.argumentTypeMapper.getInputString(context, input)));
        //Input Map
        //Todo: track replaced substring indexes to prevent replacing previously replaced
        for (Map.Entry<String, String> entry : resolvedInputMap.entrySet()) { //fixme: A bit of hardcoding here
            string = string.replace(String.format("{{%s}}", entry.getKey()), entry.getValue());

            for (Map.Entry<String, Function<String, String>> entry2 : this.formattingTypeMap.getFormatTypeMap().entrySet()) {
                String tempString = String.format("{{%s@%s}}", entry.getKey(), entry2.getKey());
                if (string.contains(tempString)) {
                    String newString = entry2.getValue().apply(entry.getValue());
                    string = string.replace(tempString, newString);
                }
            }
        }

        // Function processor
        string = this.functionProcessor.processFunctions(string, context.getSource());

        string = string.trim();
        return string;
    }

    /**
     * Builds parent ArgumentBuilder
     *
     * @param dispatcher The command dispatcher
     * @return ArgumentBuilder
     */
    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> buildCommandParent(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = this.literal(this.commandAliasParent.getParent());

        if (this.commandAliasParent.getPermission() > 0 && this.commandAliasParent.getPermission() <= 4) {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getParent(), this.commandAliasParent.getPermission()));
        } else {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getParent(), true));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeAction(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new ObjectArrayList<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CustomCommandChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new ObjectArrayList<>(), this.commandAliasParent.getParent());
                if (subArgumentBuilder != null) {
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
    }

    /**
     * Builds child commands and determine if optional or not.
     *
     * @param child      CommandChild
     * @param dispatcher The command dispatcher
     * @param inputs     User input list
     * @param permission Permission String
     * @return ArgumentBuilder
     */
    protected ArgumentBuilder<ServerCommandSource, ?> buildCommandChild(CustomCommandChild child, CommandDispatcher<ServerCommandSource> dispatcher, List<String> inputs, String permission) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = this.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeMapper.contains(child.getArgumentType())) {
                argumentBuilder = this.argument(child.getChild(), this.argumentTypeMapper.getValue(child.getArgumentType()));
                inputs.add(child.getChild());
            } else {
                CommandAliasesMod.getLogger().error("Invalid Argument Type: {}", child.getArgumentType());
            }
        }
        if (argumentBuilder != null) {
            String permissionString = permission + "." + child.getChild();
            // Assign permission
            if (child.getPermission() > 0 && child.getPermission() <= 4) {
                argumentBuilder = argumentBuilder.requires(Permissions.require(permissionString, child.getPermission()));
            } else {
                argumentBuilder = argumentBuilder.requires(Permissions.require(permissionString, true));
            }

            if (child.isOptional()) {
                argumentBuilder = argumentBuilder.executes(context -> {
                    //Execution action here
                    return this.executeAction(child.getActions(), child.getMessage(), dispatcher, context, inputs);
                });
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CustomCommandChild subChild : child.getChildren()) {
                    ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new ObjectArrayList<>(inputs), permissionString);
                    if (subArgumentBuilder != null) {
                        argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                    }
                }
            }
        }
        return argumentBuilder;
    }
}
