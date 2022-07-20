package me.flashyreese.mods.commandaliases.util;

public class Atomic<T> {
    private volatile T value;

    public Atomic() {
        this(null);
    }

    public Atomic(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
