package me.flashyreese.mods.commandaliases.storage.database.leveldb;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Represents the LevelDB Implementation
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public class LevelDBImpl implements AbstractDatabase<String, String> {

    private final String path;
    private DB database;

    public LevelDBImpl(String path) {
        this.path = path;
    }

    @Override
    public boolean open() {
        Options options = new Options();
        try {
            this.database = Iq80DBFactory.factory.open(new File(this.path), options);
            return true;
        } catch (IOException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean close() {
        try {
            if (this.database != null) {
                this.database.close();
            }
            return true;
        } catch (IOException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean write(String key, String value) {
        try {
            this.database.put(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (DBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String read(String key) {
        try {
            byte[] array = this.database.get(key.getBytes(StandardCharsets.UTF_8));
            if (array != null) {
                return new String(array, StandardCharsets.UTF_8);
            }
        } catch (DBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean delete(String key) {
        try {
            this.database.delete(key.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (DBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, String> map() {
        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        this.database.forEach(entry -> map.put(new String(entry.getKey(), StandardCharsets.UTF_8), new String(entry.getValue(), StandardCharsets.UTF_8)));
        return map;
    }
}
