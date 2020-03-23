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

        //Breaking Speed
        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> blocksToUnbreakableY;
        public ConfigHelper.ConfigValueListener<Double> miningBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> woodcuttingBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> excavationBonusSpeed;

        //Mining

        //Building

        //Excavation

        //Woodcutting

        //Farming

        //Agility
        public ConfigHelper.ConfigValueListener<Double> maxFallSaveChance;
        public ConfigHelper.ConfigValueListener<Double> saveChancePerLevel;

        //Endurance
        public ConfigHelper.ConfigValueListener<Double> endurancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxEndurance;

        //Combat

        //Archery

        //Repairing

        //Flying

        //Swimming

        //Fishing

        //Crafting


        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push( "Multipliers" );

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

            builder.push( "GUI" );

            this.barOffsetX = subscriber.subscribe(builder
                    .comment( "GUI bar position X (Width)" )
                    .translation( "pmmo.barOffsetX" )
                    .defineInRange( "barOffsetX", 0.5D, 0, 1) );

            this.barOffsetY = subscriber.subscribe(builder
                    .comment( "GUI bar position Y (Height, 0 is top, 1 is bottom (1 is probably invisible due to clipping) )" )
                    .translation( "pmmo.barOffsetY" )
                    .defineInRange( "barOffsetY", 0D, 0, 1) );

            builder.pop();

            builder.push( "Breaking Speed" );

            this.minBreakSpeed = subscriber.subscribe(builder
                    .comment( "Minimum Breaking Speed" )
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

            builder.pop();

            builder.push( "Mining" );

            builder.pop();

            builder.push( "Building" );

            builder.pop();

            builder.push( "Excavation" );

            builder.pop();

            builder.push( "Woodcutting" );

            builder.pop();

            builder.push( "Farming" );

            builder.pop();

            builder.push( "Agility" );

            this.maxFallSaveChance = subscriber.subscribe(builder
                    .comment( "Maximum chance to save each point of fall damage (100 = no fall damage)" )
                    .translation( "pmmo.maxFallSaveChance" )
                    .defineInRange( "maxFallSaveChance", 64D, 0, 100) );

            this.saveChancePerLevel = subscriber.subscribe(builder
                    .comment( "How much your chance to save each point of fall damage increases per level (1 = 1% increase per Level)" )
                    .translation( "pmmo.saveChancePerLevel" )
                    .defineInRange( "saveChancePerLevel", 64D, 0, 100) );

            builder.pop();

            builder.push( "Endurance" );

            this.maxEndurance = subscriber.subscribe(builder
                    .comment( "How much endurance is max (100 = god mode)" )
                    .translation( "pmmo.maxEndurance" )
                    .defineInRange( "maxEndurance", 50D, 0, 100) );

            this.endurancePerLevel = subscriber.subscribe(builder
                    .comment( "How much endurance you gain per level (1 = 1% per level)" )
                    .translation( "pmmo.endurancePerLevel" )
                    .defineInRange( "endurancePerLevel", 0.25D, 0, 100) );

            builder.pop();

            builder.push( "Combat" );

            builder.pop();

            builder.push( "Archery" );

            builder.pop();

            builder.push( "Repairing" );

            builder.pop();

            builder.push( "Flying" );

            builder.pop();

            builder.push( "Swimming" );

            builder.pop();

            builder.push( "Fishing" );

            builder.pop();

            builder.push( "Crafting" );

            builder.pop();
        }
    }


}
