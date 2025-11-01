package harmonised.pmmo.config;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    public static final ConfigListener CONFIG = new ConfigListener();
    public static ServerData server() {return CONFIG.server();}
    public static AutoValueConfig autovalue() {return CONFIG.autovalues();}
    public static GlobalsConfig globals() {return CONFIG.globals();}
    public static PerksConfig perks() {return CONFIG.perks();}
    public static SkillsConfig skills() {return CONFIG.skills();}
    public static AntiCheeseConfig anticheese() {return CONFIG.anticheese();}

    public static ModConfigSpec CLIENT_CONFIG;
    public static ModConfigSpec COMMON_CONFIG;

    static {
        ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        setupClient(CLIENT_BUILDER);
        setupCommon(COMMON_BUILDER);

        CLIENT_CONFIG = CLIENT_BUILDER.build();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    //====================CLIENT SETTINGS===============================

    private static void setupClient(ModConfigSpec.Builder builder) {
        builder.comment("PMMO Client Configuration").push("Client");

        buildGUI(builder);
        buildTooltips(builder);
        buildVein(builder);
        buildTutorial(builder);

        builder.pop();
    }

    public static ModConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_X;
    public static ModConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_Y;
    public static ModConfigSpec.BooleanValue SKILL_LIST_USE_ICONS;
    public static ModConfigSpec.ConfigValue<Double> VEIN_GAUGE_OFFSET_X;
    public static ModConfigSpec.ConfigValue<Double> VEIN_GAUGE_OFFSET_Y;
    public static ModConfigSpec.ConfigValue<Double> GAIN_LIST_OFFSET_X;
    public static ModConfigSpec.ConfigValue<Double> GAIN_LIST_OFFSET_Y;
    public static ModConfigSpec.ConfigValue<Boolean> SKILL_LIST_DISPLAY;
    public static ModConfigSpec.ConfigValue<Boolean> GAIN_LIST_DISPLAY;
    public static ModConfigSpec.ConfigValue<Boolean> VEIN_GAUGE_DISPLAY;
    public static ModConfigSpec.ConfigValue<Integer> SECTION_HEADER_COLOR;
    public static ModConfigSpec.ConfigValue<Integer> SALVAGE_ITEM_COLOR;
    public static ModConfigSpec.ConfigValue<Integer> GAIN_LIST_LINGER_DURATION;
    public static ModConfigSpec.ConfigValue<List<? extends String>> GAIN_BLACKLIST;
    public static ModConfigSpec.BooleanValue HIDE_SKILL_BUTTON;
    public static ModConfigSpec.ConfigValue<Integer> SKILL_BUTTON_X;
    public static ModConfigSpec.ConfigValue<Integer> SKILL_BUTTON_Y;
    public static ModConfigSpec.BooleanValue SKILLUP_UNLOCKS;
    public static ModConfigSpec.BooleanValue SKILLUP_UNLOCKS_STRICT;

    private static void buildGUI(ModConfigSpec.Builder builder) {
        builder.comment("Configuration settings for the guis").push("GUI");

        SKILL_LIST_OFFSET_X = builder.comment("how far right from the top left corner the skill list should be")
                .defineInRange("Skill List Xoffset", 0.01d, 0, 1);
        SKILL_LIST_OFFSET_Y = builder.comment("how far down from the top left corner the skill list should be")
                .defineInRange("Skill List Yoffset", 0.0005d, 0, 1);
        SKILL_LIST_USE_ICONS = builder.comment("if true, uses the icon from the skill list instead of the skill name")
                .define("Skill List Use Icons", false);
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
        GAIN_LIST_LINGER_DURATION = builder.comment("How long, in ticks, items on the gain list should stay on screen before disappearing")
                .define("Gain List Linger Duration", 100);
        GAIN_BLACKLIST = builder.comment("skills that should not show their gains in the gain list.  this can be used to limit spammy skills")
                .defineList("Gain Blacklist", new ArrayList<>(), s -> s instanceof String);
        HIDE_SKILL_BUTTON = builder.comment("if true, removes the skills button from the inventory screen")
                .define("hide skill button", false);
        SKILL_BUTTON_X = builder.comment("the horizontal location of the skill button in the inventory.", "Default = 126.  For removing overlaps, 150 is a good setting")
                .define("skill_button_x", 126);
        SKILL_BUTTON_Y = builder.comment("the vertical location (from center) of the skill button in the inventory.", "Default = -22")
                .define("skill_button_y", -22);
        SKILLUP_UNLOCKS = builder.comment("If enabled, lists in chat all features unlocked when a skill levels up.")
                .define("skillup_unlocks", true);
        SKILLUP_UNLOCKS_STRICT = builder.comment("should skillups only show in chat if content is actually unlocked.")
                .define("strict_skillup_unlocks", true);

        builder.pop();
    }

    public static ModConfigSpec.BooleanValue HIDE_MET_REQS;

    private static BooleanValue[] TOOLTIP_REQ_ENABLED;
    private static BooleanValue[] TOOLTIP_XP_ENABLED;
    private static BooleanValue[] TOOLTIP_BONUS_ENABLED;

    private static final String TOOLTIP_SUFFIX = " tooltip enabled";

    public static BooleanValue tooltipReqEnabled(ReqType type) {
        return TOOLTIP_REQ_ENABLED[type.ordinal()];
    }

    public static BooleanValue tooltipXpEnabled(EventType type) {
        return TOOLTIP_XP_ENABLED[type.ordinal()];
    }

    public static BooleanValue tooltipBonusEnabled(ModifierDataType type) {
        return TOOLTIP_BONUS_ENABLED[type.ordinal()];
    }

    private static void buildTooltips(ModConfigSpec.Builder builder) {
        builder.comment("Generic Tooltip Settings").push("ToolTip_Settings");
        HIDE_MET_REQS = builder.comment("Should met reqs be hidden on the tooltip.")
                .define("Hide Met Req Tooltips", true);
        builder.pop();
        builder.comment("This section covers the various tooltip elements and whether they should be enabled").push("Tooltip_Visibility");

        List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
        builder.push("Requirement_Tooltips");
        TOOLTIP_REQ_ENABLED = rawReqList.stream().map((t) -> {
            return builder.define(t.toString() + " Req " + TOOLTIP_SUFFIX, true);
        }).toArray(BooleanValue[]::new);
        builder.pop(); //requirements

        List<EventType> rawEventList = new ArrayList<>(Arrays.asList(EventType.values()));
        builder.push("Xp_Gain_Tooltips");
        TOOLTIP_XP_ENABLED = rawEventList.stream().map((t) -> {
            return builder.define(t.toString() + " XP Gain " + TOOLTIP_SUFFIX, true);
        }).toArray(BooleanValue[]::new);
        builder.pop(); //xp gains

        List<ModifierDataType> rawBonusList = new ArrayList<>(Arrays.asList(ModifierDataType.values()));
        builder.push("Bonus_Tooltips");
        TOOLTIP_BONUS_ENABLED = rawBonusList.stream().map((t) -> {
            return builder.define(t.toString() + " Bonus " + TOOLTIP_SUFFIX, true);
        }).toArray(BooleanValue[]::new);
        builder.pop(); //bonuses

        builder.pop(); //outer
    }

    public static IntValue VEIN_LIMIT;

    private static void buildVein(ModConfigSpec.Builder builder) {
        builder.comment("Client Settings Related to the Vein Mining Ability").push("Vein_Miner");

        VEIN_LIMIT = builder.comment("The max blocks a vein activation should consume regardless of charge")
                .defineInRange("Vein_Limit", 64, 0, Integer.MAX_VALUE);

        builder.pop();
    }

    public static BooleanValue BREAK_NERF_HIGHLIGHTS;
    public static BooleanValue BLOCK_OWNER_HIGHLIGHTS;
    public static BooleanValue SALVAGE_HIGHLIGHTS;
    public static BooleanValue BREAK_SPEED_PERKS;

    private static void buildTutorial(ModConfigSpec.Builder builder) {
        builder.comment("Toggles for helpful features related to mechanics").push("Tutorial");

        BREAK_NERF_HIGHLIGHTS = builder.comment("Should blocks affected by Reuse Penalty show a red outline?")
                .define("Enable Reuse Penalty Highlights", true);
        BLOCK_OWNER_HIGHLIGHTS = builder.comment("Should the owner of a block show when hovering?")
                .define("Enable Owner Highlights", true);
        SALVAGE_HIGHLIGHTS = builder.comment("Should hovering over a salvage block show helpful tips?")
                .define("Enable Salvage Tips", true);
        BREAK_SPEED_PERKS = builder.comment("Are break speed perks enabled. can be toggled in-game with a keybind")
                .define("break_speed_enabled", true);

        builder.pop();
    }

    //====================COMMON SETTINGS===============================

    private static void setupCommon(ModConfigSpec.Builder builder) {
        builder.comment("===============================================", "", "",
                "Most Configurations are found in the server config",
                "You can find that in worldname/serverconfig/",
                "", "", "===============================================").push("Common");

        buildMsLoggy(builder);

        builder.pop(); //Common Blocks
    }

    public static ConfigValue<List<? extends String>> INFO_LOGGING;
    public static ConfigValue<List<? extends String>> DEBUG_LOGGING;
    public static ConfigValue<List<? extends String>> WARN_LOGGING;
    public static ConfigValue<List<? extends String>> ERROR_LOGGING;
    public static ConfigValue<List<? extends String>> FATAL_LOGGING;

    private static void buildMsLoggy(ModConfigSpec.Builder builder) {
        builder.comment("PMMO Error Logging Configuration", "",
                "===================================================",
                "To enable Logging with MsLoggy, enter a logging",
                "keyword into the logging level list that you want.",
                "the list of keywords are (lowercase only):",
                "'api', 'autovalues', 'chunk', 'data', 'event', ",
                "'feature', 'gui', 'loading', 'network', 'req', and 'xp'",
                "===================================================").push("Ms Loggy");

        INFO_LOGGING = builder.comment("Which MsLoggy info logging should be enabled?  This will flood your log with data, but provides essential details",
                        " when trying to find data errors and bug fixing.  ")
                .defineList("Info Logging", new ArrayList<>(List.of(LOG_CODE.LOADING.code, LOG_CODE.NETWORK.code, LOG_CODE.API.code)), s -> s instanceof String);
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
}
