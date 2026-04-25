package me.flashyreese.mods.commandaliases.command;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;

/**
 * Accessor for CommandManager
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public interface CommandManagerExtended {
    Commands.CommandSelection getEnvironment();

    CommandBuildContext getCommandRegistryAccess();
}
