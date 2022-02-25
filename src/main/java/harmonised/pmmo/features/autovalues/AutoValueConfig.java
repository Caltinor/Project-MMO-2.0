package harmonised.pmmo.features.autovalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class AutoValueConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		setupServer(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_AUTO_VALUES;
	
	public static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("Auto Values estimate values based on item/block/entity properties", 
				"and kick in when no other defined requirement or xp value is present").push("Auto_Values");

		ENABLE_AUTO_VALUES = builder.comment("set this to false to disable the auto values system.")
						.define("Auto Values Enabled", true);
		
		//call sub-sections from here
		setupReqToggles(builder);
		setupXpGainToggles(builder);
		setupXpGainMaps(builder);
		//end subsections

		builder.pop();
	}
	
	private static BooleanValue[] REQS_ENABLED;	
	public static boolean isReqEnabled(ReqType type) {return REQS_ENABLED[type.ordinal()].get();}	
	private static void setupReqToggles(ForgeConfigSpec.Builder builder) {
		builder.comment("These settings turn auto-values on/off for the specific requirement type.  These are global settings").push("Req_Toggles");
		
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		REQS_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+" Req Values Generate", true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
	}
	
	private static BooleanValue[] EVENTS_ENABLED;	
	public static boolean isXpGainEnabled(EventType type) {return EVENTS_ENABLED[type.ordinal()].get();}	
	private static void setupXpGainToggles(ForgeConfigSpec.Builder builder) {
		builder.comment("These settings turn auto-values xp awards on/off for the specific event type.  These are global settings").push("XpGain_Toggles");
		
		List<EventType> rawReqList = new ArrayList<>(Arrays.asList(EventType.values()));
		EVENTS_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+" Xp Award Values Generate", true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
	}
	
	private static Map<EventType, ConfigObject<Map<String, Long>>> ITEM_XP_AWARDS;
	private static Map<EventType, ConfigObject<Map<String, Long>>> BLOCK_XP_AWARDS;
	private static Map<EventType, ConfigObject<Map<String, Long>>> ENTITY_XP_AWARDS;
	
	public static Map<String, Long> getItemXpAward(EventType type) {
		ConfigObject<Map<String, Long>> configEntry = ITEM_XP_AWARDS.get(type);
		return configEntry == null ? new HashMap<>() : configEntry.get(); 
	}
	public static Map<String, Long> getBlockXpAward(EventType type) {
		ConfigObject<Map<String, Long>> configEntry = BLOCK_XP_AWARDS.get(type);
		return configEntry == null ? new HashMap<>() : configEntry.get(); 
	}
	public static Map<String, Long> getEntityXpAward(EventType type) {
		ConfigObject<Map<String, Long>> configEntry = ENTITY_XP_AWARDS.get(type);
		return configEntry == null ? new HashMap<>() : configEntry.get(); 
	}
	
	private static void setupXpGainMaps(ForgeConfigSpec.Builder builder) {
		builder.comment("what skills and xp amount should applicable objects be granted").push("Xp_Awards");
		
		builder.push("Items");
		ITEM_XP_AWARDS = new HashMap<>();
		for (EventType type : EventType.ITEM_APPLICABLE_EVENTS) {
			ITEM_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 10l)));
		}
		builder.pop();
		builder.push("Blocks");
		BLOCK_XP_AWARDS = new HashMap<>();
		for (EventType type : EventType.BLOCK_APPLICABLE_EVENTS) {
			BLOCK_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 10l)));
		}
		builder.pop();
		builder.push("Entities");
		ENTITY_XP_AWARDS = new HashMap<>();
		for (EventType type : EventType.ENTITY_APPLICABLE_EVENTS) {
			ENTITY_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 10l)));
		}
		builder.pop();
		
		builder.pop();
	}
}
