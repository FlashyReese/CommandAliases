package me.flashyreese.mods.commandaliases;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandAliasesMod implements ModInitializer {
	private static Logger LOGGER;

	private CommandAliasesManager commandManager;

	@Override
	public void onInitialize() {
		commandManager = new CommandAliasesManager();
	}

	public static Logger getLogger() {
		if (LOGGER == null) {
			LOGGER = LogManager.getLogger("Command Aliases");
		}
		return LOGGER;
	}
}
