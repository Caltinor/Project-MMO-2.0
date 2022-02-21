package harmonised.pmmo.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ModifierDataType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

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
		buildTooltips(builder);
		
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
	
	private static BooleanValue[] TOOLTIP_REQ_ENABLED;
	private static BooleanValue[] TOOLTIP_XP_ENABLED;
	private static BooleanValue[] TOOLTIP_BONUS_ENABLED;
	
	private static final String TOOLTIP_SUFFIX = " tooltip enabled";
	
	public static BooleanValue tooltipReqEnabled(ReqType type) {return TOOLTIP_REQ_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipXpEnabled(EventType type) {return TOOLTIP_XP_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipBonusEnabled(ModifierDataType type) {return TOOLTIP_BONUS_ENABLED[type.ordinal()];}
	
	private static void buildTooltips(ForgeConfigSpec.Builder builder) {
		builder.comment("This section covers the various tooltip elements and whether they should be enabled").push("Tooltip_Visibility");
		
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		builder.push("Requirement_Tooltips");
		TOOLTIP_REQ_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+" Req "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //requirements
		
		List<EventType> rawEventList = new ArrayList<>(Arrays.asList(EventType.values()));
		builder.push("Xp_Gain_Tooltips");
		TOOLTIP_XP_ENABLED = rawEventList.stream().map((t) -> {
			return builder.define(t.toString()+" XP Gain "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //xp gains
		
		List<ModifierDataType> rawBonusList = new ArrayList<>(Arrays.asList(ModifierDataType.values()));
		builder.push("Bonus_Tooltips");
		TOOLTIP_BONUS_ENABLED = rawBonusList.stream().map((t) -> {
			return builder.define(t.toString()+" Bonus "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //bonuses
		
		builder.pop(); //outer
	}
	
	//====================COMMON SETTINGS===============================
	
	private static void setupCommon(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Configuration").push("Common");
		
		buildMsLoggy(builder);
		
		builder.pop(); //Common Blocks
	}
	
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
						.define("Warn Logging", true);
		ERROR_LOGGING = builder.comment("Should Error Logging be enabled.  it is highly recommended this stay true.  however, you can",
									  "disable it to remove pmmo errors from the log.")
						.define("Error Logging", true);
		FATAL_LOGGING = builder.comment("Should MsLoggy fatal logging be enabled?  I can't imagine a situation where you'd want this off, but here you go.")
						.define("Fatal Logging", true);
		
		builder.pop(); //Ms. Loggy Block
	}
	
	//====================SERVER SETTINGS===============================	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		buildBasics(builder);
		buildLevels(builder);
		buildRequirements(builder);
		buildXpGains(builder);
		buildAutoValue(builder);
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> CREATIVE_REACH;
	
	private static void buildBasics(ForgeConfigSpec.Builder builder) {
		builder.comment("General settings on the server").push("General");
		
		CREATIVE_REACH = builder.comment("how much extra reach should a player get in creative mode")
				.defineInRange("Creative Reach", 50d, 4d, Double.MAX_VALUE);
		
		builder.pop();
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
	
	private static BooleanValue[] REQ_ENABLED;
	private static BooleanValue[] STRICT_REQS;
	
	private static final String REQ_ENABLED_SUFFIX = " Req Enabled";
	private static final String STRICT_REQ_SUFFIX  = " Req Strict";
	
	public static BooleanValue reqEnabled(ReqType type) {return REQ_ENABLED[type.ordinal()];}
	public static BooleanValue reqStrict(ReqType type) {return STRICT_REQS[type.ordinal()];}
	
	private static void buildRequirements(ForgeConfigSpec.Builder builder) {
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		
		builder.comment("Settngs governing requirements at the macro level").push("Requirements");
		builder.comment("Should requirements apply for the applicable action type").push("Req_Enabled");		
		
		REQ_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+REQ_ENABLED_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
		builder.comment("Should requirements be strictly enforced?"
				, "if no, then requirements will have certain penalties applied"
				, "proportionate to the gap between the player and the requirement").push("Strict_Reqs");
		
		STRICT_REQS = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+STRICT_REQ_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
		builder.pop();
		
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> REUSE_PENALTY;
	public static ForgeConfigSpec.ConfigValue<Boolean> SUMMATED_MAPS;
	
	private static void buildXpGains(ForgeConfigSpec.Builder builder) {
		builder.comment("All settings related to the gain of experience").push("XP_Gains");
		
		REUSE_PENALTY = builder.comment("how much of the original XP should be awarded when a player breaks a block they placed")
				.defineInRange("Reuse Penalty", 0d, 0d, Double.MAX_VALUE);
		SUMMATED_MAPS = builder.comment("Should xp Gains from perks be added onto by configured xp values")
				.define("Perks Plus Configs", false);
		
		buildEventBasedXPSettings(builder);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> FROM_ENVIRONMENT_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> FROM_ENVIRONMENT_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> FROM_IMPACT_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> FROM_IMPACT_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> FROM_MAGIC_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> FROM_MAGIC_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> FROM_PROJECTILE_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> FROM_PROJECTILE_SKILLS;
	
	public static ForgeConfigSpec.ConfigValue<Double> RECEIVE_DAMAGE_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> RECEIVE_DAMAGE_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> DEAL_MELEE_DAMAGE_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEAL_MELEE_DAMAGE_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> DEAL_RANGED_DAMAGE_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEAL_RANGED_DAMAGE_SKILLS;
	
	public static ForgeConfigSpec.ConfigValue<Double> JUMP_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> JUMP_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> SPRINT_JUMP_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> SPRINT_JUMP_SKILLS;
	public static ForgeConfigSpec.ConfigValue<Double> CROUCH_JUMP_MODIFIER;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> CROUCH_JUMP_SKILLS;
	
	private static void buildEventBasedXPSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related to certain default event XP awards.").push("Event_XP_Specifics");
		
		builder.push("Damage_Received");
			FROM_ENVIRONMENT_SKILLS = builder.comment("What skills should be given xp when receiving damage from environment")
					.defineList("FROM_ENVIRONMENT skills", List.of("endurance"), (s) -> s instanceof String);
			FROM_ENVIRONMENT_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("FROM_ENVIRONMENT xp modifier", 10d, 0d, Double.MAX_VALUE);
			FROM_IMPACT_SKILLS = builder.comment("What skills should be given xp when receiving damage from impacts like falling or flying into a wall")
					.defineList("FROM_IMPACT skills", List.of("endurance", "flying"), (s) -> s instanceof String);
			FROM_IMPACT_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("FROM_IMPACT xp modifier", 15d, 0d, Double.MAX_VALUE);
			FROM_MAGIC_SKILLS = builder.comment("What skills should be given xp when receiving damage from magic sources like potions")
					.defineList("FROM_MAGIC skills", List.of("magic"), (s) -> s instanceof String);
			FROM_MAGIC_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("FROM_MAGIC xp modifier", 15d, 0d, Double.MAX_VALUE);
			FROM_PROJECTILE_SKILLS = builder.comment("What skills should be given xp when receiving damage from magic sources like potions")
					.defineList("FROM_PROJECTILE skills", List.of("endurance"), (s) -> s instanceof String);
			FROM_PROJECTILE_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("FROM_PROJECTILE xp modifier", 15d, 0d, Double.MAX_VALUE);
			RECEIVE_DAMAGE_SKILLS = builder.comment("What skills should be given xp when receiving damage from an uncategorized source")
					.defineList("RECEIVE_DAMAGE skills", List.of("endurance"), (s) -> s instanceof String);
			RECEIVE_DAMAGE_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("RECEIVE_DAMAGE xp modifier", 1d, 0d, Double.MAX_VALUE);
		builder.pop();
		
		builder.push("Damage_Dealt");
			DEAL_MELEE_DAMAGE_SKILLS = builder.comment("What skills should be given xp when receiving damage from an uncategorized source")
					.defineList("DEAL_MELEE_DAMAGE skills", List.of("combat"), (s) -> s instanceof String);
			DEAL_MELEE_DAMAGE_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("DEAL_MELEE_DAMAGE xp modifier", 1d, 0d, Double.MAX_VALUE);
			DEAL_RANGED_DAMAGE_SKILLS = builder.comment("What skills should be given xp when receiving damage from an uncategorized source")
					.defineList("DEAL_RANGED_DAMAGE skills", List.of("archery"), (s) -> s instanceof String);
			DEAL_RANGED_DAMAGE_MODIFIER = builder.comment("the xp award for this event is equal to the damage received times this value.")
					.defineInRange("DEAL_RANGED_DAMAGE xp modifier", 1d, 0d, Double.MAX_VALUE);
		builder.pop();
		
		builder.push("Jumps");
		JUMP_SKILLS = builder.comment("What skills should be given xp when a player jumps")
				.defineList("JUMP skills", List.of("agility"), (s) -> s instanceof String);
		JUMP_MODIFIER = builder.comment("the xp award for this event is equal to the jump base amount times this value.", "note that perks may change the base value.")
				.defineInRange("JUMP xp modifier", 2.5, 0d, Double.MAX_VALUE);
		SPRINT_JUMP_SKILLS = builder.comment("What skills should be given xp when a player jumps while sprinting")
				.defineList("SPRINT_JUMP skills", List.of("agility"), (s) -> s instanceof String);
		SPRINT_JUMP_MODIFIER = builder.comment("the xp award for this event is equal to the jump base amount times this value.", "note that perks may change the base value.")
				.defineInRange("SPRINT_JUMP xp modifier", 2.5, 0d, Double.MAX_VALUE);
		CROUCH_JUMP_SKILLS = builder.comment("What skills should be given xp when a player jumps from a crouch")
				.defineList("CROUCH_JUMP skills", List.of("agility"), (s) -> s instanceof String);
		CROUCH_JUMP_MODIFIER = builder.comment("the xp award for this event is equal to the jump base amount times this value.", "note that perks may change the base value.")
				.defineInRange("CROUCH_JUMP xp modifier", 2.5, 0d, Double.MAX_VALUE);
		builder.pop();
			
		builder.pop();
	}
}
