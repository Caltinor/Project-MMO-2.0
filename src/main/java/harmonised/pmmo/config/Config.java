package harmonised.pmmo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec CLIENT_CONFIG;
	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		
		setupClient(CLIENT_BUILDER);
		setupCommon(COMMON_BUILDER);
		setupServer(SERVER_BUILDER);
		
		CLIENT_CONFIG = CLIENT_BUILDER.build();
		COMMON_CONFIG = COMMON_BUILDER.build();
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	//====================CLIENT SETTINGS===============================
	
	private static void setupClient(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Client Configuration").push("Client");
		
		buildGUI(builder);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_X;
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_Y;
	public static ForgeConfigSpec.ConfigValue<Boolean> SKILL_LIST_DISPLAY;
	
	private static void buildGUI(ForgeConfigSpec.Builder builder) {
		builder.comment("Configuration settings for the guis").push("GUI");
		
	SKILL_LIST_OFFSET_X = builder.comment("how far right from the top left corner the skill list should be")
						.define("Skill List Xoffset", 0d);
	SKILL_LIST_OFFSET_Y = builder.comment("how far down from the top left corner the skill list should be")
						.define("Skill List Yoffset", 0d);
	SKILL_LIST_DISPLAY = builder.comment("Should the skill list be displayed")
						.define("Display Skill List", true);
		
		builder.pop();
	}
	
	//====================COMMON SETTINGS===============================
	
	private static void setupCommon(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Configuration").push("Common");
		
		buildMsLoggy(builder);
		
		builder.pop(); //Common Blocks
	}
	
	//TODO make a setting for each toggleable logging level
	public static ForgeConfigSpec.ConfigValue<Boolean> INFO_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> WARN_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> ERROR_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> FATAL_LOGGING;
	
	private static void buildMsLoggy(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Error Logging Configuration").push("Ms Loggy");
		INFO_LOGGING = builder.comment("Should MsLoggy info logging be enabled?  This will flood your log with data, but provides essential details",
									  " when trying to find data errors and bug fixing.  ")
						.define("Info Logging", false);
		DEBUG_LOGGING = builder.comment("Should MsLoggy debug logging be enabled?  This will flood your log with data, but provides essential details",
				  					  " when trying to find bugs. DEVELOPER SETTING (mostly).  ")
						.define("Debug Logging", false);
		WARN_LOGGING = builder.comment("Should MsLoggy warn logging be enabled?  This log type is helpful for catching important but non-fatal issues")
						.define("Info Logging", true);
		ERROR_LOGGING = builder.comment("Should Error Logging be enabled.  it is highly recommended this stay true.  however, you can",
									  "disable it to remove pmmo errors from the log.")
						.define("Error Logging", true);
		FATAL_LOGGING = builder.comment("Should MsLoggy fatal logging be enabled?  I can't imagine a situation where you'd want this off, but here you go.")
						.define("Info Logging", true);
		builder.pop(); //Ms. Loggy Block
	}
	
	//====================SERVER SETTINGS===============================	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		buildLevels(builder);
		buildAutoValue(builder);
	}
	
	public static ForgeConfigSpec.ConfigValue<Integer> 	MAX_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Boolean> 	USE_EXPONENTIAL_FORUMULA;
	public static ForgeConfigSpec.ConfigValue<Long> 	LINEAR_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	LINEAR_PER_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Integer> 	EXPONENTIAL_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_POWER_BASE;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_LEVEL_MOD;
	
	private static void buildLevels(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related level gain").push("Levels");
		
		MAX_LEVEL = builder.comment("The highest level a player can acheive in any skill.")
						.defineInRange("Max Level", 1523, 1, Integer.MAX_VALUE);
		USE_EXPONENTIAL_FORUMULA = builder.comment("shold levels be determined using an exponential forumula?")
						.define("Use Exponential Formula", true);
		
		//========LINEAR SECTION===============
		builder.comment("Settings for Linear XP configuration").push("LINEAR LEVELS");
		LINEAR_BASE_XP = builder.comment("what is the base xp to reach level 2 (baseXp + level * xpPerLevel)")
							.defineInRange("Base XP", 250l, 1l, Long.MAX_VALUE);
		LINEAR_PER_LEVEL = builder.comment("What is the xp increase per level (baseXp + level * xpPerLevel)")
							.defineInRange("Per Level", 50d, 1d, Double.MAX_VALUE);		
		builder.pop(); //COMPLETE LINEAR BLOCK
		
		//========EXPONENTIAL SECTION==========
		builder.comment("Settings for Exponential XP configuration").push("EXPONENTIAL LEVELS");
		EXPONENTIAL_BASE_XP = builder.comment("What is the x in: x * ([Power Base]^([Per Level] * level))")
							.defineInRange("Base XP", 83, 1, Integer.MAX_VALUE);
		EXPONENTIAL_POWER_BASE = builder.comment("What is the x in: [Base XP] * (x^([Per Level] * level))")
							.defineInRange("Power Base", 1.104088404342588d, 0d, Double.MAX_VALUE);
		EXPONENTIAL_LEVEL_MOD = builder.comment("What is the x in: [Base XP] * ([Power Base]^(x * level))")
							.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);
		builder.pop(); //COMPLETE EXPONENTIAL BLOCK
		builder.pop(); //COMPLETE LEVELS BLOCK
		
	}
	
	public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_AUTO_VALUES;
	
	private static void buildAutoValue(ForgeConfigSpec.Builder builder) {
		builder.comment("Auto Values estimate values based on item/block/entity properties", 
						"and kick in when no other defined requirement or xp value is present").push("Auto Values");
		
		ENABLE_AUTO_VALUES = builder.comment("set this to false to disable the auto values system.")
								.define("Auto Values Enabled", true);
		
		builder.pop();
	}
}
