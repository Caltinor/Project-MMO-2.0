package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.config.Config;

/**This class serves as a run-time cache of data that the
 * client will utiliize for various rendering tasks.
 * 
 * @author Caltinor
 *
 */
public class DataMirror {
	private static Map<String, Long> mySkills = new HashMap<>();
	private static List<Long> levelCache = new ArrayList<>();
	
	public static void setLevelCache(List<Long> cache) {levelCache = cache;}
	public static void setExperience(String skill, long amount) {mySkills.put(skill, amount);}
	
	public static long getXpForSkill(String skill) {
		return mySkills.getOrDefault(skill, 0l);
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
		long currentXPBar = levelCache.get(currentLevel);
		long xpToNextLevel = levelCache.get(currentLevel + 1) - currentXPBar;
		long progress = rawXP - currentXPBar;
		return (double)currentLevel + (double)progress/(double)xpToNextLevel;
	}

}
