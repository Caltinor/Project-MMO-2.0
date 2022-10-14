package harmonised.pmmo.features.autovalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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
				"and apply when no other defined requirement or xp value is present").push("Auto_Values");

		ENABLE_AUTO_VALUES = builder.comment("set this to false to disable the auto values system.")
						.define("Auto Values Enabled", true);
		
		//call sub-sections from here
		setupReqToggles(builder);
		setupXpGainToggles(builder);
		setupXpGainMaps(builder);
		setupReqMaps(builder);
		configureItemTweaks(builder);
		configureEntityTweaks(builder);
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
	public static ConfigObject<Map<String, Long>> AXE_OVERRIDE;
	public static ConfigObject<Map<String, Long>> HOE_OVERRIDE;
	public static ConfigObject<Map<String, Long>> SHOVEL_OVERRIDE;
	public static ForgeConfigSpec.DoubleValue RARITIES_MODIFIER;
	public static ConfigObject<Map<String, Long>> BREWABLES_OVERRIDE;
	public static ConfigObject<Map<String, Long>> SMELTABLES_OVERRIDE;
	
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
		for (EventType type : AutoItem.EVENTTYPES) {
			ITEM_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 10l)));
		}
		BREWABLES_OVERRIDE = TomlConfigHelper.<Map<String, Long>>defineObject(
				builder,
				EventType.BREW.toString()+" Default Xp Award",
				CodecTypes.LONG_CODEC,
				Collections.singletonMap(EventType.BREW.autoValueSkill, 100l));
		SMELTABLES_OVERRIDE = TomlConfigHelper.<Map<String, Long>>defineObject(
				builder,
				EventType.SMELT.toString()+" Default Xp Award",
				CodecTypes.LONG_CODEC,
				Collections.singletonMap(EventType.SMELT.autoValueSkill, 100l));
		builder.pop();
		builder.push("Blocks");
		BLOCK_XP_AWARDS = new HashMap<>();
		for (EventType type : AutoBlock.EVENTTYPES) {
			BLOCK_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 1l)));
		}
		AXE_OVERRIDE = TomlConfigHelper.<Map<String, Long>>defineObject(
				builder.comment("Special override for "+EventType.BLOCK_BREAK.toString()+" and "+EventType.BLOCK_PLACE.toString()+" events when breaking",
						"blocks in the minecraft:mineable/axe tag."),
				"Axe Breakable Block Action Override", 
				CodecTypes.LONG_CODEC, 
				Collections.singletonMap("woodcutting", 10l));
		HOE_OVERRIDE = TomlConfigHelper.<Map<String, Long>>defineObject(
				builder.comment("Special override for "+EventType.BLOCK_BREAK.toString()+" and "+EventType.BLOCK_PLACE.toString()+" events when breaking",
						"blocks in the minecraft:mineable/hoe tag."),
				"Hoe Breakable Block Action Override", 
				CodecTypes.LONG_CODEC, 
				Collections.singletonMap("farming", 10l));
		SHOVEL_OVERRIDE = TomlConfigHelper.<Map<String, Long>>defineObject(
				builder.comment("Special override for "+EventType.BLOCK_BREAK.toString()+" and "+EventType.BLOCK_PLACE.toString()+" events when breaking",
						"blocks in the minecraft:mineable/shovel tag."),
				"Shovel Breakable Block Action Override", 
				CodecTypes.LONG_CODEC, 
				Collections.singletonMap("excavation", 10l));
		RARITIES_MODIFIER = builder.comment("How much should xp for rare blocks like ores be multiplied by.")
				.defineInRange("Rarities Mulitplier", 10d, 0d, Double.MAX_VALUE);
		builder.pop();
		builder.push("Entities");
		ENTITY_XP_AWARDS = new HashMap<>();
		for (EventType type : AutoEntity.EVENTTYPES) {
			ENTITY_XP_AWARDS.put(type, TomlConfigHelper.<Map<String, Long>>defineObject(builder, type.toString()+" Default Xp Award", CodecTypes.LONG_CODEC, Collections.singletonMap(type.autoValueSkill, 1l)));
		}
		builder.pop();
		
		builder.pop();
	}
	
	private static Map<ReqType, ConfigObject<Map<String, Integer>>> ITEM_REQS;
	private static Map<ReqType, ConfigObject<Map<String, Integer>>> BLOCK_REQS;
	public static TomlConfigHelper.ConfigObject<Map<ResourceLocation, Integer>> ITEM_PENALTIES;
	
	public static Map<String, Integer> getItemReq(ReqType type) {
		ConfigObject<Map<String, Integer>> configEntry = ITEM_REQS.get(type);
		return configEntry == null ? new HashMap<>() : configEntry.get();
	}
	public static Map<String, Integer> getBlockReq(ReqType type) {
		ConfigObject<Map<String, Integer>> configEntry = BLOCK_REQS.get(type);
		return configEntry == null ? new HashMap<>() : configEntry.get();
	}
	
	private static void setupReqMaps(ForgeConfigSpec.Builder builder) {
		builder.comment("what skills and level should be required to perform the specified action").push("Requirements");
		
		builder.push("Items");
		ITEM_REQS = new HashMap<>();
		for (ReqType type : AutoItem.REQTYPES) {
			ITEM_REQS.put(type, TomlConfigHelper.<Map<String, Integer>>defineObject(builder, type.toString()+" Default Req", CodecTypes.INTEGER_CODEC, Collections.singletonMap(type.defaultSkill, 1)));
		}
		ITEM_PENALTIES = TomlConfigHelper.defineObject(builder.comment("")
				, "Item Penalties"
				, Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
				, Map.of(new ResourceLocation("mining_fatigue"), 1,
						new ResourceLocation("weakness"), 1,
						new ResourceLocation("slowness"), 1));
		builder.pop();
		builder.push("BLocks");
		BLOCK_REQS = new HashMap<>();
		for (ReqType type : AutoBlock.REQTYPES) {
			BLOCK_REQS.put(type, TomlConfigHelper.<Map<String, Integer>>defineObject(builder, type.toString()+" Default Req", CodecTypes.INTEGER_CODEC, Collections.singletonMap(type.defaultSkill, 1)));
		}
		builder.pop();
		
		builder.pop();
	}
	
	private static Map<UtensilTypes, ConfigObject<Map<String, Double>>> UTENSIL_ATTRIBUTES;
	private static Map<WearableTypes,ConfigObject<Map<String, Double>>> WEARABLE_ATTRIBUTES;
	
	public static enum UtensilTypes {
		SWORD,
		PICKAXE,
		AXE,
		SHOVEL,
		HOE;
	}
	public static enum WearableTypes {
		HEAD,
		CHEST,
		LEGS,
		BOOTS,
		WINGS;
		
		public static WearableTypes fromSlot(EquipmentSlot slot, boolean isElytra) {
			switch (slot) {
			case HEAD: return HEAD;
			case CHEST: return isElytra ? WINGS : CHEST;
			case LEGS: return LEGS;
			case FEET: return BOOTS;
			default: return null;
			}
		}
	}
	
	public static enum AttributeKey {
		DUR("Durability", 0.01),
		TIER("Tier", 10d),
		DMG("Damage", 1.5d),
		SPD("Attack_Speed", 10d),
		DIG("Dig_Speed", 10d),
		//Armor attributes
		AMR("Armor", 10d),
		KBR("Knockback_Resistance", 10d),
		TUF("Toughness", 10d),
		//EntityAttributes
		HEALTH("Health", 0.5),
		SPEED("Move_Speed", 0.15);
		
		String key;
		double value;
		AttributeKey(String key, double value) {this.key = key; this.value = value;}
		
		public static final Map<String, Double> DEFAULT_ITEM_MAP = Map.of(
			AttributeKey.DUR.key, AttributeKey.DUR.value,
			AttributeKey.TIER.key, AttributeKey.TIER.value,
			AttributeKey.DMG.key, AttributeKey.DMG.value,
			AttributeKey.SPD.key, AttributeKey.SPD.value,
			AttributeKey.DIG.key, AttributeKey.DIG.value);
		public static Map<String, Double> DEFAULT_ARMOR_MAP = Map.of(
			AttributeKey.DUR.key, AttributeKey.DUR.value,
			AttributeKey.AMR.key, AttributeKey.AMR.value,
			AttributeKey.KBR.key, AttributeKey.KBR.value,
			AttributeKey.TUF.key, AttributeKey.TUF.value);
		public static Map<String, Double> DEFAULT_ENTITY_MAP = Map.of(
			AttributeKey.DMG.key, AttributeKey.DMG.value,
			AttributeKey.HEALTH.key, AttributeKey.HEALTH.value,
			AttributeKey.SPEED.key, AttributeKey.SPEED.value);
	}
	
	public static double getUtensilAttribute(UtensilTypes tool, AttributeKey key) {return UTENSIL_ATTRIBUTES.get(tool).get().getOrDefault(key.key, 0d);}
	public static double getWearableAttribute(WearableTypes piece, AttributeKey key) {return WEARABLE_ATTRIBUTES.get(piece).get().getOrDefault(key.key, 0d);}
	
	public static ForgeConfigSpec.ConfigValue<Double> HARDNESS_MODIFIER;
	
	private static void configureItemTweaks(ForgeConfigSpec.Builder builder) {		
		builder.comment("Configuration tweaks specific to items."
				,"'"+AttributeKey.DUR.key+"' determines how much item durability affects auto value calculations"
				,"Default: 0.01 is equal to 1 per hundred durability"
				,"'"+AttributeKey.DMG.key+"' determines how much item damage affects auto value calculations"
				,"'"+AttributeKey.SPD.key+"' determines how much item attack speed affects auto value calculations"
				,"'"+AttributeKey.TIER.key+"' multiplies the default req by this per teir."
				,"'"+AttributeKey.DIG.key+"' Determines how much item block break speed affects auto value calculations"
				,"'"+AttributeKey.AMR.key+"' Determines how much item armor amount affects auto value calculations"
				,"'"+AttributeKey.KBR.key+"' Determines how much item knockback resistance affects auto value calculations"
				,"'"+AttributeKey.TUF.key+"' Determines how much item armor toughness affects auto value calculations").push("Item_Tweaks");
		
		UTENSIL_ATTRIBUTES = new HashMap<>();
		for (UtensilTypes utensil : UtensilTypes.values()) {
			UTENSIL_ATTRIBUTES.put(utensil, TomlConfigHelper.<Map<String, Double>>defineObject(builder, utensil.toString()+"_Attributes", CodecTypes.DOUBLE_CODEC, AttributeKey.DEFAULT_ITEM_MAP));
		}
		WEARABLE_ATTRIBUTES = new HashMap<>();
		for (WearableTypes piece : WearableTypes.values()) {
			WEARABLE_ATTRIBUTES.put(piece, TomlConfigHelper.<Map<String, Double>>defineObject(builder, piece.toString()+"_Attributes", CodecTypes.DOUBLE_CODEC, AttributeKey.DEFAULT_ARMOR_MAP));
		}
		HARDNESS_MODIFIER = builder.comment("how much should block hardness contribute to value calculations")
				.define("Block Hardness Modifier", 0.65d);
		
		builder.pop();
	}
	
	public static ConfigObject<Map<String, Double>> ENTITY_ATTRIBUTES;
	
	private static void configureEntityTweaks(ForgeConfigSpec.Builder builder) {		
		builder.comment("Configuration tweaks specific to entities."
				,"'"+AttributeKey.HEALTH.key+"' Determines how much entity health affects auto value calculations"
				,"'"+AttributeKey.SPEED.key+"' Determines how much entity speed affects auto value calculations"
				,"'"+AttributeKey.DMG.key+"' Determines how much entity damage affects auto value calculations").push("Entity_Tweaks");
		
		ENTITY_ATTRIBUTES = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
				"Entity_Attributes", CodecTypes.DOUBLE_CODEC, AttributeKey.DEFAULT_ENTITY_MAP);
		
		builder.pop();
	}
}
