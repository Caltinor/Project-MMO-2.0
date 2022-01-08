package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.readers.XpValueDataType;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.resources.ResourceLocation;

public class XpUtils {
	
	private static Map<EventType, Map<ResourceLocation, Map<String, Long>>> xpGainData = new HashMap<>();
	private static Map<XpValueDataType, Map<ResourceLocation, Map<String, Double>>> xpModifierData = new HashMap<>();
	
	//===================XP INTERACTION METHODS=======================================
	public static void setXpDiff(UUID playerID, String skillName, long change) {
		long newValue = PmmoSavedData.get().getXpRaw(playerID, skillName) + change;
		PmmoSavedData.get().setXpRaw(playerID, skillName, newValue);
	}
	
	public static long getPlayerXpRaw(UUID playerID, String skill) {
		return PmmoSavedData.get().getXpRaw(playerID, skill);
	}
	
	public static Map<String, Long> getObjectExperienceMap(EventType EventType, ResourceLocation objectID) {
		return xpGainData.computeIfAbsent(EventType, s -> new HashMap<>()).getOrDefault(objectID, new HashMap<>());
	}
	
	public static void setObjectXpGainMap(EventType eventType, ResourceLocation objectID, Map<String, Long> xpMap) {
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpGainData.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	public static void setObjectXpModifierMap(XpValueDataType XpValueDataType, ResourceLocation objectID, Map<String, Double> xpMap) {
		Preconditions.checkNotNull(XpValueDataType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpModifierData.computeIfAbsent(XpValueDataType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	//====================UTILITY METHODS==============================================
	public static int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	//====================LOGICAL METHODS==============================================
	
	private static List<Long> levelCache = new ArrayList<>();
	
	public static void computeLevelsForCache() {
		boolean exponential = Config.USE_EXPONENTIAL_FORUMULA.get();
		
		long linearBase = Config.LINEAR_BASE_XP.get();
		double linearPer = Config.LINEAR_PER_LEVEL.get();
		
		int exponentialBase = Config.EXPONENTIAL_BASE_XP.get();
		double exponentialRoot = Config.EXPONENTIAL_POWER_BASE.get();
		double exponentialRate = Config.EXPONENTIAL_LEVEL_MOD.get();
		
		long current = 0;
		for (int i = 1; i <= Config.MAX_LEVEL.get(); i++) {
			current += exponential?
					exponentialBase * Math.pow(exponentialRoot, exponentialRate * (i)) :
					linearBase + (i) * linearPer;
			levelCache.add(current);
		}
	}
}
