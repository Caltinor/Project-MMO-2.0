package harmonised.pmmo.features.anticheese;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.FORGE, value=Dist.DEDICATED_SERVER)
public class CheeseTracker {

	public static void applyAntiCheese(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
		Setting setting = AntiCheeseConfig.SETTINGS_AFK.get().get(event); 
		if (setting != null)
			setting.applyAFK(event, source, player, awardIn);
		if ((setting = AntiCheeseConfig.SETTINGS_DIMINISHING.get().get(event)) != null)
			setting.applyDiminuation(event, source, player, awardIn);
		if ((setting = AntiCheeseConfig.SETTINGS_NORMALIZED.get().get(event)) != null) {
			setting.applyNormalization(event, source, player, awardIn);
		}
		if ((setting = AntiCheeseConfig.SETTINGS_RANGE_LIMIT.get().get(event)) != null)
			setting.applyRangeLimit(event, source, player, awardIn);
	}
	
	@SubscribeEvent
	public static void playerWatcher(PlayerTickEvent event) {
		NORMALIZED_DATA.getOrDefault(event.player, new HashMap<>()).forEach((type, tracker) -> {
			tracker.retainTimeRemaining--;
		});
	}
	
	private static final Map<Player, Map<EventType, AFKTracker>> AFK_DATA = new HashMap<>();
	private static final Map<Player, Map<EventType, Integer>> DIMINISH_DATA = new HashMap<>();
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
			this.durationAFK +=  (lastLookAngle.equals(player.getLookAngle()) && lastPos.equals(player.blockPosition())) 
					? 1 : -cooldownBy;
			return this;
		}
		
		public boolean isAFK() {
			return durationAFK >= minDuration;
		}
		
		public int getAFKDuration() {
			return durationAFK - minDuration;
		}
	}
	
	private static class NormTracker {
		public final Map<String, Long> norms = new HashMap<>();
		@SuppressWarnings("unused")  //for some reason the IDE doesn't detect its usage on line 62
		public int retainTimeRemaining;
		
		public NormTracker(int defaultRetention) {
			this.retainTimeRemaining = defaultRetention;
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
			AFKTracker afkData = AFK_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> new AFKTracker(player, minTime, cooloff)).update(player);
			if ((this.source().isEmpty() || this.source().contains(source.toString())) 
					&& afkData.isAFK()) {
				awardIn.keySet().forEach(skill -> {
					awardIn.compute(skill, (key, xp) -> xp * Double.valueOf(1d - (reduction * (double)afkData.getAFKDuration())).longValue());
				});
			}
		}
		public void applyDiminuation(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
			int duration = DIMINISH_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> 0);
			if (this.source().isEmpty() || this.source().contains(source.toString())) {				
				duration++;
				final int atomicDuration = duration;
				awardIn.keySet().forEach(skill -> {
					awardIn.compute(skill, (key, xp) -> xp * Double.valueOf(1d - (reduction * atomicDuration)).longValue());
				});
			}
			else 
				duration -= cooloff;
		}
		public void applyNormalization(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {			
			if (this.source().isEmpty() || this.source().contains(source.toString())) {
				NormTracker norms = NORMALIZED_DATA.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(event, e -> new NormTracker(retention));
				norms.retainTimeRemaining = retention;
				awardIn.forEach((skill, value) -> {
					long norm = norms.norms.getOrDefault(norms, value);
					long acceptableVariance = Double.valueOf(Math.max(norm + (norm * tolerancePercent), norm + toleranceFlat)).longValue();
					norms.norms.put(skill, value > acceptableVariance ? acceptableVariance : value);
				});
				awardIn = norms.norms;
			}
		}
		public void applyRangeLimit(EventType event, ResourceLocation source, Player player, Map<String, Long> awardIn) {
			//TODO this will be tricky because we need to track blocks that are awarding xp and that's gonna take
			// an extra layer.  I may just abandon this feature.
		}
	}
}
