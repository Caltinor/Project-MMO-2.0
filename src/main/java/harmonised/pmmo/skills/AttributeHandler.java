package harmonised.pmmo.skills;

import java.util.UUID;

import harmonised.pmmo.config.Config;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID HPModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID DamageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	private static int levelsPerBlockReach = Config.config.levelsPerBlockReach.get();
	private static int levelsPerHeart = Config.config.levelsPerHeart.get();
	private static int levelsPerDamage = Config.config.levelsPerDamage.get();
	private static double speedBoostMax = Config.config.speedBoostMax.get();
	private static double speedBoostPerLevel = Config.config.speedBoostPerLevel.get();
	private static int maxHeartCap = Config.config.maxHeartCap.get();
	private static double maxReach = Config.config.maxReach.get();
	private static double maxDamage = Config.config.maxDamage.get();

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
		double speedBoostMaxPref = prefsTag.getDouble( "speedBoostMax" );
		double speedBoost = agilityLevel * speedBoostPerLevel;
		double baseValue = speedAttribute.getBaseValue();
		double maxSpeed = baseValue * (speedBoostMax / 100);
		if( maxSpeed > baseValue * (speedBoostMaxPref / 100) && prefsTag.contains( "speedBoostMax" ) )
			maxSpeed = baseValue * (speedBoostMaxPref / 100);

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
		IAttributeInstance HPAttribute = player.getAttribute( SharedMonsterAttributes.MAX_HEALTH );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double enduranceLevel = XP.getLevel( "endurance", player );
		int maxHP = (int) Math.floor( enduranceLevel / levelsPerHeart ) * 2;
		int maxHPPref = (int) Math.floor(prefsTag.getDouble( "maxExtraHeart" ) * 2);
		if( maxHP > maxHeartCap * 2 )
			maxHP = maxHeartCap * 2;
		if( maxHP > maxHPPref && prefsTag.contains( "maxExtraHeart" ) )
			maxHP = maxHPPref;

		AttributeModifier HPModifier = new AttributeModifier( HPModifierID, "Max HP Bonus thanks to Endurance Level", maxHP, AttributeModifier.Operation.ADDITION );
		HPAttribute.removeModifier( HPModifierID );
		HPAttribute.applyModifier( HPModifier );
	}
	
	public static void updateDamage( PlayerEntity player )
	{
		IAttributeInstance DamageAttribute = player.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double maxDamagePref = prefsTag.getDouble( "maxExtraDamageBoost" );
		double combatLevel = XP.getLevel( "combat", player );
		double damageBoost = combatLevel / levelsPerDamage;
		if( damageBoost > maxDamage )
			damageBoost = maxDamage;
		if( damageBoost > maxDamagePref && prefsTag.contains( "maxExtraDamageBoost" ) )
			damageBoost = maxDamagePref;

		AttributeModifier damageModifier = new AttributeModifier( DamageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		DamageAttribute.removeModifier( DamageModifierID );
		DamageAttribute.applyModifier( damageModifier );
	}
}
