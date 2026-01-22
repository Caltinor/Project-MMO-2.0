package harmonised.pmmo.features.anticheese;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**The intent behind this feature is to implement countermeasures to obvious and universal means of abusing
 * the XP system.  Things like being AFK and getting a steady stream of XP were one aspect of this feature.
 * @author Caltinor
 */
@EventBusSubscriber(modid=Reference.MOD_ID)
public class CheeseTracker {

	public static void applyAntiCheese(EventType event, Identifier source, Player player, Map<String, Long> awardIn) {
		applyAntiCheese(event, List.of(source), player, awardIn);
	}
	public static void applyAntiCheese(EventType event, List<Identifier> source, Player player, Map<String, Long> awardIn) {
		if (!(player instanceof ServerPlayer) || event == null)
			return;
		Setting setting = Config.anticheese().afk().get(event);
		if (setting != null)
			for (Identifier src : source) {setting.applyAFK(event, src, player, awardIn);}
		if ((setting =  Config.anticheese().diminish().get(event)) != null)
			for (Identifier src : source) {setting.applyDiminuation(event, src, player, awardIn);}
		if ((setting = Config.anticheese().normal().get(event)) != null)
			for (Identifier src : source) {setting.applyNormalization(event, src, player, awardIn);}
	}
	
	@SubscribeEvent
	public static void playerWatcher(ServerTickEvent.Pre event) {
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
		int durationAFK = 0, minDuration = 0, cooldownBy = 1, tolerance = 0;
		boolean strictFacing;
		BlockPos lastPos;
		Vec3 lastLookAngle;
		
		public AFKTracker(Player player, int minDuration, int cooldownBy, int tolerance, boolean strictFacing) {
			this.lastPos = player.blockPosition();
			this.lastLookAngle = player.getLookAngle();
			this.minDuration = minDuration;
			this.cooldownBy = cooldownBy;
			this.tolerance = tolerance;
			this.strictFacing = strictFacing;
		}
		
		public AFKTracker update(Player player) {
			if (meetsAFKCriteria(player))
				durationAFK++;
			else if (durationAFK <= 0){
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
			BlockPos curPos = player.blockPosition();
			return (!strictFacing || lastLookAngle.equals(player.getLookAngle()))
					&& Math.abs(lastPos.getX() - curPos.getX()) < tolerance
					&& Math.abs(lastPos.getY() - curPos.getY()) < tolerance
					&& Math.abs(lastPos.getZ() - curPos.getZ()) < tolerance;
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
			double tolerancePercent,
			boolean strictTolerance) {
		
		public static Builder build() {return new Builder();}
		
		public static class Builder {
			private final List<String> source = new ArrayList<>();
			private int minTime = 0;
			private int retention = 0;
			private int toleranceFlat = 0;
			private double reduction = 0.0;
			private int cooloff = 0;
			private double tolerancePercent = 0.0;
			private boolean strictTolerance = true;
			protected Builder() {}
			public Builder source(String entry) {
				source.add(entry);
				return this;
			}
			public Builder source(String...entries) {
				source.addAll(Arrays.asList(entries));
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
			public Builder setStrictness(boolean isStrict) {
				this.strictTolerance = isStrict;
				return this;
			}
			public Setting build() {
				return new Setting(source, minTime, retention, toleranceFlat, reduction, cooloff, tolerancePercent, strictTolerance);
			}
			public Setting fromScripting(Map<String, String> values) {
				if (values.containsKey(SOURCE)) this.source(values.get(SOURCE).split(","));
				if (values.containsKey(MIN_TIME_TO_APPLY)) this.minTime(Integer.parseInt(values.get(MIN_TIME_TO_APPLY)));
				if (values.containsKey(REDUCTION)) this.reduction(Double.parseDouble(values.get(REDUCTION)));
				if (values.containsKey(COOLOFF)) this.cooloff(Integer.parseInt(values.get(COOLOFF)));
				if (values.containsKey(TOLERANCE_PERCENT)) this.tolerance(Double.parseDouble(values.get(TOLERANCE_PERCENT)));
				if (values.containsKey(TOLERANCE_FLAT)) this.tolerance(Integer.parseInt(values.get(TOLERANCE_FLAT)));
				if (values.containsKey(RETENTION)) this.retention(Integer.parseInt(values.get(RETENTION)));
				if (values.containsKey(STRICT)) this.setStrictness(Boolean.parseBoolean(values.get(STRICT)));
				return this.build();
			}
		}
		
		public static final String SOURCE = "source";
		public static final String MIN_TIME_TO_APPLY = "min_time_to_apply";
		public static final String REDUCTION = "reduction";
		public static final String COOLOFF = "cooloff_amount";
		public static final String TOLERANCE_PERCENT = "tolerance_percent";
		public static final String TOLERANCE_FLAT = "tolerance_flat";
		public static final String RETENTION = "retention_duration";
		public static final String STRICT = "strict_tolerance";
		
		public static final Codec<Setting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().optionalFieldOf(SOURCE).forGetter(s -> Optional.of(s.source)),
				Codec.INT.optionalFieldOf(MIN_TIME_TO_APPLY).forGetter(s -> Optional.of(s.minTime)),
				Codec.INT.optionalFieldOf(RETENTION).forGetter(s -> Optional.of(s.retention)),
				Codec.INT.optionalFieldOf(TOLERANCE_FLAT).forGetter(s -> Optional.of(s.toleranceFlat)),
				Codec.DOUBLE.optionalFieldOf(REDUCTION).forGetter(s -> Optional.of(s.reduction)),
				Codec.INT.optionalFieldOf(COOLOFF).forGetter(s -> Optional.of(s.cooloff)),
				Codec.DOUBLE.optionalFieldOf(TOLERANCE_PERCENT).forGetter(s -> Optional.of(s.tolerancePercent)),
				Codec.BOOL.optionalFieldOf(STRICT).forGetter(s -> Optional.of(s.strictTolerance))
				).apply(instance, (src, min, ret, flat, red, cool, per, strict) -> new Setting(
						src.orElse(new ArrayList<>()),
						min.orElse(0),
						ret.orElse(0),
						flat.orElse(0),
						red.orElse(1.0),
						cool.orElse(1),
						per.orElse(0.0),
						strict.orElse(true)
				)));
		
		public void applyAFK(EventType event, Identifier source, Player player, Map<String, Long> awardIn) {
			AFKTracker afkData = AFK_DATA.computeIfAbsent(player, p -> new HashMap<>())
					.computeIfAbsent(event, e -> new AFKTracker(player, minTime(), cooloff(), toleranceFlat(), strictTolerance())).update(player);
			if ((this.source().isEmpty() || this.source().contains(source.toString())) && afkData.isAFK()) {
				awardIn.keySet().forEach(skill -> {
					double scaledRedux = MsLoggy.DEBUG.logAndReturn(reduction * (double)afkData.getAFKDuration(), LOG_CODE.XP, "AFK reduction factor: {}");
					awardIn.compute(skill, (key, xp) -> {
						long reductionAmount = Double.valueOf(xp * scaledRedux).longValue();
						return xp - (Config.anticheese().afkSubtract()
									? reductionAmount
									: reductionAmount > xp ? xp : reductionAmount);
					});
				});
			}
		}
		public void applyDiminuation(EventType event, Identifier source, Player player, Map<String, Long> awardIn) {
			var tracker = DIMINISH_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> new DiminishTracker(retention));
			if (this.source().isEmpty() || this.source().contains(source.toString())) {				
				tracker.diminish();
				awardIn.keySet().forEach(skill -> {
					double reductionScale = 1d - (reduction * (double)tracker.persistedTime);
					awardIn.compute(skill, (key, xp) -> Double.valueOf((double)xp * Math.max(0d, reductionScale)).longValue());
				});
			}
		}
		public void applyNormalization(EventType event, Identifier source, Player player, Map<String, Long> awardIn) {			
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
