package me.flashyreese.mods.commandaliases.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Command Aliases Configuration
 * <p>
 * Always end-users to toggle settings
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public class CommandAliasesConfig {
    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public final DatabaseSettings databaseSettings = new DatabaseSettings();
    public final DebugSettings debugSettings = new DebugSettings();
    private File file;

    public static CommandAliasesConfig load(File file) {
        CommandAliasesConfig config;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = gson.fromJson(reader, CommandAliasesConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            }
        } else {
            config = new CommandAliasesConfig();
        }

        config.file = file;
        config.writeChanges();

        return config;
    }

    public void writeChanges() {
        File dir = this.file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
        }
    }

    public enum DatabaseMode {
        IN_MEMORY,
        LEVELDB,
        MYSQL,
        REDIS
    }

    public static class DatabaseSettings {
        public DatabaseMode databaseMode;
        public String host;
        public int port;
        public String database;
        public String user;
        public String password;

        public DatabaseSettings() {
            this.databaseMode = DatabaseMode.LEVELDB;
            this.host = "localhost";
            this.port = 3306;
            this.database = "command_aliases";
            this.user = "root";
            this.password = "";
        }
    }

    public static class DebugSettings {
        public boolean debugMode;
        public boolean showProcessingTime;
        public boolean broadcastToOps;

        public DebugSettings() {
            this.debugMode = false;
            this.showProcessingTime = false;
            this.broadcastToOps = false;
        }
    }
}
