package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.setup.Reference;
import net.minecraft.nbt.CompoundTag;
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
		return xp.getOrDefault(playerID, new HashMap<>()).getOrDefault(skillName, 0l);
	}
	
	public void setXpRaw(UUID playerID, String skillName, long value) {
		xp.getOrDefault(playerID, new HashMap<>()).put(skillName, value);
	}
	
	public Map<String, Long> getXpMap(UUID playerID) {
		return xp.getOrDefault(playerID, new HashMap<>());
	}
	
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		xp.put(playerID, map != null ? map : new HashMap<>());
	}
	//===========================CORE WSD LOGIC=====================
	public PmmoSavedData() {super();}
	
	public PmmoSavedData(CompoundTag nbt) {
		//TODO build WSD load logic
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		// TODO build WSD save logic
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
