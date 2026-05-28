package me.flashyreese.mods.commandaliases.command.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

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
public class FunctionProcessor<S extends SharedSuggestionProvider> {
    private final Pattern singleArgumentFunction = Pattern.compile("\\$(?<functionName>\\w+?)\\((?<arg>[+-]?(\\d+([.]\\d*)?|[.]\\d+)?|[\\w._]+?)\\)");

    private final Map<String, BiFunction<SharedSuggestionProvider, String, String>> functionMap = new Object2ObjectOpenHashMap<>();

    private final AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider;

    public FunctionProcessor(AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider) {
        this.abstractCommandAliasesProvider = abstractCommandAliasesProvider;
        this.registerFunctions();
    }

    public void registerFunctions() {
        this.functionMap.put("executor_name", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                return serverCommandSource.getTextName();
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return clientCommandSource.getPlayer().getScoreboardName();
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
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                return String.valueOf(serverCommandSource.getLevel().players().stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)));
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getLevel().players().stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)));
            }
            return "false";
        });
        this.functionMap.put("get_time", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                return String.valueOf(serverCommandSource.getLevel().getGameTime());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getLevel().getGameTime());
            }
            return null;
        });
        this.functionMap.put("get_time_of_day", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                return String.valueOf(serverCommandSource.getLevel().getDefaultClockTime());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getLevel().getDefaultClockTime());
            }
            return null;
        });
        this.functionMap.put("get_lunar_time", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                return String.valueOf(serverCommandSource.getLevel().getDefaultClockTime());
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                return String.valueOf(clientCommandSource.getLevel().getDefaultClockTime());
            }
            return null;
        });
        this.functionMap.put("get_dimension", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().level().dimension().identifier().toString();
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().level().dimension().identifier().toString();
                }
            }
            return null;
        });
        this.functionMap.put("get_world", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().level().dimensionType().skybox().toString();
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return optionalPlayer.get().level().dimensionType().skybox().toString();
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_x", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockX());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockX());
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_y", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockY());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockY());
                }
            }
            return null;
        });
        this.functionMap.put("get_block_pos_z", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockZ());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getBlockZ());
                }
            }
            return null;
        });
        this.functionMap.put("get_yaw", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getYRot());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getYRot());
                }
            }
            return null;
        });
        this.functionMap.put("get_pitch", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getXRot());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getXRot());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_x", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getX());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getX());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_y", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getY());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getY());
                }
            }
            return null;
        });
        this.functionMap.put("get_pos_z", (commandSource, input) -> {
            if (commandSource instanceof CommandSourceStack serverCommandSource) {
                Optional<ServerPlayer> optionalPlayer = serverCommandSource.getLevel().players().stream()
                        .filter(serverPlayerEntity -> serverPlayerEntity.getScoreboardName().equals(input)).findFirst();
                if (optionalPlayer.isPresent()) {
                    return String.valueOf(optionalPlayer.get().getZ());
                }
            } else if (commandSource instanceof FabricClientCommandSource clientCommandSource) {
                Optional<AbstractClientPlayer> optionalPlayer = clientCommandSource.getLevel().players().stream()
                        .filter(clientPlayerEntity -> clientPlayerEntity.getScoreboardName().equals(input)).findFirst();
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

    public String processFunctions(String original, SharedSuggestionProvider commandSource) {
        String modified = original;
        Matcher matcher = this.singleArgumentFunction.matcher(modified);
        while (matcher.find()) {
            String functionName = matcher.group("functionName");
            String arg = matcher.group("arg");

            if (!this.functionMap.containsKey(functionName)) {
                CommandAliasesMod.logger().error("Invalid function of `{}` in `{}`. Please check the functionMap", functionName, original);
                throw new IllegalArgumentException("Invalid function name : " + functionName);
            }

            BiFunction<SharedSuggestionProvider, String, String> function = this.functionMap.get(functionName);

            String value = function.apply(commandSource, arg);
            modified = modified.replace(matcher.group(), Objects.requireNonNullElse(value, "null"));

            matcher = this.singleArgumentFunction.matcher(modified);
        }
        return modified;
    }

    public Map<String, BiFunction<SharedSuggestionProvider, String, String>> getFunctionMap() {
        return functionMap;
    }
}
