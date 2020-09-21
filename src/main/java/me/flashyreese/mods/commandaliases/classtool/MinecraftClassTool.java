package me.flashyreese.mods.commandaliases.classtool;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MinecraftClassTool implements ClassTool<Function<CommandContext<ServerCommandSource>, String>> {

    private Map<String, Function<CommandContext<ServerCommandSource>, String>> minecraftMap = new HashMap<>();

    public MinecraftClassTool() {
        this.registerClassTools();
    }

    private void registerClassTools() {
        this.minecraftMap.put("SELF", (context) -> {
            try {
                return context.getSource().getPlayer().getEntityName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public Map<String, Function<CommandContext<ServerCommandSource>, String>> getMinecraftMap() {
        return minecraftMap;
    }

    @Override
    public String getName() {
        return "this";
    }

    @Override
    public boolean contains(String key) {
        return this.minecraftMap.containsKey(key);
    }

    @Override
    public Function<CommandContext<ServerCommandSource>, String> getValue(String key) {
        return this.minecraftMap.get(key);
    }
}
