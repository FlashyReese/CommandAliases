package me.flashyreese.mods.commandaliases.command.builder.reassign;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.command.builder.reassign.format.ReassignCommand;
import net.minecraft.command.CommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the CommandAliases Reassign Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.3.0
 */
public class ReassignCommandBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
    protected final String filePath;
    protected final ReassignCommand command;
    protected final Map<String, String> reassignCommandMap;
    private final Field literalCommandNodeLiteralField;
    private final CommandType commandType;

    public ReassignCommandBuilder(String filePath, ReassignCommand command, Field literalCommandNodeLiteralField, Map<String, String> reassignCommandMap, CommandType commandType) {
        this.filePath = filePath;
        this.command = command;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
        this.reassignCommandMap = reassignCommandMap;
        this.commandType = commandType;
    }

    /**
     * Builds a command for command registry
     *
     * @param dispatcher CommandDispatcher
     * @return Command
     */
    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        if (this.reassignCommand(this.command, dispatcher)) {
            String command = this.command.getCommand().trim();
            String reassignTo = this.command.getReassignTo().trim();
            this.reassignCommandMap.put(command, reassignTo);
        }
        return null;
    }

    /**
     * Try to reassign a command name to another command name.
     *
     * @param cmd        Command Alias
     * @param dispatcher CommandDispatcher
     * @return If {@code true} then it was successful, else if {@code false} failed.
     */
    protected boolean reassignCommand(ReassignCommand cmd, CommandDispatcher<S> dispatcher) {
        String command = cmd.getCommand().trim();
        String reassignTo = cmd.getReassignTo().trim();

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
