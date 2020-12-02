package harmonised.pmmo.config;

import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Config( modid = Reference.MOD_ID )
@Config.LangKey( "pmmo.config" )
public class FConfig
{
//    public static final Logger LOGGER = LogManager.getLogger();

//    public static Configuration forgeConfig;

    public static Map<String, Double> localConfig = new HashMap<>();
    private static Map<String, Double> config = new HashMap<>();

    //Client only, too lazy to put it somewhere better
    private static final Map<String, Double> abilities = new HashMap<>();
    private static Map<String, Double> preferences = new HashMap<>();
    private static Map<String, Map<Skill, Double>> xpBoosts = new HashMap<>();

    //Miscellaneous
    @Config.Comment( "Should the Welcome message come up?" )
    @Config.Name( "showWelcome" )
    public static boolean showWelcome = true;

    @Config.Comment( "Should your personal Donator Welcome message come up?" )
    @Config.Name( "showPatreonWelcome" )
    public static boolean showPatreonWelcome = true;

    @Config.Comment( "Should Xp Boosts be scaled by the item Durability? (Max boost at Max durability, 50% at half Durability)" )
    @Config.Name( "scaleXpBoostByDurability" )
    public static boolean scaleXpBoostByDurability = true;

    @Config.Comment( "At what durability percentage should the xp bonus go away fully at and below? (25 means at 25% or below durability, there is no longer any xp boost)" )
    @Config.Name( "scaleXpBoostByDurabilityStart" )
    @Config.RangeDouble( min = 0D, max = 100D )
    public static double scaleXpBoostByDurabilityStart = 0D;

    @Config.Comment( "At what durability percentage should the xp bonus start going down from? (75 means that at 75% durability or above, the xp bonus will be max, and at 37.5%, the xp boost will be half" )
    @Config.Name( "scaleXpBoostByDurabilityEnd" )
    @Config.RangeDouble( min = 0D, max = 100D )
    public static double scaleXpBoostByDurabilityEnd = 75D;

