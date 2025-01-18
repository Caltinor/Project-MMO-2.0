package harmonised.pmmo.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record PerksConfig(Map<EventType, List<CompoundTag>> perks) implements ConfigData<PerksConfig> {
	public PerksConfig() {this(PerksConfig.generateDefaults());}
	
	public static final MapCodec<PerksConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(EventType.CODEC, CompoundTag.CODEC.listOf()).fieldOf("perks").forGetter(PerksConfig::perks)
	).apply(instance, PerksConfig::new));

	private static Map<EventType, List<CompoundTag>> generateDefaults() {
		Map<EventType, List<CompoundTag>> defaultSettings = new HashMap<>();
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
					.withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.BLOCK_INTERACTION_RANGE).toString())
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withDouble(APIUtils.MAX_BOOST, 10d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "building").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "excavation").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "woodcutting").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "farming").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "agility")
					.withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.MOVEMENT_SPEED).toString())
					.withDouble(APIUtils.PER_LEVEL, 0.000015)
					.withDouble(APIUtils.MAX_BOOST, 1d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "agility").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "endurance")
					.withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.MAX_HEALTH).toString())
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withDouble(APIUtils.MAX_BOOST, 10d).build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:fireworks").withString(APIUtils.SKILLNAME, "endurance").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "combat")
				.withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.ATTACK_DAMAGE).toString())
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
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "magic")
				.withDouble(APIUtils.MAX_BOOST, 1d).withDouble(APIUtils.PER_LEVEL, 0.005d).withString("attribute", "irons_spellbooks:cooldown_reduction").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "magic")
				.withDouble(APIUtils.MAX_BOOST, 2d).withDouble(APIUtils.PER_LEVEL, 0.01d).withString("attribute", "irons_spellbooks:spell_power").build());
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:attributen").withString(APIUtils.SKILLNAME, "magic")
				.withDouble(APIUtils.MAX_BOOST, 300d).withDouble(APIUtils.PER_LEVEL, 1d).withString("attribute", "irons_spellbooks:mana_regen").build());
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
				.withString("effect", "minecraft:night_vision")
				.withInt(APIUtils.MAX_BOOST, 160)
				.withInt(APIUtils.MIN_LEVEL, 25).build());
		defaultSettings.put(EventType.SUBMERGED, new ArrayList<>(bodyList));
		bodyList.clear();

		//=====================INTERACT DEFAULTS=========================
		bodyList.add(TagBuilder.start().withString("perk", "pmmo:villager_boost")
				.withString(APIUtils.SKILLNAME, "charisma").build());
		defaultSettings.put(EventType.ENTITY, new ArrayList<>(bodyList));
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
				.withList("applies_to",
				StringTag.valueOf("cgm:*"),
				StringTag.valueOf("scguns:arc_worker"),
				StringTag.valueOf("scguns:astella"),
				StringTag.valueOf("scguns:auvtomag"),
				StringTag.valueOf("scguns:blasphemy"),
				StringTag.valueOf("scguns:blunderbuss"),
				StringTag.valueOf("scguns:bomb_lance"),
				StringTag.valueOf("scguns:boomstick"),
				StringTag.valueOf("scguns:brawler"),
				StringTag.valueOf("scguns:bruiser"),
				StringTag.valueOf("scguns:callwell"),
				StringTag.valueOf("scguns:carapice"),
				StringTag.valueOf("scguns:cogloader"),
				StringTag.valueOf("scguns:combat_shotgun"),
				StringTag.valueOf("scguns:cyclone"),
				StringTag.valueOf("scguns:dark_matter"),
				StringTag.valueOf("scguns:defender_pistol"),
				StringTag.valueOf("scguns:dozier_rl"),
				StringTag.valueOf("scguns:earths_corpse"),
				StringTag.valueOf("scguns:echoes_2"),
				StringTag.valueOf("scguns:flintlock_pistol"),
				StringTag.valueOf("scguns:floundergat"),
				StringTag.valueOf("scguns:freyr"),
				StringTag.valueOf("scguns:gale"),
				StringTag.valueOf("scguns:gattaler"),
				StringTag.valueOf("scguns:gauss_rifle"),
				StringTag.valueOf("scguns:greaser_smg"),
				StringTag.valueOf("scguns:gyrojet_pistol"),
				StringTag.valueOf("scguns:handcannon"),
				StringTag.valueOf("scguns:howler"),
				StringTag.valueOf("scguns:howler_conversion"),
				StringTag.valueOf("scguns:inertial"),
				StringTag.valueOf("scguns:iron_javelin"),
				StringTag.valueOf("scguns:iron_spear"),
				StringTag.valueOf("scguns:jackhammer"),
				StringTag.valueOf("scguns:krauser"),
				StringTag.valueOf("scguns:laser_musket"),
				StringTag.valueOf("scguns:llr_director"),
				StringTag.valueOf("scguns:lockewood"),
				StringTag.valueOf("scguns:locust"),
				StringTag.valueOf("scguns:lone_wonder"),
				StringTag.valueOf("scguns:m22_waltz"),
				StringTag.valueOf("scguns:m3_carabine"),
				StringTag.valueOf("scguns:makeshift_rifle"),
				StringTag.valueOf("scguns:marlin"),
				StringTag.valueOf("scguns:mas_55"),
				StringTag.valueOf("scguns:mk43_rifle"),
				StringTag.valueOf("scguns:musket"),
				StringTag.valueOf("scguns:newborn_cyst"),
				StringTag.valueOf("scguns:osgood_50"),
				StringTag.valueOf("scguns:pax"),
				StringTag.valueOf("scguns:plasgun"),
				StringTag.valueOf("scguns:prush_gun"),
				StringTag.valueOf("scguns:pulsar"),
				StringTag.valueOf("scguns:rat_king_and_queen"),
				StringTag.valueOf("scguns:raygun"),
				StringTag.valueOf("scguns:repeating_musket"),
				StringTag.valueOf("scguns:rocket_rifle"),
				StringTag.valueOf("scguns:rusty_gnat"),
				StringTag.valueOf("scguns:saketini"),
				StringTag.valueOf("scguns:sawed_off_callwell"),
				StringTag.valueOf("scguns:scrapper"),
				StringTag.valueOf("scguns:sculk_resonator"),
				StringTag.valueOf("scguns:sequoia"),
				StringTag.valueOf("scguns:shellurker"),
				StringTag.valueOf("scguns:soul_drummer"),
				StringTag.valueOf("scguns:spitfire"),
				StringTag.valueOf("scguns:super_shotgun"),
				StringTag.valueOf("scguns:thunderhead"),
				StringTag.valueOf("scguns:umax_pistol"),
				StringTag.valueOf("scguns:uppercut"),
				StringTag.valueOf("scguns:venturi"),
				StringTag.valueOf("scguns:vulcanic_repeater"),
				StringTag.valueOf("scguns:waltz_conversion"),
				StringTag.valueOf("scguns:whispers"),
				StringTag.valueOf("scguns:winnie")
		).build());
		defaultSettings.put(EventType.DEAL_DAMAGE, new ArrayList<>(bodyList));
		return defaultSettings;
	}


	@Override
	public MapCodec<PerksConfig> getCodec() {return CODEC;}

	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.PERKS;}

	private static final String EVENT = "event";
	private static final String CLEAR = "clear_all";
	@Override
	public ConfigData<PerksConfig> getFromScripting(String param, Map<String, String> value) {
		if (param.equals(CLEAR))
			return new PerksConfig(new HashMap<>());
		else if (value.containsKey(EVENT)) {
			EventType type = EventType.byName(value.get(EVENT));
			if (type == null) {
				MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "perk script for event {%s} is not valid", value.get(EVENT));
				return this;
			}

			PerksConfig config = new PerksConfig(makeMutable(this.perks()));

			config.perks().computeIfAbsent(type, t -> new ArrayList<>()).add(tagFromValueMap(value));
			return config;
		}
		else return this;
	}

	private CompoundTag tagFromValueMap(Map<String, String> values) {
		CompoundTag outTag = new CompoundTag();
		values.entrySet().stream().filter(entry -> !entry.getKey().equals(EVENT)).forEach(entry -> {
			try {
				Tag tag = new TagParser(new StringReader(entry.getValue())).readValue();
				outTag.put(entry.getKey(), tag);
			}
			catch (CommandSyntaxException e) {
				MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "unable to parse perk value %s", entry.getValue());
			}
		});
		return outTag;
	}

	private Map<EventType, List<CompoundTag>> makeMutable(Map<EventType, List<CompoundTag>> inMap) {
		Map<EventType, List<CompoundTag>> outMap = new HashMap<>();
		inMap.forEach((type, list) -> outMap.put(type, new ArrayList<>(list)));
		return outMap;
	}

	@Override
	public PerksConfig combine(PerksConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}
}
