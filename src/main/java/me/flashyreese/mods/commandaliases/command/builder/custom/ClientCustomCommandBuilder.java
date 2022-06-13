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
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.db.AbstractDatabase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Client Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.6.0
 * @since 0.5.0
 */
public class ClientCustomCommandBuilder extends AbstractCustomCommandBuilder<FabricClientCommandSource> {

    public ClientCustomCommandBuilder(CustomCommand commandAliasParent, CommandRegistryAccess registryAccess, AbstractDatabase<byte[], byte[]> database) {
        super(commandAliasParent, registryAccess, database);
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
    protected int executeAction(List<CustomCommandAction> actions, String message, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandContext<FabricClientCommandSource> context, List<String> currentInputList) {
        if (actions == null || actions.isEmpty()) {
            String formatString = this.formatString(context, currentInputList, message);
            context.getSource().sendFeedback(Text.literal(formatString));
            return Command.SINGLE_SUCCESS;
        } else if (message == null || message.isEmpty()) {
            return this.executeCommand(actions, dispatcher, context, currentInputList);
        } else {
            int state = this.executeCommand(actions, dispatcher, context, currentInputList);
            String formatString = this.formatString(context, currentInputList, message);
            context.getSource().sendFeedback(Text.literal(formatString));
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
    protected int executeCommand(List<CustomCommandAction> actions, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandContext<FabricClientCommandSource> context, List<String> currentInputList) {
        Thread thread = new Thread(() -> {
            try {
                if (actions != null) {
                    for (CustomCommandAction action : actions) {
                        if (action.getCommand() != null) {
                            String actionCommand = this.formatString(context, currentInputList, action.getCommand());

                            context.getSource().getPlayer().sendChatMessage("/" + actionCommand);
                        }
                        if (action.getMessage() != null) {
                            String message = this.formatString(context, currentInputList, action.getMessage());
                            context.getSource().sendFeedback(Text.literal(message));
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
                context.getSource().sendFeedback(Text.literal(output));
            }
        });
        thread.setName("Command Aliases");
        thread.start();
        return Command.SINGLE_SUCCESS;//executeState.get();
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
    protected String formatString(CommandContext<FabricClientCommandSource> context, List<String> currentInputList, String string) {
        Map<String, String> resolvedInputMap = new Object2ObjectOpenHashMap<>();
        //Todo: valid if getInputString returns null and if it does catch it :>
        currentInputList.forEach(input -> resolvedInputMap.put(input, this.argumentTypeManager.getInputString(context, input)));
        //Functions fixme: more hardcoding
        string = string.replace("$executor_name()", context.getSource().getPlayer().getName().getString());
        string = string.replace("$executor_pos_x()", String.valueOf(context.getSource().getPlayer().getX()));
        string = string.replace("$executor_pos_y()", String.valueOf(context.getSource().getPlayer().getY()));
        string = string.replace("$executor_pos_z()", String.valueOf(context.getSource().getPlayer().getZ()));
        string = string.replace("$executor_yaw()", String.valueOf(context.getSource().getPlayer().getYaw()));
        string = string.replace("$executor_pitch()", String.valueOf(context.getSource().getPlayer().getPitch()));
        string = string.replace("$executor_block_pos_x()", String.valueOf(context.getSource().getPlayer().getBlockX()));
        string = string.replace("$executor_block_pos_y()", String.valueOf(context.getSource().getPlayer().getBlockY()));
        string = string.replace("$executor_block_pos_z()", String.valueOf(context.getSource().getPlayer().getBlockZ()));
        string = string.replace("$executor_dimension()", String.valueOf(context.getSource().getPlayer().getWorld().getRegistryKey().getValue()));
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

        //Variable Mapping
        Matcher matcher = Pattern.compile("\\{\\{(?<var>\\w+.*?)}}").matcher(string);
        while (matcher.find()) {
            String var = matcher.group("var");
            byte[] value = this.database.read(var.getBytes(StandardCharsets.UTF_8));
            if (value != null)
                string = string.replace("{{" + var + "}}", new String(value, StandardCharsets.UTF_8));
        }

        string = string.trim();
        return string;
    }
}
