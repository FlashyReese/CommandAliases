package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * Represents the Client Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.9.0
 * @since 0.5.0
 */
public class ClientCustomCommandBuilder extends AbstractCustomCommandBuilder<FabricClientCommandSource> {
    public ClientCustomCommandBuilder(String filePath, CustomCommand commandAliasParent, AbstractCommandAliasesProvider<FabricClientCommandSource> abstractCommandAliasesProvider, CommandBuildContext registryAccess) {
        super(filePath, commandAliasParent, abstractCommandAliasesProvider, registryAccess, CommandType.CLIENT);
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandContext<FabricClientCommandSource> context, String actionCommand) throws CommandSyntaxException {
        int state = 0;
        if (action.getCommandType() == CommandType.CLIENT) {
            state = dispatcher.execute(actionCommand, context.getSource());
        } else if (action.getCommandType() == CommandType.SERVER) {
            context.getSource().getPlayer().connection.sendCommand(actionCommand); // Todo: Dangerous might cause abuse by spammers
            state = Command.SINGLE_SUCCESS;
        }
        return state;
    }

    @Override
    protected void sendFeedback(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(Component.literal(message));
    }
}
