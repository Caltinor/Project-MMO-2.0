package harmonised.pmmo.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Config
{
    public static ConfigImplementation config;

    public static void init()
    {
        config = ConfigHelper.register( ModConfig.Type.COMMON, ConfigImplementation::new );
    }

    public static class ConfigImplementation
    {
        //Miscellaneous
        public ConfigHelper.ConfigValueListener<Boolean> crawlingAllowed;

        //Levels
        public ConfigHelper.ConfigValueListener<Integer> maxLevel;
        public ConfigHelper.ConfigValueListener<Integer> baseXp;
        public ConfigHelper.ConfigValueListener<Integer> xpIncreasePerLevel;

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

        //GUI
        public ConfigHelper.ConfigValueListener<Double> barOffsetX;
        public ConfigHelper.ConfigValueListener<Double> barOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetX;
        public ConfigHelper.ConfigValueListener<Double> xpDropOffsetY;
        public ConfigHelper.ConfigValueListener<Double> xpDropSpawnDistance;
        public ConfigHelper.ConfigValueListener<Double> xpDropOpacityPerTime;
        public ConfigHelper.ConfigValueListener<Double> xpDropMaxOpacity;
        public ConfigHelper.ConfigValueListener<Integer> xpDropDecayAge;
        public ConfigHelper.ConfigValueListener<Boolean> showXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> stackXpDrops;
        public ConfigHelper.ConfigValueListener<Boolean> xpDropsAttachedToBar;

        //Breaking Speed
        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> blocksToUnbreakableY;
        public ConfigHelper.ConfigValueListener<Double> miningBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> woodcuttingBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> excavationBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> farmingBonusSpeed;

        //Mining
        public ConfigHelper.ConfigValueListener<Double> blockHardnessLimit;

        //Building
        public ConfigHelper.ConfigValueListener<Integer> levelsPerBlockReach;

        //Excavation

        //Woodcutting

        //Farming

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

        //Archery

        //Repairing
        public ConfigHelper.ConfigValueListener<Double> maxSalvageMaterialChance;
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

        //Crafting

        //Magic


        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push( "Levels" );
            {
                this.crawlingAllowed = subscriber.subscribe(builder
                        .comment( "Is crawling allowed? true = on, false = off" )
                        .translation( "pmmo.crawlingAllowed" )
                        .define( "crawlingAllowed", true) );

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
                        .defineInRange( "xpDropSpawnDistance", 100D, 0, 1000) );

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
                        .defineInRange( "xpDropDecayAge", 500, 0, 5000) );

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
                this.blockHardnessLimit = subscriber.subscribe(builder
                        .comment( "Hardest considered block (1 hardness = 1 build/remove xp. 0 = no xp for block hardness, 30 means obsidian caps at 30xp per block.)" )
                        .translation( "pmmo.blockHardnessLimit" )
                        .defineInRange( "blockHardnessLimit", 250D, 0, 1000000) );

                builder.pop();
            }

            builder.push( "Building" );
            {
                this.levelsPerBlockReach = subscriber.subscribe(builder
                        .comment( "Every how many levels you gain an extra block of reach" )
                        .translation( "pmmo.levelsPerBlockReach" )
                        .defineInRange( "levelsPerBlockReach", 25, 0, 1000) );

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
                        .comment( "How much speed boost you can get from Agility (Incredibly sensitive, default 0.1)" )
                        .translation( "pmmo.maxSpeedBoost" )
                        .defineInRange( "maxSpeedBoost", 0.1D, 0, 10) );

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
                        .comment( "How many Bonus Max Hearts you can have" )
                        .translation( "pmmo.maxHeartCap" )
                        .defineInRange( "maxHeartCap", 20, 0, 1000) );

                builder.pop();
            }

            builder.push( "Combat" );
            {
                this.levelsPerDamage = subscriber.subscribe(builder
                        .comment( "Per how many levels you gain 1 Extra Damage" )
                        .translation( "pmmo.levelsPerDamage" )
                        .defineInRange( "levelsPerDamage", 20, 0, 1000) );

                builder.pop();
            }

            builder.push( "Archery" );
            {
                builder.pop();
            }
            builder.push( "Repairing" );
            {
                this.maxSalvageMaterialChance = subscriber.subscribe(builder
                        .comment( "Max Percentage chance to return each Material" )
                        .translation( "pmmo.maxSalvageMaterialChance" )
                        .defineInRange( "maxSalvageMaterialChance", 80D, 0, 100) );

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
                        .define( "bypassEnchantLimit", true) );

                this.levelsPerOneEnchantBypass = subscriber.subscribe(builder
                        .comment( "How many levels per each Enchantment Level Bypass above max level enchantment can support in vanilla" )
                        .translation( "pmmo.levelsPerOneEnchantBypass" )
                        .defineInRange( "levelsPerOneEnchantBypass", 50, 0, 1000) );

                this.maxEnchantmentBypass = subscriber.subscribe(builder
                        .comment( "Max amount of levels enchants are able to go above max vanilla level" )
                        .translation( "pmmo.maxEnchantmentBypass" )
                        .defineInRange( "maxEnchantmentBypass", 2, 0, 1000) );

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
                builder.pop();
            }

            builder.push( "Crafting" );
            {
                builder.pop();
            }
        }
    }


}
