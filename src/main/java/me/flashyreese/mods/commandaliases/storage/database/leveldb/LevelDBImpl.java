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
import java.util.Map;

/**
 * Represents the LevelDB Implementation
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public class LevelDBImpl implements AbstractDatabase<byte[], byte[]> {

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
    public boolean write(byte[] key, byte[] value) {
        try {
            this.database.put(key, value);
            return true;
        } catch (DBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] read(byte[] key) {
        try {
            return this.database.get(key);
        } catch (DBException e) {
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
        } catch (DBException e) {
            CommandAliasesMod.logger().error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<byte[], byte[]> list() {
        Map<byte[], byte[]> map = new Object2ObjectOpenHashMap<>();
        this.database.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        return map;
    }
}
