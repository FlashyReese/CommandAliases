package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ServerCommandAliasesProvider extends AbstractCommandAliasesProvider<ServerCommandSource> {
    public ServerCommandAliasesProvider(Field literalCommandNodeLiteralField) {
        super(FabricLoader.getInstance().getConfigDir().resolve("commandaliases"), literalCommandNodeLiteralField, "commandaliases");
    }

    @Override
    protected void sendFeedback(ServerCommandSource source, Text text) {
        source.sendFeedback(text, CommandAliasesMod.options().debugSettings.broadcastToOps);
    }

    @Override
    protected int commandAliasesLoad(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Loading all Command Aliases!"));
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);

        for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            context.getSource().getServer().getPlayerManager().sendCommandTree(e);
        }
        this.sendFeedback(context.getSource(), Text.literal("Loaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesUnload(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Unloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);

        for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            context.getSource().getServer().getPlayerManager().sendCommandTree(e);
        }
        this.sendFeedback(context.getSource(), Text.literal("Unloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesReload(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.sendFeedback(context.getSource(), Text.literal("Reloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);

        //Update Command Tree
        for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            context.getSource().getServer().getPlayerManager().sendCommandTree(e);
        }

        this.sendFeedback(context.getSource(), Text.literal("Reloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> buildCustomCommand(CustomCommand customCommand, AbstractCommandAliasesProvider<ServerCommandSource> abstractCommandAliasesProvider, CommandRegistryAccess registryAccess, CommandDispatcher<ServerCommandSource> dispatcher) {
        return new ServerCustomCommandBuilder(customCommand, abstractCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
    }
}
