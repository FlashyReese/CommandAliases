package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        this.commands.addAll(loadCommandAliases(new File("commandaliases.json")));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            for (CommandAlias cmd : this.commands) {
                LiteralArgumentBuilder<ServerCommandSource> command = commandAliasesParser.parseCommand(cmd.getCommand());
                ArgumentBuilder<ServerCommandSource, ?> arguments = commandAliasesParser.parseArguments(cmd, dispatcher);
                if (arguments != null){
                    command = command.then(arguments);
                }else{
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
        }

        return commandAliases;
    }
}
