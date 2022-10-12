package harmonised.pmmo.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public class PerkRegistry {
	public PerkRegistry() {}
	
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkExecutions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkTerminations = new HashMap<>();
	
	public void registerPerk(
			ResourceLocation perkID, 
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onExecute, 
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onConclude) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		perkExecutions.put(perkID, onExecute);
		perkTerminations.put(perkID, onConclude);
		MsLoggy.DEBUG.log(LOG_CODE.API, "Registered Perk: "+perkID.toString());
	}
	
	public CompoundTag executePerk(EventType cause, Player player, LogicalSide side) {
		return executePerk(cause, player, new CompoundTag(), side);
	}
	
	public CompoundTag executePerk(EventType cause, Player player, CompoundTag dataIn, LogicalSide side) {
		if (player == null) return new CompoundTag();
		if (dataIn == null) dataIn = new CompoundTag();
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
				int maxSetting = src.contains(APIUtils.MAX_LEVEL) ? src.getInt(APIUtils.MAX_LEVEL) : Config.MAX_LEVEL.get();
				int minSetting = src.contains(APIUtils.MIN_LEVEL) ? src.getInt(APIUtils.MIN_LEVEL) : 0;
				int perSetting = src.contains(APIUtils.MODULUS) ? src.getInt(APIUtils.MODULUS) : skillLevel;
				if (skillLevel <= maxSetting && skillLevel >= minSetting && skillLevel % Math.max(1, perSetting) == 0)
					executionOutput = perkExecutions.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				output = TagUtils.mergeTags(output, executionOutput);
			}
		}
		return output;
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
