package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;

public class DataConfig {
	private static Map<ResourceLocation, Map<ResourceLocation, Map<String, Double>>> mobModifierData = new HashMap<>();
	private static Map<CoreType, Map<ResourceLocation, Map<ResourceLocation, Integer>>> locationEffectData = new HashMap<>();
	private static LinkedListMultimap<ResourceLocation, ResourceLocation> veinBlacklistData = LinkedListMultimap.create();
	
	//==================DATA SETTER METHODS==============================
	public static void setMobModifierData(ResourceLocation locationID, ResourceLocation mobID, Map<String, Double> data) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(mobID);
		Preconditions.checkNotNull(data);
		mobModifierData.computeIfAbsent(locationID, s -> new HashMap<>()).put(mobID, data);
	}
	
	public static void setLocationEffectData(CoreType coreType, ResourceLocation objectID, Map<ResourceLocation, Integer> dataMap) {
		Preconditions.checkNotNull(coreType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(dataMap);
		locationEffectData.computeIfAbsent(coreType, s -> new HashMap<>()).put(objectID, dataMap);
	}
	
	public static void setArrayData(ResourceLocation locationID, List<ResourceLocation> blockIDs) {
		Preconditions.checkNotNull(locationID);
		Preconditions.checkNotNull(blockIDs);
		veinBlacklistData.putAll(locationID, blockIDs);
	}
	
	//==================UTILITY METHODS==============================
	private static final String COLOR_KEY = "color";
	
	public static int getSkillColor(String skill) {
		JsonElement colorElement = mobModifierData.computeIfAbsent(CoreType.SKILLS,s -> new HashMap<>())
				.getOrDefault(new ResourceLocation(skill),new JsonObject())
				.get(COLOR_KEY);
		if (colorElement != null) {
			try {int out = colorElement.getAsInt();
				return out;
			}
			catch(Exception e) {
				MsLoggy.error("Color key for "+skill+" is incorrectly defined. Default used");
			}
		}
		return 16777215;
	}
}
