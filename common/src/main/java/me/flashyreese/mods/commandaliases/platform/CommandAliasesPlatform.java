package me.flashyreese.mods.commandaliases.platform;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

/**
 * Small adapter for APIs that are different between Fabric and NeoForge.
 */
public interface CommandAliasesPlatform {
    Path configDirectory();

    Path gameDirectory();

    String modVersion();

    boolean checkPermission(SharedSuggestionProvider source, String permission, boolean defaultValue);

    boolean checkPermission(SharedSuggestionProvider source, String permission, int defaultRequiredLevel);

    default void registerPermission(String permission, boolean defaultValue) {
    }

    default void registerPermission(String permission, int defaultRequiredLevel) {
    }

    CommandSourceInfo sourceInfo(SharedSuggestionProvider source);

    void sendClientFeedback(SharedSuggestionProvider source, Component text);

    int sendCommandToServer(SharedSuggestionProvider source, String command);

    default String processServerPlaceholders(CommandSourceStack source, String input) {
        return input;
    }

    default boolean isServerCommandSource(SharedSuggestionProvider source) {
        return source instanceof CommandSourceStack;
    }

    default <S extends SharedSuggestionProvider> int executeCommand(CommandDispatcher<S> dispatcher, String command, CommandContext<S> context) throws CommandSyntaxException {
        if (context.getSource() instanceof CommandSourceStack source && this.isServerCommandSource(context.getSource())) {
            source.getServer().getCommands().performPrefixedCommand(source, command);
            return Command.SINGLE_SUCCESS;
        }
        return dispatcher.execute(command, context.getSource());
    }

    default <S extends SharedSuggestionProvider> int executeClientAction(CommandType commandType, CommandDispatcher<S> dispatcher, S source, String command) throws CommandSyntaxException {
        if (commandType == CommandType.CLIENT) {
            return dispatcher.execute(command, source);
        }
        if (commandType == CommandType.SERVER) {
            return this.sendCommandToServer(source, command);
        }
        return 0;
    }
}
