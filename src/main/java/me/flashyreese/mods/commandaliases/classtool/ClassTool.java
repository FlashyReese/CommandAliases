package me.flashyreese.mods.commandaliases.classtool;

public interface ClassTool<T> {
    String getName();

    boolean contains(String key);

    T getValue(String key);
}
