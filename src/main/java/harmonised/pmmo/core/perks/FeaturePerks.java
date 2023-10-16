package harmonised.pmmo.core.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.features.fireworks.FireworkHandler;
import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.registries.ForgeRegistries;

public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();

	private static Map<UUID, Long> regen_cooldown = new HashMap<>();
	private static Map<UUID, Long> breathe_cooldown = new HashMap<>();
	
	private static final UUID ATTRIBUTE_ID = UUID.fromString("b902b6aa-8393-4bdc-8f0d-b937268ef5af");
	private static final Map<String, Attribute> attributeCache = new HashMap<>();
	
	private static Attribute getAttribute(CompoundTag nbt) {
		return attributeCache.computeIfAbsent(nbt.getString(APIUtils.ATTRIBUTE), 
				name -> ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(name)));
	}
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> ATTRIBUTE = (player, nbt, level) -> {
		double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
		double maxBoost = nbt.getDouble(APIUtils.MAX_BOOST);
		AttributeInstance instance = player.getAttribute(getAttribute(nbt));
		double boost = Math.min(perLevel * level, maxBoost);
		
		AttributeModifier modifier = new AttributeModifier(ATTRIBUTE_ID, "PMMO-modifier based on user skill", boost, AttributeModifier.Operation.ADDITION);
		instance.removeModifier(ATTRIBUTE_ID);
		instance.addPermanentModifier(modifier);
		return NONE;
	};
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> ATTRIBUTE_TERM = (player, nbt, level) -> {
		AttributeInstance instance = player.getAttribute(getAttribute(nbt));
		instance.removeModifier(ATTRIBUTE_ID);
		return NONE;
	};
	
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> SPEED = (player, nbt, level) -> {
		double maxSpeedBoost = nbt.getDouble(APIUtils.MAX_BOOST);
		AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
		double speedBoost = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() 
							* Math.max(0, Math.min(maxSpeedBoost, Math.min(maxSpeedBoost, (level * nbt.getDouble(APIUtils.PER_LEVEL)) / 100)));

		if(speedBoost > 0)
		{
			if(speedAttribute.getModifier(speedModifierID) == null || speedAttribute.getModifier(speedModifierID).getAmount() != speedBoost)
			{
				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPermanentModifier(speedModifier);
			}
		}
		return NONE;
	};
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> SPEED_TERM = (p, nbt, l) -> {
		AttributeInstance speedAttribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
		speedAttribute.removeModifier(speedModifierID);
		return NONE;
	};
	
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> DAMAGE = (player, nbt, level) -> {
		double maxDamage = nbt.getDouble(APIUtils.MAX_BOOST);
		double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		double damageBoost = Math.min(maxDamage, level * perLevel);
		AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.MULTIPLY_BASE);
		damageAttribute.removeModifier(damageModifierID);
		damageAttribute.addPermanentModifier(damageModifier);
		return NONE;
	};
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> DAMAGE_TERM = (player, nbt, level) -> {
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		damageAttribute.removeModifier(damageModifierID);
		return NONE;
	};
	
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> REACH = (player, nbt, level) -> {
		double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
		double maxReach = nbt.getDouble(APIUtils.MAX_BOOST);
		double reach = -0.91 + (level * perLevel);
		reach = Math.min(maxReach, reach);	
		AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if(reachAttribute.getModifier(reachModifierID) == null || reachAttribute.getModifier(reachModifierID).getAmount() != reach)
		{
			AttributeModifier reachModifier = new AttributeModifier(reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION);
			reachAttribute.removeModifier(reachModifierID);
			reachAttribute.addPermanentModifier(reachModifier);
		}
		return NONE;
	};
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> REACH_TERM = (player, nbt, level) -> {
		AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		reachAttribute.removeModifier(reachModifierID);
		return NONE;
	};
	
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> HEALTH = (player, nbt, level) -> {
		double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
		int maxHeart = nbt.getInt(APIUtils.MAX_BOOST);
		int heartBoost = (int)(perLevel * (double)level);
		heartBoost = Math.min(maxHeart, heartBoost);		
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION);
		hpAttribute.removeModifier(hpModifierID);
		hpAttribute.addPermanentModifier(hpModifier);
		return NONE;
	};
	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> HEALTH_TERM = (player, nbt, level) -> {
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		hpAttribute.removeModifier(hpModifierID);
		return NONE;
	};

	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> NIGHT_VISION = (player, nbt, level) -> {
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, nbt.getInt(APIUtils.DURATION), 0, true, false, false));
		return NONE;
	};
	
	public static TriPredicate<Player, CompoundTag, Integer> NIGHT_VISION_CHECK = (player, nbt, level) -> {
		return !player.hasEffect(MobEffects.NIGHT_VISION) || player.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 80;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> REGEN = (player, nbt, level) -> {
		long cooldown = nbt.getLong(APIUtils.COOLDOWN);
		int duration = nbt.getInt(APIUtils.DURATION);
		double strength = nbt.getDouble(APIUtils.PER_LEVEL);
		int perLevel = Math.max(0, (int)((double)level * strength));
		long currentCD = regen_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		if (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis()) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, perLevel));
			regen_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return NONE;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> JUMP_CLIENT = (player, nbt, level) -> {
        double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + level * nbt.getDouble(APIUtils.PER_LEVEL));
        player.setDeltaMovement(player.getDeltaMovement().add(0, jumpBoost, 0));
        player.hurtMarked = true; 
        return NONE;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> JUMP_SERVER = (player, nbt, level) -> {
		double jumpBoost = Math.min(nbt.getDouble(APIUtils.MAX_BOOST), -0.011 + level * nbt.getDouble(APIUtils.PER_LEVEL));
        return TagBuilder.start().withDouble(APIUtils.JUMP_OUT, player.getDeltaMovement().y + jumpBoost).build();
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> BREATH = (player, nbt, level) -> {
		int perLevel = Math.max(1, (int)((double)level * nbt.getDouble(APIUtils.PER_LEVEL)));
		player.setAirSupply(player.getAirSupply() + perLevel);
		player.sendSystemMessage(LangProvider.PERK_BREATH_REFRESH.asComponent());
		breathe_cooldown.put(player.getUUID(), System.currentTimeMillis());
		return NONE;
	};
	
	public static TriPredicate<Player, CompoundTag, Integer> BREATH_CHECK = (player, nbt, level) -> {
		long currentCD = breathe_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		return player.getAirSupply() < 2 && (currentCD < System.currentTimeMillis() - nbt.getLong(APIUtils.COOLDOWN) 
				|| currentCD + 20 >= System.currentTimeMillis());
	};

	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> FALL_SAVE = (player, nbt, level) -> {
		float saved = (int)(nbt.getDouble(APIUtils.PER_LEVEL) * (double)level);
		return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, Math.max(nbt.getFloat(APIUtils.DAMAGE_IN) - saved, 0)).build();
	};

	public static final String APPLICABLE_TO = "applies_to";
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> DAMAGE_BOOST = (player, nbt, level) -> {
		float damage = nbt.getFloat(APIUtils.DAMAGE_IN) * (1f + (float)(nbt.getDouble(APIUtils.PER_LEVEL) * (double)level));
		return TagBuilder.start().withFloat(APIUtils.DAMAGE_OUT, damage).build();
	};
	
	public static TriPredicate<Player, CompoundTag, Integer> DAMAGE_BOOST_CHECK = (player, nbt, level) -> {
		List<String> type = nbt.getList(APPLICABLE_TO, Tag.TAG_STRING).stream().map(tag -> tag.getAsString()).toList();
		return type.contains(RegistryUtil.getId(player.getMainHandItem()).toString());
	};
	
	private static final String COMMAND = "command";
	private static final String FUNCTION = "function";
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> RUN_COMMAND = (p, nbt, level) -> {
		if (!(p instanceof ServerPlayer)) return NONE;
		//skill up command context to filter by skill
		if (nbt.contains(APIUtils.SKILLNAME)
				&& nbt.contains(FireworkHandler.FIREWORK_SKILL)
				&& !nbt.getString(APIUtils.SKILLNAME).equals(nbt.getString(FireworkHandler.FIREWORK_SKILL)))
			return NONE;
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
	};
	
	public static final String EFFECT = "effect";
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> GIVE_EFFECT = (player, nbt, level) -> {
		MobEffect effect;
		if ((effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(nbt.getString(EFFECT)))) != null) {
			int perLevel = nbt.getInt(APIUtils.PER_LEVEL);
			int amplifier = nbt.getInt(APIUtils.MODIFIER);
			boolean ambient = nbt.getBoolean(APIUtils.AMBIENT);
			boolean visible = nbt.getBoolean(APIUtils.VISIBLE);
			player.addEffect(new MobEffectInstance(effect, perLevel * level, amplifier, ambient, visible));
		}
		return NONE;
	};
}
