package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandAliasesManager {


    private CommandAliasesParser commandAliasesParser = new CommandAliasesParser();
    private final Gson gson = new Gson();
    private List<CommandAlias> commands = new ArrayList<>();

    public CommandAliasesManager() {
        registerCommands();
    }

    private void registerCommands() {
        this.commands.addAll(loadCommandAliases(new File("config/commandaliases.json")));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            for (CommandAlias cmd : this.commands) {
                LiteralArgumentBuilder<ServerCommandSource> command = commandAliasesParser.parseCommand(cmd.getCommand());
                ArgumentBuilder<ServerCommandSource, ?> arguments = commandAliasesParser.parseArguments(cmd, dispatcher);
                if (arguments != null) {
                    command = command.then(arguments);
                } else {
                    command = command.executes(context -> commandAliasesParser.executeCommandAliases(cmd, dispatcher, context));
                }
                dispatcher.register(command);
            }
        });

        CommandAliasesMod.getLogger().info("Registered all your commands :P, you can now single command nuke!");
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
