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
 * Represents the CommandAliases Command
 * <p>
 * JSON Serialization Template
 *
 * @author FlashyReese
 * @version 0.2.0
 * @since 0.0.9
 */
public class CommandAlias {
    private String command;
    private CommandType type;
    private List<CommandAlias> execution;
    private String sleep;
    private String message;

    private boolean reassignOnly;
    private String reassignOriginal;

    public String getCommand() {
        return command;
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

    public boolean isReassignOnly() {
        return reassignOnly;
    }

    public String getReassignOriginal() {
        return reassignOriginal;
    }
}
