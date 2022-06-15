package me.flashyreese.mods.commandaliases.command.builder.alias.format;

import me.flashyreese.mods.commandaliases.command.CommandType;

import java.util.List;

/**
 * Represents an alias command
 * <p>
 * JSON Serialization Template
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.5.0
 * @deprecated As of 0.7.0, because format is no longer viable to maintain use {@link me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand} instead.
 */
@Deprecated
public class AliasCommand {
    private String command;
    private CommandType type;
    private List<AliasCommand> execution;
    private String sleep;
    private String message;
    private String reassignTo;
    private String redirectTo;
    private boolean ignoreOptionalRemoval;

    public String getCommand() {
        return command;
    }

    public CommandType getType() {
        return type;
    }

    public List<AliasCommand> getExecution() {
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
