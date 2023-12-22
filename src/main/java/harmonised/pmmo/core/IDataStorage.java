package harmonised.pmmo.core;

import harmonised.pmmo.storage.Experience;

import java.util.Map;
import java.util.UUID;

public interface IDataStorage {
	public long getXp(UUID playerID, String skillName);
	public default void addXp(UUID playerID, String skillName, long change) {}
	public void setXp(UUID playerID, String skillName, long value);
	public Map<String, Experience> getXpMap(UUID playerID);
	public void setXpMap(UUID playerID, Map<String, Experience> map);
	public long getLevel(String skill, UUID player);
	public default void setLevel(String skill, UUID player, long level) {}
	public default void addLevel(String skill, UUID playerID, long change) {}
	public IDataStorage get();
}
