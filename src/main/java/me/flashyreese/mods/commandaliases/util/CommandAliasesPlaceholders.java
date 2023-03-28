package me.flashyreese.mods.commandaliases.util;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.flashyreese.mods.commandaliases.command.impl.FunctionProcessor;
import me.flashyreese.mods.commandaliases.command.loader.ServerCommandAliasesProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import static me.flashyreese.mods.commandaliases.CommandAliasesMod.MOD_ID;

public class CommandAliasesPlaceholders {
    public static void register(ServerCommandAliasesProvider serverCommandAliasesProvider) {
        FunctionProcessor<ServerCommandSource> functionProcessor = new FunctionProcessor<>(serverCommandAliasesProvider);

        functionProcessor.getFunctionMap().forEach((key, value) -> Placeholders.register(new Identifier(MOD_ID, key), ((context, argument) -> PlaceholderResult.value(value.apply(context.source(), argument)))));
    }
}
