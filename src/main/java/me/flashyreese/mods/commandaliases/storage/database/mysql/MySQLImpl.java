package me.flashyreese.mods.commandaliases.storage.database.mysql;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;

public class MySQLImpl implements AbstractDatabase<byte[], byte[]> {
    private final String host;
    private final String database;
    private final String user;
    private final String password;
    private final String table;
    private Connection connection;

    public MySQLImpl(String host, String database, String user, String password, String table) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.table = table;
    }

    @Override
    public boolean open() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database, this.user, this.password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?;");
            preparedStatement.setString(1, this.table);
            if (!preparedStatement.executeQuery().next()) {
                connection.createStatement().executeUpdate("CREATE TABLE `" + this.database + "`.`" + this.table + "` ( `id` INT NOT NULL AUTO_INCREMENT , `key` MEDIUMTEXT NOT NULL , `value` MEDIUMTEXT NOT NULL , PRIMARY KEY (`id`), UNIQUE (`key`)) ENGINE = InnoDB, CHARSET=utf8mb4;");
            }
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean close() {
        try {
            if (connection != null) {
                connection.close();
                CommandAliasesMod.logger().info("Database connection closed successfully.");
            }
            return true;
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
            CommandAliasesMod.logger().warn("Database connection not closed");
        }
        return false;
    }

    @Override
    public boolean write(byte[] key, byte[] value) {
        String keyString = new String(key, StandardCharsets.UTF_8);
        String valueString = new String(value, StandardCharsets.UTF_8);

        byte[] exists = this.read(key);

        if (exists == null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + this.table + " (`key`, `value`) VALUES (?, ?);");
                preparedStatement.setString(1, keyString);
                preparedStatement.setString(2, valueString);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                CommandAliasesMod.logger().error("write error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.table + " SET `value` = ? WHERE `key` = ?;");
                preparedStatement.setString(1, valueString);
                preparedStatement.setString(1, keyString);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                CommandAliasesMod.logger().error(e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public byte[] read(byte[] key) {
        String keyString = new String(key, StandardCharsets.UTF_8);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `value` FROM " + this.table + " WHERE `key` = ?;");
            preparedStatement.setString(1, keyString);
            ResultSet query = preparedStatement.executeQuery();
            if (query.next()) {
                return query.getString(1).getBytes(StandardCharsets.UTF_8);
            }
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean delete(byte[] key) {
        String keyString = new String(key, StandardCharsets.UTF_8);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + this.table + " WHERE `key` = ?;");
            preparedStatement.setString(1, keyString);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<byte[], byte[]> list() {
        Map<byte[], byte[]> map = new Object2ObjectOpenHashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + this.table);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                map.put(resultSet.getString(2).getBytes(StandardCharsets.UTF_8), resultSet.getString(3).getBytes(StandardCharsets.UTF_8));
            }
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return map;
    }
}
