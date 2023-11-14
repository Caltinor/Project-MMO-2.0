package harmonised.pmmo.features.anticheese;

import java.util.Map;
import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import harmonised.pmmo.features.anticheese.CheeseTracker.Setting;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AntiCheeseConfig {
	public static ModConfigSpec SERVER_CONFIG;
	
	private static final Codec<Map<EventType, Setting>> CODEC = Codec.unboundedMap(EventType.CODEC, Setting.CODEC);	
	
	static {
		ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
		setupServer(SERVER_BUILDER);		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}	
	
	public static ModConfigSpec.BooleanValue AFK_CAN_SUBTRACT;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_AFK;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_DIMINISHING;
	public static ConfigObject<Map<EventType, Setting>> SETTINGS_NORMALIZED;
	
	private static void setupServer(ModConfigSpec.Builder builder) {
		builder.comment("Anti-Cheese is a system for managing how XP is gained.",
				"There are various ways in which players can exploit",
				"features of various mods and mechanics to gain xp at",
				"rates that are not intended.  The below anti-cheese",
				"features address many of the most common.")
			.push("AntiCheese");
			
		AFK_CAN_SUBTRACT = builder.comment("if set to true, players who are AFK will continuously grow their",
				"reduction value and will end up gaining negative xp.",
				"If this is false, they only stop gaining and never lose.")
				.define("AFK_Can_Subtract", false);
		
		SETTINGS_AFK = TomlConfigHelper.defineObject(builder.comment("AFK Tracking allows you to control if and when a player",
				"should not gain xp while afk.  All afk timers are configuration",
				"specific, so you can configure separate thresholds for different",
				"types of xp."
				), "AFK", CODEC, Map.of(EventType.SUBMERGED, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
								EventType.SWIMMING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
								EventType.DIVING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
								EventType.SURFACING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build(),
								EventType.SWIM_SPRINTING, Setting.build().minTime(200).reduction(0.1).cooloff(1).build()));
				
		SETTINGS_DIMINISHING = TomlConfigHelper.defineObject(builder.comment("Diminishing XP allows you to reduce the amount of XP earned",
				"for a specific event when the xp is earned in quick succession."
				), "DiminishingXP", CODEC, Map.of(EventType.RIDING, Setting.build()
						.source("minecraft:horse","minecraft:boat").retention(200).reduction(0.005).build()));		
		
		SETTINGS_NORMALIZED = TomlConfigHelper.defineObject(builder.comment("Normalization allows you to keep xp gain values from spiking",
				"by keeping them within a range of tolerance.  When normalized,",
				"xp from an event will not exceed the threshold above the previously",
				"earned xp value."
				), "Normalization", CODEC, Map.of(EventType.SPRINTING, Setting.build().retention(400).tolerance(0.1).tolerance(10).build()));
		
		builder.pop();
		
	}

}
