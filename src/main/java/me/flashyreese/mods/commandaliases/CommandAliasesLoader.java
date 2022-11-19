package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ClientReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ServerReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.redirect.CommandRedirectBuilder;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 0.9.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private final CommandAliasesProvider serverCommandAliasesProvider;
    private final CommandAliasesProvider clientCommandAliasesProvider;
    private Field literalCommandNodeLiteralField = null;

    private static final Identifier ALIASES_REGISTRATION_PHASE_ID = Identifier.of("commandaliases", "register_aliases_phase");

    public CommandAliasesLoader() {
        try {
            this.literalCommandNodeLiteralField = LiteralCommandNode.class.getDeclaredField("literal");
            this.literalCommandNodeLiteralField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        this.serverCommandAliasesProvider = new CommandAliasesProvider(FabricLoader.getInstance().getConfigDir().resolve("commandaliases"), this.literalCommandNodeLiteralField);
        this.clientCommandAliasesProvider = new CommandAliasesProvider(FabricLoader.getInstance().getConfigDir().resolve("commandaliases-client"), this.literalCommandNodeLiteralField);
    }

    public void registerCommandAliases() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() == null) {
                this.serverCommandAliasesProvider.setDatabase(new LevelDBImpl(server.getSavePath(WorldSavePath.ROOT).resolve("commandaliases").toString()));
                this.serverCommandAliasesProvider.getDatabase().open();
            }

            if (this.serverCommandAliasesProvider.getScheduler() == null) {
                this.serverCommandAliasesProvider.setScheduler(new Scheduler());
            }
        });
        // CommandAliases must perform registration after all other mods, so that mod-added commands can be referenced
        // in Aliases. We add our own phase that must execute after the default phase to achieve this.
        CommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, ALIASES_REGISTRATION_PHASE_ID);
        CommandRegistrationCallback.EVENT.register(ALIASES_REGISTRATION_PHASE_ID, (dispatcher, registryAccess, environment) -> {
            this.registerCommandAliasesCommands(dispatcher, registryAccess);
            this.serverCommandAliasesProvider.loadCommandAliases();
            this.registerCommands(dispatcher, registryAccess);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (this.serverCommandAliasesProvider.getDatabase() != null) {
                this.serverCommandAliasesProvider.getDatabase().close();
                this.serverCommandAliasesProvider.setDatabase(null);
            }
            if (this.serverCommandAliasesProvider.getScheduler() != null) {
                this.serverCommandAliasesProvider.setScheduler(null);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (this.serverCommandAliasesProvider.getScheduler() != null) {
                this.serverCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }

    public void registerClientSidedCommandAliases() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            if (this.clientCommandAliasesProvider.getDatabase() == null) {
                this.clientCommandAliasesProvider.setDatabase(new LevelDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString()));
                this.clientCommandAliasesProvider.getDatabase().open();
            }
            if (this.clientCommandAliasesProvider.getScheduler() == null) {
                this.clientCommandAliasesProvider.setScheduler(new Scheduler());
            }
            this.registerClientCommandAliasesCommands(dispatcher, registryAccess);
            this.clientCommandAliasesProvider.loadCommandAliases();
            this.registerClientCommands(dispatcher, registryAccess);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.clientCommandAliasesProvider.getScheduler() != null) {
                this.clientCommandAliasesProvider.getScheduler().processEvents();
            }
        });
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param dispatcher Server CommandDispatcher
     */
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.serverCommandAliasesProvider.getCommands().forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_ALIAS) {
                CommandAliasesMod.logger().warn("The command mode \"COMMAND_ALIAS\" is now deprecated and scheduled to remove on version 1.0.0");
                CommandAliasesMod.logger().warn("Please migrate to command mode \"COMMAND_CUSTOM\", it is more feature packed and receives more support. :)");
                LiteralArgumentBuilder<ServerCommandSource> command = new AliasCommandBuilder(cmd.getAliasCommand(), registryAccess).buildCommand(dispatcher);
                if (command != null) {
                    command = command.requires(Permissions.require("commandaliases." + command.getLiteral(), true));
                    dispatcher.register(command);
                    this.serverCommandAliasesProvider.getLoadedCommands().add(cmd.getAliasCommand().getCommand());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                LiteralArgumentBuilder<ServerCommandSource> command = new ServerCustomCommandBuilder(cmd.getCustomCommand(), this.serverCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.serverCommandAliasesProvider.getLoadedCommands().add(cmd.getCustomCommand().getParent());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                LiteralArgumentBuilder<ServerCommandSource> command = null;
                if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    command = new CommandRedirectBuilder<ServerCommandSource>(cmd, CommandType.SERVER).buildCommand(dispatcher);
                } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                    command = new ServerReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.serverCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
                }
                if (command != null) {
                    //Assign permission for alias Fixme: better implementation
                    command = command.requires(Permissions.require("commandaliases." + command.getLiteral(), true));
                    dispatcher.register(command);
                    if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        this.serverCommandAliasesProvider.getLoadedCommands().add(cmd.getRedirectCommand().getCommand());
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                        this.serverCommandAliasesProvider.getLoadedCommands().add(cmd.getReassignCommand().getCommand());
                    }
                }
            }
        });
        CommandAliasesMod.logger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' custom commands.
     */
    private void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        this.clientCommandAliasesProvider.getCommands().forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                LiteralArgumentBuilder<FabricClientCommandSource> command = new ClientCustomCommandBuilder(cmd.getCustomCommand(), this.clientCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.clientCommandAliasesProvider.getLoadedCommands().add(cmd.getCustomCommand().getParent());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                LiteralArgumentBuilder<FabricClientCommandSource> command = null;
                if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    command = new CommandRedirectBuilder<FabricClientCommandSource>(cmd, CommandType.CLIENT).buildCommand(dispatcher);
                } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                    command = new ClientReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.clientCommandAliasesProvider, registryAccess).buildCommand(dispatcher);
                }
                if (command != null) {
                    dispatcher.register(command);
                    if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        this.clientCommandAliasesProvider.getLoadedCommands().add(cmd.getRedirectCommand().getCommand());
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                        this.clientCommandAliasesProvider.getLoadedCommands().add(cmd.getReassignCommand().getCommand());
                    }
                }
            }
        });
        CommandAliasesMod.logger().info("Registered/Reloaded all your client commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' commands
     *
     * @param dispatcher The CommandDispatcher
     */
    private void registerCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("commandaliases").requires(Permissions.require("commandaliases", 4))
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(Text.literal("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED)), false));

                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.literal("scheduler")
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("eventName", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String eventName = StringArgumentType.getString(context, "eventName");
                                            if (this.serverCommandAliasesProvider.getScheduler().contains(eventName) && this.serverCommandAliasesProvider.getScheduler().remove(eventName))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("compute").requires(Permissions.require("commandaliases.compute", 4))
                        .then(CommandManager.literal("equals").requires(Permissions.require("commandaliases.compute.equals", 4))
                                .then(CommandManager.argument("value1", StringArgumentType.string())
                                        .then(CommandManager.argument("value2", StringArgumentType.string())
                                                .executes(context -> {
                                                    if (StringArgumentType.getString(context, "value1").equals(StringArgumentType.getString(context, "value2")))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("addition").requires(Permissions.require("commandaliases.compute.addition", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 + originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("subtraction").requires(Permissions.require("commandaliases.compute.subtraction", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 - originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("multiplication").requires(Permissions.require("commandaliases.compute.multiplication", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 * originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("division").requires(Permissions.require("commandaliases.compute.division", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 / originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("modulus").requires(Permissions.require("commandaliases.compute.modulus", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 % originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(CommandManager.literal("database").requires(Permissions.require("commandaliases.database", 4))
                        .then(CommandManager.literal("put").requires(Permissions.require("commandaliases.database.put", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String originalKey = StringArgumentType.getString(context, "key");
                                                    String originalValue = StringArgumentType.getString(context, "value");

                                                    byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                    byte[] value = originalValue.getBytes(StandardCharsets.UTF_8);
                                                    if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                        this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                    }
                                                    this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("delete").requires(Permissions.require("commandaliases.database.delete", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(CommandManager.literal("get").requires(Permissions.require("commandaliases.database.get", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.serverCommandAliasesProvider.getDatabase().read(key);
                                            if (value != null) {
                                                context.getSource().sendFeedback(Text.literal(new String(value, StandardCharsets.UTF_8)), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(CommandManager.literal("contains").requires(Permissions.require("commandaliases.database.contains", 4))
                                .then(CommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.serverCommandAliasesProvider.getDatabase().read(key);
                                            if (value != null) {
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("reload").requires(Permissions.require("commandaliases.reload", 4))
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Reloading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.serverCommandAliasesProvider.unregisterCommands(dispatcher);
                                    this.serverCommandAliasesProvider.loadCommandAliases();
                                    this.registerCommands(dispatcher, registryAccess);

                                    //Update Command Tree
                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }

                                    context.getSource().sendFeedback(Text.literal("Reloaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("load").requires(Permissions.require("commandaliases.load", 4))
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Loading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.serverCommandAliasesProvider.loadCommandAliases();
                                    this.registerCommands(dispatcher, registryAccess);

                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(Text.literal("Loaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("unload").requires(Permissions.require("commandaliases.unload", 4))
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Unloading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.serverCommandAliasesProvider.unregisterCommands(dispatcher);

                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(Text.literal("Unloaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    /**
     * Registers all client Command Aliases' commands
     */
    private void registerClientCommandAliasesCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("commandaliases:client")
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(Text.literal("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED))));

                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("scheduler")
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("eventName", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String eventName = StringArgumentType.getString(context, "eventName");
                                            if (this.clientCommandAliasesProvider.getScheduler().contains(eventName) && this.clientCommandAliasesProvider.getScheduler().remove(eventName))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )
                )
                .then(ClientCommandManager.literal("compute")
                        .then(ClientCommandManager.literal("equals")
                                .then(ClientCommandManager.argument("value1", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value2", StringArgumentType.string())
                                                .executes(context -> {
                                                    if (StringArgumentType.getString(context, "value1").equals(StringArgumentType.getString(context, "value2")))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("addition")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 + originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("subtraction")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 - originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("multiplication")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 * originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("division")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 / originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("modulus")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value1", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("value2", FloatArgumentType.floatArg())
                                                        .executes(context -> {
                                                            String originalKey = StringArgumentType.getString(context, "key");
                                                            float originalValue1 = FloatArgumentType.getFloat(context, "value1");
                                                            float originalValue2 = FloatArgumentType.getFloat(context, "value2");

                                                            float finalValue = originalValue1 % originalValue2;

                                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                            byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                            }
                                                            this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(ClientCommandManager.literal("database")
                        .then(ClientCommandManager.literal("put")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String originalKey = StringArgumentType.getString(context, "key");
                                                    String originalValue = StringArgumentType.getString(context, "value");

                                                    byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                    byte[] value = originalValue.getBytes(StandardCharsets.UTF_8);
                                                    if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                        this.serverCommandAliasesProvider.getDatabase().delete(key);
                                                    }
                                                    this.serverCommandAliasesProvider.getDatabase().write(key, value);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("delete")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            if (this.serverCommandAliasesProvider.getDatabase().read(key) != null) {
                                                this.serverCommandAliasesProvider.getDatabase().delete(key);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("get")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.serverCommandAliasesProvider.getDatabase().read(key);
                                            if (value != null) {
                                                context.getSource().sendFeedback(Text.literal(new String(value, StandardCharsets.UTF_8)));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("contains")
                                .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.serverCommandAliasesProvider.getDatabase().read(key);
                                            if (value != null) {
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
                .then(ClientCommandManager.literal("reload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Reloading all client Command Aliases!"));
                                    this.clientCommandAliasesProvider.unregisterCommands(dispatcher);
                                    this.clientCommandAliasesProvider.loadCommandAliases();
                                    this.registerClientCommands(dispatcher, registryAccess);
                                    context.getSource().sendFeedback(Text.literal("Reloaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("load")
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Loading all client Command Aliases!"));
                                    this.clientCommandAliasesProvider.loadCommandAliases();
                                    this.registerClientCommands(dispatcher, registryAccess);
                                    context.getSource().sendFeedback(Text.literal("Loaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("unload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Unloading all client Command Aliases!"));
                                    this.clientCommandAliasesProvider.unregisterCommands(dispatcher);
                                    context.getSource().sendFeedback(Text.literal("Unloaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }
}
