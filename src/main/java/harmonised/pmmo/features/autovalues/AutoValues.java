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
	private static Map<String, Integer> cacheRequirement(ReqType reqType, ResourceLocation objectID, Map<String, Integer> requirementMap) {
		reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).put(objectID, requirementMap);
		return requirementMap;
	}
	
	private static Map<String, Long> cacheXpGainValue(EventType eventType, ResourceLocation objectID, Map<String, Long> xpGainMap) {
		xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpGainMap);
		return xpGainMap;
	}
	
	public static void resetCache() {
		reqValues = new ConcurrentHashMap<>();
		xpGainValues = new ConcurrentHashMap<>();
	}

	//============================AUTO VALUE GETTERS=======================================================
	public static Map<String, Integer> getRequirements(ReqType reqType, ResourceLocation objectID, ObjectType autoValueType) {
		//ignore processing if individual req is disabled and clear the cache for this object
		if (!AutoValueConfig.isReqEnabled(reqType)) {
			reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).remove(objectID);
			return new HashMap<>();
		}
		
		Map<String, Integer> requirements = new HashMap<>();
		//Check the cache for an existing calculation
		if (reqValues.computeIfAbsent(reqType, s -> new HashMap<>()).containsKey(objectID) && !(requirements = new HashMap<>(reqValues.get(reqType).get(objectID))).isEmpty())
			return requirements;
				
		requirements = switch (autoValueType) {
		case ITEM -> AutoItem.processReqs(reqType, objectID);
		case BLOCK -> AutoBlock.processReqs(reqType, objectID);
		case ENTITY -> AutoEntity.processReqs(reqType, objectID);
		default -> requirements;
		};
		Map<String, Integer> finalReqs = new HashMap<>();
		requirements.forEach((skill, level) -> {
			if (level > 0)
				finalReqs.put(skill, level);
		});
		return cacheRequirement(reqType, objectID, finalReqs);
	}
	
	public static Map<String, Long> getExperienceAward(EventType eventType, ResourceLocation objectID, ObjectType autoValueType) {
		//ignore processing if individual event is disabled and clear existing data
		if (!AutoValueConfig.isXpGainEnabled(eventType)) {
			xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).remove(objectID);
			return new HashMap<>();
		}
		Map<String, Long> awards = new HashMap<>();
		//Check the cache for an existing calculation
		if (xpGainValues.computeIfAbsent(eventType, s -> new HashMap<>()).containsKey(objectID) && !(awards = new HashMap<>(xpGainValues.get(eventType).get(objectID))).isEmpty())
			return awards;

		awards = switch (autoValueType) {
		case ITEM -> AutoItem.processXpGains(eventType, objectID);
		case BLOCK -> AutoBlock.processXpGains(eventType, objectID);
		case ENTITY -> AutoEntity.processXpGains(eventType, objectID);
		default -> awards;
		};
		Map<String, Long> finalAwards = new HashMap<>();
		awards.forEach((skill, value) -> {
			if (value > 0)
				finalAwards.put(skill, value);
		});
		return cacheXpGainValue(eventType, objectID, finalAwards);
	}
	
}
