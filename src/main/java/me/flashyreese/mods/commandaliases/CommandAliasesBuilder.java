/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.classtool.exec.MinecraftClassTool;
import me.flashyreese.mods.commandaliases.classtool.impl.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the CommandAliases Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.1.3
 * @since 0.1.3
 */
public class CommandAliasesBuilder {

    private static final Pattern REQUIRED_COMMAND_ALIAS_HOLDER = Pattern.compile("\\{(?<classTool>\\w+)(::(?<method>[\\w:]+))?(#(?<variableName>\\w+))?(@(?<formattingType>\\w+))?}");
    private static final Pattern OPTIONAL_COMMAND_ALIAS_HOLDER = Pattern.compile("\\[(?<classTool>\\w+)(::(?<method>[\\w:]+))?(#(?<variableName>\\w+))?(@(?<formattingType>\\w+))?]");

    private final CommandAlias command;
    private final List<CommandAliasesHolder> commandAliasesHolders = new ArrayList<>();

    private final Map<String, ClassTool<?>> classToolMap = new HashMap<>();
    private final FormattingTypeMap formattingTypeMap;

    public CommandAliasesBuilder(CommandAlias command) {
        this.command = command;
        this.commandAliasesHolders.addAll(this.buildHolders(this.findHolders(command.getCommand())));

        this.classToolMap.put("arg", new ArgumentTypeManager());
        this.classToolMap.put("this", new MinecraftClassTool());
        this.formattingTypeMap = new FormattingTypeMap();
    }

    private List<String> findHolders(String command) {
        List<String> holders = new ArrayList<>();
        Matcher m = REQUIRED_COMMAND_ALIAS_HOLDER.matcher(command);
        while (m.find()) {
            holders.add(m.group());
        }
        return holders;
    }

    private List<CommandAliasesHolder> buildHolders(List<String> holders) {
        List<CommandAliasesHolder> commandAliasesHolders = new ArrayList<>();
        holders.forEach(holder -> {
            commandAliasesHolders.add(new CommandAliasesHolder(holder));
        });
        return commandAliasesHolders;
    }


    private Map<String, String> getHolderInputMap(CommandContext<ServerCommandSource> context) {
        Map<String, String> inputMap = new HashMap<>();

        for (CommandAliasesHolder holder : this.commandAliasesHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {
                if (this.classToolMap.get(holder.getClassTool()).contains(holder.getMethod())) {
                    String key = holder.getVariableName();
                    String value = this.classToolMap.get(holder.getClassTool()).getValue(context, holder);

                    if (!holder.isRequired() && value == null){
                        //Optional don't put in map
                        continue;
                    }

                    if (holder.getFormattingType() != null) {
                        if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                            inputMap.put(key, this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                        } else {
                            CommandAliasesMod.getLogger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                            inputMap.put(key, value);
                        }
                    } else {
                        inputMap.put(key, value);
                    }
                } else {
                    CommandAliasesMod.getLogger().error("No method found for \"{}\"", holder.getHolder());
                }
            } else {
                CommandAliasesMod.getLogger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }

        return inputMap;
    }

    private String formatSubCommandOrMessage(CommandContext<ServerCommandSource> context, String text) {
        Map<String, String> inputMap = this.getHolderInputMap(context);
        String formattedText = text;

        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            String format = String.format("{%s}", entry.getKey());
            if (formattedText.contains(format)) {
                formattedText = formattedText.replace(format, entry.getValue());
            }
        }

        Map<String, String> newInputMap = new HashMap<>();
        List<CommandAliasesHolder> textHolders = this.buildHolders(this.findHolders(formattedText));
        for (CommandAliasesHolder holder : textHolders) {
            String value = null;

            if (inputMap.containsKey(holder.getVariableName())) {
                value = inputMap.get(holder.getVariableName());
            } else if (this.classToolMap.containsKey(holder.getClassTool())) {
                if (this.classToolMap.get(holder.getClassTool()).contains(holder.getMethod())) {
                    value = this.classToolMap.get(holder.getClassTool()).getValue(context, holder);
                } else {
                    CommandAliasesMod.getLogger().warn("No method found for \"{}\"", holder.getHolder());
                }
            }

            if (value == null) {
                CommandAliasesMod.getLogger().warn("Unable to find a value for \"{}\", skipping", holder.getHolder());
                continue;
            }


            if (holder.getFormattingType() != null) {
                if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                    newInputMap.put(holder.getHolder(), this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                } else {
                    CommandAliasesMod.getLogger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                    newInputMap.put(holder.getHolder(), value);
                }
            } else {
                newInputMap.put(holder.getHolder(), value);
            }
        }
        for (Map.Entry<String, String> entry : newInputMap.entrySet()) {
            if (formattedText.contains(entry.getKey())) {
                formattedText = formattedText.replace(entry.getKey(), entry.getValue());
            }
        }

        return formattedText;
    }


