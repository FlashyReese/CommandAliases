/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.classtool.FormattingTypeMap;
import me.flashyreese.mods.commandaliases.classtool.impl.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.CommandAction;
import me.flashyreese.mods.commandaliases.command.CommandChild;
import me.flashyreese.mods.commandaliases.command.CommandParent;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Represents the Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.4.2
 * @since 0.4.0
 */
public class CommandBuilder {
    private final CommandParent commandAliasParent;

    private final ArgumentTypeManager argumentTypeManager = new ArgumentTypeManager();
    private final FormattingTypeMap formattingTypeMap = new FormattingTypeMap();

    public CommandBuilder(CommandParent commandAliasParent) {
        this.commandAliasParent = commandAliasParent;
    }

    /**
     * Builds Command for Dispatcher to register.
     *
     * @param dispatcher The command dispatcher
     * @return ArgumentBuilder
     */
    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        return this.buildCommandParent(dispatcher);
    }

    /**
     * Builds parent ArgumentBuilder
     *
     * @param dispatcher The command dispatcher
     * @return ArgumentBuilder
     */
    private LiteralArgumentBuilder<ServerCommandSource> buildCommandParent(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal(this.commandAliasParent.getParent());
        if (this.commandAliasParent.getPermission() < 0 && this.commandAliasParent.getPermission() >= 4) {
            argumentBuilder = argumentBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(this.commandAliasParent.getPermission()));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeAction(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new ObjectArrayList<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CommandChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new ObjectArrayList<>());
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
     * @return ArgumentBuilder
     */
    private ArgumentBuilder<ServerCommandSource, ?> buildCommandChild(CommandChild child, CommandDispatcher<ServerCommandSource> dispatcher, List<String> inputs) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = CommandManager.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeManager.contains(child.getArgumentType())) {
                argumentBuilder = CommandManager.argument(child.getChild(), this.argumentTypeManager.getValue(child.getArgumentType()));
                inputs.add(child.getChild());
            } else {
                CommandAliasesMod.getLogger().warn("Invalid Argument Type: {}", child.getArgumentType());
            }
        }
        if (argumentBuilder != null) {
            // Assign permission
            if (child.getPermission() < 0 && child.getPermission() >= 4) {
                argumentBuilder = argumentBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(child.getPermission()));
            }

            if (child.isOptional()) {
                argumentBuilder = argumentBuilder.executes(context -> {
                    //Execution action here
                    return this.executeAction(child.getActions(), child.getMessage(), dispatcher, context, inputs);
                });
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CommandChild subChild : child.getChildren()) {
                    ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new ObjectArrayList<>(inputs));
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
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
    private int executeAction(List<CommandAction> actions, String message, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, List<String> currentInputList) {
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
    private int executeCommand(List<CommandAction> actions, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, List<String> currentInputList) {
        AtomicInteger executeState = new AtomicInteger();
        Thread thread = new Thread(() -> {
            try {
                if (actions != null) {
                    for (CommandAction action : actions) {
                        if (action.getCommand() != null) {
                            String actionCommand = this.formatString(context, currentInputList, action.getCommand());
                            if (action.getCommandType() == CommandType.CLIENT) {
                                executeState.set(dispatcher.execute(actionCommand, context.getSource()));
                            } else if (action.getCommandType() == CommandType.SERVER) {
                                executeState.set(dispatcher.execute(actionCommand, context.getSource().getServer().getCommandSource()));
                            }
                        }
                        if (action.getMessage() != null) {
                            String message = this.formatString(context, currentInputList, action.getMessage());
                            context.getSource().sendFeedback(new LiteralText(message), true);
                        }
                        if (action.getSleep() != null) {
                            String formattedTime = this.formatString(context, currentInputList, action.getSleep());
                            int time = Integer.parseInt(formattedTime);
                            Thread.sleep(time);
                        }
                    }
                }
            } catch (CommandSyntaxException | InterruptedException e) {
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
    private String formatString(CommandContext<ServerCommandSource> context, List<String> currentInputList, String string) {
        Map<String, String> resolvedInputMap = new Object2ObjectOpenHashMap<>();
        //Todo: valid if getInputString returns null and if it does catch it :>
        currentInputList.forEach(input -> resolvedInputMap.put(input, this.argumentTypeManager.getInputString(context, input)));
        //Functions fixme: more hardcoding
        string = string.replace("$executor_name()", context.getSource().getName());
        //Input Map
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
