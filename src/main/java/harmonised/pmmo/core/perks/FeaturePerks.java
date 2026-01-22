package harmonised.pmmo.core.perks;

import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@EventBusSubscriber(modid=Reference.MOD_ID)
public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();
	//<editor-fold defaultstate="collapsed" desc="Attribute Perk">
	private static final Map<String, Holder.Reference<Attribute>> attributeCache = new HashMap<>();
	
	private static Holder.Reference<Attribute> getAttribute(CompoundTag nbt) {
		return attributeCache.computeIfAbsent(nbt.getStringOr(APIUtils.ATTRIBUTE, "missing"),
				name -> BuiltInRegistries.ATTRIBUTE.get(Reference.of(name)).orElse(null));
	}

	private static Identifier attributeID(String attribute, String skill) {
		return Reference.rl("perk/"+attribute.replace(':','_')+"/"+skill);
	}
	
	public static final Perk ATTRIBUTE = Perk.begin()
			.addDefaults(TagBuilder.start()
					.withDouble(APIUtils.MAX_BOOST, 0d)
					.withDouble(APIUtils.PER_LEVEL, 0d)
					.withDouble(APIUtils.BASE, 0d)
					.withBool(APIUtils.MULTIPLICATIVE, false).build())
			.setStart((player, nbt) -> {
				double perLevel = nbt.getDoubleOr(APIUtils.PER_LEVEL, 0d);
				double maxBoost = nbt.getDoubleOr(APIUtils.MAX_BOOST, 0d);
				AttributeInstance instance = player.getAttribute(getAttribute(nbt));
				if (instance == null) return NONE;
				double boost = Math.min(perLevel * nbt.getIntOr(APIUtils.SKILL_LEVEL, 0), maxBoost) + nbt.getDoubleOr(APIUtils.BASE, 0d);
				AttributeModifier.Operation operation = nbt.getBooleanOr(APIUtils.MULTIPLICATIVE, false) ? Operation.ADD_MULTIPLIED_BASE :  Operation.ADD_VALUE;
				
				Identifier attributeID = attributeID(nbt.getStringOr(APIUtils.ATTRIBUTE, "missing"), nbt.getStringOr(APIUtils.SKILLNAME, "missing"));
				AttributeModifier modifier = new AttributeModifier(attributeID, boost, operation);
				instance.removeModifier(attributeID);
				instance.addPermanentModifier(modifier);
				return NONE;
			}).build();

	private static final LinkedListMultimap<Player, AttributeRecord> respawnAttributes = LinkedListMultimap.create();
	private static record AttributeRecord(Holder<Attribute> attribute, AttributeModifier modifier) {}
	@SubscribeEvent
	public static void saveAttributesOnDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof Player player) {
			for (CompoundTag nbt : Config.perks().perks().getOrDefault(EventType.SKILL_UP, new ArrayList<>()).stream()
					.filter(tag -> tag.getString("perk").equals("pmmo:attribute")).toList()) {
				Holder<Attribute> attribute = getAttribute(nbt);
				AttributeInstance instance = player.getAttributes().getInstance(attribute);
				if (instance != null)
						instance.getModifiers().stream()
						.filter(mod -> mod.id().equals(attributeID(nbt.getStringOr(APIUtils.ATTRIBUTE, "missing"), nbt.getStringOr(APIUtils.SKILLNAME, "missing"))))
						.forEach(mod -> respawnAttributes.put(player, new AttributeRecord(attribute, mod)));
			}

		}
	}

	@SubscribeEvent
	public static void restoreAttributesOnSpawn(PlayerEvent.PlayerRespawnEvent event) {
		if (respawnAttributes.containsKey(event.getEntity())) {
			respawnAttributes.get(event.getEntity()).stream()
					.filter(mod -> {
						AttributeInstance instance = event.getEntity().getAttribute(mod.attribute());
						return instance != null && !instance.hasModifier(mod.modifier().id());
					})
					.forEach(mod ->	{
						AttributeInstance instance = event.getEntity().getAttribute(mod.attribute());
						if (instance != null)
							instance.addPermanentModifier(mod.modifier());
					});
			respawnAttributes.get(event.getEntity()).clear();
		}
	}

	public static final Perk TEMP_ATTRIBUTE = Perk.begin()
			.addDefaults(ATTRIBUTE.propertyDefaults())
			.setStart((player, nbt) -> {
				double perLevel = nbt.getDoubleOr(APIUtils.PER_LEVEL, 0d);
				double maxBoost = nbt.getDoubleOr(APIUtils.MAX_BOOST, 0d);
				AttributeInstance instance = player.getAttribute(getAttribute(nbt));
				double boost = Math.min(perLevel * nbt.getIntOr(APIUtils.SKILL_LEVEL, 0), maxBoost) + nbt.getDoubleOr(APIUtils.BASE, 0d);
				AttributeModifier.Operation operation = nbt.getBooleanOr(APIUtils.MULTIPLICATIVE, false) ? Operation.ADD_MULTIPLIED_BASE :  Operation.ADD_VALUE;

				Identifier attributeID = Reference.rl("temp+perk/"+nbt.getStringOr(APIUtils.ATTRIBUTE, "missing").replace(':','_')+"/"+nbt.getStringOr(APIUtils.SKILLNAME, "missing"));
				AttributeModifier modifier = new AttributeModifier(attributeID, boost, operation);
				if (instance.hasModifier(modifier.id()))
					instance.removeModifier(attributeID);
				instance.addTransientModifier(modifier);
				return NONE;
			})
			.setStop((player, nbt) -> {
				Identifier attributeID = Reference.rl("temp+perk/"+nbt.getStringOr(APIUtils.ATTRIBUTE, "missing").replace(':','_')+"/"+nbt.getStringOr(APIUtils.SKILLNAME, "missing"));
				player.getAttribute(getAttribute(nbt)).removeModifier(attributeID);
				return NONE;
			})
