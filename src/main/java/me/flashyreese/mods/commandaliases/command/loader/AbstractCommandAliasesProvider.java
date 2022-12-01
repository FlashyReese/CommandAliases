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
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
            .enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS).enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS)
            .build());
    private final ObjectMapper tomlMapper = new TomlMapper();
    private final ObjectMapper yamlMapper = new YAMLMapper();

    private final Map<String, CommandAlias> commands = new Object2ObjectOpenHashMap<>();
    private final List<String> loadedCommands = new ObjectArrayList<>();
    private final Map<String, String> reassignedCommandMap = new Object2ObjectOpenHashMap<>();
    private final Path commandsDirectory;
    private final Field literalCommandNodeLiteralField;
    private final String rootCommand;
    private final CommandType commandType;
    private AbstractDatabase<String, String> database;
    private Scheduler scheduler;

    public AbstractCommandAliasesProvider(Path commandsDirectory, Field literalCommandNodeLiteralField, String rootCommand, CommandType commandType) {
        this.commandsDirectory = commandsDirectory;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
        this.rootCommand = rootCommand;
        this.commandType = commandType;
    }

    /**
     * Registers all server Command Aliases' custom commands.
     *
     * @param dispatcher Server CommandDispatcher
     */
    protected void registerCommands(CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess) {
        // Load reassignments first
        this.getCommands().entrySet().stream().filter(cmd -> cmd.getValue().getCommandMode() == CommandMode.COMMAND_REASSIGN).forEach(cmd -> {
            if (cmd.getValue().getCommandMode() == CommandMode.COMMAND_REASSIGN && cmd.getValue() instanceof ReassignCommand reassignCommand) {
                new ReassignCommandBuilder<S>(cmd.getKey(), reassignCommand, this.literalCommandNodeLiteralField, this.getReassignedCommandMap(), this.getLoadedCommands(), this.commandType).buildCommand(dispatcher);
            }
        });
        // Load other commands
        this.getCommands().entrySet().stream().filter(cmd -> cmd.getValue().getCommandMode() != CommandMode.COMMAND_REASSIGN).forEach(cmd -> {
            if (cmd.getValue().getCommandMode() == CommandMode.COMMAND_CUSTOM && cmd.getValue() instanceof CustomCommand customCommand) {
                LiteralArgumentBuilder<S> command = this.buildCustomCommand(cmd.getKey(), customCommand, this, registryAccess, dispatcher);
                if (command != null) {
                    dispatcher.register(command);
                    this.getLoadedCommands().add(customCommand.getCommand());
                }
            } else if ((cmd.getValue().getCommandMode() == CommandMode.COMMAND_REDIRECT || cmd.getValue().getCommandMode() == CommandMode.COMMAND_REDIRECT_NOARG) && cmd.getValue() instanceof RedirectCommand redirectCommand) {
                LiteralArgumentBuilder<S> command = new CommandRedirectBuilder<S>(cmd.getKey(), redirectCommand, this.commandType).buildCommand(dispatcher);
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
                .then(this.literal("scheduler").requires(Permissions.require("commandaliases.scheduler", 4))
                        .then(this.literal("match").requires(Permissions.require("commandaliases.scheduler.match", 4))
                                .then(this.argument("eventName", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String eventName = StringArgumentType.getString(context, "eventName");
                                            if (this.getScheduler().contains(eventName))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )
                        .then(this.literal("remove").requires(Permissions.require("commandaliases.scheduler.remove", 4))
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
                .then(this.literal("compare").requires(Permissions.require("commandaliases.compare", 4))
                        .then(this.literal("equals").requires(Permissions.require("commandaliases.compare.equals", 4))
                                .then(this.argument("string1", StringArgumentType.string())
                                        .then(this.argument("string2", StringArgumentType.string())
                                                .executes(context -> {
                                                    String string1 = StringArgumentType.getString(context, "string1");
                                                    String string2 = StringArgumentType.getString(context, "string2");
                                                    if (string1.equals(string2))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("notEquals").requires(Permissions.require("commandaliases.compare.not_equals", 4))
                                .then(this.argument("string1", StringArgumentType.string())
                                        .then(this.argument("string2", StringArgumentType.string())
                                                .executes(context -> {
                                                    String string1 = StringArgumentType.getString(context, "string1");
                                                    String string2 = StringArgumentType.getString(context, "string2");
                                                    if (!string1.equals(string2))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
                .then(this.literal("compute").requires(Permissions.require("commandaliases.compute", 4))
                        .then(this.literal("condition").requires(Permissions.require("commandaliases.compute.condition", 4))
                                .then(this.argument("expression", StringArgumentType.string())
                                        .executes(context -> {
                                            String expression = StringArgumentType.getString(context, "expression");
                                            boolean value = new SimpleBooleanEvaluator().evaluate(expression);
                                            if (value)
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )

                        // Todo: Comparison Evaluator
                        .then(this.literal("equals").requires(Permissions.require("commandaliases.compute.equals", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", StringArgumentType.string())
                                                .then(this.argument("value2", StringArgumentType.string())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = StringArgumentType.getString(context, "value1").equals(StringArgumentType.getString(context, "value2"));
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(this.literal("notEquals").requires(Permissions.require("commandaliases.compute.not_equals", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", StringArgumentType.string())
                                                .then(this.argument("value2", StringArgumentType.string())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = !StringArgumentType.getString(context, "value1").equals(StringArgumentType.getString(context, "value2"));
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(this.literal("moreThan").requires(Permissions.require("commandaliases.compute.more_than", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", DoubleArgumentType.doubleArg())
                                                .then(this.argument("value2", DoubleArgumentType.doubleArg())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = DoubleArgumentType.getDouble(context, "value1") > DoubleArgumentType.getDouble(context, "value2");
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(this.literal("lessThan").requires(Permissions.require("commandaliases.compute.less_than", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", DoubleArgumentType.doubleArg())
                                                .then(this.argument("value2", DoubleArgumentType.doubleArg())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = DoubleArgumentType.getDouble(context, "value1") < DoubleArgumentType.getDouble(context, "value2");
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(this.literal("moreThanEquals").requires(Permissions.require("commandaliases.compute.more_than_equals", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", DoubleArgumentType.doubleArg())
                                                .then(this.argument("value2", DoubleArgumentType.doubleArg())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = DoubleArgumentType.getDouble(context, "value1") >= DoubleArgumentType.getDouble(context, "value2");
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(this.literal("lessThanEquals").requires(Permissions.require("commandaliases.compute.less_than_equals", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("value1", DoubleArgumentType.doubleArg())
                                                .then(this.argument("value2", DoubleArgumentType.doubleArg())
                                                        .executes(context -> {
                                                            String key = StringArgumentType.getString(context, "key");
                                                            boolean value = DoubleArgumentType.getDouble(context, "value1") <= DoubleArgumentType.getDouble(context, "value2");
                                                            if (this.getDatabase().write(key, String.valueOf(value)))
                                                                return Command.SINGLE_SUCCESS;
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )

                        .then(this.literal("booleanEvaluate").requires(Permissions.require("commandaliases.compute.boolean_evaluate", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("expression", StringArgumentType.string())
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    String expression = StringArgumentType.getString(context, "expression");

                                                    boolean value = new SimpleBooleanEvaluator().evaluate(expression);

                                                    if (this.getDatabase().write(key, String.valueOf(value)))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("numericalEvaluate").requires(Permissions.require("commandaliases.compute.numerical_evaluate", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .then(this.argument("expression", StringArgumentType.string())
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    String expression = StringArgumentType.getString(context, "expression");
                                                    double value = new ExtendedDoubleEvaluator().evaluate(expression);
                                                    if (this.getDatabase().write(key, String.valueOf(value)))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
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
                                                    String key = StringArgumentType.getString(context, "key");
                                                    String value = StringArgumentType.getString(context, "value");
                                                    if (this.getDatabase().write(key, String.valueOf(value)))
                                                        return Command.SINGLE_SUCCESS;
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(this.literal("delete").requires(Permissions.require("commandaliases.database.delete", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            if (this.getDatabase().read(key) != null && this.getDatabase().delete(key))
                                                return Command.SINGLE_SUCCESS;
                                            return 0;
                                        })
                                )
                        )
                        .then(this.literal("match").requires(Permissions.require("commandaliases.database.match", 4))
                                .then(this.argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            String value = this.getDatabase().read(key);
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
                                            String key = StringArgumentType.getString(context, "key");
                                            String value = this.getDatabase().read(key);
                                            if (value != null) {
                                                this.sendFeedback(context.getSource(), Text.literal(value));
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

    protected abstract LiteralArgumentBuilder<S> buildCustomCommand(String filePath, CustomCommand customCommand, AbstractCommandAliasesProvider<S> abstractCommandAliasesProvider, CommandRegistryAccess registryAccess, CommandDispatcher<S> dispatcher);

    /**
     * Loads command aliases file, meant for integrated/dedicated servers.
     */
    protected void loadCommandAliases() {
        this.commands.clear();
        this.commands.putAll(this.loadCommandAliasesFromDirectory(this.commandsDirectory.toFile()));
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
    private @NotNull Map<String, CommandAlias> loadCommandAliasesFromDirectory(File file) {
        Map<String, CommandAlias> commandAliases = new Object2ObjectOpenHashMap<>();

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

    public String loadAndRenderDirectoryTree(TreeNode<File> tree, Map<String, CommandAlias> commandAliases) {
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

    private Map<String, CommandAlias> objectMapDataFormat(ObjectMapper objectMapper, File file, AtomicReference<String> state) throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, CommandAlias> commandAliases = new Object2ObjectOpenHashMap<>();
        CommandAlias commandAlias = objectMapper.readerFor(CommandAlias.class).readValue(file);
        if (commandAlias.getSchemaVersion() == 1) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            commandAliases.put(file.getAbsolutePath(), objectMapper.readerFor(this.getCommandModeClass(commandAlias.getCommandMode())).readValue(file));
            state.set(" - Successfully loaded");
        } else {
            state.set(" - Unsupported schema version");
        }
        return commandAliases;
    }

    public List<StringBuilder> loadAndRenderDirectoryTreeLines(TreeNode<File> tree, Map<String, CommandAlias> commandAliases) {
        List<StringBuilder> result = new ArrayList<>();
        AtomicReference<String> state = new AtomicReference<>("");
        if (tree.getData().isFile()) {
            File file = tree.getData();
            try {
                if (file.getAbsolutePath().endsWith(".toml")) {
                    commandAliases.putAll(this.objectMapDataFormat(this.tomlMapper, file, state));
                } else if (file.getAbsolutePath().endsWith(".json")) {
                    commandAliases.putAll(this.objectMapDataFormat(this.jsonMapper, file, state));
                } else if (file.getAbsolutePath().endsWith(".json5")) {
                    commandAliases.putAll(this.objectMapDataFormat(this.json5Mapper, file, state));
                } else if (file.getAbsolutePath().endsWith(".yml") || file.getAbsolutePath().endsWith(".yaml")) {
                    commandAliases.putAll(this.objectMapDataFormat(this.yamlMapper, file, state));
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

    public Map<String, CommandAlias> getCommands() {
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

    public AbstractDatabase<String, String> getDatabase() {
        return database;
    }

    public void setDatabase(AbstractDatabase<String, String> database) {
        this.database = database;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
