package harmonised.pmmo.client.utils;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.LogicalSide;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	
	private Map<String, Experience> mySkills = new HashMap<>();
	private Map<String, Experience> otherSkills = new HashMap<>();
	
	public double getXpWithPercentToNextLevel(Experience rawXP) {
		return  ((double)rawXP.getXp()/(double)rawXP.getLevel().getXpToNext()) + (double)rawXP.getLevel().getLevel();
	}
	
	@Override
	public long getXp(UUID playerID, String skillName) {
		return me(playerID) ? mySkills.getOrDefault(skillName, new Experience()).getXp() : otherSkills.getOrDefault(skillName, new Experience()).getXp();
	}
	@Override
	public void setXp(UUID playerID, String skillName, long value) {
		if (!me(playerID)) return;
		mySkills.computeIfAbsent(skillName, s -> new Experience()).setXp(value);
		MsLoggy.DEBUG.log(LOG_CODE.XP,"Client Side Skill Map: "+MsLoggy.mapToString(mySkills));		
	}
	@Override
	public Map<String, Experience> getXpMap(UUID playerID) {return me(playerID) ? mySkills : otherSkills;}
	@Override
	public void setXpMap(UUID playerID, Map<String, Experience> map) {
		if (me(playerID))
			mySkills = map;
		else
			otherSkills = map;
	}
	@Override
	public long getLevel(String skill, UUID player) {
		long rawLevel =  me(player) ? mySkills.getOrDefault(skill, new Experience()).getLevel().getLevel()
			: otherSkills.getOrDefault(skill, new Experience()).getLevel().getLevel();
		rawLevel = Core.get(LogicalSide.CLIENT).getLevelProvider().process(skill, rawLevel);
		long skillMax = Config.skills().get(skill).getMaxLevel();
		return Math.min(rawLevel, skillMax);
	}
	@Override
	public IDataStorage get() {return this;}
}
