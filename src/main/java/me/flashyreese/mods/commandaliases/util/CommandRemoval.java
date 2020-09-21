/*
 * Copyright © 2020 vacla
 *
 * This file is part of Watson.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.util;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the CommandRemoval Util
 * <p>
 * Removes root commands from during runtime.
 *
 * @author vacla
 * @version 0.1.3
 * @since 0.1.1
 */
public class CommandRemoval {
    private static final Field CHILDREN, LITERALS, ARGUMENTS;

    static {
        try {
            CHILDREN = CommandNode.class.getDeclaredField("children");
            LITERALS = CommandNode.class.getDeclaredField("literals");
            ARGUMENTS = CommandNode.class.getDeclaredField("arguments");
            CHILDREN.setAccessible(true);
            LITERALS.setAccessible(true);
            ARGUMENTS.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to get CommandNode fields", e);
        }
    }

    /**
     * Remove the command with the given name from the given {@link MinecraftServer}
     *
     * @param server  The server to remove the command from
     * @param command The name of the command to remove
     */
    public static void removeCommand(MinecraftServer server, String command) {
        removeCommand(server.getCommandManager().getDispatcher().getRoot(), command);
    }

    /**
     * Remove the command with the given name from the given {@link CommandNode}
     *
     * @param node    The command node to remove the command from
     * @param command The name of the command to remove
     */
    @SuppressWarnings("unchecked")
    public static void removeCommand(CommandNode<?> node, String command) {
        Object child = node.getChild(command);
        if (child != null) {
            try {
                if (child instanceof LiteralCommandNode<?>) {
                    ((Map<String, ?>) LITERALS.get(node)).remove(command);
                } else if (child instanceof ArgumentCommandNode<?, ?>) {
                    ((Map<String, ?>) ARGUMENTS.get(node)).remove(command);
                }

                ((Map<String, ?>) CHILDREN.get(node)).remove(command);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Error removing command: " + command, e);
            }
        }
    }
}