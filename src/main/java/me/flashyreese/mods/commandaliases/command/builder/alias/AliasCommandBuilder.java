package me.flashyreese.mods.commandaliases.command.builder.alias;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.classtool.exec.MinecraftClassTool;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.alias.format.AliasCommand;
import me.flashyreese.mods.commandaliases.command.impl.ArgumentTypeMapper;
import me.flashyreese.mods.commandaliases.command.impl.FormattingTypeProcessor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the CommandAliases Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.1.3
 * @deprecated As of 0.7.0, because format is no longer viable to maintain use {@link me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder} instead.
 */
@Deprecated
public class AliasCommandBuilder {
    protected static final Pattern REQUIRED_COMMAND_ALIAS_HOLDER = Pattern.compile("\\{(?<classTool>\\w+)(::(?<method>[\\w:]+))?(#(?<variableName>\\w+))?(@(?<formattingType>\\w+))?}");
    protected static final Pattern OPTIONAL_COMMAND_ALIAS_HOLDER = Pattern.compile("\\[(?<classTool>\\w+)(::(?<method>[\\w:]+))?(#(?<variableName>\\w+))?(@(?<formattingType>\\w+))?]");

    private final AliasCommand command;
    private final List<AliasHolder> commandAliasesRequiredHolders = new ObjectArrayList<>();
    private final List<AliasHolder> commandAliasesOptionalHolders = new ObjectArrayList<>();

    private final Map<String, ClassTool<?>> classToolMap = new Object2ObjectOpenHashMap<>();
    private final FormattingTypeProcessor formattingTypeMap;

    public AliasCommandBuilder(AliasCommand command) {
        this.command = command;
        this.commandAliasesRequiredHolders.addAll(this.getCommandAliasesHolders(command.getCommand(), true));
        this.commandAliasesOptionalHolders.addAll(this.getCommandAliasesHolders(command.getCommand(), false));
        this.classToolMap.put("arg", new ArgumentTypeMapper());
        this.classToolMap.put("this", new MinecraftClassTool());
        this.formattingTypeMap = new FormattingTypeProcessor();
    }

    /**
     * Locates required/optional arguments within a Command Alias Command string.
     *
     * @param commandAliasCommand Command Alias Command String
     * @param required            locates required arguments if {@code true}, else if {@code false} locates optional arguments.
     * @return List of Command Alias Holders as String
     */
    private List<String> locateHolders(String commandAliasCommand, boolean required) {
        List<String> holders = new ObjectArrayList<>();
        Matcher m = required ? REQUIRED_COMMAND_ALIAS_HOLDER.matcher(commandAliasCommand) : OPTIONAL_COMMAND_ALIAS_HOLDER.matcher(commandAliasCommand);
        while (m.find()) {
            holders.add(m.group());
        }
        return holders;
    }

    /**
     * Builds a list of CommandAliasesHolder from list of string holders.
     *
     * @param holders  List of string holders
     * @param required Required/Optional
     * @return List of CommandAliasesHolders
     */
    private List<AliasHolder> buildHolders(List<String> holders, boolean required) {
        List<AliasHolder> aliasHolders = new ObjectArrayList<>();
        holders.forEach(holder -> aliasHolders.add(new AliasHolder(holder, required)));
        return aliasHolders;
    }

    /**
     * Gets a list of CommandAliasesHolder from Command Alias Command string.
     *
     * @param command  Command Alias Command
     * @param required Required/Optional
     * @return List of CommandAliasesHolders
     */
    public List<AliasHolder> getCommandAliasesHolders(String command, boolean required) {
        return this.buildHolders(this.locateHolders(command, required), required);
    }

