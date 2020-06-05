package harmonised.pmmo.config;

import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashMap;
import java.util.Map;

public class Config
{
    public static Map<String, Double> localConfig = new HashMap<>();
    public static Map<String, Double> config = new HashMap<>();

    public static ConfigImplementation forgeConfig;

    public static void init()
    {
        forgeConfig = ConfigHelper.register( ModConfig.Type.COMMON, ConfigImplementation::new );
    }

    public static void initServer()
    {
        localConfig.put( "baseXp", (double) forgeConfig.baseXp.get() );
        localConfig.put( "xpIncreasePerLevel", (double) forgeConfig.xpIncreasePerLevel.get() );
        localConfig.put( "maxLevel", (double) forgeConfig.maxLevel.get() );
        localConfig.put( "maxXp", XP.xpAtLevel( forgeConfig.maxLevel.get() ) );
        localConfig.put( "biomePenaltyMultiplier", forgeConfig.biomePenaltyMultiplier.get() );
        localConfig.put( "nightvisionUnlockLevel", (double) forgeConfig.nightvisionUnlockLevel.get() );
        localConfig.put( "speedBoostPerLevel", forgeConfig.speedBoostPerLevel.get() );
        localConfig.put( "maxSpeedBoost", forgeConfig.maxSpeedBoost.get() );
        localConfig.put( "maxJumpBoost", forgeConfig.maxJumpBoost.get() );
        localConfig.put( "levelsCrouchJumpBoost", (double) forgeConfig.levelsCrouchJumpBoost.get() );
        localConfig.put( "levelsSprintJumpBoost", (double) forgeConfig.levelsSprintJumpBoost.get() );
        localConfig.put( "levelsPerHardnessMining", forgeConfig.levelsPerHardnessMining.get() );
        localConfig.put( "levelsPerHardnessWoodcutting", forgeConfig.levelsPerHardnessWoodcutting.get() );
        localConfig.put( "levelsPerHardnessExcavation", forgeConfig.levelsPerHardnessExcavation.get() );
        localConfig.put( "levelsPerHardnessFarming", forgeConfig.levelsPerHardnessFarming.get() );
        localConfig.put( "levelsPerHardnessCrafting", forgeConfig.levelsPerHardnessCrafting.get() );
        localConfig.put( "minVeinCost", forgeConfig.minVeinCost.get() );
        localConfig.put( "minVeinHardness", forgeConfig.minVeinHardness.get() );
        localConfig.put( "maxVeinCharge", forgeConfig.maxVeinCharge.get() );

        if( Config.forgeConfig.crawlingAllowed.get() )
            localConfig.put( "crawlingAllowed", 1D );
        else
            localConfig.put( "crawlingAllowed", 0D );

        if( Config.forgeConfig.veiningAllowed.get() )
            localConfig.put( "veiningAllowed", 1D );
        else
            localConfig.put( "veiningAllowed", 0D );

        config = localConfig;
    }

    public static class ConfigImplementation
    {
        //Miscellaneous
        public ConfigHelper.ConfigValueListener<Boolean> crawlingAllowed;
        public ConfigHelper.ConfigValueListener<Boolean> showWelcome;
        public ConfigHelper.ConfigValueListener<Boolean> showDonatorWelcome;
        
        //Vein Mining
        public ConfigHelper.ConfigValueListener<Boolean> veiningAllowed;
        public ConfigHelper.ConfigValueListener<Boolean> veinWoodTopToBottom;
        public ConfigHelper.ConfigValueListener<Boolean> sleepRechargesWorldPlayersVeinCharge;
        public ConfigHelper.ConfigValueListener<Double> veinMaxDistance;
        public ConfigHelper.ConfigValueListener<Double> veinMaxBlocks;
        public ConfigHelper.ConfigValueListener<Double> veinSpeed;
        public ConfigHelper.ConfigValueListener<Double> minVeinCost;
        public ConfigHelper.ConfigValueListener<Double> minVeinHardness;
        public ConfigHelper.ConfigValueListener<Double> maxVeinCharge;
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
        public ConfigHelper.ConfigValueListener<Boolean> toolReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> weaponReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> killReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> killXpEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> mobRareDropEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> useReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> placeReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> breakReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> biomeReqEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> biomeXpBonusEnabled;
        public ConfigHelper.ConfigValueListener<Boolean> xpValueEnabled;
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

        //Levels
        public ConfigHelper.ConfigValueListener<Integer> maxLevel;
        public ConfigHelper.ConfigValueListener<Integer> baseXp;
        public ConfigHelper.ConfigValueListener<Integer> xpIncreasePerLevel;
        public ConfigHelper.ConfigValueListener<Integer> levelsPerMilestone;
        public ConfigHelper.ConfigValueListener<Boolean> wipeAllSkillsUponDeathPermanently;
        public ConfigHelper.ConfigValueListener<Boolean> broadcastMilestone;
        public ConfigHelper.ConfigValueListener<Boolean> levelUpFirework;
        public ConfigHelper.ConfigValueListener<Boolean> milestoneLevelUpFirework;

