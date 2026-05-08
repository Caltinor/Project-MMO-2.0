package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.codecs.SkillTypeData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combined config of all skills and their optional groupings (types).
 * <p>
 * Loaded from {@code data/<ns>/config/skills.json} on the server side and synced to
 * clients. The {@code types} map is optional in the JSON; if absent, every skill
 * renders untyped.
 */
public record SkillsConfig(Map<String, SkillData> skills, Map<String, SkillTypeData> types) implements ConfigData<SkillsConfig> {
	/**
	 * Reserved param key in {@code .pmmo} scripts. Writing
	 * {@code set(name).skillType()...} dispatches to the type-update path; without
	 * this flag, {@code set(name)...} updates a skill instead. The flag's value is
	 * ignored — only its presence matters.
	 */
	private static final String TYPE_FLAG = "skillType";

	/** No-arg default used by {@link ConfigListener} when no datapack ships a skills config. */
	public SkillsConfig() {this(generateDefaults(), generateDefaultTypes());}

	public static final MapCodec<SkillsConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, SkillData.CODEC).fieldOf("skills").forGetter(SkillsConfig::skills),
			// `types` is optional in JSON; missing field → empty map (no grouping).
			Codec.unboundedMap(Codec.STRING, SkillTypeData.CODEC).optionalFieldOf("types", new HashMap<>()).forGetter(SkillsConfig::types)
	).apply(instance, SkillsConfig::new));

	/** Lookup with fallback to a builder default — never returns null. */
	public SkillData get(String skill) {return skills().getOrDefault(skill, SkillData.Builder.getDefault());}

	private static Map<String, SkillData> generateDefaults() {
		Map<String, SkillData> defaultSkills = new HashMap<>();
		defaultSkills.put("mining", 	SkillData.Builder.start().withColor(0x00ffff).withIcon(Reference.mc("textures/mob_effect/haste.png")).build());
		defaultSkills.put("building", 	SkillData.Builder.start().withColor(0x00ffff).withIcon(Reference.of("pmmo:textures/skills/building.png")).withIconSize(32).build());
		defaultSkills.put("excavation", SkillData.Builder.start().withColor(0xe69900).withIcon(Reference.mc("textures/item/iron_shovel.png")).withIconSize(16).build());
		defaultSkills.put("woodcutting",SkillData.Builder.start().withColor(0xffa31a).withIcon(Reference.mc("textures/item/iron_axe.png")).withIconSize(16).build());
		defaultSkills.put("farming", 	SkillData.Builder.start().withColor(0x00e600).withIcon(Reference.mc("textures/item/wheat.png")).withIconSize(16).build());
		defaultSkills.put("agility", 	SkillData.Builder.start().withColor(0x66cc66).withIcon(Reference.mc("textures/mob_effect/speed.png")).build());
		defaultSkills.put("endurance", 	SkillData.Builder.start().withColor(0xcc0000).withIcon(Reference.mc("textures/mob_effect/absorption.png")).build());
		defaultSkills.put("combat", 	SkillData.Builder.start().withColor(0xff3300).withIcon(Reference.mc("textures/mob_effect/strength.png")).build());
		defaultSkills.put("gunslinging",SkillData.Builder.start().withColor(0xd3c1a3).build());
		defaultSkills.put("archery", 	SkillData.Builder.start().withColor(0xffff00).withIcon(Reference.mc("textures/item/bow.png")).withIconSize(16).build());
		defaultSkills.put("smithing", 	SkillData.Builder.start().withColor(0xf0f0f0).withAfkExempt(true).withIcon(Reference.of("pmmo:textures/skills/smithing.png")).withIconSize(32).build());
		defaultSkills.put("flying", 	SkillData.Builder.start().withColor(0xccccff).withIcon(Reference.mc("textures/item/elytra.png")).withIconSize(16).build());
		defaultSkills.put("swimming", 	SkillData.Builder.start().withColor(0x3366ff).withIcon(Reference.mc("textures/mob_effect/dolphins_grace.png")).build());
		defaultSkills.put("sailing", 	SkillData.Builder.start().withColor(0x99b3ff).withIcon(Reference.mc("textures/item/oak_boat.png")).withIconSize(16).build());
		defaultSkills.put("fishing", 	SkillData.Builder.start().withColor(0x00ccff).withIcon(Reference.mc("textures/item/fishing_rod.png")).withIconSize(16).build());
		defaultSkills.put("crafting", 	SkillData.Builder.start().withColor(0xff9900).withIcon(Reference.of("pmmo:textures/skills/crafting.png")).withIconSize(32).build());
		defaultSkills.put("magic",	 	SkillData.Builder.start().withColor(0x0000ff).withIcon(Reference.mc("textures/particle/enchanted_hit.png")).withIconSize(8).build());
		defaultSkills.put("slayer", 	SkillData.Builder.start().withColor(0xffffff).withIcon(Reference.mc("textures/item/netherite_sword.png")).withIconSize(16).build());
		defaultSkills.put("hunter", 	SkillData.Builder.start().withColor(0xcf7815).withIcon(Reference.mc("textures/item/diamond_sword.png")).withIconSize(16).build());
		defaultSkills.put("taming", 	SkillData.Builder.start().withColor(0xffffff).withIcon(Reference.mc("textures/item/lead.png")).withIconSize(16).build());
		defaultSkills.put("cooking", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).withIcon(Reference.mc("textures/item/cooked_mutton.png")).withIconSize(16).build());
		defaultSkills.put("alchemy", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).withIcon(Reference.mc("textures/item/potion.png")).withIconSize(16).build());
		defaultSkills.put("engineering",SkillData.Builder.start().withColor(0xffffff).withMaxLevel(100).withIcon(Reference.mc("textures/item/redstone.png")).withIconSize(16).build());
		defaultSkills.put("fightgroup", SkillData.Builder.start().setGroupOf(Map.of(
				"combat", 0.5,
				"endurance", 0.3,
				"archery", 0.2))
				.withShowInList(false)
				.build());
		defaultSkills.put("charisma", SkillData.Builder.start().withIcon(Reference.mc("textures/item/emerald.png")).withIconSize(16).build());
		return defaultSkills;
	}

	/**
	 * Default skill-type groupings shipped with PMMO. Each type's {@code skills}
	 * list controls which skills appear under it in the inventory panel; the
	 * {@code order} field controls the vertical sort.
	 */
	private static Map<String, SkillTypeData> generateDefaultTypes() {
		Map<String, SkillTypeData> map = new HashMap<>();
		map.put("warfare", SkillTypeData.Builder.start()
				.withOrder(0).withColor(0xCC3333)
				.withSkills(List.of("combat", "slayer", "hunter", "archery", "gunslinging")).build());
		map.put("athletics", SkillTypeData.Builder.start()
				.withOrder(1).withColor(0x66CC66)
				.withSkills(List.of("endurance", "agility", "swimming", "flying", "sailing")).build());
		map.put("harvesting", SkillTypeData.Builder.start()
				.withOrder(2).withColor(0xCC9933)
				.withSkills(List.of("mining", "woodcutting", "excavation", "farming", "fishing")).build());
		map.put("artisanry", SkillTypeData.Builder.start()
				.withOrder(3).withColor(0xCC6633)
				.withSkills(List.of("smithing", "crafting", "building", "engineering", "cooking", "alchemy")).build());
		map.put("arcana", SkillTypeData.Builder.start()
				.withOrder(4).withColor(0x9933CC)
				.withSkills(List.of("magic")).build());
		map.put("social", SkillTypeData.Builder.start()
				.withOrder(5).withColor(0xFFD700)
				.withSkills(List.of("charisma", "taming")).build());
		return map;
	}

	@Override
	public MapCodec<SkillsConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.SKILLS;}

	/**
	 * Called by the {@code .pmmo} scripting layer for each {@code set(...)} expression.
	 * Returns a new {@code SkillsConfig} with the entry added/replaced. The
	 * {@link #TYPE_FLAG} key on {@code value} chooses whether the param key
	 * is treated as a skill name or a skill-type name.
	 */
	@Override
	public ConfigData<SkillsConfig> getFromScripting(String param, Map<String, String> value) {
		if (value.containsKey(TYPE_FLAG)) {
			Map<String, SkillTypeData> updatedTypes = new HashMap<>(this.types());
			updatedTypes.put(param, SkillTypeData.Builder.start().fromScripting(value));
			return new SkillsConfig(this.skills(), updatedTypes);
		}
		Map<String, SkillData> updatedSkills = new HashMap<>(this.skills());
		updatedSkills.put(param, SkillData.Builder.start().fromScripting(value));
		return new SkillsConfig(updatedSkills, this.types());
	}

	@Override
	public SkillsConfig combine(SkillsConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
