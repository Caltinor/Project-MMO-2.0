package harmonised.pmmo.api;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriFunction;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;

public class APIUtils {
	/* NOTES
	 * 
	 * - Add methods to modify the configuration while live
	 * - Add subjective methods for creating individualized req and xp behaviors
	 * - JAVADOCS!!!!
	 */
	//===============CORE HOOKS======================================
	public static int getLevel(String skill, UUID playerID) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		return PmmoSavedData.get().getPlayerSkillLevel(skill, playerID);
	}
	
	public static void setLevel(String skill, UUID playerID, int level) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		PmmoSavedData.get().setPlayerSkillLevel(skill, playerID, level);
	}
	
	public static boolean addLevel(String skill, UUID playerID, int levelChange) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		return PmmoSavedData.get().changePlayerSkillLevel(skill, playerID, levelChange);
	}
	
	public static long getXp(String skill, UUID playerID) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		return PmmoSavedData.get().getXpRaw(playerID, skill);
	}
	
	public static void setXp(String skill, UUID playerID, long xpRaw) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		PmmoSavedData.get().setXpRaw(playerID, skill, xpRaw);
	}
	
	public static boolean addXp(String skill, UUID playerID, long change) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(playerID);
		return PmmoSavedData.get().setXpDiff(playerID, skill, change);
	}
	
	public static Map<String, Long> getXpAwardMap(ItemStack item, EventType type, LogicalSide side) {
		Preconditions.checkNotNull(item);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		Core core = Core.get(side);
		ResourceLocation rl = item.getItem().getRegistryName();
		Map<String, Long> map =  core.getTooltipRegistry().getItemXpGainTooltipData(rl, type, item);
		if (map.isEmpty())
			map = core.getXpUtils().getObjectExperienceMap(type, rl);
		if (map.isEmpty())
			map = AutoValues.getExperienceAward(type, rl, ObjectType.ITEM);
		return map;
	}
	
	public static Map<String, Long> getXpAwardMap(BlockEntity tile, EventType type, LogicalSide side) {
		Preconditions.checkNotNull(tile);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		Core core = Core.get(side);
		ResourceLocation rl = tile.getBlockState().getBlock().getRegistryName();
		Map<String, Long> map =  core.getTooltipRegistry().getBlockXpGainTooltipData(rl, type, tile);
		if (map.isEmpty())
			map = core.getXpUtils().getObjectExperienceMap(type, rl);
		if (map.isEmpty())
			map = AutoValues.getExperienceAward(type, rl, ObjectType.BLOCK);
		return map;
	}
	
	public static Map<String, Long> getXpAwardMap(Entity entity, EventType type, LogicalSide side) {
		Preconditions.checkNotNull(entity);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		Core core = Core.get(side);
		ResourceLocation rl = new ResourceLocation(entity.getEncodeId());
		Map<String, Long> map =  core.getTooltipRegistry().getEntityXpGainTooltipData(rl, type, entity);
		if (map.isEmpty())
			map = core.getXpUtils().getObjectExperienceMap(type, rl);
		if (map.isEmpty())
			map = AutoValues.getExperienceAward(type, rl, ObjectType.ENTITY);
		return map;
	}
	
	//===============REQ AND TOOLTIP REFERENCES======================
	public static void registerActionPredicate(ResourceLocation res, ReqType jType, BiPredicate<Player, ItemStack> pred) {
		Core.get(LogicalSide.SERVER).getPredicateRegistry().registerPredicate(res, jType, pred);
	}
	
	public static void registerBreakPredicate(ResourceLocation res, ReqType jType, BiPredicate<Player, BlockEntity> pred) {
		Core.get(LogicalSide.SERVER).getPredicateRegistry().registerBreakPredicate(res, jType, pred);
	}
	
	public static void registerItemRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<ItemStack, Map<String, Integer>> func)  {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<BlockEntity, Map<String, Integer>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerBlockRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<Entity, Map<String, Integer>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerEntityRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerItemXpGainTooltipData(ResourceLocation res, EventType eventType, Function<ItemStack, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemXpGainTooltipData(res, eventType, func);
	}
	
	public static void registerBlockXpGainTooltipData(ResourceLocation res, EventType eventType, Function<BlockEntity, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerBlockXpGainTooltipData(res, eventType, func);
	}
	
	public static void registerEntityXpGainTooltipData(ResourceLocation res, EventType eventType, Function<Entity, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerEntityXpGainTooltipData(res, eventType, func);
	}
	
	//===============EVENT TRIGGER REFERENCES========================
	public static final String IS_CANCELLED = "is_cancelled";
	
	public static void registerListener(
			@NonNull ResourceLocation listenerID, 
			@NonNull EventType eventType, 
			@NonNull BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger) {
		Core.get(LogicalSide.SERVER).getEventTriggerRegistry().registerListener(listenerID, eventType, executeOnTrigger);
	}
	
	//===============PERK REFERENCES=================================
	public static final String PER_LEVEL = "per_level";
	public static final String MAX_BOOST = "max_boost";
	public static final String RATIO = "ratio";
	public static final String MODIFIER = "modifier";
	public static final String MIN_LEVEL = "min_level";
	public static final String COOLDOWN = "cooldown";
	public static final String DURATION = "duration";
	
	public static final String BLOCK_POS = "block_pos";
	public static final String SKILLNAME = "skill";
	
	public static final String BREAK_SPEED_INPUT_VALUE = "speedIn";
	public static final String BREAK_SPEED_OUTPUT_VALUE = "speed";
	
	public static final String DAMAGE_IN = "damageIn";
	public static final String DAMAGE_OUT ="damage";
	
	public static void registerPerk(
			@NonNull ResourceLocation perkID, 
			@NonNull TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onExecute, 
			@NonNull TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onConclude) {
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(perkID, onExecute, onConclude);
	}	
	
	//===============UTILITY METHODS=================================
	public static final String SERIALIZED_AWARD_MAP = "serialized_award_map";
	
	public static ListTag serializeAwardMap(Map<String, Long> awardMap) {
		ListTag out = new ListTag();
		for (Map.Entry<String, Long> entry : awardMap.entrySet()) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString(Reference.API_MAP_SERIALIZER_KEY, entry.getKey());
			nbt.putLong(Reference.API_MAP_SERIALIZER_VALUE, entry.getValue());
			out.add(nbt);
		}
		return out;
	}
}
