package harmonised.pmmo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.config.codecs.CodecMapPlayer;
import harmonised.pmmo.config.codecs.CodecMapPlayer.PlayerData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class DataConfig {
	public DataConfig() {}
	
	private Map<ResourceLocation, Map<ResourceLocation, Map<String, Double>>> mobModifierData = new HashMap<>();
	private Map<Boolean, Map<ResourceLocation, Map<ResourceLocation, Integer>>> locationEffectData = new HashMap<>();
	private Map<ResourceLocation, Map<ResourceLocation, Integer>> reqEffectData = new HashMap<>();
	private LinkedListMultimap<ResourceLocation, ResourceLocation> veinBlacklistData = LinkedListMultimap.create();
	private Map<UUID, PlayerData> playerSpecificSettings = new HashMap<>();	
	
	public void reset() {
		mobModifierData = new HashMap<>();
		locationEffectData = new HashMap<>();
		reqEffectData = new HashMap<>();
		veinBlacklistData = LinkedListMultimap.create();
		playerSpecificSettings = new HashMap<>();
	}
	
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
	
	public void setReqEffectData(ResourceLocation itemID, ResourceLocation effectID, Integer effectPower) {
		Preconditions.checkNotNull(itemID);
		Preconditions.checkNotNull(effectID);
		Preconditions.checkNotNull(effectPower);
		reqEffectData.computeIfAbsent(itemID, s -> new HashMap<>()).put(effectID, effectPower);
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
		return SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault()).getColor();
	}
	public Style getSkillStyle(String skill) {
		return Style.EMPTY.withColor(TextColor.fromRgb(getSkillColor(skill)));
	}
	
	public double getMobModifier(ResourceLocation locationID, ResourceLocation mobID, String modifierID) {
		return mobModifierData
				.computeIfAbsent(locationID, s -> new HashMap<>())
				.computeIfAbsent(mobID, s -> new HashMap<>())
				.getOrDefault(modifierID, 0d);
	}
	
	public Map<ResourceLocation, Map<String, Double>> getMobModifierMap(ResourceLocation locationID) {
		return mobModifierData.getOrDefault(locationID, new HashMap<>());
	}
	
	public PlayerData getPlayerData(UUID playerID) {
		return playerSpecificSettings.getOrDefault(playerID, PlayerData.getDefault());
	}
	
	public List<MobEffectInstance> getLocationEffect(boolean isPositive, ResourceLocation biomeID) {
		List<MobEffectInstance> effects = new ArrayList<>();
		for (Map.Entry<ResourceLocation, Integer> effect 
				: locationEffectData.getOrDefault(isPositive, new HashMap<>()).getOrDefault(biomeID, new HashMap<>()).entrySet()) {
			MobEffect effectRoot = ForgeRegistries.MOB_EFFECTS.getValue(effect.getKey());
			effects.add(new MobEffectInstance(effectRoot, 75, effect.getValue(), true, true));
		}
		return effects; 
	}
	
	public List<MobEffectInstance> getItemEffect(ResourceLocation itemID) {
		List<MobEffectInstance> effects = new ArrayList<>();
		Map<ResourceLocation, Integer> effectSettings = reqEffectData.getOrDefault(itemID, AutoValueConfig.ITEM_PENALTIES.get());
		for (Map.Entry<ResourceLocation, Integer> effect : effectSettings.entrySet()) {
			MobEffect effectRoot = ForgeRegistries.MOB_EFFECTS.getValue(effect.getKey());
			effects.add(new MobEffectInstance(effectRoot, 75, effect.getValue()));
		}
		return effects;
	}
	
	public List<ResourceLocation> getVeinBlacklist(ResourceLocation locationID) {
		return veinBlacklistData.get(locationID);
	}
	
	@SuppressWarnings("deprecation")
	public boolean isBlockVeinBlacklisted(ResourceLocation locationID, Block block) {
		return veinBlacklistData.get(locationID).contains(block.builtInRegistryHolder().unwrapKey().get().location());
	}
}
