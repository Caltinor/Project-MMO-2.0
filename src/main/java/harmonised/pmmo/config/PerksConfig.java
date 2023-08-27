package harmonised.pmmo.config;

import java.util.ArrayList;
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
	
	private static final Codec<Map<EventType, List<CompoundTag>>> CODEC = 
			Codec.unboundedMap(EventType.CODEC, CompoundTag.CODEC.listOf());
	
	static {
		generateDefaults();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		buildPerkSettings(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ConfigObject<Map<EventType, List<CompoundTag>>> PERK_SETTINGS;
	private static Map<EventType, List<CompoundTag>> defaultSettings;
	
	private static void buildPerkSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("These settings define which perks are used and the settings which govern them.").push("Perks");
		
		PERK_SETTINGS = TomlConfigHelper.defineObject(builder, "For_Event", CODEC, defaultSettings);
		
		builder.pop();
	}
	
	private static void generateDefaults() {
		defaultSettings = new HashMap<>();
		List<CompoundTag> bodyList = new ArrayList<>();
		
		//====================BREAK SPEED DEFAULTS========================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "mining").withDouble("pickaxe_dig", 0.005).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "excavation").withDouble("shovel_dig", 0.005).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "woodcutting").withDouble("axe_dig", 0.005).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "farming").withDouble("sword_dig", 0.005).withDouble("hoe_dig", 0.005).withDouble("shears_dig", 0.005).build());
		defaultSettings.put(EventType.BREAK_SPEED, new ArrayList<>(bodyList));
		bodyList.clear();
		//====================SKILL_UP DEFAULTS==========================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "mining").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "building")
					.withString(APIUtils.ATTRIBUTE, "forge:reach_distance")
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withDouble(APIUtils.MAX_BOOST, 10d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "building").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "excavation").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "woodcutting").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "farming").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "agility")
					.withString(APIUtils.ATTRIBUTE, "minecraft:generic.movement_speed")
					.withDouble(APIUtils.PER_LEVEL, 0.000015)
					.withDouble(APIUtils.MAX_BOOST, 1d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "agility").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "endurance")
					.withString(APIUtils.ATTRIBUTE, "minecraft:generic.max_health")
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withDouble(APIUtils.MAX_BOOST, 10d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "endurance").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "combat")
				.withString(APIUtils.ATTRIBUTE, "minecraft:generic.attack_damage")
				.withDouble(APIUtils.PER_LEVEL, 0.005)
				.withDouble(APIUtils.MAX_BOOST, 1d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "combat").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "gunslinging").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "archery").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "smithing").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "flying").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "swimming").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "sailing").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "fishing").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "crafting").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "magic").build());
		bodyList.add(TagBuilder.start().withString("perk", "ars_scalaes:mana_boost").withString(APIUtils.SKILLNAME, "magic")
				.withDouble(APIUtils.MAX_BOOST, 3000d).withDouble(APIUtils.PER_LEVEL, 3.0d).build());
		bodyList.add(TagBuilder.start().withString("perk", "ars_scalaes:mana_regen").withString(APIUtils.SKILLNAME, "magic")
				.withDouble(APIUtils.MAX_BOOST, 100d).withDouble(APIUtils.PER_LEVEL, 0.06d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "slayer").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "hunter").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "taming").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "cooking").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "alchemy").build());
		
		defaultSettings.put(EventType.SKILL_UP, new ArrayList<>(bodyList));
		bodyList.clear();
		
		//=====================JUMP DEFAULTS=============================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost").withString(APIUtils.SKILLNAME, "agility").withDouble(APIUtils.PER_LEVEL, 0.0005).build());
		defaultSettings.put(EventType.JUMP, new ArrayList<>(bodyList));
		bodyList.clear();
		
		//=====================JUMP DEFAULTS=============================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost").withString(APIUtils.SKILLNAME, "agility").withDouble(APIUtils.PER_LEVEL, 0.001).build());
		defaultSettings.put(EventType.SPRINT_JUMP, new ArrayList<>(bodyList));
		bodyList.clear();
				
		//=====================JUMP DEFAULTS=============================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost").withString(APIUtils.SKILLNAME, "agility").withDouble(APIUtils.PER_LEVEL, 0.0015).build());
		defaultSettings.put(EventType.CROUCH_JUMP, new ArrayList<>(bodyList));
		bodyList.clear();
		
		//=====================SUBMERGED DEFAULTS========================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:breath").withString(APIUtils.SKILLNAME, "swimming").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:effect").withString(APIUtils.SKILLNAME, "swimming")
				.withString("effect", "minecraft:night_vision").build());
		defaultSettings.put(EventType.SUBMERGED, new ArrayList<>(bodyList));
		bodyList.clear();
		
		//=====================FROM_IMPACT==============================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_reduce").withString(APIUtils.SKILLNAME, "agility")
				.withString(APIUtils.DAMAGE_TYPE_IN, "minecraft:fall")
				.withDouble(APIUtils.PER_LEVEL, 0.025).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_reduce").withString(APIUtils.SKILLNAME, "endurance")
				.withString(APIUtils.DAMAGE_TYPE_IN, "minecraft:mob_attack")
				.withDouble(APIUtils.PER_LEVEL, 0.025).build());
		defaultSettings.put(EventType.RECEIVE_DAMAGE, new ArrayList<>(bodyList));
		bodyList.clear();
		
		//=====================DEAL_RANGED_DAMAGE=======================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString(APIUtils.SKILLNAME, "archery")
				.withList("applies_to", StringTag.valueOf("minecraft:bow"), StringTag.valueOf("minecraft:crossbow"), StringTag.valueOf("minecraft:trident")).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString(APIUtils.SKILLNAME, "magic")
				.withList("applies_to", StringTag.valueOf("ars_nouveau:spell_bow")).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString(APIUtils.SKILLNAME, "gunslinging")
				.withList("applies_to", StringTag.valueOf("cgm:pistol"),StringTag.valueOf("cgm:shotgun"),StringTag.valueOf("cgm:rifle")).build());
		defaultSettings.put(EventType.DEAL_DAMAGE, new ArrayList<>(bodyList));
		bodyList.clear();

		//=====================SPRINTING================================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:temp_attribute").withString(APIUtils.SKILLNAME, "agility")
				.withString(APIUtils.ATTRIBUTE, "minecraft:generic.movement_speed")
				.withInt(APIUtils.DURATION, 120)
				.withDouble(APIUtils.PER_LEVEL, 0.0035)
				.withDouble(APIUtils.MAX_BOOST, 1d).build());
		defaultSettings.put(EventType.SPRINTING, new ArrayList<>(bodyList));
	}
	
	
}
