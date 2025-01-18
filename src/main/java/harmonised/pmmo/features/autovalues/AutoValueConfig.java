package harmonised.pmmo.features.autovalues;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.config.scripting.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public record AutoValueConfig(
		boolean enabled,
		Map<ReqType, Boolean> reqEnabled,
		Map<EventType, Boolean> xpEnabled,
		XpAwards xpAwards,
		Requirements reqs,
		Tweaks tweaks
) implements ConfigData<AutoValueConfig> {
	public AutoValueConfig() {this (
			true,
			Arrays.stream(ReqType.values()).collect(Collectors.toMap(req -> req, r -> true)),
			Arrays.stream(EventType.values()).collect(Collectors.toMap(event -> event, event -> true)),
			XpAwards.DEFAULT,
			Requirements.DEFAULT,
			Tweaks.DEFAULT
	);}
	private static final String ENABLED = "enabled";
	private static final String REQ_ENABLE = "enabled_requirements";
	private static final String XP_ENABLE = "enabled_xp_awards";
	private static final String REQ = "requirement";
	private static final String EVENT = "event";

	public static final MapCodec<AutoValueConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.fieldOf(ENABLED).forGetter(AutoValueConfig::enabled),
			Codec.simpleMap(ReqType.CODEC, Codec.BOOL, StringRepresentable.keys(ReqType.values()))
					.fieldOf(REQ_ENABLE).forGetter(AutoValueConfig::reqEnabled),
			Codec.simpleMap(EventType.CODEC, Codec.BOOL, StringRepresentable.keys(EventType.values()))
					.fieldOf(XP_ENABLE).forGetter(AutoValueConfig::xpEnabled),
			XpAwards.CODEC.fieldOf("xp_awards").forGetter(AutoValueConfig::xpAwards),
			Requirements.CODEC.fieldOf("requirements").forGetter(AutoValueConfig::reqs),
			Tweaks.CODEC.fieldOf("tweaks").forGetter(AutoValueConfig::tweaks)
	).apply(instance, AutoValueConfig::new));
	@Override
	public MapCodec<AutoValueConfig> getCodec() {
		return null;
	}
	@Override
	public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.AUTOVALUES;}

	@Override
	public ConfigData<AutoValueConfig> getFromScripting(String param, Map<String, String> value) {
		boolean enabled = param.equals(ENABLED) ? Functions.getBool(value) : this.enabled();
		var reqEnable = new HashMap<>(this.reqEnabled());
		var xpEnable = new HashMap<>(this.xpEnabled());
		XpAwards awards = XpAwards.fromValues(param, value, this);
		Requirements reqs = Requirements.fromValues(param, value, this);
		Tweaks tweaks = Tweaks.fromValues(param, value, this);
		AutoValueConfig config = new AutoValueConfig(enabled, reqEnable, xpEnable, awards, reqs, tweaks);
		switch (param) {
			case REQ_ENABLE -> {
				ReqType type = ReqType.byName(value.getOrDefault(REQ, ""));
				if (type == null) {
					MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "ReqType in autovalue req enabled script invalid: %s", value.getOrDefault(EVENT, "not_provided"));
					break;
				}
				config.reqEnabled.put(type, Functions.getBool(value));
			}
			case XP_ENABLE -> {
				EventType type = EventType.byName(value.getOrDefault(EVENT, ""));
				if (type == null) {
					MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "EventType in autovalue req enabled script invalid: %s", value.getOrDefault(EVENT, "not_provided"));
					break;
				}
				config.xpEnabled.put(type, Functions.getBool(value));
			}
			default -> {}
		}
		return config;
	}

	@Override
	public AutoValueConfig combine(AutoValueConfig two) {return two;}
	@Override
	public boolean isUnconfigured() {return false;}

	public boolean reqEnabled(ReqType type) {return reqEnabled().getOrDefault(type, false);}
	public boolean xpEnabled(EventType type) {return xpEnabled().getOrDefault(type, false);}

	public record XpAwards(
			Double raritiesMultiplier,
			Map<EventType, Map<String, Long>> itemXp,
			Map<EventType, Map<String, Long>> blockXp,
			Map<String, Long> axeOverride,
			Map<String, Long> hoeOverride,
			Map<String, Long> shovelOverride,
			Map<EventType, Map<String, Long>> entityXp
	) {
		public static final XpAwards DEFAULT = new XpAwards(
				10d,
				Arrays.stream(AutoItem.EVENTTYPES).collect(Collectors
						.toMap(event -> event, event -> Map.of(event.autoValueSkill, event == EventType.SMELT || event == EventType.BREW ? 100L : 10L))),
				Arrays.stream(AutoBlock.EVENTTYPES).collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 1L))),
				Map.of("woodcutting", 10L),
				Map.of("farming", 10L),
				Map.of("excavation", 10L),
				Arrays.stream(AutoEntity.EVENTTYPES).collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 1L)))
		);

		private static final String RARITIES = "rarities_multiplier";
		private static final String ITEM_XP = "item_xp";
		private static final String BLOCK_XP = "block_xp";
		private static final String AXE_OVERRIDE = "axe_breakable_override";
		private static final String HOE_OVERRIDE = "hoe_breakable_override";
		private static final String SHOVEL_OVERRIDE = "shovel_breakable_override";
		private static final String ENTITY_XP = "entity_xp";
		private static final String EVENT = "event";

		public static XpAwards fromValues(String param, Map<String, String> values, AutoValueConfig current) {
			double raritiesMult = param.equals(RARITIES) ? Functions.getDouble(values) : current.xpAwards().raritiesMultiplier();
			Map<String, Long> axe = param.equals(AXE_OVERRIDE) ? Functions.mapValue(values.get(AXE_OVERRIDE)) : current.xpAwards().axeOverride();
			Map<String, Long> hoe = param.equals(HOE_OVERRIDE) ? Functions.mapValue(values.get(HOE_OVERRIDE)) : current.xpAwards().hoeOverride();
			Map<String, Long> shovel = param.equals(SHOVEL_OVERRIDE) ? Functions.mapValue(values.get(SHOVEL_OVERRIDE)) : current.xpAwards().shovelOverride();
			XpAwards config = new XpAwards(raritiesMult,
					new HashMap<>(current.xpAwards().itemXp()),
					new HashMap<>(current.xpAwards().blockXp()),
					axe, hoe, shovel,
					new HashMap<>(current.xpAwards().entityXp()));
			if (!param.equals(ITEM_XP) && !param.equals(BLOCK_XP) && !param.equals(ENTITY_XP)) return config;
			EventType type = EventType.byName(values.getOrDefault(EVENT, ""));
			if (type == null)
				MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "AutoValue script event value invalid [%s]", values.getOrDefault(EVENT, param));
			else
				switch (param) {
					case ITEM_XP -> config.itemXp.put(type, Functions.mapValue(values.getOrDefault("value", "")));
					case BLOCK_XP -> config.blockXp.put(type, Functions.mapValue(values.getOrDefault("value", "")));
					case ENTITY_XP -> config.entityXp.put(type, Functions.mapValue(values.getOrDefault("value", "")));
					default -> {}
				};
			return config;
		}

		public static final Codec<XpAwards> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf(RARITIES).forGetter(XpAwards::raritiesMultiplier),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf(ITEM_XP).forGetter(XpAwards::itemXp),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf(BLOCK_XP).forGetter(XpAwards::blockXp),
				CodecTypes.LONG_CODEC.fieldOf(AXE_OVERRIDE).forGetter(XpAwards::axeOverride),
				CodecTypes.LONG_CODEC.fieldOf(HOE_OVERRIDE).forGetter(XpAwards::hoeOverride),
				CodecTypes.LONG_CODEC.fieldOf(SHOVEL_OVERRIDE).forGetter(XpAwards::shovelOverride),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf(ENTITY_XP).forGetter(XpAwards::entityXp)
		).apply(instance, XpAwards::new));

		public Map<String, Long> item(EventType type) {return itemXp().getOrDefault(type, new HashMap<>());}
		public Map<String, Long> block(EventType type) {return blockXp().getOrDefault(type, new HashMap<>());}
		public Map<String, Long> entity(EventType type) {return entityXp().getOrDefault(type, new HashMap<>());}
	}

	public record Requirements(
			Map<ReqType, Map<String, Long>> itemReqs,
			Map<String, Long> shovelOverride,
			Map<String, Long> swordOverride,
			Map<String, Long> axeOverride,
			Map<String, Long> hoeOverride,
			Map<ResourceLocation, Integer> penalties,
			Map<String, Long> blockDefault
	) {
		public static final Requirements DEFAULT = new Requirements(
				Map.of(
						ReqType.WEAR, Map.of("endurance", 1L),
						ReqType.USE_ENCHANTMENT, Map.of("magic", 1L),
						ReqType.TOOL, Map.of("mining", 1L),
						ReqType.WEAPON, Map.of("combat", 1L)
				),
				Map.of("excavation", 1L),
				Map.of("farming", 1L),
				Map.of("woodcutting", 1L),
				Map.of("farming", 1L),
				Map.of(
						Reference.mc("mining_fatigue"), 1,
						Reference.mc("weakness"), 1,
						Reference.mc("slowness"), 1
				),
				Map.of("mining", 1L));

		private static final String SHOVEL_OVERRIDE = "shovel_override";
		private static final String SWORD_OVERRIDE = "sword_override";
		private static final String AXE_OVERRIDE = "axe_override";
		private static final String HOE_OVERRIDE = "hoe_override";
		private static final String BLOCK_DEFAULT = "block_default";
		private static final String PENALTIES = "penalties";
		private static final String ITEMS = "items";
		private static final String REQ = "requirement";

		public static Requirements fromValues(String param, Map<String, String> values, AutoValueConfig current) {
			var shovel = param.equals(SHOVEL_OVERRIDE) ? Functions.mapValue(values.getOrDefault("value", "")) : current.reqs().shovelOverride;
			var sword = param.equals(SWORD_OVERRIDE) ? Functions.mapValue(values.getOrDefault("value", "")) : current.reqs().swordOverride;
			var axe = param.equals(AXE_OVERRIDE) ? Functions.mapValue(values.getOrDefault("value", "")) : current.reqs().axeOverride;
			var hoe = param.equals(HOE_OVERRIDE) ? Functions.mapValue(values.getOrDefault("value", "")) : current.reqs().hoeOverride;
			var block = param.equals(BLOCK_DEFAULT) ? Functions.mapValue(values.getOrDefault("value", "")) : current.reqs().blockDefault;
			var penalties = param.equals(PENALTIES) ? rlMap(values) : current.reqs().penalties;
			var items = new HashMap<>(current.reqs().itemReqs);
			if (param.equals(ITEMS)) {
				ReqType type = ReqType.byName(values.getOrDefault(REQ, ""));
				if (type == null)
					MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "Autovalue script items requirement value invalid: %s", values.getOrDefault(REQ, "missing value"));
				else
					items.put(type, Functions.mapValue(values.getOrDefault("value", "")));
			}
			return new Requirements(items, shovel, sword, axe, hoe, penalties, block);
		}

		private static Map<ResourceLocation, Integer> rlMap(Map<String, String> values) {
			Map<ResourceLocation, Integer> outMap = new HashMap<>();
			String[] elements = values.getOrDefault("value", "").replaceAll("\\)", "").split(",");
			for (int i = 0; i <= elements.length-2; i += 2) {
				outMap.put(Reference.of(elements[i]), Integer.parseInt(elements[i+1]));
			}
			return outMap;
		}
		public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.simpleMap(ReqType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(ReqType.values()))
						.fieldOf(ITEMS).forGetter(Requirements::itemReqs),
				CodecTypes.LONG_CODEC.fieldOf(SHOVEL_OVERRIDE).forGetter(Requirements::shovelOverride),
				CodecTypes.LONG_CODEC.fieldOf(SWORD_OVERRIDE).forGetter(Requirements::swordOverride),
				CodecTypes.LONG_CODEC.fieldOf(AXE_OVERRIDE).forGetter(Requirements::axeOverride),
				CodecTypes.LONG_CODEC.fieldOf(HOE_OVERRIDE).forGetter(Requirements::hoeOverride),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf(PENALTIES).forGetter(Requirements::penalties),
				CodecTypes.LONG_CODEC.fieldOf(BLOCK_DEFAULT).forGetter(Requirements::blockDefault)
		).apply(instance, Requirements::new));

		public Map<String, Long> req(ReqType type) {return itemReqs().getOrDefault(type, new HashMap<>());}

		public Map<String, Long> getToolReq(ItemStack stack) {
			Item item = stack.getItem();
			if (item instanceof ShovelItem) return shovelOverride();
			else if (item instanceof SwordItem) return swordOverride();
			else if (item instanceof AxeItem) return axeOverride();
			else if (item instanceof HoeItem) return hoeOverride();
			else return req(ReqType.TOOL);
		}
	}

	public record Tweaks(
			double hardnessModifier,
			Map<UtensilTypes, Map<String, Double>> utensilTweaks,
			Map<WearableTypes, Map<String, Double>> wearableTweaks,
			Map<String, Double> entityTweaks
	) {
		public static final Tweaks DEFAULT = new Tweaks(0.65,
				Arrays.stream(UtensilTypes.values()).collect(Collectors.toMap(t -> t, t -> Map.of(
						AttributeKey.DUR.key, AttributeKey.DUR.value,
						AttributeKey.TIER.key, AttributeKey.TIER.value,
						AttributeKey.DMG.key, AttributeKey.DMG.value,
						AttributeKey.SPD.key, AttributeKey.SPD.value,
						AttributeKey.DIG.key, AttributeKey.DIG.value))),
				Arrays.stream(WearableTypes.values()).collect(Collectors.toMap(t -> t, t -> Map.of(
						AttributeKey.DUR.key, AttributeKey.DUR.value,
						AttributeKey.AMR.key, AttributeKey.AMR.value,
						AttributeKey.KBR.key, AttributeKey.KBR.value,
						AttributeKey.TUF.key, AttributeKey.TUF.value))),
				Map.of(
						AttributeKey.DMG.key, AttributeKey.DMG.value,
						AttributeKey.HEALTH.key, AttributeKey.HEALTH.value,
						AttributeKey.SPEED.key, AttributeKey.SPEED.value)
		);

		private static final String HARDNESS = "hardness_modifier";
		private static final String TOOL_TWEAKS = "tool_tweaks";
		private static final String WEAR_TWEAKS = "wearable_tweaks";
		private static final String ENTITY_TWEAK = "entity_tweaks";
		private static final String TYPE = "type";

		public static Tweaks fromValues(String param, Map<String, String> values, AutoValueConfig current) {
			double hardness = param.equals(HARDNESS) ? Functions.getDouble(values) : current.tweaks().hardnessModifier();
			var utensil = new HashMap<>(current.tweaks().utensilTweaks);
			if (param.equals(TOOL_TWEAKS)) {
				UtensilTypes type = UtensilTypes.byName(values.getOrDefault(TYPE, ""));
				if (type == null)
					MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "tool tweaks type invalid: %s", values.getOrDefault(TYPE, "no value provided"));
				else
					utensil.put(type, Functions.doubleMap(values.getOrDefault("value", "")));
			}
			var wearable = new HashMap<>(current.tweaks().wearableTweaks);
			if (param.equals(WEAR_TWEAKS)) {
				WearableTypes type = WearableTypes.byName(values.getOrDefault(TYPE, ""));
				if (type == null)
					MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "wearable tweaks type invalide: %s", values.getOrDefault(TYPE, "no value provided"));
				else
					wearable.put(type, Functions.doubleMap(values.getOrDefault("value", "")));
			}
			var entitweak = param.equals(ENTITY_TWEAK) ? Functions.doubleMap(values.getOrDefault("value", "")) : current.tweaks().entityTweaks;
			return new Tweaks(hardness, utensil, wearable, entitweak);
		}
		public static final Codec<Tweaks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf(HARDNESS).forGetter(Tweaks::hardnessModifier),
				Codec.simpleMap(UtensilTypes.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(UtensilTypes.values()))
						.fieldOf(TOOL_TWEAKS).forGetter(Tweaks::utensilTweaks),
				Codec.simpleMap(WearableTypes.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(WearableTypes.values()))
						.fieldOf(WEAR_TWEAKS).forGetter(Tweaks::wearableTweaks),
				CodecTypes.DOUBLE_CODEC.fieldOf(ENTITY_TWEAK).forGetter(Tweaks::entityTweaks)
		).apply(instance, Tweaks::new));

		public Double utensil(UtensilTypes type, AttributeKey key) {
			return utensilTweaks().getOrDefault(type, new HashMap<>()).getOrDefault(key.key, 0d);}
		public Double wearable(WearableTypes type, AttributeKey key) {
			return wearableTweaks().getOrDefault(type, new HashMap<>()).getOrDefault(key.key, 0d);}
	}
	public enum UtensilTypes implements StringRepresentable{
		SWORD,
		PICKAXE,
		AXE,
		SHOVEL,
		HOE;
		public static final Codec<UtensilTypes> CODEC = StringRepresentable.fromEnum(UtensilTypes::values);
		public static final Map<String, UtensilTypes> BY_NAME = Arrays.stream(UtensilTypes.values()).collect(Collectors.toMap(UtensilTypes::getSerializedName, s -> s));
		public static UtensilTypes byName(String name) {return BY_NAME.get(name);}
		@Override
		public String getSerializedName() {return this.name();}
		public static UtensilTypes create(String name) {throw new IllegalStateException("Enum not extended");}
	}
	public enum WearableTypes implements StringRepresentable{
		HEAD,
		CHEST,
		LEGS,
		BOOTS,
		WINGS;
		public static final Codec<WearableTypes> CODEC = StringRepresentable.fromEnum(WearableTypes::values);
		public static final Map<String, WearableTypes> BY_NAME = Arrays.stream(WearableTypes.values()).collect(Collectors.toMap(WearableTypes::getSerializedName, s -> s));
		public static WearableTypes byName(String name) {return BY_NAME.get(name);}
		@Override
		public String getSerializedName() {return this.name();}
		public static WearableTypes create(String name) {throw new IllegalStateException("Enum not extended");}
		
		public static WearableTypes fromSlot(EquipmentSlot slot, boolean isElytra) {
			if (slot == null) return null;
            return switch (slot) {
                case HEAD -> HEAD;
                case CHEST -> isElytra ? WINGS : CHEST;
                case LEGS -> LEGS;
                case FEET -> BOOTS;
                default -> null;
            };
		}
	}
	public enum AttributeKey{
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
	}
}