package me.flashyreese.mods.commandaliases.command;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;

/**
 * Accessor for CommandManager
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public interface CommandManagerExtended {
    CommandManager.RegistrationEnvironment getEnvironment();

    CommandRegistryAccess getCommandRegistryAccess();
}
