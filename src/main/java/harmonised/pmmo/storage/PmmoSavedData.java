package harmonised.pmmo.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.features.loot_modifiers.SkillUpTrigger;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PmmoSavedData extends SavedData implements IDataStorage{
	
	private static String NAME = Reference.MOD_ID;
	
	private Map<UUID, Map<String, Long>> xp = new HashMap<>();
	private Map<UUID, Map<String, Long>> scheduledXP = new HashMap<>();
	
	private static final Codec<Map<UUID, Map<String, Long>>> XP_CODEC = 
			Codec.unboundedMap(CodecTypes.UUID_CODEC, 
					Codec.unboundedMap(Codec.STRING, Codec.LONG)
						.xmap(map -> new HashMap<>(map), map -> new HashMap<>(map)));
	
	//===========================GETTERS AND SETTERS================
	@Override
	public long getXpRaw(UUID playerID, String skillName) {
		return xp.computeIfAbsent(playerID, s -> new HashMap<>()).getOrDefault(skillName, 0l);
	}
	@Override
	public boolean setXpDiff(UUID playerID, String skillName, long change) {
		long oldValue = getXpRaw(playerID, skillName);
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);
		
		//if player is not online, schedule the XP for player join.
		if (player == null) {
			scheduledXP.computeIfAbsent(playerID, (id) -> new HashMap<>()).merge(skillName, change, (o, n) -> o + n);
			return true;
		}
		//If player is online proceed with xp events, perks and committing xp to master map
		XpEvent gainXpEvent = new XpEvent(player, skillName, oldValue, change, TagBuilder.start().build());
		if (MinecraftForge.EVENT_BUS.post(gainXpEvent))
			return false;

		setXpRaw(playerID, gainXpEvent.skill, oldValue + gainXpEvent.amountAwarded);
		return true;
	}
	@Override
	public void setXpRaw(UUID playerID, String skillName, long value) {
		long formerRaw = getLevelFromXP(getXpRaw(playerID, skillName));
		xp.computeIfAbsent(playerID, s -> new HashMap<>()).put(skillName, value);
		this.setDirty();
		ServerPlayer player;
		if ((player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID)) != null) {
			Networking.sendToClient(new CP_UpdateExperience(skillName, value), player);
			MsLoggy.DEBUG.log(LOG_CODE.XP, "Skill Update Packet sent to Client"+playerID.toString());
			//capture command cases for XP gain which should prompt a skillup event
			if (formerRaw != getLevelFromXP(value)) {
				SkillUpTrigger.SKILL_UP.trigger(player);
				Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
					TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skillName).build(), LogicalSide.SERVER);
			}
		}
	}
	@Override
	public Map<String, Long> getXpMap(UUID playerID) {
		return xp.getOrDefault(playerID, new HashMap<>());
	}
	@Override
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		xp.put(playerID, map != null ? map : new HashMap<>());
		this.setDirty();
	}
	@Override
	public int getPlayerSkillLevel(String skill, UUID player) {
		int rawLevel = Core.get(LogicalSide.SERVER).getLevelProvider().process(skill, getLevelFromXP(getXpRaw(player, skill)));
		int skillMaxLevel = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault()).getMaxLevel();
		return Math.min(rawLevel, skillMaxLevel);
	}
	@Override
	public void setPlayerSkillLevel(String skill, UUID player, int level) {
		long xpRaw = level > 0 ? levelCache.get(level-1) : 0;
		setXpRaw(player, skill, xpRaw);
	}
	@Override
	public boolean changePlayerSkillLevel(String skill, UUID playerID, int change) {
		int currentLevel = getPlayerSkillLevel(skill, playerID);
		long oldXp = getXpRaw(playerID, skill);
		long newXp = (currentLevel - 1 + change) >= 0 ? levelCache.get(currentLevel + change - 1) : 0L;
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);	
			
		if (player != null) {
			XpEvent gainXpEvent = new XpEvent(player, skill, oldXp, newXp - oldXp, TagBuilder.start().build());
			if (MinecraftForge.EVENT_BUS.post(gainXpEvent))
				return false;
			
			if (gainXpEvent.isLevelUp()) 
				Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
						TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skill).build(), LogicalSide.SERVER);
			setPlayerSkillLevel(gainXpEvent.skill, playerID, gainXpEvent.endLevel());
		}
		else 
			setPlayerSkillLevel(skill, playerID, currentLevel + change);
		return true;
	}
	@Override
	public long getBaseXpForLevel(int level) {
		return level > 0 && (level -1) < levelCache.size() ? levelCache.get(level - 1) : 0l;
	}
	//===========================CORE WSD LOGIC=====================
	public PmmoSavedData() {}
	
	private static final String XP_KEY = "xp_data";
	private static final String SCHEDULED_KEY = "scheduled_xp";
	
	public PmmoSavedData(CompoundTag nbt) {
		xp = new HashMap<>(XP_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(XP_KEY)).result().orElse(new HashMap<>()));
		scheduledXP = new HashMap<>(XP_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(SCHEDULED_KEY)).result().orElse(new HashMap<>()));
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt.put(XP_KEY, ((CompoundTag)(XP_CODEC.encodeStart(NbtOps.INSTANCE, xp).result().orElse(new CompoundTag()))));
		nbt.put(SCHEDULED_KEY, ((CompoundTag)(XP_CODEC.encodeStart(NbtOps.INSTANCE, scheduledXP).result().orElse(new CompoundTag()))));
		return nbt;
	}
	
	@Override
	public IDataStorage get() { 
		if (ServerLifecycleHooks.getCurrentServer() != null)
			return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(PmmoSavedData::new, PmmoSavedData::new, NAME);
		else
			return new PmmoSavedData();
    }

	//============================UTILITY METHODS===========================
	@Override
	public int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return Core.get(LogicalSide.SERVER).getLevelProvider().process("", i);
		}
		return Config.MAX_LEVEL.get();
	}	
	
	
	private List<Long> levelCache = new ArrayList<>();
	
	public List<Long> getLevelCache() {return levelCache;}
	
	public void computeLevelsForCache() {
		if (Config.STATIC_LEVELS.get().size() > 0 && Config.STATIC_LEVELS.get().get(0) != -1) {
			List<Long> values = new ArrayList<>(Config.STATIC_LEVELS.get());
			boolean validList = true;
			//Iterate through the list and ensure all values are greater than their preceding value.
			for (int i = 1; i < values.size(); i++) {
				if (values.get(i) <= values.get(i-1)) {
					validList = false;
					break;
				}					
			}
			//If all values are valid, set the cache and exit the function
			if (validList) {
				Config.MAX_LEVEL.set(values.size());
				levelCache = values;
				return;
			}
		}
		
		boolean exponential = Config.USE_EXPONENTIAL_FORMULA.get();
		
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
			if (current >= Long.MAX_VALUE) {
				Config.MAX_LEVEL.set(i-1);
				break;
			}
			levelCache.add(current);
		}
		for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			Networking.sendToClient(new CP_UpdateLevelCache(levelCache), player);
		}
	}
	
	public void awardScheduledXP(UUID playerID) {
		//Clone the original scheduled XP so that we can remove the original
		//This is vital because a disconnect while this is running would result in 
		//the player having their scheduledXP rescheduled, and we cannot modify
		//a collection whilst iterating over it.
		Map<String, Long> queue = new HashMap<>(scheduledXP.getOrDefault(playerID, new HashMap<>()));
		scheduledXP.remove(playerID);
		for (Map.Entry<String, Long> scheduledValue : queue.entrySet()) {
			setXpDiff(playerID, scheduledValue.getKey(), scheduledValue.getValue());
		}
	}
}
