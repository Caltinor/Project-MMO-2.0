package harmonised.pmmo.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.setup.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;

public class PmmoSavedData extends SavedData{
	
	private static MinecraftServer server;
	private static String NAME = Reference.MOD_ID;
	
	private static Map<UUID, Map<String, Long>> xp = new HashMap<>();
	
	//===========================GETTERS AND SETTERS================
	public long getXpRaw(UUID playerID, String skillName) {
		return xp.computeIfAbsent(playerID, s -> new HashMap<>()).getOrDefault(skillName, 0l);
	}
	
	public boolean setXpDiff(UUID playerID, String skillName, long change) {
		long oldValue = getXpRaw(playerID, skillName);
		ServerPlayer player = server.getPlayerList().getPlayer(playerID);
		
		XpEvent levelUpEvent = new XpEvent(player, skillName, oldValue, change, TagBuilder.start().build());
		if (MinecraftForge.EVENT_BUS.post(levelUpEvent))
			return false;
		
		if (levelUpEvent.isLevelUp()) 
			Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
					TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skillName).build());
		setXpRaw(playerID, levelUpEvent.skill, oldValue + levelUpEvent.amountAwarded);
		return true;
	}
	
	public void setXpRaw(UUID playerID, String skillName, long value) {
		xp.computeIfAbsent(playerID, s -> new HashMap<>()).put(skillName, value);
		this.setDirty();
		if (server.getPlayerList().getPlayer(playerID) != null) {
			Networking.sendToClient(new CP_UpdateExperience(skillName, value), server.getPlayerList().getPlayer(playerID));
			MsLoggy.debug("Skill Update Packet sent to Client"+playerID.toString());
		}
	}
	
	public Map<String, Long> getXpMap(UUID playerID) {
		return xp.getOrDefault(playerID, new HashMap<>());
	}
	
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		xp.put(playerID, map != null ? map : new HashMap<>());
		this.setDirty();
	}
	
	public int getPlayerSkillLevel(String skill, UUID player) {
		return getLevelFromXP(getXpRaw(player, skill));
	}
	//===========================CORE WSD LOGIC=====================
	public PmmoSavedData() {}
	
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
	
	public static void init(MinecraftServer server) {
        PmmoSavedData.server = server;
    }
	
	public static PmmoSavedData get() {  //Only available on Server Side, after the Server has Started.
		//TODO maybe throw an exception if null?
        return server.overworld().getDataStorage().computeIfAbsent(PmmoSavedData::new, PmmoSavedData::new, NAME);
    }
	
	public static MinecraftServer getServer() {
		//TODO maybe throw an exception if null?
		return server;
	}

	//============================UTILITY METHODS===========================
	public int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}	
	
	private List<Long> levelCache = new ArrayList<>();
	
	public List<Long> getLevelCache() {return levelCache;}
	
	public void computeLevelsForCache() {
		boolean exponential = Config.USE_EXPONENTIAL_FORUMULA.get();
		
		long linearBase = Config.LINEAR_BASE_XP.get();
		double linearPer = Config.LINEAR_PER_LEVEL.get();
		
		int exponentialBase = Config.EXPONENTIAL_BASE_XP.get();
		double exponentialRoot = Config.EXPONENTIAL_POWER_BASE.get();
		double exponentialRate = Config.EXPONENTIAL_LEVEL_MOD.get();
		
		long current = 0;
		for (int i = 1; i <= Config.MAX_LEVEL.get(); i++) {
			current += exponential?
					exponentialBase * Math.pow(exponentialRoot, exponentialRate * (i)) :
					linearBase + (i) * linearPer;
			levelCache.add(current);
		}
		for (ServerPlayer player : PmmoSavedData.getServer().getPlayerList().getPlayers()) {
			Networking.sendToClient(new CP_UpdateLevelCache(levelCache), player);
		}
	}
}
