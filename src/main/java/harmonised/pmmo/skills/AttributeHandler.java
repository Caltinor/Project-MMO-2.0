package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class AttributeHandler
{
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");

	public static void updateHP(Mob mob, float bonus)
	{
		AttributeInstance hpAttribute = mob.getAttribute(Attributes.MAX_HEALTH);
		if(hpAttribute != null)
		{
			boolean wasMaxHealth = mob.getHealth() == mob.getMaxHealth();
			double maxMobHPBoost = Config.getConfig("maxMobHPBoost");
			double mobHPBoostPerPowerLevel = Config.getConfig("mobHPBoostPerPowerLevel");
			if(!(mob instanceof Animal))
				bonus *= mobHPBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier(mob, "hpBonus");

			if(bonus > maxMobHPBoost)
				bonus = (float) maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.addPermanentModifier(hpModifier);

			if(wasMaxHealth)
				mob.setHealth(mob.getMaxHealth());
		}
	}

	public static void updateDamage(Mob mob, float bonus)
	{
		AttributeInstance damageAttribute = mob.getAttribute(Attributes.ATTACK_DAMAGE);
		if(damageAttribute != null)
		{
			double maxMobDamageBoost = Config.getConfig("maxMobDamageBoost");
			double mobDamageBoostPerPowerLevel = Config.getConfig("mobDamageBoostPerPowerLevel");
			bonus *= mobDamageBoostPerPowerLevel;
			bonus *= getBiomeMobMultiplier(mob, "damageBonus");

			if(bonus > maxMobDamageBoost)
				bonus = (float) maxMobDamageBoost;

			AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);
			damageAttribute.removeModifier(damageModifierID);
			damageAttribute.addPermanentModifier(damageModifier);
		}
//		else
//			System.out.println(mob.getDisplayName().getString());
	}

	public static void updateSpeed(Mob mob, float bonus)
	{
		AttributeInstance speedAttribute = mob.getAttribute(Attributes.MOVEMENT_SPEED);
		if(speedAttribute != null)
		{
			if(!(mob instanceof  Animal))
			{
				double maxMobSpeedBoost = Config.getConfig("maxMobSpeedBoost");
				double mobSpeedBoostPerPowerLevel = Config.getConfig("mobSpeedBoostPerPowerLevel");
				bonus *= mobSpeedBoostPerPowerLevel;

				bonus *= getBiomeMobMultiplier(mob, "speedBonus");

				if(bonus > maxMobSpeedBoost)
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPermanentModifier(speedModifier);
			}
		}
	}

	private static double getBiomeMobMultiplier(Mob mob, String type)
	{
		Biome biome = mob.level.getBiome(new BlockPos(mob.position())).value();
		ResourceLocation biomeResLoc = biome.getRegistryName();
		double multiplier = 1;

		if(biomeResLoc != null)
		{
			String biomeKey = biome.getRegistryName().toString();
			Map<String, Double> theMap = JsonConfig.data.get(JType.BIOME_MOB_MULTIPLIER).get(biomeKey);


			if(theMap != null && theMap.containsKey(type))
				multiplier = theMap.get(type);
		}

		return multiplier;
	}
}