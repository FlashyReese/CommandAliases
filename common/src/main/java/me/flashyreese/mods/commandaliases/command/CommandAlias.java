package me.flashyreese.mods.commandaliases.command;

/**
 * Represents the CommandAliases Command
 * <p>
 * Base Serialization Template
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.0.9
 */
public class CommandAlias {
    private int schemaVersion;
    private CommandMode commandMode;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public CommandMode getCommandMode() {
        return commandMode;
    }
}
