package me.flashyreese.mods.commandaliases.storage.database;

/**
 * Represents the Abstract Database
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public interface AbstractDatabase<K, V> {
    void open();

    void close();

    void write(K key, V value);

    V read(K key);

    void delete(K key);
}
