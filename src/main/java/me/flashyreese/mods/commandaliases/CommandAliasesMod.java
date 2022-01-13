/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the Command Aliases Fabric mod.
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.0.9
 */
public class CommandAliasesMod implements ModInitializer, ClientModInitializer {
    private static Logger LOGGER;

    private final CommandAliasesLoader commandManager = new CommandAliasesLoader();

    @Override
    public void onInitialize() {
        this.commandManager.registerCommandAliases();
        //fixme:
        //ServerLifecycleEvents.SERVER_STARTED.register((server -> this.commandManager.registerCommandAliases()));
    }

    @Override
    public void onInitializeClient() {
        this.commandManager.registerClientSidedCommandAliases();
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("Command Aliases");
        }
        return LOGGER;
    }
}
