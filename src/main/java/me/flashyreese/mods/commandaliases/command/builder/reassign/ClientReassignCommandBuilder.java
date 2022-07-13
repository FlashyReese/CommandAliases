package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.mods.commandaliases.CommandAliasesProvider;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.lang.reflect.Field;

/**
 * Represents a client reassign command builder
 * <p>
 *
 * @author FlashyReese
 * @version 0.9.0
 * @since 0.5.0
 */
public class ClientReassignCommandBuilder extends AbstractReassignCommandBuilder<FabricClientCommandSource> {

    public ClientReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, CommandAliasesProvider commandAliasesProvider, CommandRegistryAccess registryAccess) {
        super(command, literalCommandNodeLiteralField, commandAliasesProvider, CommandType.CLIENT, registryAccess);
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> reassignCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (this.reassignCommand(this.command, dispatcher)) {
            String command = this.command.getReassignCommand().getCommand().trim();
            String reassignTo = this.command.getReassignCommand().getReassignTo().trim();
            this.commandAliasesProvider.getReassignedCommandMap().put(command, reassignTo);

            if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                return new ClientCustomCommandBuilder(this.command.getCustomCommand(), this.commandAliasesProvider, this.registryAccess).buildCommand(dispatcher);
            }
        }
        return null;
    }
}
