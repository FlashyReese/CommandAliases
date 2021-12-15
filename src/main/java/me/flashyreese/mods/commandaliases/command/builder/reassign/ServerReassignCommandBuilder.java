/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents a server reassign command builder
 * <p>
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public class ServerReassignCommandBuilder extends AbstractReassignCommandBuilder<ServerCommandSource> {

    public ServerReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, Map<String, String> reassignCommandMap) {
        super(command, literalCommandNodeLiteralField, reassignCommandMap, CommandType.SERVER);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> reassignCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (this.reassignCommand(this.command, dispatcher)) {
            String command = this.command.getReassignCommand().getCommand().trim();
            String reassignTo = this.command.getReassignCommand().getReassignTo().trim();
            this.reassignCommandMap.put(command, reassignTo);

            //Todo: check alias/custom command literal match reassignment command, if not spit a warning.
            if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS) {
                return new AliasCommandBuilder(this.command.getAliasCommand()).buildCommand(dispatcher);
            } else if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                return new ServerCustomCommandBuilder(this.command.getCustomCommand()).buildCommand(dispatcher);
            }
        }
        return null;
    }
}