        //Multipliers
        public ConfigHelper.ConfigValueListener<Double> globalMultiplier;
        public ConfigHelper.ConfigValueListener<Double> peacefulMultiplier;
        public ConfigHelper.ConfigValueListener<Double> easyMultiplier;
        public ConfigHelper.ConfigValueListener<Double> normalMultiplier;
        public ConfigHelper.ConfigValueListener<Double> hardMultiplier;
        public ConfigHelper.ConfigValueListener<Double> miningMultiplier;
        public ConfigHelper.ConfigValueListener<Double> buildingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> excavationMultiplier;
        public ConfigHelper.ConfigValueListener<Double> woodcuttingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> farmingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> agilityMultiplier;
        public ConfigHelper.ConfigValueListener<Double> enduranceMultiplier;
        public ConfigHelper.ConfigValueListener<Double> combatMultiplier;
        public ConfigHelper.ConfigValueListener<Double> archeryMultiplier;
        public ConfigHelper.ConfigValueListener<Double> repairingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> flyingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> swimmingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> fishingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> craftingMultiplier;
        public ConfigHelper.ConfigValueListener<Double> biomePenaltyMultiplier;
        public ConfigHelper.ConfigValueListener<Double> deathXpPenaltyMultiplier;

        //GUI
        public ConfigHelper.ConfigValueListener<Double> barOffsetX;
        public ConfigHelper.ConfigValueListener<Double> barOffsetY;
        public ConfigHelper.ConfigValueListener<Double> veinBarOffsetX;
        public ConfigHelper.ConfigValueListener<Double> veinBarOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetX;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropSpawnDistance;
        public ConfigHelper.ConfigValueListener<Double> xpDropOpacityPerTime;
        public ConfigHelper.ConfigValueListener<Double> xpDropMaxOpacity;
        public ConfigHelper.ConfigValueListener<Double> xpDropDecayAge;
        public ConfigHelper.ConfigValueListener<Double> minXpGrow;
        public ConfigHelper.ConfigValueListener<Boolean> showXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> stackXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> xpDropsAttachedToBar;
        public ConfigHelper.ConfigValueListener<Boolean> xpBarAlwaysOn;
        public ConfigHelper.ConfigValueListener<Boolean> xpLeftDisplayAlwaysOn;
        public ConfigHelper.ConfigValueListener<Boolean> lvlUpScreenshot;
        public ConfigHelper.ConfigValueListener<Boolean> lvlUpScreenshotShowSkills;

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
        public ConfigHelper.ConfigValueListener<Integer> levelsPerHardnessReach;
        public ConfigHelper.ConfigValueListener<Double> maxReach;
        public ConfigHelper.ConfigValueListener<Double> blockHardnessLimitForPlacing;

        //Excavation

        //Woodcutting

        //Farming
        public ConfigHelper.ConfigValueListener<Boolean> tamingXpEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultBreedingXp;

        //Agility
        public ConfigHelper.ConfigValueListener<Double> maxFallSaveChance;
        public ConfigHelper.ConfigValueListener<Double> saveChancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxJumpBoost;
        public ConfigHelper.ConfigValueListener<Double> maxSpeedBoost;
        public ConfigHelper.ConfigValueListener<Double> speedBoostPerLevel;
        public ConfigHelper.ConfigValueListener<Integer> levelsCrouchJumpBoost;
        public ConfigHelper.ConfigValueListener<Integer> levelsSprintJumpBoost;

        //Endurance
        public ConfigHelper.ConfigValueListener<Double> endurancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxEndurance;
        public ConfigHelper.ConfigValueListener<Integer> levelsPerHeart;
        public ConfigHelper.ConfigValueListener<Integer> maxHeartCap;

        //Combat
        public ConfigHelper.ConfigValueListener<Integer> levelsPerDamage;
        public ConfigHelper.ConfigValueListener<Integer> maxDamage;

        //Archery

        //Repairing
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

        //Flying

        //Swimming
        public ConfigHelper.ConfigValueListener<Integer> nightvisionUnlockLevel;

        //Fishing
        public ConfigHelper.ConfigValueListener<Double> fishPoolBaseChance;
        public ConfigHelper.ConfigValueListener<Double> fishPoolChancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> fishPoolMaxChance;

        //Crafting
        public ConfigHelper.ConfigValueListener<Boolean> xpValueCraftingEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultCraftingXp;

        //Magic

        //Slayer
        public ConfigHelper.ConfigValueListener<Double> passiveMobHunterXp;
        public ConfigHelper.ConfigValueListener<Double> aggresiveMobSlayerXp;

