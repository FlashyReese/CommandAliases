package me.flashyreese.mods.commandaliases.command;

import java.util.List;

public class CommandAliasChild {
    private String child;
    private String type;
    private String argumentType;
    private int permission;
    private List<CommandAliasAction> actions;
    private List<CommandAliasChild> children;
    private String message;

    public String getChild() {
        return child;
    }

    public String getType() {
        return type;
    }

    public String getArgumentType() {
        return argumentType;
    }

    public int getPermission() {
        return permission;
    }

    public List<CommandAliasAction> getActions() {
        return actions;
    }

    public List<CommandAliasChild> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOptional() {
        return (actions != null && !actions.isEmpty()) || (message != null && !message.isEmpty());
    }
}
