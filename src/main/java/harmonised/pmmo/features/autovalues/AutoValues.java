package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AutoValues {
	//============================DATA CACHES==============================================================
	private static Map<ReqType, Map<ResourceLocation, Map<String, Integer>>> reqValues = new HashMap<>();
	private static Map<EventType, Map<ResourceLocation, Map<String, Long>>> xpGainValues = new HashMap<>();
	
	//============================CACHE GETTERS============================================================	
	public static void cacheRequirement(ReqType reqType, ResourceLocation objectID, Map<String, Integer> requirementMap) {
		reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).put(objectID, requirementMap);
	}
	
	public static void cacheXpGainValue(EventType eventType, ResourceLocation objectID, Map<String, Long> xpGainMap) {
		xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpGainMap);
	}

	//============================AUTO VALUE GETTERS=======================================================
	public static Map<String, Integer> getRequirements(ReqType reqType, BlockState state) {
		ResourceLocation blockID = state.getBlock().getRegistryName();
		//Check the cache for an existing calculation
		if (reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).containsKey(blockID))
			return reqValues.get(reqType).get(blockID);
		
		Map<String, Integer> requirements = new HashMap<>();
		//TODO calculate a new requirement
		cacheRequirement(reqType, blockID, requirements);
		return requirements;
	}
	
	public static Map<String, Integer> getRequirements(ReqType reqType, Entity entity) {
		ResourceLocation entityID = new ResourceLocation(entity.getEncodeId());
		//Check the cache for an existing calculation
		if (reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).containsKey(entityID))
			return reqValues.get(reqType).get(entityID);

		Map<String, Integer> requirements = new HashMap<>();
		//TODO calculate a new requirement
		cacheRequirement(reqType, entityID, requirements);
		return requirements;
	}

	public static Map<String, Integer> getRequirements(ReqType reqType, ItemStack stack) {
		ResourceLocation itemID = stack.getItem().getRegistryName();
		//Check the cache for an existing calculation
		if (reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).containsKey(itemID))
			return reqValues.get(reqType).get(itemID);

		Map<String, Integer> requirements = new HashMap<>();
		//TODO calculate a new requirement
		cacheRequirement(reqType, itemID, requirements);
		return requirements;
	}
	
	public static Map<String, Long> getExperienceAward(EventType eventType, BlockState state) {
		ResourceLocation blockID = state.getBlock().getRegistryName();
		//Check the cache for an existing calculation
		if (xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).containsKey(blockID))
			return xpGainValues.get(eventType).get(blockID);

		Map<String, Long> awards = new HashMap<>();
		//TODO calculate new xp gains
		cacheXpGainValue(eventType, blockID, awards);
		return awards;
	}
	
	public static Map<String, Long> getExperienceAward(EventType eventType, Entity entity) {
		ResourceLocation entityID = new ResourceLocation(entity.getEncodeId());
		//Check the cache for an existing calculation
		if (xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).containsKey(entityID))
			return xpGainValues.get(eventType).get(entityID);

		Map<String, Long> awards = new HashMap<>();
		//TODO calculate new xp gains
		cacheXpGainValue(eventType, entityID, awards);
		return awards;
	}
	
	public static Map<String, Long> getExperienceAward(EventType eventType, ItemStack stack) {
		ResourceLocation itemID = stack.getItem().getRegistryName();
		//Check the cache for an existing calculation
		if (xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).containsKey(itemID))
			xpGainValues.get(eventType).get(itemID);

		Map<String, Long> awards = new HashMap<>();
		//TODO calculate new xp gains
		cacheXpGainValue(eventType, itemID, awards);
		return awards;
	}
}
