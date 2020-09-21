package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.classtool.MinecraftClassTool;
import me.flashyreese.mods.commandaliases.classtool.argument.ArgumentTypeManager;
import me.flashyreese.mods.commandaliases.classtool.argument.CommandAliasesArgumentType;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandAliasesBuilder {

    private static final Pattern REQUIRED_COMMAND_ALIAS_HOLDER = Pattern.compile("\\{(?<classTool>\\w+)(::)?(?<method>\\w+)?#?(?<variableName>\\w+)?@?(?<formattingType>\\w+)?}");

    private CommandAlias command;
    private List<String> holders = new ArrayList<>();
    private List<CommandAliasesHolder> commandAliasesHolders = new ArrayList<>();

    private Map<String, ClassTool<?>> classToolMap = new HashMap<>();
    //private ArgumentTypeManager argumentTypeManager;
    //private MinecraftClassTool minecraftClassTool;
    private FormattingTypeMap formattingTypeMap;

    public CommandAliasesBuilder(CommandAlias command) {
        this.command = command;
        this.holders.addAll(this.findHolders(command.getCommand()));
        this.commandAliasesHolders.addAll(this.buildHolders(this.holders));

        this.classToolMap.put("arg", new ArgumentTypeManager());
        this.classToolMap.put("this", new MinecraftClassTool());
        //this.argumentTypeManager = new ArgumentTypeManager();
        //this.minecraftClassTool = new MinecraftClassTool();
        this.formattingTypeMap = new FormattingTypeMap();
    }

    private List<String> findHolders(String command) {
        List<String> holders = new ArrayList<>();
        Matcher m = REQUIRED_COMMAND_ALIAS_HOLDER.matcher(command);
        while (m.find()) {
            holders.add(m.group());
        }
        return holders;
    }

    private List<CommandAliasesHolder> buildHolders(List<String> holders) {
        List<CommandAliasesHolder> commandAliasesHolders = new ArrayList<>();
        holders.stream().forEach(holder -> {
            commandAliasesHolders.add(new CommandAliasesHolder(holder));
        });
        return commandAliasesHolders;
    }


    private Map<String, String> getHolderInputMap(CommandContext<ServerCommandSource> context) {
        Map<String, String> inputMap = new HashMap<>();

        for (CommandAliasesHolder holder : this.commandAliasesHolders) {
            /*String key = "{" + holder.getVariableName() + "}";
            String value = null;

            if (holder.getClassTool().equals(this.minecraftClassTool.getName())){
                if (this.minecraftClassTool.contains(holder.getMethod())){
                    value = this.minecraftClassTool.getValue(holder.getMethod()).apply(context);
                }
            } else if (holder.getClassTool().equals(this.argumentTypeManager.getName())){
                if (this.argumentTypeManager.contains(holder.getMethod())){
                    value = this.argumentTypeManager.getValue(holder.getMethod()).getBiFunction().apply(context, holder.getVariableName());
                }
            }
            if (holder.getFormattingType() != null){
                if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())){
                    inputMap.put(key, this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                }else{
                    CommandAliasesMod.getLogger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                    inputMap.put(key, value);
                }
            }else{
                inputMap.put(key, value);
            }*/

            if (this.classToolMap.containsKey(holder.getClassTool())) {
                if (this.classToolMap.get(holder.getClassTool()).contains(holder.getMethod())) {
                    String key = "{" + holder.getVariableName() + "}";
                    String value = null;

                    //Fixme: ClassToolMap casting
                    Object tool = this.classToolMap.get(holder.getClassTool()).getValue(holder.getMethod());
                    if (tool instanceof CommandAliasesArgumentType) {
                        value = ((CommandAliasesArgumentType) tool).getBiFunction().apply(context, holder.getVariableName());
                    }


                    if (holder.getFormattingType() != null) {
                        if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                            inputMap.put(key, this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                        } else {
                            CommandAliasesMod.getLogger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                            inputMap.put(key, value);
                        }
                    } else {
                        inputMap.put(key, value);
                    }
                } else {
                    CommandAliasesMod.getLogger().error("No method found for \"{}\"", holder.getHolder());
                }
            } else {
                CommandAliasesMod.getLogger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }

        return inputMap;
    }

    private String formatSubCommandOrMessage(CommandContext<ServerCommandSource> context, String text) {
        Map<String, String> inputMap = this.getHolderInputMap(context);
        String formattedText = text;

        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            if (formattedText.contains(entry.getKey())) {
                formattedText = formattedText.replace(entry.getKey(), entry.getValue());
            }
        }

        Map<String, String> newInputMap = new HashMap<>();
        List<CommandAliasesHolder> textHolders = this.buildHolders(this.findHolders(formattedText));
        for (CommandAliasesHolder holder : textHolders) {//Fixme: divide classtools type
            if (holder.getClassTool().contains("this")) {
                MinecraftClassTool tool = ((MinecraftClassTool) this.classToolMap.get("this"));

                String value = tool.getValue(holder.getMethod()).apply(context);
                if (holder.getFormattingType() != null) {
                    if (this.formattingTypeMap.getFormatTypeMap().containsKey(holder.getFormattingType())) {
                        newInputMap.put(holder.getHolder(), this.formattingTypeMap.getFormatTypeMap().get(holder.getFormattingType()).apply(value));
                    } else {
                        CommandAliasesMod.getLogger().warn("No formatting type found for \"{}\", skipping formatting", holder.getHolder());
                        newInputMap.put(holder.getHolder(), value);
                    }
                } else {
                    newInputMap.put(holder.getHolder(), value);
                }
            }
        }
        for (Map.Entry<String, String> entry : newInputMap.entrySet()) {
            if (formattedText.contains(entry.getKey())) {
                formattedText = formattedText.replace(entry.getKey(), entry.getValue());
            }
        }

        return formattedText;
    }


    private int executeCommandAliases(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int execute = 0;
        for (CommandAlias subCmd : cmd.getExecution()) {
            String subCommand = formatSubCommandOrMessage(context, subCmd.getCommand());
            if (subCmd.getType() == CommandType.CLIENT) {
                execute = dispatcher.execute(subCommand, context.getSource());
            } else if (subCmd.getType() == CommandType.SERVER) {
                execute = dispatcher.execute(subCommand, context.getSource().getMinecraftServer().getCommandSource());
            }
            if (subCmd.getMessage() != null) {
                String message = formatSubCommandOrMessage(context, subCmd.getMessage());
                context.getSource().sendFeedback(new LiteralText(message), true);
            }
            if (subCmd.getSleep() != null) {
                String formattedTime = subCmd.getSleep();
                int time = Integer.parseInt(formattedTime);
                //Todo: Sleep
            }
        }
        if (cmd.getMessage() != null) {
            String message = formatSubCommandOrMessage(context, cmd.getMessage());
            context.getSource().sendFeedback(new LiteralText(message), true);
        }
        return execute;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = parseCommand(this.command);
        ArgumentBuilder<ServerCommandSource, ?> arguments = parseArguments(this.command, dispatcher);
        if (arguments != null) {
            command = command.then(arguments);
        } else {
            command = command.executes(context -> executeCommandAliases(this.command, dispatcher, context));
        }
        return command;
    }

    private LiteralArgumentBuilder<ServerCommandSource> parseCommand(CommandAlias command) { //Fixme: {arg} spacing instead of " "
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.getCommand());
        if (command.getCommand().contains(" ")) {
            commandBuilder = CommandManager.literal(command.getCommand().split(" ")[0]);
        }
        //commandBuilder = commandBuilder.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(command.getPermissionLevel()));
        return commandBuilder;
    }

    private ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) { // Todo: Optional Arguments
        List<CommandAliasesHolder> commandHolders = this.buildHolders(findHolders(cmd.getCommand()));
        ArgumentBuilder<ServerCommandSource, ?> arguments = null;
        Collections.reverse(commandHolders);
        for (CommandAliasesHolder holder : commandHolders) {
            if (this.classToolMap.containsKey(holder.getClassTool())) {//Fixme: Casting dangerous
                ClassTool<?> tool = this.classToolMap.get(holder.getClassTool());
                if (tool instanceof ArgumentTypeManager) {
                    if (tool.contains(holder.getMethod())) {
                        if (arguments != null) {
                            arguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeManager) tool).getValue(holder.getMethod()).getArgumentType()).then(arguments);
                        } else {
                            arguments = CommandManager.argument(holder.getVariableName(), ((ArgumentTypeManager) tool).getValue(holder.getMethod()).getArgumentType()).executes(context -> executeCommandAliases(cmd, dispatcher, context));
                        }
                    }
                }
            } else {
                CommandAliasesMod.getLogger().error("No class tool found for \"{}\"", holder.getHolder());
            }
        }
        return arguments;
    }

    public static class CommandAliasesHolder {
        private String holder;

        private String classTool;
        private String method;
        private String variableName;
        private String formattingType;

        public CommandAliasesHolder(String holder) {
            this.holder = holder;
            this.findVariables();
        }

        private void findVariables() {
            Matcher matcher = CommandAliasesBuilder.REQUIRED_COMMAND_ALIAS_HOLDER.matcher(this.holder);
            if (matcher.matches()) {
                String classTool = matcher.group("classTool");
                String method = matcher.group("method");
                String variableName = matcher.group("variableName");
                String formattingType = matcher.group("formattingType");

                this.updateVariables(classTool, method, variableName, formattingType);
                //System.out.println(String.format("Command: %s ClassTool: %s Method: %s VariableName: %s FormattingType: %s", this.holder, this.classTool, this.method, this.variableName, this.formattingType));
            } else {
                CommandAliasesMod.getLogger().error("Invalid Command Aliases Holder: {}", this.holder);
            }
        }

        private void updateVariables(String classTool, String method, String variableName, String formattingType) {
            String cT = classTool;
            String vN = variableName;

            if (method == null && vN == null) {
                vN = cT;
                cT = null;
            }

            this.classTool = cT;
            this.method = method;
            this.variableName = vN;
            this.formattingType = formattingType;
        }

        public String toString() {
            return this.holder;
        }

        public String getHolder() {
            return this.holder;
        }

        public String getClassTool() {
            return this.classTool;
        }

        public String getMethod() {
            return this.method;
        }

        public String getVariableName() {
            return this.variableName;
        }

        public String getFormattingType() {
            return this.formattingType;
        }
    }
}
