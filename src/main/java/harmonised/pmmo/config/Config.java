package harmonised.pmmo.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Config
{
    public static ConfigImplementation config;

    public static void init()
    {
        config = ConfigHelper.register( ModConfig.Type.SERVER, ConfigImplementation::new );
    }

    public static class ConfigImplementation
    {
        public ConfigHelper.ConfigValueListener<Double> minBreakSpeed;
        public ConfigHelper.ConfigValueListener<Double> bananas;

        public ConfigImplementation(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push("General Category");
            this.minBreakSpeed = subscriber.subscribe(builder
                    .comment("Minimum Breaking Speed")
                    .translation("pmmo.bones")
                    .defineInRange("minBreakSpeed", 0.5, 0, 100));
            this.bananas = subscriber.subscribe(builder
                    .comment("Bananas")
                    .translation("pmmo.bananas")
                    .defineInRange("bananas", 0.5D, -10D, Double.MAX_VALUE));
            builder.pop();
        }
    }


}
