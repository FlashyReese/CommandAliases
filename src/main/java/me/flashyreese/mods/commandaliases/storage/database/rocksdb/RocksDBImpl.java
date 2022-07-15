package me.flashyreese.mods.commandaliases.storage.database.rocksdb;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.Map;

/**
 * Represents the RocksDB Implementation
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public class RocksDBImpl implements AbstractDatabase<byte[], byte[]> {

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
    public boolean write(byte[] key, byte[] value) {
        try {
            this.database.put(key, value);
            return true;
        } catch (RocksDBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] read(byte[] key) {
        try {
            return this.database.get(key);
        } catch (RocksDBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean delete(byte[] key) {
        try {
            this.database.delete(key);
            return true;
        } catch (RocksDBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<byte[], byte[]> list() {
        Map<byte[], byte[]> map = new Object2ObjectOpenHashMap<>();
        RocksIterator iterator = this.database.newIterator();
        for (iterator.seekToLast(); iterator.isValid(); iterator.next()) {
            map.put(iterator.key(), iterator.value());
        }
        return map;
    }
}