//			.setDescription(ATTRIBUTE.description())
//			.setStatus(ATTRIBUTE.status())
			.build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Effect Perk">
	public static BiFunction<Player, CompoundTag, CompoundTag> EFFECT_SETTER = (player, nbt) -> {
		Holder<MobEffect> effect;
		if ((effect = BuiltInRegistries.MOB_EFFECT.get(Reference.of(nbt.getStringOr("effect", "missing"))).orElse(null)) != null) {
			int skillLevel = nbt.getIntOr(APIUtils.SKILL_LEVEL, 0);
			int configDuration = nbt.getIntOr(APIUtils.DURATION, 100);
			double perLevel = nbt.getDoubleOr(APIUtils.PER_LEVEL, 1d);
			int base = nbt.getIntOr(APIUtils.BASE, 0);
			int calculatedDuration = (int)((double)skillLevel * (double) configDuration * perLevel) + base;
			calculatedDuration = Math.min(nbt.getIntOr(APIUtils.MAX_BOOST, 0), calculatedDuration);
			int duration = player.hasEffect(effect) && player.getEffect(effect).getDuration() > calculatedDuration
					? player.getEffect(effect).getDuration() 
					: calculatedDuration;

			int amplifier = nbt.getIntOr(APIUtils.MODIFIER, 0);
			boolean ambient = nbt.getBooleanOr(APIUtils.AMBIENT, false);
			boolean visible = nbt.getBooleanOr(APIUtils.VISIBLE, true);
			boolean showIcon = nbt.getBooleanOr(APIUtils.SHOW_ICON, true);
			player.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon));
		}
		return NONE;
	};
	
	public static final Perk EFFECT = Perk.begin()
			.addDefaults(TagBuilder.start().withString("effect", "modid:effect")
					.withInt(APIUtils.DURATION, 100)
					.withDouble(APIUtils.BASE, 0d)
					.withInt(APIUtils.PER_LEVEL, 1)
					.withInt(APIUtils.MIN_LEVEL, 1)
					.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE)
					.withInt(APIUtils.MODIFIER, 0)
					.withBool(APIUtils.AMBIENT, false)
					.withBool(APIUtils.VISIBLE, true)
					.withBool(APIUtils.SHOW_ICON, true).build())
			.setStart(EFFECT_SETTER)
			.setTick((player, nbt, ticks) -> EFFECT_SETTER.apply(player, nbt)).build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Jump Perks">
	private static final CompoundTag JUMP_DEFAULTS = TagBuilder.start()
			.withDouble(APIUtils.BASE, 0d)
			.withDouble(APIUtils.PER_LEVEL, 0.0005)
			.withDouble(APIUtils.MAX_BOOST, 0.25).build();
	
	public static final Perk JUMP_CLIENT = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
	        double jumpBoost = Math.min(nbt.getDoubleOr(APIUtils.MAX_BOOST, 0), -0.011 + nbt.getIntOr(APIUtils.SKILL_LEVEL, 0) * nbt.getDoubleOr(APIUtils.PER_LEVEL, 0d)) + nbt.getDoubleOr(APIUtils.BASE, 0d);
	        player.setDeltaMovement(player.getDeltaMovement().add(0, jumpBoost, 0));
	        player.hurtMarked = true; 
	        return NONE;
		}).build();
	
	public static final Perk JUMP_SERVER = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
			double jumpBoost = Math.min(nbt.getDoubleOr(APIUtils.MAX_BOOST, 0), -0.011 + nbt.getIntOr(APIUtils.SKILL_LEVEL, 0) * nbt.getDoubleOr(APIUtils.PER_LEVEL, 0)) + nbt.getDoubleOr(APIUtils.BASE, 0);
	        return TagBuilder.start().withDouble(APIUtils.JUMP_OUT, player.getDeltaMovement().y + jumpBoost).build();
		}).build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Breath Perk">
	public static final Perk BREATH = Perk.begin()
			.addConditions((player, nbt) -> player.getAirSupply() < 2)
			.addDefaults(TagBuilder.start()
					.withLong(APIUtils.COOLDOWN, 600l)
					.withDouble(APIUtils.BASE, 0d)
					.withDouble(APIUtils.PER_LEVEL, 1d)
					.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE).build())
			.setStart((player, nbt) -> {
				int perLevel = Math.max(1, (int)((double)nbt.getIntOr(APIUtils.SKILL_LEVEL, 0) * nbt.getDoubleOr(APIUtils.PER_LEVEL, 0d))) + nbt.getIntOr(APIUtils.BASE, 0);
				perLevel = Math.min(nbt.getIntOr(APIUtils.MAX_BOOST, Integer.MAX_VALUE), perLevel);
				player.setAirSupply(player.getAirSupply() + perLevel);
				player.displayClientMessage(LangProvider.PERK_BREATH_REFRESH.asComponent(), true);
				return NONE;
			}).build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Damage Boost/Reduce Perks">
	public static final Perk DAMAGE_REDUCE = Perk.begin()
			.addConditions((player, nbt) -> {
				String perkApplicableDamageType = nbt.getStringOr(APIUtils.DAMAGE_TYPE_IN, "missing");
				Registry<DamageType> damageRegistry = player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
				ResourceKey<DamageType> resourceKey = ResourceKey.create(damageRegistry.key(), Reference.of(nbt.getStringOr(APIUtils.DAMAGE_TYPE, "missing")));
				if (perkApplicableDamageType.startsWith("#") && damageRegistry
						.get(TagKey.create(Registries.DAMAGE_TYPE, Reference.of(perkApplicableDamageType.substring(1)))
								).stream().anyMatch(typeTag -> typeTag.contains(damageRegistry.get(resourceKey).get())))
					return true;
				else if (perkApplicableDamageType.endsWith(":*") && perkApplicableDamageType.substring(0, perkApplicableDamageType.indexOf(':'))
						.equals(nbt.getStringOr(APIUtils.DAMAGE_TYPE, "missing").substring(0, nbt.getStringOr(APIUtils.DAMAGE_TYPE, "missing").indexOf(':'))))
					return true;
				return perkApplicableDamageType.equals(nbt.getStringOr(APIUtils.DAMAGE_TYPE, "missing"));
			})
			.addDefaults(TagBuilder.start()
					.withDouble(APIUtils.PER_LEVEL, 0.025)
					.withDouble(APIUtils.BASE, 0d)
					.withFloat(APIUtils.DAMAGE_IN, 0)
					.withString(APIUtils.DAMAGE_TYPE, "missing")
					.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE)
					.withString(APIUtils.DAMAGE_TYPE_IN, "omitted").build())
			.setStart((player, nbt) -> {
				float saved = (nbt.getFloatOr(APIUtils.PER_LEVEL, 0f) * (float)nbt.getIntOr(APIUtils.SKILL_LEVEL, 0)) + nbt.getFloatOr(APIUtils.BASE, 0f);
				saved = Math.min(nbt.getFloatOr(APIUtils.MAX_BOOST, 0f), saved);
				float baseDamage = nbt.contains(APIUtils.DAMAGE_OUT)
						? nbt.getFloatOr(APIUtils.DAMAGE_OUT, 0)
						: nbt.getFloatOr(APIUtils.DAMAGE_IN, 0);
				nbt.putFloat(APIUtils.DAMAGE_OUT, Math.max(baseDamage - saved, 0));
				return nbt;
			}).build();

	public static final String APPLICABLE_TO = "applies_to";
	public static final Perk DAMAGE_BOOST = Perk.begin()
			.addConditions((player, nbt) -> {
				String perkApplicableDamageType = nbt.getStringOr(APIUtils.DAMAGE_TYPE, "missing");
				List<String> dmgType = nbt.getListOrEmpty(APIUtils.DAMAGE_TYPE_IN).stream().map(t -> t.asString().orElse("")).toList();
				boolean damageMatch = dmgType.isEmpty() || dmgType.stream().anyMatch(key -> {
					if (key.startsWith("#") && RegistryUtil.isInTag(player.level().registryAccess(), Registries.DAMAGE_TYPE, perkApplicableDamageType, key.substring(1)))
						return true;
					else if (key.endsWith(":*") && Reference.of(perkApplicableDamageType).getNamespace().equals(key.replace(":*", "")))
						return true;
					else return key.equals(perkApplicableDamageType);
				});
				List<String> type = nbt.getListOrEmpty(APPLICABLE_TO).stream().map(t -> t.asString().orElse("")).toList();
				boolean weaponMatch = type.isEmpty() || type.stream().anyMatch(key -> {
					if (key.startsWith("#") && RegistryUtil.isInTag(player.level().registryAccess(), Registries.ITEM, RegistryUtil.getId(player.level().registryAccess(), player.getMainHandItem()), key.substring(1)))
						return true;
					else if (key.endsWith(":*") && RegistryUtil.getId(player.getMainHandItem().getItemHolder()).getNamespace().equals(key.replace(":*","")))
						return true;
					else return key.equals(RegistryUtil.getId(player.level().registryAccess(), player.getMainHandItem()).toString());
				});
				return weaponMatch && damageMatch;
			})
			.addDefaults(TagBuilder.start()
				.withFloat(APIUtils.DAMAGE_IN, 0)
				.withFloat(APIUtils.DAMAGE_OUT, 0)
				.withList(FeaturePerks.APPLICABLE_TO)
				.withDouble(APIUtils.PER_LEVEL, 0.05)
				.withDouble(APIUtils.BASE, 1d)
				.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE)
				.withBool(APIUtils.MULTIPLICATIVE, true).build())
			.setStart((player, nbt) -> {
				float damageModification = (float)(nbt.getDoubleOr(APIUtils.BASE, 0d) + nbt.getDoubleOr(APIUtils.PER_LEVEL ,0d) * (double)nbt.getIntOr(APIUtils.SKILL_LEVEL, 0));
				damageModification = Math.min(nbt.getIntOr(APIUtils.MAX_BOOST, 0), damageModification);
				float damage = nbt.getBooleanOr(APIUtils.MULTIPLICATIVE, true)
						? nbt.getFloatOr(APIUtils.DAMAGE_OUT, 0) * damageModification
						: nbt.getFloatOr(APIUtils.DAMAGE_OUT, 0) + damageModification;
				return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, damage).build();
			}).build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Command Perk">
	public static final String COMMAND = "command";
	public static final String FUNCTION = "function";
	public static final Perk RUN_COMMAND = Perk.begin()
		.setStart((p, nbt) -> {
			if (!(p instanceof ServerPlayer)) return NONE;
			ServerPlayer player = (ServerPlayer) p;
			if (nbt.contains(FUNCTION)) {
				player.level().getServer().getFunctions().execute(
						player.level().getServer().getFunctions().get(Reference.of(nbt.getStringOr(FUNCTION, "missing"))).get(),
						player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(PermissionSet.ALL_PERMISSIONS));
			}
			else if (nbt.contains(COMMAND)) {
				player.level().getServer().getCommands().performPrefixedCommand(
						player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(PermissionSet.ALL_PERMISSIONS),
						nbt.getStringOr(COMMAND, "tell @s command perk missing command property"));
			}
			return NONE;
		}).build();
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Villager Trading Perk">
	public static final Perk VILLAGER_TRADING = Perk.begin()
			.addConditions((player, tag) -> tag.getString(APIUtils.TARGET).equals("minecraft:villager"))
			.addDefaults(TagBuilder.start()
					.withString(APIUtils.TARGET, "missing")
					.withDouble(APIUtils.BASE, 0d)
					.withInt(APIUtils.ENTITY_ID, -1)
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withLong(APIUtils.COOLDOWN, 1000L).build())
			.setStart((player, nbt) -> {
				int villagerID = nbt.getIntOr(APIUtils.ENTITY_ID, 0);
				Villager villager = (Villager) player.level().getEntity(villagerID);
				villager.onReputationEventFrom(ReputationEventType.ZOMBIE_VILLAGER_CURED, player);
				player.displayClientMessage(LangProvider.PERK_VILLAGE_FEEDBACK.asComponent(), true);
				return NONE;
			}).build();
	//</editor-fold>
}
