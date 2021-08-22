package harmonised.pmmo.config;

import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Config
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static Map<String, Double> localConfig = new HashMap<>();
    private static Map<String, Double> config = new HashMap<>();

    //Client only, too lazy to put it somewhere better
    private static final Map<String, Double> abilities = new HashMap<>();
    private static Map<String, Double> preferences = new HashMap<>();
    public static Map<String, Map<String, Double>> xpBoosts = new HashMap<>();

    public static ConfigImplementation forgeConfig;

    public static void init()
    {
        forgeConfig = ConfigHelper.register( ModConfig.Type.COMMON, ConfigImplementation::new );
    }

    public static void initServer()
    {
        //Info that will also be sent to Client so it's accessible remotely
        localConfig.put( "veiningAllowed", forgeConfig.veiningAllowed.get() ? 1D : 0D );
        localConfig.put( "useExponentialFormula", forgeConfig.useExponentialFormula.get() ? 1D : 0D );
        localConfig.put( "strictReqTool", forgeConfig.strictReqTool.get() ? 1D : 0D );
        localConfig.put( "autoGenerateValuesEnabled", forgeConfig.autoGenerateValuesEnabled.get() ? 1D : 0D );
        localConfig.put( "autoGenerateWearReqDynamicallyEnabled", forgeConfig.autoGenerateWearReqDynamicallyEnabled.get() ? 1D : 0D );
        localConfig.put( "autoGenerateWearReqAsCombat", forgeConfig.autoGenerateWearReqAsCombat.get() ? 1D : 0D );
        localConfig.put( "autoGenerateWeaponReqDynamicallyEnabled", forgeConfig.autoGenerateWeaponReqDynamicallyEnabled.get() ? 1D : 0D );
        localConfig.put( "autoGenerateToolReqDynamicallyEnabled", forgeConfig.autoGenerateToolReqDynamicallyEnabled.get() ? 1D : 0D );

        localConfig.put( "autoGenerateWearReqOffset", forgeConfig.autoGenerateWearReqOffset.get() );
        localConfig.put( "autoGenerateWeaponReqOffset", forgeConfig.autoGenerateWeaponReqOffset.get() );
        localConfig.put( "autoGenerateToolReqOffset", forgeConfig.autoGenerateToolReqOffset.get() );

        localConfig.put( "wearReqEnabled", forgeConfig.wearReqEnabled.get() ? 1D : 0D );
        localConfig.put( "toolReqEnabled", forgeConfig.toolReqEnabled.get() ? 1D : 0D );
        localConfig.put( "weaponReqEnabled", forgeConfig.weaponReqEnabled.get() ? 1D : 0D );
        localConfig.put( "enchantUseReqEnabled", forgeConfig.enchantUseReqEnabled.get() ? 1D : 0D );

        localConfig.put( "scaleXpBoostByDurability", forgeConfig.scaleXpBoostByDurability.get() ? 1D : 0D );
        localConfig.put( "scaleXpBoostByDurabilityStart", forgeConfig.scaleXpBoostByDurabilityStart.get() );
        localConfig.put( "scaleXpBoostByDurabilityEnd", forgeConfig.scaleXpBoostByDurabilityEnd.get() );

        localConfig.put( "maxLevel", (double) forgeConfig.maxLevel.get() );
        localConfig.put( "baseXp", forgeConfig.baseXp.get() );
        localConfig.put( "xpIncreasePerLevel", forgeConfig.xpIncreasePerLevel.get() );
        localConfig.put( "exponentialBaseXp", forgeConfig.exponentialBaseXp.get() );
        localConfig.put( "exponentialBase", forgeConfig.exponentialBase.get() );
        localConfig.put( "exponentialRate", forgeConfig.exponentialRate.get() );
        localConfig.put( "maxXp", Math.min( Double.MAX_VALUE, XP.xpAtLevel( forgeConfig.maxLevel.get() ) ) );
        localConfig.put( "biomePenaltyMultiplier", forgeConfig.biomePenaltyMultiplier.get() );
        localConfig.put( "nightvisionUnlockLevel", (double) forgeConfig.nightvisionUnlockLevel.get() );
        localConfig.put( "speedBoostPerLevel", forgeConfig.speedBoostPerLevel.get() );
        localConfig.put( "maxSpeedBoost", forgeConfig.maxSpeedBoost.get() );
        localConfig.put( "maxJumpBoost", forgeConfig.maxJumpBoost.get() );
        localConfig.put( "maxFallSaveChance", forgeConfig.maxFallSaveChance.get() );
        localConfig.put( "saveChancePerLevel", forgeConfig.saveChancePerLevel.get() );
        localConfig.put( "levelsPerCrouchJumpBoost", forgeConfig.levelsPerCrouchJumpBoost.get() );
        localConfig.put( "levelsPerSprintJumpBoost", forgeConfig.levelsPerSprintJumpBoost.get() );
        localConfig.put( "levelsPerOneReach", forgeConfig.levelsPerOneReach.get() );
        localConfig.put( "endurancePerLevel", forgeConfig.endurancePerLevel.get() );
        localConfig.put( "hpRegenPerMinuteBase", forgeConfig.hpRegenPerMinuteBase.get() );
        localConfig.put( "hpRegenPerMinuteBoostPerLevel", forgeConfig.hpRegenPerMinuteBoostPerLevel.get() );
        localConfig.put( "maxEndurance", forgeConfig.maxEndurance.get() );
        localConfig.put( "levelsPerHeart", forgeConfig.levelsPerHeart.get() );
        localConfig.put( "maxExtraHeartBoost", (double) forgeConfig.maxExtraHeartBoost.get() );
        localConfig.put( "maxExtraReachBoost", forgeConfig.maxExtraReachBoost.get() );

        localConfig.put( "damageBonusPercentPerLevelMelee", forgeConfig.damageBonusPercentPerLevelMelee.get() );
        localConfig.put( "maxExtraDamagePercentageBoostMelee", forgeConfig.maxExtraDamagePercentageBoostMelee.get() );
        localConfig.put( "damageBonusPercentPerLevelArchery", forgeConfig.damageBonusPercentPerLevelArchery.get() );
        localConfig.put( "maxExtraDamagePercentageBoostArchery", forgeConfig.maxExtraDamagePercentageBoostArchery.get() );
        localConfig.put( "damageBonusPercentPerLevelMagic", forgeConfig.damageBonusPercentPerLevelMagic.get() );
        localConfig.put( "damageBonusPercentPerLevelGunslinging", forgeConfig.damageBonusPercentPerLevelGunslinging.get() );
        localConfig.put( "maxExtraDamagePercentageBoostMagic", forgeConfig.maxExtraDamagePercentageBoostMagic.get() );

        localConfig.put( "mobHPBoostPerPowerLevel", forgeConfig.mobHPBoostPerPowerLevel.get() );
        localConfig.put( "maxMobHPBoost", forgeConfig.maxMobHPBoost.get() );
        localConfig.put( "mobSpeedBoostPerPowerLevel", forgeConfig.mobSpeedBoostPerPowerLevel.get() );
        localConfig.put( "maxMobSpeedBoost", forgeConfig.maxMobSpeedBoost.get() );
        localConfig.put( "mobDamageBoostPerPowerLevel", forgeConfig.mobDamageBoostPerPowerLevel.get() );
        localConfig.put( "maxMobDamageBoost", forgeConfig.maxMobDamageBoost.get() );

        localConfig.put( "levelsPerHardnessMining", forgeConfig.levelsPerHardnessMining.get() );
        localConfig.put( "levelsPerHardnessWoodcutting", forgeConfig.levelsPerHardnessWoodcutting.get() );
        localConfig.put( "levelsPerHardnessExcavation", forgeConfig.levelsPerHardnessExcavation.get() );
        localConfig.put( "levelsPerHardnessFarming", forgeConfig.levelsPerHardnessFarming.get() );
        localConfig.put( "levelsPerHardnessCrafting", forgeConfig.levelsPerHardnessCrafting.get() );
        localConfig.put( "minVeinCost", forgeConfig.minVeinCost.get() );
        localConfig.put( "minVeinHardness", forgeConfig.minVeinHardness.get() );
        localConfig.put( "maxVeinCharge", forgeConfig.maxVeinCharge.get() );
        localConfig.put( "veinMaxBlocks", (double) forgeConfig.veinMaxBlocks.get() );
        localConfig.put( "toolSpeedVeinScale", forgeConfig.toolSpeedVeinScale.get() );

        localConfig.put( "dualSalvageSmithingLevelReq", (double) forgeConfig.dualSalvageSmithingLevelReq.get() );

        localConfig.put( "partyRange", forgeConfig.partyRange.get() );

        localConfig.put( "fishPoolBaseChance", forgeConfig.fishPoolBaseChance.get() );
        localConfig.put( "fishPoolChancePerLevel", forgeConfig.fishPoolChancePerLevel.get() );
        localConfig.put( "fishPoolMaxChance", forgeConfig.fishPoolMaxChance.get() );

        config = localConfig;
    }

    public static class ConfigImplementation
    {
        //Miscellaneous
        public ConfigHelper.ConfigValueListener<Boolean> showWelcome;
        public ConfigHelper.ConfigValueListener<Boolean> showPatreonWelcome;
        public ConfigHelper.ConfigValueListener<Boolean> warnOutdatedVersion;
        public ConfigHelper.ConfigValueListener<Boolean> scaleXpBoostByDurability;
        public ConfigHelper.ConfigValueListener<Boolean> logXpGainedInDebugLog;
        public ConfigHelper.ConfigValueListener<Double> scaleXpBoostByDurabilityStart;
        public ConfigHelper.ConfigValueListener<Double> scaleXpBoostByDurabilityEnd;
        public ConfigHelper.ConfigValueListener<Boolean> rightClickXpEnabled;

        //Party
        public ConfigHelper.ConfigValueListener<Double> partyRange;
        public ConfigHelper.ConfigValueListener<Integer> partyMaxMembers;
        public ConfigHelper.ConfigValueListener<Double> partyXpIncreasePerPlayer;
        public ConfigHelper.ConfigValueListener<Double> maxPartyXpBonus;
        public ConfigHelper.ConfigValueListener<Double> partyFriendlyFireAmount;
        public ConfigHelper.ConfigValueListener<Boolean> autoLeavePartyOnDisconnect;

        //Anti Cheese
        public ConfigHelper.ConfigValueListener<Boolean> antiCheeseEnabled;
        public ConfigHelper.ConfigValueListener<Integer> cheeseMaxStorage;
        public ConfigHelper.ConfigValueListener<Integer> freeCheese;
        public ConfigHelper.ConfigValueListener<Integer> activityCheeseReplenishSpeed;
        public ConfigHelper.ConfigValueListener<Double> cheeseCheckFrequency;
        public ConfigHelper.ConfigValueListener<Double> minimumCheeseXpMultiplier;
        public ConfigHelper.ConfigValueListener<Double> sendPlayerCheeseWarningBelowMultiplier;

        //Vein Mining
        public ConfigHelper.ConfigValueListener<Boolean> veiningAllowed;
        public ConfigHelper.ConfigValueListener<Boolean> veinWoodTopToBottom;
        public ConfigHelper.ConfigValueListener<Boolean> veiningOtherPlayerBlocksAllowed;
        public ConfigHelper.ConfigValueListener<Boolean> damageToolWhileVeining;
        public ConfigHelper.ConfigValueListener<Integer> veinMaxBlocks;
        public ConfigHelper.ConfigValueListener<Integer> veinSpeed;
        public ConfigHelper.ConfigValueListener<Integer> maxVeinDisplay;
        public ConfigHelper.ConfigValueListener<Double> veinMaxDistance;
        public ConfigHelper.ConfigValueListener<Double> minVeinCost;
        public ConfigHelper.ConfigValueListener<Double> minVeinHardness;
        public ConfigHelper.ConfigValueListener<Double> maxVeinCharge;
        public ConfigHelper.ConfigValueListener<Double> sleepVeinRestorePercent;
        public ConfigHelper.ConfigValueListener<Double> exhaustionPerBlock;
        public ConfigHelper.ConfigValueListener<Double> toolSpeedVeinScale;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHardnessMining;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHardnessWoodcutting;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHardnessExcavation;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHardnessFarming;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHardnessCrafting;

        //Mob Scaling
        public ConfigHelper.ConfigValueListener<Double> maxMobSpeedBoost;
        public ConfigHelper.ConfigValueListener<Double> mobSpeedBoostPerPowerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxMobHPBoost;
        public ConfigHelper.ConfigValueListener<Double> mobHPBoostPerPowerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxMobDamageBoost;
        public ConfigHelper.ConfigValueListener<Double> mobDamageBoostPerPowerLevel;
        public ConfigHelper.ConfigValueListener<Boolean> biomeMobMultiplierEnabled;

        //Requirements
        public ConfigHelper.ConfigValueListener<Boolean> wearReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> enchantUseReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> enchantUseReqAutoScaleEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> toolReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> weaponReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> killReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> killXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> mobRareDropEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> useReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> placeReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> breakReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> biomeReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> dimensionTravelReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> craftReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> negativeBiomeEffectEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> positiveBiomeEffectEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> biomeXpBonusEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> xpValueBreakingEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> xpValueGeneralEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> oreEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> logEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> plantEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> salvageEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> fishPoolEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> fishEnchantPoolEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> levelUpCommandEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> heldItemXpBoostEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> wornItemXpBoostEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> loadDefaultConfig;

        public ConfigHelper.ConfigValueListener<Boolean> strictReqTool;
        public ConfigHelper.ConfigValueListener<Boolean> strictReqKill;
        public ConfigHelper.ConfigValueListener<Boolean> strictReqWeapon;
        public ConfigHelper.ConfigValueListener<Boolean> strictReqWear;
        public ConfigHelper.ConfigValueListener<Boolean> strictReqUseEnchantment;

        //Levels
        public ConfigHelper.ConfigValueListener<Integer> maxLevel;
        public ConfigHelper.ConfigValueListener<Double> baseXp;
        public ConfigHelper.ConfigValueListener<Double> xpIncreasePerLevel;
        public ConfigHelper.ConfigValueListener<Integer> levelsPerMilestone;
        public ConfigHelper.ConfigValueListener<Integer> levelsPerTotalLevelMilestone;
        public ConfigHelper.ConfigValueListener<Boolean> wipeAllSkillsUponDeathPermanently;
        public ConfigHelper.ConfigValueListener<Boolean> broadcastMilestone;
        public ConfigHelper.ConfigValueListener<Boolean> levelUpFirework;
        public ConfigHelper.ConfigValueListener<Boolean> milestoneLevelUpFirework;
        public ConfigHelper.ConfigValueListener<Boolean> spawnFireworksCausedByMe;
        public ConfigHelper.ConfigValueListener<Boolean> spawnFireworksCausedByOthers;
        public ConfigHelper.ConfigValueListener<Boolean> underwaterNightVision;
        public ConfigHelper.ConfigValueListener<Boolean> deathLoosesLevels;

        public ConfigHelper.ConfigValueListener<Boolean> useExponentialFormula;
        public ConfigHelper.ConfigValueListener<Double> exponentialBaseXp;
        public ConfigHelper.ConfigValueListener<Double> exponentialBase;
        public ConfigHelper.ConfigValueListener<Double> exponentialRate;

        //Multipliers
        public ConfigHelper.ConfigValueListener<Double> globalMultiplier;
        public ConfigHelper.ConfigValueListener<Double> peacefulMultiplier;
        public ConfigHelper.ConfigValueListener<Double> easyMultiplier;
        public ConfigHelper.ConfigValueListener<Double> normalMultiplier;
        public ConfigHelper.ConfigValueListener<Double> hardMultiplier;
        public ConfigHelper.ConfigValueListener<Double> biomePenaltyMultiplier;
        public ConfigHelper.ConfigValueListener<Double> deathPenaltyMultiplier;

        //GUI
        public ConfigHelper.ConfigValueListener<Boolean> xpBarTheme;
        public ConfigHelper.ConfigValueListener<Double> barOffsetX;
        public ConfigHelper.ConfigValueListener<Double> barOffsetY;
        public ConfigHelper.ConfigValueListener<Double> veinBarOffsetX;
        public ConfigHelper.ConfigValueListener<Double> veinBarOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetX;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetY;
        public ConfigHelper.ConfigValueListener<Double> skillListOffsetX;
        public ConfigHelper.ConfigValueListener<Double> skillListOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropSpawnDistance;
        public ConfigHelper.ConfigValueListener<Double> xpDropOpacityPerTime;
        public ConfigHelper.ConfigValueListener<Double> xpDropMaxOpacity;
        public ConfigHelper.ConfigValueListener<Double> xpDropDecayAge;
        public ConfigHelper.ConfigValueListener<Double> minXpGrow;
        public ConfigHelper.ConfigValueListener<Boolean> showSkillsListAtCorner;
        public ConfigHelper.ConfigValueListener<Boolean> showXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> stackXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> xpDropsAttachedToBar;
        public ConfigHelper.ConfigValueListener<Boolean> xpBarAlwaysOn;
        public ConfigHelper.ConfigValueListener<Boolean> xpLeftDisplayAlwaysOn;
        public ConfigHelper.ConfigValueListener<Boolean> lvlUpScreenshot;
        public ConfigHelper.ConfigValueListener<Boolean> lvlUpScreenshotShowSkills;
        public ConfigHelper.ConfigValueListener<Boolean> xpDropsShowXpBar;
        public ConfigHelper.ConfigValueListener<Boolean> showLevelUpUnlocks;

        public ConfigHelper.ConfigValueListener<Boolean> worldXpDropsEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> worldXpDropsShowSkill;
        public ConfigHelper.ConfigValueListener<Boolean> showOthersWorldXpDrops;
        public ConfigHelper.ConfigValueListener<Double> worldXpDropsSizeMultiplier;
        public ConfigHelper.ConfigValueListener<Double> worldXpDropsDecaySpeedMultiplier;
        public ConfigHelper.ConfigValueListener<Double> worldXpDropsRotationCap;

        //Breaking Speed
        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> blocksToUnbreakableY;
        public ConfigHelper.ConfigValueListener<Double> miningBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> woodcuttingBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> excavationBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> farmingBonusSpeed;

        //Mining
        public ConfigHelper.ConfigValueListener<Double> blockHardnessLimitForBreaking;

        //Building
        public ConfigHelper.ConfigValueListener<Double> levelsPerOneReach;
        public ConfigHelper.ConfigValueListener<Double> maxExtraReachBoost;
        public ConfigHelper.ConfigValueListener<Double> blockHardnessLimitForPlacing;
        public ConfigHelper.ConfigValueListener<Boolean> xpValuePlacingEnabled;

        //Excavation
        public ConfigHelper.ConfigValueListener<Boolean> treasureEnabled;

        //Woodcutting

        //Farming
        public ConfigHelper.ConfigValueListener<Boolean> tamingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> growingXpEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultBreedingXp;
        public ConfigHelper.ConfigValueListener<Double> defaultCropGrowXp;
        public ConfigHelper.ConfigValueListener<Double> defaultSaplingGrowXp;

        //Agility
        public ConfigHelper.ConfigValueListener<Double> maxFallSaveChance;
        public ConfigHelper.ConfigValueListener<Double> saveChancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxJumpBoost;
        public ConfigHelper.ConfigValueListener<Double> maxSpeedBoost;
        public ConfigHelper.ConfigValueListener<Double> speedBoostPerLevel;
        public ConfigHelper.ConfigValueListener<Double> levelsPerCrouchJumpBoost;
        public ConfigHelper.ConfigValueListener<Double> levelsPerSprintJumpBoost;

        //Endurance
        public ConfigHelper.ConfigValueListener<Double> endurancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxEndurance;
        public ConfigHelper.ConfigValueListener<Double> levelsPerHeart;
        public ConfigHelper.ConfigValueListener<Integer> maxExtraHeartBoost;
        public ConfigHelper.ConfigValueListener<Double> hpRegenPerMinuteBase;
        public ConfigHelper.ConfigValueListener<Double> hpRegenPerMinuteBoostPerLevel;
        public ConfigHelper.ConfigValueListener<Double> hpRegenXpMultiplier;

        //Combat
        public ConfigHelper.ConfigValueListener<Double> damageBonusPercentPerLevelMelee;
        public ConfigHelper.ConfigValueListener<Double> maxExtraDamagePercentageBoostMelee;
        public ConfigHelper.ConfigValueListener<Double> maxExtraDamagePercentageBoostArchery;
        public ConfigHelper.ConfigValueListener<Double> maxExtraDamagePercentageBoostMagic;

        //Archery
        public ConfigHelper.ConfigValueListener<Double> damageBonusPercentPerLevelArchery;

        //Smithing
        public ConfigHelper.ConfigValueListener<Boolean> anvilHandlingEnabled;

        public ConfigHelper.ConfigValueListener<Double> maxSalvageEnchantChance;
        public ConfigHelper.ConfigValueListener<Double> enchantSaveChancePerLevel;
        public ConfigHelper.ConfigValueListener<Boolean> bypassEnchantLimit;
        public ConfigHelper.ConfigValueListener<Integer> levelsPerOneEnchantBypass;
        public ConfigHelper.ConfigValueListener<Integer> maxEnchantmentBypass;
        public ConfigHelper.ConfigValueListener<Integer> maxEnchantLevel;
        public ConfigHelper.ConfigValueListener<Double> upgradeChance;
        public ConfigHelper.ConfigValueListener<Double> failedUpgradeKeepLevelChance;
        public ConfigHelper.ConfigValueListener<Boolean> alwaysUseUpgradeChance;

        public ConfigHelper.ConfigValueListener<Double> anvilCostReductionPerLevel;         //Salvage
        public ConfigHelper.ConfigValueListener<Double> extraChanceToNotBreakAnvilPerLevel;
        public ConfigHelper.ConfigValueListener<Double> anvilFinalItemBonusRepaired;
        public ConfigHelper.ConfigValueListener<Integer> anvilFinalItemMaxCostToAnvil;
        public ConfigHelper.ConfigValueListener<Integer> dualSalvageSmithingLevelReq;

        public ConfigHelper.ConfigValueListener<Boolean> smeltingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> smeltingEnabled;

        //Cooking
        public ConfigHelper.ConfigValueListener<Boolean> cookingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> cookingEnabled;

        //Alchemy
        public ConfigHelper.ConfigValueListener<Boolean> brewingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> brewingEnabled;

        //Swimming
        public ConfigHelper.ConfigValueListener<Integer> nightvisionUnlockLevel;

        //Fishing
        public ConfigHelper.ConfigValueListener<Boolean> disableNormalFishDrops;
        public ConfigHelper.ConfigValueListener<Double> fishPoolBaseChance;
        public ConfigHelper.ConfigValueListener<Double> fishPoolChancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> fishPoolMaxChance;

        //Crafting
        public ConfigHelper.ConfigValueListener<Boolean> xpValueCraftingEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultCraftingXp;

        //Magic
        public ConfigHelper.ConfigValueListener<Double> damageBonusPercentPerLevelMagic;

        //Gunslinging
        public ConfigHelper.ConfigValueListener<Double> damageBonusPercentPerLevelGunslinging;

        //Slayer
        public ConfigHelper.ConfigValueListener<Double> passiveMobHunterXp;
        public ConfigHelper.ConfigValueListener<Double> aggresiveMobSlayerXp;

        //Taming
        public ConfigHelper.ConfigValueListener<Boolean> breedingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> defaultTamingXpFarming;
        public ConfigHelper.ConfigValueListener<Double> defaultTamingXp;

        //Easter Eggs
        public ConfigHelper.ConfigValueListener<Double> jesusXp;

        //Auto Values
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateValuesEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateRoundedValuesOnly;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateExtraChanceEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultExtraChanceOre;
        public ConfigHelper.ConfigValueListener<Double> defaultExtraChanceLog;
        public ConfigHelper.ConfigValueListener<Double> defaultExtraChancePlant;
        //        public ConfigHelper.ConfigValueListener<Double> defaultExtraChanceSmelt;
//        public ConfigHelper.ConfigValueListener<Double> defaultExtraChanceCook;
//        public ConfigHelper.ConfigValueListener<Double> defaultExtraChanceBrew;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateWearReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateWearReqDynamicallyEnabled;
        public ConfigHelper.ConfigValueListener<Double> autoGenerateWearReqOffset;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateWearReqAsCombat;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateWeaponReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateWeaponReqDynamicallyEnabled;
        public ConfigHelper.ConfigValueListener<Double> autoGenerateWeaponReqOffset;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateToolReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateToolReqDynamicallyEnabled;
        public ConfigHelper.ConfigValueListener<Double> autoGenerateToolReqOffset;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateCraftingXpEnabled;
        //        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateKillXpAggresiveEnabled;
        public ConfigHelper.ConfigValueListener<Double> autoGeneratedCraftingXpValueMultiplierCrafting;
        public ConfigHelper.ConfigValueListener<Double> autoGeneratedCraftingXpValueMultiplierSmithing;
        public ConfigHelper.ConfigValueListener<Double> autoGeneratedCraftingXpValueMultiplierCooking;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateCookingXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> autoGenerateCookingExtraChanceEnabled;
        //        public ConfigHelper.ConfigValueListener<Double> autoGeneratedKillXpValueAggresiveMultiplierSlayer;
        public ConfigHelper.ConfigValueListener<Double> armorReqScale;
        public ConfigHelper.ConfigValueListener<Double> armorToughnessReqScale;
        public ConfigHelper.ConfigValueListener<Double> attackDamageReqScale;
        public ConfigHelper.ConfigValueListener<Double> toolReqScaleLog;
        public ConfigHelper.ConfigValueListener<Double> toolReqScaleOre;
        public ConfigHelper.ConfigValueListener<Double> toolReqScaleDirt;
        public ConfigHelper.ConfigValueListener<Boolean> outputAllAutoValuesToLog;

        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push( "Miscellaneous" );
            {
                this.showWelcome = subscriber.subscribe(builder
                        .comment( "Should the Welcome message come up?" )
                        .translation( "pmmo.showWelcome" )
                        .define( "showWelcome", true ) );

                this.showPatreonWelcome = subscriber.subscribe(builder
                        .comment( "Should your personal Donator Welcome message come up?" )
                        .translation( "pmmo.showPatreonWelcome" )
                        .define( "showPatreonWelcome", true ) );

                this.warnOutdatedVersion = subscriber.subscribe(builder
                        .comment( "Should a warning come up if your Project MMO is outdated?" )
                        .translation( "pmmo.warnOutdatedVersion" )
                        .define( "warnOutdatedVersion", true ) );

                this.scaleXpBoostByDurability = subscriber.subscribe(builder
                        .comment( "Should Xp Boosts be scaled by the item Durability? (Max boost at Max durability, 50% at half Durability)" )
                        .translation( "pmmo.scaleXpBoostByDurability" )
                        .define( "scaleXpBoostByDurability", true ) );

                this.logXpGainedInDebugLog = subscriber.subscribe(builder
                        .comment( "Should Xp gains be logged in debug.log?" )
                        .translation( "pmmo.logXpGainedInDebugLog" )
                        .define( "logXpGainedInDebugLog", false ) );

                this.scaleXpBoostByDurabilityStart = subscriber.subscribe(builder
                        .comment( "At what durability percentage should the xp bonus go away fully at and below? (25 means at 25% or below durability, there is no longer any xp boost)" )
                        .translation( "pmmo.scaleXpBoostByDurabilityStart" )
                        .defineInRange( "scaleXpBoostByDurabilityStart", 0D, 0D, 100D ) );

                this.scaleXpBoostByDurabilityEnd = subscriber.subscribe(builder
                        .comment( "At what durability percentage should the xp bonus start going down from? (75 means that at 75% durability or above, the xp bonus will be max, and at 37.5%, the xp boost will be half" )
                        .translation( "pmmo.scaleXpBoostByDurabilityEnd" )
                        .defineInRange( "scaleXpBoostByDurabilityEnd", 75D, 0D, 100D ) );

                this.rightClickXpEnabled = subscriber.subscribe(builder
                        .comment( "Should right click xp be enabled?" )
                        .translation( "pmmo.rightClickXpEnabled" )
                        .define( "rightClickXpEnabled", true ) );

                builder.pop();
            }

            builder.push( "Party" );
            {
                this.partyRange = subscriber.subscribe(builder
                        .comment( "In what range do Party members have to be to benefit from the other members?" )
                        .translation( "pmmo.partyRange" )
                        .defineInRange( "partyRange", 64D, 1, 1000000000 ) );

                this.partyMaxMembers = subscriber.subscribe(builder
                        .comment( "How many Members can a party have?" )
                        .translation( "pmmo.partyMaxMembers" )
                        .defineInRange( "partyMaxMembers", 10, 1, 1000000000 ) );

                this.partyXpIncreasePerPlayer = subscriber.subscribe(builder
                        .comment( "How much bonus xp a Party gains extra, per player? (5 = 1 player -> 5% xp bonus, 2 players -> 10% xp bonus" )
                        .translation( "pmmo.partyXpIncreasePerPlayer" )
                        .defineInRange( "partyXpIncreasePerPlayer", 5D, 0, 1000000000 ) );

                this.maxPartyXpBonus = subscriber.subscribe(builder
                        .comment( "How much bonus xp is the maximum that a Party can receive? (50 = 50% increase max. If partyXpIncreasePerPlayer is 5, and there are 20 members, the xp bonus caps at 50%, at 10 members)" )
                        .translation( "pmmo.maxPartyXpBonus" )
                        .defineInRange( "maxPartyXpBonus", 50D, 0, 1000000000 ) );

                this.partyFriendlyFireAmount = subscriber.subscribe(builder
                        .comment( "How much damage you can deal to people in the same Party (0 = no damage, 100 = full damage)" )
                        .translation( "pmmo.partyFriendlyFireAmount" )
                        .defineInRange( "partyFriendlyFireAmount", 100/3D, 0, 100 ) );

                this.autoLeavePartyOnDisconnect = subscriber.subscribe(builder
                        .comment( "Should players leave their party if they disconnect?" )
                        .translation( "pmmo.autoLeavePartyOnDisconnect" )
                        .define( "autoLeavePartyOnDisconnect", false ) );

                builder.pop();
            }

            builder.push( "Anti Cheese" );
            {
                this.antiCheeseEnabled = subscriber.subscribe(builder
                        .comment( "Should player xp rates be reduced while they afk?" )
                        .translation( "pmmo.antiCheeseEnabled" )
                        .define( "antiCheeseEnabled", true ) );

                this.cheeseMaxStorage = subscriber.subscribe(builder
                        .comment( "How many points of Cheese is the maximum? (At maximum, the xp gain is at minimum, which is the minimumCheeseXpMultiplier value)" )
                        .translation( "pmmo.cheeseMaxStorage" )
                        .defineInRange( "cheeseMaxStorage", 200, 1, 1000000000 ) );

                this.freeCheese = subscriber.subscribe(builder
                        .comment( "How many points of Cheese do not contribute to the Cheese Xp Penalty? (This should always be lower than cheeseMaxStorage, otherwise it'd break.)" )
                        .translation( "pmmo.freeCheese" )
                        .defineInRange( "freeCheese", 100, 1, 1000000000 ) );

                this.activityCheeseReplenishSpeed = subscriber.subscribe(builder
                        .comment( "How many points of Cheese are reduced when the player is no longer afk? (10 means ten times as fast as gaining while afk)" )
                        .translation( "pmmo.activityCheeseReplenishSpeed" )
                        .defineInRange( "activityCheeseReplenishSpeed", 10, 1, 1000000000 ) );

                this.cheeseCheckFrequency = subscriber.subscribe(builder
                        .comment( "How often are Cheese Points Given/Taken? (5 means every 5 seconds)" )
                        .translation( "pmmo.cheeseCheckFrequency" )
                        .defineInRange( "cheeseCheckFrequency", 5D, 0.1, 1000000000 ) );

                this.minimumCheeseXpMultiplier = subscriber.subscribe(builder
                        .comment( "What is the minimum xp multiplier a player can reach by afking? (0.1 means 10% xp gained of what you'd gain while not afking at all)" )
                        .translation( "pmmo.minimumCheeseXpMultiplier" )
                        .defineInRange( "minimumCheeseXpMultiplier", 0.1D, 0, 1 ) );

                this.sendPlayerCheeseWarningBelowMultiplier = subscriber.subscribe(builder
                        .comment( "When the Cheese Multiplier falls below this value, the player will get a warning to stop afking every Cheese Check (If this value is 1, any penalty will be displayed. 0 means no warnings will be displayed)" )
                        .translation( "pmmo.sendPlayerCheeseWarningBelowMultiplier" )
                        .defineInRange( "sendPlayerCheeseWarningBelowMultiplier", 1D, 0, 1 ) );

                builder.pop();
            }

            builder.push( "Vein Mining" );
            {
                this.veiningAllowed = subscriber.subscribe(builder
                        .comment( "Is vein mining allowed? true = on, false = off" )
                        .translation( "pmmo.veiningAllowed" )
                        .define( "veiningAllowed", true ) );

                this.veinWoodTopToBottom = subscriber.subscribe(builder
                        .comment( "Should veining wood material blocks start from the highest block?" )
                        .translation( "pmmo.veinWoodTopToBottom" )
                        .define( "veinWoodTopToBottom", true ) );

                this.veiningOtherPlayerBlocksAllowed = subscriber.subscribe(builder
                        .comment( "Should players be allowed to vein blocks that they did not place?" )
                        .translation( "pmmo.veiningOtherPlayerBlocksAllowed" )
                        .define( "veiningOtherPlayerBlocksAllowed", false ) );

                this.damageToolWhileVeining = subscriber.subscribe(builder
                        .comment( "Should blocks broken by veining damage your tool?" )
                        .translation( "pmmo.damageToolWhileVeining" )
                        .define( "damageToolWhileVeining", true ) );

                this.veinMaxDistance = subscriber.subscribe(builder
                        .comment( "What is the maximum distance a player's vein can reach?" )
                        .translation( "pmmo.veinMaxDistance" )
                        .defineInRange( "veinMaxDistance", 1000D, 1, 1000000000 ) );

                this.veinMaxBlocks = subscriber.subscribe(builder
                        .comment( "How many blocks max can be veined?" )
                        .translation( "pmmo.veinMaxBlocks" )
                        .defineInRange( "veinMaxBlocks", 10000, 1, 1000000 ) );

                this.veinSpeed = subscriber.subscribe(builder
                        .comment( "How many blocks get broken every tick?" )
                        .translation( "pmmo.veinSpeed" )
                        .defineInRange( "veinSpeed", 1, 1, 10000 ) );

                this.maxVeinDisplay = subscriber.subscribe(builder
                        .comment( "How many blocks should be highlighted while holding the Vein key" )
                        .translation( "pmmo.maxVeinDisplay" )
                        .defineInRange( "maxVeinDisplay", 250, 0, 10000 ) );

                this.minVeinCost = subscriber.subscribe(builder
                        .comment( "How much is the lowest cost for each block veined? (1 = 1 charge, 1 charge regens per second)" )
                        .translation( "pmmo.minVeinCost" )
                        .defineInRange( "minVeinCost", 0.5D, 0.01, 10000 ) );

                this.minVeinHardness = subscriber.subscribe(builder
                        .comment( "What is the lowest hardness for each block veined? (Crops have 0 hardness, this makes crops not infinitely veined)" )
                        .translation( "pmmo.minVeinHardness" )
                        .defineInRange( "minVeinHardness", 0.5D, 0, 10000 ) );

                this.levelsPerHardnessMining = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160 ) * 320 = 100 hardness worth of blocks, which is 2.0 Obsidian, or 33.3 Coal Ore)" )
                        .translation( "pmmo.levelsPerHardnessMining" )
                        .defineInRange( "levelsPerHardnessMining", 240D, 0.01, 10000 ) );

                this.levelsPerHardnessWoodcutting = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160 ) * 320 = 100 hardness worth of logs, which is 50 Logs)" )
                        .translation( "pmmo.levelsPerHardnessWoodcutting" )
                        .defineInRange( "levelsPerHardnessWoodcutting", 240D, 0.01, 10000 ) );

                this.levelsPerHardnessExcavation = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 16, your level is 50, and you have 64 charge, you can vein (50 / 320 ) * 320 = 50 hardness worth of ground, which is 100 Dirt)" )
                        .translation( "pmmo.levelsPerHardnessExcavation" )
                        .defineInRange( "levelsPerHardnessExcavation", 240D, 0.01, 10000 ) );

                this.levelsPerHardnessFarming = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? Plants have no hardness, but there is a minimum hardness while veining config in here, which is 0.5 by default, making it 200 plants at level 50 farming, with 320 charge, if this is set to 160" )
                        .translation( "pmmo.levelsPerHardnessFarming" )
                        .defineInRange( "levelsPerHardnessFarming", 240D, 0.1, 10000 ) );

                this.levelsPerHardnessCrafting = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 80, your level is 50, and you have 320 charge, you can vein (50 / 80 ) * 320 = 200 hardness worth of Crafting Related (Such as wool, carpet, bed) blocks, which depends on how hard they are)" )
                        .translation( "pmmo.levelsPerHardnessCrafting" )
                        .defineInRange( "levelsPerHardnessCrafting", 240D, 0.1, 10000 ) );

                this.maxVeinCharge = subscriber.subscribe(builder
                        .comment( "How much vein charge can a player hold at max? (1 recharges every second)" )
                        .translation( "pmmo.maxVeinCharge" )
                        .defineInRange( "maxVeinCharge", 320D, 0, 100000 ) );

                this.sleepVeinRestorePercent = subscriber.subscribe(builder
                        .comment( "How much vein is restored for each player when the day is skipped by sleeping in bed?" )
                        .translation( "pmmo.sleepVeinRestorePercent" )
                        .defineInRange( "sleepVeinRestorePercent", 0.8477D, 0, 1) );

                this.exhaustionPerBlock = subscriber.subscribe(builder
                        .comment( "How much hunger should be exhausted per block veined?" )
                        .translation( "pmmo.exhaustionPerBlock" )
                        .defineInRange( "exhaustionPerBlock", 0.2D, 0, 20 ) );

                this.toolSpeedVeinScale = subscriber.subscribe(builder
                        .comment( "Boost in veining ability dependant on your tool's speed (5 is fairly balanced, raising this number makes tools more powerful while veining)" )
                        .translation( "pmmo.toolSpeedVeinScale" )
                        .defineInRange( "toolSpeedVeinScale", 5D, 0.000000001, 1000000000 ) );

                builder.pop();
            }

            builder.push( "Mob Scaling" );
            {
                this.maxMobDamageBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's damage will be boosted?" )
                        .translation( "pmmo.maxMobDamageBoost" )
                        .defineInRange( "maxMobDamageBoost", 100D, 0, 1000000000 ) );

                this.mobDamageBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's damage will increase per one Power Level?" )
                        .translation( "pmmo.mobDamageBoostPerPowerLevel" )
                        .defineInRange( "mobDamageBoostPerPowerLevel", 1D, 0, 10 ) );

                this.maxMobHPBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's HP will be boosted?" )
                        .translation( "pmmo.maxMobHPBoost" )
                        .defineInRange( "maxMobHPBoost", 1000D, 0, 1024) );

                this.mobHPBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's HP will increase per one Power Level?" )
                        .translation( "pmmo.mobHPBoostPerPowerLevel" )
                        .defineInRange( "mobHPBoostPerPowerLevel", 5D, 0, 100 ) );

                this.maxMobSpeedBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's speed will be boosted?" )
                        .translation( "pmmo.maxMobSpeedBoost" )
                        .defineInRange( "maxMobSpeedBoost", 10D, 0, 100 ) );

                this.mobSpeedBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's speed will increase per one Power Level?" )
                        .translation( "pmmo.mobSpeedBoostPerPowerLevel" )
                        .defineInRange( "mobSpeedBoostPerPowerLevel", 1D, 0, 10 ) );

                this.biomeMobMultiplierEnabled = subscriber.subscribe(builder
                        .comment( "Should mob xp multipliers inside of biomes be enabled? false means no multipliers" )
                        .translation( "pmmo.biomeMobMultiplierEnabled" )
                        .define( "biomeMobMultiplierEnabled", true ) );


                builder.pop();
            }

            builder.push( "Requirements" );
            {
                this.wearReqEnabled = subscriber.subscribe(builder
                        .comment( "Should wear requirements be enabled? false means no requirements" )
                        .translation( "pmmo.wearReqEnabled" )
                        .define( "wearReqEnabled", true ) );

                this.enchantUseReqEnabled = subscriber.subscribe(builder
                        .comment( "Should Enchantment Use requirements be enabled? false means no requirements" )
                        .translation( "pmmo.enchantUseReqEnabled" )
                        .define( "enchantUseReqEnabled", true ) );

                this.enchantUseReqAutoScaleEnabled = subscriber.subscribe(builder
                        .comment( "Should Enchantment Use requirements automatically scale according to previous values, provided they exist? example: level1Req = 5 agility, level2Req = 10 farming - Level 4 enchantment would require level 10 agility, and level 20 farming (highestSpecifiedLevelReqs / highestSpecifiedLevel * enchantLevel)" )
                        .translation( "pmmo.enchantUseReqAutoScaleEnabled" )
                        .define( "enchantUseReqAutoScaleEnabled", true ) );

                this.toolReqEnabled = subscriber.subscribe(builder
                        .comment( "Should tool requirements be enabled? false means no requirements" )
                        .translation( "pmmo.toolReqEnabled" )
                        .define( "toolReqEnabled", true ) );

                this.weaponReqEnabled = subscriber.subscribe(builder
                        .comment( "Should weapon requirements be enabled? false means no requirements" )
                        .translation( "pmmo.weaponReqEnabled" )
                        .define( "weaponReqEnabled", true ) );

                this.killReqEnabled = subscriber.subscribe(builder
                        .comment( "Should mob kill req be enabled? false means no requirements" )
                        .translation( "pmmo.killReqEnabled" )
                        .define( "killReqEnabled", true ) );

                this.killXpEnabled = subscriber.subscribe(builder
                        .comment( "Should mob kill xp be enabled? false means no requirements" )
                        .translation( "pmmo.killXpEnabled" )
                        .define( "killXpEnabled", true ) );

                this.mobRareDropEnabled = subscriber.subscribe(builder
                        .comment( "Should mob rare drops be enabled? false means no requirements" )
                        .translation( "pmmo.mobRareDropEnabled" )
                        .define( "mobRareDropEnabled", true ) );


                this.useReqEnabled = subscriber.subscribe(builder
                        .comment( "Should use requirements be enabled? false means no requirements" )
                        .translation( "pmmo.useReqEnabled" )
                        .define( "useReqEnabled", true ) );

                this.placeReqEnabled = subscriber.subscribe(builder
                        .comment( "Should place requirements be enabled? false means no requirements" )
                        .translation( "pmmo.placeReqEnabled" )
                        .define( "placeReqEnabled", true ) );

                this.breakReqEnabled = subscriber.subscribe(builder
                        .comment( "Should break requirements be enabled? false means no requirements" )
                        .translation( "pmmo.breakReqEnabled" )
                        .define( "breakReqEnabled", true ) );

                this.biomeReqEnabled = subscriber.subscribe(builder
                        .comment( "Should biome requirements be enabled? false means no requirements" )
                        .translation( "pmmo.biomeReqEnabled" )
                        .define( "biomeReqEnabled", true ) );

                this.dimensionTravelReqEnabled = subscriber.subscribe(builder
                        .comment( "Should dimensional travel requirements be enabled? false means no requirements" )
                        .translation( "pmmo.dimensionTravelReqEnabled" )
                        .define( "dimensionTravelReqEnabled", true ) );

                this.craftReqEnabled = subscriber.subscribe(builder
                        .comment( "Should certain items be restricted from being crafted, without the level requirement?" )
                        .translation( "pmmo.craftReqEnabled" )
                        .define( "craftReqEnabled", true ) );

                this.negativeBiomeEffectEnabled = subscriber.subscribe(builder
                        .comment( "Should biome negative effects be enabled? false means no negative effects" )
                        .translation( "pmmo.negativeBiomeEffectEnabled" )
                        .define( "negativeBiomeEffectEnabled", true ) );

                this.positiveBiomeEffectEnabled = subscriber.subscribe(builder
                        .comment( "Should biome positive effects be enabled? false means no positive effects" )
                        .translation( "pmmo.positiveBiomeEffectEnabled" )
                        .define( "positiveBiomeEffectEnabled", true ) );

                this.biomeXpBonusEnabled = subscriber.subscribe(builder
                        .comment( "Should xp multipliers be enabled? false means no multipliers" )
                        .translation( "pmmo.biomeXpBonusEnabled" )
                        .define( "biomeXpBonusEnabled", true ) );

                this.xpValueGeneralEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for general things be enabled? (Such as catching fish)" )
                        .translation( "pmmo.xpValueGeneralEnabled" )
                        .define( "xpValueGeneralEnabled", true ) );

                this.xpValueBreakingEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for breaking things first time be enabled? false means only Hardness xp is awarded for breaking" )
                        .translation( "pmmo.xpValueBreakingEnabled" )
                        .define( "xpValueBreakingEnabled", true ) );

                this.oreEnabled = subscriber.subscribe(builder
                        .comment( "Should ores be enabled? false means no extra chance" )
                        .translation( "pmmo.oreEnabled" )
                        .define( "oreEnabled", true ) );

                this.logEnabled = subscriber.subscribe(builder
                        .comment( "Should logs be enabled? false means no extra chance" )
                        .translation( "pmmo.logEnabled" )
                        .define( "logEnabled", true ) );

                this.plantEnabled = subscriber.subscribe(builder
                        .comment( "Should plants be enabled? false means no extra chance" )
                        .translation( "pmmo.plantEnabled" )
                        .define( "plantEnabled", true ) );

                this.salvageEnabled = subscriber.subscribe(builder
                        .comment( "Is Salvaging items using the Repairing skill enabled? false = off" )
                        .translation( "pmmo.salvageEnabled" )
                        .define( "salvageEnabled", true ) );

                this.fishPoolEnabled = subscriber.subscribe(builder
                        .comment( "Is catching items from Fish Pool while Fishing enabled? false = off" )
                        .translation( "pmmo.fishPoolEnabled" )
                        .define( "fishPoolEnabled", true ) );

                this.fishEnchantPoolEnabled = subscriber.subscribe(builder
                        .comment( "Should fished items have a chance at being Enchanted? enabled? false = off" )
                        .translation( "pmmo.fishEnchantPoolEnabled" )
                        .define( "fishEnchantPoolEnabled", true ) );

                this.levelUpCommandEnabled = subscriber.subscribe(builder
                        .comment( "Commands being fired on specific level ups enabled? false = off" )
                        .translation( "pmmo.levelUpCommandEnabled" )
                        .define( "levelUpCommandEnabled", true ) );

                this.heldItemXpBoostEnabled = subscriber.subscribe(builder
                        .comment( "Main held items xp multiplier enabled? false = off" )
                        .translation( "pmmo.heldItemXpBoostEnabled" )
                        .define( "heldItemXpBoostEnabled", true ) );

                this.wornItemXpBoostEnabled = subscriber.subscribe(builder
                        .comment( "worn items xp boost enabled? false = off" )
                        .translation( "pmmo.wornItemXpBoostEnabled" )
                        .define( "wornItemXpBoostEnabled", true ) );

                this.loadDefaultConfig = subscriber.subscribe(builder
                        .comment( "Should config from default_data.json be loaded? false means only data.json is loaded" )
                        .translation( "pmmo.loadDefaultConfig" )
                        .define( "loadDefaultConfig", true ) );

                this.strictReqTool = subscriber.subscribe(builder
                        .comment( "When a Tool requirement is not met, should the player be stopped from breaking with it completely?" )
                        .translation( "pmmo.strictReqTool" )
                        .define( "strictReqTool", true ) );

                this.strictReqKill = subscriber.subscribe(builder
                        .comment( "When a Kill requirement is not met, should the player be stopped from dealing any damage?" )
                        .translation( "pmmo.strictReqKill" )
                        .define( "strictReqKill", true ) );

                this.strictReqWeapon = subscriber.subscribe(builder
                        .comment( "When a Weapon requirement is not met, should the player be stopped from dealing any damage?" )
                        .translation( "pmmo.strictReqWeapon" )
                        .define( "strictReqWeapon", false ) );

                this.strictReqWear = subscriber.subscribe(builder
                        .comment( "When a Wear requirement is not met, should the item be dropped?" )
                        .translation( "pmmo.strictReqWear" )
                        .define( "strictReqWear", false ) );

                this.strictReqUseEnchantment = subscriber.subscribe(builder
                        .comment( "When a Use Enchantment requirement is not met, should the item be dropped?" )
                        .translation( "pmmo.strictReqUseEnchantment" )
                        .define( "strictReqUseEnchantment", false ) );

                builder.pop();
            }

            builder.push( "Levels" );
            {
                this.maxLevel = subscriber.subscribe(builder
                        .comment( "What is the global max level" )
                        .translation( "pmmo.maxLevel" )
                        .defineInRange( "maxLevel", 1523, 1, 2000000000 ) );

                this.baseXp = subscriber.subscribe(builder
                        .comment( "What is the baseXp to reach level 2 ( baseXp + level * xpPerLevel )" )
                        .translation( "pmmo.baseXp" )
                        .defineInRange( "baseXp", 250D, 1, 1000000 ) );

                this.xpIncreasePerLevel = subscriber.subscribe(builder
                        .comment( "What is the xp increase per level ( baseXp + level * xpPerLevel )" )
                        .translation( "pmmo.xpIncreasePerLevel" )
                        .defineInRange( "xpIncreasePerLevel", 50D, 1, 1000000 ) );

                this.levelsPerMilestone = subscriber.subscribe(builder
                        .comment( "Every how many levels should a level up broadcast be sent to all players? (10 = every 10 levels)" )
                        .translation( "pmmo.levelsPerMilestone" )
                        .defineInRange( "levelsPerMilestone", 10, 1, 1000000 ) );

                this.levelsPerTotalLevelMilestone = subscriber.subscribe(builder
                        .comment( "Every how many levels should a total level milestone broadcast be sent to all players? (50 = every 50 levels)" )
                        .translation( "pmmo.levelsPerTotalLevelMilestone" )
                        .defineInRange( "levelsPerTotalLevelMilestone", 50, 1, 1000000 ) );

                this.wipeAllSkillsUponDeathPermanently = subscriber.subscribe(builder
                        .comment( "Should a player have all their skills wiped to level 1 upon death?" )
                        .translation( "pmmo.wipeAllSkillsUponDeathPermanently" )
                        .define( "wipeAllSkillsUponDeathPermanently", false ) );

                this.broadcastMilestone = subscriber.subscribe(builder
                        .comment( "Should every 10th level up be broadcast to everyone?" )
                        .translation( "pmmo.broadcastMilestone" )
                        .define( "broadcastMilestone", true ) );

                this.levelUpFirework = subscriber.subscribe(builder
                        .comment( "Should fireworks appear on level up?" )
                        .translation( "pmmo.levelUpFirework" )
                        .define( "levelUpFirework", true ) );

                this.milestoneLevelUpFirework = subscriber.subscribe(builder
                        .comment( "Should fireworks appear on Milestone level up, to other players?" )
                        .translation( "pmmo.milestoneLevelUpFirework" )
                        .define( "milestoneLevelUpFirework", true ) );

                this.spawnFireworksCausedByMe = subscriber.subscribe(builder
                        .comment( "Should fireworks appear on Milestone level up, to anyone, by you?" )
                        .translation( "pmmo.spawnFireworksCausedByMe" )
                        .define( "spawnFireworksCausedByMe", true ) );

                this.spawnFireworksCausedByOthers = subscriber.subscribe(builder
                        .comment( "Should fireworks appear on Milestone level up, from other players?" )
                        .translation( "pmmo.spawnFireworksCausedByOthers" )
                        .define( "spawnFireworksCausedByOthers", true ) );

                this.deathLoosesLevels = subscriber.subscribe(builder
                        .comment( "Should players loose Percentage of Full Levels instead of Xp above Whole Level upon death?" )
                        .translation( "pmmo.deathLoosesLevels" )
                        .define( "deathLoosesLevels", false ) );

                this.useExponentialFormula = subscriber.subscribe(builder
                        .comment( "Should levels be determined using an Exponential formula? (false = the original way)" )
                        .translation( "pmmo.useExponentialFormula" )
                        .define( "useExponentialFormula", true ) );

                this.exponentialBaseXp = subscriber.subscribe(builder
                        .comment( "What is the x in: x * ( exponentialBase^( exponentialRate * level ) )" )
                        .translation( "pmmo.exponentialBaseXp" )
                        .defineInRange( "exponentialBaseXp", 83D, 1, 1000000 ) );

                this.exponentialBase = subscriber.subscribe(builder
                        .comment( "What is the x in: exponentialBaseXp * ( x^( exponentialRate * level ) )" )
                        .translation( "pmmo.exponentialBase" )
                        .defineInRange( "exponentialBase", 1.104088404342588D, 0, 1000000 ) );

                this.exponentialRate = subscriber.subscribe(builder
                        .comment( "What is the x in: exponentialBaseXp * ( exponentialBase^( x * level ) )" )
                        .translation( "pmmo.exponentialRate" )
                        .defineInRange( "exponentialRate", 1D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Multipliers" );
            {
                this.globalMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.globalMultiplier" )
                        .defineInRange( "globalMultiplier", 1D, 0, 1000 ) );

                this.peacefulMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Peaceful Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.peacefulMultiplier" )
                        .defineInRange( "peacefulMultiplier", 1/3D, 0, 1000 ) );

                this.easyMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Easy Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.easyMultiplier" )
                        .defineInRange( "easyMultiplier", 2/3D, 0, 1000 ) );

                this.normalMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Normal Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.normalMultiplier" )
                        .defineInRange( "normalMultiplier", 1D, 0, 1000 ) );

                this.hardMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Hard Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.hardMultiplier" )
                        .defineInRange( "hardMultiplier", 4/3D, 0, 1000 ) );

                this.biomePenaltyMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp you get in biomes you do not meet the requirements for (1 = Full xp, 0.5 = Half xp)" )
                        .translation( "pmmo.biomePenaltyMultiplier" )
                        .defineInRange( "biomePenaltyMultiplier", 0.5D, 0, 1) );

                this.deathPenaltyMultiplier = subscriber.subscribe(builder
                        .comment( "How much percentage of level you loose on death (Full Levels or Xp above Whole Level depends on deathLoosesLevels)" )
                        .translation( "pmmo.deathPenaltyMultiplier" )
                        .defineInRange( "deathPenaltyMultiplier", 0.5D, 0, 1) );

                builder.pop();
            }

            builder.push( "GUI" );
            {
                this.xpBarTheme = subscriber.subscribe(builder
                        .comment( "True is the animated rainbow, False is the old, plain grey box" )
                        .translation( "pmmo.xpBarTheme" )
                        .define( "xpBarTheme", true ) );

                this.barOffsetX = subscriber.subscribe(builder
                        .comment( "GUI bar position X (Width)" )
                        .translation( "pmmo.barOffsetX" )
                        .defineInRange( "barOffsetX", 0.5D, 0, 1) );

                this.barOffsetY = subscriber.subscribe(builder
                        .comment( "GUI bar position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
                        .translation( "pmmo.barOffsetY" )
                        .defineInRange( "barOffsetY", 0D, 0, 1) );

                this.veinBarOffsetX = subscriber.subscribe(builder
                        .comment( "GUI bar position X (Width)" )
                        .translation( "pmmo.veinBarOffsetX" )
                        .defineInRange( "veinBarOffsetX", 0.5D, 0, 1) );

                this.veinBarOffsetY = subscriber.subscribe(builder
                        .comment( "GUI bar position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
                        .translation( "pmmo.veinBarOffsetY" )
                        .defineInRange( "veinBarOffsetY", 0.65D, 0, 1) );


                this.xpDropOffsetX = subscriber.subscribe(builder
                        .comment( "GUI Xp drops position X (Width)" )
                        .translation( "pmmo.xpDropOffsetX" )
                        .defineInRange( "xpDropOffsetX", 0.5D, 0, 1) );

                this.xpDropOffsetY = subscriber.subscribe(builder
                        .comment( "GUI Xp drops position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
                        .translation( "pmmo.xpDropOffsetY" )
                        .defineInRange( "xpDropOffsetY", 0D, 0, 1) );

                this.skillListOffsetX = subscriber.subscribe(builder
                        .comment( "GUI Skills List position X (Width)" )
                        .translation( "pmmo.skillListOffsetX" )
                        .defineInRange( "skillListOffsetX", 0D, 0, 1) );

                this.skillListOffsetY = subscriber.subscribe(builder
                        .comment( "GUI Skills List position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
                        .translation( "pmmo.skillListOffsetY" )
                        .defineInRange( "skillListOffsetY", 0D, 0, 1) );

                this.xpDropSpawnDistance = subscriber.subscribe(builder
                        .comment( "How far away does the Xp Drop spawn" )
                        .translation( "pmmo.xpDropSpawnDistance" )
                        .defineInRange( "xpDropSpawnDistance", 50D, 0, 1000 ) );

                this.xpDropOpacityPerTime = subscriber.subscribe(builder
                        .comment( "How much out of MaxOpacity does the Xp Drop become visible per 1 distance" )
                        .translation( "pmmo.xpDropOpacityPerTime" )
                        .defineInRange( "xpDropOpacityPerTime", 5D, 0, 255) );

                this.xpDropMaxOpacity = subscriber.subscribe(builder
                        .comment( "How opaque (visible) can the xp drop get?" )
                        .translation( "pmmo.xpDropMaxOpacity" )
                        .defineInRange( "xpDropMaxOpacity", 200D, 0, 255) );

                this.xpDropDecayAge = subscriber.subscribe(builder
                        .comment( "At what age do xp drops start to decay?" )
                        .translation( "pmmo.xpDropDecayAge" )
                        .defineInRange( "xpDropDecayAge", 350D, 0, 5000 ) );

                this.minXpGrow = subscriber.subscribe(builder
                        .comment( "What is the minimum amount xp grows a set amount of time? (Default 0.2, increase to speed up growth)" )
                        .translation( "pmmo.minXpGrow" )
                        .defineInRange( "minXpGrow", 5D, 0.01D, 100D ) );

                this.xpDropsAttachedToBar = subscriber.subscribe(builder
                        .comment( "Should xp drops sync up with the bar being open or closed? HIGHLY RECOMMEND TO KEEP FALSE IF YOU ARE MOVING XP DROP POSITIONS" )
                        .translation( "pmmo.xpDropsAttachedToBar" )
                        .define( "xpDropsAttachedToBar", true ) );

                this.showSkillsListAtCorner = subscriber.subscribe(builder
                        .comment( "If Off, The skills list at the top left corner will no longer appear (You still have the GUI to show you all of your skills info)" )
                        .translation( "pmmo.showSkillsListAtCorner" )
                        .define( "showSkillsListAtCorner", true ) );

                this.showXpDrops = subscriber.subscribe(builder
                        .comment( "If Off, xp drops will no longer appear" )
                        .translation( "pmmo.showXpDrops" )
                        .define( "showXpDrops", true ) );

                this.stackXpDrops = subscriber.subscribe(builder
                        .comment( "If Off, xp drops will no longer stack with each other" )
                        .translation( "pmmo.stackXpDrops" )
                        .define( "stackXpDrops", true ) );

                this.xpBarAlwaysOn = subscriber.subscribe(builder
                        .comment( "Should the Xp Bar always be on? false = only appears while holding Show GUI or when you gain xp" )
                        .translation( "pmmo.xpBarAlwaysOn" )
                        .define( "xpBarAlwaysOn", false ) );

                this.xpLeftDisplayAlwaysOn = subscriber.subscribe(builder
                        .comment( "Should the Xp left indicator always be on? false = only appears with Show GUI key" )
                        .translation( "pmmo.xpLeftDisplayAlwaysOn" )
                        .define( "xpLeftDisplayAlwaysOn", false ) );

                this.lvlUpScreenshot = subscriber.subscribe(builder
                        .comment( "Should a screenshot be taken everytime you level up?" )
                        .translation( "pmmo.lvlUpScreenshot" )
                        .define( "lvlUpScreenshot", false ) );

                this.lvlUpScreenshotShowSkills = subscriber.subscribe(builder
                        .comment( "When a screenshot is taken upon levelling up, should the skills list turn on automatically to be included in the screenshot?" )
                        .translation( "pmmo.lvlUpScreenshotShowSkills" )
                        .define( "lvlUpScreenshotShowSkills", false ) );

                this.xpDropsShowXpBar = subscriber.subscribe(builder
                        .comment( "Should Xp Drops make the Xp Bar pop up?" )
                        .translation( "pmmo.xpDropsShowXpBar" )
                        .define( "xpDropsShowXpBar", true ) );

                this.showLevelUpUnlocks = subscriber.subscribe(builder
                        .comment( "Should you be notified by what new features you have access to on level ups?" )
                        .translation( "pmmo.showLevelUpUnlocks" )
                        .define( "showLevelUpUnlocks", true ) );

                this.worldXpDropsEnabled = subscriber.subscribe(builder
                        .comment( "Should World Xp Drops appear when xp is gained?" )
                        .translation( "pmmo.worldXpDropsEnabled" )
                        .define( "worldXpDropsEnabled", true ) );

                this.worldXpDropsShowSkill = subscriber.subscribe(builder
                        .comment( "Should World Xp Drops Skill appear?" )
                        .translation( "pmmo.worldXpDropsShowSkill" )
                        .define( "worldXpDropsShowSkill", true ) );

                this.showOthersWorldXpDrops = subscriber.subscribe(builder
                        .comment( "Should World Xp Drops of other people show up for you?" )
                        .translation( "pmmo.showOthersWorldXpDrops" )
                        .define( "showOthersWorldXpDrops", false ) );

                this.worldXpDropsSizeMultiplier = subscriber.subscribe(builder
                        .comment( "Value by which World Xp Drops Size is scaled (2 = twice as big)" )
                        .translation( "pmmo.worldXpDropsSizeMultiplier" )
                        .defineInRange( "worldXpDropsSizeMultiplier", 1D, 0.01D, 100D ) );

                this.worldXpDropsDecaySpeedMultiplier = subscriber.subscribe(builder
                        .comment( "Value by which World Xp Drops Decay Speed is scaled (2 = twice as fast)" )
                        .translation( "pmmo.worldXpDropsDecaySpeedMultiplier" )
                        .defineInRange( "worldXpDropsDecaySpeedMultiplier", 1D, 0.01D, 100D ) );

                this.worldXpDropsRotationCap = subscriber.subscribe(builder
                        .comment( "How far (Max) should World Xp Drops be rotated (Degrees, either direction from flat)" )
                        .translation( "pmmo.worldXpDropsDecaySpeedMultiplier" )
                        .defineInRange( "worldXpDropsDecaySpeedMultiplier", 1D, 0D, 180D ) );

                builder.pop();
            }

            builder.push( "Breaking Speed" );
            {
                this.minBreakSpeed = subscriber.subscribe(builder
                        .comment( "Minimum Breaking Speed (1 is Original speed, 0.5 is half)" )
                        .translation( "pmmo.minBreakSpeed" )
                        .defineInRange( "minBreakSpeed", 0.5D, 0, 100 ) );

                this.blocksToUnbreakableY = subscriber.subscribe(builder
                        .comment( "How many blocks it takes to reach 0 Break Speed (will get capped by Minimum Breaking Speed)" )
                        .translation( "pmmo.blocksToUnbreakableY" )
                        .defineInRange( "blocksToUnbreakableY", 1000D, -Double.MAX_VALUE, Double.MAX_VALUE) );

                this.miningBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your mining speed increases per level (1 = 1% increase per level)" )
                        .translation( "pmmo.miningBonusSpeed" )
                        .defineInRange( "miningBonusSpeed", 1D, 0, 10 ) );

                this.woodcuttingBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your cutting speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.woodcuttingBonusSpeed" )
                        .defineInRange( "woodcuttingBonusSpeed", 1D, 0, 10 ) );

                this.excavationBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your digging speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.excavationBonusSpeed" )
                        .defineInRange( "excavationBonusSpeed", 1D, 0, 10 ) );

                this.farmingBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your farming speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.farmingBonusSpeed" )
                        .defineInRange( "farmingBonusSpeed", 1D, 0, 10 ) );

                builder.pop();
            }

            builder.push( "Mining" );
            {
                this.blockHardnessLimitForBreaking = subscriber.subscribe(builder
                        .comment( "Hardest considered block (1 hardness = 1 remove xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
                        .translation( "pmmo.blockHardnessLimitForBreaking" )
                        .defineInRange( "blockHardnessLimitForBreaking", 20D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Building" );
            {
                this.levelsPerOneReach = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of reach" )
                        .translation( "pmmo.levelsPerOneReach" )
                        .defineInRange( "levelsPerOneReach", 20D, 1, 1000000000 ) );

                this.maxExtraReachBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum reach a player can have" )
                        .translation( "pmmo.maxExtraReachBoost" )
                        .defineInRange( "maxExtraReachBoost", 20D, 0, 1000000000 ) );

                this.blockHardnessLimitForPlacing = subscriber.subscribe(builder
                        .comment( "Hardest considered block (1 hardness = 1 build xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
                        .translation( "pmmo.blockHardnessLimitForPlacing" )
                        .defineInRange( "blockHardnessLimitForPlacing", 20D, 0, 1000000 ) );

                this.xpValuePlacingEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for crafting be enabled? false means the hardness value is used" )
                        .translation( "pmmo.xpValuePlacingEnabled" )
                        .define( "xpValuePlacingEnabled", true ) );


                builder.pop();
            }

            builder.push( "Excavation" );
            {
                this.treasureEnabled = subscriber.subscribe(builder
                        .comment( "Do players find Treasure inside of blocks?" )
                        .translation( "pmmo.treasureEnabled" )
                        .define( "treasureEnabled", true ) );

                builder.pop();
            }
            builder.push( "Woodcutting" );
            {
                builder.pop();
            }
            builder.push( "Farming" );
            {
                this.breedingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for breeding animals?" )
                        .translation( "pmmo.breedingXpEnabled" )
                        .define( "breedingXpEnabled", true ) );

                this.defaultBreedingXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Farming for breeding two animals? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultBreedingXp" )
                        .defineInRange( "defaultBreedingXp", 10.0D, 0, 1000000 ) );

                this.defaultSaplingGrowXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Farming for growing a sapling? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultBreedingXp" )
                        .defineInRange( "defaultBreedingXp", 25.0D, 0, 1000000 ) );

                this.defaultCropGrowXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Farming for growing crops? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultBreedingXp" )
                        .defineInRange( "defaultBreedingXp", 15.0D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Agility" );
            {
                this.maxFallSaveChance = subscriber.subscribe(builder
                        .comment( "Maximum chance to save each point of fall damage (100 = no fall damage)" )
                        .translation( "pmmo.maxFallSaveChance" )
                        .defineInRange( "maxFallSaveChance", 64D, 0, 100 ) );

                this.saveChancePerLevel = subscriber.subscribe(builder
                        .comment( "How much your chance to save each point of fall damage increases per level (1 = 1% increase per Level)" )
                        .translation( "pmmo.saveChancePerLevel" )
                        .defineInRange( "saveChancePerLevel", 64D, 0, 100 ) );

                this.maxJumpBoost = subscriber.subscribe(builder
                        .comment( "How much jump boost can you gain max (above 0.33 makes you take fall damage)" )
                        .translation( "pmmo.maxJumpBoost" )
                        .defineInRange( "maxJumpBoost", 0.33D, 0, 100 ) );

                this.levelsPerCrouchJumpBoost = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of jumping height while Crouching" )
                        .translation( "pmmo.levelsPerCrouchJumpBoost" )
                        .defineInRange( "levelsPerCrouchJumpBoost", 45D, 1, 1000000000 ) );

                this.levelsPerSprintJumpBoost = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of jumping height while Sprinting" )
                        .translation( "pmmo.levelsPerSprintJumpBoost" )
                        .defineInRange( "levelsPerSprintJumpBoost", 60D, 1, 1000000000 ) );

                this.maxSpeedBoost = subscriber.subscribe(builder
                        .comment( "How much speed boost you can get from Agility (100 = 100% vanilla + 100% = twice as fast max)" )
                        .translation( "pmmo.maxSpeedBoost" )
                        .defineInRange( "maxSpeedBoost", 100D, 0, 1000000000 ) );

                this.speedBoostPerLevel = subscriber.subscribe(builder
                        .comment( "How much speed boost you get from each level (1 = 1% speed boost per level)" )
                        .translation( "pmmo.speedBoostPerLevel" )
                        .defineInRange( "speedBoostPerLevel", 0.5D, 0, 100 ) );

                builder.pop();
            }

            builder.push( "Endurance" );
            {
                this.maxEndurance = subscriber.subscribe(builder
                        .comment( "How much endurance is max (100 = god mode)" )
                        .translation( "pmmo.maxEndurance" )
                        .defineInRange( "maxEndurance", 50D, 0, 100 ) );

                this.endurancePerLevel = subscriber.subscribe(builder
                        .comment( "How much endurance you gain per level (1 = 1% per level)" )
                        .translation( "pmmo.endurancePerLevel" )
                        .defineInRange( "endurancePerLevel", 0.25D, 0, 100 ) );

                this.levelsPerHeart = subscriber.subscribe(builder
                        .comment( "Per how many levels you gain 1 Max Heart" )
                        .translation( "pmmo.levelsPerHeart" )
                        .defineInRange( "levelsPerHeart", 10D, 1, 1000000000 ) );

                this.maxExtraHeartBoost = subscriber.subscribe(builder
                        .comment( "How many Max Hearts you can have (20 means 10 vanilla + 20 boosted)" )
                        .translation( "pmmo.maxExtraHeartBoost" )
                        .defineInRange( "maxExtraHeartBoost", 100, 0, 1000000000 ) );

                this.hpRegenPerMinuteBase = subscriber.subscribe(builder
                        .comment( "How many half hearts regenerate per minute at level 0 Endurance (at 1, and at level 0 Endurance, you regenerate half a heart every 60 seconds, 0 means no base regeneration)" )
                        .translation( "pmmo.hpRegenPerMinuteBase" )
                        .defineInRange( "hpRegenPerMinuteBase", 1D, 0, 1000 ) );

                this.hpRegenPerMinuteBoostPerLevel = subscriber.subscribe(builder
                        .comment( "Addition per level to hpRegenPerMinuteBase (if set to 0.01, every 100 Endurance levels, you regen 1 more half heart per 60 seconds, 0 means no extra regeneration)" )
                        .translation( "pmmo.hpRegenPerMinuteBoostPerLevel" )
                        .defineInRange( "hpRegenPerMinuteBoostPerLevel", 0.02D, 0, 1000 ) );

                this.hpRegenXpMultiplier = subscriber.subscribe(builder
                        .comment( "Multiplier for xp gained in endurance from Regeneration" )
                        .translation( "pmmo.hpRegenXpMultiplier" )
                        .defineInRange( "hpRegenXpMultiplier", 15.23D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Combat" );
            {
                this.damageBonusPercentPerLevelMelee = subscriber.subscribe(builder
                        .comment( "How much percentage bonus damage do you get per Combat level in Melee?" )
                        .translation( "pmmo.damageBonusPercentPerLevelMelee" )
                        .defineInRange( "damageBonusPercentPerLevelMelee", 0.005D, 0.001, 1000 ) );

                this.maxExtraDamagePercentageBoostMelee = subscriber.subscribe(builder
                        .comment( "How much extra damage can you get from the Combat skill max?" )
                        .translation( "pmmo.maxExtraDamagePercentageBoostMelee" )
                        .defineInRange( "maxExtraDamagePercentageBoostMelee", 100D, 0, 1000000000 ) );

                builder.pop();
            }

            builder.push( "Archery" );
            {
                this.damageBonusPercentPerLevelArchery = subscriber.subscribe(builder
                        .comment( "How much percentage bonus damage do you get per Archery level in Archery?" )
                        .translation( "pmmo.damageBonusPercentPerLevelArchery" )
                        .defineInRange( "damageBonusPercentPerLevelArchery", 0.005D, 0.001, 1000 ) );

                this.maxExtraDamagePercentageBoostArchery = subscriber.subscribe(builder
                        .comment( "How much extra damage can you get from the Archery skill max?" )
                        .translation( "pmmo.maxExtraDamagePercentageBoostArchery" )
                        .defineInRange( "maxExtraDamagePercentageBoostArchery", 100D, 0, 1000000000 ) );

                builder.pop();
            }

            builder.push( "Smithing" );
            {
                this.anvilHandlingEnabled = subscriber.subscribe(builder
                        .comment( "Should PMMO anvil handling be enabled? (xp rewards for repair, and also Enchantment handling) (some mod items break, if you experience lost enchantments, set this to false)" )
                        .translation( "pmmo.anvilHandlingEnabled" )
                        .define( "anvilHandlingEnabled", true ) );

                this.maxSalvageEnchantChance = subscriber.subscribe(builder
                        .comment( "Max Percentage chance to return each Enchantment Level" )
                        .translation( "pmmo.maxSalvageEnchantChance" )
                        .defineInRange( "maxSalvageEnchantChance", 90D, 0, 100 ) );

                this.enchantSaveChancePerLevel = subscriber.subscribe(builder
                        .comment( "Each Enchantment Save Chance per Level" )
                        .translation( "pmmo.enchantSaveChancePerLevel" )
                        .defineInRange( "enchantSaveChancePerLevel", 0.9D, 0, 100 ) );

                this.anvilCostReductionPerLevel = subscriber.subscribe(builder
                        .comment( "Vanilla starts at 50, hence: (50 - [this] * level)" )
                        .translation( "pmmo.anvilCostReductionPerLevel" )
                        .defineInRange( "anvilCostReductionPerLevel", 0.25D, 0, 100 ) );

                this.extraChanceToNotBreakAnvilPerLevel = subscriber.subscribe(builder
                        .comment( "Chance to not break anvil, 100 = twice the value, half the chance per Level." )
                        .translation( "pmmo.extraChanceToNotBreakAnvilPerLevel" )
                        .defineInRange( "extraChanceToNotBreakAnvilPerLevel", 1D, 0, 100 ) );

                this.anvilFinalItemBonusRepaired = subscriber.subscribe(builder
                        .comment( "Bonus repair durability per level (100 = twice as much repair per level)" )
                        .translation( "pmmo.anvilFinalItemBonusRepaired" )
                        .defineInRange( "anvilFinalItemBonusRepaired", 1D, 0, 100 ) );

                this.anvilFinalItemMaxCostToAnvil = subscriber.subscribe(builder
                        .comment( "Vanilla caps at 50, at around 30 vanilla you can no longer anvil the item again. allows unlocking infinite Anvil uses." )
                        .translation( "pmmo.anvilFinalItemMaxCostToAnvil" )
                        .defineInRange( "anvilFinalItemMaxCostToAnvil", 10, 0, 50 ) );

                this.dualSalvageSmithingLevelReq = subscriber.subscribe(builder
                        .comment( "From what level can you salvage from both hands at the same time?" )
                        .translation( "pmmo.dualSalvageSmithingLevelReq" )
                        .defineInRange( "dualSalvageSmithingLevelReq", 50, 1, 99999) );

                this.bypassEnchantLimit = subscriber.subscribe(builder
                        .comment( "Anvil combination limits enchantments to max level set in this config" )
                        .translation( "pmmo.bypassEnchantLimit" )
                        .define( "bypassEnchantLimit", true ) );

                this.levelsPerOneEnchantBypass = subscriber.subscribe(builder
                        .comment( "How many levels per each Enchantment Level Bypass above max level enchantment can support in vanilla" )
                        .translation( "pmmo.levelsPerOneEnchantBypass" )
                        .defineInRange( "levelsPerOneEnchantBypass", 50, 1, 1000000000 ) );

                this.maxEnchantmentBypass = subscriber.subscribe(builder
                        .comment( "Max amount of levels enchants are able to go above max vanilla level" )
                        .translation( "pmmo.maxEnchantmentBypass" )
                        .defineInRange( "maxEnchantmentBypass", 10, 0, 1000000000 ) );

                this.maxEnchantLevel = subscriber.subscribe(builder
                        .comment( "Anvil combination limits enchantments to this level" )
                        .translation( "pmmo.maxEnchantLevel" )
                        .defineInRange( "maxEnchantLevel", 255, 0, 255) );

                this.upgradeChance = subscriber.subscribe(builder
                        .comment( "What is the chance to Bypass a max enchant level (provided you got the skill to do so)" )
                        .translation( "pmmo.upgradeChance" )
                        .defineInRange( "upgradeChance", 50D, 0, 100 ) );

                this.failedUpgradeKeepLevelChance = subscriber.subscribe(builder
                        .comment( "What is the chance to Reduce a level after a Upgrade chance fails (100 = everytime you fail bypass, enchant level goes down by 1)" )
                        .translation( "pmmo.failedUpgradeKeepLevelChance" )
                        .defineInRange( "failedUpgradeKeepLevelChance", 50D, 0, 100 ) );

                this.alwaysUseUpgradeChance = subscriber.subscribe(builder
                        .comment( "false = Upgrade Chance if only rolled if you are trying to upgrade your item ABOVE vanilla max level. true = you ALWAYS have an upgrade chance level." )
                        .translation( "pmmo.alwaysUseUpgradeChance" )
                        .define( "alwaysUseUpgradeChance", false) );

                this.smeltingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for Smelting items in a Furnace?" )
                        .translation( "pmmo.smeltingXpEnabled" )
                        .define( "smeltingXpEnabled", true ) );

                this.smeltingEnabled = subscriber.subscribe(builder
                        .comment( "Do Furnaces produce extra items according to Item Owner Smithing level?" )
                        .translation( "pmmo.smeltingEnabled" )
                        .define( "smeltingEnabled", true ) );

                builder.pop();
            }

            builder.push( "Cooking" );
            {
                this.cookingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for Cooking items in Furnaces/Smokers/Fireplaces?" )
                        .translation( "pmmo.cookingXpEnabled" )
                        .define( "cookingXpEnabled", true ) );

                this.cookingEnabled = subscriber.subscribe(builder
                        .comment( "Do Furnaces/Smokers/Fireplaces produce extra items according to Item Owner Cooking level?" )
                        .translation( "pmmo.cookingEnabled" )
                        .define( "cookingEnabled", true ) );

                builder.pop();
            }

            builder.push( "Alchemy" );
            {
                this.brewingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for Brewing potions in Brewing Stands?" )
                        .translation( "pmmo.brewingXpEnabled" )
                        .define( "brewingXpEnabled", true ) );

                this.brewingEnabled = subscriber.subscribe(builder
                        .comment( "Does Brewing provide a chance to produce Extra potions?" )
                        .translation( "pmmo.brewingEnabled" )
                        .define( "brewingEnabled", true ) );

                builder.pop();
            }

            builder.push( "Flying" );
            {
                builder.pop();
            }

            builder.push( "Swimming" );
            {
                this.nightvisionUnlockLevel = subscriber.subscribe(builder
                        .comment( "Underwater Nightvision Unlock Level" )
                        .translation( "pmmo.nightvisionUnlockLevel" )
                        .defineInRange( "nightvisionUnlockLevel", 25, 0, 1000000 ) );

                this.underwaterNightVision = subscriber.subscribe(builder
                        .comment( "Is Underwater Nightvision enabled?" )
                        .translation( "pmmo.underwaterNightVision" )
                        .define( "underwaterNightVision", true ) );

                builder.pop();
            }

            builder.push( "Fishing" );
            {
                this.disableNormalFishDrops = subscriber.subscribe(builder
                        .comment( "Should normal drops from Fishing be disabled?" )
                        .translation( "pmmo.disableNormalFishDrops" )
                        .define( "disableNormalFishDrops", false ) );

                this.fishPoolBaseChance = subscriber.subscribe(builder
                        .comment( "What is the chance on each successful fishing attempt to access the fish_pool" )
                        .translation( "pmmo.fishPoolBaseChance" )
                        .defineInRange( "fishPoolBaseChance", 0D, 0, 100 ) );

                this.fishPoolChancePerLevel = subscriber.subscribe(builder
                        .comment( "What is the increase per level to access the fish_pool" )
                        .translation( "pmmo.fishPoolChancePerLevel" )
                        .defineInRange( "fishPoolChancePerLevel", 0.1523D, 0, 100 ) );

                this.fishPoolMaxChance = subscriber.subscribe(builder
                        .comment( "What is the max chance to access the fish_pool" )
                        .translation( "pmmo.fishPoolMaxChance" )
                        .defineInRange( "fishPoolMaxChance", 80D, 0, 100 ) );


                builder.pop();
            }

            builder.push( "Crafting" );
            {
                this.xpValueCraftingEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for crafting be enabled? false means the default value is used" )
                        .translation( "pmmo.xpValueCraftingEnabled" )
                        .define( "xpValueCraftingEnabled", true ) );

                this.defaultCraftingXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Crafting for each item crafted? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultCraftingXp" )
                        .defineInRange( "defaultCraftingXp", 1D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Magic" );
            {
                this.damageBonusPercentPerLevelMagic = subscriber.subscribe(builder
                        .comment( "How much percentage bonus damage do you get per Magic level in Magic?" )
                        .translation( "pmmo.damageBonusPercentPerLevelMagic" )
                        .defineInRange( "damageBonusPercentPerLevelMagic", 0.005D, 0.001, 1000 ) );

                this.maxExtraDamagePercentageBoostMagic = subscriber.subscribe(builder
                        .comment( "How much extra damage can you get from the Magic skill max?" )
                        .translation( "pmmo.maxExtraDamagePercentageBoostMagic" )
                        .defineInRange( "maxExtraDamagePercentageBoostMagic", 100D, 0, 1000000000 ) );

                builder.pop();
            }

            builder.push( "Gunslinging" );
            {
                this.damageBonusPercentPerLevelGunslinging = subscriber.subscribe(builder
                        .comment( "How much percentage bonus damage do you get per Gunslinging level in Gunslinging?" )
                        .translation( "pmmo.damageBonusPercentPerLevelGunslinging" )
                        .defineInRange( "damageBonusPercentPerLevelGunslinging", 0.005D, 0.001, 1000 ) );

                builder.pop();
            }

            builder.push( "Slayer" );
            {
                this.aggresiveMobSlayerXp = subscriber.subscribe(builder
                        .comment( "How much slayer xp is awarded upon killing an aggresive mob by default" )
                        .translation( "pmmo.aggresiveMobSlayerXp" )
                        .defineInRange( "aggresiveMobSlayerXp", 0D, 0, 10000 ) );

                builder.pop();
            }

            builder.push( "Hunter" );
            {
                this.passiveMobHunterXp = subscriber.subscribe(builder
                        .comment( "How much hunter xp is awarded upon killing a passive mob by default" )
                        .translation( "pmmo.passiveMobHunterXp" )
                        .defineInRange( "passiveMobHunterXp", 0D, 0, 10000 ) );

                builder.pop();
            }

            builder.push( "Taming" );
            {
                this.tamingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for taming animals?" )
                        .translation( "pmmo.tamingXpEnabled" )
                        .define( "tamingXpEnabled", true ) );

                this.growingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for growing Plants? (Different from Harvest xp)" )
                        .translation( "pmmo.growingXpEnabledXpEnabled" )
                        .define( "growingXpEnabledXpEnabled", true ) );

                this.defaultTamingXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Taming for Taming an animal? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultTamingXp" )
                        .defineInRange( "defaultTamingXp", 0D, 0, 1000000 ) );

                this.defaultTamingXpFarming = subscriber.subscribe(builder
                        .comment( "Should default Taming Xp go to Farming instead of Taming?" )
                        .translation( "pmmo.defaultTamingXpFarming" )
                        .define( "defaultTamingXpFarming", false ) );

                builder.pop();
            }

            builder.push( "Easter Eggs" );
            {
                this.jesusXp = subscriber.subscribe(builder
                        .comment( "How much xp do you get for impersonating Jesus?" )
                        .translation( "pmmo.jesusXp" )
                        .defineInRange( "jesusXp", 0.075D, 0, 1000000 ) );

                builder.pop();
            }

            builder.push( "Auto Values" );
            {
                this.autoGenerateValuesEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for un-assigned items? (May be inaccurate)" )
                        .translation( "pmmo.autoGenerateValuesEnabled" )
                        .define( "autoGenerateValuesEnabled", true ) );

                this.autoGenerateRoundedValuesOnly = subscriber.subscribe(builder
                        .comment( "If this is off, Decimal point level requirements will be assigned during Auto Value generation" )
                        .translation( "pmmo.autoGenerateRoundedValuesOnly" )
                        .define( "autoGenerateRoundedValuesOnly", true ) );

                this.autoGenerateExtraChanceEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Extra Chance? (Works for Ores/Logs/Plants)" )
                        .translation( "pmmo.autoGenerateExtraChanceEnabled" )
                        .define( "autoGenerateExtraChanceEnabled", true ) );

                this.defaultExtraChanceOre = subscriber.subscribe(builder
                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Ores" )
                        .translation( "pmmo.defaultExtraChanceOre" )
                        .defineInRange( "defaultExtraChanceOre", 1D, 0D, 1000000D ) );

                this.defaultExtraChanceLog = subscriber.subscribe(builder
                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Logs" )
                        .translation( "pmmo.defaultExtraChanceLog" )
                        .defineInRange( "defaultExtraChanceLog", 2D, 0D, 1000000D ) );

                this.defaultExtraChancePlant = subscriber.subscribe(builder
                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Plants" )
                        .translation( "pmmo.defaultExtraChancePlant" )
                        .defineInRange( "defaultExtraChancePlant", 1.5D, 0D, 1000000D ) );

