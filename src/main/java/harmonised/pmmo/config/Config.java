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
		
		builder.pop();
	}
	
	//====================COMMON SETTINGS===============================
	
	private static void setupCommon(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Configuration").push("Common");
		
		builder.pop();
	}
	
	//====================SERVER SETTINGS===============================	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		buildLevels(builder);
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
}
