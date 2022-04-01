package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;

public class AutoValues {
	//============================DATA CACHES==============================================================
	private static ConcurrentMap<ReqType, Map<ResourceLocation, Map<String, Integer>>> reqValues = new ConcurrentHashMap<>();
	private static ConcurrentMap<EventType, Map<ResourceLocation, Map<String, Long>>> xpGainValues = new ConcurrentHashMap<>();
	
	//============================CACHE GETTERS============================================================	
	private static void cacheRequirement(ReqType reqType, ResourceLocation objectID, Map<String, Integer> requirementMap) {
		reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).put(objectID, requirementMap);
	}
	
	private static void cacheXpGainValue(EventType eventType, ResourceLocation objectID, Map<String, Long> xpGainMap) {
		xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpGainMap);
	}

	//============================AUTO VALUE GETTERS=======================================================
	public static Map<String, Integer> getRequirements(ReqType reqType, ResourceLocation objectID, ObjectType autoValueType) {
		//Check the cache for an existing calculation
		if (reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).containsKey(objectID))
			return reqValues.get(reqType).get(objectID);
		
		Map<String, Integer> requirements = new HashMap<>();
		switch (autoValueType) {
		case ITEM: {
			requirements = AutoItem.processReqs(reqType, objectID);
			break;
		}
		case BLOCK: {
			requirements = AutoBlock.processReqs(reqType, objectID);
			break;
		}
		case ENTITY: {
			requirements = AutoEntity.processReqs(reqType, objectID);
			break;
		}
		default: {}
		}
		cacheRequirement(reqType, objectID, requirements);
		return requirements;
	}
	
	public static Map<String, Long> getExperienceAward(EventType eventType, ResourceLocation objectID, ObjectType autoValueType) {
		//Check the cache for an existing calculation
		if (xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).containsKey(objectID))
			return xpGainValues.get(eventType).get(objectID);

		Map<String, Long> awards = new HashMap<>();
		switch (autoValueType) {
		case ITEM: {
			awards = AutoItem.processXpGains(eventType, objectID);
			break;
		}
		case BLOCK: {
			awards = AutoBlock.processXpGains(eventType, objectID);
			break;
		}
		case ENTITY: {
			awards = AutoEntity.processXpGains(eventType, objectID);
			break;
		}
		default: {}
		}
		cacheXpGainValue(eventType, objectID, awards);
		return awards;
	}
	
}
