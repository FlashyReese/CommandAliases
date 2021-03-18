/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ClientReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ServerReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.redirect.CommandRedirectBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private final Gson gson = new Gson();
    private final List<CommandAlias> serverCommands = new ObjectArrayList<>();
    private final List<CommandAlias> clientCommands = new ObjectArrayList<>();
    private final List<String> loadedServerCommands = new ObjectArrayList<>();
    private final List<String> loadedClientCommands = new ObjectArrayList<>();
    private final Map<String, String> reassignServerCommandMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, String> reassignClientCommandMap = new Object2ObjectOpenHashMap<>();

    private Field literalCommandNodeLiteralField = null;

    public CommandAliasesLoader() {
        try {
            this.literalCommandNodeLiteralField = LiteralCommandNode.class.getDeclaredField("literal");
            this.literalCommandNodeLiteralField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            this.registerServerCommandAliasesCommands(dispatcher);
            this.loadServerCommands();
            this.registerServerCommands(dispatcher);
            if (!dedicated) {
                this.registerClientCommandAliasesCommands(dispatcher);
                this.loadClientCommands();
                this.registerClientCommands(dispatcher);
            }
        });
    }

    /**
     * Loads command aliases file, meant for integrated/dedicated servers.
     */
    private void loadServerCommands() {
        this.serverCommands.clear();
        this.serverCommands.addAll(this.loadCommandAliases(new File("config/commandaliases.json")));
    }

    /**
     * Loads client command aliases file, meant for clients.
     */
    private void loadClientCommands() {
        this.clientCommands.clear();
        this.clientCommands.addAll(this.loadCommandAliases(new File("config/commandaliases-client.json")));
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param dispatcher Server CommandDispatcher
     */
    private void registerServerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.serverCommands.stream()
                .filter(cmd -> cmd.getCommandType() != null && cmd.getCommandType() == CommandType.SERVER)
                .forEach(cmd -> {
                    if (cmd.getCommandMode() == CommandMode.COMMAND_ALIAS) {
                        LiteralArgumentBuilder<ServerCommandSource> command = new AliasCommandBuilder(cmd.getAliasCommand()).buildCommand(dispatcher);
                        if (command != null) {
                            dispatcher.register(command);
                            this.loadedServerCommands.add(cmd.getAliasCommand().getCommand());
                        }
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                        LiteralArgumentBuilder<ServerCommandSource> command = new ServerCustomCommandBuilder(cmd.getCustomCommand()).buildCommand(dispatcher);
                        if (command != null) {
                            dispatcher.register(command);
                            this.loadedServerCommands.add(cmd.getCustomCommand().getParent());
                        }
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        LiteralArgumentBuilder<ServerCommandSource> command = null;
                        if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                            command = new CommandRedirectBuilder<ServerCommandSource>(cmd).buildCommand(dispatcher);
                        } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                            command = new ServerReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.reassignServerCommandMap).buildCommand(dispatcher);
                        }
                        if (command != null) {
                            dispatcher.register(command);
                            if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                                this.loadedServerCommands.add(cmd.getRedirectCommand().getCommand());
                            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                                this.loadedServerCommands.add(cmd.getReassignCommand().getCommand());
                            }
                        }
                    }
                });
        CommandAliasesMod.getLogger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param serverDispatcher Server CommandDispatcher
     */
    private void registerClientCommands(CommandDispatcher<ServerCommandSource> serverDispatcher) {
        this.clientCommands.stream()
                .filter(cmd -> cmd.getCommandType() != null && cmd.getCommandType() == CommandType.CLIENT)
                .forEach(cmd -> {
                    if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                        LiteralArgumentBuilder<FabricClientCommandSource> command = new ClientCustomCommandBuilder(cmd.getCustomCommand(), serverDispatcher).buildCommand(ClientCommandManager.DISPATCHER);
                        if (command != null) {
                            ClientCommandManager.DISPATCHER.register(command);
                            this.loadedClientCommands.add(cmd.getCustomCommand().getParent());
                        }
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        LiteralArgumentBuilder<FabricClientCommandSource> command = null;
                        if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                            command = new CommandRedirectBuilder<FabricClientCommandSource>(cmd).buildCommand(ClientCommandManager.DISPATCHER);
                        } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                            command = new ClientReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.reassignClientCommandMap, serverDispatcher).buildCommand(ClientCommandManager.DISPATCHER);
                        }
                        if (command != null) {
                            ClientCommandManager.DISPATCHER.register(command);
                            if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                                this.loadedClientCommands.add(cmd.getRedirectCommand().getCommand());
                            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                                this.loadedClientCommands.add(cmd.getReassignCommand().getCommand());
                            }
                        }
                    }
                });
        CommandAliasesMod.getLogger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' commands
     *
     * @param dispatcher The CommandDispatcher
     */
    private void registerServerCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("commandaliases").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(new LiteralText("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(new LiteralText(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED)), false));

                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Reloading all Command Aliases!"), true);
                                    this.unregisterServerCommands(dispatcher);
                                    this.loadServerCommands();
                                    this.registerServerCommands(dispatcher);

                                    //Update Command Tree
                                    for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
                                    }

                                    context.getSource().sendFeedback(new LiteralText("Reloaded all Command Aliases!"), true);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("load")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Loading all Command Aliases!"), true);
                                    this.loadServerCommands();
                                    this.registerServerCommands(dispatcher);

                                    for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(new LiteralText("Loaded all Command Aliases!"), true);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("unload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Unloading all Command Aliases!"), true);
                                    this.unregisterServerCommands(dispatcher);

                                    for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(new LiteralText("Unloaded all Command Aliases!"), true);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    /**
     * Registers all client Command Aliases' commands
     *
     * @param dispatcher The Server CommandDispatcher
     */
    private void registerClientCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("commandaliases").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(new LiteralText("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(new LiteralText(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED))));

                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("reload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Reloading all Command Aliases!"));
                                    this.unregisterClientCommands();
                                    this.loadClientCommands();
                                    this.registerClientCommands(dispatcher);
                                    context.getSource().sendFeedback(new LiteralText("Reloaded all Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("load")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Loading all Command Aliases!"));
                                    this.loadClientCommands();
                                    this.registerClientCommands(dispatcher);
                                    context.getSource().sendFeedback(new LiteralText("Loaded all Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("unload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Unloading all Command Aliases!"));
                                    this.unregisterClientCommands();
                                    context.getSource().sendFeedback(new LiteralText("Unloaded all Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    /**
     * Unregisters all server command aliases and reassignments.
     *
     * @param dispatcher Server CommandDispatcher
     */
    private void unregisterServerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (String cmd : this.loadedServerCommands) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignServerCommandMap.entrySet()) {
            CommandNode<ServerCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<ServerCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getKey())).findFirst().orElse(null);

            if (commandNode != null && commandReassignNode == null) {
                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(entry.getValue()));

                try {
                    this.literalCommandNodeLiteralField.set(commandNode, entry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                dispatcher.getRoot().addChild(commandNode);
            }
        }

        this.reassignServerCommandMap.clear();
        this.loadedServerCommands.clear();
    }

    /**
     * Unregisters all client command aliases and reassignments.
     */
    private void unregisterClientCommands() {
        for (String cmd : this.loadedClientCommands) {
            ClientCommandManager.DISPATCHER.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignClientCommandMap.entrySet()) {
            CommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.DISPATCHER.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<FabricClientCommandSource> commandReassignNode = ClientCommandManager.DISPATCHER.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getKey())).findFirst().orElse(null);

            if (commandNode != null && commandReassignNode == null) {
                ClientCommandManager.DISPATCHER.getRoot().getChildren().removeIf(node -> node.getName().equals(entry.getValue()));

                try {
                    this.literalCommandNodeLiteralField.set(commandNode, entry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                ClientCommandManager.DISPATCHER.getRoot().addChild(commandNode);
            }
        }

        this.reassignClientCommandMap.clear();
        this.loadedClientCommands.clear();
    }

    /**
     * Reads JSON file and serializes them to a List of CommandAliases
     *
     * @param file JSON file path
     * @return List of CommandAliases
     */
    private List<CommandAlias> loadCommandAliases(File file) {
        List<CommandAlias> commandAliases = new ObjectArrayList<>();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                commandAliases = gson.fromJson(reader, new TypeToken<List<CommandAlias>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse CommandAliases File", e);
            }
        } else {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                String json = gson.toJson(new ObjectArrayList<>());
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("Could not write CommandAliases File", e);
            }
        }

        return commandAliases;
    }
}
