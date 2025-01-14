package harmonised.pmmo.features.anticheese;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.config.scripting.Functions;
import harmonised.pmmo.features.anticheese.CheeseTracker.Setting;

import java.util.HashMap;
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

	private static final String AFK = "afk";
	private static final String AFK_SUB = "afk_can_subtract";
	private static final String DIM = "diminishing_xp";
	private static final String NORM = "normalization";
	private static final String EVENT = "event";

	public static final MapCodec<AntiCheeseConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.fieldOf(AFK_SUB).forGetter(AntiCheeseConfig::afkSubtract),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf(AFK).forGetter(AntiCheeseConfig::afk),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf(DIM).forGetter(AntiCheeseConfig::diminish),
			Codec.unboundedMap(EventType.CODEC, Setting.CODEC).fieldOf(NORM).forGetter(AntiCheeseConfig::normal)
	).apply(instance, AntiCheeseConfig::new));
	@Override
	public MapCodec<AntiCheeseConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.ANTICHEESE;}


	@Override
	public ConfigData<AntiCheeseConfig> getFromScripting(String param, Map<String, String> value) {
		if (!value.containsKey(EVENT)) return this;
		EventType type = EventType.byName(value.get(EVENT));
		if (type == null) return this;

		boolean afk_sub = param.equals(AFK_SUB) ? Functions.getBool(value) : this.afkSubtract();
		AntiCheeseConfig config = new AntiCheeseConfig(afk_sub,
				new HashMap<>(this.afk()),
				new HashMap<>(this.diminish()),
				new HashMap<>(this.normal()));
		switch (param) {
			case AFK -> config.afk().put(type, Setting.build().fromScripting(value));
			case DIM -> config.diminish().put(type, Setting.build().fromScripting(value));
			case NORM -> config.normal().put(type, Setting.build().fromScripting(value));
			default -> {}
		}
		return this;
	}

	@Override
	public AntiCheeseConfig combine(AntiCheeseConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
