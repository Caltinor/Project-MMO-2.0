package harmonised.pmmo.core.perks;

import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME)
public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();
	
	private static final Map<String, Holder.Reference<Attribute>> attributeCache = new HashMap<>();
	
	private static Holder.Reference<Attribute> getAttribute(CompoundTag nbt) {
		return attributeCache.computeIfAbsent(nbt.getString(APIUtils.ATTRIBUTE), 
				name -> BuiltInRegistries.ATTRIBUTE.getHolder(Reference.of(name)).orElse(null));
	}

	private static ResourceLocation attributeID(String attribute, String skill) {
		return Reference.rl("perk/"+attribute.replace(':','_')+"/"+skill);
	}
	
	public static final Perk ATTRIBUTE = Perk.begin()
			.addDefaults(TagBuilder.start()
					.withDouble(APIUtils.MAX_BOOST, 0d)
					.withDouble(APIUtils.PER_LEVEL, 0d)
					.withDouble(APIUtils.BASE, 0d)
					.withBool(APIUtils.MULTIPLICATIVE, false).build())
			.setStart((player, nbt) -> {
				double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
				double maxBoost = nbt.getDouble(APIUtils.MAX_BOOST);
				AttributeInstance instance = player.getAttribute(getAttribute(nbt));
				if (instance == null) return NONE;
				double boost = Math.min(perLevel * nbt.getInt(APIUtils.SKILL_LEVEL), maxBoost) + nbt.getDouble(APIUtils.BASE);
				AttributeModifier.Operation operation = nbt.getBoolean(APIUtils.MULTIPLICATIVE) ? Operation.ADD_MULTIPLIED_BASE :  Operation.ADD_VALUE;
				
				ResourceLocation attributeID = attributeID(nbt.getString(APIUtils.ATTRIBUTE), nbt.getString(APIUtils.SKILLNAME));
				AttributeModifier modifier = new AttributeModifier(attributeID, boost, operation);
				instance.removeModifier(attributeID);
				instance.addPermanentModifier(modifier);
				return NONE;
			})
			.setDescription(LangProvider.PERK_ATTRIBUTE_DESC.asComponent())
			.setStatus((player, settings) -> {
				double perLevel = settings.getDouble(APIUtils.PER_LEVEL);
				String skillname = settings.getString(APIUtils.SKILLNAME);
				int skillLevel = settings.getInt(APIUtils.SKILL_LEVEL);
				String attrName = getAttribute(settings) == null
						? settings.getString(APIUtils.ATTRIBUTE)
						: getAttribute(settings).value().getDescriptionId();
				return List.of(
				LangProvider.PERK_ATTRIBUTE_STATUS_1.asComponent(Component.translatable(attrName)),
				LangProvider.PERK_ATTRIBUTE_STATUS_2.asComponent(perLevel, Component.translatable("pmmo."+skillname)),
				LangProvider.PERK_ATTRIBUTE_STATUS_3.asComponent(perLevel * skillLevel));
			}).build();

	private static final LinkedListMultimap<Player, AttributeRecord> respawnAttributes = LinkedListMultimap.create();
	private static record AttributeRecord(Holder<Attribute> attribute, AttributeModifier modifier) {}
	@SubscribeEvent
	public static void saveAttributesOnDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof Player player) {
			for (CompoundTag nbt : Config.perks().perks().get(EventType.SKILL_UP).stream()
					.filter(tag -> tag.getString("perk").equals("pmmo:attribute")).toList()) {
				Holder<Attribute> attribute = getAttribute(nbt);
				AttributeInstance instance = player.getAttributes().getInstance(attribute);
				if (instance != null)
						instance.getModifiers().stream()
						.filter(mod -> mod.id().equals(attributeID(nbt.getString(APIUtils.ATTRIBUTE), nbt.getString(APIUtils.SKILLNAME))))
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
				double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
				double maxBoost = nbt.getDouble(APIUtils.MAX_BOOST);
				AttributeInstance instance = player.getAttribute(getAttribute(nbt));
				double boost = Math.min(perLevel * nbt.getInt(APIUtils.SKILL_LEVEL), maxBoost) + nbt.getDouble(APIUtils.BASE);
				AttributeModifier.Operation operation = nbt.getBoolean(APIUtils.MULTIPLICATIVE) ? Operation.ADD_MULTIPLIED_BASE :  Operation.ADD_VALUE;

				ResourceLocation attributeID = Reference.rl("temp+perk/"+nbt.getString(APIUtils.ATTRIBUTE).replace(':','_')+"/"+nbt.getString(APIUtils.SKILLNAME));
				AttributeModifier modifier = new AttributeModifier(attributeID, boost, operation);
				if (instance.hasModifier(modifier.id()))
					instance.removeModifier(attributeID);
				instance.addTransientModifier(modifier);
				return NONE;
			})
			.setStop((player, nbt) -> {
				ResourceLocation attributeID = Reference.rl("temp+perk/"+nbt.getString(APIUtils.ATTRIBUTE).replace(':','_')+"/"+nbt.getString(APIUtils.SKILLNAME));
				player.getAttribute(getAttribute(nbt)).removeModifier(attributeID);
				return NONE;
			})
			.setDescription(ATTRIBUTE.description())
			.setStatus(ATTRIBUTE.status()).build();
	
	public static BiFunction<Player, CompoundTag, CompoundTag> EFFECT_SETTER = (player, nbt) -> {
		Holder<MobEffect> effect;
		if ((effect = BuiltInRegistries.MOB_EFFECT.getHolder(Reference.of(nbt.getString("effect"))).get()) != null) {
			int skillLevel = nbt.getInt(APIUtils.SKILL_LEVEL);
			int configDuration = nbt.getInt(APIUtils.DURATION);
			double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
			int base = nbt.getInt(APIUtils.BASE);
			int calculatedDuration = (int)((double)skillLevel * (double) configDuration * perLevel) + base;
			calculatedDuration = Math.min(nbt.getInt(APIUtils.MAX_BOOST), calculatedDuration);
			int duration = player.hasEffect(effect) && player.getEffect(effect).getDuration() > calculatedDuration
					? player.getEffect(effect).getDuration() 
					: calculatedDuration;

			int amplifier = nbt.getInt(APIUtils.MODIFIER);
			boolean ambient = nbt.getBoolean(APIUtils.AMBIENT);
			boolean visible = nbt.getBoolean(APIUtils.VISIBLE);
			boolean showIcon = nbt.getBoolean(APIUtils.SHOW_ICON);
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
			.setTick((player, nbt, ticks) -> EFFECT_SETTER.apply(player, nbt))
			.setDescription(LangProvider.PERK_EFFECT_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_EFFECT_STATUS_1.asComponent(Component.translatable(BuiltInRegistries.MOB_EFFECT.get(Reference.of(nbt.getString("effect"))).getDescriptionId())),
					LangProvider.PERK_EFFECT_STATUS_2.asComponent(nbt.getInt(APIUtils.MODIFIER),
							(nbt.getInt(APIUtils.DURATION) * nbt.getDouble(APIUtils.PER_LEVEL) * nbt.getInt(APIUtils.SKILL_LEVEL))/20)))
			.build();
	
	private static BiFunction<Player, CompoundTag, List<MutableComponent>> JUMP_LINES = (player, nbt) -> 
			List.of(LangProvider.PERK_JUMP_BOOST_STATUS_1.asComponent(
			nbt.getInt(APIUtils.PER_LEVEL) * nbt.getInt(APIUtils.SKILL_LEVEL)));
	private static CompoundTag JUMP_DEFAULTS = TagBuilder.start()
			.withDouble(APIUtils.BASE, 0d)
			.withDouble(APIUtils.PER_LEVEL, 0.0005)
			.withDouble(APIUtils.MAX_BOOST, 0.25).build();
	
	public static final Perk JUMP_CLIENT = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
	        double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)) + nbt.getDouble(APIUtils.BASE);
	        player.setDeltaMovement(player.getDeltaMovement().add(0, jumpBoost, 0));
	        player.hurtMarked = true; 
	        return NONE;
		})
		.setDescription(LangProvider.PERK_JUMP_BOOST_DESC.asComponent())
		.setStatus(JUMP_LINES).build();
	
	public static final Perk JUMP_SERVER = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
			double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)) + nbt.getDouble(APIUtils.BASE);
	        return TagBuilder.start().withDouble(APIUtils.JUMP_OUT, player.getDeltaMovement().y + jumpBoost).build();
		})
		.setDescription(LangProvider.PERK_JUMP_BOOST_DESC.asComponent())
		.setStatus(JUMP_LINES).build();
	
	public static final Perk BREATH = Perk.begin()
			.addConditions((player, nbt) -> player.getAirSupply() < 2)
			.addDefaults(TagBuilder.start()
					.withLong(APIUtils.COOLDOWN, 600l)
					.withDouble(APIUtils.BASE, 0d)
					.withDouble(APIUtils.PER_LEVEL, 1d)
					.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE).build())
			.setStart((player, nbt) -> {
				int perLevel = Math.max(1, (int)((double)nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL))) + nbt.getInt(APIUtils.BASE);
				perLevel = Math.min(nbt.getInt(APIUtils.MAX_BOOST), perLevel);
				player.setAirSupply(player.getAirSupply() + perLevel);
				player.sendSystemMessage(LangProvider.PERK_BREATH_REFRESH.asComponent());
				return NONE;
			})
			.setDescription(LangProvider.PERK_BREATH_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_BREATH_STATUS_1.asComponent((int)((double)nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL))),
					LangProvider.PERK_BREATH_STATUS_2.asComponent(nbt.getInt(APIUtils.COOLDOWN)/20))).build();

	public static final Perk DAMAGE_REDUCE = Perk.begin()
			.addConditions((player, nbt) -> {
				String perkApplicableDamageType = nbt.getString(APIUtils.DAMAGE_TYPE_IN);
				Registry<DamageType> damageRegistry = player.level().registryAccess().registry(Registries.DAMAGE_TYPE).get();
				ResourceKey<DamageType> resourceKey = ResourceKey.create(damageRegistry.key(), Reference.of(nbt.getString(APIUtils.DAMAGE_TYPE)));
				if (perkApplicableDamageType.startsWith("#") && damageRegistry
						.getTag(TagKey.create(Registries.DAMAGE_TYPE, Reference.of(perkApplicableDamageType.substring(1)))
								).stream().anyMatch(typeTag -> typeTag.contains(damageRegistry.getHolder(resourceKey).get())))
					return true;
				else if (perkApplicableDamageType.endsWith(":*") && perkApplicableDamageType.substring(0, perkApplicableDamageType.indexOf(':'))
						.equals(nbt.getString(APIUtils.DAMAGE_TYPE).substring(0, nbt.getString(APIUtils.DAMAGE_TYPE).indexOf(':'))))
					return true;
				return perkApplicableDamageType.equals(nbt.getString(APIUtils.DAMAGE_TYPE));
			})
			.addDefaults(TagBuilder.start()
					.withDouble(APIUtils.PER_LEVEL, 0.025)
					.withDouble(APIUtils.BASE, 0d)
					.withFloat(APIUtils.DAMAGE_IN, 0)
					.withString(APIUtils.DAMAGE_TYPE, "missing")
					.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE)
					.withString(APIUtils.DAMAGE_TYPE_IN, "omitted").build())
			.setStart((player, nbt) -> {
				float saved = (int)(nbt.getDouble(APIUtils.PER_LEVEL) * (double)nbt.getInt(APIUtils.SKILL_LEVEL)) + nbt.getInt(APIUtils.BASE);
				saved = Math.min(nbt.getInt(APIUtils.MAX_BOOST), saved);
				float baseDamage = nbt.contains(APIUtils.DAMAGE_OUT)
						? nbt.getFloat(APIUtils.DAMAGE_OUT)
						: nbt.getFloat(APIUtils.DAMAGE_IN);
				return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, Math.max(baseDamage - saved, 0)).build();
			})
			.setDescription(LangProvider.PERK_FALL_SAVE_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_FALL_SAVE_STATUS_1.asComponent(nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)),
					LangProvider.PERK_BREATH_STATUS_2.asComponent(nbt.getInt(APIUtils.COOLDOWN)/20))).build();

	public static final String APPLICABLE_TO = "applies_to";
	public static final Perk DAMAGE_BOOST = Perk.begin()
			.addConditions((player, nbt) -> {
				String perkApplicableDamageType = nbt.getString(APIUtils.DAMAGE_TYPE);
				List<String> dmgType = nbt.getList(APIUtils.DAMAGE_TYPE_IN, Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
				boolean damageMatch = dmgType.isEmpty() || dmgType.stream().anyMatch(key -> {
					if (key.startsWith("#") && RegistryUtil.isInTag(player.level().registryAccess(), Registries.DAMAGE_TYPE, perkApplicableDamageType, key.substring(1)))
						return true;
					else if (key.endsWith(":*") && Reference.of(perkApplicableDamageType).getNamespace().equals(key.replace(":*", "")))
						return true;
					else return key.equals(perkApplicableDamageType);
				});
				List<String> type = nbt.getList(APPLICABLE_TO, Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
				boolean weaponMatch = type.isEmpty() || type.stream().anyMatch(key -> {
					if (key.startsWith("#") && RegistryUtil.isInTag(player.level().registryAccess(), Registries.ITEM, RegistryUtil.getId(player.level().registryAccess(), player.getMainHandItem()), key.substring(1)))
						return true;
					else if (key.endsWith(":*") && BuiltInRegistries.ITEM.stream().anyMatch(item -> player.getMainHandItem().getItem().equals(item)))
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
				float damageModification = (float)(nbt.getDouble(APIUtils.BASE) + nbt.getDouble(APIUtils.PER_LEVEL) * (double)nbt.getInt(APIUtils.SKILL_LEVEL));
				damageModification = Math.min(nbt.getInt(APIUtils.MAX_BOOST), damageModification);
				float damage = nbt.getBoolean(APIUtils.MULTIPLICATIVE) 
						? nbt.getFloat(APIUtils.DAMAGE_OUT) * damageModification
						: nbt.getFloat(APIUtils.DAMAGE_OUT) + damageModification;
				return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, damage).build();
			})
			.setDescription(LangProvider.PERK_DAMAGE_BOOST_DESC.asComponent())
			.setStatus((player, nbt) ->{
				List<MutableComponent> lines = new ArrayList<>();
				MutableComponent line1 = LangProvider.PERK_DAMAGE_BOOST_STATUS_1.asComponent();
				for (Tag entry : nbt.getList(APPLICABLE_TO, Tag.TAG_STRING)) {
					Component description = Component.literal(entry.getAsString());
					if (ResourceLocation.tryParse(entry.getAsString()) != null) {
						Item item = BuiltInRegistries.ITEM.get(Reference.of(entry.getAsString()));
						if (!item.equals(Items.AIR)) description = item.getDescription();
					}
					line1.append(description);
					line1.append(Component.literal(", "));
				}
				lines.add(line1);
				lines.add(LangProvider.PERK_DAMAGE_BOOST_STATUS_2.asComponent(
						nbt.getBoolean(APIUtils.MULTIPLICATIVE) ? "x" : "+",
						(double)nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)
				));
				return lines;
			}).build();
	
	private static final String COMMAND = "command";
	private static final String FUNCTION = "function";	
	public static final Perk RUN_COMMAND = Perk.begin()
		.setStart((p, nbt) -> {
			if (!(p instanceof ServerPlayer)) return NONE;
			ServerPlayer player = (ServerPlayer) p;
			if (nbt.contains(FUNCTION)) {
				player.getServer().getFunctions().execute(
						player.getServer().getFunctions().get(Reference.of(nbt.getString(FUNCTION))).get(),
						player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2));			
			}
			else if (nbt.contains(COMMAND)) {
				player.getServer().getCommands().performPrefixedCommand(
						player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2), 
						nbt.getString(COMMAND));
			}
			return NONE;
		})
		.setDescription(LangProvider.PERK_COMMAND_DESC.asComponent())
		.setStatus((player, nbt) -> List.of(
				LangProvider.PERK_COMMAND_STATUS_1.asComponent(
				nbt.contains(FUNCTION) ? "Function" : "Command",
				nbt.contains(FUNCTION) ? nbt.getString(FUNCTION) : nbt.getString(COMMAND)))).build();

	public static final Perk VILLAGER_TRADING = Perk.begin()
			.addConditions((player, tag) -> tag.getString(APIUtils.TARGET).equals("minecraft:villager"))
			.addDefaults(TagBuilder.start()
					.withString(APIUtils.TARGET, "missing")
					.withDouble(APIUtils.BASE, 0d)
					.withInt(APIUtils.ENTITY_ID, -1)
					.withDouble(APIUtils.PER_LEVEL, 0.05)
					.withLong(APIUtils.COOLDOWN, 1000L).build())
			.setStart((player, nbt) -> {
				int villagerID = nbt.getInt(APIUtils.ENTITY_ID);
				Villager villager = (Villager) player.level().getEntity(villagerID);
				villager.onReputationEventFrom(ReputationEventType.ZOMBIE_VILLAGER_CURED, player);
				player.sendSystemMessage(LangProvider.PERK_VILLAGE_FEEDBACK.asComponent());
				return NONE;
			})
			.setDescription(LangProvider.PERK_VILLAGER_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
				LangProvider.PERK_VILLAGE_STATUS_1.asComponent(
						nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)
				)
			)).build();
}
