package me.flashyreese.mods.commandaliases.command;

import java.util.List;

public class CommandAlias {
    private String command;
    private CommandType type;
    private List<CommandAlias> execution;
    private String message;

    public String getCommand() {
        return command;
    }

    public CommandType getType() {
        return type;
    }

    public List<CommandAlias> getExecution() {
        return execution;
    }

    public String getMessage() {
        return message;
    }
}
