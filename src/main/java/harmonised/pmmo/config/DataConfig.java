package harmonised.pmmo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.config.codecs.CodecMapPlayer;
import harmonised.pmmo.config.codecs.CodecMapSkills.SkillData;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class DataConfig {
	public DataConfig() {}
	
	private Map<ResourceLocation, Map<ResourceLocation, Map<String, Double>>> mobModifierData = new HashMap<>();
	private Map<Boolean, Map<ResourceLocation, Map<ResourceLocation, Integer>>> locationEffectData = new HashMap<>();
	private LinkedListMultimap<ResourceLocation, ResourceLocation> veinBlacklistData = LinkedListMultimap.create();
	private Map<UUID, CodecMapPlayer.PlayerData> playerSpecificSettings = new HashMap<>();	
	
	//==================DATA SETTER METHODS==============================
	public void setMobModifierData(ResourceLocation locationID, ResourceLocation mobID, Map<String, Double> data) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(mobID);
		Preconditions.checkNotNull(data);
		mobModifierData.computeIfAbsent(locationID, s -> new HashMap<>()).put(mobID, data);
	}
	
	public void setLocationEffectData(Boolean isPositiveEffect, ResourceLocation objectID, Map<ResourceLocation, Integer> dataMap) {
		Preconditions.checkNotNull(isPositiveEffect);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(dataMap);
		locationEffectData.computeIfAbsent(isPositiveEffect, s -> new HashMap<>()).put(objectID, dataMap);
	}
	
	public void setArrayData(ResourceLocation locationID, List<ResourceLocation> blockIDs) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(blockIDs);
		veinBlacklistData.putAll(locationID, blockIDs);
	}
	
	public void setPlayerSpecificData(UUID playerID, CodecMapPlayer.PlayerData data) {
		Preconditions.checkNotNull(playerID);
		Preconditions.checkNotNull(data);
		playerSpecificSettings.put(playerID, data);
	}
	
	//==================UTILITY METHODS==============================	
	public int getSkillColor(String skill) {
		return SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.getDefault()).color();
	}
	public Style getSkillStyle(String skill) {
		return Style.EMPTY.withColor(TextColor.fromRgb(getSkillColor(skill)));
	}
	
	public List<MobEffectInstance> getLocationEffect(boolean isPositive, ResourceLocation biomeID) {
		List<MobEffectInstance> effects = new ArrayList<>();
		for (Map.Entry<ResourceLocation, Integer> effect 
				: locationEffectData.getOrDefault(isPositive, new HashMap<>()).getOrDefault(biomeID, new HashMap<>()).entrySet()) {
			MobEffect effectRoot = ForgeRegistries.MOB_EFFECTS.getValue(effect.getKey());
			effects.add(new MobEffectInstance(effectRoot, 20, effect.getValue()));
		}
		return effects; 
	}
}
