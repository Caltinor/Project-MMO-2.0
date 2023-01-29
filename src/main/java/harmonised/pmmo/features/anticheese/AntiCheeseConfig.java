package harmonised.pmmo.features.anticheese;

import java.util.Map;
import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import harmonised.pmmo.features.anticheese.CheeseTracker.Setting;
import net.minecraftforge.common.ForgeConfigSpec;

public class AntiCheeseConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	private static final Codec<Map<EventType, Setting>> CODEC = Codec.unboundedMap(EventType.CODEC, Setting.CODEC);	
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		setupServer(SERVER_BUILDER);		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}	
	
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_AFK;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_DIMINISHING;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_NORMALIZED;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_RANGE_LIMIT;
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("Anti-Cheese is a system for managing how XP is gained.",
				"There are various ways in which players can exploit",
				"features of various mods and mechanics to gain xp at",
				"rates that are not intended.  The below anti-cheese",
				"features address many of the most common.")
			.push("AntiCheese");
			
		
		SETTINGS_AFK = TomlConfigHelper.defineObject(builder.comment("AFK Tracking allows you to control if and when a player",
				"should not gain xp while afk.  All afk timers are configuration",
				"specific, so you can configure separate thresholds for different",
				"types of xp."
				), "AFK", CODEC, Map.of(EventType.SWIMMING, Setting.build().minTime(200).reduction(0.01).cooloff(400).build()));
		
//		
		SETTINGS_DIMINISHING = TomlConfigHelper.defineObject(builder.comment("Diminishing XP allows you to reduce the amount of XP earned",
				"for a specific event when the xp is earned in quick succession."
				), "DiminishingXP", CODEC, Map.of(EventType.RIDING, Setting.build().source("minecraft:horse","minecraft:boat").cooloff(600).reduction(0.5).build()));
		
		
		SETTINGS_NORMALIZED = TomlConfigHelper.defineObject(builder.comment("Normalization allows you to keep xp gain values from spiking",
				"by keeping them within a range of tolerance.  When normalized,",
				"xp from an event will not exceed the threshold above the previously",
				"earned xp value."
				), "Normalization", CODEC, Map.of(EventType.SPRINTING, Setting.build().retention(400).tolerance(0.05).tolerance(15).build()));

		
		SETTINGS_RANGE_LIMIT = TomlConfigHelper.defineObject(builder.comment("Range Limits allow you to specify a distance in which xp is",
				"no longer awarded.  This can be used to prevent remote xp from",
				"GROW and SMELT events when chunk loaders are being used."
		), "RangeLimits", CODEC, Map.of(EventType.SMELT, Setting.build().build()));
		
		builder.pop();
		
	}

}
