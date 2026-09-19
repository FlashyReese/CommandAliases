package me.flashyreese.mods.commandaliases.fabric;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.platform.CommandAliasesPlatform;
import me.flashyreese.mods.commandaliases.platform.CommandSourceInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.player.AbstractClientPlayer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static me.lucko.fabric.api.permissions.v0.Permissions.check;

public final class CommandAliasesFabricPlatform implements CommandAliasesPlatform {
    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path gameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance().getModContainer(CommandAliasesMod.MOD_ID)
                .map(container -> String.valueOf(container.getMetadata().getVersion()))
                .orElse("unknown");
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, boolean defaultValue) {
        return check(source, permission, defaultValue);
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, int defaultRequiredLevel) {
        return check(source, permission, defaultRequiredLevel);
    }

    @Override
    public CommandSourceInfo sourceInfo(SharedSuggestionProvider source) {
        Map<String, CommandSourceInfo.PlayerInfo> players = new HashMap<>();
        String executorName;
        long gameTime;
        long timeOfDay;

        if (source instanceof CommandSourceStack serverSource) {
            executorName = serverSource.getTextName();
            gameTime = serverSource.getLevel().getGameTime();
            timeOfDay = serverSource.getLevel().getDefaultClockTime();
            for (ServerPlayer player : serverSource.getLevel().players()) {
                players.put(player.getScoreboardName(), new CommandSourceInfo.PlayerInfo(
                        player.level().dimension().identifier().toString(),
                        player.level().dimensionType().skybox().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()
                ));
            }
        } else if (source instanceof FabricClientCommandSource clientSource) {
            executorName = clientSource.getPlayer().getScoreboardName();
            gameTime = clientSource.getLevel().getGameTime();
            timeOfDay = clientSource.getLevel().getDefaultClockTime();
            for (AbstractClientPlayer player : clientSource.getLevel().players()) {
                players.put(player.getScoreboardName(), new CommandSourceInfo.PlayerInfo(
                        player.level().dimension().identifier().toString(),
                        player.level().dimensionType().skybox().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()
                ));
            }
        } else {
            return null;
        }

        return new CommandSourceInfo(executorName, gameTime, timeOfDay, Map.copyOf(players));
    }

    @Override
    public void sendClientFeedback(SharedSuggestionProvider source, Component text) {
        if (source instanceof FabricClientCommandSource clientSource) {
            clientSource.sendFeedback(text);
        }
    }

    @Override
    public int sendCommandToServer(SharedSuggestionProvider source, String command) {
        if (source instanceof FabricClientCommandSource clientSource) {
            clientSource.getPlayer().connection.sendCommand(command);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    @Override
    public String processServerPlaceholders(CommandSourceStack source, String input) {
        return Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(input, ServerPlaceholderContext.of(source).asParserContext()).getString();
    }

    @Override
    public boolean isServerCommandSource(SharedSuggestionProvider source) {
        return source instanceof CommandSourceStack;
    }
}
