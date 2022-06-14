package me.flashyreese.mods.commandaliases.storage.database;

public interface AbstractDatabase<K,V> {
    void create();
    void write(K key, V value);
    V read(K key);
    void delete(K key);
}
