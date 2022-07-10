package me.flashyreese.mods.commandaliases;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandMode;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.command.builder.custom.format.CustomCommand;
import me.flashyreese.mods.commandaliases.command.builder.reassign.format.ReassignCommand;
import me.flashyreese.mods.commandaliases.command.builder.redirect.format.RedirectCommand;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import me.flashyreese.mods.commandaliases.util.Atomic;
import me.flashyreese.mods.commandaliases.util.TreeNode;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents the command aliases provider.
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.9.0
 */
public class CommandAliasesProvider {
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
    private AbstractDatabase<byte[], byte[]> database;
    private Scheduler scheduler;

    public CommandAliasesProvider(Path commandsDirectory, Field literalCommandNodeLiteralField) {
        this.commandsDirectory = commandsDirectory;
        this.literalCommandNodeLiteralField = literalCommandNodeLiteralField;
    }

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
    protected <S extends CommandSource> void unregisterCommands(CommandDispatcher<S> dispatcher) {
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
