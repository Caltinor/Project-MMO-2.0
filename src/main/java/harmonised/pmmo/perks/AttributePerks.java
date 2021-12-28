package harmonised.pmmo.perks;

import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.skills.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class AttributePerks {
	private static final String MAX_BOOST = "max";
	private static final String PER_LEVEL = "power";
	
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED = (p, nbt, l) -> {
		double maxSpeedBoost = nbt.getDouble(MAX_BOOST);
		double boostPerLevel = nbt.getDouble(PER_LEVEL);
		AttributeInstance speedAttribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
		int agilityLevel = APIUtils.getLevel(Skill.AGILITY.toString(), p);
		double speedBoost = p.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() 
							* Math.max(0, Math.min(maxSpeedBoost, Math.min(maxSpeedBoost, (agilityLevel * boostPerLevel) / 100)));

		if(speedBoost > 0)
		{
			if(speedAttribute.getModifier(speedModifierID) == null || speedAttribute.getModifier(speedModifierID).getAmount() != speedBoost)
			{
				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPermanentModifier(speedModifier);
			}
		}
		return new CompoundTag();
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED_TERM = (p, nbt, l) -> {
		AttributeInstance speedAttribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
		speedAttribute.removeModifier(speedModifierID);
		return new CompoundTag();
	};
	
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE = (p, nbt, l) -> {
		double maxDamage = nbt.getDouble(MAX_BOOST);
		double perLevel = nbt.getDouble(PER_LEVEL);
		AttributeInstance damageAttribute = p.getAttribute(Attributes.ATTACK_DAMAGE);
		double combatLevel = APIUtils.getLevel(Skill.COMBAT.toString(), p);
		double damageBoost = Math.min(maxDamage, combatLevel * perLevel);

		AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.MULTIPLY_BASE);
		damageAttribute.removeModifier(damageModifierID);
		damageAttribute.addPermanentModifier(damageModifier);
		return new CompoundTag();
	};
	
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REACH = (p, nbt, l) -> {
		double perLevel = nbt.getDouble(PER_LEVEL);
		double maxReach = nbt.getDouble(MAX_BOOST);
		double buildLevel = APIUtils.getLevel(Skill.BUILDING.toString(), p);
		double reach = -0.91 + (buildLevel / perLevel);
		reach = Math.min(maxReach, reach);
		reach = p.isCreative() ? Math.max(50, reach) : reach;		
		AttributeInstance reachAttribute = p.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if(reachAttribute.getModifier(reachModifierID) == null || reachAttribute.getModifier(reachModifierID).getAmount() != reach)
		{
			AttributeModifier reachModifier = new AttributeModifier(reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION);
			reachAttribute.removeModifier(reachModifierID);
			reachAttribute.addPermanentModifier(reachModifier);
		}
		return new CompoundTag();
	};
	
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> HEALTH = (p, nbt, l) -> {
		double perLevel = nbt.getDouble(PER_LEVEL);
		double maxHeart	= nbt.getDouble(MAX_BOOST);
		double enduranceLevel = APIUtils.getLevel(Skill.ENDURANCE.toString(), p);
		int heartBoost = (int) Math.floor(enduranceLevel / perLevel) * 2;
		int maxHP = (int) maxHeart * 2;
		heartBoost = Math.min(maxHP, heartBoost);

		AttributeInstance hpAttribute = p.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION);
		hpAttribute.removeModifier(hpModifierID);
		hpAttribute.addPermanentModifier(hpModifier);
		return new CompoundTag();
	};
}
