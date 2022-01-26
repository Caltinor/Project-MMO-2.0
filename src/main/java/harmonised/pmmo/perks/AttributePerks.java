package harmonised.pmmo.perks;

import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class AttributePerks {
	private static final String MAX_BOOST = "max_boost";
	private static final String PER_LEVEL = "per_level";
	private static final CompoundTag NONE = new CompoundTag();
	
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED = (player, nbt, level) -> {
		double maxSpeedBoost = nbt.contains(MAX_BOOST) ? nbt.getDouble(MAX_BOOST) : 1d;
		double boostPerLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.5;
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
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED_TERM = (p, nbt, l) -> {
		AttributeInstance speedAttribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
		speedAttribute.removeModifier(speedModifierID);
		return NONE;
	};
	
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE = (player, nbt, level) -> {
		double maxDamage = nbt.contains(MAX_BOOST) ? nbt.getDouble(MAX_BOOST) : 1;
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.05;
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		double damageBoost = Math.min(maxDamage, level * perLevel);
		AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.MULTIPLY_BASE);
		damageAttribute.removeModifier(damageModifierID);
		damageAttribute.addPermanentModifier(damageModifier);
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE_TERM = (player, nbt, level) -> {
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		damageAttribute.removeModifier(damageModifierID);
		return NONE;
	};
	
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REACH = (player, nbt, level) -> {
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.1;
		double maxReach = nbt.contains(MAX_BOOST) ? nbt.getDouble(MAX_BOOST) : 10d;
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
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REACH_TERM = (player, nbt, level) -> {
		AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		reachAttribute.removeModifier(reachModifierID);
		return NONE;
	};
	
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> HEALTH = (player, nbt, level) -> {
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.1;
		int maxHeart	= nbt.contains(MAX_BOOST) ? nbt.getInt(MAX_BOOST) : 10;
		int heartBoost = (int)(perLevel * (double)level);
		heartBoost = Math.min(maxHeart, heartBoost);		
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION);
		hpAttribute.removeModifier(hpModifierID);
		hpAttribute.addPermanentModifier(hpModifier);
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> HEALTH_TERM = (player, nbt, level) -> {
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		hpAttribute.removeModifier(hpModifierID);
		return NONE;
	};
}
