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

    public String parse(CommandContext<ServerCommandSource> context, String cmd, String subCmd) throws CommandSyntaxException {
        String newCmd = subCmd;
        try {
            String playerName = context.getSource().getPlayer().getEntityName();
            newCmd = subCmd.replaceAll("\\{this::SELF}", playerName);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        newCmd = formatCommand(getInputMap(cmd, context), newCmd);

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

    public Map<String, String> getInputMap(String cmd, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Map<String, String> map = new HashMap<>();
        List<String> args = getArgumentsFromString(cmd);
        for (String arg : args) {
            String line = arg.split("#")[1].split("}")[0];
            String newArg = "{" + line + "}";
            String argType = arg.split("\\{arg::")[1].split("#")[0];
            String value = argumentTypeManager.getArgumentMap().get(argType).getBiFunction().apply(context, line);
            System.out.printf("%s :: %s%n", newArg, value);
            map.put(newArg, value);//Fixme: This needs a patch time to pass Optional BiFunction into a map
        }
        return map;
    }

    public String formatCommand(Map<String, String> map, String cmd) {
        String command = cmd;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (command.contains(entry.getKey())) {
                command = command.replaceAll("\\{" + entry.getKey().substring(1), entry.getValue());
            }
        }
        return command;
    }

    LiteralArgumentBuilder<ServerCommandSource> parseCommand(String command) {
        if (command.contains(" ")) {
            return CommandManager.literal(command.split(" ")[0]);
        }
        return CommandManager.literal(command);
    }

    public ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> args = getArgumentsFromString(cmd.getCommand());
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
            String subCommand = parse(context, cmd.getCommand(), subCmd.getCommand());
            if (subCmd.getType() == CommandType.CLIENT) {
                execute = dispatcher.execute(subCommand, context.getSource());
            } else if (subCmd.getType() == CommandType.SERVER) {
                execute = dispatcher.execute(subCommand, context.getSource().getMinecraftServer().getCommandSource());
            }
            if (subCmd.getMessage() != null) {
                context.getSource().sendFeedback(new LiteralText(subCmd.getMessage()), true);
            }
        }
        if (cmd.getMessage() != null) {
            context.getSource().sendFeedback(new LiteralText(cmd.getMessage()), true);
        }
        return execute;
    }
}
