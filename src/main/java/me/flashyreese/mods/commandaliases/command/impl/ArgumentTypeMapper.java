package me.flashyreese.mods.commandaliases.command.impl;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.core.registries.Registries;

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

    public ArgumentTypeMapper(CommandBuildContext registryAccess) {
        this.registerArgumentTypes(registryAccess);
        try {
            this.commandContextArgumentsField = CommandContext.class.getDeclaredField("arguments");
            this.commandContextArgumentsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void registerArgumentTypes(CommandBuildContext registryAccess) {
        this.argumentMap.put("minecraft:word", StringArgumentType.word());
        this.argumentMap.put("minecraft:string", StringArgumentType.string());
        this.argumentMap.put("minecraft:greedy_string", StringArgumentType.greedyString());

        this.argumentMap.put("minecraft:entity", EntityArgument.entity());
        this.argumentMap.put("minecraft:entities", EntityArgument.entities());
        this.argumentMap.put("minecraft:player", EntityArgument.player());
        this.argumentMap.put("minecraft:players", EntityArgument.players());

        this.argumentMap.put("minecraft:score_holder", ScoreHolderArgument.scoreHolder());
        this.argumentMap.put("minecraft:score_holders", ScoreHolderArgument.scoreHolders());

        this.argumentMap.put("minecraft:game_profile", GameProfileArgument.gameProfile());
        this.argumentMap.put("minecraft:block_pos", BlockPosArgument.blockPos());
        this.argumentMap.put("minecraft:column_pos", ColumnPosArgument.columnPos());
        this.argumentMap.put("minecraft:vec3", Vec3Argument.vec3());
        this.argumentMap.put("minecraft:vec2", Vec2Argument.vec2());
        this.argumentMap.put("minecraft:block_state", BlockStateArgument.block(registryAccess));
        this.argumentMap.put("minecraft:block_predicate", BlockPredicateArgument.blockPredicate(registryAccess));
        this.argumentMap.put("minecraft:item_stack", ItemArgument.item(registryAccess));
        this.argumentMap.put("minecraft:item_predicate", ItemPredicateArgument.itemPredicate(registryAccess));
        this.argumentMap.put("minecraft:color", ColorArgument.color());
        this.argumentMap.put("minecraft:component", ComponentArgument.textComponent(registryAccess));
        this.argumentMap.put("minecraft:message", MessageArgument.message());
        this.argumentMap.put("minecraft:nbt_compound_tag", CompoundTagArgument.compoundTag());
        this.argumentMap.put("minecraft:nbt_tag", NbtTagArgument.nbtTag());
        this.argumentMap.put("minecraft:nbt_path", NbtPathArgument.nbtPath());
        this.argumentMap.put("minecraft:objective", ObjectiveArgument.objective());
        this.argumentMap.put("minecraft:objective_criteria", ObjectiveCriteriaArgument.criteria());
        this.argumentMap.put("minecraft:operation", OperationArgument.operation());
        this.argumentMap.put("minecraft:particle", ParticleArgument.particle(registryAccess));
        this.argumentMap.put("minecraft:angle", AngleArgument.angle());
        this.argumentMap.put("minecraft:rotation", RotationArgument.rotation());
        this.argumentMap.put("minecraft:scoreboard_slot", ScoreboardSlotArgument.displaySlot());
        this.argumentMap.put("minecraft:swizzle", SwizzleArgument.swizzle());
        this.argumentMap.put("minecraft:team", TeamArgument.team());
        this.argumentMap.put("minecraft:item_slot", SlotArgument.slot());
        this.argumentMap.put("minecraft:resource_location", IdentifierArgument.id());
        this.argumentMap.put("minecraft:function", FunctionArgument.functions());
        this.argumentMap.put("minecraft:entity_anchor", EntityAnchorArgument.anchor());
        this.argumentMap.put("minecraft:int_range", RangeArgument.intRange());
        this.argumentMap.put("minecraft:float_range", RangeArgument.floatRange());
        this.argumentMap.put("minecraft:dimension", DimensionArgument.dimension());
        this.argumentMap.put("minecraft:gamemode", GameModeArgument.gameMode());
        this.argumentMap.put("minecraft:time", TimeArgument.time());

        // Todo: Allow entire registry keys by creating registry map
        this.argumentMap.put("minecraft:entry.attribute_key", ResourceArgument.resource(registryAccess, Registries.ATTRIBUTE));
        this.argumentMap.put("minecraft:entry.status_effect_key", ResourceArgument.resource(registryAccess, Registries.MOB_EFFECT));
        this.argumentMap.put("minecraft:entry.enchantment_type", ResourceArgument.resource(registryAccess, Registries.ENCHANTMENT));
        this.argumentMap.put("minecraft:entry.biome_key", ResourceArgument.resource(registryAccess, Registries.BIOME));
        this.argumentMap.put("minecraft:entry.entity_type_key", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE));

        this.argumentMap.put("minecraft:entry_predicate.biome_key", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.BIOME));
        this.argumentMap.put("minecraft:entry_predicate.poi_type_key", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.POINT_OF_INTEREST_TYPE));

        this.argumentMap.put("minecraft:predicate.structure_key", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE));

        this.argumentMap.put("minecraft:key.configured_feature_key", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE));
        this.argumentMap.put("minecraft:key.template_pool_key", ResourceKeyArgument.key(Registries.TEMPLATE_POOL));
        this.argumentMap.put("minecraft:key.structure_key", ResourceKeyArgument.key(Registries.STRUCTURE));
        // end

        this.argumentMap.put("minecraft:template_mirror", TemplateMirrorArgument.templateMirror());
        this.argumentMap.put("minecraft:template_rotation", TemplateRotationArgument.templateRotation());

        this.argumentMap.put("minecraft:uuid", UuidArgument.uuid());

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
