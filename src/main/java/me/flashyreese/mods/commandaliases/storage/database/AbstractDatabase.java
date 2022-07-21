package me.flashyreese.mods.commandaliases.storage.database;

import java.util.Map;

/**
 * Represents the Abstract Database
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public interface AbstractDatabase<K, V> {
    boolean open();

    boolean close();

    boolean write(K key, V value);

    V read(K key);

    boolean delete(K key);

    Map<K, V> map();
}
