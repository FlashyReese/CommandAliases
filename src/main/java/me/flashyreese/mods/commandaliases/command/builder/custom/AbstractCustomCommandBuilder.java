package me.flashyreese.mods.commandaliases.command.builder.custom;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.Permissions;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.command.builder.CommandBuilderDelegate;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.*;
import me.flashyreese.mods.commandaliases.command.impl.ArgumentTypeMapper;
import me.flashyreese.mods.commandaliases.command.impl.FunctionProcessor;
import me.flashyreese.mods.commandaliases.command.impl.InputMapper;
import me.flashyreese.mods.commandaliases.command.loader.AbstractCommandAliasesProvider;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an Abstract Custom Command Builder
 * <p>
 * Used to build a LiteralArgumentBuilder
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.4.0
 */
public abstract class AbstractCustomCommandBuilder<S extends CommandSource> implements CommandBuilderDelegate<S> {
    protected final CustomCommand commandAliasParent;

    protected final ArgumentTypeMapper argumentTypeMapper;
    protected final FunctionProcessor<S> functionProcessor;
    protected final InputMapper<S> inputMapper;

    protected final AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider;

    public AbstractCustomCommandBuilder(CustomCommand commandAliasParent, AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider) {
        this.argumentTypeMapper = new ArgumentTypeMapper();
        this.commandAliasParent = commandAliasParent;
        this.abstractCommandAliasesProvider = abstractCommandAliasesProvider;
        this.functionProcessor = new FunctionProcessor<>(abstractCommandAliasesProvider);
        this.inputMapper = new InputMapper<>();
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
    protected LiteralArgumentBuilder<S> buildCommandParent(CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> argumentBuilder = this.literal(this.commandAliasParent.getCommand());
        if (this.commandAliasParent.getPermission() > 0 && this.commandAliasParent.getPermission() <= 4) {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getCommand(), this.commandAliasParent.getPermission()));
        } else {
            argumentBuilder = argumentBuilder.requires(Permissions.require(this.commandAliasParent.getCommand(), true));
        }