        //Taming
        public ConfigHelper.ConfigValueListener<Boolean> breedingXpEnabled;
        public ConfigHelper.ConfigValueListener<Double> defaultTamingXp;

        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push( "Miscellaneous" );
            {
                this.crawlingAllowed = subscriber.subscribe(builder
                        .comment( "Is crawling allowed? true = on, false = off" )
                        .translation( "pmmo.crawlingAllowed" )
                        .define( "crawlingAllowed", true ) );

                this.showWelcome = subscriber.subscribe(builder
                        .comment( "Should the Welcome message come up?" )
                        .translation( "pmmo.showWelcome" )
                        .define( "showWelcome", true ) );

                this.showDonatorWelcome = subscriber.subscribe(builder
                        .comment( "Should your personal Donator Welcome message come up?" )
                        .translation( "pmmo.showDonatorWelcome" )
                        .define( "showDonatorWelcome", true ) );
                
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

                this.sleepRechargesWorldPlayersVeinCharge = subscriber.subscribe(builder
                        .comment( "Should a succesful sleep recharge every player currently in that world Vein Charge?" )
                        .translation( "pmmo.sleepRechargesWorldPlayersVeinCharge" )
                        .define( "sleepRechargesWorldPlayersVeinCharge", true ) );
                
                this.veinMaxDistance = subscriber.subscribe(builder
                        .comment( "What is the maximum distance a player's vein can reach?" )
                        .translation( "pmmo.veinMaxDistance" )
                        .defineInRange( "veinMaxDistance", 1000D, 1, 1000) );

                this.veinMaxBlocks = subscriber.subscribe(builder
                        .comment( "How many blocks max can be veined?" )
                        .translation( "pmmo.veinMaxBlocks" )
                        .defineInRange( "veinMaxBlocks", 10000D, 1, 1000000) );

                this.veinSpeed = subscriber.subscribe(builder
                        .comment( "How many blocks get broken every tick?" )
                        .translation( "pmmo.veinSpeed" )
                        .defineInRange( "veinSpeed", 1D, 1, 10000) );

                this.minVeinCost = subscriber.subscribe(builder
                        .comment( "How much is the lowest cost for each block veined? (1 = 1 charge, 1 charge regens per second)" )
                        .translation( "pmmo.minVeinCost" )
                        .defineInRange( "minVeinCost", 2D, 0.01, 10000) );

                this.minVeinHardness = subscriber.subscribe(builder
                        .comment( "What is the lowest hardness for each block veined? (Crops have 0 hardness, this makes crops not infinitely veined)" )
                        .translation( "pmmo.minVeinHardness" )
                        .defineInRange( "minVeinHardness", 0.2D, 0, 10000) );

                this.levelsPerHardnessMining = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160) * 320 = 100 hardness worth of blocks, which is 2.0 Obsidian, or 33.3 Coal Ore)" )
                        .translation( "pmmo.levelsPerHardnessMining" )
                        .defineInRange( "levelsPerHardnessMining", 160D, 0.01, 10000) );

                this.levelsPerHardnessWoodcutting = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 32, your level is 50, and you have 64 charge, you can vein (50 / 160) * 320 = 100 hardness worth of logs, which is 50 Logs)" )
                        .translation( "pmmo.levelsPerHardnessWoodcutting" )
                        .defineInRange( "levelsPerHardnessWoodcutting", 160D, 0.01, 10000) );

                this.levelsPerHardnessExcavation = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 16, your level is 50, and you have 64 charge, you can vein (50 / 80) * 320 = 50 hardness worth of ground, which is 100 Dirt)" )
                        .translation( "pmmo.levelsPerHardnessExcavation" )
                        .defineInRange( "levelsPerHardnessExcavation", 80D, 0.01, 10000) );

                this.levelsPerHardnessFarming = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? Plants have no hardness, but there is a minimum hardness while veining config in here, which is 0.5 by default, making it 50 plants at level 50 farming, with 320 charge, if this is set to 160" )
                        .translation( "pmmo.levelsPerHardnessFarming" )
                        .defineInRange( "levelsPerHardnessFarming", 160D, 0.1, 10000) );

                this.levelsPerHardnessCrafting = subscriber.subscribe(builder
                        .comment( "Every how many levels does 1 charge become worth +1 hardness? (If this is set to 80, your level is 50, and you have 320 charge, you can vein (50 / 80) * 320 = 200 hardness worth of Crafting Related (Such as wool, carpet, bed) blocks, which depends on how hard they are)" )
                        .translation( "pmmo.levelsPerHardnessCrafting" )
                        .defineInRange( "levelsPerHardnessCrafting", 160D, 0.1, 10000) );

                this.maxVeinCharge = subscriber.subscribe(builder
                        .comment( "How much vein charge can a player hold at max? (1 recharges every second)" )
                        .translation( "pmmo.maxVeinCharge" )
                        .defineInRange( "maxVeinCharge", 320D, 0, 100000) );

                builder.pop();
            }

            builder.push( "Mob Scaling" );
            {
                this.maxMobDamageBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's damage will be boosted?" )
                        .translation( "pmmo.maxMobDamageBoost" )
                        .defineInRange( "maxMobDamageBoost", 100D, 0, 1000) );

                this.mobDamageBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's damage will increase per one Power Level?" )
                        .translation( "pmmo.mobDamageBoostPerPowerLevel" )
                        .defineInRange( "mobDamageBoostPerPowerLevel", 1D, 0, 10) );

                this.maxMobHPBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's HP will be boosted?" )
                        .translation( "pmmo.maxMobHPBoost" )
                        .defineInRange( "maxMobHPBoost", 1000D, 0, 1024) );

                this.mobHPBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's HP will increase per one Power Level?" )
                        .translation( "pmmo.mobHPBoostPerPowerLevel" )
                        .defineInRange( "mobHPBoostPerPowerLevel", 5D, 0, 100) );

                this.maxMobSpeedBoost = subscriber.subscribe(builder
                        .comment( "What is the maximum amount an aggressive mob's speed will be boosted?" )
                        .translation( "pmmo.maxMobSpeedBoost" )
                        .defineInRange( "maxMobSpeedBoost", 10D, 0, 100) );

                this.mobSpeedBoostPerPowerLevel = subscriber.subscribe(builder
                        .comment( "How much an aggresive mob's speed will increase per one Power Level?" )
                        .translation( "pmmo.mobSpeedBoostPerPowerLevel" )
                        .defineInRange( "mobSpeedBoostPerPowerLevel", 1D, 0, 10) );

                this.biomeMobMultiplierEnabled = subscriber.subscribe(builder
                        .comment( "Should mob xp multipliers inside of biomes be enabled? False means no multipliers" )
                        .translation( "pmmo.biomeMobMultiplierEnabled" )
                        .define( "biomeMobMultiplierEnabled", true ) );


                builder.pop();
            }

            builder.push( "Requirements" );
            {
                this.wearReqEnabled = subscriber.subscribe(builder
                        .comment( "Should wear requirements be enabled? False means no requirements" )
                        .translation( "pmmo.wearReqEnabled" )
                        .define( "wearReqEnabled", true ) );

                this.toolReqEnabled = subscriber.subscribe(builder
                        .comment( "Should tool requirements be enabled? False means no requirements" )
                        .translation( "pmmo.toolReqEnabled" )
                        .define( "toolReqEnabled", true ) );

                this.weaponReqEnabled = subscriber.subscribe(builder
                        .comment( "Should weapon requirements be enabled? False means no requirements" )
                        .translation( "pmmo.weaponReqEnabled" )
                        .define( "weaponReqEnabled", true ) );

                this.killReqEnabled = subscriber.subscribe(builder
                        .comment( "Should mob kill req be enabled? False means no requirements" )
                        .translation( "pmmo.killReqEnabled" )
                        .define( "killReqEnabled", true ) );

                this.killXpEnabled = subscriber.subscribe(builder
                        .comment( "Should mob kill xp be enabled? False means no requirements" )
                        .translation( "pmmo.killXpEnabled" )
                        .define( "killXpEnabled", true ) );

                this.mobRareDropEnabled = subscriber.subscribe(builder
                        .comment( "Should mob rare drops be enabled? False means no requirements" )
                        .translation( "pmmo.mobRareDropEnabled" )
                        .define( "mobRareDropEnabled", true ) );


                this.useReqEnabled = subscriber.subscribe(builder
                        .comment( "Should use requirements be enabled? False means no requirements" )
                        .translation( "pmmo.useReqEnabled" )
                        .define( "useReqEnabled", true ) );

                this.placeReqEnabled = subscriber.subscribe(builder
                        .comment( "Should place requirements be enabled? False means no requirements" )
                        .translation( "pmmo.placeReqEnabled" )
                        .define( "placeReqEnabled", true ) );

                this.breakReqEnabled = subscriber.subscribe(builder
                        .comment( "Should break requirements be enabled? False means no requirements" )
                        .translation( "pmmo.breakReqEnabled" )
                        .define( "breakReqEnabled", true ) );

                this.biomeReqEnabled = subscriber.subscribe(builder
                        .comment( "Should biome requirements be enabled? False means no requirements" )
                        .translation( "pmmo.biomeReqEnabled" )
                        .define( "biomeReqEnabled", true ) );

                this.biomeXpBonusEnabled = subscriber.subscribe(builder
                        .comment( "Should xp multipliers be enabled? False means no multipliers" )
                        .translation( "pmmo.biomeXpBonusEnabled" )
                        .define( "biomeXpBonusEnabled", true ) );

                this.xpValueEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for breaking things first time be enabled? False means only Hardness xp is awarded for breaking" )
                        .translation( "pmmo.xpValueEnabled" )
                        .define( "xpValueEnabled", true ) );

                this.oreEnabled = subscriber.subscribe(builder
                        .comment( "Should ores be enabled? False means no extra chance" )
                        .translation( "pmmo.oreEnabled" )
                        .define( "oreEnabled", true ) );

                this.logEnabled = subscriber.subscribe(builder
                        .comment( "Should logs be enabled? False means no extra chance" )
                        .translation( "pmmo.logEnabled" )
                        .define( "logEnabled", true ) );

                this.plantEnabled = subscriber.subscribe(builder
                        .comment( "Should plants be enabled? False means no extra chance" )
                        .translation( "pmmo.plantEnabled" )
                        .define( "plantEnabled", true ) );

                this.salvageEnabled = subscriber.subscribe(builder
                        .comment( "Is Salvaging items using the Repairing skill enabled? False = off" )
                        .translation( "pmmo.salvageEnabled" )
                        .define( "salvageEnabled", true ) );

                this.fishPoolEnabled = subscriber.subscribe(builder
                        .comment( "Is catching items from Fish Pool while Fishing enabled? False = off" )
                        .translation( "pmmo.fishPoolEnabled" )
                        .define( "fishPoolEnabled", true ) );

                this.fishEnchantPoolEnabled = subscriber.subscribe(builder
                        .comment( "Should fished items have a chance at being Enchanted? enabled? False = off" )
                        .translation( "pmmo.fishEnchantPoolEnabled" )
                        .define( "fishEnchantPoolEnabled", true ) );

                this.levelUpCommandEnabled = subscriber.subscribe(builder
                        .comment( "Commands being fired on specific level ups enabled? False = off" )
                        .translation( "pmmo.levelUpCommandEnabled" )
                        .define( "levelUpCommandEnabled", true ) );

                this.heldItemXpBoostEnabled = subscriber.subscribe(builder
                        .comment( "Main held items xp multiplier enabled? False = off" )
                        .translation( "pmmo.heldItemXpBoostEnabled" )
                        .define( "heldItemXpBoostEnabled", true ) );

                this.wornItemXpBoostEnabled = subscriber.subscribe(builder
                        .comment( "worn items xp boost enabled? False = off" )
                        .translation( "pmmo.wornItemXpBoostEnabled" )
                        .define( "wornItemXpBoostEnabled", true ) );

                this.loadDefaultConfig = subscriber.subscribe(builder
                        .comment( "Should config from default_data.json be loaded? False means only data.json is loaded" )
                        .translation( "pmmo.loadDefaultConfig" )
                        .define( "loadDefaultConfig", true ) );

                builder.pop();
            }

            builder.push( "Levels" );
            {
                this.maxLevel = subscriber.subscribe(builder
                        .comment( "What is the global max level" )
                        .translation( "pmmo.maxLevel" )
                        .defineInRange( "maxLevel", 999, 1, 1000000) );

                this.baseXp = subscriber.subscribe(builder
                        .comment( "What is the baseXp to reach level 2 ( baseXp + level * xpPerLevel )" )
                        .translation( "pmmo.baseXp" )
                        .defineInRange( "baseXp", 250, 1, 1000000 ) );

                this.xpIncreasePerLevel = subscriber.subscribe(builder
                        .comment( "What is the xp increase per level ( baseXp + level * xpPerLevel )" )
                        .translation( "pmmo.xpIncreasePerLevel" )
                        .defineInRange( "xpIncreasePerLevel", 50, 1, 1000000 ) );

                this.levelsPerMilestone = subscriber.subscribe(builder
                        .comment( "Every how many levels should a level up broadcast be sent to all players? (10 = every 10 levels)" )
                        .translation( "pmmo.levelsPerMilestone" )
                        .defineInRange( "levelsPerMilestone", 10, 1, 1000000 ) );

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

                builder.pop();
            }

            builder.push( "Multipliers" );
            {
                this.globalMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.globalMultiplier" )
                        .defineInRange( "globalMultiplier", 1D, 0, 1000) );

                this.peacefulMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Peaceful Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.peacefulMultiplier" )
                        .defineInRange( "peacefulMultiplier", 1/3D, 0, 1000) );

                this.easyMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Easy Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.easyMultiplier" )
                        .defineInRange( "easyMultiplier", 2/3D, 0, 1000) );

                this.normalMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Normal Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.normalMultiplier" )
                        .defineInRange( "normalMultiplier", 1D, 0, 1000) );

                this.hardMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains on Hard Difficulty (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.hardMultiplier" )
                        .defineInRange( "hardMultiplier", 4/3D, 0, 1000) );

                this.miningMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Mining (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.miningMultiplier" )
                        .defineInRange( "miningMultiplier", 1D, 0, 1000) );

                this.buildingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Building (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.buildingMultiplier" )
                        .defineInRange( "buildingMultiplier", 1D, 0, 1000) );

                this.excavationMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Excavation (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.excavationMultiplier" )
                        .defineInRange( "excavationMultiplier", 1D, 0, 1000) );

                this.woodcuttingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Woodcutting (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.woodcuttingMultiplier" )
                        .defineInRange( "woodcuttingMultiplier", 1D, 0, 1000) );

                this.farmingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Farming (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.farmingMultiplier" )
                        .defineInRange( "farmingMultiplier", 1D, 0, 1000) );

                this.agilityMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Agility (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.agilityMultiplier" )
                        .defineInRange( "agilityMultiplier", 1D, 0, 1000) );

                this.enduranceMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Endurance (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.enduranceMultiplier" )
                        .defineInRange( "enduranceMultiplier", 1D, 0, 1000) );

                this.combatMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Combat (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.combatMultiplier" )
                        .defineInRange( "combatMultiplier", 1D, 0, 1000) );

                this.archeryMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Archery (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.archeryMultiplier" )
                        .defineInRange( "archeryMultiplier", 1D, 0, 1000) );

                this.repairingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Repairing (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.repairingMultiplier" )
                        .defineInRange( "repairingMultiplier", 1D, 0, 1000) );

                this.flyingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Flying (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.flyingMultiplier" )
                        .defineInRange( "flyingMultiplier", 1D, 0, 1000) );

                this.swimmingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Swimming (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.swimmingMultiplier" )
                        .defineInRange( "swimmingMultiplier", 1D, 0, 1000) );

                this.fishingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Fishing (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.fishingMultiplier" )
                        .defineInRange( "fishingMultiplier", 1D, 0, 1000) );

                this.craftingMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp everyone gains in Crafting (1 = normal, 2 = twice as much)" )
                        .translation( "pmmo.craftingMultiplier" )
                        .defineInRange( "craftingMultiplier", 1D, 0, 1000) );

                this.biomePenaltyMultiplier = subscriber.subscribe(builder
                        .comment( "How much xp you get in biomes you do not meet the requirements for (1 = Full xp, 0.5 = Half xp)" )
                        .translation( "pmmo.biomePenaltyMultiplier" )
                        .defineInRange( "biomePenaltyMultiplier", 0.5D, 0, 1) );

                this.deathXpPenaltyMultiplier = subscriber.subscribe(builder
                        .comment( "How much of the xp above whole level you loose (1 = 100% = from 5.5 to 5.0, 0.5 = 50% = from 5.5 to 5.25" )
                        .translation( "pmmo.deathXpPenaltyMultiplier" )
                        .defineInRange( "deathXpPenaltyMultiplier", 1D, 0, 1) );

                builder.pop();
            }

            builder.push( "GUI" );
            {
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

                this.xpDropSpawnDistance = subscriber.subscribe(builder
                        .comment( "How far away does the Xp Drop spawn" )
                        .translation( "pmmo.xpDropSpawnDistance" )
                        .defineInRange( "xpDropSpawnDistance", 50D, 0, 1000) );

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
                        .defineInRange( "xpDropDecayAge", 350D, 0, 5000) );

                this.minXpGrow = subscriber.subscribe(builder
                        .comment( "What is the minimum amount xp grows a set amount of time? (Default 0.2, increase to speed up growth)" )
                        .translation( "pmmo.minXpGrow" )
                        .defineInRange( "minXpGrow", 1D, 0.01D, 100D ) );

                this.xpDropsAttachedToBar = subscriber.subscribe(builder
                        .comment( "Should xp drops sync up with the bar being open or closed? HIGHLY RECOMMEND TO KEEP FALSE IF YOU ARE MOVING XP DROP POSITIONS" )
                        .translation( "pmmo.xpDropsAttachedToBar" )
                        .define( "xpDropsAttachedToBar", true ) );

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

                builder.pop();
            }

            builder.push( "Breaking Speed" );
            {
                this.minBreakSpeed = subscriber.subscribe(builder
                        .comment( "Minimum Breaking Speed (1 is Original speed, 0.5 is half)" )
                        .translation( "pmmo.minBreakSpeed" )
                        .defineInRange( "minBreakSpeed", 0.5, 0, 100) );

                this.blocksToUnbreakableY = subscriber.subscribe(builder
                        .comment( "How many blocks it takes to reach 0 Break Speed (will get capped by Minimum Breaking Speed)" )
                        .translation( "pmmo.blocksToUnbreakableY" )
                        .defineInRange( "blocksToUnbreakableY", 1000D, -Double.MAX_VALUE, Double.MAX_VALUE) );

                this.miningBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your mining speed increases per level (1 = 1% increase per level)" )
                        .translation( "pmmo.miningBonusSpeed" )
                        .defineInRange( "miningBonusSpeed", 1D, 0, 10) );

                this.woodcuttingBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your cutting speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.woodcuttingBonusSpeed" )
                        .defineInRange( "woodcuttingBonusSpeed", 1D, 0, 10) );

                this.excavationBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your digging speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.excavationBonusSpeed" )
                        .defineInRange( "excavationBonusSpeed", 1D, 0, 10) );

                this.farmingBonusSpeed = subscriber.subscribe(builder
                        .comment( "How much your farming speed increases per level in (1 = 1% increase per level)" )
                        .translation( "pmmo.farmingBonusSpeed" )
                        .defineInRange( "farmingBonusSpeed", 1D, 0, 10) );

                builder.pop();
            }

            builder.push( "Mining" );
            {
                this.blockHardnessLimitForBreaking = subscriber.subscribe(builder
                        .comment( "Hardest considered block (1 hardness = 1 remove xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
                        .translation( "pmmo.blockHardnessLimitForBreaking" )
                        .defineInRange( "blockHardnessLimitForBreaking", 250D, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Building" );
            {
                this.levelsPerHardnessReach = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of reach" )
                        .translation( "pmmo.levelsPerHardnessReach" )
                        .defineInRange( "levelsPerHardnessReach", 25, 0, 1000) );

                this.maxReach = subscriber.subscribe(builder
                        .comment( "What is the maximum reach a player can have" )
                        .translation( "pmmo.maxReach" )
                        .defineInRange( "maxReach", 20D, 0, 1000) );

                this.blockHardnessLimitForPlacing = subscriber.subscribe(builder
                        .comment( "Hardest considered block (1 hardness = 1 build xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
                        .translation( "pmmo.blockHardnessLimitForPlacing" )
                        .defineInRange( "blockHardnessLimitForPlacing", 250D, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Excavation" );
            {
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
                        .defineInRange( "defaultBreedingXp", 10.0D, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Agility" );
            {
                this.maxFallSaveChance = subscriber.subscribe(builder
                        .comment( "Maximum chance to save each point of fall damage (100 = no fall damage)" )
                        .translation( "pmmo.maxFallSaveChance" )
                        .defineInRange( "maxFallSaveChance", 64D, 0, 100) );

                this.saveChancePerLevel = subscriber.subscribe(builder
                        .comment( "How much your chance to save each point of fall damage increases per level (1 = 1% increase per Level)" )
                        .translation( "pmmo.saveChancePerLevel" )
                        .defineInRange( "saveChancePerLevel", 64D, 0, 100) );

                this.maxJumpBoost = subscriber.subscribe(builder
                        .comment( "How much jump boost can you gain max (above 0.33 makes you take fall damage)" )
                        .translation( "pmmo.maxJumpBoost" )
                        .defineInRange( "maxJumpBoost", 0.33D, 0, 100) );

                this.levelsCrouchJumpBoost = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of jumping height while Crouching" )
                        .translation( "pmmo.levelsCrouchJumpBoost" )
                        .defineInRange( "levelsCrouchJumpBoost", 33, 0, 1000) );

                this.levelsSprintJumpBoost = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of jumping height while Sprinting" )
                        .translation( "pmmo.levelsSprintJumpBoost" )
                        .defineInRange( "levelsSprintJumpBoost", 50, 0, 1000) );

                this.maxSpeedBoost = subscriber.subscribe(builder
                        .comment( "How much speed boost you can get from Agility (100 = 100% vanilla + 100% = twice as fast max)" )
                        .translation( "pmmo.maxSpeedBoost" )
                        .defineInRange( "maxSpeedBoost", 100D, 0, 1000) );

                this.speedBoostPerLevel = subscriber.subscribe(builder
                        .comment( "How much speed boost you get from each level (Incredibly sensitive, default 0.0005)" )
                        .translation( "pmmo.speedBoostPerLevel" )
                        .defineInRange( "speedBoostPerLevel", 0.0005D, 0, 10) );

                builder.pop();
            }

            builder.push( "Endurance" );
            {
                this.maxEndurance = subscriber.subscribe(builder
                        .comment( "How much endurance is max (100 = god mode)" )
                        .translation( "pmmo.maxEndurance" )
                        .defineInRange( "maxEndurance", 50D, 0, 100) );

                this.endurancePerLevel = subscriber.subscribe(builder
                        .comment( "How much endurance you gain per level (1 = 1% per level)" )
                        .translation( "pmmo.endurancePerLevel" )
                        .defineInRange( "endurancePerLevel", 0.25D, 0, 100) );

                this.levelsPerHeart = subscriber.subscribe(builder
                        .comment( "Per how many levels you gain 1 Max Heart" )
                        .translation( "pmmo.levelsPerHeart" )
                        .defineInRange( "levelsPerHeart", 10, 0, 1000) );

                this.maxHeartCap = subscriber.subscribe(builder
                        .comment( "How many Max Hearts you can have (20 means 10 vanilla + 20 boosted)" )
                        .translation( "pmmo.maxHeartCap" )
                        .defineInRange( "maxHeartCap", 100, 0, 1000) );

                builder.pop();
            }

            builder.push( "Combat" );
            {
                this.levelsPerDamage = subscriber.subscribe(builder
                        .comment( "Per how many levels you gain 1 Extra Damage" )
                        .translation( "pmmo.levelsPerDamage" )
                        .defineInRange( "levelsPerDamage", 20, 0, 1000) );

                this.maxDamage = subscriber.subscribe(builder
                        .comment( "How much extra damage can you get from the Combat skill max?" )
                        .translation( "pmmo.maxDamage" )
                        .defineInRange( "maxDamage", 10, 0, 1000) );

                builder.pop();
            }

            builder.push( "Archery" );
            {
                builder.pop();
            }
            builder.push( "Smithing" );
            {
                this.maxSalvageEnchantChance = subscriber.subscribe(builder
                        .comment( "Max Percentage chance to return each Enchantment Level" )
                        .translation( "pmmo.maxSalvageEnchantChance" )
                        .defineInRange( "maxSalvageEnchantChance", 90D, 0, 100) );

                this.enchantSaveChancePerLevel = subscriber.subscribe(builder
                        .comment( "Each Enchantment Save Chance per Level" )
                        .translation( "pmmo.enchantSaveChancePerLevel" )
                        .defineInRange( "enchantSaveChancePerLevel", 0.9D, 0, 100) );

                this.anvilCostReductionPerLevel = subscriber.subscribe(builder
                        .comment( "Vanilla starts at 50, hence: (50 - [this] * level)" )
                        .translation( "pmmo.anvilCostReductionPerLevel" )
                        .defineInRange( "anvilCostReductionPerLevel", 0.25D, 0, 100) );

                this.extraChanceToNotBreakAnvilPerLevel = subscriber.subscribe(builder
                        .comment( "Chance to not break anvil, 100 = twice the value, half the chance per Level." )
                        .translation( "pmmo.extraChanceToNotBreakAnvilPerLevel" )
                        .defineInRange( "extraChanceToNotBreakAnvilPerLevel", 1D, 0, 100) );

                this.anvilFinalItemBonusRepaired = subscriber.subscribe(builder
                        .comment( "Bonus repair durability per level (100 = twice as much repair per level)" )
                        .translation( "pmmo.anvilFinalItemBonusRepaired" )
                        .defineInRange( "anvilFinalItemBonusRepaired", 1D, 0, 100) );

                this.anvilFinalItemMaxCostToAnvil = subscriber.subscribe(builder
                        .comment( "Vanilla caps at 50, at around 30 vanilla you can no longer anvil the item again. allows unlocking infinite Anvil uses." )
                        .translation( "pmmo.anvilFinalItemMaxCostToAnvil" )
                        .defineInRange( "anvilFinalItemMaxCostToAnvil", 10, 0, 50) );

                this.bypassEnchantLimit = subscriber.subscribe(builder
                        .comment( "Anvil combination limits enchantments to max level set in this config" )
                        .translation( "pmmo.bypassEnchantLimit" )
                        .define( "bypassEnchantLimit", true ) );

                this.levelsPerOneEnchantBypass = subscriber.subscribe(builder
                        .comment( "How many levels per each Enchantment Level Bypass above max level enchantment can support in vanilla" )
                        .translation( "pmmo.levelsPerOneEnchantBypass" )
                        .defineInRange( "levelsPerOneEnchantBypass", 50, 0, 1000) );

                this.maxEnchantmentBypass = subscriber.subscribe(builder
                        .comment( "Max amount of levels enchants are able to go above max vanilla level" )
                        .translation( "pmmo.maxEnchantmentBypass" )
                        .defineInRange( "maxEnchantmentBypass", 10, 0, 1000) );

                this.maxEnchantLevel = subscriber.subscribe(builder
                        .comment( "Anvil combination limits enchantments to this level" )
                        .translation( "pmmo.maxEnchantLevel" )
                        .defineInRange( "maxEnchantLevel", 255, 0, 255) );

                this.upgradeChance = subscriber.subscribe(builder
                        .comment( "What is the chance to Bypass a max enchant level (provided you got the skill to do so)" )
                        .translation( "pmmo.upgradeChance" )
                        .defineInRange( "upgradeChance", 50D, 0, 100) );

                this.failedUpgradeKeepLevelChance = subscriber.subscribe(builder
                        .comment( "What is the chance to Reduce a level after a Upgrade chance fails (100 = everytime you fail bypass, enchant level goes down by 1)" )
                        .translation( "pmmo.failedUpgradeKeepLevelChance" )
                        .defineInRange( "failedUpgradeKeepLevelChance", 50D, 0, 100) );

                this.alwaysUseUpgradeChance = subscriber.subscribe(builder
                        .comment( "false = Upgrade Chance if only rolled if you are trying to upgrade your item ABOVE vanilla max level. true = you ALWAYS have an upgrade chance level." )
                        .translation( "pmmo.alwaysUseUpgradeChance" )
                        .define( "alwaysUseUpgradeChance", false) );

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
                        .defineInRange( "nightvisionUnlockLevel", 25, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Fishing" );
            {
                this.fishPoolBaseChance = subscriber.subscribe(builder
                        .comment( "What is the chance on each successful fishing attempt to access the fish_pool" )
                        .translation( "pmmo.fishPoolBaseChance" )
                        .defineInRange( "fishPoolBaseChance", 0D, 0, 100) );

                this.fishPoolChancePerLevel = subscriber.subscribe(builder
                        .comment( "What is the increase per level to access the fish_pool" )
                        .translation( "pmmo.fishPoolChancePerLevel" )
                        .defineInRange( "fishPoolChancePerLevel", 0.25D, 0, 100) );

                this.fishPoolMaxChance = subscriber.subscribe(builder
                        .comment( "What is the max chance to access the fish_pool" )
                        .translation( "pmmo.fishPoolMaxChance" )
                        .defineInRange( "fishPoolMaxChance", 80D, 0, 100) );


                builder.pop();
            }

            builder.push( "Crafting" );
            {
                this.xpValueCraftingEnabled = subscriber.subscribe(builder
                        .comment( "Should xp values for crafting be enabled? False means the default value is used" )
                        .translation( "pmmo.xpValueCraftingEnabled" )
                        .define( "xpValueCraftingEnabled", true ) );

                this.defaultCraftingXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Crafting for each item crafted? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultCraftingXp" )
                        .defineInRange( "defaultCraftingXp", 1D, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Slayer" );
            {
                this.aggresiveMobSlayerXp = subscriber.subscribe(builder
                        .comment( "How much slayer xp is awarded upon killing an aggresive mob by default" )
                        .translation( "pmmo.aggresiveMobSlayerXp" )
                        .defineInRange( "aggresiveMobSlayerXp", 0D, 0, 10000) );

                builder.pop();
            }

            builder.push( "Hunter" );
            {
                this.passiveMobHunterXp = subscriber.subscribe(builder
                        .comment( "How much hunter xp is awarded upon killing a passive mob by default" )
                        .translation( "pmmo.passiveMobHunterXp" )
                        .defineInRange( "passiveMobHunterXp", 0D, 0, 10000) );

                builder.pop();
            }

            builder.push( "Taming" );
            {
                this.tamingXpEnabled = subscriber.subscribe(builder
                        .comment( "Do players get xp for taming animals?" )
                        .translation( "pmmo.tamingXpEnabled" )
                        .define( "tamingXpEnabled", true ) );

                this.defaultTamingXp = subscriber.subscribe(builder
                        .comment( "How much xp should be awarded in Taming for Taming an animal? (Json Overrides this) (Set to 0 to disable default xp)" )
                        .translation( "pmmo.defaultTamingXp" )
                        .defineInRange( "defaultTamingXp", 0D, 0, 1000000) );

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
            LogHandler.LOGGER.error( "UNABLE TO READ PMMO CONFIG \"" + key + "\" PLEASE REPORT (This is normal during boot if JEI is installed)" );
            return -1;
        }
    }
}
