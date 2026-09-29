package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ClientCustomCommandBuilder<S extends SharedSuggestionProvider> extends AbstractCustomCommandBuilder<S> {
    public ClientCustomCommandBuilder(String filePath, CustomCommand commandAliasParent, AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider, CommandBuildContext registryAccess) {
        super(filePath, commandAliasParent, abstractCommandAliasesProvider, registryAccess, CommandType.CLIENT);
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<S> dispatcher, CommandContext<S> context, String actionCommand) throws CommandSyntaxException {
        return CommandAliasesMod.platform().executeClientAction(action.getCommandType(), dispatcher, context.getSource(), actionCommand);
    }

    @Override
    protected void sendFeedback(CommandContext<S> context, String message) {
        CommandAliasesMod.platform().sendClientFeedback(context.getSource(), Component.literal(message));
    }
}
