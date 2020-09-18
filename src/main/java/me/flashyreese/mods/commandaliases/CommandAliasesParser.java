package me.flashyreese.mods.commandaliases;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.Map;

public class CommandAliasesParser {

    private Map<String, ArgumentType<?>> argumentMap = new HashMap<>();

    public CommandAliasesParser() {
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
    }

    public String parse(CommandContext<ServerCommandSource> context, String subCmd) {
        String newCmd = subCmd;
        try {
            String playerName = context.getSource().getPlayer().getEntityName();
            newCmd = subCmd.replaceAll("\\{this::SELF}", playerName);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return newCmd;
    }
}