    private int executeCommandAliases(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context) {
        AtomicInteger execute = new AtomicInteger();
        Thread thread = new Thread(() -> {
            try {
                for (CommandAlias subCmd : cmd.getExecution()) {
                    String subCommand = formatSubCommandOrMessage(context, subCmd.getCommand());
                    if (subCmd.getType() == CommandType.CLIENT) {
                        execute.set(dispatcher.execute(subCommand, context.getSource()));
                    } else if (subCmd.getType() == CommandType.SERVER) {
                        execute.set(dispatcher.execute(subCommand, context.getSource().getMinecraftServer().getCommandSource()));
                    }
                    if (subCmd.getMessage() != null) {
                        String message = formatSubCommandOrMessage(context, subCmd.getMessage());
                        context.getSource().sendFeedback(new LiteralText(message), true);
                    }
                    if (subCmd.getSleep() != null) {
                        String formattedTime = subCmd.getSleep();
                        int time = Integer.parseInt(formattedTime);
                        Thread.sleep(time);
                    }
                }
            } catch (CommandSyntaxException | InterruptedException e) {
                String output = e.getLocalizedMessage();
                context.getSource().sendFeedback(new LiteralText(output), true);
            }
            if (cmd.getMessage() != null) {
                String message = formatSubCommandOrMessage(context, cmd.getMessage());
                context.getSource().sendFeedback(new LiteralText(message), true);
            }
        });
        thread.setName("Command Aliases");
        thread.start();
        return execute.get();
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        return this.parseCommand(this.command, dispatcher);
    }

    private LiteralArgumentBuilder<ServerCommandSource> parseCommand(CommandAlias command, CommandDispatcher<ServerCommandSource> dispatcher) { //Todo: Optional Building is borked AF
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = null;
        List<String> holders = this.findHolders(command.getCommand());
        String newCommand = command.getCommand();

        for (String holder : holders) {
            newCommand = newCommand.replace(holder, "");
        }
        newCommand = newCommand.trim();

        if (newCommand.contains(" ") && holders.size() == 0) {
            List<String> literals = Arrays.asList(newCommand.split(" "));
            Collections.reverse(literals);
            for (String literal : literals) {
                if (commandBuilder != null) {
                    commandBuilder = CommandManager.literal(literal).then(commandBuilder);
                } else {
                    commandBuilder = CommandManager.literal(literal).executes(context -> this.executeCommandAliases(command, dispatcher, context));
                }
            }
        } else if (newCommand.contains(" ") && holders.size() != 0) {
            ArgumentBuilder<ServerCommandSource, ?> arguments = this.parseArguments(command, dispatcher);
            List<String> literals = Arrays.asList(newCommand.split(" "));
            Collections.reverse(literals);
            for (String literal : literals) {
                if (commandBuilder != null) {
                    commandBuilder = CommandManager.literal(literal).then(commandBuilder);
                } else {
                    if (arguments != null) {
                        commandBuilder = CommandManager.literal(literal).then(arguments);
                    } else {
                        commandBuilder = CommandManager.literal(literal).executes(context -> this.executeCommandAliases(command, dispatcher, context));
                    }
                }
            }

        } else if (!newCommand.contains(" ") && holders.size() == 0) {
            commandBuilder = CommandManager.literal(newCommand).executes(context -> this.executeCommandAliases(command, dispatcher, context));
            ;
        } else if (!newCommand.contains(" ") && holders.size() != 0) {
            ArgumentBuilder<ServerCommandSource, ?> arguments = parseArguments(command, dispatcher);
            if (arguments != null) {
                commandBuilder = CommandManager.literal(newCommand).then(arguments);
            } else {
                commandBuilder = CommandManager.literal(newCommand).executes(context -> this.executeCommandAliases(command, dispatcher, context));
            }
        }
        //commandBuilder = commandBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(command.getPermissionLevel()));
        return commandBuilder;
    }

