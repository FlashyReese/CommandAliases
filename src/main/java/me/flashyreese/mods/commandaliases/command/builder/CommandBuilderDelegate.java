package me.flashyreese.mods.commandaliases.command.builder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

/**
 * Represents a command builder
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public interface CommandBuilderDelegate<S extends CommandSource> {
    LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher);

    default LiteralArgumentBuilder<S> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    default <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
