package me.flashyreese.mods.commandaliases.command;

import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.reassign.format.ReassignCommand;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;

/**
 * Represents the CommandAliases Command
 * <p>
 * JSON Serialization Template
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
