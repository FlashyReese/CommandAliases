/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command;

import me.flashyreese.mods.commandaliases.command.builder.alias.format.AliasCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.reassign.format.ReassignCommand;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;

/**
 * Represents the CommandAliases Command
 * <p>
 * JSON Serialization Template
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.0.9
 */
public class CommandAlias {
    private CommandMode commandMode;
    private CustomCommand customCommand;
    private ReassignCommand reassignCommand;
    private RedirectCommand redirectCommand;
    private AliasCommand aliasCommand;

    public CommandMode getCommandMode() {
        return commandMode;
    }

    public CustomCommand getCustomCommand() {
        return customCommand;
    }

    public ReassignCommand getReassignCommand() {
        return reassignCommand;
    }

    public RedirectCommand getRedirectCommand() {
        return redirectCommand;
    }

    public AliasCommand getAliasCommand() {
        return aliasCommand;
    }
}
