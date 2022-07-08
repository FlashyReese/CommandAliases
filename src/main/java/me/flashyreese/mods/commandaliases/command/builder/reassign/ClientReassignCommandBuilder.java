package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents a client reassign command builder
 * <p>
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.5.0
 */
public class ClientReassignCommandBuilder extends AbstractReassignCommandBuilder<FabricClientCommandSource> {

    public ClientReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, Map<String, String> reassignClientCommandMap, AbstractDatabase<byte[], byte[]> database, Scheduler scheduler) {
        super(command, literalCommandNodeLiteralField, reassignClientCommandMap, CommandType.CLIENT, database, scheduler);
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> reassignCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (this.reassignCommand(this.command, dispatcher)) {
            String command = this.command.getReassignCommand().getCommand().trim();
            String reassignTo = this.command.getReassignCommand().getReassignTo().trim();
            this.reassignCommandMap.put(command, reassignTo);

            if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                return new ClientCustomCommandBuilder(this.command.getCustomCommand(), this.database, this.scheduler).buildCommand(dispatcher);
            }
        }
        return null;
    }
}
