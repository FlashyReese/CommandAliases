/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool;

import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasHolder;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Represents the ClassTool Interface
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.1.3
 */
public interface ClassTool<T> {
    String getName();

    boolean contains(String key);

    T getValue(String key);

    String getValue(CommandContext<ServerCommandSource> context, AliasHolder holder);
}