        if (this.commandAliasParent.isOptional()) {
            argumentBuilder = argumentBuilder.executes(context -> {
                //Execution action here
                return this.executeCommand(this.commandAliasParent.getActions(), this.commandAliasParent.getMessage(), dispatcher, context, new ObjectArrayList<>());
            });
        }
        if (this.commandAliasParent.getChildren() != null && !this.commandAliasParent.getChildren().isEmpty()) {
            for (CustomCommandChild child : this.commandAliasParent.getChildren()) {
                ArgumentBuilder<S, ?> subArgumentBuilder = this.buildCommandChild(child, dispatcher, new ObjectArrayList<>(), this.commandAliasParent.getCommand());
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
    protected ArgumentBuilder<S, ?> buildCommandChild(CustomCommandChild child, CommandDispatcher<S> dispatcher, List<String> inputs, String permission) {
        ArgumentBuilder<S, ?> argumentBuilder = null;
        if (child.getType().equals("literal")) {
            argumentBuilder = this.literal(child.getChild());
        } else if (child.getType().equals("argument")) {
            if (this.argumentTypeMapper.getArgumentMap().containsKey(child.getArgumentType())) {
                argumentBuilder = this.argument(child.getChild(), this.argumentTypeMapper.getArgumentMap().get(child.getArgumentType()));
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
                argumentBuilder = this.buildCommandChildSuggestion(dispatcher, argumentBuilder, child, new ObjectArrayList<>(inputs));
            }
            //Start building children if exist
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                for (CustomCommandChild subChild : child.getChildren()) {
                    ArgumentBuilder<S, ?> subArgumentBuilder = this.buildCommandChild(subChild, dispatcher, new ObjectArrayList<>(inputs), permissionString);
                    if (subArgumentBuilder != null) {
                        argumentBuilder = argumentBuilder.then(subArgumentBuilder);
                    }
                }
            }
        }
        return argumentBuilder;
    }

    /**
     * Executes command action
     *
     * @param actions          List of command actions
     * @param message          Message
     * @param dispatcher       The command dispatcher
     * @param context          The command context
     * @param currentInputList User input list
     * @return Command execution state
     */
    protected int executeCommand(List<CustomCommandAction> actions, String message, CommandDispatcher<S> dispatcher, CommandContext<S> context, List<String> currentInputList) {
        int state = Command.SINGLE_SUCCESS;
        if (actions != null && !actions.isEmpty())
            state = this.performActions(actions, dispatcher, context, currentInputList);

        if (message != null && !message.isEmpty()) {
            String formatString = this.formatString(context, currentInputList, message);
            this.sendFeedback(context, formatString);
        }
        return state;
    }

    /**
     * Builds suggestion provider for child components using argument types and has a specified suggestion provider.
     *
     * @param dispatcher      The command dispatcher
     * @param argumentBuilder The argument builder
     * @param child           The child component
     * @param inputs          User input list
     * @return The argument builder with suggestion provider if specified
     */
    @SuppressWarnings("unchecked")
    public ArgumentBuilder<S, ?> buildCommandChildSuggestion(CommandDispatcher<S> dispatcher, ArgumentBuilder<S, ?> argumentBuilder, CustomCommandChild child, List<String> inputs) {
        if (!(argumentBuilder instanceof RequiredArgumentBuilder))
            return argumentBuilder;

        SuggestionProvider<S> SUGGESTION_PROVIDER;

        CustomCommandSuggestionProvider suggestionProvider = child.getSuggestionProvider();

        if (suggestionProvider.getSuggestionMode() == null) {
            CommandAliasesMod.logger().warn("Missing suggestion mode in \"{}\"", child.getChild());
            return argumentBuilder;
        }

        if (suggestionProvider.getSuggestion() == null || suggestionProvider.getSuggestion().isEmpty()) {
            CommandAliasesMod.logger().warn("Missing suggestion in \"{}\"", child.getChild());
            return argumentBuilder;
        }

        if (Arrays.stream(CustomCommandSuggestionMode.values()).filter(value -> suggestionProvider.getSuggestionMode() == value).count() != 1) {
            CommandAliasesMod.logger().error("Invalid suggestion mode \"{}\"", suggestionProvider.getSuggestionMode());
            return argumentBuilder;
        }

        if (suggestionProvider.getSuggestionMode() == CustomCommandSuggestionMode.COMMAND_LIST_LOOKUP) {
            CommandNode<S> redirect = dispatcher.findNode(Lists.newArrayList(suggestionProvider.getSuggestion().split(" ")));
            if (redirect != null && redirect.createBuilder() instanceof RequiredArgumentBuilder requiredArgumentBuilder) {
                SUGGESTION_PROVIDER = requiredArgumentBuilder.getSuggestionsProvider();
            } else {
                CommandAliasesMod.logger().error("Invalid suggestion \"{}\"", suggestionProvider.getSuggestion());
                return argumentBuilder;
            }
        } else if (suggestionProvider.getSuggestionMode() == CustomCommandSuggestionMode.JSON_LIST) {
            SUGGESTION_PROVIDER = (context, builder) -> {
                long start = System.nanoTime();
                String formattedSuggestion = this.formatString(context, inputs, suggestionProvider.getSuggestion());
                List<String> suggestions = new Gson().fromJson(formattedSuggestion, new TypeToken<List<String>>() {
                }.getType());
                long end = System.nanoTime();
                if (CommandAliasesMod.options().debugSettings.showProcessingTime) {
                    CommandAliasesMod.logger().info("""
                                    \n\t======================================================
                                    \tSuggestion Provider: {}
                                    \tProcessing time: {}ms
                                    \t======================================================""",
                            formattedSuggestion, (end - start) / 1000000.0);
                }
                return CommandSource.suggestMatching(suggestions.stream().map(StringArgumentType::escapeIfRequired), builder);
            };
        } else {
            SUGGESTION_PROVIDER = (context, builder) -> {
                List<String> suggestions = new ObjectArrayList<>();
                long start = System.nanoTime();
                String formattedSuggestion = this.formatString(context, inputs, suggestionProvider.getSuggestion());
                this.abstractCommandAliasesProvider.getDatabase().map().forEach((key, value) -> {
                    if (!(suggestionProvider.getSuggestionMode() == CustomCommandSuggestionMode.DATABASE_STARTS_WITH && key.startsWith(formattedSuggestion)) &&
                            !(suggestionProvider.getSuggestionMode() == CustomCommandSuggestionMode.DATABASE_ENDS_WITH && key.endsWith(formattedSuggestion)) &&
                            !(suggestionProvider.getSuggestionMode() == CustomCommandSuggestionMode.DATABASE_CONTAINS && key.contains(formattedSuggestion)))
                        return;

                    suggestions.add(value);
                });
                long end = System.nanoTime();
                if (CommandAliasesMod.options().debugSettings.showProcessingTime) {
                    CommandAliasesMod.logger().info("""
                                    \n\t======================================================
                                    \tSuggestion Provider: {}
                                    \t"Processing time: {}ms
                                    \t======================================================""",
                            formattedSuggestion, (end - start) / 1000000.0);
                }
                return CommandSource.suggestMatching(suggestions.stream().map(StringArgumentType::escapeIfRequired), builder);
            };
        }

        RequiredArgumentBuilder<S, ?> requiredArgumentBuilder = (RequiredArgumentBuilder<S, ?>) argumentBuilder;
        return requiredArgumentBuilder.suggests(SUGGESTION_PROVIDER);
    }

    /**
     * Executes command in command action
     *
     * @param actions          List of command actions
     * @param dispatcher       The command dispatcher
     * @param context          The command context
     * @param currentInputList User input list
     * @return Command execution state
     */
    protected int performActions(List<CustomCommandAction> actions, CommandDispatcher<S> dispatcher, CommandContext<S> context, List<String> currentInputList) {
        Queue<CustomCommandAction> customCommandActionQueue = new LinkedList<>(actions);
        return this.scheduleAction(customCommandActionQueue, System.currentTimeMillis(), dispatcher, context, currentInputList);
    }


    /**
     * Schedules all actions and sub-actions recursively
     *
     * @param customCommandActionQueue Queue of actions
     * @param triggerTime              triggerTime
     * @param dispatcher               The command dispatcher
     * @param context                  The command context
     * @param currentInputList         User input list
     * @return Command execution state
     */
    private int scheduleAction(Queue<CustomCommandAction> customCommandActionQueue, long triggerTime, CommandDispatcher<S> dispatcher, CommandContext<S> context, List<String> currentInputList) {
        AtomicInteger state = new AtomicInteger();
        if (customCommandActionQueue.isEmpty())
            return Command.SINGLE_SUCCESS;

        CustomCommandAction action = customCommandActionQueue.poll();
        String eventName = "generic";
        if (action.getId() != null && !action.getId().isEmpty())
            eventName = this.formatString(context, currentInputList, action.getId());

        if (action.getStartTime() != null && !action.getStartTime().isEmpty()) {
            String time = this.formatString(context, currentInputList, action.getStartTime());
            triggerTime = System.currentTimeMillis() + Long.parseLong(time);
        }

        this.abstractCommandAliasesProvider.getScheduler().addEvent(new Scheduler.Event(triggerTime, eventName, () -> {
            if (action.getCommand() != null && action.getCommandType() != null) {
                long startFormat = System.nanoTime();
                String actionCommand = this.formatString(context, currentInputList, action.getCommand());
                long endFormat = System.nanoTime();
                try {
                    state.set(this.dispatcherExecute(action, dispatcher, context, actionCommand));
                } catch (CommandSyntaxException e) {
                    if (CommandAliasesMod.options().debugSettings.debugMode) {
                        CommandAliasesMod.logger().error("""
                                \n\t======================================================
                                \tFailed to process command
                                \tOriginal Action Command: {}
                                \tOriginal Action Command Type: {}
                                \tPost Processed Action Command: {}
                                \t======================================================""", action.getCommand(), action.getCommandType(), actionCommand);
                        String output = e.getLocalizedMessage();
                        this.sendFeedback(context, output);
                    }
                    e.printStackTrace();
                }
                long endExecution = System.nanoTime();
                if (CommandAliasesMod.options().debugSettings.showProcessingTime) {
                    CommandAliasesMod.logger().info("""
                                    \n\t======================================================
                                    \tOriginal Action Command: {}
                                    \tOriginal Action Command Type: {}
                                    \tPost Processed Action Command: {}
                                    \tFormatting time: {}ms
                                    \tExecuting time: {}ms
                                    \t======================================================""",
                            action.getCommand(), action.getCommandType(), actionCommand, (endFormat - startFormat) / 1000000.0, (endExecution - endFormat) / 1000000.0);
                }
                if (state.get() != Command.SINGLE_SUCCESS) {
                    if (action.getMessageIfUnsuccessful() != null) {
                        String message = this.formatString(context, currentInputList, action.getMessageIfUnsuccessful());
                        this.sendFeedback(context, message);
                    }
                    if (action.getActionsIfUnsuccessful() != null && !action.getActionsIfUnsuccessful().isEmpty()) {
                        Queue<CustomCommandAction> unsuccessfulActionsQueue = new LinkedList<>(action.getActionsIfUnsuccessful());
                        state.set(this.scheduleAction(unsuccessfulActionsQueue, System.currentTimeMillis(), dispatcher, context, currentInputList));
                    }
                    if (action.isRequireSuccess()) {
                        customCommandActionQueue.clear();
                    }
                } else {
                    if (action.getMessageIfSuccessful() != null) {
                        String message = this.formatString(context, currentInputList, action.getMessageIfSuccessful());
                        this.sendFeedback(context, message);
                    }
                    if (action.getActionsIfSuccessful() != null && !action.getActionsIfSuccessful().isEmpty()) {
                        Queue<CustomCommandAction> successfulActionsQueue = new LinkedList<>(action.getActionsIfSuccessful());
                        state.set(this.scheduleAction(successfulActionsQueue, System.currentTimeMillis(), dispatcher, context, currentInputList));
                    }
                }
            }
            if (action.getMessage() != null) {
                String message = this.formatString(context, currentInputList, action.getMessage());
                this.sendFeedback(context, message);
            }
            state.set(this.scheduleAction(customCommandActionQueue, System.currentTimeMillis(), dispatcher, context, currentInputList));
        }));
        return state.get();
    }

    /**
     * Method to format string(command or messages) with user input map.
     *
     * @param context          The command context
     * @param currentInputList User input map with functions
     * @param string           Input string
     * @return Formatted string
     */
    protected String formatString(CommandContext<S> context, List<String> currentInputList, String string) {
        // Input mapping and formatting
        string = this.inputMapper.formatAndMapInputs(string, context, currentInputList, this.argumentTypeMapper);

        // Function processor
        string = this.functionProcessor.processFunctions(string, context.getSource());

        string = string.trim();
        return string;
    }

    /**
     * Provides feedback to the user executing the command
     *
     * @param context The command context
     * @param message The message
     */
    protected abstract void sendFeedback(CommandContext<S> context, String message);

    /**
     * @param action        The custom command action
     * @param dispatcher    The command dispatcher
     * @param context       The command context
     * @param actionCommand The action command after being formatted by {@link #formatString(CommandContext, List, String)}
     * @return Command execution state
     * @throws CommandSyntaxException Thrown if action command is invalid
     */
    protected abstract int dispatcherExecute(CustomCommandAction action, CommandDispatcher<S> dispatcher, CommandContext<S> context, String actionCommand) throws CommandSyntaxException;
}
