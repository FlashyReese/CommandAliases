package me.flashyreese.mods.commandaliases.storage.database.rocksdb;

import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;

/**
 * Represents the RocksDB Implementation
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public class RocksDBImpl implements AbstractDatabase<byte[], byte[]> {

    static {
        RocksDB.loadLibrary();
    }

    private RocksDB database;
    private final String path;

    public RocksDBImpl(String path) {
        this.path = path;
    }

    @Override
    public void open() {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try {
                this.database = RocksDB.open(options, this.path);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        if (this.database != null) {
            this.database.close();
        }
    }

    @Override
    public void write(byte[] key, byte[] value) {
        try {
            this.database.put(key, value);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] read(byte[] key) {
        try {
            return this.database.get(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(byte[] key) {
        try {
            this.database.delete(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
