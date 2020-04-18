package harmonised.pmmo.skills;

import java.util.UUID;

import harmonised.pmmo.config.Config;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID HPModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID DamageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	private static int levelsPerBlockReach = Config.config.levelsPerBlockReach.get();
	private static int levelsPerHeart = Config.config.levelsPerHeart.get();
	private static int levelsPerDamage = Config.config.levelsPerDamage.get();
	private static double maxSpeedBoost = Config.config.maxSpeedBoost.get();
	private static double speedBoostPerLevel = Config.config.speedBoostPerLevel.get();
	private static int maxHeartCap = Config.config.maxHeartCap.get();

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
		float buildLevel = XP.getLevel( "building", player );

		if( buildLevel == 1 )
			return;

		double reach = -0.91 + ( buildLevel / levelsPerBlockReach );
		
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
		float agilityLevel = XP.getLevel( "agility", player );
		double speedBoost = agilityLevel * speedBoostPerLevel;
		if( speedBoost > maxSpeedBoost )
			speedBoost = maxSpeedBoost;
		if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
		{
			AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION );
			speedAttribute.removeModifier( speedModifierID );
			speedAttribute.applyModifier( speedModifier );
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
		float enduranceLevel = XP.getLevel( "endurance", player );
		int maxHP = (int) Math.floor( enduranceLevel / levelsPerHeart ) * 2;
		if( maxHP > maxHeartCap * 2 )
			maxHP = maxHeartCap;
		AttributeModifier HPModifier = new AttributeModifier( HPModifierID, "Max HP Bonus thanks to Endurance Level", maxHP, AttributeModifier.Operation.ADDITION );
		HPAttribute.removeModifier( HPModifierID );
		HPAttribute.applyModifier( HPModifier );
	}
	
	public static void updateDamage( PlayerEntity player )
	{
		IAttributeInstance DamageAttribute = player.getAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		float combatLevel = XP.getLevel( "combat", player );
		int damageBoost = (int) Math.floor( combatLevel / levelsPerDamage );
		AttributeModifier damageModifier = new AttributeModifier( DamageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.ADDITION );
		DamageAttribute.removeModifier( DamageModifierID );
		DamageAttribute.applyModifier( damageModifier );
	}
}
