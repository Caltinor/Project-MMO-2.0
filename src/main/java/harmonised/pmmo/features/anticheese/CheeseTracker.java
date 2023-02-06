package harmonised.pmmo.features.anticheese;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**This class was originally implemented as a successor feature to the anti-cheese
 * behavior in legacy PMMO.  The intent behind the feature was to implement countermeasures
 * to obvious and universal means of abusing the XP system.  Things like being AFK 
 * and getting a steady stream of XP were one aspect of this feature.  
 * 
 * During the development of PMMO 2.0 it was assessed that such a measure was not 
 * needed. It was my assessment that the new features offered enough configuration
 * flexibility that ops would not feel these features added any value.  As such no
 * real attempt was made or planned to complete this class.  
 * 
 * That said, this class is embedded in every single event that awards xp and to
 * delete it would mean removing this single method call from every single event.
 * Is that hard? No, but it means I would have to reference the delete commit to 
 * go back and add this back if ever a reason was identified that justified adding
 * an anti-cheese behavior.  Therefore, it is much easier to leave this bypass 
 * method in place until it is either needed or truly determined to be obsolete.
 * 
 * @author Caltinor
 *
 */
@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.FORGE)
public class CheeseTracker {

	public static void applyAntiCheese(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
		if (player == null || event == null || !(player instanceof ServerPlayer))
			return;
		Setting setting = AntiCheeseConfig.SETTINGS_AFK.get().get(event); 
		if (setting != null)
			setting.applyAFK(event, source, player, awardIn);
		if ((setting = AntiCheeseConfig.SETTINGS_DIMINISHING.get().get(event)) != null)
			setting.applyDiminuation(event, source, player, awardIn);
		if ((setting = AntiCheeseConfig.SETTINGS_NORMALIZED.get().get(event)) != null)
			setting.applyNormalization(event, source, player, awardIn);
	}
	
	@SubscribeEvent
	public static void playerWatcher(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END || event.side == LogicalSide.CLIENT) 
			return;
		
