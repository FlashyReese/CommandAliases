package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ClientCommandAliasesProvider extends AbstractCommandAliasesProvider<FabricClientCommandSource> {
    public ClientCommandAliasesProvider(Field literalCommandNodeLiteralField) {
        super(FabricLoader.getInstance().getConfigDir().resolve("commandaliases-client"), literalCommandNodeLiteralField, "commandaliases:client", CommandType.CLIENT);
    }

    @Override
    protected void sendFeedback(FabricClientCommandSource source, Text text) {
        source.sendFeedback(text);
    }

    @Override
    protected int commandAliasesLoad(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Loading all client Command Aliases!"));
        this.loadCommandAliases();
        this.registerCommands(dispatcher);
        this.sendFeedback(context.getSource(), new LiteralText("Loaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesUnload(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Unloading all client Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.sendFeedback(context.getSource(), new LiteralText("Unloaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesReload(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Reloading all client Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.loadCommandAliases();
        this.registerCommands(dispatcher);
        this.sendFeedback(context.getSource(), new LiteralText("Reloaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> buildCustomCommand(String filePath, CustomCommand customCommand, AbstractCommandAliasesProvider<FabricClientCommandSource> abstractCommandAliasesProvider, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return new ClientCustomCommandBuilder(filePath, customCommand, abstractCommandAliasesProvider).buildCommand(dispatcher);
    }
}
