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
 * @version 0.4.0
 * @since 0.0.9
 */
public class CommandAlias {
    private CommandMode commandMode;
    private String command;
    private CommandParent customCommand;
    private CommandType type;
    private List<CommandAlias> execution;
    private String sleep;
    private String message;
    private String reassignTo;
    private String redirectTo;
    private boolean ignoreOptionalRemoval;

    public CommandMode getCommandMode() {
        return commandMode;
    }

    public String getCommand() {
        return command;
    }

    public CommandParent getCustomCommand() {
        return customCommand;
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

    public String getReassignTo() {
        return reassignTo;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public boolean isIgnoreOptionalRemoval() {
        return ignoreOptionalRemoval;
    }
}
