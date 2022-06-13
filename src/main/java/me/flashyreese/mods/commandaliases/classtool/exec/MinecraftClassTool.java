/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool.exec;

import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasHolder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the Minecraft Class Tool
 * <p>
 * Maps custom values to hashmap
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.1.3
 */
public class MinecraftClassTool implements ClassTool<Function<CommandContext<ServerCommandSource>, String>> {

    private final Map<String, Function<CommandContext<ServerCommandSource>, String>> minecraftMap = new Object2ObjectOpenHashMap<>();

    public MinecraftClassTool() {
        this.registerClassTools();
    }

    private void registerClassTools() {
        this.minecraftMap.put("SELF", (context) -> context.getSource().getPlayer().getEntityName());
        this.minecraftMap.put("TIME", (context) -> String.valueOf(context.getSource().getWorld().getTime()));
        this.minecraftMap.put("RANDOM_ALIVE_PLAYER", (context) -> Objects.requireNonNull(context.getSource().getWorld().getRandomAlivePlayer()).getEntityName());
    }

    public Map<String, Function<CommandContext<ServerCommandSource>, String>> getMinecraftMap() {
        return minecraftMap;
    }

    @Override
    public String getName() {
        return "this";
    }

    @Override
    public boolean contains(String key) {
        return this.minecraftMap.containsKey(key);
    }

    @Override
    public Function<CommandContext<ServerCommandSource>, String> getValue(String key) {
        return this.minecraftMap.get(key);
    }

    @Override
    public String getValue(CommandContext<ServerCommandSource> context, AliasHolder holder) {
        return this.getValue(holder.getMethod()).apply(context);
    }
}
