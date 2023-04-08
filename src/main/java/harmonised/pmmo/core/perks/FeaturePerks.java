package harmonised.pmmo.core.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();
	
	private static final Map<String, Attribute> attributeCache = new HashMap<>();
	
	private static Attribute getAttribute(CompoundTag nbt) {
		return attributeCache.computeIfAbsent(nbt.getString(APIUtils.ATTRIBUTE), 
				name -> ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(name)));
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
				double boost = Math.min(perLevel * nbt.getInt(APIUtils.SKILL_LEVEL), maxBoost) + nbt.getDouble(APIUtils.BASE);
				AttributeModifier.Operation operation = nbt.getBoolean(APIUtils.MULTIPLICATIVE) ? Operation.MULTIPLY_BASE :  Operation.ADDITION;
				
				UUID attributeID = Functions.getReliableUUID(nbt.getString(APIUtils.ATTRIBUTE)+"/"+nbt.getString(APIUtils.SKILLNAME));
				AttributeModifier modifier = new AttributeModifier(attributeID, "PMMO-modifier based on user skill", boost, operation);
				instance.removeModifier(attributeID);
				instance.addPermanentModifier(modifier);
				return NONE;
			})
			.setDescription(LangProvider.PERK_ATTRIBUTE_DESC.asComponent())
			.setStatus((player, settings) -> {
				double perLevel = settings.getDouble(APIUtils.PER_LEVEL);
				String skillname = settings.getString(APIUtils.SKILLNAME);
				int skillLevel = settings.getInt(APIUtils.SKILL_LEVEL);
				return List.of(
				LangProvider.PERK_ATTRIBUTE_STATUS_1.asComponent(Component.translatable(getAttribute(settings).getDescriptionId())),
				LangProvider.PERK_ATTRIBUTE_STATUS_2.asComponent(perLevel, Component.translatable("pmmo."+skillname)),
				LangProvider.PERK_ATTRIBUTE_STATUS_3.asComponent(perLevel * skillLevel));
			}).build();
	
	public static BiFunction<Player, CompoundTag, CompoundTag> EFFECT_SETTER = (player, nbt) -> {
		MobEffect effect;
		if ((effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(nbt.getString("effect")))) != null) {
			int configDuration = nbt.getInt(APIUtils.DURATION);
			int duration = player.hasEffect(effect) && player.getEffect(effect).getDuration() > configDuration 
					? player.getEffect(effect).getDuration() 
					: configDuration;
			int perLevel = nbt.getInt(APIUtils.PER_LEVEL);
			int amplifier = nbt.getInt(APIUtils.MODIFIER);
			boolean ambient = nbt.getBoolean(APIUtils.AMBIENT);
			boolean visible = nbt.getBoolean(APIUtils.VISIBLE);
			player.addEffect(new MobEffectInstance(effect, perLevel * duration, amplifier, ambient, visible));
		}
		return NONE;
	};
	
	public static final Perk EFFECT = Perk.begin()
			.addDefaults(TagBuilder.start().withString("effect", "modid:effect")
					.withInt(APIUtils.DURATION, 100)
					.withInt(APIUtils.PER_LEVEL, 1)
					.withInt(APIUtils.MODIFIER, 0)
					.withBool(APIUtils.AMBIENT, false)
					.withBool(APIUtils.VISIBLE, true).build())
			.setStart(EFFECT_SETTER)
			.setTick((player, nbt, ticks) -> EFFECT_SETTER.apply(player, nbt))
			.setDescription(LangProvider.PERK_EFFECT_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_EFFECT_STATUS_1.asComponent(Component.translatable(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(nbt.getString("effect"))).getDescriptionId())),
					LangProvider.PERK_EFFECT_STATUS_2.asComponent(nbt.getInt(APIUtils.MODIFIER), nbt.getInt(APIUtils.DURATION))))
			.build();
	
	private static BiFunction<Player, CompoundTag, List<MutableComponent>> JUMP_LINES = (player, nbt) -> 
			List.of(LangProvider.PERK_JUMP_BOOST_STATUS_1.asComponent(
			nbt.getInt(APIUtils.PER_LEVEL) * nbt.getInt(APIUtils.SKILL_LEVEL)));
	private static CompoundTag JUMP_DEFAULTS = TagBuilder.start()
			.withDouble(APIUtils.PER_LEVEL, 0.0005)
			.withDouble(APIUtils.MAX_BOOST, 0.033).build();
	
	public static final Perk JUMP_CLIENT = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
	        double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL));
	        player.setDeltaMovement(player.getDeltaMovement().add(0, jumpBoost, 0));
	        player.hurtMarked = true; 
	        return NONE;
		})
		.setDescription(LangProvider.PERK_JUMP_BOOST_DESC.asComponent())
		.setStatus(JUMP_LINES).build();
	
	public static final Perk JUMP_SERVER = Perk.begin()
		.addDefaults(JUMP_DEFAULTS)
		.setStart((player, nbt) -> {
			double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL));
	        return TagBuilder.start().withDouble(APIUtils.JUMP_OUT, player.getDeltaMovement().y + jumpBoost).build();
		})
		.setDescription(LangProvider.PERK_JUMP_BOOST_DESC.asComponent())
		.setStatus(JUMP_LINES).build();
	
	public static final Perk BREATH = Perk.begin()
			.addConditions((player, nbt) -> player.getAirSupply() < 2)
			.addDefaults(TagBuilder.start().withLong(APIUtils.COOLDOWN, 30000l).withDouble(APIUtils.PER_LEVEL, 1d).build())
			.setStart((player, nbt) -> {
				int perLevel = Math.max(1, (int)((double)nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)));
				player.setAirSupply(player.getAirSupply() + perLevel);
				player.sendSystemMessage(LangProvider.PERK_BREATH_REFRESH.asComponent());
				return NONE;
			})
			.setDescription(LangProvider.PERK_BREATH_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_BREATH_STATUS_1.asComponent((int)((double)nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL))),
					LangProvider.PERK_BREATH_STATUS_2.asComponent(nbt.getInt(APIUtils.COOLDOWN)/20))).build();

	public static final Perk FALL_SAVE = Perk.begin()
			.addDefaults(TagBuilder.start().withDouble(APIUtils.PER_LEVEL, 0.025).withFloat(APIUtils.DAMAGE_IN, 0).build())
			.setStart((player, nbt) -> {
				float saved = (int)(nbt.getDouble(APIUtils.PER_LEVEL) * (double)nbt.getInt(APIUtils.SKILL_LEVEL));
				return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, Math.max(nbt.getFloat(APIUtils.DAMAGE_IN) - saved, 0)).build();
			})
			.setDescription(LangProvider.PERK_FALL_SAVE_DESC.asComponent())
			.setStatus((player, nbt) -> List.of(
					LangProvider.PERK_FALL_SAVE_STATUS_1.asComponent(nbt.getInt(APIUtils.SKILL_LEVEL) * nbt.getDouble(APIUtils.PER_LEVEL)),
					LangProvider.PERK_BREATH_STATUS_2.asComponent(nbt.getInt(APIUtils.COOLDOWN)/20))).build();

	public static final String APPLICABLE_TO = "applies_to";
	public static final Perk DAMAGE_BOOST = Perk.begin()
			.addConditions((player, nbt) -> {
				List<String> type = nbt.getList(APPLICABLE_TO, Tag.TAG_STRING).stream().map(tag -> tag.getAsString()).toList();
				for (String key : type) {
					if (key.startsWith("#") && ForgeRegistries.ITEMS.tags()
							.getTag(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(key.substring(1))))
							.stream().anyMatch(item -> player.getMainHandItem().getItem().equals(item))) {
						return true;
					}
					else if (key.endsWith(":*") && ForgeRegistries.ITEMS.getValues().stream()
							.anyMatch(item -> player.getMainHandItem().getItem().equals(item))) {
						return true;
					}
					else if (key.equals(RegistryUtil.getId(player.getMainHandItem()).toString()))
						return true;
						
				}
				System.out.println("Not applicable To"); //TODO remove
				return false;
			})
			.addDefaults(TagBuilder.start()
				.withFloat(APIUtils.DAMAGE_IN, 0)
				.withList(FeaturePerks.APPLICABLE_TO, StringTag.valueOf("weapon:id"))
				.withDouble(APIUtils.PER_LEVEL, 0.05)
				.withDouble(APIUtils.BASE, 1d)
				.withBool(APIUtils.MULTIPLICATIVE, true).build())
			.setStart((player, nbt) -> {
				float damageModification = (float)(nbt.getDouble(APIUtils.BASE) + nbt.getDouble(APIUtils.PER_LEVEL) * (double)nbt.getInt(APIUtils.SKILL_LEVEL));
				float damage = nbt.getBoolean(APIUtils.MULTIPLICATIVE) 
						? nbt.getFloat(APIUtils.DAMAGE_IN) * damageModification
						: nbt.getFloat(APIUtils.DAMAGE_IN) + damageModification;
				return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, damage).build();
			})
			.setDescription(LangProvider.PERK_DAMAGE_BOOST_DESC.asComponent())
			.setStatus((player, nbt) ->{
				List<MutableComponent> lines = new ArrayList<>();
				MutableComponent line1 = LangProvider.PERK_DAMAGE_BOOST_STATUS_1.asComponent();
				for (Tag entry : nbt.getList(APPLICABLE_TO, Tag.TAG_STRING)) {
					Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getAsString()));
					line1.append(item.equals(Items.AIR) ? Component.literal(entry.getAsString()) : item.getDescription());
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
						player.getServer().getFunctions().get(new ResourceLocation(nbt.getString(FUNCTION))).get(), 
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

}
