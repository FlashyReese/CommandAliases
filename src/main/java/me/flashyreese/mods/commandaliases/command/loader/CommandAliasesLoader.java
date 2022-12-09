package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import me.flashyreese.mods.commandaliases.storage.database.in_memory.InMemoryImpl;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.flashyreese.mods.commandaliases.storage.database.mysql.MySQLImpl;
import me.flashyreese.mods.commandaliases.storage.database.redis.RedisImpl;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.lang.reflect.Field;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private static final Identifier ALIASES_REGISTRATION_PHASE_ID = new Identifier("commandaliases", "register_aliases_phase");
    private final AbstractCommandAliasesProvider<ServerCommandSource> serverCommandAliasesProvider;
    private final AbstractCommandAliasesProvider<FabricClientCommandSource> clientCommandAliasesProvider;

    public CommandAliasesLoader() {
        Field literalCommandNodeLiteralField = null;
        try {
            literalCommandNodeLiteralField = LiteralCommandNode.class.getDeclaredField("literal");
            literalCommandNodeLiteralField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        this.serverCommandAliasesProvider = new ServerCommandAliasesProvider(literalCommandNodeLiteralField);
        this.clientCommandAliasesProvider = new ClientCommandAliasesProvider(literalCommandNodeLiteralField);
    }

    public void registerCommandAliases() {
        // CommandAliases must perform registration after all other mods, so that mod-added commands can be referenced
        // in Aliases. We add our own phase that must execute after the default phase to achieve this.
        CommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, ALIASES_REGISTRATION_PHASE_ID);
        CommandRegistrationCallback.EVENT.register(
                ALIASES_REGISTRATION_PHASE_ID,
                (dispatcher, environment) -> {
                    this.serverCommandAliasesProvider.registerCommandAliasesCommands(dispatcher);
                    this.serverCommandAliasesProvider.loadCommandAliases();
                    this.serverCommandAliasesProvider.registerCommands(dispatcher);
                });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() == null) {
                if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.IN_MEMORY) {
                    this.serverCommandAliasesProvider.setDatabase(new InMemoryImpl());
                } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
                    this.serverCommandAliasesProvider.setDatabase(new LevelDBImpl(server.getSavePath(WorldSavePath.ROOT).resolve("commandaliases").toString()));
                } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
                    this.serverCommandAliasesProvider.setDatabase(new MySQLImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.port, CommandAliasesMod.options().databaseSettings.database, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password, "server"));
                } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.REDIS) {
                    this.serverCommandAliasesProvider.setDatabase(new RedisImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.port, 0, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password));
                }
                this.serverCommandAliasesProvider.getDatabase().open();
            }

            if (this.serverCommandAliasesProvider.getScheduler() == null) {
                this.serverCommandAliasesProvider.setScheduler(new Scheduler());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() != null) {
                this.serverCommandAliasesProvider.getDatabase().close();
                this.serverCommandAliasesProvider.setDatabase(null);
            }
            if (this.serverCommandAliasesProvider.getScheduler() != null) {
                this.serverCommandAliasesProvider.setScheduler(null);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (this.serverCommandAliasesProvider.getScheduler() != null) {
                this.serverCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }

    public void registerClientSidedCommandAliases() {
        if (this.clientCommandAliasesProvider.getDatabase() == null) {
            if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.IN_MEMORY) {
                this.clientCommandAliasesProvider.setDatabase(new InMemoryImpl());
            } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
                this.clientCommandAliasesProvider.setDatabase(new LevelDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString()));
            } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
                this.clientCommandAliasesProvider.setDatabase(new MySQLImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.port, CommandAliasesMod.options().databaseSettings.database, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password, "client"));
            } else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.REDIS) {
                this.clientCommandAliasesProvider.setDatabase(new RedisImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.port, 1, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password));
            }
            this.clientCommandAliasesProvider.getDatabase().open();
        }
        if (this.clientCommandAliasesProvider.getScheduler() == null) {
            this.clientCommandAliasesProvider.setScheduler(new Scheduler());
        }
        this.clientCommandAliasesProvider.registerCommandAliasesCommands(ClientCommandManager.DISPATCHER);
        this.clientCommandAliasesProvider.loadCommandAliases();
        this.clientCommandAliasesProvider.registerCommands(ClientCommandManager.DISPATCHER);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.clientCommandAliasesProvider.getScheduler() != null) {
                this.clientCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }
}
