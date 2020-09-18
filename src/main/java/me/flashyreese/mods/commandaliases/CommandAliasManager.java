package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.flashyreese.mods.commandaliases.command.Command;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.*;

public class CommandAliasManager {

    private final Gson gson = new Gson();
    private List<Command> commands = new ArrayList<>();

    public CommandAliasManager() {
        registerCommands();
    }

    private void registerCommands() {
        this.commands.addAll(loadCommandAliases(new File("commandaliases.json")));

        for (Command cmd : this.commands) {
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                dispatcher.register(literal(cmd.getCommand()).executes(context -> {
                    int execute = 0;
                    for (Command subCmd : cmd.getExecution()) {
                        if (subCmd.getType() == CommandType.CLIENT) {
                            execute = dispatcher.execute(subCmd.getCommand(), context.getSource());
                        } else if (subCmd.getType() == CommandType.SERVER) {
                            execute = dispatcher.execute(subCmd.getCommand(), context.getSource().getMinecraftServer().getCommandSource());
                        }
                        if (subCmd.getMessage() != null) {
                            context.getSource().sendFeedback(new LiteralText(subCmd.getMessage()), true);
                        }
                    }
                    if (cmd.getMessage() != null) {
                        context.getSource().sendFeedback(new LiteralText(cmd.getMessage()), true);
                    }
                    return execute;
                }));
            });
        }
    }


    private List<Command> loadCommandAliases(File file) {
        List<Command> commandAliases = new ArrayList<>();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {

                commandAliases = gson.fromJson(reader, new TypeToken<List<Command>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse CommandAliases File", e);
            }
        }

        return commandAliases;
    }
}
