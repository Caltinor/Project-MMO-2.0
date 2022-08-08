package harmonised.pmmo.core.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();

	private static Map<UUID, Long> regen_cooldown = new HashMap<>();
	private static Map<UUID, Long> breathe_cooldown = new HashMap<>();
	
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");	
	public static final TriFunction<Player, CompoundTag, Integer, CompoundTag> SPEED = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double maxSpeedBoost = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 1d;
		double boostPerLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.005;
		AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
		double speedBoost = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() 
							* Math.max(0, Math.min(maxSpeedBoost, Math.min(maxSpeedBoost, (level * boostPerLevel) / 100)));

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
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double maxDamage = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 1;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.005;
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
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.1;
		double maxReach = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 10d;
		double reach = -0.91 + (level * perLevel);
		reach = Math.min(maxReach, reach);
		reach = player.isCreative() ? Math.max(50, reach) : reach;		
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
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.05;
		int maxHeart	= nbt.contains(APIUtils.MAX_BOOST) ? nbt.getInt(APIUtils.MAX_BOOST) : 10;
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
		int min = nbt.contains(APIUtils.MIN_LEVEL) ? nbt.getInt(APIUtils.MIN_LEVEL) : 50;
		int duration = nbt.contains(APIUtils.DURATION) ? nbt.getInt(APIUtils.DURATION) : 100;
		if (level < min) return NONE;
		if (!player.hasEffect(MobEffects.NIGHT_VISION) || player.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 80) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0, true, false, false));
		}
		return NONE;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> REGEN = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		long cooldown = nbt.contains(APIUtils.COOLDOWN) ? nbt.getLong(APIUtils.COOLDOWN) : 300l;
		int duration = nbt.contains(APIUtils.DURATION) ? nbt.getInt(APIUtils.DURATION) : 1;
		double strength = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.02;
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
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.0005;
		double maxBoost = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 0.033;
        double jumpBoost;
        jumpBoost = -0.011 + level * perLevel;
        jumpBoost = Math.min(maxBoost, jumpBoost);
        player.setDeltaMovement(player.getDeltaMovement().add(0, jumpBoost, 0));
        player.hurtMarked = true; 
        return NONE;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> JUMP_SERVER = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.0005;
		double maxBoost = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 0.033;
        double jumpBoost;
        jumpBoost = -0.011 + level * perLevel;
        jumpBoost = Math.min(maxBoost, jumpBoost);
        CompoundTag output = new CompoundTag();
        output.putDouble(APIUtils.JUMP_OUT, player.getDeltaMovement().y + jumpBoost);
        return output;
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> BREATH = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		long cooldown = nbt.contains(APIUtils.COOLDOWN) ? nbt.getLong(APIUtils.COOLDOWN) : 300l;
		double strength = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 1d;
		int perLevel = Math.max(1, (int)((double)level * strength));
		long currentCD = breathe_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		int currentAir = player.getAirSupply();
		if (currentAir < 2 && (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis())) {
			player.setAirSupply(currentAir + perLevel);
			player.sendSystemMessage(Component.translatable("pmmo.perks.breathrefresh"));
			breathe_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return new CompoundTag();
	};
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> FALL_SAVE = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		CompoundTag output = new CompoundTag();
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.025;
		int saved = (int)(perLevel * (double)level);
		output.putInt("saved", saved);
		return output;
	};

	private static final String APPLICABLE_TO = "applies_to";
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> DAMAGE_BOOST = (player, nbt, level) -> {
		if (nbt.contains(APIUtils.MIN_LEVEL) && nbt.getInt(APIUtils.MIN_LEVEL) > level) return NONE;
		CompoundTag output = new CompoundTag();
		if (!nbt.contains(APIUtils.DAMAGE_IN) || !nbt.contains(APPLICABLE_TO)) return output;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.05;
		List<String> type = nbt.getList(APPLICABLE_TO, Tag.TAG_STRING).stream().map(tag -> tag.getAsString()).toList();
		if (!type.contains(RegistryUtil.getId(player.getMainHandItem()).toString())) return output;
		float modifier = 1f + (float)(perLevel * (double)level);
		float damage = nbt.getFloat(APIUtils.DAMAGE_IN) * modifier;
		output.putFloat(APIUtils.DAMAGE_OUT, damage);
		return output;
	};
}
