package me.flashyreese.mods.commandaliases.command.impl;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.*;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.RegistryKeys;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents the Argument Type Mapper
 *
 * @author FlashyReese
 * @version 1.0.0
 * @since 0.0.9
 * <p>
 * This ArgumentTypeMapper maps ArgumentTypes to String.
 * See <a href="https://minecraft.gamepedia.com/Argument_types">...</a>
 */
public class ArgumentTypeMapper { // Todo: Singleton instance - Map registry via mixin maybe

    private final Map<String, ArgumentType<?>> argumentMap = new Object2ObjectOpenHashMap<>();

    private Field commandContextArgumentsField = null;

    public ArgumentTypeMapper(CommandRegistryAccess registryAccess) {
        this.registerArgumentTypes(registryAccess);
        try {
            this.commandContextArgumentsField = CommandContext.class.getDeclaredField("arguments");
            this.commandContextArgumentsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void registerArgumentTypes(CommandRegistryAccess registryAccess) {
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
        this.argumentMap.put("minecraft:block_pos", BlockPosArgumentType.blockPos()); // fixme: Relative pos - we should parse this them convert to string, should we fix this? or should we not? because pain
        this.argumentMap.put("minecraft:column_pos", ColumnPosArgumentType.columnPos()); //
        this.argumentMap.put("minecraft:vec3", Vec3ArgumentType.vec3()); //
        this.argumentMap.put("minecraft:vec2", Vec2ArgumentType.vec2()); //
        this.argumentMap.put("minecraft:block_state", BlockStateArgumentType.blockState(registryAccess));
        this.argumentMap.put("minecraft:block_predicate", BlockPredicateArgumentType.blockPredicate(registryAccess));
        this.argumentMap.put("minecraft:item_stack", ItemStackArgumentType.itemStack(registryAccess));
        this.argumentMap.put("minecraft:item_predicate", ItemPredicateArgumentType.itemPredicate(registryAccess));
        this.argumentMap.put("minecraft:color", ColorArgumentType.color());
        this.argumentMap.put("minecraft:component", TextArgumentType.text());
        this.argumentMap.put("minecraft:message", MessageArgumentType.message());
        this.argumentMap.put("minecraft:nbt_compound_tag", NbtCompoundArgumentType.nbtCompound());
        this.argumentMap.put("minecraft:nbt_tag", NbtElementArgumentType.nbtElement());
        this.argumentMap.put("minecraft:nbt_path", NbtPathArgumentType.nbtPath());
        this.argumentMap.put("minecraft:objective", ScoreboardObjectiveArgumentType.scoreboardObjective());
        this.argumentMap.put("minecraft:objective_criteria", ScoreboardCriterionArgumentType.scoreboardCriterion());
        this.argumentMap.put("minecraft:operation", OperationArgumentType.operation());
        this.argumentMap.put("minecraft:particle", ParticleEffectArgumentType.particleEffect(registryAccess));
        this.argumentMap.put("minecraft:angle", AngleArgumentType.angle()); //
        this.argumentMap.put("minecraft:rotation", RotationArgumentType.rotation()); //
        this.argumentMap.put("minecraft:scoreboard_slot", ScoreboardSlotArgumentType.scoreboardSlot());
        this.argumentMap.put("minecraft:swizzle", SwizzleArgumentType.swizzle());
        this.argumentMap.put("minecraft:team", TeamArgumentType.team());
        this.argumentMap.put("minecraft:item_slot", ItemSlotArgumentType.itemSlot());
        this.argumentMap.put("minecraft:resource_location", IdentifierArgumentType.identifier());
        this.argumentMap.put("minecraft:function", CommandFunctionArgumentType.commandFunction());
        this.argumentMap.put("minecraft:entity_anchor", EntityAnchorArgumentType.entityAnchor());
        this.argumentMap.put("minecraft:int_range", NumberRangeArgumentType.intRange()); // todo: range
        this.argumentMap.put("minecraft:float_range", NumberRangeArgumentType.floatRange()); // todo :rage
        this.argumentMap.put("minecraft:dimension", DimensionArgumentType.dimension());
        this.argumentMap.put("minecraft:gamemode", GameModeArgumentType.gameMode());
        this.argumentMap.put("minecraft:time", TimeArgumentType.time());

        // Todo: Allow entire registry keys by creating registry map
        this.argumentMap.put("minecraft:entry.attribute_key", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE));
        this.argumentMap.put("minecraft:entry.status_effect_key", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT));
        this.argumentMap.put("minecraft:entry.enchantment_type", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT));
        this.argumentMap.put("minecraft:entry.biome_key", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BIOME));
        this.argumentMap.put("minecraft:entry.entity_type_key", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE));

        this.argumentMap.put("minecraft:entry_predicate.biome_key", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.BIOME));
        this.argumentMap.put("minecraft:entry_predicate.poi_type_key", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.POINT_OF_INTEREST_TYPE));

        this.argumentMap.put("minecraft:predicate.structure_key", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.STRUCTURE));

        this.argumentMap.put("minecraft:key.configured_feature_key", RegistryKeyArgumentType.registryKey(RegistryKeys.CONFIGURED_FEATURE));
        this.argumentMap.put("minecraft:key.template_pool_key", RegistryKeyArgumentType.registryKey(RegistryKeys.TEMPLATE_POOL));
        this.argumentMap.put("minecraft:key.structure_key", RegistryKeyArgumentType.registryKey(RegistryKeys.STRUCTURE));
        // end

        this.argumentMap.put("minecraft:template_mirror", BlockMirrorArgumentType.blockMirror());
        this.argumentMap.put("minecraft:template_rotation", BlockRotationArgumentType.blockRotation());

        if (SharedConstants.isDevelopment) {
            this.argumentMap.put("minecraft:test_argument", TestFunctionArgumentType.testFunction());
            this.argumentMap.put("minecraft:test_class", TestClassArgumentType.testClass());
        }

        this.argumentMap.put("minecraft:uuid", UuidArgumentType.uuid());

        this.argumentMap.put("brigadier:bool", BoolArgumentType.bool());
        this.argumentMap.put("brigadier:float", FloatArgumentType.floatArg());
        this.argumentMap.put("brigadier:double", DoubleArgumentType.doubleArg());
        this.argumentMap.put("brigadier:integer", IntegerArgumentType.integer());
        this.argumentMap.put("brigadier:long", LongArgumentType.longArg());
        this.argumentMap.put("brigadier:string", StringArgumentType.string());
    }

    public Map<String, ArgumentType<?>> getArgumentMap() {
        return argumentMap;
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
    private <S> Map<String, ParsedArgument<S, ?>> getArguments(CommandContext<S> commandContext) {
        if (this.commandContextArgumentsField == null) return null;
        try {
            return (Map<String, ParsedArgument<S, ?>>) this.commandContextArgumentsField.get(commandContext);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
