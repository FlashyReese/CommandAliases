package me.flashyreese.mods.commandaliases.command;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

import static me.lucko.fabric.api.permissions.v0.Permissions.check;

public class Permissions {
    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to the {@code defaultValue} if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param permission   the permission to check
     * @param defaultValue the default value to use if nothing has been set
     * @return a predicate that will perform the permission check
     */
    public static <S extends CommandSource> @NotNull Predicate<S> require(@NotNull String permission, boolean defaultValue) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultValue);
    }

    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to requiring the {@code defaultRequiredLevel} if the resultant state is
     * {@link TriState#DEFAULT}.
     *
     * @param permission           the permission to check
     * @param defaultRequiredLevel the required permission level to check for as a fallback
     * @return a predicate that will perform the permission check
     */
    public static <S extends CommandSource> @NotNull Predicate<S> require(@NotNull String permission, int defaultRequiredLevel) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultRequiredLevel);
    }

    /**
     * Creates a predicate which returns the result of performing a permission check,
     * falling back to {@code false} if the resultant state is {@link TriState#DEFAULT}.
     *
     * @param permission the permission to check
     * @return a predicate that will perform the permission check
     */
    public static <S extends CommandSource> @NotNull Predicate<S> require(@NotNull String permission) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission);
    }
}
