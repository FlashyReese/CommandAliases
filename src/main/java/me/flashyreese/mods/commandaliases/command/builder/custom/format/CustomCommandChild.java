package me.flashyreese.mods.commandaliases.command.builder.custom.format;

import java.util.List;

/**
 * Represents a custom command child component
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.4.0
 */
public class CustomCommandChild {
    private String child;
    private String type;
    private String argumentType;
    private CustomCommandSuggestionProvider suggestionProvider;
    private int permission;
    private List<CustomCommandAction> actions;
    private List<CustomCommandChild> children;
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

    public CustomCommandSuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
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
