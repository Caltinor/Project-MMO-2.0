package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.util.XP;
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
	private static double levelsPerOneReach;
	private static double levelsPerHeart;
	private static double levelsPerDamage;
	private static double maxSpeedBoost;
	private static double speedBoostPerLevel;
	private static int maxExtraHeartBoost;
	private static double maxExtraReachBoost;
	private static double maxExtraDamageBoost;
	private static final double maxMobSpeedBoost = Config.forgeConfig.maxMobSpeedBoost.get();
	private static final double mobSpeedBoostPerPowerLevel = Config.forgeConfig.mobSpeedBoostPerPowerLevel.get();
	private static final double maxMobHPBoost = Config.forgeConfig.maxMobHPBoost.get();
	private static final double mobHPBoostPerPowerLevel = Config.forgeConfig.mobHPBoostPerPowerLevel.get();
	private static final double maxMobDamageBoost = Config.forgeConfig.maxMobDamageBoost.get();
	private static final double mobDamageBoostPerPowerLevel = Config.forgeConfig.mobDamageBoostPerPowerLevel.get();

	public static void init()
	{
		levelsPerOneReach = Config.getConfig( "levelsPerOneReach" );
		levelsPerHeart = Config.getConfig( "levelsPerHeart" );
		levelsPerDamage = Config.getConfig( "levelsPerDamage" );
		maxSpeedBoost = Config.getConfig( "maxSpeedBoost" );
		speedBoostPerLevel = Config.getConfig( "speedBoostPerLevel" );
		maxExtraHeartBoost = (int) Config.getConfig( "maxExtraHeartBoost" );
		maxExtraReachBoost = Config.getConfig( "maxExtraReachBoost" );
		maxExtraDamageBoost = Config.getConfig( "maxExtraDamageBoost" );
	}

	public static void updateAll( PlayerEntity player )
	{
		if( !player.world.isRemote() )
			WorldTickHandler.updateVein( player, 0 );

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
		double buildLevel = XP.getLevel( Skill.BUILDING, player );
		double reach = -0.91 + ( buildLevel / levelsPerOneReach );
		double maxReachPref = prefsTag.getDouble( "maxExtraReachBoost" );
		if( reach > maxExtraReachBoost )
			reach = maxExtraReachBoost;
		if( reach > maxReachPref && prefsTag.contains( "maxExtraReachBoost" ) )
			reach = maxReachPref;

		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.applyModifier( reachModifier );
		}
	}

	public static double getBaseSpeed( PlayerEntity player )
	{
		return player.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED ).getBaseValue();
	}

	public static double getSpeedBoost( PlayerEntity player )
	{
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double agilityLevel = XP.getLevel( Skill.AGILITY, player );
		double maxSpeedBoostPref = prefsTag.getDouble( "maxSpeedBoost" );
		double speedBoost = agilityLevel * speedBoostPerLevel;
		double baseValue = getBaseSpeed( player );
		double maxSpeed = baseValue * (maxSpeedBoost / 100);
		if( maxSpeed > baseValue * (maxSpeedBoostPref / 100) && prefsTag.contains( "maxSpeedBoost" ) )
			maxSpeed = baseValue * (maxSpeedBoostPref / 100);

		if( speedBoost > maxSpeed )
			speedBoost = maxSpeed;

		return speedBoost;
	}
	
	public static void updateSpeed( PlayerEntity player )
	{
		IAttributeInstance speedAttribute = player.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		double speedBoost = getSpeedBoost( player );

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
		double enduranceLevel = XP.getLevel( Skill.ENDURANCE, player );
		int heartBoost = (int) Math.floor( enduranceLevel / levelsPerHeart ) * 2;
		int maxHPPref = (int) Math.floor(prefsTag.getDouble( "maxExtraHeartBoost" ) * 2);
		if( heartBoost > maxExtraHeartBoost * 2 )
			heartBoost = maxExtraHeartBoost * 2;
		if( heartBoost > maxHPPref && prefsTag.contains( "maxExtraHeartBoost" ) )
			heartBoost = maxHPPref;

		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.applyModifier( hpModifier );
	}
	
	public static void updateDamage( PlayerEntity player )
	{
		IAttributeInstance damageAttribute = player.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		CompoundNBT prefsTag = XP.getPreferencesTag( player );
		double maxDamagePref = prefsTag.getDouble( "maxExtraDamageBoost" );
		double combatLevel = XP.getLevel( Skill.COMBAT, player );
		double damageBoost = combatLevel / levelsPerDamage;
		if( damageBoost > maxExtraDamageBoost )
			damageBoost = maxExtraDamageBoost;
		if( damageBoost > maxDamagePref && prefsTag.contains( "maxExtraDamageBoost" ) )
			damageBoost = maxDamagePref;

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.applyModifier( damageModifier );
	}

	public static void updateHP( MobEntity mob, float bonus )
	{
//		System.out.println( mob.getDisplayName().getString() );
		IAttributeInstance hpAttribute = mob.getAttribute( SharedMonsterAttributes.MAX_HEALTH );
		if( hpAttribute != null )
		{
			if( !(mob instanceof AnimalEntity) )
				bonus *= mobHPBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "hpBonus" );

			if( bonus > maxMobHPBoost )
				bonus = (float) maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.applyModifier(hpModifier);

//			mob.setHealth( newHealth );
		}
	}

	public static void updateDamage( MobEntity mob, float bonus )
	{
		IAttributeInstance damageAttribute = mob.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		if( damageAttribute != null )
		{
//			System.out.println( "damage boost " + bonus / damageAttribute.getBaseValue() + " " + bonus + " " + damageAttribute.getBaseValue() );
			bonus *= mobDamageBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "damageBonus" );

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

				bonus *= getBiomeMobMultiplier( mob, "speedBonus" );

				if( bonus > maxMobSpeedBoost )
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.applyModifier(speedModifier);
			}
		}
	}

	private static double getBiomeMobMultiplier( MobEntity mob, String type )
	{
		String biomeKey = mob.world.getBiome( mob.getPosition() ).getRegistryName().toString();
		Map<String, Object> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );
		double multiplier = 1;

		if( theMap != null && theMap.containsKey( type ) )
			multiplier = (double) theMap.get( type );

//		if( multiplier != 1 )
//			System.out.println( type + " " + multiplier );

		return multiplier;
	}
}