//                this.defaultExtraChanceSmelt = subscriber.subscribe(builder
//                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Smeltables" )
//                        .translation( "pmmo.defaultExtraChanceSmelt" )
//                        .defineInRange( "defaultExtraChanceSmelt", 1.0D, 0D, 1000000D ) );
//
//                this.defaultExtraChanceCook = subscriber.subscribe(builder
//                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Cookables" )
//                        .translation( "pmmo.defaultExtraChanceCook" )
//                        .defineInRange( "defaultExtraChanceCook", 1.5D, 0D, 1000000D ) );
//
//                this.defaultExtraChanceBrew = subscriber.subscribe(builder
//                        .comment( "Valued used by autoGenerateExtraChanceEnabled, for Brewing Ingredients" )
//                        .translation( "pmmo.defaultExtraChanceBrewables" )
//                        .defineInRange( "defaultExtraChanceBrewables", 1.5D, 0D, 1000000D ) );

                this.autoGenerateWearReqEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Wear Requirement?" )
                        .translation( "pmmo.autoGenerateWearReqEnabled" )
                        .define( "autoGenerateWearReqEnabled", true ) );

                this.autoGenerateWearReqDynamicallyEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Wear Requirement Dynamically? (This enables Live Scaling, for mods like Tinker's, or Draconic Evolution)" )
                        .translation( "pmmo.autoGenerateWearReqDynamicallyEnabled" )
                        .define( "autoGenerateWearReqDynamicallyEnabled", true ) );

                this.autoGenerateWearReqOffset = subscriber.subscribe(builder
                        .comment( "Level Offset for all auto-generated Wear Requirements" )
                        .translation( "pmmo.autoGenerateWearReqOffset" )
                        .defineInRange( "autoGenerateWearReqOffset", 0D, -1000000D, 1000000D ) );

                this.autoGenerateWearReqAsCombat = subscriber.subscribe(builder
                        .comment( "Should Automatically generated values for Wearing be Combat instead of Endurance? (True = Combat, False = Endurance)" )
                        .translation( "pmmo.autoGenerateWearReqAsCombat" )
                        .define( "autoGenerateWearReqAsCombat", false ) );

                this.autoGenerateWeaponReqEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Weapon Requirement?" )
                        .translation( "pmmo.autoGenerateWeaponReqEnabled" )
                        .define( "autoGenerateWeaponReqEnabled", true ) );

                this.autoGenerateWeaponReqDynamicallyEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Weapon Requirement Dynamically? (This enables Live Scaling, for mods like Tinker's, or Draconic Evolution)" )
                        .translation( "pmmo.autoGenerateWeaponReqDynamicallyEnabled" )
                        .define( "autoGenerateWeaponReqDynamicallyEnabled", true ) );

                this.autoGenerateWeaponReqOffset = subscriber.subscribe(builder
                        .comment( "Level Offset for all auto-generated Weapon Requirements" )
                        .translation( "pmmo.autoGenerateWeaponReqOffset" )
                        .defineInRange( "autoGenerateWeaponReqOffset", 0D, -1000000D, 1000000D ) );

                this.autoGenerateToolReqEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Tool Requirement?" )
                        .translation( "pmmo.autoGenerateToolReqEnabled" )
                        .define( "autoGenerateToolReqEnabled", true ) );

                this.autoGenerateToolReqDynamicallyEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Tool Requirement Dynamically? (This enables Live Scaling, for mods like Tinker's, or Draconic Evolution)" )
                        .translation( "pmmo.autoGenerateToolReqDynamicallyEnabled" )
                        .define( "autoGenerateToolReqDynamicallyEnabled", true ) );

                this.autoGenerateToolReqOffset = subscriber.subscribe(builder
                        .comment( "Level Offset for all auto-generated Tool Requirements" )
                        .translation( "pmmo.autoGenerateToolReqOffset" )
                        .defineInRange( "autoGenerateToolReqOffset", 0D, -1000000D, 1000000D ) );

                this.autoGenerateCraftingXpEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Crafting Experience? (Works for Armor/Tools/Weapons)" )
                        .translation( "pmmo.autoGenerateCraftingXpEnabled" )
                        .define( "autoGenerateCraftingXpEnabled", true ) );

                this.autoGenerateCookingXpEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Cooking Experience? (Works for Food)" )
                        .translation( "pmmo.autoGenerateCookingXpEnabled" )
                        .define( "autoGenerateCookingXpEnabled", true ) );

                this.autoGenerateCookingExtraChanceEnabled = subscriber.subscribe(builder
                        .comment( "Automatically assign values for Cooking Extra Chance? (Works for Food)" )
                        .translation( "pmmo.autoGenerateCookingExtraChanceEnabled" )
                        .define( "autoGenerateCookingExtraChanceEnabled", true ) );

