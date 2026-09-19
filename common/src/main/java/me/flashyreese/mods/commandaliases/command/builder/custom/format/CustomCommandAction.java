package me.flashyreese.mods.commandaliases.command.builder.custom.format;

import me.flashyreese.mods.commandaliases.command.CommandType;

import java.util.List;

/**
 * Represents a custom command action component
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.4.0
 */
public class CustomCommandAction {
    private String startTime;
    private String id;
    private String command;
    private CommandType commandType;
    private String message;
    private boolean requireSuccess;
    private String messageIfUnsuccessful;
    private String messageIfSuccessful;
    private List<CustomCommandAction> actionsIfUnsuccessful;
    private List<CustomCommandAction> actionsIfSuccessful;

    public String getStartTime() {
        return startTime;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRequireSuccess() {
        return requireSuccess;
    }

    public String getMessageIfUnsuccessful() {
        return messageIfUnsuccessful;
    }

    public String getMessageIfSuccessful() {
        return messageIfSuccessful;
    }

    public List<CustomCommandAction> getActionsIfUnsuccessful() {
        return actionsIfUnsuccessful;
    }

    public List<CustomCommandAction> getActionsIfSuccessful() {
        return actionsIfSuccessful;
    }
}
