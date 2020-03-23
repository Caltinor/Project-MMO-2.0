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
        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> blocksToUnbreakableY;

        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push("General Category");
            this.minBreakSpeed = subscriber.subscribe(builder
                    .comment("Minimum Breaking Speed")
                    .translation("pmmo.minBreakSpeed")
                    .defineInRange("minBreakSpeed", 0.5, 0, 100));
            this.blocksToUnbreakableY = subscriber.subscribe(builder
                    .comment("How many blocks it takes to reach 0 Break Speed (will get capped by Minimum Breaking Speed)")
                    .translation("pmmo.blocksToUnbreakableY")
                    .defineInRange("blocksToUnbreakableY", 1000D, -Double.MAX_VALUE, Double.MAX_VALUE));
            builder.pop();
        }
    }


}
