package me.flashyreese.mods.commandaliases.db;

public interface AbstractDatabase<K,V> {
    void create();
    void write(K key, V value);
    V read(K key);
    void delete(K key);
}
