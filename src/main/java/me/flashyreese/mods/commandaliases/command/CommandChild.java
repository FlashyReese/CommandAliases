/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command;

import java.util.List;

/**
 * Represents the CommandAliases Custom Command Child Component
 *
 * @author FlashyReese
 * @version 0.4.0
 * @since 0.4.0
 */
public class CommandChild {
    private String child;
    private String type;
    private String argumentType;
    private int permission;
    private List<CommandAction> actions;
    private List<CommandChild> children;
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

    public List<CommandAction> getActions() {
        return actions;
    }

    public List<CommandChild> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOptional() {
        return (actions != null && !actions.isEmpty()) || (message != null && !message.isEmpty());
    }
}
