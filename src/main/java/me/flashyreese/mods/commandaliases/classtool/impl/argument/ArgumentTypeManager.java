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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.classtool.ClassTool;
import me.flashyreese.mods.commandaliases.command.builders.CommandAliasesBuilder;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.Map;

/**
 * Represents the Argument Type Manager Class Tool
 *
 * @author FlashyReese
 * @version 0.1.3
 * @since 0.0.9
 * <p>
 * This ArgumentTypeManager maps ArgumentTypes to String.
 * See https://minecraft.gamepedia.com/Argument_types
 * There is probably a vanilla way to do this but I can't figure it out
 */
public class ArgumentTypeManager implements ClassTool<CommandAliasesArgumentType> {

    private final Map<String, CommandAliasesArgumentType> argumentMap = new Object2ObjectOpenHashMap<>();

    public ArgumentTypeManager() {
        registerArgumentTypes();
    }

    private void registerArgumentTypes() {
        this.argumentMap.put("word", new CommandAliasesArgumentType(StringArgumentType.word(), StringArgumentType::getString));
        this.argumentMap.put("string", new CommandAliasesArgumentType(StringArgumentType.string(), StringArgumentType::getString));
        this.argumentMap.put("greedy_string", new CommandAliasesArgumentType(StringArgumentType.greedyString(), StringArgumentType::getString));

        this.argumentMap.put("entity", new CommandAliasesArgumentType(EntityArgumentType.entity(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getEntity(commandContext, name).getName().asString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("entities", new CommandAliasesArgumentType(EntityArgumentType.entities(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getEntities(commandContext, name).toString();//Fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("player", new CommandAliasesArgumentType(EntityArgumentType.player(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getPlayer(commandContext, name).getName().asString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("players", new CommandAliasesArgumentType(EntityArgumentType.players(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getPlayers(commandContext, name).toString();//Fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));

        this.argumentMap.put("score_holder", new CommandAliasesArgumentType(ScoreHolderArgumentType.scoreHolder(), (commandContext, name) -> {
            try {
                return ScoreHolderArgumentType.getScoreHolder(commandContext, name);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("score_holders", new CommandAliasesArgumentType(ScoreHolderArgumentType.scoreHolders(), (commandContext, name) -> {
            try {
                return ScoreHolderArgumentType.getScoreHolders(commandContext, name).toString();//Fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));

        this.argumentMap.put("game_profile", new CommandAliasesArgumentType(GameProfileArgumentType.gameProfile(), (commandContext, name) -> {
            try {
                return GameProfileArgumentType.getProfileArgument(commandContext, name).toString();//Fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("block_pos", new CommandAliasesArgumentType(BlockPosArgumentType.blockPos(), (commandContext, name) -> {
            try {
                BlockPos blockPos = BlockPosArgumentType.getBlockPos(commandContext, name);
                return String.format("%s %s %s", blockPos.getX(), blockPos.getY(), blockPos.getZ());
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("column_pos", new CommandAliasesArgumentType(ColumnPosArgumentType.columnPos(), (commandContext, name) -> {
            ColumnPos columnPos = ColumnPosArgumentType.getColumnPos(commandContext, name);
            return String.format("%s %s", columnPos.x, columnPos.z);
        }));
        this.argumentMap.put("vec3", new CommandAliasesArgumentType(Vec3ArgumentType.vec3(), (commandContext, name) -> {
            try {
                Vec3d vec3d = Vec3ArgumentType.getVec3(commandContext, name);
                return String.format("%s %s %s", vec3d.x, vec3d.y, vec3d.z);//Todo: Additional method rounding
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("vec2", new CommandAliasesArgumentType(Vec2ArgumentType.vec2(), (commandContext, name) -> {
            try {
                Vec2f vec2f = Vec2ArgumentType.getVec2(commandContext, name);
                return String.format("%s %s", vec2f.x, vec2f.y);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("block_state", new CommandAliasesArgumentType(BlockStateArgumentType.blockState(), (commandContext, name) -> {
            BlockStateArgument blockStateArgument = BlockStateArgumentType.getBlockState(commandContext, name);//Fixme: oh boi more reading
            return "";
        }));
        this.argumentMap.put("block_predicate", new CommandAliasesArgumentType(BlockPredicateArgumentType.blockPredicate(), (commandContext, name) -> {
            try {
                return BlockPredicateArgumentType.getBlockPredicate(commandContext, name).toString();//fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("item_stack", new CommandAliasesArgumentType(ItemStackArgumentType.itemStack(), (commandContext, name) -> Registry.ITEM.getId(ItemStackArgumentType.getItemStackArgument(commandContext, name).getItem()).toString()));
        this.argumentMap.put("item_predicate", new CommandAliasesArgumentType(ItemPredicateArgumentType.itemPredicate(), (commandContext, name) -> {
            try {
                return ItemPredicateArgumentType.getItemPredicate(commandContext, name).toString();//fixme:
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("color", new CommandAliasesArgumentType(ColorArgumentType.color(), (commandContext, name) -> ColorArgumentType.getColor(commandContext, name).getName()));
        this.argumentMap.put("component", new CommandAliasesArgumentType(TextArgumentType.text(), (commandContext, name) -> TextArgumentType.getTextArgument(commandContext, name).asString()));//Maybe broken not sure
        this.argumentMap.put("message", new CommandAliasesArgumentType(MessageArgumentType.message(), (commandContext, name) -> {
            try {
                return MessageArgumentType.getMessage(commandContext, name).getString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("nbt_compound_tag", new CommandAliasesArgumentType(NbtCompoundTagArgumentType.nbtCompound(), (commandContext, name) -> NbtCompoundTagArgumentType.getCompoundTag(commandContext, name).asString()));
        this.argumentMap.put("nbt_tag", new CommandAliasesArgumentType(NbtTagArgumentType.nbtTag(), (commandContext, name) -> NbtTagArgumentType.getTag(commandContext, name).asString()));
        this.argumentMap.put("nbt_path", new CommandAliasesArgumentType(NbtPathArgumentType.nbtPath(), (commandContext, name) -> NbtPathArgumentType.getNbtPath(commandContext, name).toString()));
        this.argumentMap.put("objective", new CommandAliasesArgumentType(ObjectiveArgumentType.objective(), (commandContext, name) -> {
            try {
                return ObjectiveArgumentType.getObjective(commandContext, name).getName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("objective_criteria", new CommandAliasesArgumentType(ObjectiveCriteriaArgumentType.objectiveCriteria(), (commandContext, name) -> ObjectiveCriteriaArgumentType.getCriteria(commandContext, name).getName()));
        this.argumentMap.put("operation", new CommandAliasesArgumentType(OperationArgumentType.operation(), (commandContext, name) -> {
            try {
                return OperationArgumentType.getOperation(commandContext, name).toString();//fixme: super borked
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("particle", new CommandAliasesArgumentType(ParticleArgumentType.particle(), (commandContext, name) -> ParticleArgumentType.getParticle(commandContext, name).asString()));
        this.argumentMap.put("angle", new CommandAliasesArgumentType(AngleArgumentType.angle(), (commandContext, name) -> String.valueOf(AngleArgumentType.getAngle(commandContext, name))));
        this.argumentMap.put("rotation", new CommandAliasesArgumentType(RotationArgumentType.rotation(), (commandContext, name) -> RotationArgumentType.getRotation(commandContext, name).toString())); //Fixme:
        this.argumentMap.put("scoreboard_slot", new CommandAliasesArgumentType(ScoreboardSlotArgumentType.scoreboardSlot(), (commandContext, name) -> String.valueOf(ScoreboardSlotArgumentType.getScoreboardSlot(commandContext, name))));
        this.argumentMap.put("swizzle", new CommandAliasesArgumentType(SwizzleArgumentType.swizzle(), (commandContext, name) -> SwizzleArgumentType.getSwizzle(commandContext, name).toString()));
        this.argumentMap.put("team", new CommandAliasesArgumentType(TeamArgumentType.team(), (commandContext, name) -> {
            try {
                return TeamArgumentType.getTeam(commandContext, name).getName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("item_slot", new CommandAliasesArgumentType(ItemSlotArgumentType.itemSlot(), (commandContext, name) -> String.valueOf(ItemSlotArgumentType.getItemSlot(commandContext, name))));
        this.argumentMap.put("resource_location", new CommandAliasesArgumentType(IdentifierArgumentType.identifier(), (commandContext, name) -> IdentifierArgumentType.getIdentifier(commandContext, name).toString()));
        this.argumentMap.put("mob_effect", new CommandAliasesArgumentType(MobEffectArgumentType.mobEffect(), (commandContext, name) -> {
            try {
                return Registry.STATUS_EFFECT.getId(MobEffectArgumentType.getMobEffect(commandContext, name)).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("function", new CommandAliasesArgumentType(FunctionArgumentType.function(), (commandContext, name) -> {
            try {
                return FunctionArgumentType.getFunctions(commandContext, name).toString();//Fixme: extra borked
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("entity_anchor", new CommandAliasesArgumentType(EntityAnchorArgumentType.entityAnchor(), (commandContext, name) -> EntityAnchorArgumentType.getEntityAnchor(commandContext, name).name()));
        //this.argumentMap.put("int_range", new ArgType(NumberRangeArgumentType.numberRange(), (commandContext, name) -> {}));
        //this.argumentMap.put("float_range", NumberRangeArgumentType.method_30918());
        this.argumentMap.put("item_enchantment", new CommandAliasesArgumentType(ItemEnchantmentArgumentType.itemEnchantment(), (commandContext, name) -> Registry.ENCHANTMENT.getId(ItemEnchantmentArgumentType.getEnchantment(commandContext, name)).toString()));
        this.argumentMap.put("entity_summon", new CommandAliasesArgumentType(EntitySummonArgumentType.entitySummon(), (commandContext, name) -> {
            try {
                return EntitySummonArgumentType.getEntitySummon(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }));
        this.argumentMap.put("dimension", new CommandAliasesArgumentType(DimensionArgumentType.dimension(), (commandContext, name) -> commandContext.getArgument(name, Identifier.class).toString()));
        //this.argumentMap.put("time", new ArgType(TimeArgumentType.time(), (commandContext, name) -> {}));
        this.argumentMap.put("uuid", new CommandAliasesArgumentType(UuidArgumentType.uuid(), (commandContext, name) -> UuidArgumentType.getUuid(commandContext, name).toString()));

        this.argumentMap.put("brigadier:bool", new CommandAliasesArgumentType(BoolArgumentType.bool(), (commandContext, name) -> String.valueOf(BoolArgumentType.getBool(commandContext, name))));
        this.argumentMap.put("brigadier:float", new CommandAliasesArgumentType(FloatArgumentType.floatArg(), (commandContext, name) -> String.valueOf(FloatArgumentType.getFloat(commandContext, name))));
        this.argumentMap.put("brigadier:double", new CommandAliasesArgumentType(DoubleArgumentType.doubleArg(), (commandContext, name) -> String.valueOf(DoubleArgumentType.getDouble(commandContext, name))));
        this.argumentMap.put("brigadier:integer", new CommandAliasesArgumentType(IntegerArgumentType.integer(), (commandContext, name) -> String.valueOf(IntegerArgumentType.getInteger(commandContext, name))));
        this.argumentMap.put("brigadier:long", new CommandAliasesArgumentType(LongArgumentType.longArg(), (commandContext, name) -> String.valueOf(LongArgumentType.getLong(commandContext, name))));
        this.argumentMap.put("brigadier:string", new CommandAliasesArgumentType(StringArgumentType.string(), (commandContext, name) -> String.valueOf(StringArgumentType.getString(commandContext, name))));
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
    public CommandAliasesArgumentType getValue(String key) {
        return this.argumentMap.get(key);
    }

    @Override
    public String getValue(CommandContext<ServerCommandSource> context, CommandAliasesBuilder.CommandAliasesHolder holder) {
        return this.getValue(holder.getMethod()).getBiFunction().apply(context, holder.getVariableName());
    }
}
