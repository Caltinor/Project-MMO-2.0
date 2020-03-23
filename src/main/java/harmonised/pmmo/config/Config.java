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
        public ConfigHelper.ConfigValueListener<Double> globalMultiplier;

        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> blocksToUnbreakableY;
        public ConfigHelper.ConfigValueListener<Double> miningBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> woodcuttingBonusSpeed;
        public ConfigHelper.ConfigValueListener<Double> excavationBonusSpeed;

        public ConfigHelper.ConfigValueListener<Double> maxFallSaveChance;
        public ConfigHelper.ConfigValueListener<Double> saveChancePerLevel;

        public ConfigHelper.ConfigValueListener<Double> endurancePerLevel;
        public ConfigHelper.ConfigValueListener<Double> maxEndurance;


        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push("General Config");

            this.globalMultiplier = subscriber.subscribe(builder
                    .comment("How much xp everyone gains (1 = 100%, 2 = 200%, 2 = twice as much)")
                    .translation("pmmo.globalMultiplier")
                    .defineInRange("globalMultiplier", 1D, 0, 1000) );

            builder.pop();

            builder.push("Breaking Speed");

            this.minBreakSpeed = subscriber.subscribe(builder
                    .comment("Minimum Breaking Speed")
                    .translation("pmmo.minBreakSpeed")
                    .defineInRange("minBreakSpeed", 0.5, 0, 100) );

            this.blocksToUnbreakableY = subscriber.subscribe(builder
                    .comment("How many blocks it takes to reach 0 Break Speed (will get capped by Minimum Breaking Speed)")
                    .translation("pmmo.blocksToUnbreakableY")
                    .defineInRange("blocksToUnbreakableY", 1000D, -Double.MAX_VALUE, Double.MAX_VALUE) );

            this.miningBonusSpeed = subscriber.subscribe(builder
                    .comment("How much your mining speed increases per level (1 = 1% increase per level)")
                    .translation("pmmo.blocksToUnbreakableY")
                    .defineInRange("blocksToUnbreakableY", 1D, 0, 10) );

            this.woodcuttingBonusSpeed = subscriber.subscribe(builder
                    .comment("How much your cutting speed increases per level in (1 = 1% increase per level)")
                    .translation("pmmo.woodcuttingBonusSpeed")
                    .defineInRange("woodcuttingBonusSpeed", 1D, 0, 10) );

            this.excavationBonusSpeed = subscriber.subscribe(builder
                    .comment("How much your digging speed increases per level in (1 = 1% increase per level)")
                    .translation("pmmo.excavationBonusSpeed")
                    .defineInRange("excavationBonusSpeed", 1D, 0, 10) );

            builder.push("Agility");

            this.maxFallSaveChance = subscriber.subscribe(builder
                    .comment("Maximum chance to save each point of fall damage (100 = no fall damage)")
                    .translation("pmmo.maxFallSaveChance")
                    .defineInRange("maxFallSaveChance", 64D, 0, 100) );

            this.saveChancePerLevel = subscriber.subscribe(builder
                    .comment("How much your chance to save each point of fall damage increases per level (1 = 1% increase per Level)")
                    .translation("pmmo.saveChancePerLevel")
                    .defineInRange("saveChancePerLevel", 64D, 0, 100) );

            builder.pop();

            builder.push("Endurance");

            this.maxEndurance = subscriber.subscribe(builder
                    .comment("How much endurance is max (100 = god mode)")
                    .translation("pmmo.maxEndurance")
                    .defineInRange("maxEndurance", 50D, 0, 100) );

            this.endurancePerLevel = subscriber.subscribe(builder
                    .comment("How much endurance you gain per level (1 = 1% per level)")
                    .translation("pmmo.endurancePerLevel")
                    .defineInRange("endurancePerLevel", 0.25D, 0, 100) );

            builder.pop();
        }
    }


}
