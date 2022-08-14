package me.flashyreese.mods.commandaliases.storage.database.in_memory;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;

import java.util.Map;

public class InMemoryImpl implements AbstractDatabase<String, String> {
    private Map<String, String> map;

    @Override
    public boolean open() {
        if (this.map == null) {
            this.map = new Object2ObjectOpenHashMap<>();
            return true;
        }
        return false;
    }

    @Override
    public boolean close() {
        if (this.map != null) {
            this.map = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean write(String key, String value) {
        this.map.put(key, value);
        return true;
    }

    @Override
    public String read(String key) {
        return this.map.get(key);
    }

    @Override
    public boolean delete(String key) {
        this.map.remove(key);
        return true;
    }

    @Override
    public Map<String, String> map() {
        return this.map;
    }
}
