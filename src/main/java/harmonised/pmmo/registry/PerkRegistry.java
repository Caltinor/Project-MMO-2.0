package harmonised.pmmo.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fml.LogicalSide;

public class PerkRegistry {
	public PerkRegistry() {}
	
	private Map<ResourceLocation, CompoundTag> properties = new HashMap<>();
	private Map<ResourceLocation, TriPredicate<Player, CompoundTag, Integer>> conditions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkExecutions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkTerminations = new HashMap<>();
	
	public void registerPerk(
			ResourceLocation perkID, 
			CompoundTag propertyDefaults,
			TriPredicate<Player, CompoundTag, Integer> customConditions,
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onExecute, 
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onConclude) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(propertyDefaults);
		Preconditions.checkNotNull(customConditions);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		properties.put(perkID, propertyDefaults);
		conditions.put(perkID, customConditions);
		perkExecutions.put(perkID, onExecute);
		perkTerminations.put(perkID, onConclude);
		MsLoggy.DEBUG.log(LOG_CODE.API, "Registered Perk: "+perkID.toString());
	}
	
	/* REWORK NOTES
	 * 1. add a predicate for non-standard conditions
	 * 2. add a property registry with a record of (String key, T default value)
	 */
	
	public CompoundTag executePerk(EventType cause, Player player, LogicalSide side) {
		return executePerk(cause, player, new CompoundTag(), side);
	}
	
	public CompoundTag executePerk(EventType cause, Player player, @NotNull CompoundTag dataIn, LogicalSide side) {
		if (player == null) return new CompoundTag();
		CompoundTag output = new CompoundTag();
		PerksConfig.PERK_SETTINGS.get().getOrDefault(cause, new HashMap<>()).forEach((skill, list) -> {
			int skillLevel = Core.get(side).getData().getPlayerSkillLevel(skill, player.getUUID());
			list.forEach(src -> {
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				src = properties.get(perkID).merge(src.merge(dataIn));
				CompoundTag executionOutput = new CompoundTag();
				
				if (isValidContext(perkID, player, src, skillLevel))
					executionOutput = perkExecutions.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				output.merge(TagUtils.mergeTags(output, executionOutput));
			});
		});
		return output;
	}
	
	
	private final Random rand = new Random();
	
	private boolean isValidContext(ResourceLocation perkID, Player player, CompoundTag src, int skillLevel) {
		if (src.contains(APIUtils.MAX_LEVEL) && skillLevel > src.getInt(APIUtils.MAX_LEVEL))
			return false;
		if (src.contains(APIUtils.MIN_LEVEL) && skillLevel < src.getInt(APIUtils.MIN_LEVEL))
			return false;
		boolean modulus = src.contains(APIUtils.MODULUS), 
				milestone = src.contains(APIUtils.MILESTONES);
		if (modulus || milestone) {
			boolean modulus_match = modulus,
					milestone_match = milestone;
			if (modulus && skillLevel % Math.max(1, src.getInt(APIUtils.MODULUS)) != 0)
				modulus_match = false;
			if (milestone && !src.getList(APIUtils.MILESTONES, Tag.TAG_DOUBLE).stream()
					.map(tag -> ((DoubleTag)tag).getAsInt()).toList().contains(skillLevel))
				milestone_match = false;
			if (!modulus_match && !milestone_match)
				return false;
		}
		if (src.contains(APIUtils.CHANCE) && src.getDouble(APIUtils.CHANCE) < rand.nextDouble())
			return false;
		
		return conditions.getOrDefault(perkID, (p,s,l) -> true).test(player, src, skillLevel);
	}
	
	public CompoundTag terminatePerk(EventType cause, Player player, LogicalSide side) {
		return terminatePerk(cause, player, new CompoundTag(), side);
	}
	
	public CompoundTag terminatePerk(EventType cause, Player player, CompoundTag dataIn, LogicalSide side) {
		Map<String, List<CompoundTag>> map =  PerksConfig.PERK_SETTINGS.get().getOrDefault(cause, new HashMap<>());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<CompoundTag> entries = map.get(skill);
			int skillLevel = Core.get(side).getData().getPlayerSkillLevel(skill, player.getUUID());
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = entries.get(i);
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				CompoundTag executionOutput = new CompoundTag();
				executionOutput = perkTerminations.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				output = TagUtils.mergeTags(output, executionOutput);
			}
		}
		return output;
	}
}
