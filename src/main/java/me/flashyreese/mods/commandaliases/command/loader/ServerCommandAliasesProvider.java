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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;

public class ServerCommandAliasesProvider extends AbstractCommandAliasesProvider<CommandSourceStack> {
    public ServerCommandAliasesProvider(Field literalCommandNodeLiteralField) {
        super(FabricLoader.getInstance().getConfigDir().resolve("commandaliases"), literalCommandNodeLiteralField, "commandaliases", CommandType.SERVER);
    }

    @Override
    protected void sendFeedback(CommandSourceStack source, Component text) {
        source.sendSuccess(() -> text, CommandAliasesMod.options().debugSettings.broadcastToOps);
    }

    @Override
    protected int commandAliasesLoad(CommandContext<CommandSourceStack> context, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        this.sendFeedback(context.getSource(), Component.literal("Loading all Command Aliases!"));
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);

        for (ServerPlayer e : context.getSource().getServer().getPlayerList().getPlayers()) {
            context.getSource().getServer().getCommands().sendCommands(e);
        }
        this.sendFeedback(context.getSource(), Component.literal("Loaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesUnload(CommandContext<CommandSourceStack> context, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        this.sendFeedback(context.getSource(), Component.literal("Unloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);

        for (ServerPlayer e : context.getSource().getServer().getPlayerList().getPlayers()) {
            context.getSource().getServer().getCommands().sendCommands(e);
        }
        this.sendFeedback(context.getSource(), Component.literal("Unloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int commandAliasesReload(CommandContext<CommandSourceStack> context, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        this.sendFeedback(context.getSource(), Component.literal("Reloading all Command Aliases!"));
        this.unregisterCommands(dispatcher);
        this.loadCommandAliases();
        this.registerCommands(dispatcher, registryAccess);

        for (ServerPlayer e : context.getSource().getServer().getPlayerList().getPlayers()) {
            context.getSource().getServer().getCommands().sendCommands(e);
        }

        this.sendFeedback(context.getSource(), Component.literal("Reloaded all Command Aliases!"));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> buildCustomCommand(String filePath, CustomCommand customCommand, AbstractCommandAliasesProvider<CommandSourceStack> abstractCommandAliasesProvider, CommandBuildContext registryAccess, CommandDispatcher<CommandSourceStack> dispatcher) {
        return new ServerCustomCommandBuilder(filePath, customCommand, abstractCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
    }
}
