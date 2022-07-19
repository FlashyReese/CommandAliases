package me.flashyreese.mods.commandaliases.storage.database.rocksdb;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Represents the RocksDB Implementation
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public class RocksDBImpl implements AbstractDatabase<String, String> {

    static {
        RocksDB.loadLibrary();
    }

    private final String path;
    private RocksDB database;

    public RocksDBImpl(String path) {
        this.path = path;
    }

    @Override
    public boolean open() {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try {
                this.database = RocksDB.open(options, this.path);
                return true;
            } catch (RocksDBException e) {
                CommandAliasesMod.logger().error(e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean close() {
        if (this.database != null) {
            this.database.close();
        }
        return true;
    }

    @Override
    public boolean write(String key, String value) {
        try {
            this.database.put(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (RocksDBException e) {
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
        } catch (RocksDBException e) {
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
        } catch (RocksDBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, String> map() {
        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        RocksIterator iterator = this.database.newIterator();
        for (iterator.seekToLast(); iterator.isValid(); iterator.next()) {
            map.put(new String(iterator.key(), StandardCharsets.UTF_8), new String(iterator.value(), StandardCharsets.UTF_8));
        }
        return map;
    }
}
