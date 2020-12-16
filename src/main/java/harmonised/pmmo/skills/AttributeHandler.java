package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");

	public static void updateAll( EntityPlayer player )
	{
		if( !player.world.isRemote )
			WorldTickHandler.updateVein( player, 0 );

		updateReach( player );
		updateHP( player );
		updateDamage( player );
	}

	public static double getReach( EntityPlayer player )
	{
		IAttributeInstance reachAttribute = player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE );

		if( reachAttribute.getModifier( reachModifierID ) == null )
			return ( reachAttribute.getBaseValue() );
		else
			return ( reachAttribute.getBaseValue() + reachAttribute.getModifier( reachModifierID ).getAmount() );
	}

	public static double getReachBoost( EntityPlayer player )
	{
		Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
		double buildLevel = Skill.BUILDING.getLevel( player );
		double reach = -0.91 + ( buildLevel / FConfig.getConfig( "levelsPerOneReach" ) );
		double maxReach = FConfig.getConfig( "maxExtraReachBoost" );
		double maxReachPref = maxReach;
		if( prefsMap.containsKey( "maxExtraReachBoost" ) )
			maxReachPref = prefsMap.get( "maxExtraReachBoost" );
		reach = Math.min( maxReach, Math.min( maxReachPref, reach ) );
		return reach;
	}

	public static void updateReach( EntityPlayer player )
	{
		double reach = getReachBoost( player );
		IAttributeInstance reachAttribute = player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE );
		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, 0 );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.applyModifier( reachModifier );
		}
	}

	public static double getBaseSpeed( EntityPlayer player )
	{
		return player.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MOVEMENT_SPEED ).getBaseValue();
	}

	public static double getSpeedBoost( EntityPlayer player )
	{
		int agilityLevel = Skill.AGILITY.getLevel( player );
		return getSpeedBoost( agilityLevel, getBaseSpeed( player ) );
	}

	public static double getSpeedBoost( int agilityLevel, double baseSpeed )
	{
		Map<String, Double> prefsMap = FConfig.getPreferencesMapOffline();
		Double maxSpeedBoostPref = null;
		if( prefsMap.containsKey( "maxSpeedBoost" ) )
			maxSpeedBoostPref = prefsMap.get( "maxSpeedBoost" );
		double speedBoost = agilityLevel * FConfig.getConfig( "speedBoostPerLevel" );
		double maxSpeed = baseSpeed * (FConfig.getConfig( "maxSpeedBoost" ) / 100);
		if( maxSpeedBoostPref != null && maxSpeed > baseSpeed * (maxSpeedBoostPref / 100) )
			maxSpeed = baseSpeed * (maxSpeedBoostPref / 100);

		if( speedBoost > maxSpeed )
			speedBoost = maxSpeed;

		return speedBoost;
	}

	public static int getHeartBoost( EntityPlayer player )
	{
		Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
		double enduranceLevel = Skill.ENDURANCE.getLevel( player );
		int heartBoost = (int) Math.floor( enduranceLevel / FConfig.getConfig( "levelsPerHeart" ) ) * 2;
		int maxHP = (int) FConfig.getConfig( "maxExtraHeartBoost" ) * 2;
		int maxHPPref = maxHP;
		if( prefsMap.containsKey( "maxExtraHeartBoost" ) )
			maxHPPref = (int) Math.floor( prefsMap.get( "maxExtraHeartBoost" ) * 2);
		heartBoost = Math.min( maxHP, Math.min( maxHPPref, heartBoost ) );
		return heartBoost;
	}

	public static void updateSpeed( EntityPlayer player )
	{
		IAttributeInstance speedAttribute = player.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MOVEMENT_SPEED );
		double speedBoost = getSpeedBoost( player );

		if( speedBoost > 0 )
		{
			if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
			{
				AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, 0 );
				speedAttribute.removeModifier( speedModifierID );
				speedAttribute.applyModifier( speedModifier );

//				System.out.println( speedModifier.getAmount() );
			}
		}
	}

	public static void resetSpeed( EntityPlayer player )
	{
		IAttributeInstance speedAttribute = player.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}

	public static void updateHP( EntityPlayer player )
	{
		int heartBoost = getHeartBoost( player );
		IAttributeInstance hpAttribute = player.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MAX_HEALTH );
		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, 0 );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.applyModifier( hpModifier );
	}

	public static void updateDamage( EntityPlayer player )
	{
		IAttributeInstance damageAttribute = player.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.ATTACK_DAMAGE );
		Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
		double maxDamage = FConfig.getConfig( "maxExtraDamageBoost" );
		double maxDamagePref = maxDamage;
		if( prefsMap.containsKey( "maxExtraDamageBoost" ) )
			maxDamagePref = prefsMap.get( "maxExtraDamageBoost" );
		double combatLevel = Skill.COMBAT.getLevel( player );
		double damageBoost = Math.min( maxDamage, Math.min( maxDamagePref, combatLevel / FConfig.getConfig( "levelsPerDamageMelee" ) ) );

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, 0 );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.applyModifier( damageModifier );
	}

	public static void updateHP( EntityMob mob, double bonus )
	{
		IAttributeInstance hpAttribute = mob.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MAX_HEALTH );
		if( hpAttribute != null )
		{
			boolean wasMaxHealth = mob.getHealth() == mob.getMaxHealth();
			double maxMobHPBoost = FConfig.getConfig( "maxMobHPBoost" );
			double mobHPBoostPerPowerLevel = FConfig.getConfig( "mobHPBoostPerPowerLevel" );

			bonus *= mobHPBoostPerPowerLevel;
			bonus *= getBiomeMobMultiplier( mob, "hpBonus" );

			if( bonus > maxMobHPBoost )
				bonus = maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, 0);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.applyModifier(hpModifier);

			if( wasMaxHealth )
				mob.setHealth( mob.getMaxHealth() );
		}
	}

	public static void updateDamage( EntityMob mob, double bonus )
	{
		IAttributeInstance damageAttribute = mob.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.ATTACK_DAMAGE );
		if( damageAttribute != null )
		{
			double maxMobDamageBoost = FConfig.getConfig( "maxMobDamageBoost" );
			double mobDamageBoostPerPowerLevel = FConfig.getConfig( "mobDamageBoostPerPowerLevel" );
			bonus *= mobDamageBoostPerPowerLevel;
			bonus *= getBiomeMobMultiplier( mob, "damageBonus" );

			if( bonus > maxMobDamageBoost )
				bonus = maxMobDamageBoost;

			AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Nearby Player Power Level", bonus, 0);
			damageAttribute.removeModifier(damageModifierID);
			damageAttribute.applyModifier(damageModifier);
		}