    /**
     * Maps inputted arguments to holder variable name. It's used for formatting execution commands or messages.
     *
     * @param context CommandContext
     * @return HashMap Holder Variable Name - Input
     */
    private Map<String, String> getHolderInputMap(CommandContext<ServerCommandSource> context, boolean required) {
        Map<String, String> inputMap = new Object2ObjectOpenHashMap<>();

        for (AliasHolder holder : required ? this.commandAliasesRequiredHolders : this.commandAliasesOptionalHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {
                if (this.classToolMap.get(holder.getClassTool()).contains(holder.getMethod())) {
                    String key = holder.getVariableName();
                    String value;
                    try {
                        value = this.classToolMap.get(holder.getClassTool()).getValue(context, holder);
                    } catch (Exception e) {
                        value = null;
                    }

                    if (value == null) {
                        CommandAliasesMod.logger().error("Return value for \"{}\" was null, skipped!", key);
                        if (required) {
                            break;
                        } else {
                            continue;
                        }
                    }

                    if (holder.getFormattingType() != null) {
                        if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                            inputMap.put(key, this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                        } else {
                            CommandAliasesMod.logger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                            inputMap.put(key, value);
                        }
                    } else {
                        inputMap.put(key, value);
                    }
                } else {
                    CommandAliasesMod.logger().error("No method found for \"{}\"", holder.getHolder());
                }
            } else {
                CommandAliasesMod.logger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }
        return inputMap;
    }

    /**
     * Formats Execution Command or Message with user input map.
     *
     * @param context CommandContext
     * @param text    Execution Command or Message
     * @return Executable Command
     */
    private String formatExecutionCommandOrMessage(CommandContext<ServerCommandSource> context, String text, boolean ignoreOptionalRemoval) {
        //Bind Input Map to execution commands
        Map<String, String> requiredInputMap = this.getHolderInputMap(context, true);
        Map<String, String> optionalInputMap = this.getHolderInputMap(context, false);
        String formattedText = text;
        for (Map.Entry<String, String> entry : requiredInputMap.entrySet()) {
            String format = String.format("{%s}", entry.getKey());
            if (formattedText.contains(format)) {
                formattedText = formattedText.replace(format, entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : optionalInputMap.entrySet()) {
            String format = String.format("[%s]", entry.getKey());
            if (formattedText.contains(format)) {
                formattedText = formattedText.replace(format, entry.getValue());
            }
        }

        //Execution Formatting/Binding
        Map<String, String> newInputMap = new Object2ObjectOpenHashMap<>();
        List<AliasHolder> textHolders = this.getCommandAliasesHolders(formattedText, true);
        textHolders.addAll(this.getCommandAliasesHolders(formattedText, false));
        for (AliasHolder holder : textHolders) {
            String value = null;

            if (requiredInputMap.containsKey(holder.getVariableName())) {
                value = requiredInputMap.get(holder.getVariableName());
            } else if (optionalInputMap.containsKey(holder.getVariableName())) {
                value = optionalInputMap.get(holder.getVariableName());
            } else if (this.classToolMap.containsKey(holder.getClassTool())) {
                if (this.classToolMap.get(holder.getClassTool()).contains(holder.getMethod())) {
                    try {
                        value = this.classToolMap.get(holder.getClassTool()).getValue(context, holder);
                    } catch (Exception ignored) {

                    }
                } else {
                    CommandAliasesMod.logger().warn("No method found for \"{}\"", holder.getHolder());
                }
            }

            if (value == null) {
                CommandAliasesMod.logger().warn("Unable to find a value for \"{}\", skipping", holder.getHolder());
                continue;
            }

            if (holder.getFormattingType() != null) {
                if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                    newInputMap.put(holder.getHolder(), this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                } else {
                    CommandAliasesMod.logger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
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
        //Check for missing optional arguments and remove
        if (!ignoreOptionalRemoval) {
            List<String> missingOptionalHolders = this.locateHolders(formattedText, false);
            if (missingOptionalHolders.size() != 0) {
                for (String holder : missingOptionalHolders) {
                    formattedText = formattedText.replace(holder, "");
                }
            }
        }

        formattedText = formattedText.trim();

        return formattedText;
    }

    /**
     * Executes command aliases
     *
     * @param cmd        Command Alias
     * @param dispatcher CommandDispatcher
     * @param context    CommandContext
     * @return Command Execution Status
     */
    private int executeCommandAliases(AliasCommand cmd, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context) {
        AtomicInteger execute = new AtomicInteger();
        Thread thread = new Thread(() -> {
            try {
                if (cmd.getExecution() != null) {
                    for (AliasCommand subCommandAlias : cmd.getExecution()) {
                        if (subCommandAlias.getCommand() != null) {
                            String executionCommand = this.formatExecutionCommandOrMessage(context, subCommandAlias.getCommand(), subCommandAlias.isIgnoreOptionalRemoval());
                            if (subCommandAlias.getType() == CommandType.CLIENT) {
                                try {
                                    execute.set(dispatcher.execute(executionCommand, context.getSource()));
                                } catch (CommandSyntaxException e) {
                                    String output = e.getLocalizedMessage();
                                    context.getSource().sendFeedback(new LiteralText(output), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                }
                            } else if (subCommandAlias.getType() == CommandType.SERVER) {
                                try {
                                    execute.set(dispatcher.execute(executionCommand, context.getSource().getMinecraftServer().getCommandSource()));
                                } catch (CommandSyntaxException e) {
                                    String output = e.getLocalizedMessage();
                                    context.getSource().sendFeedback(new LiteralText(output), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                }
                            }
                        }
                        if (subCommandAlias.getMessage() != null) {
                            String message = this.formatExecutionCommandOrMessage(context, subCommandAlias.getMessage(), subCommandAlias.isIgnoreOptionalRemoval());
                            context.getSource().sendFeedback(new LiteralText(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
                        }
                        if (subCommandAlias.getSleep() != null) {
                            String formattedTime = this.formatExecutionCommandOrMessage(context, subCommandAlias.getSleep(), false);
                            int time = Integer.parseInt(formattedTime);
                            Thread.sleep(time);
                        }
                    }
                }
            } catch (InterruptedException e) {
                String output = e.getLocalizedMessage();
                context.getSource().sendFeedback(new LiteralText(output), CommandAliasesMod.options().debugSettings.broadcastToOps);
            }
            if (cmd.getMessage() != null) {
                String message = this.formatExecutionCommandOrMessage(context, cmd.getMessage(), false);
                context.getSource().sendFeedback(new LiteralText(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
            }
        });
        thread.setName("Command Aliases");
        thread.start();
        return execute.get();
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
    private LiteralArgumentBuilder<ServerCommandSource> parseCommand(AliasCommand command, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = null;

        List<String> allHolders = this.locateHolders(command.getCommand(), true);
        allHolders.addAll(this.locateHolders(command.getCommand(), false));

        String newCommand = command.getCommand();

        for (String holder : allHolders) {
            newCommand = newCommand.replace(holder, "");
        }
        newCommand = newCommand.trim();


        if (newCommand.contains(" ") && allHolders.size() == 0) { //If no holders are found and command contains spaces, parse json literals
            List<String> literals = Arrays.asList(newCommand.split(" "));
            Collections.reverse(literals);
            for (String literal : literals) {
                if (commandBuilder != null) {
                    commandBuilder = CommandManager.literal(literal).then(commandBuilder);
                } else {
                    commandBuilder = CommandManager.literal(literal).executes(context -> this.executeCommandAliases(command, dispatcher, context));
                }
            }
        } else if (newCommand.contains(" ") && allHolders.size() != 0) {
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
        } else if (!newCommand.contains(" ") && allHolders.size() == 0) { // No holders no spaces just a command rename with extra steps
            commandBuilder = CommandManager.literal(newCommand).executes(context -> this.executeCommandAliases(command, dispatcher, context));
        } else if (!newCommand.contains(" ") && allHolders.size() != 0) { // Holders no spaces, parse city
            ArgumentBuilder<ServerCommandSource, ?> arguments = this.parseArguments(command, dispatcher); //parsearguments does most of the work although optional arguments might need to come through here
            if (arguments != null) {
                commandBuilder = CommandManager.literal(newCommand).then(arguments);
            } else {
                commandBuilder = CommandManager.literal(newCommand).executes(context -> this.executeCommandAliases(command, dispatcher, context));
            }
        }
        return commandBuilder;
    }

    /**
     * Argument Builder for required/optional arguments
     *
     * @param commandAlias CommandAlias
     * @param dispatcher   CommandDispatcher
     * @return ArgumentBuilder
     */
    private ArgumentBuilder<ServerCommandSource, ?> parseArguments(AliasCommand commandAlias, CommandDispatcher<ServerCommandSource> dispatcher) {
        //Check for optional arguments then build
        List<AliasHolder> commandOptionalHolders = this.getCommandAliasesHolders(this.command.getCommand(), false);
        ArgumentBuilder<ServerCommandSource, ?> optionalArguments = null;
        for (AliasHolder holder : commandOptionalHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {
                ClassTool<?> tool = this.classToolMap.get(holder.getClassTool());
                if (tool instanceof ArgumentTypeMapper) {
                    if (tool.contains(holder.getMethod())) {
                        if (optionalArguments != null) { //If first argument start building
                            optionalArguments = optionalArguments.then(CommandManager.argument(holder.getVariableName(), ((ArgumentTypeMapper) tool).getValue(holder.getMethod())).executes(context -> this.executeCommandAliases(commandAlias, dispatcher, context)));
                        } else {
                            optionalArguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeMapper) tool).getValue(holder.getMethod())).executes(context -> this.executeCommandAliases(commandAlias, dispatcher, context));
                        }
                    }
                }
            }
        }
        //Build Required Arguments
        List<AliasHolder> commandRequiredHolders = this.getCommandAliasesHolders(this.command.getCommand(), true);
        ArgumentBuilder<ServerCommandSource, ?> requiredArguments = null;
        Collections.reverse(commandRequiredHolders);
        for (AliasHolder holder : commandRequiredHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {
                ClassTool<?> tool = this.classToolMap.get(holder.getClassTool());
                if (tool instanceof ArgumentTypeMapper) {
                    if (tool.contains(holder.getMethod())) {
                        if (requiredArguments != null) {
                            requiredArguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeMapper) tool).getValue(holder.getMethod())).then(requiredArguments);
                        } else {
                            if (optionalArguments != null) {
                                requiredArguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeMapper) tool).getValue(holder.getMethod())).executes(context -> this.executeCommandAliases(commandAlias, dispatcher, context)).then(optionalArguments);
                            } else {
                                requiredArguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeMapper) tool).getValue(holder.getMethod())).executes(context -> this.executeCommandAliases(commandAlias, dispatcher, context));
                            }
                        }
                    }
                }
            } else {
                CommandAliasesMod.logger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }
        return requiredArguments;
    }
}
