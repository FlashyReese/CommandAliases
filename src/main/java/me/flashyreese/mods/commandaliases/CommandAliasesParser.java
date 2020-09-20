package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.arguments.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;

public class CommandAliasesParser {

    private final ArgumentTypeManager argumentTypeManager;

    public CommandAliasesParser() {
        this.argumentTypeManager = new ArgumentTypeManager();
    }

    public String formatText(CommandContext<ServerCommandSource> context, String cmd, String text) {
        String newCmd = text;
        try {
            String playerName = context.getSource().getPlayer().getEntityName();
            newCmd = text.replaceAll("\\{this::SELF}", playerName);//Fixme: write this better
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        newCmd = formatTextWithArgumentMap(getRequiredArgumentInputMap(cmd, context), newCmd);

        return newCmd;
    }

    private List<String> getRequiredArgumentsFromString(String cmd) { //Todo: Pattern match
        List<String> args = new ArrayList<>();
        if (!(cmd.contains("{") && cmd.contains("}")))
            return args;
        boolean log = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cmd.length(); i++) {
            if (cmd.charAt(i) == '{') {
                log = true;
                stringBuilder.append(cmd.charAt(i));
            } else if (cmd.charAt(i) == '}') {
                log = false;
                stringBuilder.append(cmd.charAt(i));
                args.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            } else if (log) {
                stringBuilder.append(cmd.charAt(i));
            }
        }
        return args;
    }

    public Map<String, String> getRequiredArgumentInputMap(String cmd, CommandContext<ServerCommandSource> context) {
        Map<String, String> map = new HashMap<>();
        List<String> args = getRequiredArgumentsFromString(cmd);
        for (String arg : args) {
            String line = arg.split("#")[1].split("}")[0];
            String newArg = "{" + line + "}";
            String argType = arg.split("\\{arg::")[1].split("#")[0];
            String value = argumentTypeManager.getArgumentMap().get(argType).getBiFunction().apply(context, line);
            map.put(newArg, value);
        }
        return map;
    }

    public String formatTextWithArgumentMap(Map<String, String> map, String cmd) {
        String command = cmd;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (command.contains(entry.getKey())) {
                command = command.replaceAll("\\{" + entry.getKey().substring(1), entry.getValue());
            }
        }
        return command;
    }

    LiteralArgumentBuilder<ServerCommandSource> parseCommand(CommandAlias command) { //Fixme: {arg} spacing instead of " "
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.getCommand());
        if (command.getCommand().contains(" ")) {
            commandBuilder = CommandManager.literal(command.getCommand().split(" ")[0]);
        }
        //commandBuilder = commandBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(command.getPermissionLevel()));
        return commandBuilder;
    }

    public ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) { // Todo: Optional Arguments
        List<String> args = getRequiredArgumentsFromString(cmd.getCommand());
        ArgumentBuilder<ServerCommandSource, ?> arguments = null;
        Collections.reverse(args);
        for (String arg : args) {
            if (arg.startsWith("{arg::")) {
                String argType = arg.split("\\{arg::")[1].split("#")[0];
                String variable = arg.split("#")[1].split("}")[0];
                if (argumentTypeManager.getArgumentMap().containsKey(argType)) {
                    if (arguments != null) {
                        arguments = CommandManager.argument(variable, argumentTypeManager.getArgumentMap().get(argType).getArgumentType()).then(arguments);
                    } else {
                        arguments = CommandManager.argument(variable, argumentTypeManager.getArgumentMap().get(argType).getArgumentType()).executes(context -> executeCommandAliases(cmd, dispatcher, context));
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
            if (subCmd.getSleep() != null){
                String formattedTime = subCmd.getSleep();
                int time = Integer.parseInt(formattedTime);
                //Todo: Sleep
            }
        }
        if (cmd.getMessage() != null) {
            String message = formatText(context, cmd.getCommand(), cmd.getMessage());
            context.getSource().sendFeedback(new LiteralText(message), true);
        }
        return execute;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher){
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
