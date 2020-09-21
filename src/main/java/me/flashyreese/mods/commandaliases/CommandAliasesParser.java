package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.classtool.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;

/**
 * @Deprecated Use me.flashyreese.mods.commandaliases.CommandAliasesBuilder
 */
public class CommandAliasesParser {

    private final ArgumentTypeManager argumentTypeManager;
    private final FormattingTypeMap formattingTypeMap;

    public CommandAliasesParser() {
        this.argumentTypeManager = new ArgumentTypeManager();
        this.formattingTypeMap = new FormattingTypeMap();
    }

    public String formatText(CommandContext<ServerCommandSource> context, String cmd, String text) {
        String newCmd = text;
        try {
            String playerName = context.getSource().getPlayer().getEntityName();
            newCmd = text.replace("{this::SELF}", playerName);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        newCmd = formatTextWithArgumentMap(getRequiredArgumentInputMap(cmd, context), newCmd);

        return newCmd;
    }

    private List<String> getArgumentsFromString(String cmd) {
        List<String> args = new ArrayList<>();
        if (!(cmd.contains("{") && cmd.contains("}")))
            return args;
        boolean log = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cmd.length(); i++) {
            if (cmd.charAt(i) == '{') {
                if (cmd.charAt(i + 1) == '"') continue;
                log = true;
                stringBuilder.append(cmd.charAt(i));
            } else if (cmd.charAt(i) == '}') {
                if (log) {
                    stringBuilder.append(cmd.charAt(i));
                    String value = stringBuilder.toString();
                    if (!value.contains("\"")) //Validation purpose, need to change this to use pattern match
                        args.add(value);
                    log = false;
                    stringBuilder = new StringBuilder();
                }
            } else if (log) {
                stringBuilder.append(cmd.charAt(i));
            }
        }
        return args;
    }

    public Map<String, String> getRequiredArgumentInputMap(String cmd, CommandContext<ServerCommandSource> context) {
        Map<String, String> map = new HashMap<>();

        this.getArgumentsFromString(cmd).forEach(arg -> {
            String line = arg.split("#")[1];
            String optionalFormatting = null;
            if (line.contains("@")) {
                line = line.split("@")[0];
                optionalFormatting = arg.split("@")[1].split("}")[0];
            } else {
                line = line.split("}")[0];
            }
            String newArg = "{" + line + "}";
            String argType = arg.split("\\{arg::")[1].split("#")[0];
            String value;
            if (optionalFormatting == null) {
                value = argumentTypeManager.getValue(argType).getBiFunction().apply(context, line);
            } else {
                if (formattingTypeMap.getFormatTypeMap().containsKey(optionalFormatting)) {
                    value = formattingTypeMap.getFormatTypeMap().get(optionalFormatting).apply(argumentTypeManager.getValue(argType).getBiFunction().apply(context, line));
                } else {
                    value = argumentTypeManager.getValue(argType).getBiFunction().apply(context, line);
                    CommandAliasesMod.getLogger().error("Invalid formatting type: {}", arg);
                }
            }
            map.put(newArg, value);
        });

        return map;
    }

    public String formatTextWithArgumentMap(Map<String, String> map, String cmd) {
        String command = cmd;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (command.contains(entry.getKey())) {
                command = command.replace(entry.getKey(), entry.getValue());
            }
        }

        //Formatting Type Parser
        List<String> arguments = this.getArgumentsFromString(command);
        Map<String, String> optionalValueMap = new HashMap<>();
        for (String arg : arguments) {
            String variable = arg.split("\\{")[1].split("@")[0];
            String formattingType = arg.split("@")[1].split("}")[0];
            String newArg = "{" + variable + "}";
            if (map.containsKey(newArg)) {
                String value = map.get(newArg);
                if (formattingTypeMap.getFormatTypeMap().containsKey(formattingType)) {
                    optionalValueMap.put(arg, formattingTypeMap.getFormatTypeMap().get(formattingType).apply(value));
                } else {
                    CommandAliasesMod.getLogger().error("Invalid formatting type: {}", arg);
                }
            }
        }
        for (Map.Entry<String, String> entry : optionalValueMap.entrySet()) {
            if (command.contains(entry.getKey())) {
                command = command.replace(entry.getKey(), entry.getValue());
            }
        }

        return command;
    }

    LiteralArgumentBuilder<ServerCommandSource> parseCommand(CommandAlias command) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.getCommand());
        if (command.getCommand().contains(" ")) {
            commandBuilder = CommandManager.literal(command.getCommand().split(" ")[0]);
        }
        //commandBuilder = commandBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(command.getPermissionLevel()));
        return commandBuilder;
    }

    public ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> args = this.getArgumentsFromString(cmd.getCommand());
        ArgumentBuilder<ServerCommandSource, ?> arguments = null;
        Collections.reverse(args);
        for (String arg : args) {
            if (arg.startsWith("{arg::")) {
                String argType = arg.split("\\{arg::")[1].split("#")[0];
                String variable = arg.split("#")[1];
                if (variable.contains("@")) {
                    variable = variable.split("@")[0];
                } else {
                    variable = variable.split("}")[0];
                }
                if (argumentTypeManager.contains(argType)) {
                    if (arguments != null) {
                        arguments = CommandManager.argument(variable, argumentTypeManager.getValue(argType).getArgumentType()).then(arguments);
                    } else {
                        arguments = CommandManager.argument(variable, argumentTypeManager.getValue(argType).getArgumentType()).executes(context -> executeCommandAliases(cmd, dispatcher, context));
                    }
                }
            }
        }
        return arguments;
    }

    public int executeCommandAliases(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        int execute = 0;
        for (CommandAlias subCmd : cmd.getExecution()) {
            String subCommand = formatText(context, cmd.getCommand(), subCmd.getCommand());
            if (subCmd.getType() == CommandType.CLIENT) {
                execute = dispatcher.execute(subCommand, context.getSource());
            } else if (subCmd.getType() == CommandType.SERVER) {
                execute = dispatcher.execute(subCommand, context.getSource().getMinecraftServer().getCommandSource());
            }
            if (subCmd.getMessage() != null) {
                String message = formatText(context, cmd.getCommand(), subCmd.getMessage());
                context.getSource().sendFeedback(new LiteralText(message), true);
            }
            if (subCmd.getSleep() != null) {
                String formattedTime = subCmd.getSleep();
                int time = Integer.parseInt(formattedTime);
            }
        }
        if (cmd.getMessage() != null) {
            String message = formatText(context, cmd.getCommand(), cmd.getMessage());
            context.getSource().sendFeedback(new LiteralText(message), true);
        }
        return execute;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = parseCommand(cmd);
        ArgumentBuilder<ServerCommandSource, ?> arguments = parseArguments(cmd, dispatcher);
        if (arguments != null) {
            command = command.then(arguments);
        } else {
            command = command.executes(context -> executeCommandAliases(cmd, dispatcher, context));
        }
        return command;
    }
}
