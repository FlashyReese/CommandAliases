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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 0.1.3
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private final Gson gson = new Gson();
    private final List<CommandAlias> commands = new ArrayList<>();
    private final List<String> loadedCommands = new ArrayList<>();

    public CommandAliasesLoader() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            this.registerCommandAliasesCommands(dispatcher, dedicated);
            this.registerCommands(false, dispatcher, dedicated);
        });
    }

    /**
     * Registers all Command Aliases' custom commands. You can re-register them if you enable @param unregisterLoaded
     *
     * @param unregisterLoaded If method should unload loaded commands
     * @param dispatcher The CommandDispatcher
     * @param dedicated Is Dedicated Server
     */
    private void registerCommands(boolean unregisterLoaded, CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated){
        this.commands.clear();
        this.commands.addAll(loadCommandAliases(new File("config/commandaliases.json")));

        if (unregisterLoaded) {
            this.loadedCommands.clear();
        }

        for (CommandAlias cmd : this.commands) {
            if (cmd.getReassignOriginal() != null){
                CommandNode<ServerCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                        node.getName().equals(cmd.getCommand())).findFirst().orElse(null);

                CommandNode<ServerCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node -> node.getName().equals(cmd.getReassignOriginal())).findFirst().orElse(null);

                if (commandNode != null && commandReassignNode == null){
                    dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd.getCommand()));

                    try{
                        Field f = commandNode.getClass().getDeclaredField("literal");
                        f.setAccessible(true);
                        Field modifiers = Field.class.getDeclaredField("modifiers");
                        modifiers.setAccessible(true);
                        modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                        f.set(commandNode, cmd.getReassignOriginal());
                    }catch (NoSuchFieldException | IllegalAccessException e){
                        e.printStackTrace();
                        CommandAliasesMod.getLogger().error("Skipping \"{}\", couldn't modify command literal", cmd.getCommand());
                        continue;
                    }

                    dispatcher.getRoot().addChild(commandNode);
                    CommandAliasesMod.getLogger().info("Command \"{}\" has been reassigned to \"{}\"", cmd.getCommand(), cmd.getReassignOriginal());
                    if (cmd.isReassignOnly()){
                        continue;
                    }
                }

            }
            if (cmd.getCommand().contains(" ")) {
                this.loadedCommands.add(cmd.getCommand().split(" ")[0]);
            } else {
                this.loadedCommands.add(cmd.getCommand());
            }
            dispatcher.register(new CommandAliasesBuilder(cmd).buildCommand(dispatcher));
        }

        if (!unregisterLoaded)
            CommandAliasesMod.getLogger().info("Registered all your commands :P, you can now single command nuke!");
        else
            CommandAliasesMod.getLogger().info("Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all Command Aliases' commands
     *
     * @param dispatcher The CommandDispatcher
     * @param dedicated Is Dedicated Server
     */
    private void registerCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("commandAliases").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(context -> {
                    context.getSource().sendFeedback(new LiteralText("Working?"), true);
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Reloading all Command Aliases!"), true);
                                    for (String cmd : this.loadedCommands) {
                                        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
                                    }
                                    registerCommands(true, dispatcher, dedicated);

                                    //Update Command Tree
                                    for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
                                    }

                                    context.getSource().sendFeedback(new LiteralText("Reloaded all Command Aliases!"), true);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                ).then(CommandManager.literal("load")
                        .then(CommandManager.argument("json", StringArgumentType.greedyString())
                                .executes(context -> {
                                            String json = StringArgumentType.getString(context, "json");
                                            context.getSource().sendFeedback(new LiteralText(json), true);
                                            return Command.SINGLE_SUCCESS;
                                        }
                                )
                        )
                ).then(CommandManager.literal("unload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Unloading all Command Aliases!"), true);
                                    for (String cmd : this.loadedCommands) {
                                        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
                                    }
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
     * Reads JSON file and serializes them to a List of CommandAliases
     *
     * @param file JSON file path
     * @return List of CommandAliases
     */
    private List<CommandAlias> loadCommandAliases(File file) {
        List<CommandAlias> commandAliases = new ArrayList<>();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                commandAliases = gson.fromJson(reader, new TypeToken<List<CommandAlias>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse CommandAliases File", e);
            }
        } else {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                String json = gson.toJson(new ArrayList<>());
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("Could not write CommandAliases File", e);
            }
        }

        return commandAliases;
    }
}
