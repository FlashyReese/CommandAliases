package me.flashyreese.mods.commandaliases.storage.database.leveldb;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
    private volatile DB database;

    public LevelDBImpl(String path) {
        this.path = path;
    }

    @Override
    public synchronized boolean open() {
        if (this.database != null) {
            return true;
        }

        Options options = new Options()
                .createIfMissing(true)
                .errorIfExists(false);
        try {
            this.database = Iq80DBFactory.factory.open(new File(this.path), options);
            return true;
        } catch (IOException | RuntimeException e) {
            this.database = null;
            CommandAliasesMod.logger().error("Could not open LevelDB at {}", this.path, e);
            return false;
        }
    }

    @Override
    public synchronized boolean close() {
        DB currentDatabase = this.database;
        this.database = null;
        if (currentDatabase == null) {
            return true;
        }

        try {
            currentDatabase.close();
            return true;
        } catch (IOException | RuntimeException e) {
            CommandAliasesMod.logger().error("Could not close LevelDB at {}", this.path, e);
            return false;
        }
    }

    @Override
    public boolean write(String key, String value) {
        if (key == null || value == null) {
            return false;
        }

        DB currentDatabase = this.database;
        if (currentDatabase == null) {
            return false;
        }

        try {
            currentDatabase.put(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not write key to LevelDB at {}", this.path, e);
            return false;
        }
    }

    @Override
    public String read(String key) {
        if (key == null) {
            return null;
        }

        DB currentDatabase = this.database;
        if (currentDatabase == null) {
            return null;
        }

        try {
            byte[] array = currentDatabase.get(key.getBytes(StandardCharsets.UTF_8));
            if (array != null) {
                return new String(array, StandardCharsets.UTF_8);
            }
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not read key from LevelDB at {}", this.path, e);
        }
        return null;
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }

        DB currentDatabase = this.database;
        if (currentDatabase == null) {
            return false;
        }

        try {
            currentDatabase.delete(key.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not delete key from LevelDB at {}", this.path, e);
            return false;
        }
    }

    @Override
    public Map<String, String> map() {
        DB currentDatabase = this.database;
        if (currentDatabase == null) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        try {
            currentDatabase.forEach(entry -> map.put(new String(entry.getKey(), StandardCharsets.UTF_8), new String(entry.getValue(), StandardCharsets.UTF_8)));
            return Map.copyOf(map);
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not read LevelDB contents at {}", this.path, e);
            return Collections.emptyMap();
        }
    }
}
