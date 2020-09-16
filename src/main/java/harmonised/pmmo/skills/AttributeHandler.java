package harmonised.pmmo.skills;

import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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
	private static double levelsPerOneReach;
	private static double levelsPerHeart;
	private static double levelsPerDamage;
	private static double maxSpeedBoost;
	private static double speedBoostPerLevel;
	private static int maxExtraHeartBoost;
	private static double maxExtraReachBoost;
	private static double maxExtraDamageBoost;

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
		ModifiableAttributeInstance reachAttribute = player.getAttribute( ForgeMod.REACH_DISTANCE.get() );
		
		if( reachAttribute.getModifier( reachModifierID ) == null )
			return ( reachAttribute.getBaseValue() );
		else
			return ( reachAttribute.getBaseValue() + reachAttribute.getModifier( reachModifierID ).getAmount() );
	}
	
	public static void updateReach( PlayerEntity player )
	{
		ModifiableAttributeInstance reachAttribute = player.getAttribute( ForgeMod.REACH_DISTANCE.get() );
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double buildLevel = Skill.BUILDING.getLevel( player );
		double reach = -0.91 + ( buildLevel / levelsPerOneReach );
		Double maxReachPref = null;
		if( prefsMap.containsKey( "maxExtraReachBoost" ) )
			maxReachPref = prefsMap.get( "maxExtraReachBoost" );
		if( reach > maxExtraReachBoost )
			reach = maxExtraReachBoost;
		if( maxReachPref != null && reach > maxReachPref )
			reach = maxReachPref;

		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID ).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.addPersistentModifier( reachModifier );
		}
	}

	public static double getBaseSpeed( PlayerEntity player )
	{
		return player.getAttribute( Attributes.GENERIC_MOVEMENT_SPEED ).getBaseValue();
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
		double speedBoost = agilityLevel * speedBoostPerLevel;
		double maxSpeed = baseSpeed * (maxSpeedBoost / 100);
		if( maxSpeedBoostPref != null && maxSpeed > baseSpeed * (maxSpeedBoostPref / 100) )
			maxSpeed = baseSpeed * (maxSpeedBoostPref / 100);

		if( speedBoost > maxSpeed )
			speedBoost = maxSpeed;

		return speedBoost;
	}
	
	public static void updateSpeed( PlayerEntity player )
	{
		ModifiableAttributeInstance speedAttribute = player.getAttribute( Attributes.GENERIC_MOVEMENT_SPEED );
		double speedBoost = getSpeedBoost( player );

		if( speedBoost > 0 )
		{
			if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
			{
				AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION );
				speedAttribute.removeModifier( speedModifierID );
				speedAttribute.addPersistentModifier( speedModifier );

//				System.out.println( speedModifier.getAmount() );
			}
		}
	}
	
	public static void resetSpeed( PlayerEntity player )
	{
		ModifiableAttributeInstance speedAttribute = player.getAttribute( Attributes.GENERIC_MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}
	
	public static void updateHP( PlayerEntity player )
	{
		ModifiableAttributeInstance hpAttribute = player.getAttribute( Attributes.GENERIC_MAX_HEALTH );
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		double enduranceLevel = Skill.ENDURANCE.getLevel( player );
		int heartBoost = (int) Math.floor( enduranceLevel / levelsPerHeart ) * 2;
		Integer maxHPPref = null;
		if( prefsMap.containsKey( "maxExtraHeartBoost" ) )
			maxHPPref = (int) Math.floor( prefsMap.get( "maxExtraHeartBoost" ) * 2);
		if( heartBoost > maxExtraHeartBoost * 2 )
			heartBoost = maxExtraHeartBoost * 2;
		if( maxHPPref != null && heartBoost > maxHPPref )
			heartBoost = maxHPPref;

		AttributeModifier hpModifier = new AttributeModifier( hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION );
		hpAttribute.removeModifier( hpModifierID );
		hpAttribute.addPersistentModifier( hpModifier );
	}
	
	public static void updateDamage( PlayerEntity player )
	{
		ModifiableAttributeInstance damageAttribute = player.getAttribute( Attributes.GENERIC_ATTACK_DAMAGE );
		Map<String, Double> prefsMap = Config.getPreferencesMap( player );
		Double maxDamagePref = null;
		if( prefsMap.containsKey( "maxExtraDamageBoost" ) )
			maxDamagePref = prefsMap.get( "maxExtraDamageBoost" );
		double combatLevel = Skill.COMBAT.getLevel( player );
		double damageBoost = combatLevel / levelsPerDamage;
		if( damageBoost > maxExtraDamageBoost )
			damageBoost = maxExtraDamageBoost;
		if( maxDamagePref != null && damageBoost > maxDamagePref )
			damageBoost = maxDamagePref;

		AttributeModifier damageModifier = new AttributeModifier( damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		damageAttribute.removeModifier( damageModifierID );
		damageAttribute.addPersistentModifier( damageModifier );
	}

	public static void updateHP( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance hpAttribute = mob.getAttribute( Attributes.GENERIC_MAX_HEALTH );
		if( hpAttribute != null )
		{
			boolean wasMaxHealth = mob.getHealth() == mob.getMaxHealth();
			double maxMobHPBoost = Config.forgeConfig.maxMobHPBoost.get();
			double mobHPBoostPerPowerLevel = Config.forgeConfig.mobHPBoostPerPowerLevel.get();
			if( !(mob instanceof AnimalEntity) )
				bonus *= mobHPBoostPerPowerLevel;

			bonus *= getBiomeMobMultiplier( mob, "hpBonus" );

			if( bonus > maxMobHPBoost )
				bonus = (float) maxMobHPBoost;

			AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);

			hpAttribute.removeModifier(hpModifierID);
			hpAttribute.addPersistentModifier(hpModifier);

			if( wasMaxHealth )
				mob.setHealth( mob.getMaxHealth() );
		}
	}

	public static void updateDamage( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance damageAttribute = mob.getAttribute( Attributes.GENERIC_ATTACK_DAMAGE );
		if( damageAttribute != null )
		{
			double maxMobDamageBoost = Config.forgeConfig.maxMobDamageBoost.get();
			double mobDamageBoostPerPowerLevel = Config.forgeConfig.mobDamageBoostPerPowerLevel.get();
			bonus *= mobDamageBoostPerPowerLevel;
			bonus *= getBiomeMobMultiplier( mob, "damageBonus" );

			if( bonus > maxMobDamageBoost )
				bonus = (float) maxMobDamageBoost;

			AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Nearby Player Power Level", bonus, AttributeModifier.Operation.ADDITION);
			damageAttribute.removeModifier(damageModifierID);
			damageAttribute.addPersistentModifier(damageModifier);
		}
//		else
//			System.out.println( mob.getDisplayName().getString() );
	}

	public static void updateSpeed( MobEntity mob, float bonus )
	{
		ModifiableAttributeInstance speedAttribute = mob.getAttribute( Attributes.GENERIC_MOVEMENT_SPEED );
		if( speedAttribute != null )
		{
			if( !(mob instanceof  AnimalEntity) )
			{
				double maxMobSpeedBoost = Config.forgeConfig.maxMobSpeedBoost.get();
				double mobSpeedBoostPerPowerLevel = Config.forgeConfig.mobSpeedBoostPerPowerLevel.get();
				bonus *= mobSpeedBoostPerPowerLevel;

				bonus *= getBiomeMobMultiplier( mob, "speedBonus" );

				if( bonus > maxMobSpeedBoost )
					bonus = (float) maxMobSpeedBoost;

				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Movement Speed Boost thanks to Nearby Player Power Level", bonus / 100, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPersistentModifier(speedModifier);
			}
		}
	}

	private static double getBiomeMobMultiplier( MobEntity mob, String type )
	{
		Biome biome = mob.world.getBiome( new BlockPos( mob.getPositionVec() ) );
		String biomeKey = XP.getBiomeResLoc( mob.world, biome ).toString();
		Map<String, Double> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );
		double multiplier = 1;

		if( theMap != null && theMap.containsKey( type ) )
			multiplier = (double) theMap.get( type );

//		if( multiplier != 1 )
//			System.out.println( type + " " + multiplier );

		return multiplier;
	}
}
