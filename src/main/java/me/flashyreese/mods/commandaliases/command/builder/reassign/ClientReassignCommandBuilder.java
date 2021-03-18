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
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents a client reassign command builder
 * <p>
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public class ClientReassignCommandBuilder extends AbstractReassignCommandBuilder<FabricClientCommandSource> {

    private final CommandDispatcher<ServerCommandSource> serverCommandDispatcher;

    public ClientReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, Map<String, String> reassignClientCommandMap, CommandDispatcher<ServerCommandSource> serverCommandDispatcher) {
        super(command, literalCommandNodeLiteralField, reassignClientCommandMap);
        this.serverCommandDispatcher = serverCommandDispatcher;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> reassignCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (this.reassignCommand(this.command, dispatcher)) {
            String command = this.command.getReassignCommand().getCommand().trim();
            String reassignTo = this.command.getReassignCommand().getCommand().trim();
            this.reassignCommandMap.put(command, reassignTo);

            //Fixme: Do I want to support a deprecated format?
            /*if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS) {
                return new CommandAliasesBuilder(this.command).buildCommand(dispatcher);
            } else */
            if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                return new ClientCustomCommandBuilder(this.command.getCustomCommand(), this.serverCommandDispatcher).buildCommand(dispatcher);
            }
        }
        return null;
    }
}
