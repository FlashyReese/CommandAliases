package me.flashyreese.mods.commandaliases.command;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public final class Permissions {
    private Permissions() {
    }

    public static <S extends SharedSuggestionProvider> @NotNull Predicate<S> require(@NotNull String permission, boolean defaultValue) {
        Objects.requireNonNull(permission, "permission");
        CommandAliasesMod.platform().registerPermission(permission, defaultValue);
        return source -> CommandAliasesMod.platform().checkPermission(source, permission, defaultValue);
    }

    public static <S extends SharedSuggestionProvider> @NotNull Predicate<S> require(@NotNull String permission, int defaultRequiredLevel) {
        Objects.requireNonNull(permission, "permission");
        CommandAliasesMod.platform().registerPermission(permission, defaultRequiredLevel);
        return source -> CommandAliasesMod.platform().checkPermission(source, permission, defaultRequiredLevel);
    }

    public static <S extends SharedSuggestionProvider> @NotNull Predicate<S> require(@NotNull String permission) {
        Objects.requireNonNull(permission, "permission");
        CommandAliasesMod.platform().registerPermission(permission, false);
        return source -> CommandAliasesMod.platform().checkPermission(source, permission, false);
    }
}
