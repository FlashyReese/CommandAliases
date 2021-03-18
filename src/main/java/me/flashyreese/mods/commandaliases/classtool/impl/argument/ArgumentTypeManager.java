/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool.impl.argument;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.command.builder.alias.AliasHolder;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the Argument Type Manager Class Tool
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.0.9
 * <p>
 * This ArgumentTypeManager maps ArgumentTypes to String.
 * See https://minecraft.gamepedia.com/Argument_types
 * There is probably a vanilla way to do this but I can't figure it out
 */
public class ArgumentTypeManager implements ClassTool<ArgumentType<?>> {

    private final Map<String, ArgumentType<?>> argumentMap = new Object2ObjectOpenHashMap<>();

    private Field commandContextArgumentsField = null;

    public ArgumentTypeManager() {
        this.registerArgumentTypes();
        try {
            this.commandContextArgumentsField = CommandContext.class.getDeclaredField("arguments");
            this.commandContextArgumentsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void registerArgumentTypes() {
        this.argumentMap.put("minecraft:word", StringArgumentType.word());
        this.argumentMap.put("minecraft:string", StringArgumentType.string());
        this.argumentMap.put("minecraft:greedy_string", StringArgumentType.greedyString());

        this.argumentMap.put("minecraft:entity", EntityArgumentType.entity());
        this.argumentMap.put("minecraft:entities", EntityArgumentType.entities());
        this.argumentMap.put("minecraft:player", EntityArgumentType.player());
        this.argumentMap.put("minecraft:players", EntityArgumentType.players());

        this.argumentMap.put("minecraft:score_holder", ScoreHolderArgumentType.scoreHolder());
        this.argumentMap.put("minecraft:score_holders", ScoreHolderArgumentType.scoreHolders());

        this.argumentMap.put("minecraft:game_profile", GameProfileArgumentType.gameProfile());
        this.argumentMap.put("minecraft:block_pos", BlockPosArgumentType.blockPos());
        this.argumentMap.put("minecraft:column_pos", ColumnPosArgumentType.columnPos());
        this.argumentMap.put("minecraft:vec3", Vec3ArgumentType.vec3());
        this.argumentMap.put("minecraft:vec2", Vec2ArgumentType.vec2());
        this.argumentMap.put("minecraft:block_state", BlockStateArgumentType.blockState());
        this.argumentMap.put("minecraft:block_predicate", BlockPredicateArgumentType.blockPredicate());
        this.argumentMap.put("minecraft:item_stack", ItemStackArgumentType.itemStack());
        this.argumentMap.put("minecraft:item_predicate", ItemPredicateArgumentType.itemPredicate());
        this.argumentMap.put("minecraft:color", ColorArgumentType.color());
        this.argumentMap.put("minecraft:component", TextArgumentType.text());
        this.argumentMap.put("minecraft:message", MessageArgumentType.message());
        this.argumentMap.put("minecraft:nbt_compound_tag", NbtCompoundTagArgumentType.nbtCompound());
        this.argumentMap.put("minecraft:nbt_tag", NbtTagArgumentType.nbtTag());
        this.argumentMap.put("minecraft:nbt_path", NbtPathArgumentType.nbtPath());
        this.argumentMap.put("minecraft:objective", ObjectiveArgumentType.objective());
        this.argumentMap.put("minecraft:objective_criteria", ObjectiveCriteriaArgumentType.objectiveCriteria());
        this.argumentMap.put("minecraft:operation", OperationArgumentType.operation());
        this.argumentMap.put("minecraft:particle", ParticleArgumentType.particle());
        this.argumentMap.put("minecraft:angle", AngleArgumentType.angle());
        this.argumentMap.put("minecraft:rotation", RotationArgumentType.rotation());
        this.argumentMap.put("minecraft:scoreboard_slot", ScoreboardSlotArgumentType.scoreboardSlot());
        this.argumentMap.put("minecraft:swizzle", SwizzleArgumentType.swizzle());
        this.argumentMap.put("minecraft:team", TeamArgumentType.team());
        this.argumentMap.put("minecraft:item_slot", ItemSlotArgumentType.itemSlot());
        this.argumentMap.put("minecraft:resource_location", IdentifierArgumentType.identifier());
        this.argumentMap.put("minecraft:mob_effect", MobEffectArgumentType.mobEffect());
        this.argumentMap.put("minecraft:function", FunctionArgumentType.function());
        this.argumentMap.put("minecraft:entity_anchor", EntityAnchorArgumentType.entityAnchor());
        this.argumentMap.put("minecraft:int_range", NumberRangeArgumentType.numberRange());
        this.argumentMap.put("minecraft:float_range", NumberRangeArgumentType.method_30918());
        this.argumentMap.put("minecraft:item_enchantment", ItemEnchantmentArgumentType.itemEnchantment());
        this.argumentMap.put("minecraft:entity_summon", EntitySummonArgumentType.entitySummon());
        this.argumentMap.put("minecraft:dimension", DimensionArgumentType.dimension());
        this.argumentMap.put("minecraft:time", TimeArgumentType.time());
        this.argumentMap.put("minecraft:uuid", UuidArgumentType.uuid());

        this.argumentMap.put("brigadier:bool", BoolArgumentType.bool());
        this.argumentMap.put("brigadier:float", FloatArgumentType.floatArg());
        this.argumentMap.put("brigadier:double", DoubleArgumentType.doubleArg());
        this.argumentMap.put("brigadier:integer", IntegerArgumentType.integer());
        this.argumentMap.put("brigadier:long", LongArgumentType.longArg());
        this.argumentMap.put("brigadier:string", StringArgumentType.string());
    }

    @Override
    public String getName() {
        return "arg";
    }

    @Override
    public boolean contains(String key) {
        return this.argumentMap.containsKey(key);
    }

    @Override
    public ArgumentType<?> getValue(String key) {
        return this.argumentMap.get(key);
    }

    @Override
    public String getValue(CommandContext<ServerCommandSource> context, AliasHolder holder) {
        return this.getInputString(context, holder.getVariableName());
    }

    public <S> String getInputString(CommandContext<S> commandContext, String name) {
        Map<String, ParsedArgument<S, ?>> map = this.getArguments(commandContext);
        if (map != null) {
            ParsedArgument<S, ?> parsedArgument = map.get(name);

            if (parsedArgument != null) {
                StringRange stringRange = parsedArgument.getRange();
                return commandContext.getInput().substring(stringRange.getStart(), stringRange.getEnd());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private  <S> Map<String, ParsedArgument<S, ?>> getArguments(CommandContext<S> commandContext) {
        if (this.commandContextArgumentsField == null) return null;
        try {
            return (Map<String, ParsedArgument<S, ?>>) this.commandContextArgumentsField.get(commandContext);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
