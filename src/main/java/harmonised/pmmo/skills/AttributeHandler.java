package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeMod;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString( "b20d3436-0d39-4868-96ab-d0a4856e68c6" );
	private static final UUID speedModifierID  = UUID.fromString( "d6103cbc-b90b-4c4b-b3c0-92701fb357b3" );
	private static final UUID hpModifierID     = UUID.fromString( "c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb" );
	private static final UUID damageModifierID = UUID.fromString( "992b11f1-7b3f-48d9-8ebd-1acfc3257b17" );

	public static void updateAll( Player player )
	{
		if( !player.level.isClientSide() )
			WorldTickHandler.updateVein( player, 0 );

		updateReach( player );
		updateHP( player );
		updateDamage( player );
	}

	public static double getReachBoost( Player player )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double buildLevel = Skill.getLevel( Skill.BUILDING.toString(), player );
		double reach = -0.91 + ( buildLevel / Config.getConfig( "levelsPerOneReach" ) );
		double maxReach = Config.getConfig( "maxExtraReachBoost" );
		double maxReachPref = maxReach;
		if( prefsMap.containsKey( "maxExtraReachBoost" ) )
			maxReachPref = prefsMap.get( "maxExtraReachBoost" );
		reach = Math.min( maxReach, Math.min( maxReachPref, reach ) );
		return player.isCreative() ? Math.max( 50, reach ) : reach;
	}

	public static void updateReach( Player player )
	{
		double reach = getReachBoost( player );
		AttributeInstance reachAttribute = player.getAttribute( ForgeMod.REACH_DISTANCE.get() );
		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.addPermanentModifier( reachModifier );
		}
	}

	public static double getBaseSpeed( Player player )
	{
		return player.getAttribute( Attributes.MOVEMENT_SPEED ).getBaseValue();
	}

	public static double getSpeedBoost( Player player )
	{
		int agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
		return getBaseSpeed( player ) * getSpeedBoostMultiplier( agilityLevel );
	}

	public static double getSpeedBoostMultiplier( int agilityLevel )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMapOffline();
		double maxSpeedBoost = Config.getConfig( "maxSpeedBoost" ) / 100;
		double maxSpeedBoostPref = maxSpeedBoost;
		if( prefsMap.containsKey( "maxSpeedBoost" ) )
			maxSpeedBoostPref = prefsMap.get( "maxSpeedBoost" ) / 100;

		return Math.max( 0, Math.min( maxSpeedBoost, Math.min( maxSpeedBoostPref, ( agilityLevel * Config.getConfig( "speedBoostPerLevel" ) ) / 100 ) ) );
	}

	public static int getHeartBoost( Player player )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double enduranceLevel = Skill.getLevel( Skill.ENDURANCE.toString(), player );
		int heartBoost = (int) Math.floor( enduranceLevel / Config.getConfig( "levelsPerHeart" ) ) * 2;
		int maxHP = (int) Config.getConfig( "maxExtraHeartBoost" ) * 2;
		int maxHPPref = maxHP;
		if( prefsMap.containsKey( "maxExtraHeartBoost" ) )
			maxHPPref = (int) Math.floor( prefsMap.get( "maxExtraHeartBoost" ) * 2);
		heartBoost = Math.min( maxHP, Math.min( maxHPPref, heartBoost ) );
		return heartBoost;
	}

	public static void updateSpeed( Player player )
	{
		AttributeInstance speedAttribute = player.getAttribute( Attributes.MOVEMENT_SPEED );
		double speedBoost = getSpeedBoost( player );

		if( speedBoost > 0 )
		{
			if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
			{
				AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION );
				speedAttribute.removeModifier( speedModifierID );
				speedAttribute.addPermanentModifier( speedModifier );

//				System.out.println( speedModifier.getAmount() );
			}
		}
	}

	public static void resetSpeed( Player player )
	{
		AttributeInstance speedAttribute = player.getAttribute( Attributes.MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}

	public static void updateHP( Player player )
	{
		int heartBoost = getHeartBoost( player );
		AttributeInstance hpAttribute = player.getAttribute( Attributes.MAX_HEALTH );
		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.addPermanentModifier( hpModifier );
	}

	public static void updateDamage( Player player )
	{
		AttributeInstance damageAttribute = player.getAttribute( Attributes.ATTACK_DAMAGE );
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double maxDamage = Config.getConfig( "maxExtraDamagePercentageBoostMelee" );
		double maxDamagePref = maxDamage;
		if( prefsMap.containsKey( "maxExtraDamagePercentageBoostMelee" ) )
			maxDamagePref = prefsMap.get( "maxExtraDamagePercentageBoostMelee" );
		double combatLevel = Skill.getLevel( Skill.COMBAT.toString(), player );
		double damageBoost = Math.min( maxDamage, Math.min( maxDamagePref, combatLevel * Config.getConfig( "damageBonusPercentPerLevelMelee" ) ) );

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.MULTIPLY_BASE );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.addPermanentModifier( damageModifier );
	}

	public static void updateHP( Mob mob, float bonus )
	{
		AttributeInstance hpAttribute = mob.getAttribute( Attributes.MAX_HEALTH );
		if( hpAttribute != null )
		{
			boolean wasMaxHealth = mob.getHealth() == mob.getMaxHealth();
			double maxMobHPBoost = Config.getConfig( "maxMobHPBoost" );
			double mobHPBoostPerPowerLevel = Config.getConfig( "mobHPBoostPerPowerLevel" );
			if( !(mob instanceof Animal) )
				bonus *= mobHPBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "hpBonus" );

			if( bonus > maxMobHPBoost )
				bonus = (float) maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.addPermanentModifier(hpModifier);

			if( wasMaxHealth )
				mob.setHealth( mob.getMaxHealth() );
		}
	}

	public static void updateDamage( Mob mob, float bonus )
	{
		AttributeInstance damageAttribute = mob.getAttribute( Attributes.ATTACK_DAMAGE );
		if( damageAttribute != null )
		{
			double maxMobDamageBoost = Config.getConfig( "maxMobDamageBoost" );
			double mobDamageBoostPerPowerLevel = Config.getConfig( "mobDamageBoostPerPowerLevel" );
			bonus *= mobDamageBoostPerPowerLevel;
			bonus *= getBiomeMobMultiplier( mob, "damageBonus" );

			if( bonus > maxMobDamageBoost )
				bonus = (float) maxMobDamageBoost;

			AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);
			damageAttribute.removeModifier(damageModifierID);
			damageAttribute.addPermanentModifier(damageModifier);
		}
//		else
//			System.out.println( mob.getDisplayName().getString() );
	}

	public static void updateSpeed( Mob mob, float bonus )
	{
		AttributeInstance speedAttribute = mob.getAttribute( Attributes.MOVEMENT_SPEED );
		if( speedAttribute != null )
		{
			if( !(mob instanceof  Animal) )
			{
				double maxMobSpeedBoost = Config.getConfig( "maxMobSpeedBoost" );
				double mobSpeedBoostPerPowerLevel = Config.getConfig( "mobSpeedBoostPerPowerLevel" );
				bonus *= mobSpeedBoostPerPowerLevel;

				bonus *= getBiomeMobMultiplier( mob, "speedBonus" );

				if( bonus > maxMobSpeedBoost )
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPermanentModifier(speedModifier);
			}
		}
	}

	private static double getBiomeMobMultiplier( Mob mob, String type )
	{
		Biome biome = mob.level.getBiome( new BlockPos( mob.position() ) );
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