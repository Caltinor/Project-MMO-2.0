package harmonised.pmmo.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.readers.ConfigListener;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SkillsConfig(Map<String, SkillData> skills) implements ConfigData<SkillsConfig> {
	public SkillsConfig() {this(generateDefaults());}
	public static final Codec<SkillsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, SkillData.CODEC).fieldOf("skills").forGetter(SkillsConfig::skills)
	).apply(instance, SkillsConfig::new));
	public SkillData get(String skill) {return skills().getOrDefault(skill, SkillData.Builder.getDefault());}
	private static Map<String, SkillData> generateDefaults() {
		Map<String, SkillData> defaultSkills = new HashMap<>();
		defaultSkills.put("mining", 	SkillData.Builder.start().withColor(0x00ffff).withIcon(new ResourceLocation("textures/mob_effect/haste.png")).build());
		defaultSkills.put("building", 	SkillData.Builder.start().withColor(0x00ffff).withIcon(new ResourceLocation("pmmo:textures/skills/building.png")).withIconSize(32).build());
		defaultSkills.put("excavation", SkillData.Builder.start().withColor(0xe69900).withIcon(new ResourceLocation("textures/item/iron_shovel.png")).withIconSize(16).build());
		defaultSkills.put("woodcutting",SkillData.Builder.start().withColor(0xffa31a).withIcon(new ResourceLocation("textures/item/iron_axe.png")).withIconSize(16).build());
		defaultSkills.put("farming", 	SkillData.Builder.start().withColor(0x00e600).withIcon(new ResourceLocation("textures/item/wheat.png")).withIconSize(16).build());
		defaultSkills.put("agility", 	SkillData.Builder.start().withColor(0x66cc66).withIcon(new ResourceLocation("textures/mob_effect/speed.png")).build());
		defaultSkills.put("endurance", 	SkillData.Builder.start().withColor(0xcc0000).withIcon(new ResourceLocation("textures/mob_effect/absorption.png")).build());
		defaultSkills.put("combat", 	SkillData.Builder.start().withColor(0xff3300).withIcon(new ResourceLocation("textures/mob_effect/strength.png")).build());
		defaultSkills.put("gunslinging",SkillData.Builder.start().withColor(0xd3c1a3).build());
		defaultSkills.put("archery", 	SkillData.Builder.start().withColor(0xffff00).withIcon(new ResourceLocation("textures/item/bow.png")).withIconSize(16).build());
		defaultSkills.put("smithing", 	SkillData.Builder.start().withColor(0xf0f0f0).withAfkExempt(true).withIcon(new ResourceLocation("pmmo:textures/skills/smithing.png")).withIconSize(32).build());
		defaultSkills.put("flying", 	SkillData.Builder.start().withColor(0xccccff).withIcon(new ResourceLocation("textures/item/elytra.png")).withIconSize(16).build());
		defaultSkills.put("swimming", 	SkillData.Builder.start().withColor(0x3366ff).withIcon(new ResourceLocation("textures/mob_effect/dolphins_grace.png")).build());
		defaultSkills.put("sailing", 	SkillData.Builder.start().withColor(0x99b3ff).withIcon(new ResourceLocation("textures/item/oak_boat.png")).withIconSize(16).build());
		defaultSkills.put("fishing", 	SkillData.Builder.start().withColor(0x00ccff).withIcon(new ResourceLocation("textures/item/fishing_rod.png")).withIconSize(16).build());
		defaultSkills.put("crafting", 	SkillData.Builder.start().withColor(0xff9900).withIcon(new ResourceLocation("pmmo:textures/skills/crafting.png")).withIconSize(32).build());
		defaultSkills.put("magic",	 	SkillData.Builder.start().withColor(0x0000ff).withIcon(new ResourceLocation("textures/particle/enchanted_hit.png")).withIconSize(8).build());
		defaultSkills.put("slayer", 	SkillData.Builder.start().withColor(0xffffff).withIcon(new ResourceLocation("textures/item/netherite_sword.png")).withIconSize(16).build());
		defaultSkills.put("hunter", 	SkillData.Builder.start().withColor(0xcf7815).withIcon(new ResourceLocation("textures/item/diamond_sword.png")).withIconSize(16).build());
		defaultSkills.put("taming", 	SkillData.Builder.start().withColor(0xffffff).withIcon(new ResourceLocation("textures/item/lead.png")).withIconSize(16).build());
		defaultSkills.put("cooking", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).withIcon(new ResourceLocation("textures/item/cooked_mutton.png")).withIconSize(16).build());
		defaultSkills.put("alchemy", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).withIcon(new ResourceLocation("textures/item/potion.png")).withIconSize(16).build());
		defaultSkills.put("engineering",SkillData.Builder.start().withColor(0xffffff).withMaxLevel(100).withIcon(new ResourceLocation("textures/item/redstone.png")).withIconSize(16).build());
		defaultSkills.put("fightgroup", SkillData.Builder.start().setGroupOf(Map.of(
				"combat", 0.5,
				"endurance", 0.3,
				"archery", 0.2))
				.build());
		return defaultSkills;
	}

	@Override
	public Codec<SkillsConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.SKILLS;}

	@Override
	public SkillsConfig combine(SkillsConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
