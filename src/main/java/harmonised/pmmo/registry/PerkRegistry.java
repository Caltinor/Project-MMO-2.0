package harmonised.pmmo.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.LevelTickEvent;

public class PerkRegistry {
	public PerkRegistry() {}

	private final Map<ResourceLocation, Perk> perks = new HashMap<>(); 
	
	public void registerPerk(ResourceLocation perkID, Perk perk) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(perk);
		perks.put(perkID, perk);
		MsLoggy.DEBUG.log(LOG_CODE.API, "Registered Perk: "+perkID.toString());
	}
	
	public void registerClientClone(ResourceLocation perkID, Perk perk) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(perk);
		Perk clientCopy = new Perk(perk.conditions(), perk.propertyDefaults(), 
				(a,b) -> new CompoundTag(), 
				(a,b,c) -> new CompoundTag(), 
				(a,b) -> new CompoundTag(), 
				perk.description(), perk.status());
		perks.putIfAbsent(perkID, clientCopy);
	}
	
	public MutableComponent getDescription(ResourceLocation id) {
		return perks.getOrDefault(id, Perk.empty()).description();
	}
	
	public List<MutableComponent> getStatusLines(ResourceLocation id, Player player, CompoundTag settings) {
		return perks.getOrDefault(id, Perk.empty()).status().apply(player, settings);
	}
	
	public CompoundTag executePerk(EventType cause, Player player, @NotNull CompoundTag dataIn) {
		if (player == null) return new CompoundTag();
		CompoundTag output = new CompoundTag();
		PerksConfig.PERK_SETTINGS.get().getOrDefault(cause, new ArrayList<>()).forEach(src -> {
			ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
			Perk perk = perks.getOrDefault(perkID, Perk.empty());
			src = perk.propertyDefaults().merge(src.merge(dataIn));
			src.putInt(APIUtils.SKILL_LEVEL, src.contains(APIUtils.SKILLNAME) 
					? Core.get(player.level()).getData().getPlayerSkillLevel(src.getString(APIUtils.SKILLNAME), player.getUUID())
					: 0);
			CompoundTag executionOutput = perk.start(player, src);
			tickTracker.add(new TickSchedule(perk, player, src, new AtomicInteger(0)));
			if (src.contains(APIUtils.COOLDOWN))
				coolTracker.add(new PerkCooldown(perkID, player, src, player.level().getGameTime()));
			output.merge(TagUtils.mergeTags(output, executionOutput));
		});
		return output;
	}
	
	private static record TickSchedule(Perk perk, Player player, CompoundTag src, AtomicInteger ticksElapsed) {
		public boolean shouldTick() {
			return src.contains(APIUtils.DURATION) && ticksElapsed.get() <= src.getInt(APIUtils.DURATION);
		}
		
		public void tick() {
			ticksElapsed().getAndIncrement();
			perk.tick(player, src, ticksElapsed.get());
		}
	}
	private static record PerkCooldown(ResourceLocation perkID, Player player, CompoundTag src, long lastUse) {
		public boolean cooledDown(Level level) {
			return level.getGameTime() > lastUse + src.getInt(APIUtils.COOLDOWN);
		}
	}
	
	private final List<TickSchedule> tickTracker = new ArrayList<>();
	private final List<PerkCooldown> coolTracker = new ArrayList<>();
	
	public void executePerkTicks(LevelTickEvent event) {
		coolTracker.removeIf(tracker -> tracker.cooledDown(event.level));
		new ArrayList<>(tickTracker).forEach(schedule -> {
			if (schedule.shouldTick())
				schedule.tick();
			else
				schedule.perk().stop(schedule.player(), schedule.src());
				tickTracker.remove(schedule);
		});
	}

	public boolean isPerkCooledDown(Player player, CompoundTag src) {
		ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
		return coolTracker.stream().noneMatch(cd -> cd.player().equals(player) && cd.perkID().equals(perkID));
	}
}