//                this.autoGenerateKillXpAggresiveEnabled = subscriber.subscribe(builder
//                        .comment( "Automatically assign values for Aggresive Creatures?" )
//                        .translation( "pmmo.autoGenerateKillXpEnabled" )
//                        .define( "autoGenerateKillXpEnabled", true ) );

                this.autoGeneratedCraftingXpValueMultiplierCrafting = subscriber.subscribe(builder
                        .comment( "Multiplier for the Auto Generated Crafting Xp Value, in the Crafting skill" )
                        .translation( "pmmo.autoGeneratedCraftingXpValueMultiplierCrafting" )
                        .defineInRange( "autoGeneratedCraftingXpValueMultiplierCrafting", 1D, 0D, 1000000D ) );

                this.autoGeneratedCraftingXpValueMultiplierSmithing = subscriber.subscribe(builder
                        .comment( "Multiplier for the Auto Generated Crafting Xp Value, in the Smithing skill" )
                        .translation( "pmmo.autoGeneratedCraftingXpValueMultiplierSmithing" )
                        .defineInRange( "autoGeneratedCraftingXpValueMultiplierSmithing", 1D, 0D, 1000000D ) );

                this.autoGeneratedCraftingXpValueMultiplierCooking = subscriber.subscribe(builder
                        .comment( "Multiplier for the Auto Generated Crafting Xp Value, in the Cooking skill" )
                        .translation( "pmmo.autoGeneratedCraftingXpValueMultiplierCooking" )
                        .defineInRange( "autoGeneratedCraftingXpValueMultiplierCooking", 1D, 0D, 1000000D ) );

