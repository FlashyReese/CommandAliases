package me.flashyreese.mods.commandaliases;

import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import me.flashyreese.mods.commandaliases.platform.CommandAliasesPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common mod state shared by the Fabric and NeoForge entrypoints.
 */
public final class CommandAliasesMod {
    public static final String MOD_ID = "commandaliases";

    private static final Logger LOGGER = LoggerFactory.getLogger("Command Aliases");
    private static volatile CommandAliasesPlatform PLATFORM;
    private static volatile CommandAliasesConfig CONFIG;

    private CommandAliasesMod() {
    }

    public static synchronized void initialize(CommandAliasesPlatform platform) {
        if (PLATFORM == null) {
            PLATFORM = platform;
        }
    }

    public static CommandAliasesPlatform platform() {
        CommandAliasesPlatform platform = PLATFORM;
        if (platform == null) {
            throw new IllegalStateException("Command Aliases platform has not been initialized");
        }
        return platform;
    }

    public static CommandAliasesConfig options() {
        CommandAliasesConfig config = CONFIG;
        if (config == null) {
            synchronized (CommandAliasesMod.class) {
                config = CONFIG;
                if (config == null) {
                    CONFIG = config = CommandAliasesConfig.load(platform().configDirectory().resolve("command-aliases-config.json").toFile());
                }
            }
        }
        return config;
    }

    public static Logger logger() {
        return LOGGER;
    }
}
