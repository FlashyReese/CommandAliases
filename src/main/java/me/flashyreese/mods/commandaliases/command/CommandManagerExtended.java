package me.flashyreese.mods.commandaliases.command;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;

public interface CommandManagerExtended {
    CommandManager.RegistrationEnvironment getEnvironment();

    CommandRegistryAccess getCommandRegistryAccess();
}