//                this.autoGeneratedKillXpValueAggresiveMultiplierSlayer = subscriber.subscribe(builder
//                        .comment( "Multiplier for the Auto Generated Kill Xp Value for Aggresive Creatures, in the Slayer skill" )
//                        .translation( "pmmo.autoGeneratedKillXpValueAggresiveMultiplierSlayer" )
//                        .defineInRange( "autoGeneratedKillXpValueAggresiveMultiplierSlayer", 1D, 0D, 1000000D ) );

                this.armorReqScale = subscriber.subscribe(builder
                        .comment( "How much the Armor value scales the Endurance Requirement for Armor" )
                        .translation( "pmmo.armorReqScale" )
                        .defineInRange( "armorReqScale", 4D, 0D, 1000000D ) );

                this.armorToughnessReqScale = subscriber.subscribe(builder
                        .comment( "How much the Armor Toughness value scales the Endurance Requirement for Armor" )
                        .translation( "pmmo.armorToughnessReqScale" )
                        .defineInRange( "armorToughnessReqScale", 6D, 0D, 1000000D ) );

                this.attackDamageReqScale = subscriber.subscribe(builder
                        .comment( "How much the Attack Damage values scales the Combat Requirement for Weapons" )
                        .translation( "pmmo.attackDamageReqScale" )
                        .defineInRange( "attackDamageReqScale", 4D, 0D, 1000000D ) );

                this.toolReqScaleOre = subscriber.subscribe(builder
                        .comment( "How much the Speed of the tool scales the Requirement of Mining to Use the tool" )
                        .translation( "pmmo.toolReqScaleOre" )
                        .defineInRange( "toolReqScaleOre", 5D, 0D, 1000000D ) );

                this.toolReqScaleLog = subscriber.subscribe(builder
                        .comment( "How much the Speed of the tool scales the Requirement of Woodcutting to Use the tool" )
                        .translation( "pmmo.toolReqScaleLog" )
                        .defineInRange( "toolReqScaleLog", 5D, 0D, 1000000D ) );

                this.toolReqScaleDirt = subscriber.subscribe(builder
                        .comment( "How much the Speed of the tool scales the Requirement of Excavation to Use the tool" )
                        .translation( "pmmo.toolReqScaleDirt" )
                        .defineInRange( "toolReqScaleDirt", 5D, 0D, 1000000D ) );

                this.outputAllAutoValuesToLog = subscriber.subscribe(builder
                        .comment( "Spam the log with every Auto Value generation?" )
                        .translation( "pmmo.outputAllAutoValuesToLog" )
                        .define( "outputAllAutoValuesToLog", false ) );

                builder.pop();
            }
        }
    }

    public static double getConfig( String key )
    {
        if( Config.config.containsKey( key ) )
            return Config.config.get( key );
        else if( Config.localConfig.containsKey( key ) )
            return Config.localConfig.get( key );
        else
        {
            if( ProjectMMOMod.serverStarted || !ProjectMMOMod.jeiLoaded )
                LOGGER.error( "UNABLE TO READ PMMO CONFIG \"" + key + "\" PLEASE REPORT" );
            return -1;
        }
    }

    public static double getConfigOrDefault( String key, double defaultValue )
    {
        if( Config.config.containsKey( key ) )
            return Config.config.get( key );
        else
        {
            if( ProjectMMOMod.serverStarted || !ProjectMMOMod.jeiLoaded )
                LOGGER.error( "UNABLE TO READ PMMO CONFIG \"" + key + "\" PLEASE REPORT" );
            return defaultValue;
        }
    }

    public static Map<String, Double> getXpMap( Player player )
    {
        if( player.level.isClientSide() )
            return XP.getOfflineXpMap( player.getUUID() );
        else
            return getXpMap( player.getUUID() );
    }

    public static Map<String, Double> getXpMap( UUID uuid )
    {
        return PmmoSavedData.get().getXpMap( uuid );
    }

    public static Map<String, Double> getConfigMap()
    {
        return config;
    }

    public static void setConfigMap( Map<String, Double> inMap )
    {
        config = inMap;
    }

    public static Map<String, Double> getPreferencesMap( Player player )
    {
        if( player.level.isClientSide() )
            return preferences;
        else
            return PmmoSavedData.get().getPreferencesMap( player.getUUID() );
    }

    public static Map<String, Double> getPreferencesMapOffline()
    {
        return preferences;
    }

    public static Map<String, Double> getAbilitiesMap( Player player )
    {
        if( player.level.isClientSide() )
            return abilities;
        else
            return PmmoSavedData.get().getAbilitiesMap( player.getUUID() );
    }

    public static void setPreferencesMap( Map<String, Double> newPreferencesMap )
    {
        preferences = newPreferencesMap;
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * Gets all xp boost maps
     */
    @Deprecated
    public static Map<String, Map<String, Double>> getXpBoostsMap( Player player )
    {
        return APIUtils.getXpBoostsMap( player );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * Gets a specific xp boost map
     */
    @Deprecated
    public static Map<String, Double> getXpBoostMap( Player player, String xpBoostKey )
    {
        return APIUtils.getXpBoostMap( player, xpBoostKey );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * Gets a specific xp boost in a specific skill
     */
    @Deprecated
    public static double getPlayerXpBoost( Player player, String skill )
    {
        return APIUtils.getPlayerXpBoost( player, skill );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * Sets a specific xp boost map
     */
    @Deprecated
    public static void setPlayerXpBoost( ServerPlayer player, String xpBoostKey, Map<String, Double> newXpBoosts )
    {
        APIUtils.setPlayerXpBoost( player, xpBoostKey, newXpBoosts );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * Removes a specific xp boost map
     */
    @Deprecated
    public static void removePlayerXpBoost( ServerPlayer player, String xpBoostKey )
    {
        APIUtils.removePlayerXpBoost( player, xpBoostKey );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * WARNING: Removes ALL Xp Boosts, INCLUDING ONES CAUSED BY OTHER MODS
     */
    @Deprecated
    public static void removeAllPlayerXpBoosts( ServerPlayer player )
    {
        APIUtils.removeAllPlayerXpBoosts( player );
    }

    /**
     * DEPRECATED - use APIUtils. instead!
     * SERVER ONLY, THE ONLY TIME CLIENT IS CALLED WHEN A PACKET IS RECEIVED >FROM SERVER<
     * Only Project MMO should use this.
     */
    @Deprecated
    public static void setPlayerXpBoostsMaps( Player player, Map<String, Map<String, Double>> newBoosts )
    {
        APIUtils.setPlayerXpBoostsMaps( player, newBoosts );
    }
}