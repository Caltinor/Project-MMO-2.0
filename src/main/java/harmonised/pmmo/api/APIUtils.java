package harmonised.pmmo.api;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.TriFunction;
import org.checkerframework.checker.nullness.qual.NonNull;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
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

public class APIUtils {
	/* NOTES
	 * 
	 * - Add methods to modify the configuration while live
	 * - Add subjective methods for creating individualized req and xp behaviors
	 * - JAVADOCS!!!!
	 */
	//===============REQ AND TOOLTIP REFERENCES======================
	public static void registerActionPredicate(ResourceLocation res, ReqType jType, Predicate<Player> pred) {
		PredicateRegistry.registerPredicate(res, jType, pred);
	}
	
	public static void registerBreakPredicate(ResourceLocation res, ReqType jType, BiPredicate<Player, BlockEntity> pred) {
		PredicateRegistry.registerBreakPredicate(res, jType, pred);
	}
	
	public static void registerItemRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<ItemStack, Map<String, Integer>> func)  {
		TooltipRegistry.registerItemRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<BlockEntity, Map<String, Integer>> func) {
		TooltipRegistry.registerBlockRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<Entity, Map<String, Integer>> func) {
		TooltipRegistry.registerEntityRequirementTooltipData(res, reqType, func);
	}
	
	public static void registerItemXpGainTooltipData(ResourceLocation res, EventType eventType, Function<ItemStack, Map<String, Long>> func) {
		TooltipRegistry.registerItemXpGainTooltipData(res, eventType, func);
	}
	
	public static void registerBlockXpGainTooltipData(ResourceLocation res, EventType eventType, Function<BlockEntity, Map<String, Long>> func) {
		TooltipRegistry.registerBlockXpGainTooltipData(res, eventType, func);
	}
	
	public static void registerEntityXpGainTooltipData(ResourceLocation res, EventType eventType, Function<Entity, Map<String, Long>> func) {
		TooltipRegistry.registerEntityXpGainTooltipData(res, eventType, func);
	}
	
	//===============EVENT TRIGGER REFERENCES========================
	public static final String IS_CANCELLED = "is_cancelled";
	
	public static void registerListener(
			@NonNull ResourceLocation listenerID, 
			@NonNull EventType eventType, 
			@NonNull BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger) {
		EventTriggerRegistry.registerListener(listenerID, eventType, executeOnTrigger);
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
	
	public static void registerPerk(
			@NonNull ResourceLocation perkID, 
			@NonNull TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onExecute, 
			@NonNull TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onConclude) {
		PerkRegistry.registerPerk(perkID, onExecute, onConclude);
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