    private ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) { // Todo: Optional Arguments
        List<CommandAliasesHolder> commandHolders = this.buildHolders(findHolders(cmd.getCommand()));
        ArgumentBuilder<ServerCommandSource, ?> arguments = null;
        Collections.reverse(commandHolders);
        for (CommandAliasesHolder holder : commandHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {//Fixme: Casting dangerous, ClassTools Types should solve this
                ClassTool<?> tool = this.classToolMap.get(holder.getClassTool());
                if (tool instanceof ArgumentTypeManager) {
                    if (tool.contains(holder.getMethod())) {
                        if (arguments != null) {
                            arguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeManager) tool).getValue(holder.getMethod()).getArgumentType()).then(arguments);
                        } else {
                            arguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeManager) tool).getValue(holder.getMethod()).getArgumentType()).executes(context -> executeCommandAliases(cmd, dispatcher, context));
                        }
                    }
                }
            } else {
                CommandAliasesMod.getLogger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }
        return arguments;
    }

    public static class CommandAliasesHolder {
        private String holder;

        private String classTool;
        private String method;
        private String variableName;
        private String formattingType;

        private boolean required;

        public CommandAliasesHolder(String holder) {
            this.holder = holder;
            this.findVariables();
        }

        private void findVariables() {
            Matcher requiredMatcher = CommandAliasesBuilder.REQUIRED_COMMAND_ALIAS_HOLDER.matcher(this.holder);
            Matcher optionalMatcher = CommandAliasesBuilder.OPTIONAL_COMMAND_ALIAS_HOLDER.matcher(this.holder);
            if (requiredMatcher.matches()) {
                String classTool = requiredMatcher.group("classTool");
                String method = requiredMatcher.group("method");
                String variableName = requiredMatcher.group("variableName");
                String formattingType = requiredMatcher.group("formattingType");

                this.updateVariables(classTool, method, variableName, formattingType, true);
                //System.out.println(String.format("Command: %s ClassTool: %s Method: %s VariableName: %s FormattingType: %s", this.holder, this.classTool, this.method, this.variableName, this.formattingType));
            } else if (optionalMatcher.matches()) {
                String classTool = optionalMatcher.group("classTool");
                String method = optionalMatcher.group("method");
                String variableName = optionalMatcher.group("variableName");
                String formattingType = optionalMatcher.group("formattingType");

                this.updateVariables(classTool, method, variableName, formattingType, false);
            } else {
                CommandAliasesMod.getLogger().error("Invalid Command Aliases Holder: {}", this.holder);
            }
        }

        private void updateVariables(String classTool, String method, String variableName, String formattingType, boolean required) {
            String cT = classTool;
            String vN = variableName;

            if (method == null && vN == null) {
                vN = cT;
                cT = null;
            }

            this.classTool = cT;
            this.method = method;
            this.variableName = vN;
            this.formattingType = formattingType;
            this.required = required;
        }

        public String toString() {
            return this.holder;
        }

        public String getHolder() {
            return this.holder;
        }

        public String getClassTool() {
            return this.classTool;
        }

        public String getMethod() {
            return this.method;
        }

        public String getVariableName() {
            return this.variableName;
        }

        public String getFormattingType() {
            return this.formattingType;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
