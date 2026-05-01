package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.SkillTypeData;
import harmonised.pmmo.config.readers.ConfigListener;

import java.util.HashMap;
import java.util.Map;

public record SkillTypesConfig(Map<String, SkillTypeData> skillTypes) implements ConfigData<SkillTypesConfig> {
	public SkillTypesConfig() {this(new HashMap<>());}
	public static final MapCodec<SkillTypesConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, SkillTypeData.CODEC).fieldOf("skillTypes").forGetter(SkillTypesConfig::skillTypes)
	).apply(instance, SkillTypesConfig::new));

	public SkillTypeData get(String key) {
		return skillTypes.getOrDefault(key, SkillTypeData.Builder.start().build());
	}

	public Map<String, String> skillToType() {
		Map<String, String> out = new HashMap<>();
		skillTypes.forEach((typeKey, data) -> data.getSkills().forEach(skill -> out.putIfAbsent(skill, typeKey)));
		return out;
	}

	@Override
	public MapCodec<SkillTypesConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.SKILLTYPES;}

	@Override
	public ConfigData<SkillTypesConfig> getFromScripting(String param, Map<String, String> value) {
		Map<String, SkillTypeData> map = new HashMap<>(this.skillTypes());
		map.put(param, SkillTypeData.Builder.start().fromScripting(value));
		return new SkillTypesConfig(map);
	}

	@Override
	public SkillTypesConfig combine(SkillTypesConfig two) {return two;}

	@Override
	public boolean isUnconfigured() {return skillTypes.isEmpty();}
}