//		else
//			System.out.println( mob.getDisplayName().getUnformattedText() );
	}

	public static void updateSpeed( EntityMob mob, double bonus )
	{
		IAttributeInstance speedAttribute = mob.getAttributeMap().getAttributeInstance( SharedMonsterAttributes.MOVEMENT_SPEED );
		if( speedAttribute != null )
		{
			double maxMobSpeedBoost = FConfig.getConfig( "maxMobSpeedBoost" );
			double mobSpeedBoostPerPowerLevel = FConfig.getConfig( "mobSpeedBoostPerPowerLevel" );
			bonus *= mobSpeedBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "speedBonus" );

			if( bonus > maxMobSpeedBoost )
				bonus = maxMobSpeedBoost;

			AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, 0);
			speedAttribute.removeModifier(speedModifierID);
			speedAttribute.applyModifier(speedModifier);
		}
	}

	private static double getBiomeMobMultiplier( EntityMob mob, String type )
	{
		Biome biome = mob.world.getBiome( new BlockPos( mob.getPositionVector() ) );
		ResourceLocation biomeResLoc = biome.getRegistryName();
		double multiplier = 1;

		if( biomeResLoc != null )
		{
			String biomeKey = biome.getRegistryName().toString();
			Map<String, Double> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );


			if( theMap != null && theMap.containsKey( type ) )
				multiplier = theMap.get( type );
		}

		return multiplier;
	}
}