    //Party
    @Config.Comment( "In what range do Party members have to be to benefit from the other members?" )
    @Config.Name( "partyRange" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double partyRange = 64D;

    @Config.Comment( "How many Members can a party have?" )
    @Config.Name( "partyMaxMembers" )
    @Config.RangeInt( min = 1, max = 1000000000 )
    public static int partyMaxMembers = 10;

    @Config.Comment( "How much bonus xp a Party gains extra, per player? (5 = 1 player -> 5% xp bonus, 2 players -> 10% xp bonus" )
    @Config.Name( "partyXpIncreasePerPlayer" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double partyXpIncreasePerPlayer = 5D;

    @Config.Comment( "How much bonus xp is the maximum that a Party can receive? (50 = 50% increase max. If partyXpIncreasePerPlayer is 5, and there are 20 members, the xp bonus caps at 50%, at 10 members)" )
    @Config.Name( "maxPartyXpBonus" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double maxPartyXpBonus = 50D;

    @Config.Comment( "How much damage you can deal to people in the same Party (0 = no damage, 100 = full damage)" )
    @Config.Name( "partyFriendlyFireAmount" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double partyFriendlyFireAmount = 100/3D;

    @Config.Comment( "Should players leave their party if they disconnect?" )
    @Config.Name( "autoLeavePartyOnDisconnect" )
    public static boolean autoLeavePartyOnDisconnect = false;

    //Vein Mining
    @Config.Comment( "Is vein mining allowed? true = on, false = off" )
    @Config.Name( "veiningAllowed" )
    public static boolean veiningAllowed = true;

    @Config.Comment( "Should veining wood material blocks start from the highest block?" )
    @Config.Name( "veinWoodTopToBottom" )
    public static boolean veinWoodTopToBottom = true;

    @Config.Comment( "Should a succesful sleep recharge every player currently in that world Vein Charge?" )
    @Config.Name( "sleepRechargesAllPlayersVeinCharge" )
    public static boolean sleepRechargesAllPlayersVeinCharge = true;

    @Config.Comment( "Should players be allowed to vein blocks that they did not place?" )
    @Config.Name( "veiningOtherPlayerBlocksAllowed" )
    public static boolean veiningOtherPlayerBlocksAllowed = false;

    @Config.Comment( "What is the maximum distance a player's vein can reach?" )
    @Config.Name( "veinMaxDistance" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double veinMaxDistance = 1000D;

    @Config.Comment( "How many blocks max can be veined?" )
    @Config.Name( "veinMaxBlocks" )
    @Config.RangeInt( min = 1, max = 1000000000 )
    public static int veinMaxBlocks = 10000;

    @Config.Comment( "How many blocks get broken every tick?" )
    @Config.Name( "veinSpeed" )
    @Config.RangeInt( min = 1, max = 1000000000 )
    public static int veinSpeed = 1;

    @Config.Comment( "How much is the lowest cost for each block veined? (1 = 1 charge, 1 charge regens per second)" )
    @Config.Name( "minVeinCost" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double minVeinCost = 0.5D;

    @Config.Comment( "What is the lowest hardness for each block veined? (Crops have 0 hardness, this makes crops not infinitely veined)" )
    @Config.Name( "minVeinHardness" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double minVeinHardness = 0.5D;

    @Config.Comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160) * 320 = 100 hardness worth of blocks, which is 2.0 Obsidian, or 33.3 Coal Ore)" )
    @Config.Name( "levelsPerHardnessMining" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double levelsPerHardnessMining = 160D;

    @Config.Comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160) * 320 = 100 hardness worth of logs, which is 50 Logs)" )
    @Config.Name( "levelsPerHardnessWoodcutting" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double levelsPerHardnessWoodcutting = 160D;

    @Config.Comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 16, your level is 50, and you have 64 charge, you can vein (50 / 320) * 320 = 50 hardness worth of ground, which is 100 Dirt)" )
    @Config.Name( "levelsPerHardnessExcavation" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double levelsPerHardnessExcavation = 320D;

    @Config.Comment( "Every how many levels does 1 charge become worth +1 hardness? Plants have no hardness, but there is a minimum hardness while veining config in here, which is 0.5 by default, making it 200 plants at level 50 farming, with 320 charge, if this is set to 160" )
    @Config.Name( "levelsPerHardnessFarming" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double levelsPerHardnessFarming = 160D;

    @Config.Comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 80, your level is 50, and you have 320 charge, you can vein (50 / 80) * 320 = 200 hardness worth of Crafting Related (Such as wool, carpet, bed) blocks, which depends on how hard they are)" )
    @Config.Name( "levelsPerHardnessCrafting" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double levelsPerHardnessCrafting = 160D;

    @Config.Comment( "How much vein charge can a player hold at max? (1 recharges every second)" )
    @Config.Name( "maxVeinCharge" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double maxVeinCharge = 320D;

    @Config.Comment( "How much hunger should be exhausted per block veined?" )
    @Config.Name( "exhaustionPerBlock" )
    @Config.RangeDouble( min = 1, max = 1000000000 )
    public static double exhaustionPerBlock = 0.2D;

    //Mob Scaling
    @Config.Comment( "What is the maximum amount an aggressive mob's damage will be boosted?" )
    @Config.Name( "maxMobDamageBoost" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double maxMobDamageBoost = 100D;

    @Config.Comment( "How much an aggresive mob's damage will increase per one Power Level?" )
    @Config.Name( "mobDamageBoostPerPowerLevel" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double mobDamageBoostPerPowerLevel = 1D;

    @Config.Comment( "What is the maximum amount an aggressive mob's HP will be boosted?" )
    @Config.Name( "maxMobHPBoost" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double maxMobHPBoost = 1000D;

    @Config.Comment( "How much an aggresive mob's HP will increase per one Power Level?" )
    @Config.Name( "mobHPBoostPerPowerLevel" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double mobHPBoostPerPowerLevel = 5D;

    @Config.Comment( "What is the maximum amount an aggressive mob's speed will be boosted?" )
    @Config.Name( "maxMobSpeedBoost" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double maxMobSpeedBoost = 10D;

    @Config.Comment( "How much an aggresive mob's speed will increase per one Power Level?" )
    @Config.Name( "mobSpeedBoostPerPowerLevel" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double mobSpeedBoostPerPowerLevel = 1D;

    @Config.Comment( "Should mob xp multipliers inside of biomes be enabled? false means no multipliers" )
    @Config.Name( "biomeMobMultiplierEnabled" )
    public static boolean biomeMobMultiplierEnabled = true;

    //Requirements
    @Config.Comment( "Should wear requirements be enabled? false means no requirements" )
    @Config.Name( "wearReqEnabled" )
    public static boolean wearReqEnabled = true;

    @Config.Comment( "Should Enchantment Use requirements be enabled? false means no requirements" )
    @Config.Name( "enchantUseReqEnabled" )
    public static boolean enchantUseReqEnabled = true;

    @Config.Comment( "Should Enchantment Use requirements automatically scale according to previous values, provided they exist? example: level1Req = 5 agility, level2Req = 10 farming - Level 4 enchantment would require level 10 agility, and level 20 farming (highestSpecifiedLevelReqs / highestSpecifiedLevel * enchantLevel)" )
    @Config.Name( "enchantUseReqAutoScaleEnabled" )
    public static boolean enchantUseReqAutoScaleEnabled = true;

    @Config.Comment( "Should tool requirements be enabled? false means no requirements" )
    @Config.Name( "toolReqEnabled" )
    public static boolean toolReqEnabled = true;

    @Config.Comment( "Should weapon requirements be enabled? false means no requirements" )
    @Config.Name( "weaponReqEnabled" )
    public static boolean weaponReqEnabled = true;

    @Config.Comment( "Should mob kill req be enabled? false means no requirements" )
    @Config.Name( "killReqEnabled" )
    public static boolean killReqEnabled = true;

    @Config.Comment( "Should mob kill xp be enabled? false means no requirements" )
    @Config.Name( "killXpEnabled" )
    public static boolean killXpEnabled = true;

    @Config.Comment( "Should mob rare drops be enabled? false means no requirements" )
    @Config.Name( "mobRareDropEnabled" )
    public static boolean mobRareDropEnabled = true;

    @Config.Comment( "Should use requirements be enabled? false means no requirements" )
    @Config.Name( "useReqEnabled" )
    public static boolean useReqEnabled = true;

    @Config.Comment( "Should place requirements be enabled? false means no requirements" )
    @Config.Name( "placeReqEnabled" )
    public static boolean placeReqEnabled = true;

    @Config.Comment( "Should break requirements be enabled? false means no requirements" )
    @Config.Name( "breakReqEnabled" )
    public static boolean breakReqEnabled = true;

    @Config.Comment( "Should biome requirements be enabled? false means no requirements" )
    @Config.Name( "biomeReqEnabled" )
    public static boolean biomeReqEnabled = true;

    @Config.Comment( "Should certain items be restricted from being crafted, without the level requirement?" )
    @Config.Name( "craftReqEnabled" )
    public static boolean craftReqEnabled = true;

    @Config.Comment( "Should biome negative effects be enabled? false means no negative effects" )
    @Config.Name( "negativeBiomeEffectEnabled" )
    public static boolean negativeBiomeEffectEnabled = true;

    @Config.Comment( "Should biome positive effects be enabled? false means no positive effects" )
    @Config.Name( "positiveBiomeEffectEnabled" )
    public static boolean positiveBiomeEffectEnabled = true;

    @Config.Comment( "Should xp multipliers be enabled? false means no multipliers" )
    @Config.Name( "biomeXpBonusEnabled" )
    public static boolean biomeXpBonusEnabled = true;

    @Config.Comment( "Should xp values for general things be enabled? (Such as catching fish)" )
    @Config.Name( "xpValueGeneralEnabled" )
    public static boolean xpValueGeneralEnabled = true;

    @Config.Comment( "Should xp values for breaking things first time be enabled? false means only Hardness xp is awarded for breaking" )
    @Config.Name( "xpValueBreakingEnabled" )
    public static boolean xpValueBreakingEnabled = true;

    @Config.Comment( "Should ores be enabled? false means no extra chance" )
    @Config.Name( "oreEnabled" )
    public static boolean oreEnabled = true;

    @Config.Comment( "Should logs be enabled? false means no extra chance" )
    @Config.Name( "logEnabled" )
    public static boolean logEnabled = true;

    @Config.Comment( "Should plants be enabled? false means no extra chance" )
    @Config.Name( "plantEnabled" )
    public static boolean plantEnabled = true;

    @Config.Comment( "Is Salvaging items using the Repairing skill enabled? false = off" )
    @Config.Name( "salvageEnabled" )
    public static boolean salvageEnabled = true;

    @Config.Comment( "Is catching items from Fish Pool while Fishing enabled? false = off" )
    @Config.Name( "fishPoolEnabled" )
    public static boolean fishPoolEnabled = true;

    @Config.Comment( "Should fished items have a chance at being Enchanted? enabled? false = off" )
    @Config.Name( "fishEnchantPoolEnabled" )
    public static boolean fishEnchantPoolEnabled = true;

    @Config.Comment( "Commands being fired on specific level ups enabled? false = off" )
    @Config.Name( "levelUpCommandEnabled" )
    public static boolean levelUpCommandEnabled = true;

    @Config.Comment( "Main held items xp multiplier enabled? false = off" )
    @Config.Name( "heldItemXpBoostEnabled" )
    public static boolean heldItemXpBoostEnabled = true;

    @Config.Comment( "worn items xp boost enabled? false = off" )
    @Config.Name( "wornItemXpBoostEnabled" )
    public static boolean wornItemXpBoostEnabled = true;

    @Config.Comment( "Should config from default_data.json be loaded? false means only data.json is loaded" )
    @Config.Name( "loadDefaultConfig" )
    public static boolean loadDefaultConfig = true;

    @Config.Comment( "When a Tool requirement is not met, should the player be stopped from breaking with it completely?" )
    @Config.Name( "strictReqTool" )
    public static boolean strictReqTool = false;

    @Config.Comment( "When a Kill requirement is not met, should the player be stopped from dealing any damage?" )
    @Config.Name( "strictReqKill" )
    public static boolean strictReqKill = false;

    @Config.Comment( "When a Weapon requirement is not met, should the player be stopped from dealing any damage?" )
    @Config.Name( "strictReqWeapon" )
    public static boolean strictReqWeapon = false;

    @Config.Comment( "When a Wear requirement is not met, should the item be dropped?" )
    @Config.Name( "strictReqWear" )
    public static boolean strictReqWear = false;

    @Config.Comment( "When a Use Enchantment requirement is not met, should the item be dropped?" )
    @Config.Name( "strictReqUseEnchantment" )
    public static boolean strictReqUseEnchantment = false;

    //Levels
    @Config.Comment( "What is the global max level" )
    @Config.Name( "maxLevel" )
    @Config.RangeInt( min = 1, max = 1000000 )
    public static int maxLevel = 999;

    @Config.Comment( "What is the baseXp to reach level 2 ( baseXp + level * xpPerLevel )" )
    @Config.Name( "baseXp" )
    @Config.RangeInt( min = 1, max = 1000000 )
    public static int baseXp = 250;

    @Config.Comment( "What is the xp increase per level ( baseXp + level * xpPerLevel )" )
    @Config.Name( "xpIncreasePerLevel" )
    @Config.RangeInt( min = 1, max = 1000000 )
    public static int xpIncreasePerLevel = 50;

    @Config.Comment( "Every how many levels should a level up broadcast be sent to all players? (10 = every 10 levels)" )
    @Config.Name( "levelsPerMilestone" )
    @Config.RangeInt( min = 1, max = 1000000 )
    public static int levelsPerMilestone = 10;

    @Config.Comment( "Should a player have all their skills wiped to level 1 upon death?" )
    @Config.Name( "wipeAllSkillsUponDeathPermanently" )
    public static boolean wipeAllSkillsUponDeathPermanently = false;

    @Config.Comment( "Should every 10th level up be broadcast to everyone?" )
    @Config.Name( "broadcastMilestone" )
    public static boolean broadcastMilestone = true;

    @Config.Comment( "Should fireworks appear on level up?" )
    @Config.Name( "levelUpFirework" )
    public static boolean levelUpFirework = true;

    @Config.Comment( "Should fireworks appear on Milestone level up, to other players?" )
    @Config.Name( "milestoneLevelUpFirework" )
    public static boolean milestoneLevelUpFirework = true;

    @Config.Comment( "Should levels be determined using an Exponential formula? (false = the original way)" )
    @Config.Name( "useExponentialFormula" )
    public static boolean useExponentialFormula = false;

    @Config.Comment( "What is the x in: x * ( exponentialBase^( exponentialRate * level ) )" )
    @Config.Name( "exponentialBaseXp" )
    @Config.RangeDouble( min = 1, max = 1000000 )
    public static double exponentialBaseXp = 83D;

    @Config.Comment( "What is the x in: exponentialBaseXp * ( x^( exponentialRate * level ) )" )
    @Config.Name( "exponentialBase" )
    @Config.RangeDouble( min = 1, max = 1000000 )
    public static double exponentialBase = 1.104088404342588D;

    @Config.Comment( "What is the x in: exponentialBaseXp * ( exponentialBase^( x * level ) )" )
    @Config.Name( "exponentialRate" )
    @Config.RangeDouble( min = 1, max = 1000000 )
    public static double exponentialRate = 1D;

    //Multipliers
    @Config.Comment( "How much xp everyone gains (1 = normal, 2 = twice as much)" )
    @Config.Name( "globalMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double globalMultiplier = 1D;

    @Config.Comment( "How much xp everyone gains on Peaceful Difficulty (1 = normal, 2 = twice as much)" )
    @Config.Name( "peacefulMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double peacefulMultiplier = 1/3D;

    @Config.Comment( "How much xp everyone gains on Easy Difficulty (1 = normal, 2 = twice as much)" )
    @Config.Name( "easyMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double easyMultiplier = 2/3D;

    @Config.Comment( "How much xp everyone gains on Normal Difficulty (1 = normal, 2 = twice as much)" )
    @Config.Name( "normalMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double normalMultiplier = 1D;

    @Config.Comment( "How much xp everyone gains on Hard Difficulty (1 = normal, 2 = twice as much)" )
    @Config.Name( "hardMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double hardMultiplier = 4/3D;

    @Config.Comment( "How much xp you get in biomes you do not meet the requirements for (1 = Full xp, 0.5 = Half xp)" )
    @Config.Name( "biomePenaltyMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double biomePenaltyMultiplier = 0.5D;

    @Config.Comment( "How much of the xp above whole level you loose (1 = 100% = from 5.5 to 5.0, 0.5 = 50% = from 5.5 to 5.25" )
    @Config.Name( "deathXpPenaltyMultiplier" )
    @Config.RangeDouble( min = 0, max = 1000 )
    public static double deathXpPenaltyMultiplier = 0.5D;

    //GUI
    @Config.Comment( "GUI bar position X (Width)" )
    @Config.Name( "barOffsetX" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double barOffsetX = 0.5D;

    @Config.Comment( "GUI bar position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
    @Config.Name( "barOffsetY" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double barOffsetY = 0D;

    @Config.Comment( "GUI bar position X (Width)" )
    @Config.Name( "veinBarOffsetX" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double veinBarOffsetX = 0.5D;

    @Config.Comment( "GUI bar position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
    @Config.Name( "veinBarOffsetY" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double veinBarOffsetY = 0.65D;

    @Config.Comment( "GUI Xp drops position X (Width)" )
    @Config.Name( "xpDropOffsetX" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropOffsetX = 0.5D;

    @Config.Comment( "GUI Xp drops position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
    @Config.Name( "xpDropOffsetY" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropOffsetY = 0D;

    @Config.Comment( "GUI Skills List position X (Width)" )
    @Config.Name( "skillListOffsetX" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double skillListOffsetX = 0D;

    @Config.Comment( "GUI Skills List position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
    @Config.Name( "skillListOffsetY" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double skillListOffsetY = 0D;

    @Config.Comment( "How far away does the Xp Drop spawn" )
    @Config.Name( "xpDropSpawnDistance" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropSpawnDistance = 50D;

    @Config.Comment( "How much out of MaxOpacity does the Xp Drop become visible per 1 distance" )
    @Config.Name( "xpDropOpacityPerTime" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropOpacityPerTime = 5D;

    @Config.Comment( "How opaque (visible) can the xp drop get?" )
    @Config.Name( "xpDropMaxOpacity" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropMaxOpacity = 200D;

    @Config.Comment( "At what age do xp drops start to decay?" )
    @Config.Name( "xpDropDecayAge" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double xpDropDecayAge = 350D;

    @Config.Comment( "What is the minimum amount xp grows a set amount of time? (Default 0.2, increase to speed up growth)" )
    @Config.Name( "minXpGrow" )
    @Config.RangeDouble( min = 0, max = 1 )
    public static double minXpGrow = 1D;

    @Config.Comment( "Should xp drops sync up with the bar being open or closed? HIGHLY RECOMMEND TO KEEP FALSE IF YOU ARE MOVING XP DROP POSITIONS" )
    @Config.Name( "xpDropsAttachedToBar" )
    public static boolean xpDropsAttachedToBar = true;

    @Config.Comment( "If Off, The skills list at the top left corner will no longer appear (You still have the GUI to show you all of your skills info)" )
    @Config.Name( "showSkillsListAtCorner" )
    public static boolean showSkillsListAtCorner = true;

    @Config.Comment( "If Off, xp drops will no longer appear" )
    @Config.Name( "showXpDrops" )
    public static boolean showXpDrops = true;

    @Config.Comment( "If Off, xp drops will no longer stack with each other" )
    @Config.Name( "stackXpDrops" )
    public static boolean stackXpDrops = true;

    @Config.Comment( "Should the Xp Bar always be on? false = only appears while holding Show GUI or when you gain xp" )
    @Config.Name( "xpBarAlwaysOn" )
    public static boolean xpBarAlwaysOn = false;

    @Config.Comment( "Should the Xp left indicator always be on? false = only appears with Show GUI key" )
    @Config.Name( "xpLeftDisplayAlwaysOn" )
    public static boolean xpLeftDisplayAlwaysOn = false;

    @Config.Comment( "Should a screenshot be taken everytime you level up?" )
    @Config.Name( "lvlUpScreenshot" )
    public static boolean lvlUpScreenshot = false;

    @Config.Comment( "When a screenshot is taken upon levelling up, should the skills list turn on automatically to be included in the screenshot?" )
    @Config.Name( "lvlUpScreenshotShowSkills" )
    public static boolean lvlUpScreenshotShowSkills = false;

    @Config.Comment( "Should Xp Drops make the Xp Bar pop up?" )
    @Config.Name( "xpDropsShowXpBar" )
    public static boolean xpDropsShowXpBar = true;

    //Breaking Speed
    @Config.Comment( "Minimum Breaking Speed (1 is Original speed, 0.5 is half)" )
    @Config.Name( "minBreakSpeed" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double minBreakSpeed = 0.5D;

    @Config.Comment( "How many blocks it takes to reach 0 Break Speed (will get capped by Minimum Breaking Speed)" )
    @Config.Name( "blocksToUnbreakableY" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double blocksToUnbreakableY = 1000D;

    @Config.Comment( "How much your mining speed increases per level (1 = 1% increase per level)" )
    @Config.Name( "miningBonusSpeed" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double miningBonusSpeed = 1D;

    @Config.Comment( "How much your cutting speed increases per level in (1 = 1% increase per level)" )
    @Config.Name( "woodcuttingBonusSpeed" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double woodcuttingBonusSpeed = 1D;

    @Config.Comment( "How much your digging speed increases per level in (1 = 1% increase per level)" )
    @Config.Name( "excavationBonusSpeed" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double excavationBonusSpeed = 1D;

    @Config.Comment( "How much your farming speed increases per level in (1 = 1% increase per level)" )
    @Config.Name( "farmingBonusSpeed" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double farmingBonusSpeed = 1D;

    //Mining
    @Config.Comment( "Hardest considered block (1 hardness = 1 remove xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
    @Config.Name( "blockHardnessLimitForBreaking" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double blockHardnessLimitForBreaking = 20D;

    //Building
    @Config.Comment( "Every how many levels you gain an extra block of reach" )
    @Config.Name( "levelsPerOneReach" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double levelsPerOneReach = 25D;

    @Config.Comment( "What is the maximum reach a player can have" )
    @Config.Name( "maxExtraReachBoost" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double maxExtraReachBoost = 20D;

    @Config.Comment( "Hardest considered block (1 hardness = 1 build xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
    @Config.Name( "blockHardnessLimitForPlacing" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double blockHardnessLimitForPlacing = 20D;

    @Config.Comment( "Should xp values for crafting be enabled? false means the hardness value is used" )
    @Config.Name( "xpValuePlacingEnabled" )
    public static boolean xpValuePlacingEnabled = true;

    //Excavation
    @Config.Comment( "Do players find Treasure inside of blocks?" )
    @Config.Name( "treasureEnabled" )
    public static boolean treasureEnabled = true;

    //Woodcutting
//Farming
    @Config.Comment( "Do players get xp for breeding animals?" )
    @Config.Name( "breedingXpEnabled" )
    public static boolean breedingXpEnabled = true;

    @Config.Comment( "How much xp should be awarded in Farming for breeding two animals? (Json Overrides this) (Set to 0 to disable default xp)" )
    @Config.Name( "defaultBreedingXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double defaultBreedingXp = 10.0D;

    @Config.Comment( "How much xp should be awarded in Farming for growing a sapling? (Json Overrides this) (Set to 0 to disable default xp)" )
    @Config.Name( "defaultSaplingGrowXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double defaultSaplingGrowXp = 25.0D;

    @Config.Comment( "How much xp should be awarded in Farming for growing crops? (Json Overrides this) (Set to 0 to disable default xp)" )
    @Config.Name( "defaultCropGrowXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double defaultCropGrowXp = 15.0D;

    //Agility
    @Config.Comment( "Maximum chance to save each point of fall damage (100 = no fall damage)" )
    @Config.Name( "maxFallSaveChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double maxFallSaveChance = 64D;

    @Config.Comment( "How much your chance to save each point of fall damage increases per level (1 = 1% increase per Level)" )
    @Config.Name( "saveChancePerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double saveChancePerLevel = 64D;

    @Config.Comment( "How much jump boost can you gain max (above 0.33 makes you take fall damage)" )
    @Config.Name( "maxJumpBoost" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double maxJumpBoost = 0.33D;

    @Config.Comment( "Every how many levels you gain an extra block of jumping height while Crouching" )
    @Config.Name( "levelsPerCrouchJumpBoost" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double levelsPerCrouchJumpBoost = 33D;

    @Config.Comment( "Every how many levels you gain an extra block of jumping height while Sprinting" )
    @Config.Name( "levelsPerSprintJumpBoost" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double levelsPerSprintJumpBoost = 50D;

    @Config.Comment( "How much speed boost you can get from Agility (100 = 100% vanilla + 100% = twice as fast max)" )
    @Config.Name( "maxSpeedBoost" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double maxSpeedBoost = 100D;

    @Config.Comment( "How much speed boost you get from each level (Incredibly sensitive, default 0.0005)" )
    @Config.Name( "speedBoostPerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double speedBoostPerLevel = 0.00025D;

    //Endurance
    @Config.Comment( "How much endurance is max (100 = god mode)" )
    @Config.Name( "maxEndurance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double maxEndurance = 50D;

    @Config.Comment( "How much endurance you gain per level (1 = 1% per level)" )
    @Config.Name( "endurancePerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double endurancePerLevel = 0.25D;

    @Config.Comment( "Per how many levels you gain 1 Max Heart" )
    @Config.Name( "levelsPerHeart" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double levelsPerHeart = 10D;

    @Config.Comment( "How many Max Hearts you can have (20 means 10 vanilla + 20 boosted)" )
    @Config.Name( "maxExtraHeartBoost" )
    @Config.RangeInt( min = 0, max = 100 )
    public static int maxExtraHeartBoost = 100;

    //Combat
    @Config.Comment( "Per how many levels you gain 1 Extra Damage" )
    @Config.Name( "levelsPerDamage" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double levelsPerDamage = 20D;

    @Config.Comment( "How much extra damage can you get from the Combat skill max?" )
    @Config.Name( "maxExtraDamageBoost" )
    @Config.RangeDouble( min = 0, max = 1000000000 )
    public static double maxExtraDamageBoost = 100D;

    //Archery
//Smithing
    @Config.Comment( "Should PMMO anvil handling be enabled? (xp rewards for repair, and also Enchantment handling) (some mod items break, if you experience lost enchantments, set this to false)" )
    @Config.Name( "anvilHandlingEnabled" )
    public static boolean anvilHandlingEnabled = true;

    @Config.Comment( "Max Percentage chance to return each Enchantment Level" )
    @Config.Name( "maxSalvageEnchantChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double maxSalvageEnchantChance = 90D;

    @Config.Comment( "Each Enchantment Save Chance per Level" )
    @Config.Name( "enchantSaveChancePerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double enchantSaveChancePerLevel = 0.9D;

    @Config.Comment( "Vanilla starts at 50, hence: (50 - [this] * level)" )
    @Config.Name( "anvilCostReductionPerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double anvilCostReductionPerLevel = 0.25D;

    @Config.Comment( "Chance to not break anvil, 100 = twice the value, half the chance per Level." )
    @Config.Name( "extraChanceToNotBreakAnvilPerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double extraChanceToNotBreakAnvilPerLevel = 1D;

    @Config.Comment( "Bonus repair durability per level (100 = twice as much repair per level)" )
    @Config.Name( "anvilFinalItemBonusRepaired" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double anvilFinalItemBonusRepaired = 1D;

    @Config.Comment( "Vanilla caps at 50, at around 30 vanilla you can no longer anvil the item again. allows unlocking infinite Anvil uses." )
    @Config.Name( "anvilFinalItemMaxCostToAnvil" )
    @Config.RangeInt( min = 0, max = 100 )
    public static int anvilFinalItemMaxCostToAnvil = 10;

    @Config.Comment( "Anvil combination limits enchantments to max level set in this config" )
    @Config.Name( "bypassEnchantLimit" )
    public static boolean bypassEnchantLimit = true;

    @Config.Comment( "How many levels per each Enchantment Level Bypass above max level enchantment can support in vanilla" )
    @Config.Name( "levelsPerOneEnchantBypass" )
    @Config.RangeInt( min = 0, max = 100 )
    public static int levelsPerOneEnchantBypass = 50;

    @Config.Comment( "Max amount of levels enchants are able to go above max vanilla level" )
    @Config.Name( "maxEnchantmentBypass" )
    @Config.RangeInt( min = 0, max = 100 )
    public static int maxEnchantmentBypass = 10;

    @Config.Comment( "Anvil combination limits enchantments to this level" )
    @Config.Name( "maxEnchantLevel" )
    @Config.RangeInt( min = 0, max = 100 )
    public static int maxEnchantLevel = 255;

    @Config.Comment( "What is the chance to Bypass a max enchant level (provided you got the skill to do so)" )
    @Config.Name( "upgradeChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double upgradeChance = 50D;

    @Config.Comment( "What is the chance to Reduce a level after a Upgrade chance fails (100 = everytime you fail bypass, enchant level goes down by 1)" )
    @Config.Name( "failedUpgradeKeepLevelChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double failedUpgradeKeepLevelChance = 50D;

    @Config.Comment( "false = Upgrade Chance if only rolled if you are trying to upgrade your item ABOVE vanilla max level. true = you ALWAYS have an upgrade chance level." )
    @Config.Name( "alwaysUseUpgradeChance" )
    public static boolean alwaysUseUpgradeChance = false;

    @Config.Comment( "Do players get xp for Smelting items in a Furnace?" )
    @Config.Name( "smeltingXpEnabled" )
    public static boolean smeltingXpEnabled = true;

    @Config.Comment( "Do Furnaces produce extra items according to Item Owner Smithing level?" )
    @Config.Name( "smeltingEnabled" )
    public static boolean smeltingEnabled = true;

    //Cooking
    @Config.Comment( "Do players get xp for Cooking items in Furnaces/Smokers/Fireplaces?" )
    @Config.Name( "cookingXpEnabled" )
    public static boolean cookingXpEnabled = true;

    @Config.Comment( "Do Furnaces/Smokers/Fireplaces produce extra items according to Item Owner Cooking level?" )
    @Config.Name( "cookingEnabled" )
    public static boolean cookingEnabled = true;

    //Alchemy
    @Config.Comment( "Do players get xp for Brewing potions in Brewing Stands?" )
    @Config.Name( "brewingXpEnabled" )
    public static boolean brewingXpEnabled = true;

    @Config.Comment( "Does Brewing provide a chance to produce Extra potions?" )
    @Config.Name( "brewingEnabled" )
    public static boolean brewingEnabled = true;

    //Flying
//Swimming
    @Config.Comment( "Underwater Nightvision Unlock Level" )
    @Config.Name( "nightvisionUnlockLevel" )
    @Config.RangeInt( min = 0, max = 1000000 )
    public static int nightvisionUnlockLevel = 25;

    //Fishing
    @Config.Comment( "What is the chance on each successful fishing attempt to access the fish_pool" )
    @Config.Name( "fishPoolBaseChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double fishPoolBaseChance = 0D;

    @Config.Comment( "What is the increase per level to access the fish_pool" )
    @Config.Name( "fishPoolChancePerLevel" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double fishPoolChancePerLevel = 0.25D;

    @Config.Comment( "What is the max chance to access the fish_pool" )
    @Config.Name( "fishPoolMaxChance" )
    @Config.RangeDouble( min = 0, max = 100 )
    public static double fishPoolMaxChance = 80D;

    //Crafting
    @Config.Comment( "Should xp values for crafting be enabled? false means the default value is used" )
    @Config.Name( "xpValueCraftingEnabled" )
    public static boolean xpValueCraftingEnabled = true;

    @Config.Comment( "How much xp should be awarded in Crafting for each item crafted? (Json Overrides this) (Set to 0 to disable default xp)" )
    @Config.Name( "defaultCraftingXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double defaultCraftingXp = 1D;

    //Slayer
    @Config.Comment( "How much slayer xp is awarded upon killing an aggresive mob by default" )
    @Config.Name( "aggresiveMobSlayerXp" )
    @Config.RangeDouble( min = 0, max = 10000 )
    public static double aggresiveMobSlayerXp = 0D;

    //Hunter
    @Config.Comment( "How much hunter xp is awarded upon killing a passive mob by default" )
    @Config.Name( "passiveMobHunterXp" )
    @Config.RangeDouble( min = 0, max = 10000 )
    public static double passiveMobHunterXp = 0D;

    //Taming
    @Config.Comment( "Do players get xp for taming animals?" )
    @Config.Name( "tamingXpEnabled" )
    public static boolean tamingXpEnabled = true;

    @Config.Comment( "Do players get xp for growing Plants? (Different from Harvest xp)" )
    @Config.Name( "growingXpEnabled" )
    public static boolean growingXpEnabled = true;

    @Config.Comment( "How much xp should be awarded in Taming for Taming an animal? (Json Overrides this) (Set to 0 to disable default xp)" )
    @Config.Name( "defaultTamingXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double defaultTamingXp = 0D;

    //Easter Eggs
    @Config.Comment( "How much xp do you get for impersonating Jesus?" )
    @Config.Name( "jesusXp" )
    @Config.RangeDouble( min = 0, max = 1000000 )
    public static double jesusXp = 0.075D;

    //Auto Values
    @Config.Comment( "Automatically assign values for un-assigned items? (May be inaccurate)" )
    @Config.Name( "autoGenerateValuesEnabled" )
    public static boolean autoGenerateValuesEnabled = true;

    @Config.Comment( "Automatically assign values for Extra Chance? (Works for Ores/Logs/Plants)" )
    @Config.Name( "autoGenerateExtraChanceEnabled" )
    public static boolean autoGenerateExtraChanceEnabled = true;

    @Config.Comment( "Valued used by autoGenerateExtraChanceEnabled, for Ores" )
    @Config.Name( "defaultExtraChanceOre" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double defaultExtraChanceOre = 1D;

    @Config.Comment( "Valued used by autoGenerateExtraChanceEnabled, for Logs" )
    @Config.Name( "defaultExtraChanceLog" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double defaultExtraChanceLog = 2D;

    @Config.Comment( "Valued used by autoGenerateExtraChanceEnabled, for Plants" )
    @Config.Name( "defaultExtraChancePlant" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double defaultExtraChancePlant = 1.5D;

    @Config.Comment( "Automatically assign values for Wear Requirement?" )
    @Config.Name( "autoGenerateWearReqEnabled" )
    public static boolean autoGenerateWearReqEnabled = true;

    @Config.Comment( "Automatically assign values for Weapon Requirement?" )
    @Config.Name( "autoGenerateWeaponReqEnabled" )
    public static boolean autoGenerateWeaponReqEnabled = true;

    @Config.Comment( "Automatically assign values for Tool Requirement?" )
    @Config.Name( "autoGenerateToolReqEnabled" )
    public static boolean autoGenerateToolReqEnabled = true;

    @Config.Comment( "Automatically assign values for Crafting Experience? (Works for Armor/Tools/Weapons)" )
    @Config.Name( "autoGenerateCraftingXpEnabled" )
    public static boolean autoGenerateCraftingXpEnabled = true;

    @Config.Comment( "Multiplier for the Auto Generated Crafting Xp Value, in the Crafting skill" )
    @Config.Name( "autoGeneratedCraftingXpValueMultiplierCrafting" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double autoGeneratedCraftingXpValueMultiplierCrafting = 1D;

    @Config.Comment( "Multiplier for the Auto Generated Crafting Xp Value, in the Smithing skill" )
    @Config.Name( "autoGeneratedCraftingXpValueMultiplierSmithing" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double autoGeneratedCraftingXpValueMultiplierSmithing = 1D;

    @Config.Comment( "How much the Armor value scales the Endurance Requirement for Armor" )
    @Config.Name( "armorReqScale" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double armorReqScale = 4D;

    @Config.Comment( "How much the Armor Toughness value scales the Endurance Requirement for Armor" )
    @Config.Name( "armorToughnessReqScale" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double armorToughnessReqScale = 6D;

    @Config.Comment( "How much the Attack Damage values scales the Combat Requirement for Weapons" )
    @Config.Name( "attackDamageReqScale" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double attackDamageReqScale = 4D;

    @Config.Comment( "How much the Speed of the tool scales the Requirement of Mining to Use the tool" )
    @Config.Name( "toolReqScaleOre" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double toolReqScaleOre = 5D;

    @Config.Comment( "How much the Speed of the tool scales the Requirement of Woodcutting to Use the tool" )
    @Config.Name( "toolReqScaleLog" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double toolReqScaleLog = 5D;

    @Config.Comment( "How much the Speed of the tool scales the Requirement of Excavation to Use the tool" )
    @Config.Name( "toolReqScaleDirt" )
    @Config.RangeDouble( min = 0D, max = 1000000D )
    public static double toolReqScaleDirt = 5D;

    public static void init()
    {
//        forgeConfig = ConfigHelper.register( ModConfig.Type.COMMON, ConfigImplementation::new );
    }

    public static void initServer()
    {
        //Info that will also be sent to Client so it's accessible remotely
        if( FConfig.veiningAllowed )
            localConfig.put( "veiningAllowed", 1D );
        else
            localConfig.put( "veiningAllowed", 0D );

        if( FConfig.useExponentialFormula )
            localConfig.put( "useExponentialFormula", 1D );
        else
            localConfig.put( "useExponentialFormula", 0D );

        if( FConfig.strictReqTool )
            localConfig.put( "strictReqTool", 1D );
        else
            localConfig.put( "strictReqTool", 0D );

        localConfig.put( "maxLevel", (double) maxLevel );
        localConfig.put( "baseXp", (double) baseXp );
        localConfig.put( "xpIncreasePerLevel", (double) xpIncreasePerLevel );
        localConfig.put( "exponentialBaseXp", exponentialBaseXp );
        localConfig.put( "exponentialBase", exponentialBase );
        localConfig.put( "exponentialRate", exponentialRate );
        localConfig.put( "maxXp", XP.xpAtLevel( maxLevel ) );
        localConfig.put( "biomePenaltyMultiplier", biomePenaltyMultiplier );
        localConfig.put( "nightvisionUnlockLevel", (double) nightvisionUnlockLevel );
        localConfig.put( "speedBoostPerLevel", speedBoostPerLevel );
        localConfig.put( "maxSpeedBoost", maxSpeedBoost );
        localConfig.put( "maxJumpBoost", maxJumpBoost );
        localConfig.put( "maxFallSaveChance", maxFallSaveChance );
        localConfig.put( "saveChancePerLevel", saveChancePerLevel );
        localConfig.put( "levelsPerCrouchJumpBoost", levelsPerCrouchJumpBoost );
        localConfig.put( "levelsPerSprintJumpBoost", levelsPerSprintJumpBoost );
        localConfig.put( "levelsPerDamage", levelsPerDamage );
        localConfig.put( "levelsPerOneReach", levelsPerOneReach );
        localConfig.put( "endurancePerLevel", endurancePerLevel );
        localConfig.put( "maxEndurance", maxEndurance );
        localConfig.put( "levelsPerHeart", levelsPerHeart );
        localConfig.put( "maxExtraHeartBoost", (double) maxExtraHeartBoost );
        localConfig.put( "maxExtraReachBoost", maxExtraReachBoost );
        localConfig.put( "maxExtraDamageBoost", maxExtraDamageBoost );

        localConfig.put( "levelsPerHardnessMining", levelsPerHardnessMining );
        localConfig.put( "levelsPerHardnessWoodcutting", levelsPerHardnessWoodcutting );
        localConfig.put( "levelsPerHardnessExcavation", levelsPerHardnessExcavation );
        localConfig.put( "levelsPerHardnessFarming", levelsPerHardnessFarming );
        localConfig.put( "levelsPerHardnessCrafting", levelsPerHardnessCrafting );
        localConfig.put( "minVeinCost", minVeinCost );
        localConfig.put( "minVeinHardness", minVeinHardness );
        localConfig.put( "maxVeinCharge", maxVeinCharge );
        localConfig.put( "veinMaxBlocks", (double) veinMaxBlocks );

        config = localConfig;
    }

    public static double getConfig( String key )
    {
        if( FConfig.config.containsKey( key ) )
            return FConfig.config.get( key );
        else if( FConfig.localConfig.containsKey( key ) )
            return FConfig.localConfig.get( key );
        else
        {
            System.out.println( "UNABLE TO READ PMMO CONFIG \"" + key + "\" PLEASE REPORT (This is normal during boot if JEI is installed)" );
            return -1;
        }
    }

    public static Map<Skill, Double> getXpMap( EntityPlayer player )
    {
        if( player.world.isRemote )
            return XP.getOfflineXpMap( player.getUniqueID() );
        else
            return PmmoSavedData.get().getXpMap( player.getUniqueID() );
    }

    public static Map<String, Double> getConfigMap()
    {
        return config;
    }

    public static void setConfigMap( Map<String, Double> inMap )
    {
        config = inMap;
    }

    public static Map<String, Double> getPreferencesMap( EntityPlayer player )
    {
        if( player.world.isRemote )
            return preferences;
        else
            return PmmoSavedData.get().getPreferencesMap( player.getUniqueID() );
    }

    public static Map<String, Double> getPreferencesMapOffline()
    {
        return preferences;
    }

    public static Map<String, Double> getAbilitiesMap( EntityPlayer player )
    {
        if( player.world.isRemote )
            return abilities;
        else
            return PmmoSavedData.get().getAbilitiesMap( player.getUniqueID() );
    }

    public static void setPreferencesMap( Map<String, Double> newPreferencesMap )
    {
        preferences = newPreferencesMap;
    }

    public static Map<String, Map<Skill, Double>> getXpBoostsMap( EntityPlayer player )
    {
        if( player.world.isRemote )
            return xpBoosts;
        else
            return PmmoSavedData.get().getPlayerXpBoostsMap( player.getUniqueID() );
    }

    public static Map<Skill, Double> getXpBoostMap( EntityPlayer player, UUID xpBoostUUID )
    {
        if( player.world.isRemote )
            return xpBoosts.getOrDefault( xpBoostUUID, new HashMap<>() );
        else
            return PmmoSavedData.get().getPlayerXpBoostMap( player.getUniqueID(), xpBoostUUID );
    }

    public static double getPlayerXpBoost( EntityPlayer player, Skill skill )
    {
        double xpBoost = 0;

        for( Map.Entry<String, Map<Skill, Double>> entry : getXpBoostsMap( player ).entrySet() )
        {
            xpBoost += entry.getValue().getOrDefault( skill, 0D );
        }

        return xpBoost;
    }

    public static void setPlayerXpBoost(EntityPlayerMP player, String xpBoostKey, Map<Skill, Double> newXpBoosts )
    {
        PmmoSavedData.get().setPlayerXpBoost( player.getUniqueID(), xpBoostKey, newXpBoosts );
    }

    public void removePlayerXpBoost( EntityPlayerMP player, String xpBoostKey )
    {
        PmmoSavedData.get().removePlayerXpBoost( player.getUniqueID(), xpBoostKey );
    }

    public void removeAllPlayerXpBoosts( EntityPlayerMP player )  //WARNING: Removes ALL Xp Boosts, INCLUDING ONES CAUSED BY OTHER MODS
    {
        PmmoSavedData.get().removeAllPlayerXpBoosts( player.getUniqueID() );
    }

    public static void setPlayerXpBoostsMaps( EntityPlayer player, Map<String, Map<Skill, Double>> newBoosts ) //WARNING: Overwrites ALL Xp Boosts, INCLUDING ONES CAUSED BY OTHER MODS
    {   //SERVER ONLY, THE ONLY TIME CLIENT IS CALLED WHEN A PACKET IS RECEIVED >FROM SERVER<
        if( player.world.isRemote )
            xpBoosts = newBoosts;
        else
            PmmoSavedData.get().setPlayerXpBoostsMaps( player.getUniqueID(), newBoosts );
    }
}