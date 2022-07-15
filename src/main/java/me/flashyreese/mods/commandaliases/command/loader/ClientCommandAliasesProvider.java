package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ClientCommandAliasesProvider extends AbstractCommandAliasesProvider<FabricClientCommandSource> {
    public ClientCommandAliasesProvider(Field literalCommandNodeLiteralField) {
        super(FabricLoader.getInstance().getConfigDir().resolve("commandaliases-client"), literalCommandNodeLiteralField, "commandaliases:client");
    }

    @Override
    protected void sendFeedback(FabricClientCommandSource source, Text text) {
        source.sendFeedback(text);
    }

    @Override
    protected int commandAliasesLoad(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Loading all client Command Aliases!"));
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);
        this.sendFeedback(context.getSource(), Text.literal("Loaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesUnload(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Unloading all client Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.sendFeedback(context.getSource(), Text.literal("Unloaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesReload(CommandContext<FabricClientCommandSource> context, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Reloading all client Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);
        this.sendFeedback(context.getSource(), Text.literal("Reloaded all client Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> buildCustomCommand(CustomCommand customCommand, AbstractCommandAliasesProvider<FabricClientCommandSource> abstractCommandAliasesProvider, CommandRegistryAccess registryAccess, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return new ClientCustomCommandBuilder(customCommand, abstractCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
    }
}