		AFK_DATA.forEach((player, map) -> map.forEach((type, tracker) -> {
			if (player != null && !tracker.meetsAFKCriteria(player))
				tracker.cooldown();
		}));
		DIMINISH_DATA.forEach((player, map) -> map.forEach((type, tracker) -> tracker.cooloff()));
		NORMALIZED_DATA.forEach((player, map) -> map.forEach((type, tracker) -> tracker.retainTimeRemaining--));
	}
	
	private static final Map<Player, Map<EventType, AFKTracker>> AFK_DATA = new HashMap<>();
	private static final Map<Player, Map<EventType, DiminishTracker>> DIMINISH_DATA = new HashMap<>();
	private static final Map<Player, Map<EventType, NormTracker>> NORMALIZED_DATA = new HashMap<>();
	
	private static class AFKTracker {
		int durationAFK = 0, minDuration = 0, cooldownBy = 1;
		BlockPos lastPos;
		Vec3 lastLookAngle;
		
		public AFKTracker(Player player, int minDuration, int cooldownBy) {
			this.lastPos = player.blockPosition();
			this.lastLookAngle = player.getLookAngle();
			this.minDuration = minDuration;
			this.cooldownBy = cooldownBy;
		}
		
		public AFKTracker update(Player player) {
			if (meetsAFKCriteria(player))
				durationAFK++;
			else {
				lastLookAngle = player.getLookAngle();
				lastPos = player.blockPosition();
			}
			return this;
		}
		
		public void cooldown() {
			if (durationAFK > 0)
				durationAFK -= cooldownBy;
		}
		public boolean meetsAFKCriteria(Player player) {
			return lastLookAngle.equals(player.getLookAngle()) && lastPos.equals(player.blockPosition());
		}
		
		public boolean isAFK() {
			return MsLoggy.DEBUG.logAndReturn(durationAFK >= minDuration, LOG_CODE.FEATURE, "isAFK:{}({}:{})", durationAFK, minDuration);
		}
		
		public int getAFKDuration() {
			return durationAFK - minDuration;
		}
	}
	
	private static class NormTracker {
		public final Map<String, Long> norms = new HashMap<>();
		public int retainTimeRemaining;
		
		public NormTracker(int defaultRetention) {
			this.retainTimeRemaining = defaultRetention;
		}
	}
	
	private static class DiminishTracker {
		public int persistedTime, cooloffLeft;
		private final int timeToClearReduction;
		public DiminishTracker(int timeToClear) {
			this.timeToClearReduction = timeToClear;
		}
		public void cooloff() {
			if (--cooloffLeft <= 0)
				persistedTime = 0;
		}
		public void diminish() {
			persistedTime++;
			cooloffLeft = timeToClearReduction;
		}
	}
	
	public static record Setting(
			List<String> source,
			int minTime,
			int retention,
			int toleranceFlat,
			double reduction,
			int cooloff,
			double tolerancePercent) {
		
		public static Builder build() {return new Builder();}
		
		public static class Builder {
			private List<String> source = new ArrayList<>();
			private int minTime = 0;
			private int retention = 0;
			private int toleranceFlat = 0;
			private double reduction = 0.0;
			private int cooloff = 0;
			private double tolerancePercent = 0.0;
			protected Builder() {}
			public Builder source(String entry) {
				source.add(entry);
				return this;
			}
			public Builder source(String...entries) {
				source.addAll(source);
				return this;
			}
			public Builder minTime(int min) {
				this.minTime = min;
				return this;
			}
			public Builder retention(int ret) {
				this.retention = ret;
				return this;
			}
			public Builder reduction(double red) {
				this.reduction = red;
				return this;
			}
			public Builder cooloff(int cool) {
				this.cooloff = cool;
				return this;
			}
			public Builder tolerance(int flat) {
				this.toleranceFlat = flat;
				return this;
			}
			public Builder tolerance(double percent) {
				this.tolerancePercent = percent;
				return this;
			}
			public Setting build() {
				return new Setting(source, minTime, retention, toleranceFlat, reduction, cooloff, tolerancePercent);
			}
		}
		
		public static final String SOURCE = "source";
		public static final String MIN_TIME_TO_APPLY = "min_time_to_apply";
		public static final String REDUCTION = "reduction";
		public static final String COOLOFF = "cooloff_amount";
		public static final String TOLERANCE_PERCENT = "tolerance_percent";
		public static final String TOLERANCE_FLAT = "tolerance_flat";
		public static final String RETENTION = "retention_duration";
		
		public static final Codec<Setting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().optionalFieldOf(SOURCE).forGetter(s -> Optional.of(s.source)),
				Codec.INT.optionalFieldOf(MIN_TIME_TO_APPLY).forGetter(s -> Optional.of(s.minTime)),
				Codec.INT.optionalFieldOf(RETENTION).forGetter(s -> Optional.of(s.retention)),
				Codec.INT.optionalFieldOf(TOLERANCE_FLAT).forGetter(s -> Optional.of(s.toleranceFlat)),
				Codec.DOUBLE.optionalFieldOf(REDUCTION).forGetter(s -> Optional.of(s.reduction)),
				Codec.INT.optionalFieldOf(COOLOFF).forGetter(s -> Optional.of(s.cooloff)),
				Codec.DOUBLE.optionalFieldOf(TOLERANCE_PERCENT).forGetter(s -> Optional.of(s.tolerancePercent))
				).apply(instance, (src, min, ret, flat, red, cool, per) -> new Setting(
						src.orElse(new ArrayList<>()),
						min.orElse(0),
						ret.orElse(0),
						flat.orElse(0),
						red.orElse(1.0),
						cool.orElse(1),
						per.orElse(0.0)
				)));
		
		public void applyAFK(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
			AFKTracker afkData = AFK_DATA.computeIfAbsent(player, p -> new HashMap<>())
					.computeIfAbsent(event, e -> new AFKTracker(player, minTime, cooloff)).update(player);
			if ((this.source().isEmpty() || this.source().contains(source.toString())) && afkData.isAFK()) {
				awardIn.keySet().forEach(skill -> {
					MsLoggy.DEBUG.log(LOG_CODE.XP, "AFK reduction factor: {}", reduction * (double)afkData.getAFKDuration());
					awardIn.compute(skill, (key, xp) -> {
						long reductionAmount = Double.valueOf(xp * (reduction * (double)afkData.getAFKDuration())).longValue();						
						return xp - (AntiCheeseConfig.AFK_CAN_SUBTRACT.get()
									? reductionAmount
									: reductionAmount > xp ? xp : reductionAmount);
					});
				});
			}
		}
		public void applyDiminuation(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
			var tracker = DIMINISH_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> new DiminishTracker(retention));
			if (this.source().isEmpty() || this.source().contains(source.toString())) {				
				tracker.diminish();
				awardIn.keySet().forEach(skill -> {
					double reductionScale = 1d - (reduction * (double)tracker.persistedTime);
					awardIn.compute(skill, (key, xp) -> Double.valueOf((double)xp * Math.max(0d, reductionScale)).longValue());
				});
			}
		}
		public void applyNormalization(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {			
			if (this.source().isEmpty() || this.source().contains(source.toString())) {
				NormTracker norms = NORMALIZED_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> new NormTracker(retention));
				norms.retainTimeRemaining = retention;
				awardIn.forEach((skill, value) -> {
					long norm = norms.norms.computeIfAbsent(skill, s -> value);
					long acceptableVariance = Double.valueOf(Math.min(norm + (Math.max(1d, (double)norm * tolerancePercent)), norm + toleranceFlat)).longValue();
					norms.norms.put(skill, value > acceptableVariance ? acceptableVariance : value);
				});
				awardIn.putAll(norms.norms);
			}
		}
	}
}
