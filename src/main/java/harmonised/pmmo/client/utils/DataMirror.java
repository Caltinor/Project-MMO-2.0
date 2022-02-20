package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.server.MinecraftServer;

/**This class serves as a run-time cache of data that
 * PmmoSavedData typicaly stores.  
 * 
 * Deprecated interface methods are labeled such as they
 * should not be called from the client.  They are logical
 * methods used on the server
 * 
 * @author Caltinor
 *
 */
public class DataMirror implements IDataStorage{
	public DataMirror() {}
	//TODO setup an xp queue to be used in displaying xp growth in the guis
	private Map<String, Long> mySkills = new HashMap<>();
	private List<Long> levelCache = new ArrayList<>();
	
	public void setLevelCache(List<Long> cache) {levelCache = cache;}
	
	@Override
	public int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	
	public double getXpWithPercentToNextLevel(long rawXP) {
		int currentLevel = getLevelFromXP(rawXP);
		long currentXPThreshold = currentLevel - 1 >= 0 ? levelCache.get(currentLevel - 1) : 0;
		long xpToNextLevel = levelCache.get(currentLevel) - currentXPThreshold;
		long progress = rawXP - currentXPThreshold;
		return (double)currentLevel + (double)progress/(double)xpToNextLevel;
	}
	
	@Override
	public long getXpRaw(UUID playerID, String skillName) {	return mySkills.getOrDefault(skillName, 0l);}
	@Deprecated
	@Override
	public boolean setXpDiff(UUID playerID, String skillName, long change) {return false;}
	@Override
	public void setXpRaw(UUID playerID, String skillName, long value) {
		mySkills.put(skillName, value);
		MsLoggy.debug("Client Side Skill Map: "+MsLoggy.mapToString(mySkills));		
	}
	@Override
	public Map<String, Long> getXpMap(UUID playerID) {return mySkills;}
	@Override
	public void setXpMap(UUID playerID, Map<String, Long> map) {mySkills = map;}
	@Override
	public int getPlayerSkillLevel(String skill, UUID player) {return getLevelFromXP(mySkills.getOrDefault(skill, 0l));}
	@Deprecated
	@Override
	public void setPlayerSkillLevel(String skill, UUID player, int level) {}
	@Deprecated
	@Override
	public boolean changePlayerSkillLevel(String skill, UUID playerID, int change) {return false;}
	@Deprecated
	@Override
	public IDataStorage get(MinecraftServer server) {return this;}
	@Override
	public IDataStorage get() {return this;}
	@Deprecated
	@Override
	public void computeLevelsForCache() {}
}
