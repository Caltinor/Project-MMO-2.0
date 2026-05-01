package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.SkillTypeData;
import harmonised.pmmo.config.readers.ConfigListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record SkillTypesConfig(Map<String, SkillTypeData> skillTypes, Optional<List<String>> hidden) implements ConfigData<SkillTypesConfig> {
	// Reserved key in `.pmmo` scripts: `set(hiddenSkills).skills(a,b,c);` populates the hidden list.
	// In JSON, just use the top-level `"hiddenSkills": [...]` field directly.
	public static final String HIDDEN_KEY = "hiddenSkills";
	private static final String SKILLS_PARAM = "skills";

	public SkillTypesConfig() {this(generateDefaults(), Optional.of(generateDefaultHidden()));}
	public SkillTypesConfig(Map<String, SkillTypeData> skillTypes) {this(skillTypes, Optional.empty());}

	private static List<String> generateDefaultHidden() {
		return new java.util.ArrayList<>(List.of("fightgroup"));
	}

	private static Map<String, SkillTypeData> generateDefaults() {
		Map<String, SkillTypeData> map = new HashMap<>();
		map.put("warfare", SkillTypeData.Builder.start()
				.withDisplayName("Warfare").withOrder(0).withColor(0xCC3333)
				.withSkills(List.of("combat", "slayer", "hunter", "archery", "gunslinging")).build());
		map.put("athletics", SkillTypeData.Builder.start()
				.withDisplayName("Athletics").withOrder(1).withColor(0x66CC66)
				.withSkills(List.of("endurance", "agility", "swimming", "flying", "sailing")).build());
		map.put("harvesting", SkillTypeData.Builder.start()
				.withDisplayName("Harvesting").withOrder(2).withColor(0xCC9933)
				.withSkills(List.of("mining", "woodcutting", "excavation", "farming", "fishing")).build());
		map.put("artisanry", SkillTypeData.Builder.start()
				.withDisplayName("Artisanry").withOrder(3).withColor(0xCC6633)
				.withSkills(List.of("smithing", "crafting", "building", "engineering", "cooking", "alchemy")).build());
		map.put("arcana", SkillTypeData.Builder.start()
				.withDisplayName("Arcana").withOrder(4).withColor(0x9933CC)
				.withSkills(List.of("magic")).build());
		map.put("social", SkillTypeData.Builder.start()
				.withDisplayName("Social").withOrder(5).withColor(0xFFD700)
				.withSkills(List.of("charisma", "taming")).build());
		return map;
	}

	public static final MapCodec<SkillTypesConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, SkillTypeData.CODEC).fieldOf("skillTypes").forGetter(SkillTypesConfig::skillTypes),
			Codec.STRING.listOf().optionalFieldOf("hiddenSkills").forGetter(SkillTypesConfig::hidden)
	).apply(instance, SkillTypesConfig::new));

	public SkillTypeData get(String key) {
		return skillTypes.getOrDefault(key, SkillTypeData.Builder.start().build());
	}

	public Set<String> hiddenSkills() {
		return new HashSet<>(hidden.orElse(List.of()));
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
		if (HIDDEN_KEY.equals(param)) {
			List<String> existing = this.hidden().orElse(List.of());
			List<String> merged = new java.util.ArrayList<>(existing);
			if (value.containsKey(SKILLS_PARAM)) {
				Arrays.stream(value.get(SKILLS_PARAM).split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.forEach(s -> {if (!merged.contains(s)) merged.add(s);});
			}
			return new SkillTypesConfig(this.skillTypes(), merged.isEmpty() ? Optional.empty() : Optional.of(merged));
		}
		Map<String, SkillTypeData> map = new HashMap<>(this.skillTypes());
		map.put(param, SkillTypeData.Builder.start().fromScripting(value));
		return new SkillTypesConfig(map, this.hidden());
	}

	@Override
	public SkillTypesConfig combine(SkillTypesConfig two) {return two;}

	@Override
	public boolean isUnconfigured() {return skillTypes.isEmpty() && hidden.isEmpty();}
}
