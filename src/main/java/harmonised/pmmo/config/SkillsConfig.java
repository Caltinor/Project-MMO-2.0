package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SkillsConfig(Map<String, SkillData> skills) implements ConfigData<SkillsConfig> {
	public SkillsConfig() {this(generateDefaults());}
	public static final MapCodec<SkillsConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, SkillData.CODEC).fieldOf("skills").forGetter(SkillsConfig::skills)
	).apply(instance, SkillsConfig::new));
	public SkillData get(String skill) {return skills().getOrDefault(skill, SkillData.Builder.getDefault());}
	private static Map<String, SkillData> generateDefaults() {
		Map<String, SkillData> defaultSkills = new HashMap<>();
		defaultSkills.put("mining", 	SkillData.Builder.start().withColor(0xff00ffff).withIcon(Reference.mc("textures/mob_effect/haste.png")).build());
		defaultSkills.put("building", 	SkillData.Builder.start().withColor(0xff00ffff).withIcon(Reference.of("pmmo:textures/skills/building.png")).withIconSize(32).build());
		defaultSkills.put("excavation", SkillData.Builder.start().withColor(0xffe69900).withIcon(Reference.mc("textures/item/iron_shovel.png")).withIconSize(16).build());
		defaultSkills.put("woodcutting",SkillData.Builder.start().withColor(0xffffa31a).withIcon(Reference.mc("textures/item/iron_axe.png")).withIconSize(16).build());
		defaultSkills.put("farming", 	SkillData.Builder.start().withColor(0xff00e600).withIcon(Reference.mc("textures/item/wheat.png")).withIconSize(16).build());
		defaultSkills.put("agility", 	SkillData.Builder.start().withColor(0xff66cc66).withIcon(Reference.mc("textures/mob_effect/speed.png")).build());
		defaultSkills.put("endurance", 	SkillData.Builder.start().withColor(0xffcc0000).withIcon(Reference.mc("textures/mob_effect/absorption.png")).build());
		defaultSkills.put("combat", 	SkillData.Builder.start().withColor(0xffff3300).withIcon(Reference.mc("textures/mob_effect/strength.png")).build());
		defaultSkills.put("gunslinging",SkillData.Builder.start().withColor(0xffd3c1a3).build());
		defaultSkills.put("archery", 	SkillData.Builder.start().withColor(0xffffff00).withIcon(Reference.mc("textures/item/bow.png")).withIconSize(16).build());
		defaultSkills.put("smithing", 	SkillData.Builder.start().withColor(0xfff0f0f0).withAfkExempt(true).withIcon(Reference.of("pmmo:textures/skills/smithing.png")).withIconSize(32).build());
		defaultSkills.put("flying", 	SkillData.Builder.start().withColor(0xffccccff).withIcon(Reference.mc("textures/item/elytra.png")).withIconSize(16).build());
		defaultSkills.put("swimming", 	SkillData.Builder.start().withColor(0xff3366ff).withIcon(Reference.mc("textures/mob_effect/dolphins_grace.png")).build());
		defaultSkills.put("sailing", 	SkillData.Builder.start().withColor(0xff99b3ff).withIcon(Reference.mc("textures/item/oak_boat.png")).withIconSize(16).build());
		defaultSkills.put("fishing", 	SkillData.Builder.start().withColor(0xff00ccff).withIcon(Reference.mc("textures/item/fishing_rod.png")).withIconSize(16).build());
		defaultSkills.put("crafting", 	SkillData.Builder.start().withColor(0xffff9900).withIcon(Reference.of("pmmo:textures/skills/crafting.png")).withIconSize(32).build());
		defaultSkills.put("magic",	 	SkillData.Builder.start().withColor(0xff0000ff).withIcon(Reference.mc("textures/particle/enchanted_hit.png")).withIconSize(8).build());
		defaultSkills.put("slayer", 	SkillData.Builder.start().withColor(0xffffffff).withIcon(Reference.mc("textures/item/netherite_sword.png")).withIconSize(16).build());
		defaultSkills.put("hunter", 	SkillData.Builder.start().withColor(0xffcf7815).withIcon(Reference.mc("textures/item/diamond_sword.png")).withIconSize(16).build());
		defaultSkills.put("taming", 	SkillData.Builder.start().withColor(0xffffffff).withIcon(Reference.mc("textures/item/lead.png")).withIconSize(16).build());
		defaultSkills.put("cooking", 	SkillData.Builder.start().withColor(0xffe69900).withAfkExempt(true).withIcon(Reference.mc("textures/item/cooked_mutton.png")).withIconSize(16).build());
		defaultSkills.put("alchemy", 	SkillData.Builder.start().withColor(0xffe69900).withAfkExempt(true).withIcon(Reference.mc("textures/item/potion.png")).withIconSize(16).build());
		defaultSkills.put("engineering",SkillData.Builder.start().withColor(0xffffffff).withMaxLevel(100).withIcon(Reference.mc("textures/item/redstone.png")).withIconSize(16).build());
		defaultSkills.put("fightgroup", SkillData.Builder.start().setGroupOf(Map.of(
				"combat", 0.5,
				"endurance", 0.3,
				"archery", 0.2))
				.build());
		return defaultSkills;
	}

	@Override
	public MapCodec<SkillsConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.SKILLS;}

	@Override
	public ConfigData<SkillsConfig> getFromScripting(String param, Map<String, String> value) {
		Map<String, SkillData> skills = new HashMap<>(this.skills());
		skills.put(param, SkillData.Builder.start().fromScripting(value));
		return new SkillsConfig(skills);
	}

	@Override
	public SkillsConfig combine(SkillsConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
