/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.custom.format;

import java.util.List;

/**
 * Represents a custom command
 * <p>
 * JSON Serialization Template
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.4.0
 */
public class CustomCommand {
    private String parent;
    private int permission;
    private List<CustomCommandAction> actions;
    private List<CustomCommandChild> children;
    private String message;

    public String getParent() {
        return parent;
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
