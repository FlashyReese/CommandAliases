package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.flashyreese.mods.commandaliases.storage.database.in_memory.InMemoryImpl;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.flashyreese.mods.commandaliases.storage.database.mysql.MySQLImpl;
import me.flashyreese.mods.commandaliases.storage.database.redis.RedisImpl;
import me.flashyreese.mods.commandaliases.util.CommandAliasesPlaceholders;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;

import java.lang.reflect.Field;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private static final Identifier ALIASES_REGISTRATION_PHASE_ID = Identifier.fromNamespaceAndPath("commandaliases", "register_aliases_phase");
    private final AbstractCommandAliasesProvider<CommandSourceStack> serverCommandAliasesProvider;
    private final AbstractCommandAliasesProvider<FabricClientCommandSource> clientCommandAliasesProvider;

    public CommandAliasesLoader() {
        Field literalCommandNodeLiteralField = null;
        try {
            literalCommandNodeLiteralField = LiteralCommandNode.class.getDeclaredField("literal");
            literalCommandNodeLiteralField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            CommandAliasesMod.logger().error("", e);
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
                (dispatcher, registryAccess, environment) -> {
                    this.serverCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
                    this.serverCommandAliasesProvider.loadCommandAliases();
                    this.serverCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
                });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() == null) {
                this.serverCommandAliasesProvider.setDatabase(this.openDatabase(this.createServerDatabase(server), "server"));
            }

            if (this.serverCommandAliasesProvider.getScheduler() == null) {
                this.serverCommandAliasesProvider.setScheduler(new Scheduler());
            }

            // Placeholders dynamic registration
            CommandAliasesPlaceholders.register((ServerCommandAliasesProvider) this.serverCommandAliasesProvider);
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
                this.clientCommandAliasesProvider.setDatabase(this.openDatabase(this.createClientDatabase(), "client"));
            }
            if (this.clientCommandAliasesProvider.getScheduler() == null) {
                this.clientCommandAliasesProvider.setScheduler(new Scheduler());
            }
            this.clientCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
            this.clientCommandAliasesProvider.loadCommandAliases();
            this.clientCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (this.clientCommandAliasesProvider.getDatabase() != null) {
                this.clientCommandAliasesProvider.getDatabase().close();
                this.clientCommandAliasesProvider.setDatabase(null);
            }
            if (this.clientCommandAliasesProvider.getScheduler() != null) {
                this.clientCommandAliasesProvider.setScheduler(null);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.clientCommandAliasesProvider.getScheduler() != null) {
                this.clientCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }

    private AbstractDatabase<String, String> createServerDatabase(net.minecraft.server.MinecraftServer server) {
        CommandAliasesConfig.DatabaseSettings settings = CommandAliasesMod.options().databaseSettings;
        if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
            return new LevelDBImpl(server.getWorldPath(LevelResource.ROOT).resolve("commandaliases").toString());
        } else if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
            return new MySQLImpl(settings.host, settings.port, settings.database, settings.user, settings.password, "server");
        } else if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.REDIS) {
            return new RedisImpl(settings.host, settings.port, 0, settings.user, settings.password);
        }
        return new InMemoryImpl();
    }

    private AbstractDatabase<String, String> createClientDatabase() {
        CommandAliasesConfig.DatabaseSettings settings = CommandAliasesMod.options().databaseSettings;
        if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.LEVELDB) {
            return new LevelDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString());
        } else if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.MYSQL) {
            return new MySQLImpl(settings.host, settings.port, settings.database, settings.user, settings.password, "client");
        } else if (settings.databaseMode == CommandAliasesConfig.DatabaseMode.REDIS) {
            return new RedisImpl(settings.host, settings.port, 1, settings.user, settings.password);
        }
        return new InMemoryImpl();
    }

    private AbstractDatabase<String, String> openDatabase(AbstractDatabase<String, String> database, String scope) {
        if (database != null && database.open()) {
            return database;
        }

        if (database != null) {
            database.close();
        }
        CommandAliasesMod.logger().warn("Could not open the configured {} database. Falling back to in-memory storage; data will not persist.", scope);
        AbstractDatabase<String, String> fallback = new InMemoryImpl();
        fallback.open();
        return fallback;
    }
}
