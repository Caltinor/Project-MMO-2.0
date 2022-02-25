package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.CodecMapSkills.SkillData;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
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
		defaultSkills.put("mining", 	new SkillData(0x00ffff, false));
		defaultSkills.put("building", 	new SkillData(0x00ffff, false));
		defaultSkills.put("excavation", new SkillData(0xe69900, false));
		defaultSkills.put("woodcutting",new SkillData(0xffa31a, false));
		defaultSkills.put("farming", 	new SkillData(0x00e600, false));
		defaultSkills.put("agility", 	new SkillData(0x66cc66, false));
		defaultSkills.put("endurance", 	new SkillData(0xcc0000, false));
		defaultSkills.put("combat", 	new SkillData(0xff3300, false));
		defaultSkills.put("gunslinging",new SkillData(0xd3c1a3, false));
		defaultSkills.put("archery", 	new SkillData(0xffff00, false));		
		defaultSkills.put("smithing", 	new SkillData(0xf0f0f0, true));
		defaultSkills.put("flying", 	new SkillData(0xccccff, false));
		defaultSkills.put("swimming", 	new SkillData(0x3366ff, false));
		defaultSkills.put("sailing", 	new SkillData(0x99b3ff, false));
		defaultSkills.put("fishing", 	new SkillData(0x00ccff, false));
		defaultSkills.put("crafting", 	new SkillData(0xff9900, false));
		defaultSkills.put("magic",	 	new SkillData(0x0000ff, false));
		defaultSkills.put("slayer", 	new SkillData(0xffffff, false));
		defaultSkills.put("hunter", 	new SkillData(0xcf7815, false));
		defaultSkills.put("taming", 	new SkillData(0xffffff, false));		
		defaultSkills.put("cooking", 	new SkillData(0xe69900, true));
		defaultSkills.put("alchemy", 	new SkillData(0xe69900, true));
		defaultSkills.put("engineering",new SkillData(0xffffff, false));
	}
}
