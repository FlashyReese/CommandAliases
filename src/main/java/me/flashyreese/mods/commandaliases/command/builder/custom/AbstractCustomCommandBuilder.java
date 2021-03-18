/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.classtool.FormattingTypeMap;
import me.flashyreese.mods.commandaliases.classtool.impl.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandAction;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommandChild;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import net.minecraft.command.CommandSource;

import java.util.List;

/**
 * Represents an Abstract Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.4.0
 */
public abstract class AbstractCustomCommandBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
    private final CustomCommand commandAliasParent;

    protected final ArgumentTypeManager argumentTypeManager = new ArgumentTypeManager();
    protected final FormattingTypeMap formattingTypeMap = new FormattingTypeMap();

    public AbstractCustomCommandBuilder(CustomCommand commandAliasParent) {
        this.commandAliasParent = commandAliasParent;
    }

    /**
     * Builds Command for Dispatcher to register.
     *
     * @param dispatcher The command dispatcher
     * @return ArgumentBuilder
     */
    public LiteralArgumentBuilder<S> buildCommand(CommandDispatcher<S> dispatcher) {
        return this.buildCommandParent(dispatcher);
    }

    /**
     * Builds parent ArgumentBuilder
     *
     * @param dispatcher The command dispatcher
     * @return ArgumentBuilder
     */
    private LiteralArgumentBuilder<S> buildCommandParent(CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> argumentBuilder = this.literal(this.commandAliasParent.getParent());
        if (this.commandAliasParent.getPermission() < 0 && this.commandAliasParent.getPermission() >= 4) {
            argumentBuilder = argumentBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(this.commandAliasParent.getPermission()));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeAction(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new ObjectArrayList<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CustomCommandChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<S, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new ObjectArrayList<>());
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
     * @return ArgumentBuilder
     */
    private ArgumentBuilder<S, ?> buildCommandChild(CustomCommandChild child, CommandDispatcher<S> dispatcher, List<String> inputs) {
        ArgumentBuilder<S, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = this.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeManager.contains(child.getArgumentType())) {
                argumentBuilder = this.argument(child.getChild(), this.argumentTypeManager.getValue(child.getArgumentType()));
                inputs.add(child.getChild());
            } else {
                CommandAliasesMod.getLogger().warn("Invalid Argument Type: {}", child.getArgumentType());
            }
        }
        if (argumentBuilder != null) {
            // Assign permission
            if (child.getPermission() < 0 && child.getPermission() >= 4) {
                argumentBuilder = argumentBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(child.getPermission()));
            }

            if (child.isOptional()) {
                argumentBuilder = argumentBuilder.executes(context -> {
                    //Execution action here
                    return this.executeAction(child.getActions(), child.getMessage(), dispatcher, context, inputs);
                });
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CustomCommandChild subChild : child.getChildren()) {
                    ArgumentBuilder<S, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new ObjectArrayList<>(inputs));
                    argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                }
            }
        }
        return argumentBuilder;
    }

    protected abstract int executeAction(List<CustomCommandAction> actions, String message, CommandDispatcher<S> dispatcher, CommandContext<S> context, List<String> currentInputList);

    protected abstract int executeCommand(List<CustomCommandAction> actions, CommandDispatcher<S> dispatcher, CommandContext<S> context, List<String> currentInputList);

    protected abstract String formatString(CommandContext<S> context, List<String> currentInputList, String string);
}
