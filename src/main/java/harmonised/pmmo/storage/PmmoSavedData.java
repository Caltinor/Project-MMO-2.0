package harmonised.pmmo.storage;

import com.mojang.serialization.Codec;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.features.loot_modifiers.SkillUpTrigger;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PmmoSavedData extends SavedData implements IDataStorage{
	
	private static final String NAME = Reference.MOD_ID;
	
	private Map<UUID, Map<String, Experience>> xp = new HashMap<>();
	private Map<UUID, Map<String, Experience>> scheduledXP = new HashMap<>();
	
	private static final Codec<Map<UUID, Map<String, Experience>>> XP_CODEC =
			Codec.unboundedMap(CodecTypes.UUID_CODEC, 
					Codec.unboundedMap(Codec.STRING, Experience.CODEC)
						.xmap(HashMap::new, HashMap::new));
	
	//===========================GETTERS AND SETTERS================
	@Override
	public long getXp(UUID playerID, String skillName) {
		return xp.getOrDefault(playerID, new HashMap<>()).getOrDefault(skillName, new Experience()).getXp();
	}
	@Override
	public void addXp(UUID playerID, String skillName, long change) {
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);

		//if player is not online, schedule the XP for player join.
		if (player == null) {
			scheduledXP.computeIfAbsent(playerID, (id) -> new HashMap<>()).merge(skillName, new Experience(change), Experience::merge);
			return;
		}
		//If player is online proceed with xp events, perks and committing xp to master map
		XpEvent gainXpEvent = new XpEvent(player, skillName
				, Core.get(player.level()).getData().getXpMap(playerID).getOrDefault(skillName, new Experience())
				, change, TagBuilder.start().build());
		if (NeoForge.EVENT_BUS.post(gainXpEvent).isCanceled())
			return;
		if (xp.computeIfAbsent(playerID, i -> new HashMap<>())
				.computeIfAbsent(gainXpEvent.skill, s -> new Experience())
				.addXp(gainXpEvent.amountAwarded)) {
			SkillUpTrigger.SKILL_UP.trigger(player);
			Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
					TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skillName).build());
		}
		this.setDirty();
		Networking.sendToClient(new CP_UpdateExperience(skillName, xp.get(playerID).get(skillName), gainXpEvent.amountAwarded), player);
	}
	@Override
	public void setXp(UUID playerID, String skillName, long value) {
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);
		if (xp.computeIfAbsent(playerID, i -> new HashMap<>()).computeIfAbsent(skillName, s -> new Experience()).setXp(value)
			&& player != null) {
			MsLoggy.DEBUG.log(LOG_CODE.XP, "Skill Update Packet sent to Client"+playerID.toString());
			SkillUpTrigger.SKILL_UP.trigger(player);
			Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
					TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skillName).build());
		}
		this.setDirty();
		if (player != null)
			Networking.sendToClient(new CP_UpdateExperience(skillName, xp.get(playerID).get(skillName), 0), player);
	}
	@Override
	public Map<String, Experience> getXpMap(UUID playerID) {
		return xp.getOrDefault(playerID, new HashMap<>());
	}
	@Override
	public void setXpMap(UUID playerID, Map<String, Experience> map) {
		xp.put(playerID, map != null ? map : new HashMap<>());
		this.setDirty();
	}
	@Override
	public long getLevel(String skill, UUID player) {
		long rawLevel = Core.get(LogicalSide.SERVER).getLevelProvider().process(skill
				,getXpMap(player).getOrDefault(skill, new Experience()).getLevel().getLevel());
		long skillMaxLevel = Config.skills().get(skill).getMaxLevel();
		return Math.min(rawLevel, skillMaxLevel);
	}
	@Override
	public void setLevel(String skill, UUID playerID, long level) {
		xp.computeIfAbsent(playerID, p -> new HashMap<>())
				.computeIfAbsent(skill, s -> new Experience())
				.setLevel(level > 0 ? level: 0);
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);
		if (player != null)
			Networking.sendToClient(new CP_UpdateExperience(skill, xp.get(playerID).get(skill), 0), player);
	}
	@Override
	public void addLevel(String skill, UUID playerID, long change) {
		xp.computeIfAbsent(playerID, p -> new HashMap<>())
				.computeIfAbsent(skill, s -> new Experience())
				.addLevel(change);
		ServerPlayer player  = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerID);
		if (player != null) {
			if (change > 0)
				Core.get(LogicalSide.SERVER).getPerkRegistry().executePerk(EventType.SKILL_UP, player,
						TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, skill).build());
			Networking.sendToClient(new CP_UpdateExperience(skill, xp.get(playerID).get(skill), 0), player);
		}
	}
	//===========================CORE WSD LOGIC=====================
	public PmmoSavedData() {}
	
	private static final String XP_KEY = "xp_data";
	private static final String SCHEDULED_KEY = "scheduled_xp";
	
	public PmmoSavedData(CompoundTag nbt) {
		xp = new HashMap<>(XP_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(XP_KEY)).result().orElse(new HashMap<>()));
		scheduledXP = new HashMap<>(XP_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(SCHEDULED_KEY)).result().orElse(new HashMap<>()));
	}

	public static Factory<PmmoSavedData> dataFactory() {
		return new SavedData.Factory<PmmoSavedData>(PmmoSavedData::new, PmmoSavedData::new, null);
	}

	@Override
	public @NotNull CompoundTag save(CompoundTag nbt) {
		//This filter exists to scrub the data from empty values to reduce file bloat.
		Map<UUID, Map<String, Experience>> cleanXP = xp.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		nbt.put(XP_KEY, XP_CODEC.encodeStart(NbtOps.INSTANCE, cleanXP).result().orElse(new CompoundTag()));
		nbt.put(SCHEDULED_KEY, XP_CODEC.encodeStart(NbtOps.INSTANCE, scheduledXP).result().orElse(new CompoundTag()));
		return nbt;
	}
	
	@Override
	public IDataStorage get() { 
		if (ServerLifecycleHooks.getCurrentServer() != null)
			return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(dataFactory(), NAME);
		else
			return new PmmoSavedData();
    }

	//============================UTILITY METHODS===========================
	public void awardScheduledXP(UUID playerID) {
		//Clone the original scheduled XP so that we can remove the original
		//This is vital because a disconnect while this is running would result in 
		//the player having their scheduledXP rescheduled, and we cannot modify
		//a collection whilst iterating over it.
		Map<String, Experience> queue = new HashMap<>(scheduledXP.getOrDefault(playerID, new HashMap<>()));
		scheduledXP.remove(playerID);
		for (Map.Entry<String, Experience> scheduledValue : queue.entrySet()) {
			addXp(playerID, scheduledValue.getKey(), scheduledValue.getValue().getXp());
			addLevel(scheduledValue.getKey(), playerID, scheduledValue.getValue().getLevel().getLevel());
		}
	}
}
