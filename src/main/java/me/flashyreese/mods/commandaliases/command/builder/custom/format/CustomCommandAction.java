package me.flashyreese.mods.commandaliases.command.builder.custom.format;

import me.flashyreese.mods.commandaliases.command.CommandType;

import java.util.List;

/**
 * Represents a custom command Action
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.4.0
 */
public class CustomCommandAction {
    private String command;
    private CommandType commandType;
    private String sleep;
    private String message;
    private boolean requireSuccess;
    private String unsuccessfulMessage;
    private String successfulMessage;
    private List<CustomCommandAction> unsuccessfulActions;

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

    public boolean isRequireSuccess() {
        return requireSuccess;
    }

    public String getUnsuccessfulMessage() {
        return unsuccessfulMessage;
    }

    public String getSuccessfulMessage() {
        return successfulMessage;
    }

    public List<CustomCommandAction> getUnsuccessfulActions() {
        return unsuccessfulActions;
    }
}
