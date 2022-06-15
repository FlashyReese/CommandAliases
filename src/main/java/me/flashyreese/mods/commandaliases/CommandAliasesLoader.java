package me.flashyreese.mods.commandaliases;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.CommandType;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ClientCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.custom.ServerCustomCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ClientReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ServerReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.redirect.CommandRedirectBuilder;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.flashyreese.mods.commandaliases.storage.database.leveldb.LevelDBImpl;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the custom command aliases loader.
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.0.9
 */
public class CommandAliasesLoader {

    private final Gson gson = new Gson();
    private final List<CommandAlias> serverCommands = new ObjectArrayList<>();
    private final List<CommandAlias> clientCommands = new ObjectArrayList<>();
    private final List<String> loadedServerCommands = new ObjectArrayList<>();
    private final List<String> loadedClientCommands = new ObjectArrayList<>();
    private final Map<String, String> reassignServerCommandMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, String> reassignClientCommandMap = new Object2ObjectOpenHashMap<>();

    private AbstractDatabase<byte[], byte[]> serverDatabase;
    private AbstractDatabase<byte[], byte[]> clientDatabase;

    private Field literalCommandNodeLiteralField = null;

    public CommandAliasesLoader() {
        try {
            this.literalCommandNodeLiteralField = LiteralCommandNode.class.getDeclaredField("literal");
            this.literalCommandNodeLiteralField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void registerCommandAliases() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            //CommandRegistrationCallback won't work here because it gets called before the server even starts.
            CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();

            if (this.serverDatabase == null) {
                this.serverDatabase = new LevelDBImpl(server.getSavePath(WorldSavePath.ROOT).resolve("commandaliases").toString());
                this.serverDatabase.open();
            }

            this.registerCommandAliasesCommands(dispatcher);
            this.loadCommandAliases();
            this.registerCommands(dispatcher);
        });
    }

    public void registerClientSidedCommandAliases() {
        CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.DISPATCHER;
        if (this.clientDatabase == null) {
            this.clientDatabase = new LevelDBImpl(FabricLoader.getInstance().getGameDir().resolve("commandaliases.client").toString());
            this.clientDatabase.open();
        }
        this.registerClientCommandAliasesCommands(dispatcher);
        this.loadClientCommandAliases();
        this.registerClientCommands(dispatcher);
    }

    /**
     * Loads command aliases file, meant for integrated/dedicated servers.
     */
    private void loadCommandAliases() {
        this.serverCommands.clear();
        this.serverCommands.addAll(this.loadCommandAliases(new File("config/commandaliases.json")));
    }

    /**
     * Loads client command aliases file, meant for clients.
     */
    private void loadClientCommandAliases() {
        this.clientCommands.clear();
        this.clientCommands.addAll(this.loadCommandAliases(new File("config/commandaliases-client.json")));
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param dispatcher Server CommandDispatcher
     */
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.serverCommands.forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_ALIAS) {
                CommandAliasesMod.logger().warn("The command mode \"COMMAND_ALIAS\" is now deprecated and scheduled to remove on version 1.0.0");
                CommandAliasesMod.logger().warn("Please migrate to command mode \"COMMAND_CUSTOM\", it is more feature packed and receives more support. :)");
                LiteralArgumentBuilder<ServerCommandSource> command = new AliasCommandBuilder(cmd.getAliasCommand()).buildCommand(dispatcher);
                if (command != null) {
                    command = command.requires(Permissions.require("commandaliases." + command.getLiteral(), true));
                    dispatcher.register(command);
                    this.loadedServerCommands.add(cmd.getAliasCommand().getCommand());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                LiteralArgumentBuilder<ServerCommandSource> command = new ServerCustomCommandBuilder(cmd.getCustomCommand(), this.serverDatabase).buildCommand(dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.loadedServerCommands.add(cmd.getCustomCommand().getParent());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                LiteralArgumentBuilder<ServerCommandSource> command = null;
                if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    command = new CommandRedirectBuilder<ServerCommandSource>(cmd, CommandType.SERVER).buildCommand(dispatcher);
                } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                    command = new ServerReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.reassignServerCommandMap, this.serverDatabase).buildCommand(dispatcher);
                }
                if (command != null) {
                    //Assign permission for alias Fixme: better implementation
                    command = command.requires(Permissions.require("commandaliases." + command.getLiteral(), true));
                    dispatcher.register(command);
                    if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        this.loadedServerCommands.add(cmd.getRedirectCommand().getCommand());
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                        this.loadedServerCommands.add(cmd.getReassignCommand().getCommand());
                    }
                }
            }
        });
        CommandAliasesMod.logger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' custom commands.
     */
    private void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        this.clientCommands.forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM) {
                LiteralArgumentBuilder<FabricClientCommandSource> command = new ClientCustomCommandBuilder(cmd.getCustomCommand(), this.clientDatabase).buildCommand(dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.loadedClientCommands.add(cmd.getCustomCommand().getParent());
                }
            } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                LiteralArgumentBuilder<FabricClientCommandSource> command = null;
                if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                    command = new CommandRedirectBuilder<FabricClientCommandSource>(cmd, CommandType.CLIENT).buildCommand(dispatcher);
                } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN) {
                    command = new ClientReassignCommandBuilder(cmd, this.literalCommandNodeLiteralField, this.reassignClientCommandMap, this.clientDatabase).buildCommand(dispatcher);
                }
                if (command != null) {
                    dispatcher.register(command);
                    if (cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) {
                        this.loadedClientCommands.add(cmd.getRedirectCommand().getCommand());
                    } else if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_ALIAS || cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN_AND_CUSTOM) {
                        this.loadedClientCommands.add(cmd.getReassignCommand().getCommand());
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
    private void registerCommandAliasesCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("commandaliases").requires(Permissions.require("commandaliases", 4))
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(new LiteralText("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(new LiteralText(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED)), false));

                    return Command.SINGLE_SUCCESS;
                })
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                    if (this.serverDatabase.read(key) != null) {
                                                        this.serverDatabase.delete(key);
                                                    }
                                                    this.serverDatabase.write(key, value);
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
                                            if (this.serverDatabase.read(key) != null) {
                                                this.serverDatabase.delete(key);
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
                                            byte[] value = this.serverDatabase.read(key);
                                            if (value != null) {
                                                context.getSource().sendFeedback(new LiteralText(new String(value, StandardCharsets.UTF_8)), CommandAliasesMod.options().debugSettings.broadcastToOps);
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
                                            byte[] value = this.serverDatabase.read(key);
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
                                    context.getSource().sendFeedback(new LiteralText("Reloading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.unregisterServerCommands(dispatcher);
                                    this.loadCommandAliases();
                                    this.registerCommands(dispatcher);

                                    //Update Command Tree
                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }

                                    context.getSource().sendFeedback(new LiteralText("Reloaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("load").requires(Permissions.require("commandaliases.load", 4))
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Loading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.loadCommandAliases();
                                    this.registerCommands(dispatcher);

                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(new LiteralText("Loaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(CommandManager.literal("unload").requires(Permissions.require("commandaliases.unload", 4))
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Unloading all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    this.unregisterServerCommands(dispatcher);

                                    for (ServerPlayerEntity e : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        context.getSource().getServer().getPlayerManager().sendCommandTree(e);
                                    }
                                    context.getSource().sendFeedback(new LiteralText("Unloaded all Command Aliases!"), CommandAliasesMod.options().debugSettings.broadcastToOps);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    /**
     * Registers all client Command Aliases' commands
     */
    private void registerClientCommandAliasesCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("commandaliases:client")
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> context.getSource().sendFeedback(new LiteralText("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(new LiteralText(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED))));

                    return Command.SINGLE_SUCCESS;
                })
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                            if (this.serverDatabase.read(key) != null) {
                                                                this.serverDatabase.delete(key);
                                                            }
                                                            this.serverDatabase.write(key, value);
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
                                                    if (this.serverDatabase.read(key) != null) {
                                                        this.serverDatabase.delete(key);
                                                    }
                                                    this.serverDatabase.write(key, value);
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
                                            if (this.serverDatabase.read(key) != null) {
                                                this.serverDatabase.delete(key);
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
                                            byte[] value = this.serverDatabase.read(key);
                                            if (value != null) {
                                                context.getSource().sendFeedback(new LiteralText(new String(value, StandardCharsets.UTF_8)));
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
                                            byte[] value = this.serverDatabase.read(key);
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
                                    context.getSource().sendFeedback(new LiteralText("Reloading all client Command Aliases!"));
                                    this.unregisterClientCommands(dispatcher);
                                    this.loadClientCommandAliases();
                                    this.registerClientCommands(dispatcher);
                                    context.getSource().sendFeedback(new LiteralText("Reloaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("load")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Loading all client Command Aliases!"));
                                    this.loadClientCommandAliases();
                                    this.registerClientCommands(dispatcher);
                                    context.getSource().sendFeedback(new LiteralText("Loaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
                .then(ClientCommandManager.literal("unload")
                        .executes(context -> {
                                    context.getSource().sendFeedback(new LiteralText("Unloading all client Command Aliases!"));
                                    this.unregisterClientCommands(dispatcher);
                                    context.getSource().sendFeedback(new LiteralText("Unloaded all client Command Aliases!"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    /**
     * Unregisters all server command aliases and reassignments.
     *
     * @param dispatcher Server CommandDispatcher
     */
    private void unregisterServerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (String cmd : this.loadedServerCommands) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignServerCommandMap.entrySet()) {
            CommandNode<ServerCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<ServerCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getKey())).findFirst().orElse(null);

            if (commandNode != null && commandReassignNode == null) {
                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(entry.getValue()));

                try {
                    this.literalCommandNodeLiteralField.set(commandNode, entry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                dispatcher.getRoot().addChild(commandNode);
            }
        }

        this.reassignServerCommandMap.clear();
        this.loadedServerCommands.clear();
    }

    /**
     * Unregisters all client command aliases and reassignments.
     */
    private void unregisterClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (String cmd : this.loadedClientCommands) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignClientCommandMap.entrySet()) {
            CommandNode<FabricClientCommandSource> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<FabricClientCommandSource> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getKey())).findFirst().orElse(null);

            if (commandNode != null && commandReassignNode == null) {
                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(entry.getValue()));

                try {
                    this.literalCommandNodeLiteralField.set(commandNode, entry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                dispatcher.getRoot().addChild(commandNode);
            }
        }

        this.reassignClientCommandMap.clear();
        this.loadedClientCommands.clear();
    }

    /**
     * Reads JSON file and serializes them to a List of CommandAliases
     *
     * @param file JSON file path
     * @return List of CommandAliases
     */
    private List<CommandAlias> loadCommandAliases(File file) {
        List<CommandAlias> commandAliases = new ObjectArrayList<>();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                commandAliases = gson.fromJson(reader, new TypeToken<List<CommandAlias>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse CommandAliases File", e);
            }
        } else {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                String json = gson.toJson(new ObjectArrayList<>());
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("Could not write CommandAliases File", e);
            }
        }

        return commandAliases;
    }
}
