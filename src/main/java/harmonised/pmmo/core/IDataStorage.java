package harmonised.pmmo.core;

import java.util.Map;
import java.util.UUID;

public interface IDataStorage {
	public long getXpRaw(UUID playerID, String skillName);
	public default boolean setXpDiff(UUID playerID, String skillName, long change) {return false;}
	public void setXpRaw(UUID playerID, String skillName, long value);
	public Map<String, Long> getXpMap(UUID playerID);
	public void setXpMap(UUID playerID, Map<String, Long> map);
	public int getPlayerSkillLevel(String skill, UUID player);
	public default void setPlayerSkillLevel(String skill, UUID player, int level) {}
	public default boolean changePlayerSkillLevel(String skill, UUID playerID, int change) {return false;}
	public int getLevelFromXP(long xp);
	public IDataStorage get();
	public default void computeLevelsForCache() {}
	public long getBaseXpForLevel(int level);
}
