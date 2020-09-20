package me.flashyreese.mods.commandaliases.command;

import java.util.List;

public class CommandAlias {
    private String command;
    private int permissionLevel;
    private CommandType type;
    private List<CommandAlias> execution;
    private String sleep;
    private String message;

    public String getCommand() {
        return command;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public CommandType getType() {
        return type;
    }

    public List<CommandAlias> getExecution() {
        return execution;
    }

    public String getSleep() {
        return sleep;
    }

    public String getMessage() {
        return message;
    }
}
