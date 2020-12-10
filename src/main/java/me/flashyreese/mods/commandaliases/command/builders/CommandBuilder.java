package me.flashyreese.mods.commandaliases.command.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.classtool.impl.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.CommandAliasAction;
import me.flashyreese.mods.commandaliases.command.CommandAliasChild;
import me.flashyreese.mods.commandaliases.command.CommandAliasParent;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class CommandBuilder {
    private final CommandAliasParent commandAliasParent;

    private final ArgumentTypeManager argumentTypeManager = new ArgumentTypeManager();

    public CommandBuilder(CommandAliasParent commandAliasParent) {
        this.commandAliasParent = commandAliasParent;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        return this.buildCommandParent(dispatcher);
    }

    private LiteralArgumentBuilder<ServerCommandSource> buildCommandParent(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal(this.commandAliasParent.getParent());
        if (this.commandAliasParent.getPermission() < 0 && this.commandAliasParent.getPermission() >= 4) {
            argumentBuilder = argumentBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(this.commandAliasParent.getPermission()));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeAction(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new HashMap<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CommandAliasChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new HashMap<>());
                if (subArgumentBuilder != null) {
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
    }

    private ArgumentBuilder<ServerCommandSource, ?> buildCommandChild(CommandAliasChild child, CommandDispatcher<ServerCommandSource> dispatcher, Map<String, BiFunction<CommandContext<ServerCommandSource>, String, String>> input) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = CommandManager.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeManager.contains(child.getArgumentType())) {
                argumentBuilder = CommandManager.argument(child.getChild(), this.argumentTypeManager.getValue(child.getArgumentType()).getArgumentType());
                input.put(child.getChild(), this.argumentTypeManager.getValue(child.getArgumentType()).getBiFunction());
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
                    return this.executeAction(child.getActions(), child.getMessage(), dispatcher, context, input);
                });
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CommandAliasChild subChild : child.getChildren()) {
                    ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new HashMap<>(input));
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
    }

    private int executeAction(List<CommandAliasAction> actions, String message, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, Map<String, BiFunction<CommandContext<ServerCommandSource>, String, String>> currentInputMap) {
        if ((actions == null || actions.isEmpty()) && (message != null || !message.isEmpty())) {
            String formatString = this.formatString(context, currentInputMap, message);
            context.getSource().sendFeedback(new LiteralText(formatString), true);
            return Command.SINGLE_SUCCESS;
        } else if ((actions != null || !actions.isEmpty()) && (message == null || message.isEmpty())) {
            return this.executeCommand(actions, dispatcher, context, currentInputMap);
        } else {
            int state = this.executeCommand(actions, dispatcher, context, currentInputMap);
            String formatString = this.formatString(context, currentInputMap, message);
            context.getSource().sendFeedback(new LiteralText(formatString), true);
            return state;
        }
    }

    private int executeCommand(List<CommandAliasAction> actions, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, Map<String, BiFunction<CommandContext<ServerCommandSource>, String, String>> currentInputMap) {
        AtomicInteger executeState = new AtomicInteger();
        Thread thread = new Thread(() -> {
            try {
                if (actions != null) {
                    for (CommandAliasAction action : actions) {
                        if (action.getCommand() != null) {
                            String actionCommand = this.formatString(context, currentInputMap, action.getCommand());
                            if (action.getCommandType() == CommandType.CLIENT) {
                                executeState.set(dispatcher.execute(actionCommand, context.getSource()));
                            } else if (action.getCommandType() == CommandType.SERVER) {
                                executeState.set(dispatcher.execute(actionCommand, context.getSource().getMinecraftServer().getCommandSource()));
                            }
                        }
                        if (action.getMessage() != null) {
                            String message = this.formatString(context, currentInputMap, action.getMessage());
                            context.getSource().sendFeedback(new LiteralText(message), true);
                        }
                        if (action.getSleep() != null) {
                            String formattedTime = this.formatString(context, currentInputMap, action.getSleep());
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

    private String formatString(CommandContext<ServerCommandSource> context, Map<String, BiFunction<CommandContext<ServerCommandSource>, String, String>> currentInputMap, String string){
        //Functions fixme: more hardcoding
        string = string.replace("$getExecutorName()", context.getSource().getName());
        //Input Map
        for (Map.Entry<String, BiFunction<CommandContext<ServerCommandSource>, String, String>> entry : currentInputMap.entrySet()){
            string = string.replace(String.format("$getField(%s)", entry.getKey()), entry.getValue().apply(context, entry.getKey()));//fixme: A bit of hardcoding here
        }
        return string;
    }

}
