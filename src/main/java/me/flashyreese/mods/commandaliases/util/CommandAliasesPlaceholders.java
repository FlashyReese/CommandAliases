package me.flashyreese.mods.commandaliases.util;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.flashyreese.mods.commandaliases.command.impl.FunctionProcessor;
import me.flashyreese.mods.commandaliases.command.loader.ServerCommandAliasesProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

import static me.flashyreese.mods.commandaliases.CommandAliasesMod.MOD_ID;

public class CommandAliasesPlaceholders {
    public static void register(ServerCommandAliasesProvider serverCommandAliasesProvider) {
        FunctionProcessor functionProcessor = new FunctionProcessor(serverCommandAliasesProvider);

        functionProcessor.getFunctionMap().forEach((key, value) -> {
            String keyOriginal = (String) key;
            BiFunction<CommandSource, String, String> valueOriginal = (BiFunction<CommandSource, String, String>) value;
            Placeholders.register(new Identifier(MOD_ID, keyOriginal), ((context, argument) -> PlaceholderResult.value(valueOriginal.apply(context.source(), argument))));
        });
    }
}
