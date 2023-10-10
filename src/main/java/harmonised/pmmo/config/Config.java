package harmonised.pmmo.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.readers.ConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

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
		buildVein(builder);
		buildTutorial(builder);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_X;
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_Y;
	public static ForgeConfigSpec.ConfigValue<Double> VEIN_GAUGE_OFFSET_X;
	public static ForgeConfigSpec.ConfigValue<Double> VEIN_GAUGE_OFFSET_Y;
	public static ForgeConfigSpec.ConfigValue<Double> GAIN_LIST_OFFSET_X;
	public static ForgeConfigSpec.ConfigValue<Double> GAIN_LIST_OFFSET_Y;
	public static ForgeConfigSpec.ConfigValue<Boolean> SKILL_LIST_DISPLAY;
	public static ForgeConfigSpec.ConfigValue<Boolean> GAIN_LIST_DISPLAY;
	public static ForgeConfigSpec.ConfigValue<Boolean> VEIN_GAUGE_DISPLAY;
	public static ForgeConfigSpec.ConfigValue<Integer> SECTION_HEADER_COLOR;
	public static ForgeConfigSpec.ConfigValue<Integer> SALVAGE_ITEM_COLOR;
	public static ForgeConfigSpec.ConfigValue<Integer> GAIN_LIST_SIZE;
	public static ForgeConfigSpec.ConfigValue<Integer> GAIN_LIST_LINGER_DURATION;
	public static ConfigValue<List<? extends String>> GAIN_BLACKLIST;
	public static ForgeConfigSpec.BooleanValue HIDE_SKILL_BUTTON;
	public static ForgeConfigSpec.ConfigValue<Integer> SKILL_BUTTON_X;
	public static ForgeConfigSpec.ConfigValue<Integer> SKILL_BUTTON_Y;
	
	private static void buildGUI(ForgeConfigSpec.Builder builder) {
		builder.comment("Configuration settings for the guis").push("GUI");
		
		SKILL_LIST_OFFSET_X = builder.comment("how far right from the top left corner the skill list should be")
						.defineInRange("Skill List Xoffset", 0.01d, 0, 1);
		SKILL_LIST_OFFSET_Y = builder.comment("how far down from the top left corner the skill list should be")
						.defineInRange("Skill List Yoffset", 0.0005d, 0, 1);
		SKILL_LIST_DISPLAY = builder.comment("Should the skill list be displayed")
						.define("Display Skill List", true);
		VEIN_GAUGE_OFFSET_X = builder.comment("how far right from the bottom left corner the vein guage sholud be")
				.defineInRange("Vein Gauge Xoffset", 0.01d, 0, 1);
		VEIN_GAUGE_OFFSET_Y = builder.comment("how far up from the bottm left corner the vein guage should be")
				.defineInRange("Vein Gauge Yoffset", 0.95d, 0, 1);
		VEIN_GAUGE_DISPLAY = builder.comment("Should the vein charge data be displayed")
				.define("Display Vein Gauge", true);
		GAIN_LIST_OFFSET_X = builder.comment("how far offset from center the gain list should be")
				.defineInRange("Gain List Xoffset", 0.45d, 0, 1);
		GAIN_LIST_OFFSET_Y = builder.comment("how far down from the top left corner the gain list should be")
				.defineInRange("Gain List Yoffset", 0.0005d, 0, 1);
		GAIN_LIST_DISPLAY = builder.comment("Should the Gain list be displayed")
				.define("Display Gain List", true);
		SECTION_HEADER_COLOR = builder.comment("what color should the background be for the section header lines in the glossary")
				.define("Section Header Color", 0x1504B520);
		SALVAGE_ITEM_COLOR = builder.comment("What color should the background be for the salvage item lines in the glossary")
				.define("Salvage Item Color", 0x15D2A319);
		GAIN_LIST_SIZE = builder.comment("how much xp gain history should display")
				.define("Gain List Size", 3);
		GAIN_LIST_LINGER_DURATION = builder.comment("How long, in ticks, items on the gain list should stay on screen before disappearing")
				.define("Gain List Linger Duration", 100);
		GAIN_BLACKLIST = builder.comment("skills that should now show their gains in the gain list.  this can be used to limit spammy skills")
				.defineList("Gain Blacklist", new ArrayList<>(), s -> s instanceof String);
		HIDE_SKILL_BUTTON = builder.comment("if true, removes the skills button from the inventory screen")
				.define("hide skill button", false);
		SKILL_BUTTON_X = builder.comment("the horizontal location of the skill button in the inventory.","Default = 126.  For removing overlaps, 150 is a good setting")
				.define("skill_button_x", 126);
		SKILL_BUTTON_Y = builder.comment("the vertical location (from center) of the skill button in the inventory.","Default = -22")
				.define("skill_button_y", -22);

		builder.pop();
	}
	
	public static BooleanValue HIDE_MET_REQS;
	
	private static BooleanValue[] TOOLTIP_REQ_ENABLED;
	private static BooleanValue[] TOOLTIP_XP_ENABLED;
	private static BooleanValue[] TOOLTIP_BONUS_ENABLED;
	
	private static final String TOOLTIP_SUFFIX = " tooltip enabled";
	
	public static BooleanValue tooltipReqEnabled(ReqType type) {return TOOLTIP_REQ_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipXpEnabled(EventType type) {return TOOLTIP_XP_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipBonusEnabled(ModifierDataType type) {return TOOLTIP_BONUS_ENABLED[type.ordinal()];}
	
	private static void buildTooltips(ForgeConfigSpec.Builder builder) {
		builder.comment("Generic Tooltip Settings").push("ToolTip_Settings");
		HIDE_MET_REQS = builder.comment("Should met reqs be hidden on the tooltip.")
						.define("Hide Met Req Tooltips", true);
		builder.pop();
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
	
	public static IntValue VEIN_LIMIT;
	
	private static void buildVein(ForgeConfigSpec.Builder builder) {
		builder.comment("Client Settings Related to the Vein Mining Ability").push("Vein_Miner");
		
		VEIN_LIMIT = builder.comment("The max blocks a vein activation should consume regardless of charge")
				.defineInRange("Vein_Limit", 64, 0, Integer.MAX_VALUE);
		
		builder.pop();
	}
	
	public static BooleanValue BREAK_NERF_HIGHLIGHTS;
	public static BooleanValue BLOCK_OWNER_HIGHLIGHTS;
	public static BooleanValue SALVAGE_HIGHLIGHTS;
	
	private static void buildTutorial(ForgeConfigSpec.Builder builder) {
		builder.comment("Toggles for helpful features related to mechanics").push("Tutorial");
		
		BREAK_NERF_HIGHLIGHTS = builder.comment("Should blocks affected by Reuse Penalty show a red outline?")
				.define("Enable Reuse Penalty Highlights", true);
		BLOCK_OWNER_HIGHLIGHTS = builder.comment("Should the owner of a block show when hovering?")
				.define("Enable Owner Highlights", true);
		SALVAGE_HIGHLIGHTS = builder.comment("Should hovering over a salvage block show helpful tips?")
				.define("Enable Salvage Tips", true);
		
		builder.pop();
	}
	
	//====================COMMON SETTINGS===============================
	
	private static void setupCommon(ForgeConfigSpec.Builder builder) {
		builder.comment("===============================================","","",
				"Most Configurations are found in the server config",
				"You can find that in worldname/serverconfig/",
				"","","===============================================").push("Common");
		
		buildMsLoggy(builder);
		
		builder.pop(); //Common Blocks
	}
	
	public static ConfigValue<List<? extends String>> INFO_LOGGING;
	public static ConfigValue<List<? extends String>> DEBUG_LOGGING;
	public static ConfigValue<List<? extends String>> WARN_LOGGING;
	public static ConfigValue<List<? extends String>> ERROR_LOGGING;
	public static ConfigValue<List<? extends String>> FATAL_LOGGING;
	
	private static void buildMsLoggy(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Error Logging Configuration","",
				"===================================================",
				"To enable Logging with MsLoggy, enter a logging",
				"keyword into the logging level list that you want.",
				"the list of keywords are (lowercase only):",
				"'api', 'autovalues', 'chunk', 'data', 'event', ",
				"'feature', 'gui', 'loading', 'network', and 'xp'",
				"===================================================").push("Ms Loggy");
		
		INFO_LOGGING = builder.comment("Which MsLoggy info logging should be enabled?  This will flood your log with data, but provides essential details",
									  " when trying to find data errors and bug fixing.  ")
						.defineList("Info Logging", new ArrayList<>(List.of(LOG_CODE.LOADING.code,LOG_CODE.NETWORK.code,LOG_CODE.API.code)), s -> s instanceof String);
		DEBUG_LOGGING = builder.comment("Which MsLoggy debug logging should be enabled?  This will flood your log with data, but provides essential details",
				  					  " when trying to find bugs. DEVELOPER SETTING (mostly).  ")
						.defineList("Debug Logging", new ArrayList<>(List.of(LOG_CODE.AUTO_VALUES.code)), s -> s instanceof String);
		WARN_LOGGING = builder.comment("Which MsLoggy warn logging should be enabled?  This log type is helpful for catching important but non-fatal issues")
						.defineList("Warn Logging", new ArrayList<>(List.of(LOG_CODE.API.code)), s -> s instanceof String);
		ERROR_LOGGING = builder.comment("Which Error Logging should be enabled.  it is highly recommended this stay true.  however, you can",
									  "disable it to remove pmmo errors from the log.")
						.defineList("Error Logging", new ArrayList<>(List.of(LOG_CODE.DATA.code, LOG_CODE.API.code)), s -> s instanceof String);
		FATAL_LOGGING = builder.comment("Which MsLoggy fatal logging should be enabled?  I can't imagine a situation where you'd want this off, but here you go.")
						.defineList("Fatal Logging", new ArrayList<>(List.of(LOG_CODE.API.code)), s -> s instanceof String);
		
		builder.pop(); //Ms. Loggy Block
	}
	
	//====================SERVER SETTINGS===============================	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		buildBasics(builder);
		buildLevels(builder);
		buildRequirements(builder);
		buildXpGains(builder);
		buildPartySettings(builder);
		buildMobScalingSettings(builder);
		buildVeinMinerSettings(builder);
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> CREATIVE_REACH;
	public static ForgeConfigSpec.ConfigValue<String> SALVAGE_BLOCK;
	public static ForgeConfigSpec.BooleanValue TREASURE_ENABLED;
	public static ForgeConfigSpec.BooleanValue BREWING_TRACKED;
	
	private static void buildBasics(ForgeConfigSpec.Builder builder) {
		builder.comment("General settings on the server").push("General");
		
		CREATIVE_REACH = builder.comment("how much extra reach should a player get in creative mode")
				.defineInRange("Creative Reach", 50d, 4d, Double.MAX_VALUE);
		SALVAGE_BLOCK = builder.comment("Which block should be used for salvaging")
				.define("Salvage Block", "minecraft:smithing_table");
		TREASURE_ENABLED = builder.comment("if false, all pmmo loot conditions will be turned off")
				.define("Treasure Enabled", true);
		BREWING_TRACKED = builder.comment("If false, pmmo will not track if a potion was previously brewed.",
				"this helps with stacking potions from other mods, but ",
				"does not prevent users from pick-placing potions in the",
				"brewing stand for free XP. Toggle at your discretion.")
				.define("brewing_tracked", true);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Integer> 	MAX_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	LOSS_ON_DEATH;
	public static ForgeConfigSpec.ConfigValue<Boolean>	LOSE_LEVELS_ON_DEATH;
	public static ForgeConfigSpec.ConfigValue<Boolean> 	LOSE_ONLY_EXCESS;
	public static ForgeConfigSpec.ConfigValue<Boolean> USE_EXPONENTIAL_FORMULA;
	public static ForgeConfigSpec.ConfigValue<Double> 	GLOBAL_MODIFIER;
	public static TomlConfigHelper.ConfigObject<Map<String, Double>> SKILL_MODIFIERS;
	public static ForgeConfigSpec.ConfigValue<Long> 	LINEAR_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	LINEAR_PER_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Integer> 	EXPONENTIAL_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_POWER_BASE;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_LEVEL_MOD;
	public static ConfigHelper.ConfigObject<List<Long>> STATIC_LEVELS;
	
	private static void buildLevels(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related level gain").push("Levels");
		
		MAX_LEVEL = builder.comment("The highest level a player can achieve in any skill."
						, "NOTE: if this is changing on you to a lower value, that's intentional"
						, "If your formula makes the required xp to get max level greater than"
						, "pmmo can store, pmmo will replace your value with the actual max.")
						.defineInRange("Max Level", 1523, 1, Integer.MAX_VALUE);
		USE_EXPONENTIAL_FORMULA = builder.comment("should levels be determined using an exponential formula?")
						.define("Use Exponential Formula", true);
		STATIC_LEVELS = ConfigHelper.<List<Long>>defineObject(builder
				.comment("=====LEAVE -1 VALUE UNLESS YOU WANT STATIC LEVELS====="
				, "Replacing the -1 and adding values to this list will set the xp required to advance for each"
				, "level manually.  Note that the number of level settings you enter into this list"
				, "will set your max level.  If you only add 10 entries, your max level will be 10."
				, "This setting is intended for players/ops who want fine-tune control over their"
				, "level growth.  use with caution.  ", ""
				, "As a technical note, if you enter values that are not greater than their previous"
				, "value, the entire list will be ignored and revert back to the selected exponential"
				, "or linear formulaic calculation"),
				"Static_Levels",
				Codec.LONG.listOf(), 
				new ArrayList<Long>(List.of(-1L)));

		LOSS_ON_DEATH = builder.comment("How much experience should players lose when they die?"
						, "zero is no loss, one is lose everything")
						.defineInRange("Loss on death", 0.05, 0d, 1d);
		LOSE_LEVELS_ON_DEATH = builder.comment("should loss of experience cross levels?"
						, "for example, if true, a player with 1 xp above their current level would lose the"
						, "[Loss on death] percentage of xp and fall below their current level.  However,"
						, "if false, the player would lose only 1 xp as that would put them at the base xp of their current level")
						.define("Lose Levels On Death", false);
		LOSE_ONLY_EXCESS = builder.comment("This setting only matters if [Lose Level On Death] is set to false."
						, "If this is true the [Loss On Death] applies only to the experience above the current level"
						, "for example if level 3 is 1000k xp and the player has 1020 and dies.  the player will only lose"
						, "the [Loss On Death] of the 20 xp above the level's base.")
						.define("Lose Only Excess", true);
		GLOBAL_MODIFIER = builder.comment("Modifies how much xp is earned.  This is multiplicative to the XP.")
						.define("Global Modifier", 1.0);
		SKILL_MODIFIERS = TomlConfigHelper.defineObject(builder.comment("Modifies xp gains for specific skills.  This is multiplicative to the XP.")
							, "Skill Modifiers"
							, CodecTypes.DOUBLE_CODEC
							, Collections.singletonMap("agility", 1.0)); 			
					
		
		//========LINEAR SECTION===============
		builder.comment("Settings for Linear XP configuration").push("LINEAR LEVELS");
		LINEAR_BASE_XP = builder.comment("what is the base xp to reach level 2 (this + level * xpPerLevel)")
							.defineInRange("Base XP", 250L, 0L, Long.MAX_VALUE);
		LINEAR_PER_LEVEL = builder.comment("What is the xp increase per level (baseXp + level * this)")
							.defineInRange("Per Level", 500d, 0d, Double.MAX_VALUE);		
		builder.pop(); //COMPLETE LINEAR BLOCK
		
		//========EXPONENTIAL SECTION==========
		builder.comment("Settings for Exponential XP configuration").push("EXPONENTIAL LEVELS");
		EXPONENTIAL_BASE_XP = builder.comment("What is the x in: x * ([Power Base]^([Per Level] * level))")
							.defineInRange("Base XP", 250, 1, Integer.MAX_VALUE);
		EXPONENTIAL_POWER_BASE = builder.comment("What is the x in: [Base XP] * (x^([Per Level] * level))")
							.defineInRange("Power Base", 1.104088404342588d, 0d, Double.MAX_VALUE);
		EXPONENTIAL_LEVEL_MOD = builder.comment("What is the x in: [Base XP] * ([Power Base]^(x * level))")
							.defineInRange("Per Level", 1.1, 0d, Double.MAX_VALUE);
		builder.pop(); //COMPLETE EXPONENTIAL BLOCK
		builder.pop(); //COMPLETE LEVELS BLOCK
		
	}
	
	private static BooleanValue[] REQ_ENABLED;
	
	private static final String REQ_ENABLED_SUFFIX = " Req Enabled";
	
	public static BooleanValue reqEnabled(ReqType type) {return REQ_ENABLED[type.ordinal()];}
	
	private static void buildRequirements(ForgeConfigSpec.Builder builder) {
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		
		builder.comment("Should requirements apply for the applicable action type").push("Requirements");		
		
		REQ_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+REQ_ENABLED_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		
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
	
	public static ConfigObject<Map<String, Map<String, Long>>> RECEIVE_DAMAGE_XP;
	public static ConfigObject<Map<String, Map<String, Long>>> DEAL_DAMAGE_XP;

	
	public static ConfigObject<Map<String, Double>> JUMP_XP;
	public static ConfigObject<Map<String, Double>> SPRINT_JUMP_XP;
	public static ConfigObject<Map<String, Double>> CROUCH_JUMP_XP;
	
	public static ConfigObject<Map<String, Double>> BREATH_CHANGE_XP;
	public static ConfigObject<Map<String, Double>> HEALTH_CHANGE_XP;
	public static ConfigObject<Map<String, Double>> HEALTH_INCREASE_XP;
	public static ConfigObject<Map<String, Double>> HEALTH_DECREASE_XP;
	public static ConfigObject<Map<String, Double>> SPRINTING_XP;
	public static ConfigObject<Map<String, Double>> SUBMERGED_XP;
	public static ConfigObject<Map<String, Double>> SWIMMING_XP;
	public static ConfigObject<Map<String, Double>> DIVING_XP;
	public static ConfigObject<Map<String, Double>> SURFACING_XP;
	public static ConfigObject<Map<String, Double>> SWIM_SPRINTING_XP;
	
	private static void buildEventBasedXPSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related to certain default event XP awards.").push("Event_XP_Specifics");
		
		builder.push("Damage").comment(
				"damage dealt and received is defined by the damage type",
				"or damage type tag preceding it.  xp is awarded based on",
				"the value below multiplied by the damage applied.");
			DEAL_DAMAGE_XP = TomlConfigHelper.defineObject(builder,
					"DEAL_DAMAGE", CodecTypes.DAMAGE_XP_CODEC, Map.of(
							"minecraft:generic_kill", Map.of("combat", 1l),
							"minecraft:player_attack", Map.of("combat", 1l),
							"#minecraft:is_projectile", Map.of("archery", 1l)));
			RECEIVE_DAMAGE_XP = TomlConfigHelper.defineObject(builder,
					"RECEIVE_DAMAGE", CodecTypes.DAMAGE_XP_CODEC, Map.of(
							"minecraft:generic_kill", Map.of("endurance", 1l),
							"#pmmo:environment", Map.of("endurance", 10l),
							"#pmmo:impact", Map.of("endurance", 15l),
							"#pmmo:magic", Map.of("magic", 15l),
							"#minecraft:is_projectile", Map.of("endurance", 15l)));
		builder.pop();
		
		builder.push("Jumps");
			JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
			SPRINT_JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SPRINT_JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
			CROUCH_JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"CROUCH_JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
		builder.pop();
		
		builder.push("Player_Actions");
			BREATH_CHANGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 					
					"BREATH_CHANGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 1d));
			HEALTH_CHANGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"HEALTH_CHANGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 0d));
			HEALTH_INCREASE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder,
				"HEALTH_INCREASE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 1d));
			HEALTH_DECREASE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder,
				"HEALTH_DECREASE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 1d));
			SPRINTING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SPRINTING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 100d));
			SUBMERGED_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SUBMERGED Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 1d));
			SWIMMING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SWIMMING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 100d));
			DIVING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"DIVING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 150d));
			SURFACING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SURFACING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 50d));
			SWIM_SPRINTING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SWIM_SPRINTING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 200d));
		builder.pop();
			
		builder.pop();
	}
	
	public static ForgeConfigSpec.IntValue PARTY_RANGE;
	public static ForgeConfigSpec.DoubleValue PARTY_BONUS;
	
	private static void buildPartySettings(ForgeConfigSpec.Builder builder) {
		builder.comment("All settings governing party behavior").push("Party");
			PARTY_RANGE = builder.comment("How close do party members have to be to share experience.")
					.defineInRange("Party Range", 50, 0, Integer.MAX_VALUE);
			PARTY_BONUS = builder.comment("How much bonus xp should parties earn.",
					"This value is multiplied by the party size.")
					.defineInRange("Party Bonus", 1.05, 1.0, Double.MAX_VALUE);
		builder.pop();
	}
	
	public static ForgeConfigSpec.BooleanValue MOB_SCALING_ENABLED;
	
	public static ForgeConfigSpec.ConfigValue<Boolean> MOB_USE_EXPONENTIAL_FORMULA;
	public static ForgeConfigSpec.ConfigValue<Integer>  MOB_SCALING_AOE;
	public static ForgeConfigSpec.ConfigValue<Integer> 	MOB_SCALING_BASE_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_LINEAR_PER_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_EXPONENTIAL_POWER_BASE;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_EXPONENTIAL_LEVEL_MOD;

	public static ConfigObject<Map<String, Map<String, Double>>> MOB_SCALING;
	
	private static void buildMobScalingSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("settings related to how strong mobs get based on player level.").push("Mob_Scaling");
		
		MOB_SCALING_ENABLED = builder.comment("Should mob scaling be turned on.")
				.define("Enable Mob Scaling", true);
		MOB_SCALING_AOE = builder.comment("How far should players be from spawning mobs to affect scaling?")
				.defineInRange("Scaling AOE", 150, 0, Integer.MAX_VALUE);
		MOB_SCALING_BASE_LEVEL = builder.comment("what is the minimum level for scaling to kick in")
				.defineInRange("Base Level", 0, 0, Integer.MAX_VALUE);
		
			builder.comment("How should mob attributes be calculated with respect to the player's level.").push("Formula");
				MOB_USE_EXPONENTIAL_FORMULA = builder.comment("should levels be determined using an exponential formula?")
						.define("Use Exponential Formula", true);
				//========LINEAR SECTION===============
				builder.comment("Settings for Linear scaling configuration").push("LINEAR_LEVELS");				
				MOB_LINEAR_PER_LEVEL = builder.comment("What is the xp increase per level ((level - base_level) * this)")
									.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);		
				builder.pop(); //COMPLETE LINEAR BLOCK
				
				//========EXPONENTIAL SECTION==========
				builder.comment("Settings for Exponential scaling configuration").push("EXPONENTIAL_LEVELS");
				MOB_EXPONENTIAL_POWER_BASE = builder.comment("What is the x in: (x^([Per Level] * level))")
									.defineInRange("Power Base", 1.104088404342588d, 0d, Double.MAX_VALUE);
				MOB_EXPONENTIAL_LEVEL_MOD = builder.comment("What is the x in: ([Power Base]^(x * level))")
									.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);
				builder.pop();
			builder.pop(); //Formula
			builder.comment("These settings control which skills affect scaling and the ratio for each skill"
					, "minecraft:generic.max_health: 1 = half a heart, or 1 hitpoint"
					, "minecraft:generic.movement_speed: 0.7 is base for most mobs.  this is added to that. so 0.7 from scaling is double speed"
					, "minecraft:generic.attack_damage: is a multiplier of their base damage.  1 = no change, 2 = double damage"
					, "negative values are possible and you can use this to create counterbalance skills",""
					, "NOTE: TOML WILL MOVE THE QUOTATIONS OF YOUR ATTRIBUTE ID AND BREAK YOUR CONFIG."
					, "ENSURE YOU HAVE FORCIBLY PUT YOUR QUOTES AROUND YOUR ATTRIBUTE ID BEFORE SAVING.").push("Scaling_Settings");
				MOB_SCALING = TomlConfigHelper.<Map<String, Map<String, Double>>>defineObject(builder,
						"Mob Scaling IDs and Ratios", Codec.unboundedMap(Codec.STRING, CodecTypes.DOUBLE_CODEC), Map.of(
								"minecraft:generic.max_health", Map.of("combat", 0.001),
								"minecraft:generic.movement_speed", Map.of("combat", 0.000001),
								"minecraft:generic.attack_damage", Map.of("combat", 0.0001)
						));
			builder.pop(); //Scaling Settings
		builder.pop(); //Mob_Scaling
	}
	
	public static ForgeConfigSpec.ConfigValue<Boolean> VEIN_ENABLED;
	public static ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_SETTING;
	public static ForgeConfigSpec.ConfigValue<Integer> DEFAULT_CONSUME;
	public static ForgeConfigSpec.DoubleValue VEIN_CHARGE_MODIFIER;
	public static ConfigValue<List<? extends String>> VEIN_BLACKLIST;
	public static ForgeConfigSpec.DoubleValue BASE_CHARGE_RATE;
	public static ForgeConfigSpec.IntValue BASE_CHARGE_CAP;
	
	private static void buildVeinMinerSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related to the Vein Miner").push("Vein_Miner");
		VEIN_ENABLED = builder.comment("setting to false disables all vein features")
				.define("vein enabled", true);
		REQUIRE_SETTING = builder.comment("If true, default consume will be ignored in favor of only allowing"
				, "veining blocks with declared values.")
				.define("Require Settings", false);
		DEFAULT_CONSUME = builder.comment("how much a block should consume if no setting is defined.")
				.define("Vein Mine Default Consume", 1);
		VEIN_CHARGE_MODIFIER = builder.comment("a multiplier to all vein charge rates.")
				.defineInRange("Vein Charge Modifier", 1.0, 0.0, Double.MAX_VALUE);
		VEIN_BLACKLIST = builder.comment("Tools in this list do not cause the vein miner to trigger")
				.defineList("Vein_Blacklist", new ArrayList<>(List.of("silentgear:saw")) , s -> s instanceof String);
		BASE_CHARGE_RATE = builder.comment("A constant charge rate given to all players regardless of equipment.",
				"Items worn will add to this amount, not replace it.")
				.defineInRange("base charge rate", 0.01, 0.0, Double.MAX_VALUE);
		BASE_CHARGE_CAP = builder.comment("A minimum capacity given to all players regardless of equipment.",
				"Items worn will add to this amount, not replace it.")
				.defineInRange("base vein capacity", 0, 0, Integer.MAX_VALUE);
		builder.pop();
	}
}
