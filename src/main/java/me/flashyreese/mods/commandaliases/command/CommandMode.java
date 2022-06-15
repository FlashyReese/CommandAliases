package me.flashyreese.mods.commandaliases.command;

/**
 * Represents the CommandAliases Command Type
 *
 * @author FlashyReese
 * @version 0.4.0
 * @since 0.3.0
 */
public enum CommandMode {
    /**
     * @deprecated As of 0.7.0, because format is no longer viable to maintain use {@link me.flashyreese.mods.commandaliases.command.CommandMode#COMMAND_CUSTOM} instead.
     */
    COMMAND_ALIAS,
    COMMAND_CUSTOM,
    COMMAND_REASSIGN,
    /**
     * @deprecated As of 0.7.0, because format is no longer viable to maintain use {@link me.flashyreese.mods.commandaliases.command.CommandMode#COMMAND_REASSIGN_AND_CUSTOM} instead.
     */
    COMMAND_REASSIGN_AND_ALIAS,
    COMMAND_REASSIGN_AND_CUSTOM,
    COMMAND_REDIRECT,
    COMMAND_REDIRECT_NOARG
}
