package harmonised.pmmo.core;

import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;

public interface IDataStorage {
	public long getXpRaw(UUID playerID, String skillName);
	public boolean setXpDiff(UUID playerID, String skillName, long change);
	public void setXpRaw(UUID playerID, String skillName, long value);
	public Map<String, Long> getXpMap(UUID playerID);
	public void setXpMap(UUID playerID, Map<String, Long> map);
	public int getPlayerSkillLevel(String skill, UUID player);
	public void setPlayerSkillLevel(String skill, UUID player, int level);
	public boolean changePlayerSkillLevel(String skill, UUID playerID, int change);
	public int getLevelFromXP(long xp);
	public IDataStorage get();
	public IDataStorage get(MinecraftServer server);
	public void computeLevelsForCache();
}
