package me.flashyreese.mods.commandaliases.classtool;

import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasHolder;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Represents the ClassTool Interface
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.1.3
 * @deprecated As of 0.7.0, because format is no longer viable to maintain use {@link me.flashyreese.mods.commandaliases.command.impl.FunctionProcessor} instead.
 */
@Deprecated
public interface ClassTool<T> {
    String getName();

    boolean contains(String key);

    T getValue(String key);

    String getValue(CommandContext<ServerCommandSource> context, AliasHolder holder);
}
