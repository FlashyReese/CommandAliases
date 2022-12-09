package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Represents the Server Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.5.0
 */
public class ServerCustomCommandBuilder extends AbstractCustomCommandBuilder<ServerCommandSource> {
    public ServerCustomCommandBuilder(String filePath, CustomCommand commandAliasParent, AbstractCommandAliasesProvider<ServerCommandSource> abstractCommandAliasesProvider, CommandRegistryAccess registryAccess) {
        super(filePath, commandAliasParent, abstractCommandAliasesProvider, registryAccess, CommandType.SERVER);
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, String actionCommand) throws CommandSyntaxException {
        int state = 0;
        if (action.getCommandType() == CommandType.CLIENT) {
            state = dispatcher.execute(actionCommand, context.getSource());
        } else if (action.getCommandType() == CommandType.SERVER) {
            state = dispatcher.execute(actionCommand, context.getSource().getServer().getCommandSource());
        }
        return state;
    }

    @Override
    protected void sendFeedback(CommandContext<ServerCommandSource> context, String message) {
        context.getSource().sendFeedback(Text.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
    }
}
