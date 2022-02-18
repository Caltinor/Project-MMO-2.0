package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.MsLoggy;

/**This class serves as a run-time cache of data that
 * PmmoSavedData typicaly stores.  
 * 
 * @author Caltinor
 *
 */
public class DataMirror {
	//TODO setup an xp queue to be used in displaying xp growth in the guis
	private static Map<String, Long> mySkills = new HashMap<>();
	private static List<Long> levelCache = new ArrayList<>();
	
	public static void setLevelCache(List<Long> cache) {levelCache = cache;}
	public static void setExperience(String skill, long amount) {
		mySkills.put(skill, amount);
		MsLoggy.debug("Client Side Skill Map: "+MsLoggy.mapToString(mySkills));
	}
	
	public static long getXpForSkill(String skill) {
		return mySkills.getOrDefault(skill, 0l);
	}
	
	public static Map<String, Long> getSkillMap() {
		return mySkills;
	}
	
	public static int getSkillLevel(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	
	public static double getXpWithPercentToNextLevel(long rawXP) {
		int currentLevel = getSkillLevel(rawXP);
		long currentXPThreshold = currentLevel - 1 >= 0 ? levelCache.get(currentLevel - 1) : 0;
		long xpToNextLevel = levelCache.get(currentLevel) - currentXPThreshold;
		long progress = rawXP - currentXPThreshold;
		return (double)currentLevel + (double)progress/(double)xpToNextLevel;
	}

}
