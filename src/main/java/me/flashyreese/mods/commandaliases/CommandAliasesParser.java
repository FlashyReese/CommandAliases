package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.mods.commandaliases.command.CommandAlias;
import me.flashyreese.mods.commandaliases.command.CommandType;
import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;

public class CommandAliasesParser {

    private Map<String, ArgumentType<?>> argumentMap = new HashMap<>();

    public CommandAliasesParser() {
        //fixme: Currently all strings need regex
        this.argumentMap.put("word", StringArgumentType.word());
        this.argumentMap.put("string", StringArgumentType.string());
        this.argumentMap.put("greedy_string", StringArgumentType.greedyString());

        //this.argumentMap.put("entity", EntityArgumentType.class, new EntityArgumentType.Serializer(());
        this.argumentMap.put("entity", EntityArgumentType.entity());
        this.argumentMap.put("entities", EntityArgumentType.entities());
        this.argumentMap.put("player", EntityArgumentType.player());
        this.argumentMap.put("players", EntityArgumentType.players());

        //this.argumentMap.put("score_holder", ScoreHolderArgumentType.class, new net.minecraft.command.argument.ScoreHolderArgumentType.Serializer(());
        this.argumentMap.put("score_holder", ScoreHolderArgumentType.scoreHolder());
        this.argumentMap.put("score_holders", ScoreHolderArgumentType.scoreHolder());

        this.argumentMap.put("game_profile", GameProfileArgumentType.gameProfile());
        this.argumentMap.put("block_pos", BlockPosArgumentType.blockPos());
        this.argumentMap.put("column_pos", ColumnPosArgumentType.columnPos());
        this.argumentMap.put("vec3", Vec3ArgumentType.vec3());
        this.argumentMap.put("vec2", Vec2ArgumentType.vec2());
        this.argumentMap.put("block_state", BlockStateArgumentType.blockState());
        this.argumentMap.put("block_predicate", BlockPredicateArgumentType.blockPredicate());
        this.argumentMap.put("item_stack", ItemStackArgumentType.itemStack());
        this.argumentMap.put("item_predicate", ItemPredicateArgumentType.itemPredicate());
        this.argumentMap.put("color", ColorArgumentType.color());
        this.argumentMap.put("component", TextArgumentType.text());
        this.argumentMap.put("message", MessageArgumentType.message());
        this.argumentMap.put("nbt_compound_tag", NbtCompoundTagArgumentType.nbtCompound());
        this.argumentMap.put("nbt_tag", NbtTagArgumentType.nbtTag());
        this.argumentMap.put("nbt_path", NbtPathArgumentType.nbtPath());
        this.argumentMap.put("objective", ObjectiveArgumentType.objective());
        this.argumentMap.put("objective_criteria", ObjectiveCriteriaArgumentType.objectiveCriteria());
        this.argumentMap.put("operation", OperationArgumentType.operation());
        this.argumentMap.put("particle", ParticleArgumentType.particle());
        this.argumentMap.put("angle", AngleArgumentType.angle());
        this.argumentMap.put("rotation", RotationArgumentType.rotation());
        this.argumentMap.put("scoreboard_slot", ScoreboardSlotArgumentType.scoreboardSlot());
        this.argumentMap.put("swizzle", SwizzleArgumentType.swizzle());
        this.argumentMap.put("team", TeamArgumentType.team());
        this.argumentMap.put("item_slot", ItemSlotArgumentType.itemSlot());
        this.argumentMap.put("resource_location", IdentifierArgumentType.identifier());
        this.argumentMap.put("mob_effect", MobEffectArgumentType.mobEffect());
        this.argumentMap.put("function", FunctionArgumentType.function());
        this.argumentMap.put("entity_anchor", EntityAnchorArgumentType.entityAnchor());
        this.argumentMap.put("int_range", NumberRangeArgumentType.numberRange());
        this.argumentMap.put("float_range", NumberRangeArgumentType.method_30918());
        this.argumentMap.put("item_enchantment", ItemEnchantmentArgumentType.itemEnchantment());
        this.argumentMap.put("entity_summon", EntitySummonArgumentType.entitySummon());
        this.argumentMap.put("dimension", DimensionArgumentType.dimension());
        this.argumentMap.put("time", TimeArgumentType.time());
        this.argumentMap.put("uuid", UuidArgumentType.uuid());

        //Fixme: Ranges for all except boolean and string
        this.argumentMap.put("brigadier:bool", BoolArgumentType.bool());
        this.argumentMap.put("brigadier:float", FloatArgumentType.floatArg());
        this.argumentMap.put("brigadier:double", DoubleArgumentType.doubleArg());
        this.argumentMap.put("brigadier:integer", IntegerArgumentType.integer());
        this.argumentMap.put("brigadier:long", LongArgumentType.longArg());
        this.argumentMap.put("brigadier:string", StringArgumentType.string());
    }

