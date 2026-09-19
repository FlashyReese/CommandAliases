package me.flashyreese.mods.commandaliases.neoforge;

import me.flashyreese.mods.commandaliases.platform.CommandAliasesPlatform;
import me.flashyreese.mods.commandaliases.platform.CommandSourceInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.client.ClientCommandSourceStack;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class CommandAliasesNeoForgeClientPlatform implements CommandAliasesPlatform {
    @Override
    public Path configDirectory() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path gameDirectory() {
        return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
    }

    @Override
    public String modVersion() {
        return new CommandAliasesNeoForgePlatform().modVersion();
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, boolean defaultValue) {
        return source instanceof ClientCommandSourceStack clientSource
                && Commands.hasPermission(defaultValue ? PermissionCheck.AlwaysPass.INSTANCE : new PermissionCheck.Require(Permissions.COMMANDS_OWNER)).test(clientSource);
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, int defaultRequiredLevel) {
        return source instanceof ClientCommandSourceStack clientSource
                && Commands.hasPermission(this.permissionCheck(defaultRequiredLevel)).test(clientSource);
    }

    @Override
    public CommandSourceInfo sourceInfo(SharedSuggestionProvider source) {
        if (!(source instanceof ClientCommandSourceStack clientSource) || !(clientSource.getUnsidedLevel() instanceof ClientLevel level)) {
            return null;
        }

        LocalPlayer executor = clientSource.getEntity() instanceof LocalPlayer player ? player : Minecraft.getInstance().player;
        if (executor == null) {
            return null;
        }

        Map<String, CommandSourceInfo.PlayerInfo> players = new HashMap<>();
        for (AbstractClientPlayer player : level.players()) {
            players.put(player.getScoreboardName(), new CommandSourceInfo.PlayerInfo(
                    player.level().dimension().identifier().toString(),
                    player.level().dimensionType().skybox().toString(),
                    player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()
            ));
        }
        return new CommandSourceInfo(
                executor.getScoreboardName(),
                level.getGameTime(),
                level.getDefaultClockTime(),
                Map.copyOf(players)
        );
    }

    @Override
    public void sendClientFeedback(SharedSuggestionProvider source, Component text) {
        if (source instanceof ClientCommandSourceStack clientSource) {
            clientSource.sendSystemMessage(text);
        }
    }

    @Override
    public int sendCommandToServer(SharedSuggestionProvider source, String command) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendCommand(command);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    @Override
    public boolean isServerCommandSource(SharedSuggestionProvider source) {
        return false;
    }

    private PermissionCheck permissionCheck(int level) {
        return switch (Math.max(0, Math.min(level, 4))) {
            case 0 -> PermissionCheck.AlwaysPass.INSTANCE;
            case 1 -> new PermissionCheck.Require(Permissions.COMMANDS_MODERATOR);
            case 2 -> new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);
            case 3 -> new PermissionCheck.Require(Permissions.COMMANDS_ADMIN);
            default -> new PermissionCheck.Require(Permissions.COMMANDS_OWNER);
        };
    }
}
