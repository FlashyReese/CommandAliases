/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command;

/**
 * Represents the CommandAliases Custom Command Action
 *
 * @author FlashyReese
 * @version 0.4.0
 * @since 0.4.0
 */
public class CommandAction {
    private String command;
    private CommandType commandType;
    private String sleep;
    private String message;

    public String getCommand() {
        return command;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getSleep() {
        return sleep;
    }

    public String getMessage() {
        return message;
    }
}
