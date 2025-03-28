package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;

import java.util.HashMap;
import java.util.Map;

public record GlobalsConfig(Map<String, String> paths,Map<String, String> constants) implements ConfigData<GlobalsConfig> {
	public GlobalsConfig() {this(
			Map.of(
					"tmat0", "tic_materials[0]",
					"tmat1", "tic_materials[1]",
					"tmat2", "tic_materials[2]",
					"tmat3", "tic_materials[3]",
					"sgmats", "SGear_Data{}.Construction{}.Parts[].Item{}.tag{}.Materials[].ID"),
			Map.of("example", "value")
	);}

	private static final String PATHS = "paths";
	private static final String CONST = "constants";
	private static final String KEY = "key";
	@Override
	public ConfigData<GlobalsConfig> getFromScripting(String param, Map<String, String> value) {
		Map<String, String> paths = new HashMap<>(this.paths());
		Map<String, String> constants = new HashMap<>(this.constants());
		if (param.equals(PATHS))
			paths.put(value.getOrDefault(KEY, "missing"), value.getOrDefault("value", "missing"));
		if (param.equals(CONST))
			constants.put(value.getOrDefault(KEY, "missing"), value.getOrDefault("value", "missing"));
		return new GlobalsConfig(paths, constants);
	}

	public static final MapCodec<GlobalsConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf(PATHS).forGetter(GlobalsConfig::paths),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf(CONST).forGetter(GlobalsConfig::constants)
	).apply(instance, GlobalsConfig::new));
	@Override
	public MapCodec<GlobalsConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.GLOBALS;}

	@Override
	public GlobalsConfig combine(GlobalsConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
