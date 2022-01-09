package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class PmmoSavedData extends SavedData{
	
	private static PmmoSavedData pmmoSavedData;
	private static MinecraftServer server;
	private static String NAME = Reference.MOD_ID;
	
	private static Map<UUID, Map<String, Long>> xp = new HashMap<>();
	
	//===========================GETTERS AND SETTERS================
	public long getXpRaw(UUID playerID, String skillName) {
		return xp.computeIfAbsent(playerID, s -> new HashMap<>()).getOrDefault(skillName, 0l);
	}
	
	public void setXpRaw(UUID playerID, String skillName, long value) {
		xp.computeIfAbsent(playerID, s -> new HashMap<>()).put(skillName, value);
	}
	
	public Map<String, Long> getXpMap(UUID playerID) {
		return xp.getOrDefault(playerID, new HashMap<>());
	}
	
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		xp.put(playerID, map != null ? map : new HashMap<>());
	}
	//===========================CORE WSD LOGIC=====================
	public PmmoSavedData() {super();}
	
	private static final String SKILL_KEY = "skill";
	private static final String VALUE_KEY = "value";
	
	public PmmoSavedData(CompoundTag nbt) {
		for (String uuid : nbt.getAllKeys()) {
			UUID playerID = UUID.fromString(uuid);
			ListTag skillPairs = nbt.getList(uuid, Tag.TAG_COMPOUND);
			Map<String, Long> playerMap = new HashMap<>();
			for (int i = 0; i < skillPairs.size(); i++) {
				String skill = skillPairs.getCompound(i).getString(SKILL_KEY);
				long value = skillPairs.getCompound(i).getLong(VALUE_KEY);
				playerMap.put(skill, value);
			}
			xp.put(playerID, playerMap);
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		for (Map.Entry<UUID, Map<String, Long>> xpMap : xp.entrySet()) {
			ListTag skillPairs = new ListTag();
			for (Map.Entry<String, Long> skills : xpMap.getValue().entrySet()) {
				CompoundTag pair = new CompoundTag();
				pair.putString(SKILL_KEY, skills.getKey());
				pair.putLong(VALUE_KEY, skills.getValue());
				skillPairs.add(pair);
			}
			nbt.put(xpMap.getKey().toString(), skillPairs);
		}
		return nbt;
	}
	
	public static void init(MinecraftServer server)
    {
        PmmoSavedData.server = server;
        PmmoSavedData.pmmoSavedData = server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PmmoSavedData::new, PmmoSavedData::new, NAME);
    }
	
	public static PmmoSavedData get()   //Only available on Server Side, after the Server has Started.
    {
		//TODO maybe throw an exception if null?
        return pmmoSavedData;
    }
	
	public static MinecraftServer getServer() {
		//TODO maybe throw an exception if null?
		return server;
	}

}
