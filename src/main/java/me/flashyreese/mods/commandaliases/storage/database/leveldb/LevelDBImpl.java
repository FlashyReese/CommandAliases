package me.flashyreese.mods.commandaliases.storage.database.leveldb;

import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

/**
 * Represents the LevelDB Implementation
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public class LevelDBImpl implements AbstractDatabase<byte[], byte[]> {

    private final String path;
    private DB database;

    public LevelDBImpl(String path) {
        this.path = path;
    }

    @Override
    public void open() {
        Options options = new Options();
        try {
            this.database = Iq80DBFactory.factory.open(new File(this.path), options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (this.database != null) {
                this.database.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] key, byte[] value) {
        this.database.put(key, value);
    }

    @Override
    public byte[] read(byte[] key) {
        return this.database.get(key);
    }

    @Override
    public void delete(byte[] key) {
        this.database.delete(key);
    }
}
