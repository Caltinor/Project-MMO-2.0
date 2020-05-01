package harmonised.pmmo.skills;

import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.DP;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	private static int levelsPerBlockReach = Config.config.levelsPerBlockReach.get();
	private static int levelsPerHeart = Config.config.levelsPerHeart.get();
	private static int levelsPerDamage = Config.config.levelsPerDamage.get();
	private static double maxSpeedBoost = Config.config.maxSpeedBoost.get();
	private static double speedBoostPerLevel = Config.config.speedBoostPerLevel.get();
	private static int maxHeartCap = Config.config.maxHeartCap.get();
	private static double maxReach = Config.config.maxReach.get();
	private static double maxDamage = Config.config.maxDamage.get();
	private static double maxMobSpeedBoost = Config.config.maxMobSpeedBoost.get();
	private static double mobSpeedBoostPerPowerLevel = Config.config.mobSpeedBoostPerPowerLevel.get();
	private static double maxMobHPBoost = Config.config.maxMobHPBoost.get();
	private static double mobHPBoostPerPowerLevel = Config.config.mobHPBoostPerPowerLevel.get();
	private static double maxMobDamageBoost = Config.config.maxMobDamageBoost.get();
	private static double mobDamageBoostPerPowerLevel = Config.config.mobDamageBoostPerPowerLevel.get();


	public static void updateAll( PlayerEntity player )
	{
		updateReach( player );
		updateHP( player );
		updateDamage( player );
	}

	public static double getReach( PlayerEntity player )
	{
		IAttributeInstance reachAttribute = player.getAttribute( player.REACH_DISTANCE );
		
		if( reachAttribute.getModifier( reachModifierID ) == null )
			return ( reachAttribute.getBaseValue() );
		else
			return ( reachAttribute.getBaseValue() + reachAttribute.getModifier( reachModifierID ).getAmount() );
	}
	
	public static void updateReach( PlayerEntity player )
	{
		IAttributeInstance reachAttribute = player.getAttribute( player.REACH_DISTANCE );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double buildLevel = XP.getLevel( "building", player );
		double reach = -0.91 + ( buildLevel / levelsPerBlockReach );
		double maxReachPref = prefsTag.getDouble( "maxReach" );
		if( reach > maxReach )
			reach = maxReach;
		if( reach > maxReachPref && prefsTag.contains( "maxReach" ) )
			reach = maxReachPref;

		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.applyModifier( reachModifier );
		}
	}
	
	public static void updateSpeed( PlayerEntity player )
	{
		IAttributeInstance speedAttribute = player.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double agilityLevel = XP.getLevel( "agility", player );
		double maxSpeedBoostPref = prefsTag.getDouble( "maxSpeedBoost" );
		double speedBoost = agilityLevel * speedBoostPerLevel;
		double baseValue = speedAttribute.getBaseValue();
		double maxSpeed = baseValue * (maxSpeedBoost / 100);
		if( maxSpeed > baseValue * (maxSpeedBoostPref / 100) && prefsTag.contains( "maxSpeedBoost" ) )
			maxSpeed = baseValue * (maxSpeedBoostPref / 100);

		if( speedBoost > maxSpeed )
			speedBoost = maxSpeed;

		if( speedBoost > 0 )
		{
			if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
			{
				AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION );
				speedAttribute.removeModifier( speedModifierID );
				speedAttribute.applyModifier( speedModifier );

//				System.out.println( speedModifier.getAmount() );
			}
		}
	}
	
	public static void resetSpeed( PlayerEntity player )
	{
		IAttributeInstance speedAttribute = player.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}
	
	public static void updateHP( PlayerEntity player )
	{
		IAttributeInstance hpAttribute = player.getAttribute( SharedMonsterAttributes.MAX_HEALTH );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double enduranceLevel = XP.getLevel( "endurance", player );
		int maxHP = (int) Math.floor( enduranceLevel / levelsPerHeart ) * 2;
		int maxHPPref = (int) Math.floor(prefsTag.getDouble( "maxExtraHeart" ) * 2);
		if( maxHP > maxHeartCap * 2 )
			maxHP = maxHeartCap * 2;
		if( maxHP > maxHPPref && prefsTag.contains( "maxExtraHeart" ) )
			maxHP = maxHPPref;

		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", maxHP, AttributeModifier.Operation.ADDITION );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.applyModifier( hpModifier );
	}
	
	public static void updateDamage( PlayerEntity player )
	{
		IAttributeInstance damageAttribute = player.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double maxDamagePref = prefsTag.getDouble( "maxExtraDamageBoost" );
		double combatLevel = XP.getLevel( "combat", player );
		double damageBoost = combatLevel / levelsPerDamage;
		if( damageBoost > maxDamage )
			damageBoost = maxDamage;
		if( damageBoost > maxDamagePref && prefsTag.contains( "maxExtraDamageBoost" ) )
			damageBoost = maxDamagePref;

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.applyModifier( damageModifier );
	}

	public static void updateHP( MobEntity mob, float bonus )
	{
		IAttributeInstance hpAttribute = mob.getAttribute( SharedMonsterAttributes.MAX_HEALTH );
		if( hpAttribute != null )
		{
			if( mob instanceof AnimalEntity )
				bonus = 1;
			else
				bonus *= mobHPBoostPerPowerLevel;

			if( bonus > maxMobHPBoost )
				bonus = (float) maxMobHPBoost;
//			System.out.println( "hp boost " + DP.dp( bonus / hpAttribute.getBaseValue() ) + " " + bonus + " " + hpAttribute.getBaseValue() + " " + mob.getDisplayName().getString() );

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);
			boolean wasHealed = hpAttribute.hasModifier( hpModifier );

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.applyModifier(hpModifier);

			if( !wasHealed )
				mob.setHealth( mob.getHealth() + bonus );
//			System.out.println( mob.getHealth() );
		}
	}

	public static void updateDamage( MobEntity mob, float bonus )
	{
		IAttributeInstance damageAttribute = mob.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		if( damageAttribute != null )
		{
//			System.out.println( "damage boost " + bonus / damageAttribute.getBaseValue() + " " + bonus + " " + damageAttribute.getBaseValue() );
			bonus *= mobDamageBoostPerPowerLevel;

			if( bonus > maxMobDamageBoost )
				bonus = (float) maxMobDamageBoost;

			AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);
			damageAttribute.removeModifier(damageModifierID);
			damageAttribute.applyModifier(damageModifier);
		}
//		else
//			System.out.println( mob.getDisplayName().getString() );
	}

	public static void updateSpeed( MobEntity mob, float bonus )
	{
		IAttributeInstance speedAttribute = mob.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		if( speedAttribute != null )
		{
			if( !(mob instanceof  AnimalEntity) )
			{
				bonus *= mobSpeedBoostPerPowerLevel;

				if( bonus > maxMobSpeedBoost )
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.applyModifier(speedModifier);
			}
		}
	}
}
