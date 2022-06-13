/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases;

import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Command Aliases Fabric mod.
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.0.9
 */
public class CommandAliasesMod implements ClientModInitializer, ModInitializer {
    private static Logger LOGGER;

    private final CommandAliasesLoader commandManager = new CommandAliasesLoader();
    private static CommandAliasesConfig CONFIG;

    public static CommandAliasesConfig options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }
    private static CommandAliasesConfig loadConfig() {
        return CommandAliasesConfig.load(FabricLoader.getInstance().getConfigDir().resolve("command-aliases-config.json").toFile());
    }

    @Override
    public void onInitializeClient() {
        this.commandManager.registerClientSidedCommandAliases();
    }

    @Override
    public void onInitialize() {
        this.commandManager.registerCommandAliases();
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LoggerFactory.getLogger("Command Aliases");
        }
        return LOGGER;
    }
}
