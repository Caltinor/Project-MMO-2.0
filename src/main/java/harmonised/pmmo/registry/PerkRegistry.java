package harmonised.pmmo.registry;

import com.google.common.base.Preconditions;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
		Config.perks().perks().getOrDefault(cause, new ArrayList<>()).forEach(src -> {
			output.merge(processPerk(src, output, player, dataIn));
		});
		return output;
	}

	public CompoundTag executePerkFiltered(EventType cause, Player player, String filterTag, String filterValue, @NotNull CompoundTag dataIn) {
		if (player == null) return new CompoundTag();
		CompoundTag output = new CompoundTag();
		Config.perks().perks().getOrDefault(cause, new ArrayList<>()).stream()
		.filter(tag -> tag.getString(filterTag).equals(filterValue)).toList().forEach(src -> {
			output.merge(processPerk(src, output, player, dataIn));
		});
		return output;
	}

	private CompoundTag processPerk(CompoundTag src, CompoundTag output,Player player, @NotNull CompoundTag dataIn) {
		ResourceLocation perkID = Reference.of(src.getString("perk").get());
		Perk perk = perks.getOrDefault(perkID, Perk.empty());
		CompoundTag fullSrc = new CompoundTag()
				.merge(perk.propertyDefaults().copy())
				.merge(src.copy())
				.merge(dataIn.copy())
				.merge(output.copy());
		fullSrc.putLong(APIUtils.SKILL_LEVEL, fullSrc.contains(APIUtils.SKILLNAME)
				? Core.get(player.level()).getData().getLevel(fullSrc.getString(APIUtils.SKILLNAME).get(), player.getUUID())
				: 0L);
		if (perk.canActivate(player, fullSrc)) {
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Perk Executed: %s".formatted(fullSrc.toString()));
			CompoundTag executionOutput = perk.start(player, fullSrc);
			tickTracker.add(new TickSchedule(perk, player, fullSrc.copy(), new AtomicInteger(0)));
			if (fullSrc.contains(APIUtils.COOLDOWN) && isPerkCooledDown(player, fullSrc))
				coolTracker.add(new PerkCooldown(perkID, player, fullSrc, player.level().getGameTime()));
			output = executionOutput;
		}
		else
			output = new CompoundTag();
		return output;
	}
	
	private static record TickSchedule(Perk perk, Player player, CompoundTag src, AtomicInteger ticksElapsed) {
		public boolean shouldTick() {
			return src.contains(APIUtils.DURATION) && ticksElapsed.get() <= src.getInt(APIUtils.DURATION).get();
		}
		
		public void tick() {
			ticksElapsed().getAndIncrement();
			perk.tick(player, src, ticksElapsed.get());
		}
	}
	private static record PerkCooldown(ResourceLocation perkID, Player player, CompoundTag src, long lastUse) {
		public boolean cooledDown(Level level) {
			return level.getGameTime() > lastUse + src.getInt(APIUtils.COOLDOWN).get();
		}
	}
	
	private final List<TickSchedule> tickTracker = new ArrayList<>();
	private final List<PerkCooldown> coolTracker = new ArrayList<>();
	
	public void executePerkTicks(LevelTickEvent event) {
		MsLoggy.DEBUG.log(LOG_CODE.PERKS, "Perk Tick Tracker:" +MsLoggy.listToString(tickTracker));
		coolTracker.removeIf(tracker -> tracker.cooledDown(event.getLevel()));
		new ArrayList<>(tickTracker).forEach(schedule -> {
			if (schedule.perk().canActivate(schedule.player(), schedule.src())) {
				if (schedule.shouldTick())
					schedule.tick();
				else {
					schedule.perk().stop(schedule.player(), schedule.src());
					tickTracker.remove(schedule);
				}
			}
			else
				tickTracker.remove(schedule);
		});
	}

	public boolean isPerkCooledDown(Player player, CompoundTag src) {
		ResourceLocation perkID = Reference.of(src.getString("perk").get());
		return coolTracker.stream().noneMatch(cd -> cd.player().equals(player) && cd.perkID().equals(perkID));
	}
}
