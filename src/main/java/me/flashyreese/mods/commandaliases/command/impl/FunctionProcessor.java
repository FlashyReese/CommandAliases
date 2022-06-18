package me.flashyreese.mods.commandaliases.command.impl;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Function Processor
 * <p>
 * Applies function to argument passed toward the function
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.7.0
 */
public class FunctionProcessor {
    private final Pattern singleArgumentFunction = Pattern.compile("\\$(?<fn>\\w+?)\\((?<arg>[+-]?(\\d+([.]\\d*)?|[.]\\d+)?|[\\w._]+?)\\)");

    private final Map<String, BiFunction<CommandSource, String, String>> functionMap = new HashMap<>();

    private final AbstractDatabase<byte[], byte[]> database;

    public FunctionProcessor(AbstractDatabase<byte[], byte[]> database) {
        this.database = database;
        this.registerFunctions();
    }

    public void registerFunctions() {
        this.functionMap.put("executor_name", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                return serverCommandSource.getName();
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return clientCommandSource.getPlayer().getEntityName();
            }
            return null;
        });
        this.functionMap.put("get_database_value", (commandSource, input) -> {
            byte[] value = this.database.read(input.getBytes(StandardCharsets.UTF_8));
            if (value != null) {
                return new String(value, StandardCharsets.UTF_8);
            } else {
                if (CommandAliasesMod.options().debugSettings.debugMode) {
                    CommandAliasesMod.logger().error("Invalid database key: {}", input);
                }
            }
            return null;
        });
        this.functionMap.put("get_dimension", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().getEntityWorld().getRegistryKey().getValue().toString();
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().getEntityWorld().getRegistryKey().getValue().toString();
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_x", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockX());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockX());
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_y", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockY());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockY());
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_z", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockZ());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockZ());
                }
            }
            return null;
        });
        this.functionMap.put("get_yaw", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getYaw());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getYaw());
                }
            }
            return null;
        });
        this.functionMap.put("get_pitch", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getPitch());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getPitch());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_x", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getX());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getX());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_y", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getY());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getY());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_z", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                Optional<ServerPlayerEntity> optionalPlayer = serverCommandSource.getWorld().getPlayers().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getZ());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayerEntity> optionalPlayer = clientCommandSource.getWorld().getPlayers().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getEntityName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getZ());
                }
            }
            return null;
        });
    }

    public String processFunctions(String original, CommandSource commandSource) {
        String modified = original;
        Matcher matcher = this.singleArgumentFunction.matcher(modified);
        while (matcher.find()) {
            String fn = matcher.group("fn");
            String arg = matcher.group("arg");
            if (this.functionMap.containsKey(fn)) {
                boolean argNull = arg == null;
                String originalMatch = "$" + fn + "(" + (argNull ? "" : arg) + ")"; //fixme: looks terrible
                String value = this.functionMap.get(fn).apply(commandSource, arg);
                if (value != null) {
                    modified = modified.replace(originalMatch, value);
                }
            } else {
                CommandAliasesMod.logger().error("Invalid function of `{}` in `{}`", fn, original);
                break;
            }
            matcher = this.singleArgumentFunction.matcher(modified);
        }
        return modified;
    }
}
