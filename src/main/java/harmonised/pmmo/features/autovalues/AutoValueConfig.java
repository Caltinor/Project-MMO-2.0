package harmonised.pmmo.features.autovalues;

import net.minecraftforge.common.ForgeConfigSpec;

public class AutoValueConfig {
	public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_AUTO_VALUES;
	
	public static void buildServer(ForgeConfigSpec.Builder builder) {
		builder.comment("Auto Values estimate values based on item/block/entity properties", 
				"and kick in when no other defined requirement or xp value is present").push("Auto Values");

		ENABLE_AUTO_VALUES = builder.comment("set this to false to disable the auto values system.")
						.define("Auto Values Enabled", true);

		builder.pop();
	}
}
