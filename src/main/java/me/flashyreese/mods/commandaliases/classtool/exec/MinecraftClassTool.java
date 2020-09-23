/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool.exec;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.CommandAliasesBuilder;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents the Minecraft Class Tool
 * <p>
 * Maps custom values to hashmap
 *
 * @author FlashyReese
 * @version 0.1.3
 * @since 0.1.3
 */
public class MinecraftClassTool implements ClassTool<Function<CommandContext<ServerCommandSource>, String>> {

    private Map<String, Function<CommandContext<ServerCommandSource>, String>> minecraftMap = new HashMap<>();

    public MinecraftClassTool() {
        this.registerClassTools();
    }

    private void registerClassTools() {
        this.minecraftMap.put("SELF", (context) -> {
            try {
                return context.getSource().getPlayer().getEntityName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        });
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
    public String getValue(CommandContext<ServerCommandSource> context, CommandAliasesBuilder.CommandAliasesHolder holder) {
        return this.getValue(holder.getMethod()).apply(context);
    }
}
