package me.flashyreese.mods.commandaliases.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.BiFunction;

public class CommandAliasesArgumentType {

    private ArgumentType<?> argumentType;
    private BiFunction<CommandContext<ServerCommandSource>, String, String> biFunction;

    public CommandAliasesArgumentType(ArgumentType<?> argumentType, BiFunction<CommandContext<ServerCommandSource>, String, String> biFunction){
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
