package me.flashyreese.mods.commandaliases.neoforge;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.loader.ClientCommandAliasesProvider;
import me.flashyreese.mods.commandaliases.command.loader.CommandAliasesLoader;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@Mod(CommandAliasesMod.MOD_ID)
public final class CommandAliasesNeoForge {
    private static final CommandAliasesNeoForgePlatform PLATFORM = new CommandAliasesNeoForgePlatform();
    private static CommandAliasesLoader<CommandSourceStack> loader;

    public CommandAliasesNeoForge() {
        CommandAliasesMod.initialize(PLATFORM);
        loader = new CommandAliasesLoader<>(new ClientCommandAliasesProvider<>(CommandAliasesLoader.findLiteralCommandNodeLiteralField()));

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                loader.registerServerCommandAliases(event.getDispatcher(), event.getBuildContext()));
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> loader.serverStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> loader.serverStopped());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> loader.serverTick());
        NeoForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> PLATFORM.registerPermissionNodes(event));
    }

    public static CommandAliasesNeoForgePlatform platform() {
        return PLATFORM;
    }

    public static CommandAliasesLoader<CommandSourceStack> loader() {
        return loader;
    }
}
