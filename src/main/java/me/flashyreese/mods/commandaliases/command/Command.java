package me.flashyreese.mods.commandaliases.command;

import java.util.List;

public class Command {
    private String command;
    private CommandType type;
    private List<Command> execution;
    private String message;

    public String getCommand() {
        return command;
    }

    public CommandType getType() {
        return type;
    }

    public List<Command> getExecution() {
        return execution;
    }

    public String getMessage() {
        return message;
    }
}
