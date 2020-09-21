/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the Command Aliases Fabric mod.
 *
 * @author FlashyReese
 * @version 0.1.3
 * @since 0.0.9
 */
public class CommandAliasesMod implements ModInitializer {
    private static Logger LOGGER;

    private CommandAliasesLoader commandManager;

    @Override
    public void onInitialize() {
        commandManager = new CommandAliasesLoader();
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("Command Aliases");
        }
        return LOGGER;
    }
}
