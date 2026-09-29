package me.flashyreese.mods.commandaliases.command.loader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.config.CommandAliasesConfig;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.flashyreese.mods.commandaliases.storage.database.in_memory.InMemoryImpl;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.flashyreese.mods.commandaliases.storage.database.mysql.MySQLImpl;
import me.flashyreese.mods.commandaliases.storage.database.redis.RedisImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.storage.LevelResource;

import java.lang.reflect.Field;

/**
 * Owns the common command alias lifecycle. Platform entrypoints only forward
 * their command and lifecycle events to this class.
 */
public class CommandAliasesLoader<S extends SharedSuggestionProvider> {
    private final ServerCommandAliasesProvider serverCommandAliasesProvider;
    private final ClientCommandAliasesProvider<S> clientCommandAliasesProvider;

    public CommandAliasesLoader(ClientCommandAliasesProvider<S> clientCommandAliasesProvider) {
        Field literalCommandNodeLiteralField = findLiteralCommandNodeLiteralField();
        this.serverCommandAliasesProvider = new ServerCommandAliasesProvider(literalCommandNodeLiteralField);
        this.clientCommandAliasesProvider = clientCommandAliasesProvider;
    }

    public static Field findLiteralCommandNodeLiteralField() {
        try {
            Field field = LiteralCommandNode.class.getDeclaredField("literal");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            CommandAliasesMod.logger().error("Could not access the Brigadier literal command field", e);
            return null;
        }
    }

    public void registerServerCommandAliases(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        this.serverCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
        this.serverCommandAliasesProvider.loadCommandAliases();
        this.serverCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
    }

    public void serverStarted(net.minecraft.server.MinecraftServer server) {
        if (this.serverCommandAliasesProvider.getDatabase() == null) {
            this.serverCommandAliasesProvider.setDatabase(this.openDatabase(this.createServerDatabase(server), "server"));
        }
        if (this.serverCommandAliasesProvider.getScheduler() == null) {
            this.serverCommandAliasesProvider.setScheduler(new Scheduler());
        }
    }

    public void serverStopped() {
        if (this.serverCommandAliasesProvider.getDatabase() != null) {
            this.serverCommandAliasesProvider.getDatabase().close();
            this.serverCommandAliasesProvider.setDatabase(null);
        }
        this.serverCommandAliasesProvider.setScheduler(null);
    }

    public void serverTick() {
        if (this.serverCommandAliasesProvider.getScheduler() != null) {
            this.serverCommandAliasesProvider.getScheduler().processEvents();
        }
    }

    public void registerClientCommandAliases(CommandDispatcher<S> dispatcher, CommandBuildContext registryAccess) {
        if (this.clientCommandAliasesProvider == null) {
            return;
        }
        if (this.clientCommandAliasesProvider.getDatabase() == null) {
            this.clientCommandAliasesProvider.setDatabase(this.openDatabase(this.createClientDatabase(), "client"));
        }
        if (this.clientCommandAliasesProvider.getScheduler() == null) {
            this.clientCommandAliasesProvider.setScheduler(new Scheduler());
        }
        this.clientCommandAliasesProvider.registerCommandAliasesCommands(dispatcher, registryAccess);
        this.clientCommandAliasesProvider.loadCommandAliases();
        this.clientCommandAliasesProvider.registerCommands(dispatcher, registryAccess);
    }

    public void clientStopped() {
        if (this.clientCommandAliasesProvider == null) {
            return;
        }
        if (this.clientCommandAliasesProvider.getDatabase() != null) {
            this.clientCommandAliasesProvider.getDatabase().close();
            this.clientCommandAliasesProvider.setDatabase(null);
        }
        this.clientCommandAliasesProvider.setScheduler(null);
    }

    public void clientTick() {
        if (this.clientCommandAliasesProvider != null && this.clientCommandAliasesProvider.getScheduler() != null) {
            this.clientCommandAliasesProvider.getScheduler().processEvents();
        }
    }

    public ServerCommandAliasesProvider serverCommandAliasesProvider() {
        return this.serverCommandAliasesProvider;
    }

    public ClientCommandAliasesProvider<S> clientCommandAliasesProvider() {
        return this.clientCommandAliasesProvider;
    }

    private AbstractDatabase<String, String> createServerDatabase(net.minecraft.server.MinecraftServer server) {
        CommandAliasesConfig.DatabaseSettings settings = CommandAliasesMod.options().databaseSettings;
        return switch (settings.databaseMode) {
            case LEVELDB -> new LevelDBImpl(server.getWorldPath(LevelResource.ROOT).resolve("commandaliases").toString());
            case MYSQL -> new MySQLImpl(settings.host, settings.port, settings.database, settings.user, settings.password, "server");
            case REDIS -> new RedisImpl(settings.host, settings.port, 0, settings.user, settings.password);
            case IN_MEMORY -> new InMemoryImpl();
        };
    }

    private AbstractDatabase<String, String> createClientDatabase() {
        CommandAliasesConfig.DatabaseSettings settings = CommandAliasesMod.options().databaseSettings;
        return switch (settings.databaseMode) {
            case LEVELDB -> new LevelDBImpl(CommandAliasesMod.platform().gameDirectory().resolve("commandaliases.client").toString());
            case MYSQL -> new MySQLImpl(settings.host, settings.port, settings.database, settings.user, settings.password, "client");
            case REDIS -> new RedisImpl(settings.host, settings.port, 1, settings.user, settings.password);
            case IN_MEMORY -> new InMemoryImpl();
        };
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
