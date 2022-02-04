package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.config.datapack.codecs.CodecMapPlayer;
import harmonised.pmmo.config.readers.codecs.CodecTypeSkills.SkillData;
import net.minecraft.resources.ResourceLocation;

public class DataConfig {
	public DataConfig() {}
	
	private Map<ResourceLocation, Map<ResourceLocation, Map<String, Double>>> mobModifierData = new HashMap<>();
	private Map<CoreType, Map<ResourceLocation, Map<ResourceLocation, Integer>>> locationEffectData = new HashMap<>();
	private LinkedListMultimap<ResourceLocation, ResourceLocation> veinBlacklistData = LinkedListMultimap.create();
	private Map<UUID, CodecMapPlayer.PlayerData> playerSpecificSettings = new HashMap<>();	
	private Map<String, SkillData> skillData = new HashMap<>();
	
	//==================DATA SETTER METHODS==============================
	public void setMobModifierData(ResourceLocation locationID, ResourceLocation mobID, Map<String, Double> data) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(mobID);
		Preconditions.checkNotNull(data);
		mobModifierData.computeIfAbsent(locationID, s -> new HashMap<>()).put(mobID, data);
	}
	
	public void setLocationEffectData(CoreType coreType, ResourceLocation objectID, Map<ResourceLocation, Integer> dataMap) {
		Preconditions.checkNotNull(coreType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(dataMap);
		locationEffectData.computeIfAbsent(coreType, s -> new HashMap<>()).put(objectID, dataMap);
	}
	
	public void setArrayData(ResourceLocation locationID, List<ResourceLocation> blockIDs) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(blockIDs);
		veinBlacklistData.putAll(locationID, blockIDs);
	}
	
	public void setSkillData(String skill, SkillData data) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(data);
		skillData.put(skill, data);
	}
	
	public void setPlayerSpecificData(UUID playerID, CodecMapPlayer.PlayerData data) {
		Preconditions.checkNotNull(playerID);
		Preconditions.checkNotNull(data);
		playerSpecificSettings.put(playerID, data);
	}
	
	//==================UTILITY METHODS==============================	
	public int getSkillColor(String skill) {
		return skillData.getOrDefault(skill, SkillData.getDefault()).color();
	}
}
