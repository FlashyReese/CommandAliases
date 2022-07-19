package me.flashyreese.mods.commandaliases.storage.database.mysql;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;

import java.sql.*;
import java.util.Map;

public class MySQLImpl implements AbstractDatabase<String, String> {
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
    public boolean write(String key, String value) {
        String exists = this.read(key);

        if (exists == null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + this.table + " (`key`, `value`) VALUES (?, ?);");
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, value);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                CommandAliasesMod.logger().error("write error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.table + " SET `value` = ? WHERE `key` = ?;");
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, key);
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
    public String read(String key) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `value` FROM " + this.table + " WHERE `key` = ?;");
            preparedStatement.setString(1, key);
            ResultSet query = preparedStatement.executeQuery();
            if (query.next()) {
                return query.getString(1);
            }
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean delete(String key) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + this.table + " WHERE `key` = ?;");
            preparedStatement.setString(1, key);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, String> map() {
        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + this.table);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(2), resultSet.getString(3));
            }
        } catch (SQLException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return map;
    }
}
