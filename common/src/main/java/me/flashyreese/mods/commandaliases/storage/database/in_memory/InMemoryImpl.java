package me.flashyreese.mods.commandaliases.storage.database.in_memory;

import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryImpl implements AbstractDatabase<String, String> {
    private final Map<String, String> map = new ConcurrentHashMap<>();
    private volatile boolean opened;

    @Override
    public synchronized boolean open() {
        this.opened = true;
        return true;
    }

    @Override
    public synchronized boolean close() {
        this.map.clear();
        this.opened = false;
        return true;
    }

    @Override
    public boolean write(String key, String value) {
        if (!this.opened || key == null || value == null) {
            return false;
        }
        this.map.put(key, value);
        return true;
    }

    @Override
    public String read(String key) {
        if (!this.opened || key == null) {
            return null;
        }
        return this.map.get(key);
    }

    @Override
    public boolean delete(String key) {
        if (!this.opened || key == null) {
            return false;
        }
        return this.map.remove(key) != null;
    }

    @Override
    public Map<String, String> map() {
        if (!this.opened) {
            return Collections.emptyMap();
        }
        return Map.copyOf(this.map);
    }
}
