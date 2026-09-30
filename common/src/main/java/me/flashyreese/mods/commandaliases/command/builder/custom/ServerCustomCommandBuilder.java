package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Represents the Server Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.5.0
 */
public class ServerCustomCommandBuilder extends AbstractCustomCommandBuilder<CommandSourceStack> {
    public ServerCustomCommandBuilder(String filePath, CustomCommand commandAliasParent, AbstractCommandAliasesProvider<CommandSourceStack> abstractCommandAliasesProvider, CommandBuildContext registryAccess) {
        super(filePath, commandAliasParent, abstractCommandAliasesProvider, registryAccess, CommandType.SERVER);
    }

    @Override
    protected String formatString(CommandContext<CommandSourceStack> context, List<String> currentInputList, String string) {
        string = super.formatString(context, currentInputList, string);

        string = CommandAliasesMod.platform().processServerPlaceholders(context.getSource(), string);

        return string;
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<CommandSourceStack> dispatcher, CommandContext<CommandSourceStack> context, String actionCommand) throws CommandSyntaxException {
        CommandSourceStack source = null;
        if (action.getCommandType() == CommandType.CLIENT) {
            source = context.getSource();
        } else if (action.getCommandType() == CommandType.SERVER) {
            source = context.getSource().getServer().createCommandSourceStack();
        }
        if (source == null) {
            return 0;
        }
        int[] commandResult = {0};
        CommandResultCallback resultCallback = (successful, result) -> {
            // A fork may report multiple results. Any successful nonzero result succeeds the action.
            if (successful && result != 0) {
                commandResult[0] = Command.SINGLE_SUCCESS;
            }
        };
        CommandSourceStack executionSource = source.withCallback(CommandResultCallback.chain(source.callback(), resultCallback));

        // Actions run from the tick scheduler, outside an existing Minecraft command execution context.
        // This call drains its queue before returning, including when /return discards queued commands.
        // Keep Minecraft's normal parsing, error reporting, and exception handling.
        source.getServer().getCommands().performPrefixedCommand(executionSource, actionCommand);
        return commandResult[0];
    }

    @Override
    protected void sendFeedback(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
    }
}
