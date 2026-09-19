package me.flashyreese.mods.commandaliases.fabric;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.loader.ClientCommandAliasesProvider;
import me.flashyreese.mods.commandaliases.command.loader.CommandAliasesLoader;
import me.flashyreese.mods.commandaliases.util.CommandAliasesPlaceholders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;

public final class CommandAliasesFabric implements ModInitializer, ClientModInitializer {
    private static final Identifier ALIASES_REGISTRATION_PHASE_ID = Identifier.fromNamespaceAndPath(CommandAliasesMod.MOD_ID, "register_aliases_phase");
    private static CommandAliasesLoader<FabricClientCommandSource> loader;

    private static synchronized CommandAliasesLoader<FabricClientCommandSource> loader() {
        if (loader == null) {
            CommandAliasesMod.initialize(new CommandAliasesFabricPlatform());
            Field literalField = CommandAliasesLoader.findLiteralCommandNodeLiteralField();
            loader = new CommandAliasesLoader<>(new ClientCommandAliasesProvider<>(literalField));
        }
        return loader;
    }

    @Override
    public void onInitialize() {
        CommandAliasesLoader<FabricClientCommandSource> aliasesLoader = loader();

        CommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, ALIASES_REGISTRATION_PHASE_ID);
        CommandRegistrationCallback.EVENT.register(ALIASES_REGISTRATION_PHASE_ID, (dispatcher, registryAccess, environment) ->
                aliasesLoader.registerServerCommandAliases(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTED.register(aliasesLoader::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> aliasesLoader.serverStopped());
        ServerTickEvents.END_SERVER_TICK.register(_ -> aliasesLoader.serverTick());
        ServerLifecycleEvents.SERVER_STARTED.register(_ ->
                CommandAliasesPlaceholders.register(aliasesLoader.serverCommandAliasesProvider()));
    }

    @Override
    public void onInitializeClient() {
        CommandAliasesLoader<FabricClientCommandSource> aliasesLoader = loader();

        ClientCommandRegistrationCallback.EVENT.register(aliasesLoader::registerClientCommandAliases);
        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> aliasesLoader.clientStopped());
        ClientTickEvents.END_CLIENT_TICK.register(_ -> aliasesLoader.clientTick());
    }
}
