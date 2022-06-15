package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Represents the Client Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.5.0
 */
public class ClientCustomCommandBuilder extends AbstractCustomCommandBuilder<FabricClientCommandSource> {

    public ClientCustomCommandBuilder(CustomCommand commandAliasParent, CommandRegistryAccess registryAccess, AbstractDatabase<byte[], byte[]> database) {
        super(commandAliasParent, registryAccess, database);
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandContext<FabricClientCommandSource> context, String actionCommand) throws CommandSyntaxException {
        int state = 0;
        if (action.getCommandType() == CommandType.CLIENT) {
            context.getSource().getPlayer().sendChatMessage("/" + actionCommand);
            state = Command.SINGLE_SUCCESS;
        } else if (action.getCommandType() == CommandType.SERVER) {
            state = Objects.requireNonNull(context.getSource().getWorld().getServer()).getCommandManager().getDispatcher().execute(actionCommand, Objects.requireNonNull(context.getSource().getWorld().getServer()).getCommandSource());
        }
        return state;
    }

    @Override
    protected void sendFeedback(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(Text.literal(message));
    }
}
