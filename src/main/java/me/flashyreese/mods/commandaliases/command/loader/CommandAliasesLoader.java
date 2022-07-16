package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.flashyreese.mods.commandaliases.storage.database.mysql.MySQLImpl;
import me.flashyreese.mods.commandaliases.storage.database.rocksdb.RocksDBImpl;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            this.serverCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
            this.serverCommandAliasesProvider.loadCommandAliases();
            this.serverCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() == null) {
                if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
                    this.serverCommandAliasesProvider.setDatabase(new LevelDBImpl(server.getSavePath(WorldSavePath.ROOT).resolve("commandaliases").toString()));
                } /*else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.ROCKSDB) {
                    this.serverCommandAliasesProvider.setDatabase(new RocksDBImpl(server.getSavePath(WorldSavePath.ROOT).resolve("commandaliases").toString()));
                } */else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
                    this.serverCommandAliasesProvider.setDatabase(new MySQLImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.database, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password, "server"));
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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            if (this.clientCommandAliasesProvider.getDatabase() == null) {
                if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
                    this.clientCommandAliasesProvider.setDatabase(new LevelDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString()));
                } /*else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.ROCKSDB) {
                    this.clientCommandAliasesProvider.setDatabase(new RocksDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString()));
                } */else if (CommandAliasesMod.options().databaseSettings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
                    this.clientCommandAliasesProvider.setDatabase(new MySQLImpl(CommandAliasesMod.options().databaseSettings.host, CommandAliasesMod.options().databaseSettings.database, CommandAliasesMod.options().databaseSettings.user, CommandAliasesMod.options().databaseSettings.password, "client"));
                }
                this.clientCommandAliasesProvider.getDatabase().open();
            }
            if (this.clientCommandAliasesProvider.getScheduler() == null) {
                this.clientCommandAliasesProvider.setScheduler(new Scheduler());
            }
            this.clientCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
            this.clientCommandAliasesProvider.loadCommandAliases();
            this.clientCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.clientCommandAliasesProvider.getScheduler() != null) {
                this.clientCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }
}
