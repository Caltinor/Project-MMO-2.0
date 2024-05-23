package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.LogicalSide;

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
	
	public boolean me(UUID id) {return id == null || id.equals(Minecraft.getInstance().player.getUUID());}
	
	private Map<String, Long> mySkills = new HashMap<>();
	private Map<String, Long> otherSkills = new HashMap<>();
	private Map<String, Long> scheduledXp = new HashMap<>();
	private List<Long> levelCache = new ArrayList<>();
	
	public void setLevelCache(List<Long> cache) {levelCache = cache;}
	
	public long getScheduledXp(String skill) {return scheduledXp.getOrDefault(skill, 0l);}
	
	@Override
	public int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (i == Config.MAX_LEVEL.get())
				return i;
			if (levelCache.get(i) > xp)
				return Core.get(LogicalSide.CLIENT).getLevelProvider().process("", i);
		}
		return Config.MAX_LEVEL.get();
	}
	
	private int getLevelFromXPwithoutLevelProvider(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	
	public double getXpWithPercentToNextLevel(long rawXP) {
		int currentLevel = getLevelFromXPwithoutLevelProvider(rawXP);
		currentLevel = currentLevel >= levelCache.size() ? levelCache.size()-1 : currentLevel;
		long currentXPThreshold = currentLevel - 1 >= 0 ? levelCache.get(currentLevel - 1) : 0;
		long xpToNextLevel = levelCache.get(currentLevel) - currentXPThreshold;
		long progress = rawXP - currentXPThreshold;
		return (double)Core.get(LogicalSide.CLIENT).getLevelProvider().process("", currentLevel) + (double)progress/(double)xpToNextLevel;
	}
	
	@Override
	public long getXpRaw(UUID playerID, String skillName) {
		return me(playerID) ? mySkills.getOrDefault(skillName, 0L) : otherSkills.getOrDefault(skillName, 0L);
	}
	@Override
	public void setXpRaw(UUID playerID, String skillName, long value) {
		if (!me(playerID)) return;
		long oldValue = getXpRaw(playerID, skillName);
		if (value > oldValue)
			scheduledXp.merge(skillName, value-oldValue, Long::sum);
		mySkills.put(skillName, value);
		int newLevel = getLevelFromXP(value);
		int oldLevel = getLevelFromXP(oldValue);
		if (oldLevel < newLevel)
			ClientUtils.sendLevelUpUnlocks(skillName, newLevel);
		MsLoggy.DEBUG.log(LOG_CODE.XP,"Client Side Skill Map: "+MsLoggy.mapToString(mySkills));		
	}
	@Override
	public Map<String, Long> getXpMap(UUID playerID) {return me(playerID) ? mySkills : otherSkills;}
	@Override
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		if (me(playerID))
			mySkills = map;
		else
			otherSkills = map;
	}
	@Override
	public int getPlayerSkillLevel(String skill, UUID player) {
		int rawLevel =  me(player) ? getLevelFromXP(mySkills.getOrDefault(skill, 0l)) 
			: getLevelFromXP(otherSkills.getOrDefault(skill, 0l));
		rawLevel = Core.get(LogicalSide.CLIENT).getLevelProvider().process(skill, rawLevel);
		int skillMax = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault()).getMaxLevel();
		return rawLevel > skillMax ? skillMax : rawLevel;
	}
	@Override
	public IDataStorage get() {return this;}
	@Override
	public long getBaseXpForLevel(int level) {return level > 0 && (level -1) < levelCache.size() ? levelCache.get(level - 1) : 0l;}
}
