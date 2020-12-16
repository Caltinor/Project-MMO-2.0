package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ForgeMod;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");

	public static void updateAll( PlayerEntity player )
	{
		if( !player.world.isRemote() )
			WorldTickHandler.updateVein( player, 0 );

		updateReach( player );
		updateHP( player );
		updateDamage( player );
	}

	public static double getReachBoost( PlayerEntity player )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double buildLevel = Skill.BUILDING.getLevel( player );
		double reach = -0.91 + ( buildLevel / Config.getConfig( "levelsPerOneReach" ) );
		double maxReach = Config.getConfig( "maxExtraReachBoost" );
		double maxReachPref = maxReach;
		if( prefsMap.containsKey( "maxExtraReachBoost" ) )
			maxReachPref = prefsMap.get( "maxExtraReachBoost" );
		reach = Math.min( maxReach, Math.min( maxReachPref, reach ) );
		return reach;
	}

	public static void updateReach( PlayerEntity player )
	{
		double reach = getReachBoost( player );
		ModifiableAttributeInstance reachAttribute = player.getAttribute( ForgeMod.REACH_DISTANCE.get() );
		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.applyPersistentModifier( reachModifier );
		}
	}

	public static double getBaseSpeed( PlayerEntity player )
	{
		return player.getAttribute( Attributes.MOVEMENT_SPEED ).getBaseValue();
	}

	public static double getSpeedBoost( PlayerEntity player )
	{
		int agilityLevel = Skill.AGILITY.getLevel( player );
		return getSpeedBoost( agilityLevel, getBaseSpeed( player ) );
	}

	public static double getSpeedBoost( int agilityLevel, double baseSpeed )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMapOffline();
		Double maxSpeedBoostPref = null;
		if( prefsMap.containsKey( "maxSpeedBoost" ) )
			maxSpeedBoostPref = prefsMap.get( "maxSpeedBoost" );
		double speedBoost = agilityLevel * Config.getConfig( "speedBoostPerLevel" );
		double maxSpeed = baseSpeed * (Config.getConfig( "maxSpeedBoost" ) / 100);
		if( maxSpeedBoostPref != null && maxSpeed > baseSpeed * (maxSpeedBoostPref / 100) )
			maxSpeed = baseSpeed * (maxSpeedBoostPref / 100);

		if( speedBoost > maxSpeed )
			speedBoost = maxSpeed;

		return speedBoost;
	}

	public static int getHeartBoost( PlayerEntity player )
	{
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double enduranceLevel = Skill.ENDURANCE.getLevel( player );
		int heartBoost = (int) Math.floor( enduranceLevel / Config.getConfig( "levelsPerHeart" ) ) * 2;
		int maxHP = (int) Config.getConfig( "maxExtraHeartBoost" ) * 2;
		int maxHPPref = maxHP;
		if( prefsMap.containsKey( "maxExtraHeartBoost" ) )
			maxHPPref = (int) Math.floor( prefsMap.get( "maxExtraHeartBoost" ) * 2);
		heartBoost = Math.min( maxHP, Math.min( maxHPPref, heartBoost ) );
		return heartBoost;
	}

	public static void updateSpeed( PlayerEntity player )
	{
		ModifiableAttributeInstance speedAttribute = player.getAttribute( Attributes.MOVEMENT_SPEED );
		double speedBoost = getSpeedBoost( player );

		if( speedBoost > 0 )
		{
			if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
			{
				AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION );
				speedAttribute.removeModifier( speedModifierID );
				speedAttribute.applyPersistentModifier( speedModifier );

//				System.out.println( speedModifier.getAmount() );
			}
		}
	}

	public static void resetSpeed( PlayerEntity player )
	{
		ModifiableAttributeInstance speedAttribute = player.getAttribute( Attributes.MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}

	public static void updateHP( PlayerEntity player )
	{
		int heartBoost = getHeartBoost( player );
		ModifiableAttributeInstance hpAttribute = player.getAttribute( Attributes.MAX_HEALTH );
		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.applyPersistentModifier( hpModifier );
	}

	public static void updateDamage( PlayerEntity player )
	{
		ModifiableAttributeInstance damageAttribute = player.getAttribute( Attributes.ATTACK_DAMAGE );
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double maxDamage = Config.getConfig( "maxExtraDamageBoost" );
		double maxDamagePref = maxDamage;
		if( prefsMap.containsKey( "maxExtraDamageBoost" ) )
			maxDamagePref = prefsMap.get( "maxExtraDamageBoost" );
		double combatLevel = Skill.COMBAT.getLevel( player );
		double damageBoost = Math.min( maxDamage, Math.min( maxDamagePref, combatLevel / Config.getConfig( "levelsPerDamageMelee" ) ) );

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.applyPersistentModifier( damageModifier );
	}

	public static void updateHP( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance hpAttribute = mob.getAttribute( Attributes.MAX_HEALTH );
		if( hpAttribute != null )
		{
			boolean wasMaxHealth = mob.getHealth() == mob.getMaxHealth();
			double maxMobHPBoost = Config.getConfig( "maxMobHPBoost" );
			double mobHPBoostPerPowerLevel = Config.getConfig( "mobHPBoostPerPowerLevel" );
			if( !(mob instanceof AnimalEntity) )
				bonus *= mobHPBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "hpBonus" );

			if( bonus > maxMobHPBoost )
				bonus = (float) maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.applyPersistentModifier(hpModifier);

			if( wasMaxHealth )
				mob.setHealth( mob.getMaxHealth() );
		}
	}

	public static void updateDamage( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance damageAttribute = mob.getAttribute( Attributes.ATTACK_DAMAGE );
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
			damageAttribute.applyPersistentModifier(damageModifier);
		}
//		else
//			System.out.println( mob.getDisplayName().getString() );
	}

	public static void updateSpeed( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance speedAttribute = mob.getAttribute( Attributes.MOVEMENT_SPEED );
		if( speedAttribute != null )
		{
			if( !(mob instanceof  AnimalEntity) )
			{
				double maxMobSpeedBoost = Config.getConfig( "maxMobSpeedBoost" );
				double mobSpeedBoostPerPowerLevel = Config.getConfig( "mobSpeedBoostPerPowerLevel" );
				bonus *= mobSpeedBoostPerPowerLevel;

				bonus *= getBiomeMobMultiplier( mob, "speedBonus" );

				if( bonus > maxMobSpeedBoost )
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.applyPersistentModifier(speedModifier);
			}
		}
	}

	private static double getBiomeMobMultiplier( MobEntity mob, String type )
	{
		Biome biome = mob.world.getBiome( new BlockPos( mob.getPositionVec() ) );
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