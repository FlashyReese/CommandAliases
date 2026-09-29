package me.flashyreese.mods.commandaliases.neoforge;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;

@EventBusSubscriber(modid = CommandAliasesMod.MOD_ID, value = Dist.CLIENT)
public final class CommandAliasesNeoForgeClient {
    private static void initializePlatform() {
        CommandAliasesNeoForge.platform().installClientPlatform(new CommandAliasesNeoForgeClientPlatform());
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        initializePlatform();
        CommandAliasesNeoForge.loader().registerClientCommandAliases(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        CommandAliasesNeoForge.loader().clientTick();
    }

    @SubscribeEvent
    public static void clientStopping(ClientStoppingEvent event) {
        CommandAliasesNeoForge.loader().clientStopped();
    }
}
