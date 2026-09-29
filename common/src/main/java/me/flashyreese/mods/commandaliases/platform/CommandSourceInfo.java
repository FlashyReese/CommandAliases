package me.flashyreese.mods.commandaliases.platform;

import java.util.Map;

/**
 * Platform-neutral information used by alias functions.
 *
 * @param executorName the name of the command executor
 * @param gameTime     the current game time
 * @param timeOfDay    the current dimension clock time
 * @param players      players visible to the command source, keyed by scoreboard name
 */
public record CommandSourceInfo(String executorName, long gameTime, long timeOfDay, Map<String, PlayerInfo> players) {
    public PlayerInfo player(String name) {
        return this.players.get(name);
    }

    public record PlayerInfo(String dimension, String world, double x, double y, double z, float yaw, float pitch) {
    }
}
