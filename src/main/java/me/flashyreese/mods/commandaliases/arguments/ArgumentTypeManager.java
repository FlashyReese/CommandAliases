package me.flashyreese.mods.commandaliases.arguments;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.*;

import java.util.HashMap;
import java.util.Map;

public class ArgumentTypeManager {

    private final Map<String, CommandAliasesArgumentType> argumentMap = new HashMap<>();

    public ArgumentTypeManager() {
        registerArgumentTypes();
    }

    private void registerArgumentTypes() {
        this.argumentMap.put("word", new CommandAliasesArgumentType(StringArgumentType.word(), StringArgumentType::getString));
        this.argumentMap.put("string", new CommandAliasesArgumentType(StringArgumentType.string(), StringArgumentType::getString));
        this.argumentMap.put("greedy_string", new CommandAliasesArgumentType(StringArgumentType.greedyString(), StringArgumentType::getString));

        this.argumentMap.put("entity", new CommandAliasesArgumentType(EntityArgumentType.entity(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getEntity(commandContext, name).getEntityName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("entities", new CommandAliasesArgumentType(EntityArgumentType.entities(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getEntities(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("player", new CommandAliasesArgumentType(EntityArgumentType.player(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getPlayer(commandContext, name).getEntityName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("players", new CommandAliasesArgumentType(EntityArgumentType.players(), (commandContext, name) -> {
            try {
                return EntityArgumentType.getPlayers(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));

        this.argumentMap.put("score_holder", new CommandAliasesArgumentType(ScoreHolderArgumentType.scoreHolder(), (commandContext, name) -> {
            try {
                return ScoreHolderArgumentType.getScoreHolder(commandContext, name);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("score_holders", new CommandAliasesArgumentType(ScoreHolderArgumentType.scoreHolders(), (commandContext, name) -> {
            try {
                return ScoreHolderArgumentType.getScoreHolders(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));

        this.argumentMap.put("game_profile", new CommandAliasesArgumentType(GameProfileArgumentType.gameProfile(), (commandContext, name) -> {
            try {
                return GameProfileArgumentType.getProfileArgument(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("block_pos", new CommandAliasesArgumentType(BlockPosArgumentType.blockPos(), (commandContext, name) -> {
            try {
                return BlockPosArgumentType.getBlockPos(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("column_pos", new CommandAliasesArgumentType(ColumnPosArgumentType.columnPos(), (commandContext, name) -> ColumnPosArgumentType.getColumnPos(commandContext, name).toString()));
        this.argumentMap.put("vec3", new CommandAliasesArgumentType(Vec3ArgumentType.vec3(), (commandContext, name) -> {
            try {
                return Vec3ArgumentType.getVec3(commandContext, name).toString();//Additional method
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("vec2", new CommandAliasesArgumentType(Vec2ArgumentType.vec2(), (commandContext, name) -> {
            try {
                return Vec2ArgumentType.getVec2(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("block_state", new CommandAliasesArgumentType(BlockStateArgumentType.blockState(), (commandContext, name) -> BlockStateArgumentType.getBlockState(commandContext, name).toString()));
        this.argumentMap.put("block_predicate", new CommandAliasesArgumentType(BlockPredicateArgumentType.blockPredicate(), (commandContext, name) -> {
            try {
                return BlockPredicateArgumentType.getBlockPredicate(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("item_stack", new CommandAliasesArgumentType(ItemStackArgumentType.itemStack(), (commandContext, name) -> ItemStackArgumentType.getItemStackArgument(commandContext, name).toString()));
        this.argumentMap.put("item_predicate", new CommandAliasesArgumentType(ItemPredicateArgumentType.itemPredicate(), (commandContext, name) -> {
            try {
                return ItemPredicateArgumentType.getItemPredicate(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("color", new CommandAliasesArgumentType(ColorArgumentType.color(), (commandContext, name) -> ColorArgumentType.getColor(commandContext, name).name()));
        this.argumentMap.put("component", new CommandAliasesArgumentType(TextArgumentType.text(), (commandContext, name) -> TextArgumentType.getTextArgument(commandContext, name).asString()));
        this.argumentMap.put("message", new CommandAliasesArgumentType(MessageArgumentType.message(), (commandContext, name) -> {
            try {
                return MessageArgumentType.getMessage(commandContext, name).getString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
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
            return "BORKED";
        }));
        this.argumentMap.put("objective_criteria", new CommandAliasesArgumentType(ObjectiveCriteriaArgumentType.objectiveCriteria(), (commandContext, name) -> ObjectiveCriteriaArgumentType.getCriteria(commandContext, name).getName()));
        this.argumentMap.put("operation", new CommandAliasesArgumentType(OperationArgumentType.operation(), (commandContext, name) -> {
            try {
                return OperationArgumentType.getOperation(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("particle", new CommandAliasesArgumentType(ParticleArgumentType.particle(), (commandContext, name) -> ParticleArgumentType.getParticle(commandContext, name).asString()));
        this.argumentMap.put("angle", new CommandAliasesArgumentType(AngleArgumentType.angle(), (commandContext, name) -> String.valueOf(AngleArgumentType.getAngle(commandContext, name))));
        this.argumentMap.put("rotation", new CommandAliasesArgumentType(RotationArgumentType.rotation(), (commandContext, name) -> RotationArgumentType.getRotation(commandContext, name).toString()));
        this.argumentMap.put("scoreboard_slot", new CommandAliasesArgumentType(ScoreboardSlotArgumentType.scoreboardSlot(), (commandContext, name) -> String.valueOf(ScoreboardSlotArgumentType.getScoreboardSlot(commandContext, name))));
        this.argumentMap.put("swizzle", new CommandAliasesArgumentType(SwizzleArgumentType.swizzle(), (commandContext, name) -> SwizzleArgumentType.getSwizzle(commandContext, name).toString()));
        this.argumentMap.put("team", new CommandAliasesArgumentType(TeamArgumentType.team(), (commandContext, name) -> {
            try {
                return TeamArgumentType.getTeam(commandContext, name).getName();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("item_slot", new CommandAliasesArgumentType(ItemSlotArgumentType.itemSlot(), (commandContext, name) -> String.valueOf(ItemSlotArgumentType.getItemSlot(commandContext, name))));
        this.argumentMap.put("resource_location", new CommandAliasesArgumentType(IdentifierArgumentType.identifier(), (commandContext, name) -> IdentifierArgumentType.getIdentifier(commandContext, name).toString()));
        this.argumentMap.put("mob_effect", new CommandAliasesArgumentType(MobEffectArgumentType.mobEffect(), (commandContext, name) -> {
            try {
                return MobEffectArgumentType.getMobEffect(commandContext, name).getName().asString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("function", new CommandAliasesArgumentType(FunctionArgumentType.function(), (commandContext, name) -> {
            try {
                return FunctionArgumentType.getFunctions(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("entity_anchor", new CommandAliasesArgumentType(EntityAnchorArgumentType.entityAnchor(), (commandContext, name) -> EntityAnchorArgumentType.getEntityAnchor(commandContext, name).name()));
        //this.argumentMap.put("int_range", new ArgType(NumberRangeArgumentType.numberRange(), (commandContext, name) -> {}));
        //this.argumentMap.put("float_range", NumberRangeArgumentType.method_30918());
        this.argumentMap.put("item_enchantment", new CommandAliasesArgumentType(ItemEnchantmentArgumentType.itemEnchantment(), (commandContext, name) -> ItemEnchantmentArgumentType.getEnchantment(commandContext, name).type.name()));
        this.argumentMap.put("entity_summon", new CommandAliasesArgumentType(EntitySummonArgumentType.entitySummon(), (commandContext, name) -> {
            try {
                return EntitySummonArgumentType.getEntitySummon(commandContext, name).toString();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return "BORKED";
        }));
        this.argumentMap.put("dimension", new CommandAliasesArgumentType(DimensionArgumentType.dimension(), (commandContext, name) -> String.valueOf(DoubleArgumentType.getDouble(commandContext, name))));
        //this.argumentMap.put("time", new ArgType(TimeArgumentType.time(), (commandContext, name) -> {}));
        this.argumentMap.put("uuid", new CommandAliasesArgumentType(UuidArgumentType.uuid(), (commandContext, name) -> UuidArgumentType.getUuid(commandContext, name).toString()));

        this.argumentMap.put("brigadier:bool", new CommandAliasesArgumentType(BoolArgumentType.bool(), (commandContext, name) -> String.valueOf(BoolArgumentType.getBool(commandContext, name))));
        this.argumentMap.put("brigadier:float", new CommandAliasesArgumentType(FloatArgumentType.floatArg(), (commandContext, name) -> String.valueOf(FloatArgumentType.getFloat(commandContext, name))));
        this.argumentMap.put("brigadier:double", new CommandAliasesArgumentType(DoubleArgumentType.doubleArg(), (commandContext, name) -> String.valueOf(DoubleArgumentType.getDouble(commandContext, name))));
        this.argumentMap.put("brigadier:integer", new CommandAliasesArgumentType(IntegerArgumentType.integer(), (commandContext, name) -> String.valueOf(IntegerArgumentType.getInteger(commandContext, name))));
        this.argumentMap.put("brigadier:long", new CommandAliasesArgumentType(LongArgumentType.longArg(), (commandContext, name) -> String.valueOf(LongArgumentType.getLong(commandContext, name))));
        this.argumentMap.put("brigadier:string", new CommandAliasesArgumentType(StringArgumentType.string(), (commandContext, name) -> String.valueOf(StringArgumentType.getString(commandContext, name))));
    }

    public Map<String, CommandAliasesArgumentType> getArgumentMap() {
        return argumentMap;
    }
}
