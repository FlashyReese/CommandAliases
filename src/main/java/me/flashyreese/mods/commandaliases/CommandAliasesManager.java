package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.util.CommandRemoval;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandAliasesManager {


    private CommandAliasesParser commandAliasesParser = new CommandAliasesParser();
    private final Gson gson = new Gson();
    private List<CommandAlias> commands = new ArrayList<>();
    private List<String> loadedCommands = new ArrayList<>();

    public CommandAliasesManager() {
        registerCommandAliasesCommands();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCommands(false, dispatcher, dedicated);
        });
    }

    private void registerCommands(boolean unregisterLoaded, CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        this.commands.clear();
        this.commands.addAll(loadCommandAliases(new File("config/commandaliases.json")));

        if (unregisterLoaded) {
            this.loadedCommands.clear();
        }

        for (CommandAlias cmd : this.commands) {
            if (cmd.getCommand().contains(" ")) {
                this.loadedCommands.add(cmd.getCommand().split(" ")[0]);
            } else {
                this.loadedCommands.add(cmd.getCommand());
            }
            dispatcher.register(commandAliasesParser.buildCommand(cmd, dispatcher));
        }

        if (!unregisterLoaded)
            CommandAliasesMod.getLogger().info("Registered all your commands :P, you can now single command nuke!");
        else
            CommandAliasesMod.getLogger().info("Reloaded all your commands :P, you can now single command nuke!");
    }

    private void registerCommandAliasesCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("commandAliases").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .then(CommandManager.literal("reload").executes(context -> {
                        context.getSource().sendFeedback(new LiteralText("Reloading all Command Aliases!"), true);
                        for (String cmd : this.loadedCommands) {
                            CommandRemoval.removeCommand(dispatcher.getRoot(), cmd);
                        }
                        registerCommands(true, dispatcher, dedicated);

                        //Update Command Tree
                        for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                            context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
                        }

                        context.getSource().sendFeedback(new LiteralText("Reloaded all Command Aliases!"), true);
                        return Command.SINGLE_SUCCESS;
                    })));
        });
    }


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
