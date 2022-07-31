package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.common.ForgeConfigSpec;

public class PerksConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	private static final Codec<Map<EventType, Map<String, List<CompoundTag>>>> CODEC = 
			Codec.unboundedMap(EventType.CODEC, 
					Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC.listOf()));
	
	static {
		generateDefaults();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		buildPerkSettings(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ConfigObject<Map<EventType, Map<String, List<CompoundTag>>>> PERK_SETTINGS;
	private static Map<EventType, Map<String, List<CompoundTag>>> defaultSettings;
	
	private static void buildPerkSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("These settings define which perks are used and the settings which govern them.").push("Perks");
		
		PERK_SETTINGS = TomlConfigHelper.defineObject(builder, "For_Event", CODEC, defaultSettings);
		
		builder.pop();
	}
	
	private static void generateDefaults() {
		defaultSettings = new HashMap<>();
		Map<String, List<CompoundTag>> bodyMap = new HashMap<>();
		
		//====================BREAK SPEED DEFAULTS========================
		bodyMap.put("mining", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("pickaxe_dig", 0.005).build()));
		bodyMap.put("excavation", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("shovel_dig", 0.005).build()));
		bodyMap.put("woodcutting", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("axe_dig", 0.005).build()));
		bodyMap.put("farming", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("hoe_dig", 0.005).withDouble("shears_dig", 0.005).build()));
		bodyMap.put("combat", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("sword_dig", 0.005).build()));
		defaultSettings.put(EventType.BREAK_SPEED, bodyMap);
		bodyMap = new HashMap<>();
		//====================SKILL_UP DEFAULTS==========================
		bodyMap.put("mining", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "mining").build()));
		bodyMap.put("building", List.of(
				TagBuilder.start().withString("perk", "pmmo:reach").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "building").build()));
		bodyMap.put("excavation", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "excavation").build()));
		bodyMap.put("woodcutting", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "woodcutting").build()));
		bodyMap.put("farming", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "farming").build()));
		bodyMap.put("agility", List.of(
				TagBuilder.start().withString("perk", "pmmo:speed").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "agility").build()));
		bodyMap.put("endurance", List.of(
				TagBuilder.start().withString("perk", "pmmo:health").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "endurance").build()));
		bodyMap.put("combat", List.of(
				TagBuilder.start().withString("perk", "pmmo:damage").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "combat").build()));
		bodyMap.put("gunslinging", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "gunslinging").build()));
		bodyMap.put("archery", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "archery").build()));
		bodyMap.put("smithing", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "smithing").build()));
		bodyMap.put("flying", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "flying").build()));
		bodyMap.put("swimming", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "swimming").build()));
		bodyMap.put("sailing", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "sailing").build()));
		bodyMap.put("fishing", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "fishing").build()));
		bodyMap.put("crafting", List.of(
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "crafting").build(),
				TagBuilder.start().withString("perk", "ars_scalaes:mana_boost").withDouble(APIUtils.MAX_BOOST, 3000d).withDouble(APIUtils.PER_LEVEL, 3.0d).build(),
				TagBuilder.start().withString("perk", "ars_scalaes:mana_regen").withDouble(APIUtils.MAX_BOOST, 100d).withDouble(APIUtils.PER_LEVEL, 0.06d).build()));
		bodyMap.put("magic", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "magic").build()));
		bodyMap.put("slayer", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "slayer").build()));
		bodyMap.put("hunter", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "hunter").build()));
		bodyMap.put("taming", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "taming").build()));
		bodyMap.put("cooking", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "cooking").build()));
		bodyMap.put("alchemy", List.of(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "alchemy").build()));
		
		defaultSettings.put(EventType.SKILL_UP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.0005).build()));
		defaultSettings.put(EventType.JUMP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.001).build()));
		defaultSettings.put(EventType.SPRINT_JUMP, bodyMap);
		bodyMap = new HashMap<>();
				
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.0015).build()));
		defaultSettings.put(EventType.CROUCH_JUMP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================SUBMERGED DEFAULTS========================
		bodyMap.put("swimming", List.of(
				TagBuilder.start().withString("perk", "pmmo:breath").build(),
				TagBuilder.start().withString("perk", "pmmo:night_vision").build()));
		defaultSettings.put(EventType.SUBMERGED, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================FROM_IMPACT==============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:fall_save").withDouble("per_level", 0.005).build()));
		bodyMap.put("endurance", List.of(TagBuilder.start().withString("perk", "pmmo:fall_save").withDouble("per_level", 0.025).build()));
		defaultSettings.put(EventType.FROM_IMPACT, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================DEAL_RANGED_DAMAGE=======================
		bodyMap.put("archery", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withList("applies_to", StringTag.valueOf("minecraft:bow"), StringTag.valueOf("mineraft:crossbow"), StringTag.valueOf("minecraft:trident")).build()));
		bodyMap.put("magic", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withList("applies_to", StringTag.valueOf("ars_nouveau:spell_bow")).build()));
		bodyMap.put("gunslinging", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withList("applies_to", StringTag.valueOf("cgm:pistol"),StringTag.valueOf("cgm:shotgun"),StringTag.valueOf("cgm:rifle")).build()));
		defaultSettings.put(EventType.DEAL_RANGED_DAMAGE, bodyMap);
		defaultSettings.put(EventType.RANGED_TO_MOBS, bodyMap);
	}
	
	
}
