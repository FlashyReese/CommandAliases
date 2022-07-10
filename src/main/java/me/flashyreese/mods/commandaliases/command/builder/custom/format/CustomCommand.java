package me.flashyreese.mods.commandaliases.command.builder.custom.format;

import me.flashyreese.mods.commandaliases.command.CommandAlias;

import java.util.List;

/**
 * Represents a custom command
 * <p>
 * Serialization Template
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.4.0
 */
public class CustomCommand extends CommandAlias {
    private String command;
    private int permission;
    private List<CustomCommandAction> actions;
    private List<CustomCommandChild> children;
    private String message;

    public String getCommand() {
        return command;
    }

    public int getPermission() {
        return permission;
    }

    public List<CustomCommandAction> getActions() {
        return actions;
    }

    public List<CustomCommandChild> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOptional() {
        return (actions != null && !actions.isEmpty()) || (message != null && !message.isEmpty());
    }
}
