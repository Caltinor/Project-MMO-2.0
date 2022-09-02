package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class SkillsConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		generateDefaults();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		buildGlobals(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ConfigObject<Map<String, SkillData>> SKILLS;
	
	private static Map<String, SkillData> defaultSkills;
	
	private static void buildGlobals(ForgeConfigSpec.Builder builder) {
		builder.comment("===========================================================","",
						" All skills in pmmo are defined when they are used anywhere",
						" in PMMO.  You do not need to define a skill here to use it.",
						" However, defining a skills attributes here will give you a",
						" more rounded skill list and a cleaner looking mod.  Note ",
						" that all the defaults here can be replaced if you wish.", "",
						"===========================================================")
						.push("Skills");
		
		SKILLS = TomlConfigHelper.defineObject(builder, "Entry", Codec.unboundedMap(Codec.STRING, SkillData.CODEC), defaultSkills);
		
		builder.pop();
	}
	
	private static void generateDefaults() {
		defaultSkills = new HashMap<>();
		defaultSkills.put("mining", 	SkillData.Builder.start().withColor(0x00ffff).withIcon(new ResourceLocation("textures/mob_effect/haste.png")).build());
		defaultSkills.put("building", 	SkillData.Builder.start().withColor(0x00ffff).build());
		defaultSkills.put("excavation", SkillData.Builder.start().withColor(0xe69900).withIcon(new ResourceLocation("textures/item/iron_shovel.png")).build());
		defaultSkills.put("woodcutting",SkillData.Builder.start().withColor(0xffa31a).build());
		defaultSkills.put("farming", 	SkillData.Builder.start().withColor(0x00e600).build());
		defaultSkills.put("agility", 	SkillData.Builder.start().withColor(0x66cc66).withIcon(new ResourceLocation("textures/mob_effect/speed.png")).build());
		defaultSkills.put("endurance", 	SkillData.Builder.start().withColor(0xcc0000).build());
		defaultSkills.put("combat", 	SkillData.Builder.start().withColor(0xff3300).build());
		defaultSkills.put("gunslinging",SkillData.Builder.start().withColor(0xd3c1a3).build());
		defaultSkills.put("archery", 	SkillData.Builder.start().withColor(0xffff00).build());		
		defaultSkills.put("smithing", 	SkillData.Builder.start().withColor(0xf0f0f0).withAfkExempt(true).build());
		defaultSkills.put("flying", 	SkillData.Builder.start().withColor(0xccccff).build());
		defaultSkills.put("swimming", 	SkillData.Builder.start().withColor(0x3366ff).build());
		defaultSkills.put("sailing", 	SkillData.Builder.start().withColor(0x99b3ff).build());
		defaultSkills.put("fishing", 	SkillData.Builder.start().withColor(0x00ccff).build());
		defaultSkills.put("crafting", 	SkillData.Builder.start().withColor(0xff9900).build());
		defaultSkills.put("magic",	 	SkillData.Builder.start().withColor(0x0000ff).build());
		defaultSkills.put("slayer", 	SkillData.Builder.start().withColor(0xffffff).build());
		defaultSkills.put("hunter", 	SkillData.Builder.start().withColor(0xcf7815).build());
		defaultSkills.put("taming", 	SkillData.Builder.start().withColor(0xffffff).build());		
		defaultSkills.put("cooking", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).build());
		defaultSkills.put("alchemy", 	SkillData.Builder.start().withColor(0xe69900).withAfkExempt(true).build());
		defaultSkills.put("engineering",SkillData.Builder.start().withColor(0xffffff).withMaxLevel(100).build());
		defaultSkills.put("fightgroup", SkillData.Builder.start().setGroupOf(Map.of(
				"combat", 0.5,
				"endurance", 0.3,
				"archery", 0.2))
				.build());
	}
}
