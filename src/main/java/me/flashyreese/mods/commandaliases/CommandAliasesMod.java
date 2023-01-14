package me.flashyreese.mods.commandaliases;

import me.flashyreese.mods.commandaliases.command.loader.CommandAliasesLoader;
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
 * @version 0.7.0
 * @since 0.0.9
 */
public class CommandAliasesMod implements ClientModInitializer, ModInitializer {
    public static final String MOD_ID = "commandaliases";
    private static Logger LOGGER;
    private static CommandAliasesConfig CONFIG;
    private final CommandAliasesLoader commandManager = new CommandAliasesLoader();

    public static CommandAliasesConfig options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    private static CommandAliasesConfig loadConfig() {
        return CommandAliasesConfig.load(FabricLoader.getInstance().getConfigDir().resolve("command-aliases-config.json").toFile());
    }

    public static Logger logger() {
        if (LOGGER == null) {
            LOGGER = LoggerFactory.getLogger("Command Aliases");
        }
        return LOGGER;
    }

    @Override
    public void onInitializeClient() {
        this.commandManager.registerClientSidedCommandAliases();
    }

    @Override
    public void onInitialize() {
        this.commandManager.registerCommandAliases();
    }
}
