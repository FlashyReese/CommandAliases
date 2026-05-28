package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.minecraft.commands.CommandBuildContext;
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

        // Placeholder API processor
        string = Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(string, ServerPlaceholderContext.of(context.getSource()).asParserContext()).getString();

        return string;
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<CommandSourceStack> dispatcher, CommandContext<CommandSourceStack> context, String actionCommand) throws CommandSyntaxException {
        int state = 0;
        if (action.getCommandType() == CommandType.CLIENT) {
            state = dispatcher.execute(actionCommand, context.getSource());
        } else if (action.getCommandType() == CommandType.SERVER) {
            state = dispatcher.execute(actionCommand, context.getSource().getServer().createCommandSourceStack());
        }
        return state;
    }

    @Override
    protected void sendFeedback(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
    }
}
