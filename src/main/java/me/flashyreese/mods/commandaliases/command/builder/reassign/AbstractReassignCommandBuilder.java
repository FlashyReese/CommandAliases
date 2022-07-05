package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the CommandAliases Reassign Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.3.0
 */
public abstract class AbstractReassignCommandBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
    protected final CommandAlias command;
    protected final Map<String, String> reassignCommandMap;
    protected final CommandRegistryAccess registryAccess;
    protected final AbstractDatabase<byte[], byte[]> database;
    private final Field literalCommandNodeLiteralField;
    private final CommandType commandType;

    public AbstractReassignCommandBuilder(CommandAlias command, Field literalCommandNodeLiteralField, Map<String, String> reassignCommandMap, CommandType commandType, CommandRegistryAccess registryAccess, AbstractDatabase<byte[], byte[]> database) {
        this.command = command;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
        this.reassignCommandMap = reassignCommandMap;
        this.commandType = commandType;
        this.registryAccess = registryAccess;
        this.database = database;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        if (this.command.getReassignCommand() == null) {
            CommandAliasesMod.logger().error("[{}] {} - Skipping reassignment, missing declaration!", this.commandType, this.command.getCommandMode());
            return null;
        }

        if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS) {
            CommandAliasesMod.logger().warn("The command mode \"COMMAND_REASSIGN_AND_ALIAS\" is now deprecated and scheduled to remove on version 1.0.0");
            CommandAliasesMod.logger().warn("Please migrate to command mode \"COMMAND_REASSIGN_AND_CUSTOM\", it is more feature packed and receives more support. :)");
            if (this.command.getAliasCommand() == null) {
                CommandAliasesMod.logger().error("[{}] {} - Skipping reassignment, missing alias command declaration!", this.commandType, this.command.getCommandMode());
                return null;
            }
            if (!this.command.getAliasCommand().getCommand().startsWith(this.command.getReassignCommand().getCommand())) {
                CommandAliasesMod.logger().error("[{}] {} - Skipping reassignment, alias command name and reassign command mismatch!", this.commandType, this.command.getCommandMode());
                return null;
            }
        } else if (this.command.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
            if (this.command.getCustomCommand() == null) {
                CommandAliasesMod.logger().error("[{}] {} - Skipping reassignment, missing custom command declaration!", this.commandType, this.command.getCommandMode());
                return null;
            }
            if (!this.command.getCustomCommand().getParent().equals(this.command.getReassignCommand().getCommand())) {
                CommandAliasesMod.logger().error("[{}] {} - SSkipping reassignment, custom command parent and reassign command mismatch!", this.commandType, this.command.getCommandMode());
                return null;
            }
        }

        return this.reassignCommand(dispatcher);
    }

    protected abstract LiteralArgumentBuilder<S> reassignCommand(CommandDispatcher<S> dispatcher);

    /**
     * Try to reassign a command name to another command name.
     *
     * @param cmd        Command Alias
     * @param dispatcher CommandDispatcher
     * @return If {@code true} then it was successful, else if {@code false} failed.
     */
    protected boolean reassignCommand(CommandAlias cmd, CommandDispatcher<S> dispatcher) {
        if (cmd.getReassignCommand() == null) {
            CommandAliasesMod.logger().error("[{}] {} - Skipping reassignment, missing declaration!", this.commandType, cmd.getCommandMode());
            return false;
        }

        String command = cmd.getReassignCommand().getCommand().trim();
        String reassignTo = cmd.getReassignCommand().getReassignTo().trim();

        if (command.contains(" ")) {
            CommandAliasesMod.logger().error("[{}] {} - \"command\" field must not contain spaces, skipping \"{}\".", this.commandType, cmd.getCommandMode(), command);
            return false;
        }

        if (reassignTo.contains(" ")) {
            CommandAliasesMod.logger().error("[{}] {} - \"reassignTo\" field must not contain spaces, skipping \"{}\".", this.commandType, cmd.getCommandMode(), reassignTo);
            return false;
        }

        CommandNode<S> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(command)).findFirst().orElse(null);

        CommandNode<S> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                node.getName().equals(reassignTo)).findFirst().orElse(null);

        if (commandNode != null && commandReassignNode == null) {
            dispatcher.getRoot().getChildren().remove(commandNode);
            try {
                this.literalCommandNodeLiteralField.set(commandNode, reassignTo);
            } catch (IllegalAccessException e) {
                dispatcher.getRoot().addChild(commandNode);
                e.printStackTrace();
                CommandAliasesMod.logger().error("[{}] {} - Failed to modify command literal \"{}\", skipping.", this.commandType, cmd.getCommandMode(), command);
                return false;
            }

            dispatcher.getRoot().addChild(commandNode);

            CommandAliasesMod.logger().info("[{}] {} - Command \"{}\" has been reassigned to \"{}\"", this.commandType, cmd.getCommandMode(), command, reassignTo);
            return true;
        }
        return false;
    }
}
