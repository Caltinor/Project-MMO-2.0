package harmonised.pmmo.features.autovalues;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
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
			new XpAwards(),
			new Requirements(),
			new Tweaks()
	);}
	public static final MapCodec<AutoValueConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.fieldOf("enabled").forGetter(AutoValueConfig::enabled),
			Codec.simpleMap(ReqType.CODEC, Codec.BOOL, StringRepresentable.keys(ReqType.values()))
					.fieldOf("enabled_requirements").forGetter(AutoValueConfig::reqEnabled),
			Codec.simpleMap(EventType.CODEC, Codec.BOOL, StringRepresentable.keys(EventType.values()))
					.fieldOf("enabled_xp_awards").forGetter(AutoValueConfig::xpEnabled),
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
		public XpAwards() {this(
				10d,
				Arrays.stream(AutoItem.EVENTTYPES).collect(Collectors
						.toMap(event -> event, event -> Map.of(event.autoValueSkill, event == EventType.SMELT || event == EventType.BREW ? 100L : 10L))),
				Arrays.stream(AutoBlock.EVENTTYPES).collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 1L))),
				Map.of("woodcutting", 10L),
				Map.of("farming", 10L),
				Map.of("excavation", 10L),
				Arrays.stream(AutoEntity.EVENTTYPES).collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 1L)))
		);}

		public static final Codec<XpAwards> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf("rarities_multiplier").forGetter(XpAwards::raritiesMultiplier),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf("item_xp").forGetter(XpAwards::itemXp),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf("block_xp").forGetter(XpAwards::blockXp),
				CodecTypes.LONG_CODEC.fieldOf("axe_breakable_override").forGetter(XpAwards::axeOverride),
				CodecTypes.LONG_CODEC.fieldOf("hoe_breakable_override").forGetter(XpAwards::hoeOverride),
				CodecTypes.LONG_CODEC.fieldOf("shovel_breakable_override").forGetter(XpAwards::shovelOverride),
				Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).fieldOf("entity_xp").forGetter(XpAwards::entityXp)
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
		public Requirements() {this(
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
		}
		public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.simpleMap(ReqType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(ReqType.values()))
						.fieldOf("items").forGetter(Requirements::itemReqs),
				CodecTypes.LONG_CODEC.fieldOf("shovel_override").forGetter(Requirements::shovelOverride),
				CodecTypes.LONG_CODEC.fieldOf("sword_override").forGetter(Requirements::swordOverride),
				CodecTypes.LONG_CODEC.fieldOf("axe_override").forGetter(Requirements::axeOverride),
				CodecTypes.LONG_CODEC.fieldOf("hoe_override").forGetter(Requirements::hoeOverride),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("penalties").forGetter(Requirements::penalties),
				CodecTypes.LONG_CODEC.fieldOf("block_default").forGetter(Requirements::blockDefault)
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
		public Tweaks() {this(0.65,
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
		);}
		public static final Codec<Tweaks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf("hardness_modifier").forGetter(Tweaks::hardnessModifier),
				Codec.simpleMap(UtensilTypes.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(UtensilTypes.values()))
						.fieldOf("tool_tweaks").forGetter(Tweaks::utensilTweaks),
				Codec.simpleMap(WearableTypes.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(WearableTypes.values()))
						.fieldOf("wearable_tweaks").forGetter(Tweaks::wearableTweaks),
				CodecTypes.DOUBLE_CODEC.fieldOf("entity_tweaks").forGetter(Tweaks::entityTweaks)
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