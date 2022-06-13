package me.flashyreese.mods.commandaliases.db.rocksdb;

import me.flashyreese.mods.commandaliases.db.AbstractDatabase;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksDBImpl implements AbstractDatabase<byte[], byte[]> {

    static {
        RocksDB.loadLibrary();
    }

    private final String path;

    public RocksDBImpl(String path) {
        this.path = path;
    }

    public void create() {
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try {
                RocksDB.open(options, this.path).close();
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(byte[] key, byte[] value) {
        try (final RocksDB db = RocksDB.open(this.path)) {
            db.put(key, value);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] read(byte[] key) {
        try (final RocksDB db = RocksDB.openReadOnly(this.path)) {
            return db.get(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(byte[] key) {
        try (final RocksDB db = RocksDB.open(this.path)) {
            db.delete(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
