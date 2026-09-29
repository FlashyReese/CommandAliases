package me.flashyreese.mods.commandaliases.storage.database.mysql;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class MySQLImpl implements AbstractDatabase<String, String> {
    private static final String KEY_HASH_COLUMN = "key_hash";
    private static final String KEY_HASH_INDEX = "uk_command_aliases_key_hash";
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_$-]+");

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final String table;
    private volatile Connection connection;

    public MySQLImpl(String host, int port, String database, String user, String password, String table) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.table = table;
    }

    @Override
    public synchronized boolean open() {
        if (this.isConnectionOpen()) {
            return true;
        }

        if (this.database == null || this.table == null
                || !SAFE_IDENTIFIER.matcher(this.database).matches()
                || !SAFE_IDENTIFIER.matcher(this.table).matches()) {
            CommandAliasesMod.logger().error("Refusing unsafe MySQL database or table identifier: {}.{}", this.database, this.table);
            return false;
        }

        this.closeConnection();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
            this.ensureSchema();
            return true;
        } catch (SQLException | ClassNotFoundException | RuntimeException e) {
            this.closeConnection();
            CommandAliasesMod.logger().error("Could not open MySQL database {}.{}", this.database, this.table, e);
            return false;
        }
    }

    @Override
    public synchronized boolean close() {
        return this.closeConnection();
    }

    @Override
    public synchronized boolean write(String key, String value) {
        if (key == null || value == null) {
            return false;
        }

        byte[] keyHash = this.hashKey(key);
        for (int attempt = 0; attempt < 2; attempt++) {
            if (!this.ensureConnection()) {
                return false;
            }

            try {
                long id = this.findId(key, keyHash);
                if (id == -1) {
                    try (PreparedStatement statement = this.connection.prepareStatement(
                            "INSERT INTO " + this.qualifiedTable() + " (`key`, `" + KEY_HASH_COLUMN + "`, `value`) VALUES (?, ?, ?);")) {
                        statement.setString(1, key);
                        statement.setBytes(2, keyHash);
                        statement.setString(3, value);
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = this.connection.prepareStatement(
                            "UPDATE " + this.qualifiedTable() + " SET `value` = ? WHERE `id` = ?;")) {
                        statement.setString(1, value);
                        statement.setLong(2, id);
                        statement.executeUpdate();
                    }
                }
                return true;
            } catch (SQLException e) {
                if (attempt == 0 && this.reconnect()) {
                    continue;
                }
                this.logDatabaseError("write", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public synchronized String read(String key) {
        if (key == null) {
            return null;
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            if (!this.ensureConnection()) {
                return null;
            }

            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT `value` FROM " + this.qualifiedTable() + " WHERE `" + KEY_HASH_COLUMN + "` = ? AND `key` = ? LIMIT 1;")) {
                statement.setBytes(1, this.hashKey(key));
                statement.setString(2, key);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            } catch (SQLException e) {
                if (attempt == 0 && this.reconnect()) {
                    continue;
                }
                this.logDatabaseError("read", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean delete(String key) {
        if (key == null || !this.ensureConnection()) {
            return false;
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM " + this.qualifiedTable() + " WHERE `" + KEY_HASH_COLUMN + "` = ? AND `key` = ?;")) {
                statement.setBytes(1, this.hashKey(key));
                statement.setString(2, key);
                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                if (attempt == 0 && this.reconnect()) {
                    continue;
                }
                this.logDatabaseError("delete", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public synchronized Map<String, String> map() {
        for (int attempt = 0; attempt < 2; attempt++) {
            if (!this.ensureConnection()) {
                return Collections.emptyMap();
            }

            Map<String, String> map = new Object2ObjectOpenHashMap<>();
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT `key`, `value` FROM " + this.qualifiedTable() + ";");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    map.put(resultSet.getString(1), resultSet.getString(2));
                }
                return Map.copyOf(map);
            } catch (SQLException e) {
                if (attempt == 0 && this.reconnect()) {
                    continue;
                }
                this.logDatabaseError("read all", e);
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    private synchronized boolean ensureConnection() {
        if (this.isConnectionOpen()) {
            return true;
        }
        return this.open();
    }

    private synchronized boolean reconnect() {
        this.closeConnection();
        return this.open();
    }

    private boolean isConnectionOpen() {
        Connection currentConnection = this.connection;
        if (currentConnection == null) {
            return false;
        }
        try {
            return !currentConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean closeConnection() {
        Connection currentConnection = this.connection;
        this.connection = null;
        if (currentConnection == null) {
            return true;
        }

        try {
            currentConnection.close();
            return true;
        } catch (SQLException e) {
            this.logDatabaseError("close", e);
            return false;
        }
    }

    private void ensureSchema() throws SQLException {
        String tableName = this.qualifiedTable();
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                    + "`key` MEDIUMTEXT NOT NULL, "
                    + "`" + KEY_HASH_COLUMN + "` BINARY(32) NOT NULL, "
                    + "`value` MEDIUMTEXT NOT NULL, "
                    + "PRIMARY KEY (`id`), "
                    + "UNIQUE KEY `" + KEY_HASH_INDEX + "` (`" + KEY_HASH_COLUMN + "`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        }

        boolean addedKeyHashColumn = false;
        if (!this.columnExists(KEY_HASH_COLUMN)) {
            try (Statement statement = this.connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN `" + KEY_HASH_COLUMN + "` BINARY(32) NULL;");
            }
            addedKeyHashColumn = true;
        }

        if (addedKeyHashColumn || this.hasNullKeyHashes()) {
            try (PreparedStatement select = this.connection.prepareStatement("SELECT `id`, `key` FROM " + tableName + ";");
                 ResultSet resultSet = select.executeQuery();
                 PreparedStatement update = this.connection.prepareStatement("UPDATE " + tableName + " SET `" + KEY_HASH_COLUMN + "` = ? WHERE `id` = ?;")) {
                while (resultSet.next()) {
                    update.setBytes(1, this.hashKey(resultSet.getString(2)));
                    update.setLong(2, resultSet.getLong(1));
                    update.addBatch();
                }
                update.executeBatch();
            }

            if (addedKeyHashColumn) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE " + tableName + " MODIFY COLUMN `" + KEY_HASH_COLUMN + "` BINARY(32) NOT NULL;");
                }
            }
        }

        if (!this.indexExists(KEY_HASH_INDEX)) {
            try (Statement statement = this.connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ADD UNIQUE KEY `" + KEY_HASH_INDEX + "` (`" + KEY_HASH_COLUMN + "`);");
            }
        }
    }

    private boolean columnExists(String column) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1;")) {
            statement.setString(1, this.database);
            statement.setString(2, this.table);
            statement.setString(3, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean indexExists(String index) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ? LIMIT 1;")) {
            statement.setString(1, this.database);
            statement.setString(2, this.table);
            statement.setString(3, index);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasNullKeyHashes() throws SQLException {
        try (Statement statement = this.connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 FROM " + this.qualifiedTable()
                     + " WHERE `" + KEY_HASH_COLUMN + "` IS NULL LIMIT 1;")) {
            return resultSet.next();
        }
    }

    private long findId(String key, byte[] keyHash) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT `id` FROM " + this.qualifiedTable() + " WHERE `" + KEY_HASH_COLUMN + "` = ? AND `key` = ? LIMIT 1;")) {
            statement.setBytes(1, keyHash);
            statement.setString(2, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : -1;
            }
        }
    }

    private byte[] hashKey(String key) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String qualifiedTable() {
        return this.quoteIdentifier(this.database) + "." + this.quoteIdentifier(this.table);
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private void logDatabaseError(String operation, SQLException exception) {
        CommandAliasesMod.logger().error("MySQL {} failed for {}.{}", operation, this.database, this.table, exception);
    }
}
