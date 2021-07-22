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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.builders.CommandAliasesBuilder;
import me.flashyreese.mods.commandaliases.command.builders.CommandBuilder;
import me.flashyreese.mods.commandaliases.command.builders.CommandReassignBuilder;
import me.flashyreese.mods.commandaliases.command.builders.CommandRedirectBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 0.4.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private final Gson gson = new Gson();
    private final List<CommandAlias> commands = new ObjectArrayList<>();
    private final List<String> loadedCommands = new ObjectArrayList<>();
    private final Map<String, String> reassignCommandMap = new Object2ObjectOpenHashMap<>();

    public CommandAliasesLoader() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            this.registerCommandAliasesCommands(dispatcher, dedicated);
        });
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            this.registerCommands(server.getCommandManager().getDispatcher(), server.isDedicated());
        }));
    }

    /**
     * Registers all Command Aliases' custom commands. You can re-register them if you enable @param unregisterLoaded
     *
     * @param dispatcher The CommandDispatcher
     * @param dedicated  Is Dedicated Server
     */
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        this.commands.clear();
        this.commands.addAll(this.loadCommandAliases(new File("config/commandaliases.json")));

        for (CommandAlias cmd : this.commands) {
            if (cmd.getCommandMode() == CommandMode.COMMAND_ALIAS) {
                dispatcher.register(new CommandAliasesBuilder(cmd).buildCommand(dispatcher));
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                dispatcher.register(new CommandBuilder(cmd.getCustomCommand()).buildCommand(dispatcher));
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM
                    || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT
                    || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                LiteralArgumentBuilder<ServerCommandSource> command = cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG ?
                        new CommandRedirectBuilder(cmd).buildCommand(dispatcher) : new CommandReassignBuilder(cmd).buildCommand(dispatcher, this.reassignCommandMap);
                if (command == null) {
                    continue;
                } else {
                    dispatcher.register(command);
                }
            }

            if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                this.loadedCommands.add(cmd.getCustomCommand().getParent());
            } else {
                if (cmd.getCommand().contains(" ")) {
                    this.loadedCommands.add(cmd.getCommand().split(" ")[0]);
                } else {
                    this.loadedCommands.add(cmd.getCommand());
                }
            }
        }

        CommandAliasesMod.getLogger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all Command Aliases' commands
     *
     * @param dispatcher The CommandDispatcher
     * @param dedicated  Is Dedicated Server
     */
    private void registerCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
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
                                    this.unregisterCommands(dispatcher);
                                    this.registerCommands(dispatcher, dedicated);

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
                                    this.registerCommands(dispatcher, dedicated);

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
                                    this.unregisterCommands(dispatcher);

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
     * Unregisters all command aliases and reassignments.
     *
     * @param dispatcher CommandDispatcher
     */
    private void unregisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (String cmd : this.loadedCommands) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignCommandMap.entrySet()) {
            CommandNode<ServerCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<ServerCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getKey())).findFirst().orElse(null);

            if (commandNode != null && commandReassignNode == null) {
                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(entry.getValue()));

                try {
                    Field f = commandNode.getClass().getDeclaredField("literal");
                    f.setAccessible(true);
                    Field modifiers = Field.class.getDeclaredField("modifiers");
                    modifiers.setAccessible(true);
                    modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                    f.set(commandNode, entry.getKey());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                dispatcher.getRoot().addChild(commandNode);
            }
        }

        this.reassignCommandMap.clear();
        this.loadedCommands.clear();
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
