package me.flashyreese.mods.commandaliases.neoforge;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.platform.CommandAliasesPlatform;
import me.flashyreese.mods.commandaliases.platform.CommandSourceInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandAliasesNeoForgePlatform implements CommandAliasesPlatform {
    private volatile CommandAliasesPlatform clientPlatform;
    private final Map<String, PermissionNode<Boolean>> permissionNodes = new ConcurrentHashMap<>();

    public void installClientPlatform(CommandAliasesPlatform clientPlatform) {
        this.clientPlatform = clientPlatform;
    }

    public void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        this.permissionNodes.values().forEach(node -> event.addNodes(node));
    }

    public Collection<PermissionNode<Boolean>> permissionNodes() {
        return this.permissionNodes.values();
    }

    private CommandAliasesPlatform clientPlatform(SharedSuggestionProvider source) {
        CommandAliasesPlatform platform = this.clientPlatform;
        return platform != null && !this.isServerCommandSource(source) ? platform : null;
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path gameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public String modVersion() {
        return ModList.get().getModContainerById(CommandAliasesMod.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, boolean defaultValue) {
        CommandAliasesPlatform client = this.clientPlatform(source);
        if (client != null) {
            return client.checkPermission(source, permission, defaultValue);
        }
        if (source instanceof CommandSourceStack commandSource) {
            ServerPlayer player = commandSource.getPlayer();
            PermissionNode<Boolean> node = this.permissionNodes.get(permission);
            if (player != null && node != null) {
                try {
                    return PermissionAPI.getPermission(player, node);
                } catch (RuntimeException ignored) {
                    // Fall back to the vanilla permission set if a handler has not registered the node yet.
                }
            }
            return (defaultValue || commandSource.hasPermission(4));
        }
        return defaultValue;
    }

    @Override
    public boolean checkPermission(SharedSuggestionProvider source, String permission, int defaultRequiredLevel) {
        CommandAliasesPlatform client = this.clientPlatform(source);
        if (client != null) {
            return client.checkPermission(source, permission, defaultRequiredLevel);
        }
        if (source instanceof CommandSourceStack commandSource) {
            ServerPlayer player = commandSource.getPlayer();
            PermissionNode<Boolean> node = this.permissionNodes.get(permission);
            if (player != null && node != null) {
                try {
                    return PermissionAPI.getPermission(player, node);
                } catch (RuntimeException ignored) {
                    // Fall back to the vanilla permission set if a handler has not registered the node yet.
                }
            }
            return commandSource.hasPermission(Math.clamp(defaultRequiredLevel, 0, 4));
        }
        return false;
    }

    @Override
    public CommandSourceInfo sourceInfo(SharedSuggestionProvider source) {
        CommandAliasesPlatform client = this.clientPlatform(source);
        if (client != null) {
            return client.sourceInfo(source);
        }
        if (!(source instanceof CommandSourceStack serverSource) || !this.isServerCommandSource(source)) {
            return null;
        }

        Map<String, CommandSourceInfo.PlayerInfo> players = new HashMap<>();
        for (ServerPlayer player : serverSource.getLevel().players()) {
            players.put(player.getScoreboardName(), new CommandSourceInfo.PlayerInfo(
                    player.level().dimension().location().toString(),
                    player.level().dimensionType().effectsLocation().toString(),
                    player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()
            ));
        }
        return new CommandSourceInfo(
                serverSource.getTextName(),
                serverSource.getLevel().getGameTime(),
                serverSource.getLevel().getDayTime(),
                Map.copyOf(players)
        );
    }

    @Override
    public void sendClientFeedback(SharedSuggestionProvider source, Component text) {
        CommandAliasesPlatform client = this.clientPlatform(source);
        if (client != null) {
            client.sendClientFeedback(source, text);
        }
    }

    @Override
    public int sendCommandToServer(SharedSuggestionProvider source, String command) {
        CommandAliasesPlatform client = this.clientPlatform(source);
        return client == null ? 0 : client.sendCommandToServer(source, command);
    }

    @Override
    public boolean isServerCommandSource(SharedSuggestionProvider source) {
        return source instanceof CommandSourceStack
                && !source.getClass().getName().equals("net.neoforged.neoforge.client.ClientCommandSourceStack");
    }

    @Override
    public void registerPermission(String permission, boolean defaultValue) {
        this.permissionNodes.computeIfAbsent(permission, key -> this.createPermissionNode(key, defaultValue ? 0 : 4));
    }

    @Override
    public void registerPermission(String permission, int defaultRequiredLevel) {
        this.permissionNodes.computeIfAbsent(permission, key -> this.createPermissionNode(key, Math.clamp(defaultRequiredLevel, 0, 4)));
    }

    private PermissionNode<Boolean> createPermissionNode(String permission, int defaultRequiredLevel) {
        int separator = permission.indexOf('.');
        String namespace = separator > 0 ? permission.substring(0, separator) : CommandAliasesMod.MOD_ID;
        String path = separator > 0 ? permission.substring(separator + 1) : permission;
        return new PermissionNode<>(namespace, path, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.hasPermissions(defaultRequiredLevel));
    }

}
