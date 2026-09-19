package me.flashyreese.mods.commandaliases.command.builder.reassign.format;

import me.flashyreese.mods.commandaliases.command.CommandAlias;

/**
 * Represents a reassignment command
 * <p>
 * Serialization Template
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public class ReassignCommand extends CommandAlias {
    private String command;
    private String reassignTo;

    public String getCommand() {
        return command;
    }

    public String getReassignTo() {
        return reassignTo;
    }
}
