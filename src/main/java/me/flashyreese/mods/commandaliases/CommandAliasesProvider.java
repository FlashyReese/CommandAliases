package me.flashyreese.mods.commandaliases;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.Scheduler;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the command aliases provider.
 *
 * @author FlashyReese
 * @version 0.9.0
 * @since 0.9.0
 */
public class CommandAliasesProvider {
    private final Gson gson = new Gson();
    private final JsonMapper jsonMapper = new JsonMapper(JsonFactory.builder().enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA).enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS).enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
            //.enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS).enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS) todo: enable for jackson 2.14
            .build());
    private final TomlMapper tomlMapper = new TomlMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();

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
        this.commands.addAll(this.loadCommandAliases(new File(this.commandsDirectory.toFile().getAbsolutePath() + ".json")));
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
            for (File file1 : Objects.requireNonNull(file.listFiles())) {
                if (file1.isFile()) {
                    try (FileReader reader = new FileReader(file1)) {
                        if (file1.getAbsolutePath().endsWith(".toml")) {
                            commandAliases.add(this.tomlMapper.readerFor(CommandAlias.class).readValue(reader));
                        } else if (file1.getAbsolutePath().endsWith(".json")) {
                            commandAliases.add(this.gson.fromJson(reader, CommandAlias.class));
                        } else if (file1.getAbsolutePath().endsWith(".json5")) {
                            commandAliases.add(this.jsonMapper.readerFor(CommandAlias.class).readValue(reader));
                            CommandAliasesMod.logger().warn("JSON5 isn't fully supported!");
                        } else if (file1.getAbsolutePath().endsWith(".yaml")) {
                            commandAliases.add(this.yamlMapper.readerFor(CommandAlias.class).readValue(reader));
                        } else {
                            CommandAliasesMod.logger().error("Unsupported data format type \"{}\"", file1.getName());
                            continue;
                        }
                        CommandAliasesMod.logger().info("Successfully loaded \"{}\"", file1.getName());
                    } catch (IOException e) {
                        CommandAliasesMod.logger().error("Could not read file at \"{}\" throws {}", file1.getAbsolutePath(), e);
                    }
                }
            }
        } else {
            if (file.mkdir()) {
                CommandAliasesMod.logger().info("Command Aliases directory created at \"{}\"", file.getAbsolutePath());
            } else {
                CommandAliasesMod.logger().error("Could not create directory for Command Aliases at \"{}\"", file.getAbsolutePath());
            }
        }

        return commandAliases;
    }

    /**
     * Reads JSON file and serializes them to a List of CommandAliases
     *
     * @param file JSON file path
     * @return List of CommandAliases
     */
    @Deprecated
    private List<CommandAlias> loadCommandAliases(File file) {
        List<CommandAlias> commandAliases = new ObjectArrayList<>();

        if (file.exists() && file.isFile()) {
            try (FileReader reader = new FileReader(file)) {
                commandAliases = gson.fromJson(reader, new TypeToken<List<CommandAlias>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse CommandAliases File", e);
            }
            CommandAliasesMod.logger().warn("The command mode \"{}\" is now deprecated and scheduled to remove on version 1.0.0", file.getName());
            CommandAliasesMod.logger().warn("Please migrate to directory of command aliases. :)");
        }

        return commandAliases;
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
