package me.flashyreese.mods.commandaliases.command.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import me.flashyreese.mods.commandaliases.platform.CommandSourceInfo;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies functions to values embedded in an alias command or message.
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
        this.functionMap.put("executor_name", (source, input) -> {
            CommandSourceInfo info = this.sourceInfo(source);
            return info == null ? null : info.executorName();
        });
        this.functionMap.put("random", (source, input) -> {
            if (input != null && !input.isEmpty()) {
                try {
                    return String.valueOf(new Random(Long.parseLong(input)).nextInt());
                } catch (NumberFormatException e) {
                    if (CommandAliasesMod.options().debugSettings.debugMode) {
                        CommandAliasesMod.logger().error("Parsing exception: {}", e.getMessage());
                    }
                }
            }
            return String.valueOf(new Random().nextInt());
        });
        this.functionMap.put("is_online", (source, input) -> {
            CommandSourceInfo info = this.sourceInfo(source);
            return String.valueOf(info != null && info.players().containsKey(input));
        });
        this.functionMap.put("get_time", (source, input) -> {
            CommandSourceInfo info = this.sourceInfo(source);
            return info == null ? null : String.valueOf(info.gameTime());
        });
        this.functionMap.put("get_time_of_day", (source, input) -> {
            CommandSourceInfo info = this.sourceInfo(source);
            return info == null ? null : String.valueOf(info.timeOfDay());
        });
        this.functionMap.put("get_lunar_time", (source, input) -> {
            CommandSourceInfo info = this.sourceInfo(source);
            return info == null ? null : String.valueOf(info.timeOfDay());
        });
        this.functionMap.put("get_dimension", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : player.dimension();
        });
        this.functionMap.put("get_world", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : player.world();
        });
        this.functionMap.put("get_block_pos_x", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf((int) Math.floor(player.x()));
        });
        this.functionMap.put("get_block_pos_y", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf((int) Math.floor(player.y()));
        });
        this.functionMap.put("get_block_pos_z", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf((int) Math.floor(player.z()));
        });
        this.functionMap.put("get_yaw", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf(player.yaw());
        });
        this.functionMap.put("get_pitch", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf(player.pitch());
        });
        this.functionMap.put("get_pos_x", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf(player.x());
        });
        this.functionMap.put("get_pos_y", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf(player.y());
        });
        this.functionMap.put("get_pos_z", (source, input) -> {
            CommandSourceInfo.PlayerInfo player = this.playerInfo(source, input);
            return player == null ? null : String.valueOf(player.z());
        });

        this.functionMap.put("get_database_contains", (source, input) -> {
            for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
                if (entry.getKey().contains(input)) {
                    return "true";
                }
            }
            return "false";
        });
        this.functionMap.put("get_database_first_starts_with", (source, input) -> this.findDatabaseValue(input, MatchMode.STARTS_WITH));
        this.functionMap.put("get_database_first_ends_with", (source, input) -> this.findDatabaseValue(input, MatchMode.ENDS_WITH));
        this.functionMap.put("get_database_first_contains", (source, input) -> this.findDatabaseValue(input, MatchMode.CONTAINS));
        this.functionMap.put("get_database_value", (source, input) -> {
            String value = this.abstractCommandAliasesProvider.getDatabase().read(input);
            if (value == null && CommandAliasesMod.options().debugSettings.debugMode) {
                CommandAliasesMod.logger().error("Invalid database key: {}", input);
            }
            return value;
        });
    }

    private CommandSourceInfo sourceInfo(SharedSuggestionProvider source) {
        return CommandAliasesMod.platform().sourceInfo(source);
    }

    private CommandSourceInfo.PlayerInfo playerInfo(SharedSuggestionProvider source, String name) {
        CommandSourceInfo info = this.sourceInfo(source);
        return info == null ? null : info.player(name);
    }

    private String findDatabaseValue(String input, MatchMode mode) {
        for (Map.Entry<String, String> entry : this.abstractCommandAliasesProvider.getDatabase().map().entrySet()) {
            boolean matches = switch (mode) {
                case STARTS_WITH -> entry.getKey().startsWith(input);
                case ENDS_WITH -> entry.getKey().endsWith(input);
                case CONTAINS -> entry.getKey().contains(input);
            };
            if (matches) {
                return entry.getValue();
            }
        }
        if (CommandAliasesMod.options().debugSettings.debugMode) {
            CommandAliasesMod.logger().error("Empty result: {}", input);
        }
        return null;
    }

    public String processFunctions(String original, SharedSuggestionProvider commandSource) {
        String modified = original;
        Matcher matcher = this.singleArgumentFunction.matcher(modified);
        while (matcher.find()) {
            String functionName = matcher.group("functionName");
            String arg = matcher.group("arg");
            BiFunction<SharedSuggestionProvider, String, String> function = this.functionMap.get(functionName);
            if (function == null) {
                CommandAliasesMod.logger().error("Invalid function of `{}` in `{}`. Please check the functionMap", functionName, original);
                throw new IllegalArgumentException("Invalid function name : " + functionName);
            }

            String value = function.apply(commandSource, arg);
            modified = modified.replace(matcher.group(), Objects.requireNonNullElse(value, "null"));
            matcher = this.singleArgumentFunction.matcher(modified);
        }
        return modified;
    }

    public Map<String, BiFunction<SharedSuggestionProvider, String, String>> getFunctionMap() {
        return functionMap;
    }

    private enum MatchMode {
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS
    }
}
