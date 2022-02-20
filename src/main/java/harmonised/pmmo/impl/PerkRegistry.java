package harmonised.pmmo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PerkRegistry {
	public PerkRegistry() {}
	
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkServerExecutions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkServerTerminations = new HashMap<>();
	
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkClientExecutions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<Player, CompoundTag, Integer, CompoundTag>> perkClientTerminations = new HashMap<>();
	private Map<EventType, LinkedListMultimap<String, CompoundTag>> perkSettings = new HashMap<>();
	
	public void setSettings(Map<EventType, LinkedListMultimap<String, CompoundTag>> settings) {
		perkSettings = settings;
	}
	public Map<EventType, LinkedListMultimap<String, CompoundTag>> getSettings() {return perkSettings;}
	
	public void registerPerk(
			ResourceLocation perkID, 
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onExecute, 
			TriFunction<Player, CompoundTag, Integer, CompoundTag> onConclude,
			PerkSide side) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		Preconditions.checkNotNull(side);
		if (side.equals(PerkSide.CLIENT) || side.equals(PerkSide.BOTH)) {
			perkClientExecutions.put(perkID, onExecute);
			perkClientTerminations.put(perkID, onConclude);
		}
		if (side.equals(PerkSide.SERVER) || side.equals(PerkSide.BOTH)) {
			perkServerExecutions.put(perkID, onExecute);
			perkServerTerminations.put(perkID, onConclude);
		}
		MsLoggy.debug("Registered Perk: "+perkID.toString());
	}
	
	public CompoundTag executePerk(EventType cause, Player player) {
		return executePerk(cause, player, new CompoundTag());
	}
	
	public CompoundTag executePerk(EventType cause, Player player, CompoundTag dataIn) {
		LinkedListMultimap<String, CompoundTag> map =  perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<CompoundTag> entries = map.get(skill);
			int skillLevel = PmmoSavedData.get().getPlayerSkillLevel(skill, player.getUUID());
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = entries.get(i);
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				CompoundTag executionOutput = new CompoundTag();
				if (player instanceof ServerPlayer) 
					executionOutput = perkServerExecutions.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				else
					executionOutput = perkClientExecutions.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				output = TagUtils.mergeTags(output, executionOutput);
			}
		}
		return output;
	}
	
	public CompoundTag terminatePerk(EventType cause, Player player) {
		return terminatePerk(cause, player, new CompoundTag());
	}
	
	public CompoundTag terminatePerk(EventType cause, Player player, CompoundTag dataIn) {
		LinkedListMultimap<String, CompoundTag> map = perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<CompoundTag> entries = map.get(skill);
			int skillLevel = PmmoSavedData.get().getPlayerSkillLevel(skill, player.getUUID());
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = entries.get(i);
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				CompoundTag executionOutput = new CompoundTag();
				if (player instanceof ServerPlayer) 
					executionOutput = perkServerTerminations.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				else
					executionOutput = perkClientTerminations.getOrDefault(perkID, (plyr, nbt, level) -> new CompoundTag()).apply(player, src, skillLevel);
				output = TagUtils.mergeTags(output, executionOutput);
			}
		}
		return output;
	}
}
