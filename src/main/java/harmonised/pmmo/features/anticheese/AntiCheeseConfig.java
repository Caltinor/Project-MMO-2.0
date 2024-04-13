package harmonised.pmmo.features.anticheese;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.features.anticheese.CheeseTracker.Setting;

import java.util.Map;

public record AntiCheeseConfig(
		boolean afkSubtract,
		Map<EventType, Setting> afk,
		Map<EventType, Setting> diminish,
		Map<EventType, Setting> normal
) implements ConfigData<AntiCheeseConfig> {
	public AntiCheeseConfig() {this(false,
			Map.of(EventType.SUBMERGED, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
					EventType.SWIMMING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
					EventType.DIVING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
					EventType.SURFACING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
					EventType.SWIM_SPRINTING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build()),
			Map.of(EventType.RIDING, Setting.build().source("minecraft:horse","minecraft:boat").retention(200).reduction(0.005).build()),
			Map.of(EventType.SPRINTING, Setting.build().retention(400).tolerance(0.1).tolerance(10).build())
	);}
	public static final Codec<AntiCheeseConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("afk_can_subtract").forGetter(AntiCheeseConfig::afkSubtract),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf("afk").forGetter(AntiCheeseConfig::afk),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf("diminishing_xp").forGetter(AntiCheeseConfig::diminish),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf("normalization").forGetter(AntiCheeseConfig::normal)
	).apply(instance, AntiCheeseConfig::new));
	@Override
	public Codec<AntiCheeseConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.ANTICHEESE;}

	@Override
	public AntiCheeseConfig combine(AntiCheeseConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
