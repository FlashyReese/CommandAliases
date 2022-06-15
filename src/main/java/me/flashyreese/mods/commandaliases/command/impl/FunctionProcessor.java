package me.flashyreese.mods.commandaliases.command.impl;

import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionProcessor {
    private final Pattern singleArgumentFunction = Pattern.compile("\\$(?<fn>\\w+?)\\((?<arg>[+-]?(\\d+([.]\\d*)?|[.]\\d+)?|\\w+.*?)\\)");

    private final Map<String, BiFunction<ServerCommandSource, String, String>> functionMap = new HashMap<>();

    private final AbstractDatabase<byte[], byte[]> database;

    public FunctionProcessor(AbstractDatabase<byte[], byte[]> database) {
        this.database = database;
        this.registerFunctions();
    }

    public void registerFunctions() {
        this.functionMap.put("executor_name", (commandSource, input) -> commandSource.getName());
        this.functionMap.put("get_database_value", (commandSource, input) -> {
            byte[] value = this.database.read(input.getBytes(StandardCharsets.UTF_8));
            if (value != null)
                return new String(value, StandardCharsets.UTF_8);
            return null;
        });
        this.functionMap.put("get_dimension", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getEntityWorld().getRegistryKey().getValue())).orElse(null);
        });
        this.functionMap.put("get_block_pos_x", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getBlockX())).orElse(null);
        });
        this.functionMap.put("get_block_pos_y", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getBlockY())).orElse(null);
        });
        this.functionMap.put("get_block_pos_z", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getBlockZ())).orElse(null);
        });
        this.functionMap.put("get_yaw", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getYaw())).orElse(null);
        });
        this.functionMap.put("get_pitch", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getPitch())).orElse(null);
        });
        this.functionMap.put("get_pos_x", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getX())).orElse(null);
        });
        this.functionMap.put("get_pos_y", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getY())).orElse(null);
        });
        this.functionMap.put("get_pos_z", (commandSource, input) -> {
            Optional<ServerPlayerEntity> optionalPlayer = commandSource.getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getEntityName().equals(input)).findFirst();
            return optionalPlayer.map(serverPlayerEntity -> String.valueOf(serverPlayerEntity.getZ())).orElse(null);
        });
    }

    public String processFunctions(String original, ServerCommandSource serverCommandSource) {
        String modified = original;
        Matcher matcher = this.singleArgumentFunction.matcher(modified);
        while (matcher.find()) {
            String fn = matcher.group("fn");
            String arg = matcher.group("arg");
            if (this.functionMap.containsKey(fn)) {
                boolean argNull = arg == null;
                String originalMatch = "$" + fn + "(" + (argNull ? "" : arg) + ")"; //fixme: looks terrible
                String value = this.functionMap.get(fn).apply(serverCommandSource, arg);
                if (value != null) {
                    modified = modified.replace(originalMatch, value);
                }
            } else {
                CommandAliasesMod.getLogger().error("Invalid function of `{}` in `{}`", fn, original);
                break;
            }
            matcher = this.singleArgumentFunction.matcher(modified);
        }
        return modified;
    }
}
