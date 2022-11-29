package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ServerCommandAliasesProvider extends AbstractCommandAliasesProvider<ServerCommandSource> {
    public ServerCommandAliasesProvider(Field literalCommandNodeLiteralField) {
        super(FabricLoader.getInstance().getConfigDir().resolve("commandaliases"), literalCommandNodeLiteralField, "commandaliases", CommandType.SERVER);
    }

    @Override
    protected void sendFeedback(ServerCommandSource source, Text text) {
        source.sendFeedback(text, CommandAliasesMod.options().debugSettings.broadcastToOps);
    }

    @Override
    protected int commandAliasesLoad(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Loading all Command Aliases!"));
        this.loadCommandAliases();
        this.registerCommands(dispatcher);

        for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
            context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
        }
        this.sendFeedback(context.getSource(), new LiteralText("Loaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesUnload(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Unloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);

        for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
            context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
        }
        this.sendFeedback(context.getSource(), new LiteralText("Unloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesReload(CommandContext<ServerCommandSource> context, CommandDispatcher<ServerCommandSource> dispatcher) {
        this.sendFeedback(context.getSource(), new LiteralText("Reloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.loadCommandAliases();
        this.registerCommands(dispatcher);

        //Update Command Tree
        for (ServerPlayerEntity e : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
            context.getSource().getMinecraftServer().getPlayerManager().sendCommandTree(e);
        }

        this.sendFeedback(context.getSource(), new LiteralText("Reloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> buildCustomCommand(String filePath, CustomCommand customCommand, AbstractCommandAliasesProvider<ServerCommandSource> abstractCommandAliasesProvider, CommandDispatcher<ServerCommandSource> dispatcher) {
        return new ServerCustomCommandBuilder(filePath, customCommand, abstractCommandAliasesProvider).buildCommand(dispatcher);
    }
}
