package me.flashyreese.mods.commandaliases.command.loader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.command.*;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.reassign.ReassignCommandBuilder;
import me.flashyreese.mods.commandaliases.command.builder.reassign.format.ReassignCommand;
import me.flashyreese.mods.commandaliases.command.builder.redirect.CommandRedirectBuilder;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;
import me.flashyreese.mods.commandaliases.math.ExtendedDoubleEvaluator;
import me.flashyreese.mods.commandaliases.math.SimpleBooleanEvaluator;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.flashyreese.mods.commandaliases.util.Atomic;
import me.flashyreese.mods.commandaliases.util.TreeNode;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents the command aliases provider.
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.9.0
 */
public abstract class AbstractCommandAliasesProvider<S extends CommandSource> {
    private final ObjectMapper jsonMapper = new JsonMapper();
    private final ObjectMapper json5Mapper = new JsonMapper(JsonFactory.builder().enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA).enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS).enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
            //.enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS).enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS) todo: enable for jackson 2.14
            .build());
    private final ObjectMapper tomlMapper = new TomlMapper();
    private final ObjectMapper yamlMapper = new YAMLMapper();

    private final List<CommandAlias> commands = new ObjectArrayList<>();
    private final List<String> loadedCommands = new ObjectArrayList<>();
    private final Map<String, String> reassignedCommandMap = new Object2ObjectOpenHashMap<>();
    private final Path commandsDirectory;
    private final Field literalCommandNodeLiteralField;
    private final String rootCommand;
    private AbstractDatabase<byte[], byte[]> database;
    private Scheduler scheduler;

    public AbstractCommandAliasesProvider(Path commandsDirectory, Field literalCommandNodeLiteralField, String rootCommand) {
        this.commandsDirectory = commandsDirectory;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
        this.rootCommand = rootCommand;
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param dispatcher Server CommandDispatcher
     */
    protected void registerCommands(CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess) {
        // Load reassignments first
        this.getCommands().stream().filter(cmd -> cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN).forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_REASSIGN && cmd instanceof ReassignCommand reassignCommand) {
                new ReassignCommandBuilder<S>(reassignCommand, this.literalCommandNodeLiteralField, this.getReassignedCommandMap(), CommandType.SERVER).buildCommand(dispatcher);
                this.getLoadedCommands().add(reassignCommand.getCommand());
            }
        });
        // Load other commands
        this.getCommands().stream().filter(cmd -> cmd.getCommandMode() != CommandMode.COMMAND_REASSIGN).forEach(cmd -> {
            if (cmd.getCommandMode() == CommandMode.COMMAND_CUSTOM && cmd instanceof CustomCommand customCommand) {
                LiteralArgumentBuilder<S> command = this.buildCustomCommand(customCommand, this, registryAccess, dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.getLoadedCommands().add(customCommand.getCommand());
                }
            } else if ((cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) && cmd instanceof RedirectCommand redirectCommand) {
                LiteralArgumentBuilder<S> command = new CommandRedirectBuilder<S>(redirectCommand, CommandType.SERVER).buildCommand(dispatcher);
                if (command != null) {
                    //Assign permission for alias Fixme: better implementation
                    command = command.requires(Permissions.require("commandaliases." + command.getLiteral(), true));
                    dispatcher.register(command);
                    this.getLoadedCommands().add(redirectCommand.getCommand());
                }
            }
        });
        CommandAliasesMod.logger().info("Registered/Reloaded all your commands :P, you can now single command nuke!");
    }

    /**
     * Registers all server Command Aliases' commands
     *
     * @param dispatcher The CommandDispatcher
     */
    protected void registerCommandAliasesCommands(CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(this.literal(this.rootCommand).requires(Permissions.require("commandaliases", 4))
                .executes(context -> {
                    Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer("commandaliases");
                    modContainerOptional.ifPresent(modContainer -> this.sendFeedback(context.getSource(), Text.literal("Running Command Aliases")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(" v" + modContainer.getMetadata().getVersion()).formatted(Formatting.RED))));

                    return Command.SINGLE_SUCCESS;
                })
                .then(this.literal("scheduler")
                        .then(this.literal("remove")
                                .then(this.argument("eventName", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String eventName = StringArgumentType.getString(context, "eventName");
                                            if (this.getScheduler().contains(eventName) && this.getScheduler().remove(eventName))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )
                )
                .then(this.literal("compute").requires(Permissions.require("commandaliases.compute", 4))
                        .then(this.literal("equals").requires(Permissions.require("commandaliases.compute.equals", 4))
                                .then(this.argument("value1", StringArgumentType.string())
                                        .then(this.argument("value2", StringArgumentType.string())
                                                .executes(context -> {
                                                    if (StringArgumentType.getString(context, "value1").equals(StringArgumentType.getString(context, "value2")))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("boolean").requires(Permissions.require("commandaliases.compute.boolean", 4))
                                .then(this.argument("expression", StringArgumentType.string())
                                        .executes(context -> {
                                            if (new SimpleBooleanEvaluator().evaluate(StringArgumentType.getString(context, "expression")))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("expression", StringArgumentType.string())
                                                .executes(context -> {
                                                    String originalKey = StringArgumentType.getString(context, "key");
                                                    String expression = StringArgumentType.getString(context, "expression");

                                                    boolean finalValue = new SimpleBooleanEvaluator().evaluate(expression);

                                                    byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                    byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                    if (this.getDatabase().read(key) != null) {
                                                        this.getDatabase().delete(key);
                                                    }
                                                    this.getDatabase().write(key, value);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("evaluate").requires(Permissions.require("commandaliases.compute.evaluate", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("expression", StringArgumentType.string())
                                                .executes(context -> {
                                                    String originalKey = StringArgumentType.getString(context, "key");
                                                    String expression = StringArgumentType.getString(context, "expression");

                                                    double finalValue = new ExtendedDoubleEvaluator().evaluate(expression);

                                                    byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                    byte[] value = String.valueOf(finalValue).getBytes(StandardCharsets.UTF_8);
                                                    if (this.getDatabase().read(key) != null) {
                                                        this.getDatabase().delete(key);
                                                    }
                                                    this.getDatabase().write(key, value);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(this.literal("database").requires(Permissions.require("commandaliases.database", 4))
                        .then(this.literal("put").requires(Permissions.require("commandaliases.database.put", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String originalKey = StringArgumentType.getString(context, "key");
                                                    String originalValue = StringArgumentType.getString(context, "value");

                                                    byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                                    byte[] value = originalValue.getBytes(StandardCharsets.UTF_8);
                                                    if (this.getDatabase().read(key) != null) {
                                                        this.getDatabase().delete(key);
                                                    }
                                                    this.getDatabase().write(key, value);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("delete").requires(Permissions.require("commandaliases.database.delete", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            if (this.getDatabase().read(key) != null) {
                                                this.getDatabase().delete(key);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(this.literal("match").requires(Permissions.require("commandaliases.database.match", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.getDatabase().read(key);
                                            if (value != null) {
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(this.literal("get").requires(Permissions.require("commandaliases.database.get", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String originalKey = StringArgumentType.getString(context, "key");

                                            byte[] key = originalKey.getBytes(StandardCharsets.UTF_8);
                                            byte[] value = this.getDatabase().read(key);
                                            if (value != null) {
                                                this.sendFeedback(context.getSource(), Text.literal(new String(value, StandardCharsets.UTF_8)));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(this.literal("reload").requires(Permissions.require("commandaliases.reload", 4))
                        .executes(context -> this.commandAliasesReload(context, dispatcher, registryAccess))
                )
                .then(this.literal("load").requires(Permissions.require("commandaliases.load", 4))
                        .executes(context -> this.commandAliasesLoad(context, dispatcher, registryAccess))
                )
                .then(this.literal("unload").requires(Permissions.require("commandaliases.unload", 4))
                        .executes(context -> this.commandAliasesUnload(context, dispatcher, registryAccess))
                )
        );
    }

    protected abstract void sendFeedback(S source, Text text);

    protected abstract int commandAliasesLoad(CommandContext<S> context, CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess);

    protected abstract int commandAliasesUnload(CommandContext<S> context, CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess);

    protected abstract int commandAliasesReload(CommandContext<S> context, CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess);

    protected abstract LiteralArgumentBuilder<S> buildCustomCommand(CustomCommand customCommand, AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider, CommandRegistryAccess registryAccess, CommandDispatcher<S> dispatcher);

    /**
     * Loads command aliases file, meant for integrated/dedicated servers.
     */
    protected void loadCommandAliases() {
        this.commands.clear();
        this.commands.addAll(this.loadCommandAliasesFromDirectory(this.commandsDirectory.toFile()));
    }

    /**
     * Unregisters all command aliases and reassignments.
     *
     * @param dispatcher CommandDispatcher
     */
    protected void unregisterCommands(CommandDispatcher<S> dispatcher) {
        for (String cmd : this.loadedCommands) {
            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(cmd));
        }
        for (Map.Entry<String, String> entry : this.reassignedCommandMap.entrySet()) {
            CommandNode<S> commandNode = dispatcher.getRoot().getChildren().stream().filter(node ->
                    node.getName().equals(entry.getValue())).findFirst().orElse(null);

            CommandNode<S> commandReassignNode = dispatcher.getRoot().getChildren().stream().filter(node ->
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

        this.reassignedCommandMap.clear();
        this.loadedCommands.clear();
    }

    /**
     * Reads directory for command aliases files and serializes them to a List of CommandAliases
     *
     * @param file Directory File Path
     * @return List of CommandAliases
     */
    private @NotNull List<CommandAlias> loadCommandAliasesFromDirectory(File file) {
        List<CommandAlias> commandAliases = new ObjectArrayList<>();

        if (file.exists()) {
            String output = "\n" + loadAndRenderDirectoryTree(this.createDirectoryTree(file), commandAliases);
            CommandAliasesMod.logger().info(output);
        } else {
            if (file.mkdir()) {
                CommandAliasesMod.logger().info("Command Aliases directory created at \"{}\"", file.getAbsolutePath());
            } else {
                CommandAliasesMod.logger().error("Could not create directory for Command Aliases at \"{}\"", file.getAbsolutePath());
            }
        }

        return commandAliases;
    }

    private TreeNode<File> createDirectoryTree(File directory) {
        if (!directory.isDirectory())
            throw new RuntimeException("Not a directory " + directory.getAbsolutePath());
        TreeNode<File> rootTree = new TreeNode<>(directory);
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            if (file.isDirectory()) {
                rootTree.addChildTreeNode(this.createDirectoryTree(file));
            } else if (file.isFile()) {
                rootTree.addChild(file);
            }
        }
        return rootTree;
    }

    public String loadAndRenderDirectoryTree(TreeNode<File> tree, List<CommandAlias> commandAliases) {
        List<StringBuilder> lines = loadAndRenderDirectoryTreeLines(tree, commandAliases);
        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        for (StringBuilder line : lines) {
            sb.append(line);
            if (lines.get(lines.size() - 1) != line) {
                sb.append(newline);
            }
        }
        return sb.toString();
    }

    private Class<? extends CommandAlias> getCommandModeClass(CommandMode commandMode) {
        if (commandMode == CommandMode.COMMAND_CUSTOM) {
            return CustomCommand.class;
        } else if (commandMode == CommandMode.COMMAND_REASSIGN) {
            return ReassignCommand.class;
        } else if (commandMode == CommandMode.COMMAND_REDIRECT_NOARG || commandMode == CommandMode.COMMAND_REDIRECT) {
            return RedirectCommand.class;
        }
        return null;
    }

    private List<CommandAlias> objectMapDataFormat(ObjectMapper objectMapper, File file, Atomic<String> state) throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<CommandAlias> commandAliases = new ArrayList<>();
        CommandAlias commandAlias = objectMapper.readerFor(CommandAlias.class).readValue(file);
        if (commandAlias.getSchemaVersion() == 1) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            commandAliases.add(objectMapper.readerFor(this.getCommandModeClass(commandAlias.getCommandMode())).readValue(file));
            state.set(" - Successfully loaded");
        } else {
            state.set(" - Unsupported schema version");
        }
        return commandAliases;
    }

    public List<StringBuilder> loadAndRenderDirectoryTreeLines(TreeNode<File> tree, List<CommandAlias> commandAliases) {
        List<StringBuilder> result = new ArrayList<>();
        Atomic<String> state = new Atomic<>("");
        if (tree.getData().isFile()) {
            File file = tree.getData();
            try {
                if (file.getAbsolutePath().endsWith(".toml")) {
                    commandAliases.addAll(this.objectMapDataFormat(this.tomlMapper, file, state));
                } else if (file.getAbsolutePath().endsWith(".json")) {
                    commandAliases.addAll(this.objectMapDataFormat(this.jsonMapper, file, state));
                } else if (file.getAbsolutePath().endsWith(".json5")) {
                    commandAliases.addAll(this.objectMapDataFormat(this.json5Mapper, file, state));
                    CommandAliasesMod.logger().warn("JSON5 not fully supported yet! \"{}\"", file.getAbsolutePath());
                } else if (file.getAbsolutePath().endsWith(".yml")) {
                    commandAliases.addAll(this.objectMapDataFormat(this.yamlMapper, file, state));
                } else {
                    state.set(" - Unsupported data format type");
                }
            } catch (IOException e) {
                state.set(" - Failed to load");
                CommandAliasesMod.logger().error("Failed to load file at \"{}\" throws {}", file.getAbsolutePath(), e);
            }
        }
        result.add(new StringBuilder().append(tree.getData().getName()).append(state.get()));
        Iterator<TreeNode<File>> iterator = tree.getChildren().iterator();
        while (iterator.hasNext()) {
            List<StringBuilder> subtree = loadAndRenderDirectoryTreeLines(iterator.next(), commandAliases);
            Iterator<StringBuilder> iteratorSB = subtree.iterator();
            if (iterator.hasNext()) {
                result.add(iteratorSB.next().insert(0, "├── "));
                while (iteratorSB.hasNext()) {
                    result.add(iteratorSB.next().insert(0, "│   "));
                }
            } else {
                result.add(iteratorSB.next().insert(0, "└── "));
                while (iteratorSB.hasNext()) {
                    result.add(iteratorSB.next().insert(0, "    "));
                }
            }
        }
        return result;
    }

    LiteralArgumentBuilder<S> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public List<CommandAlias> getCommands() {
        return commands;
    }

    public List<String> getLoadedCommands() {
        return loadedCommands;
    }

    public Map<String, String> getReassignedCommandMap() {
        return reassignedCommandMap;
    }

    public Path getCommandsDirectory() {
        return commandsDirectory;
    }

    public AbstractDatabase<byte[], byte[]> getDatabase() {
        return database;
    }

    public void setDatabase(AbstractDatabase<byte[], byte[]> database) {
        this.database = database;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
