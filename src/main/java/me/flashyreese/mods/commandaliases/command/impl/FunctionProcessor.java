package me.flashyreese.mods.commandaliases.command.impl;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Function Processor
 * <p>
 * Applies function to argument passed toward the function
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.7.0
 */
public class FunctionProcessor<S extends CommandSource> {
    private final Pattern singleArgumentFunction = Pattern.compile("\\$(?<fn>\\w+?)\\((?<arg>[+-]?(\\d+([.]\\d*)?|[.]\\d+)?|[\\w._]+?)\\)");

    private final Map<String, BiFunction<CommandSource, String, String>> functionMap = new HashMap<>();

    private final AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider;

    public FunctionProcessor(AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider) {
        this.abstractCommandAliasesProvider = abstractCommandAliasesProvider;
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
        this.functionMap.put("random", (commandSource, input) -> {
            if (input != null && !input.isEmpty()) {
                try {
                    long seed = Long.parseLong(input);
                    return String.valueOf(new Random(seed).nextInt());
                } catch (NumberFormatException e) {
                    if (CommandAliasesMod.options().debugSettings.debugMode) {
                        CommandAliasesMod.logger().error("Parsing exception: {}", e.getMessage());
                    }
                }
            }
            return String.valueOf(new Random().nextInt());
        });
        this.functionMap.put("is_online", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                return String.valueOf(serverCommandSource.getWorld().getPlayers().stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)));
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getWorld().getPlayers().stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)));
            }
            return "false";
        });
        this.functionMap.put("get_time", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                return String.valueOf(serverCommandSource.getWorld().getTime());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getWorld().getTime());
            }
            return null;
        });
        this.functionMap.put("get_time_of_day", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                return String.valueOf(serverCommandSource.getWorld().getTimeOfDay());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getWorld().getTimeOfDay());
            }
            return null;
        });
        this.functionMap.put("get_lunar_time", (commandSource, input) -> {
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                return String.valueOf(serverCommandSource.getWorld().getLunarTime());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getWorld().getLunarTime());
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

        // Database related
        this.functionMap.put("get_database_contains", (commandSource, input) -> {
            for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
                if (entry.getKey().contains(input)) {
                    return "true";
                }
            }
            return "false";
        });
        this.functionMap.put("get_database_first_starts_with", (commandSource, input) -> {
            for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
                if (entry.getKey().startsWith(input)) {
                    return entry.getValue();
                }
            }
            if (CommandAliasesMod.options().debugSettings.debugMode) {
                CommandAliasesMod.logger().error("Empty result: {}", input);
            }
            return null;
        });
        this.functionMap.put("get_database_first_ends_with", (commandSource, input) -> {
            for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
                if (entry.getKey().endsWith(input)) {
                    return entry.getValue();
                }
            }
            if (CommandAliasesMod.options().debugSettings.debugMode) {
                CommandAliasesMod.logger().error("Empty result: {}", input);
            }
            return null;
        });
        this.functionMap.put("get_database_first_contains", (commandSource, input) -> {
            for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
                if (entry.getKey().contains(input)) {
                    return entry.getValue();
                }
            }
            if (CommandAliasesMod.options().debugSettings.debugMode) {
                CommandAliasesMod.logger().error("Empty result: {}", input);
            }
            return null;
        });
        this.functionMap.put("get_database_value", (commandSource, input) -> {
            String value = this.abstractCommandAliasesProvider.getDatabase().read(input);
            if (value != null) {
                return value;
            } else {
                if (CommandAliasesMod.options().debugSettings.debugMode) {
                    CommandAliasesMod.logger().error("Invalid database key: {}", input);
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
                String value = this.functionMap.get(fn).apply(commandSource, arg);
                modified = modified.replace(matcher.group(), Objects.requireNonNullElse(value, "null"));
            } else {
                CommandAliasesMod.logger().error("Invalid function of `{}` in `{}`", fn, original);
                break;
            }
            matcher = this.singleArgumentFunction.matcher(modified);
        }
        return modified;
    }
}
