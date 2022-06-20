package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandChild;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.List;

/**
 * Represents the Server Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.5.0
 */
public class ServerCustomCommandBuilder extends AbstractCustomCommandBuilder<ServerCommandSource> {
    public ServerCustomCommandBuilder(CustomCommand commandAliasParent, AbstractDatabase<byte[], byte[]> database) {
        super(commandAliasParent, database);
    }

    @Override
    protected int dispatcherExecute(CustomCommandAction action, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context, String actionCommand) throws CommandSyntaxException {
        int state = 0;
        if (action.getCommandType() == CommandType.CLIENT) {
            state = dispatcher.execute(actionCommand, context.getSource());
        } else if (action.getCommandType() == CommandType.SERVER) {
            state = dispatcher.execute(actionCommand, context.getSource().getServer().getCommandSource());
        }
        return state;
    }

    @Override
    protected void sendFeedback(CommandContext<ServerCommandSource> context, String message) {
        context.getSource().sendFeedback(new LiteralText(message), CommandAliasesMod.options().debugSettings.broadcastToOps);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> buildCommandParent(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = this.literal(this.commandAliasParent.getParent());

        if (this.commandAliasParent.getPermission() > 0 && this.commandAliasParent.getPermission() <= 4) {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getParent(), this.commandAliasParent.getPermission()));
        } else {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getParent(), true));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeCommand(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new ObjectArrayList<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CustomCommandChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new ObjectArrayList<>(), this.commandAliasParent.getParent());
                if (subArgumentBuilder != null) {
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
    }

    /**
     * Builds child commands and determine if optional or not.
     *
     * @param child      CommandChild
     * @param dispatcher The command dispatcher
     * @param inputs     User input list
     * @param permission Permission String
     * @return ArgumentBuilder
     */
    protected ArgumentBuilder<ServerCommandSource, ?> buildCommandChild(CustomCommandChild child, CommandDispatcher<ServerCommandSource> dispatcher, List<String> inputs, String permission) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = this.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeMapper.contains(child.getArgumentType())) {
                argumentBuilder = this.argument(child.getChild(), this.argumentTypeMapper.getValue(child.getArgumentType()));
                inputs.add(child.getChild());
            } else {
                CommandAliasesMod.logger().error("Invalid Argument Type: {}", child.getArgumentType());
            }
        }
        if (argumentBuilder != null) {
            String permissionString = permission + "." + child.getChild();
            // Assign permission
            if (child.getPermission() > 0 && child.getPermission() <= 4) {
                argumentBuilder = argumentBuilder.requires(Permissions.require(permissionString, child.getPermission()));
            } else {
                argumentBuilder = argumentBuilder.requires(Permissions.require(permissionString, true));
            }

            if (child.isOptional()) {
                argumentBuilder = argumentBuilder.executes(context -> {
                    //Execution action here
                    return this.executeCommand(child.getActions(), child.getMessage(), dispatcher, context, inputs);
                });
            }
            if (child.getSuggestionProvider() != null) {
                argumentBuilder = this.buildCommandChildSuggestion(argumentBuilder, child, new ObjectArrayList<>(inputs));
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CustomCommandChild subChild : child.getChildren()) {
                    ArgumentBuilder<ServerCommandSource, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new ObjectArrayList<>(inputs), permissionString);
                    if (subArgumentBuilder != null) {
                        argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                    }
                }
            }
        }
        return argumentBuilder;
    }
}
