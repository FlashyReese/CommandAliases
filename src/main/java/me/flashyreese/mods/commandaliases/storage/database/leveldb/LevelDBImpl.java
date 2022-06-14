package me.flashyreese.mods.commandaliases.storage.database.leveldb;

import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

public class LevelDBImpl implements AbstractDatabase<byte[], byte[]> {

    private DB database;

    private final String path;

    public LevelDBImpl(String path) {
        this.path = path;
    }

    @Override
    public void create() {
        Options options = new Options();
        try {
            this.database = Iq80DBFactory.factory.open(new File(this.path), options);
        } catch (IOException e) {
            e.printStackTrace();
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