    public String parse(CommandContext<ServerCommandSource> context, String cmd, String subCmd) {
        String newCmd = subCmd;
        try {
            String playerName = context.getSource().getPlayer().getEntityName();
            newCmd = subCmd.replaceAll("\\{this::SELF}", playerName);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        newCmd = formatCommand(getInputMap(cmd, context), newCmd);

        return newCmd;
    }

    private List<String> getArgumentsFromString(String cmd) {
        List<String> args = new ArrayList<>();
        if (!(cmd.contains("{") && cmd.contains("}")))
            return args;
        boolean log = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cmd.length(); i++) {
            if (cmd.charAt(i) == '{') {
                log = true;
                stringBuilder.append(cmd.charAt(i));
            } else if (cmd.charAt(i) == '}') {
                log = false;
                stringBuilder.append(cmd.charAt(i));
                args.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            } else if (log) {
                stringBuilder.append(cmd.charAt(i));
            }
        }
        return args;
    }

    public Map<String, String> getInputMap(String cmd, CommandContext<ServerCommandSource> context) {
        String input = context.getInput();
        input = input.substring(input.indexOf(" ") + 1);
        Map<String, String> map = new HashMap<>();
        List<String> args = getArgumentsFromString(cmd);
        List<String> inputArgs = Arrays.asList(input.split(" "));
        if (args.size() != inputArgs.size()) {
            CommandAliasesMod.getLogger().info("well time to actual regex or find a suitable replacement");
            return map;
        }
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            String inputArg = inputArgs.get(i);
            arg = "{" + arg.split("#")[1].split("}")[0] + "}";
            map.put(arg, inputArg);
        }

        return map;
    }

    public String formatCommand(Map<String, String> map, String cmd) {
        String command = cmd;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (command.contains(entry.getKey())) {
                command = command.replaceAll("\\{" + entry.getKey().substring(1), entry.getValue());
            }
        }
        return command;
    }

    LiteralArgumentBuilder<ServerCommandSource> parseCommand(String command){
        if (command.contains(" ")) {
            return CommandManager.literal(command.split(" ")[0]);
        }
        return CommandManager.literal(command);
    }

    public ArgumentBuilder<ServerCommandSource, ?> parseArguments(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> args = getArgumentsFromString(cmd.getCommand());
        ArgumentBuilder<ServerCommandSource, ?> arguments = null;
        Collections.reverse(args);
        for (String arg : args) {
            if (arg.startsWith("{arg::")) {
                String argType = arg.split("\\{arg::")[1].split("#")[0];
                String variable = arg.split("#")[1].split("}")[0];
                if (argumentMap.containsKey(argType)) {
                    if (arguments != null){
                        arguments = CommandManager.argument(variable, argumentMap.get(argType)).then(arguments);
                    }else{
                        arguments = CommandManager.argument(variable, argumentMap.get(argType)).executes(context ->  executeCommandAliases(cmd, dispatcher, context));
                    }
                }
            }
        }
        return arguments;
    }

    public int executeCommandAliases(CommandAlias cmd, CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int execute = 0;
        for (CommandAlias subCmd : cmd.getExecution()) {
            String subCommand = parse(context, cmd.getCommand(), subCmd.getCommand());
            if (subCmd.getType() == CommandType.CLIENT) {
                execute = dispatcher.execute(subCommand, context.getSource());
            } else if (subCmd.getType() == CommandType.SERVER) {
                execute = dispatcher.execute(subCommand, context.getSource().getMinecraftServer().getCommandSource());
            }
            if (subCmd.getMessage() != null) {
                context.getSource().sendFeedback(new LiteralText(subCmd.getMessage()), true);
            }
        }
        if (cmd.getMessage() != null) {
            context.getSource().sendFeedback(new LiteralText(cmd.getMessage()), true);
        }
        return execute;
    }
}