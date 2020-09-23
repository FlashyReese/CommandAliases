/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool.impl.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.BiFunction;

public class CommandAliasesArgumentType {

    private ArgumentType<?> argumentType;
    private BiFunction<CommandContext<ServerCommandSource>, String, String> biFunction;

    public CommandAliasesArgumentType(ArgumentType<?> argumentType, BiFunction<CommandContext<ServerCommandSource>, String, String> biFunction) {
        this.argumentType = argumentType;
        this.biFunction = biFunction;
    }

    public ArgumentType<?> getArgumentType() {
        return argumentType;
    }

    public BiFunction<CommandContext<ServerCommandSource>, String, String> getBiFunction() {
        return biFunction;
    }
}
