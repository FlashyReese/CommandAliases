/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

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
 * @version 0.5.0
 * @since 0.5.0
 */
public class ServerCustomCommandBuilder extends AbstractCustomCommandBuilder<ServerCommandSource> {
    public ServerCustomCommandBuilder(CustomCommand commandAliasParent) {
        super(commandAliasParent);
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
        if ((actions == null || actions.isEmpty()) && (message != null || !message.isEmpty())) {
            String formatString = this.formatString(context, currentInputList, message);
            context.getSource().sendFeedback(new LiteralText(formatString), true);
            return Command.SINGLE_SUCCESS;
        } else if ((actions != null || !actions.isEmpty()) && (message == null || message.isEmpty())) {
            return this.executeCommand(actions, dispatcher, context, currentInputList);
        } else {
            int state = this.executeCommand(actions, dispatcher, context, currentInputList);
            String formatString = this.formatString(context, currentInputList, message);
            context.getSource().sendFeedback(new LiteralText(formatString), true);
            return state;
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
                    for (CustomCommandAction action : actions) {
                        if (action.getCommand() != null) {
                            String actionCommand = this.formatString(context, currentInputList, action.getCommand());
                            if (action.getCommandType() == CommandType.CLIENT) {
                                try {
                                    executeState.set(dispatcher.execute(actionCommand, context.getSource()));
                                } catch (CommandSyntaxException e) {
                                    e.printStackTrace();
                                    String output = e.getLocalizedMessage();
                                    context.getSource().sendFeedback(new LiteralText(output), true);
                                }
                            } else if (action.getCommandType() == CommandType.SERVER) {
                                try {
                                    executeState.set(dispatcher.execute(actionCommand, context.getSource().getServer().getCommandSource()));
                                } catch (CommandSyntaxException e) {
                                    e.printStackTrace();
                                    String output = e.getLocalizedMessage();
                                    context.getSource().sendFeedback(new LiteralText(output), true);
                                }
                            }
                        }
                        if (action.getMessage() != null) {
                            String message = this.formatString(context, currentInputList, action.getMessage());
                            context.getSource().sendFeedback(new LiteralText(message), true);
                        }
                        if (action.isRequireSuccess() && executeState.get() != 1){
                            break;
                        }
                        if (action.getSleep() != null) {
                            String formattedTime = this.formatString(context, currentInputList, action.getSleep());
                            int time = Integer.parseInt(formattedTime);
                            Thread.sleep(time);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                String output = e.getLocalizedMessage();
                context.getSource().sendFeedback(new LiteralText(output), true);
            }
        });
        thread.setName("Command Aliases");
        thread.start();
        return executeState.get();
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
        currentInputList.forEach(input -> resolvedInputMap.put(input, this.argumentTypeManager.getInputString(context, input)));
        //Functions fixme: more hardcoding
        string = string.replace("$executor_name()", context.getSource().getName());
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

        string = string.trim();
        return string;
    }
}
