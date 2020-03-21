package harmonised.pmmo.skills;

import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class AttributeHandler
{
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");
	private static final UUID HPModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID DamageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	 
	public static double getReach( EntityPlayer player )
	{
		IAttributeInstance reachAttribute = player.getEntityAttribute( player.REACH_DISTANCE );		
		
		if( reachAttribute.getModifier( reachModifierID ) == null )
			return ( reachAttribute.getBaseValue() );
		else
			return ( reachAttribute.getBaseValue() + reachAttribute.getModifier( reachModifierID ).getAmount() );
	}
	
	public static void updateReach( EntityPlayer player )
	{
		IAttributeInstance reachAttribute = player.getEntityAttribute( player.REACH_DISTANCE );
		float buildLevel = XP.levelAtXp( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ).getCompoundTag( "skills" ).getFloat( "building" ) );
		if( buildLevel == 1 )
			return;

		double reach = -0.91 + ( buildLevel / 25 );
		
//		if( reach > 0 && player.getHeldItemMainhand().getItem().isDamageable() )
//			reach /= 2;
		
		if( reachAttribute.getModifier( reachModifierID ) == null || reachAttribute.getModifier( reachModifierID).getAmount() != reach )
		{
			AttributeModifier reachModifier = new AttributeModifier( reachModifierID, "Reach bonus thanks to Build Level", reach, 0 );
			reachAttribute.removeModifier( reachModifierID );
			reachAttribute.applyModifier( reachModifier );
		}
	}
	
	public static void updateSpeed( EntityPlayer player )
	{
		IAttributeInstance speedAttribute = player.getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		float agilityLevel = XP.levelAtXp( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ).getCompoundTag( "skills" ).getFloat( "agility" ) );
		double speedBoost = agilityLevel / 2000;
		if( speedBoost > 0.1 )
			speedBoost = 0.1;
		if( speedAttribute.getModifier( speedModifierID ) == null || speedAttribute.getModifier( speedModifierID ).getAmount() != speedBoost )
		{
			AttributeModifier speedModifier = new AttributeModifier( speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, 0 );
			speedAttribute.removeModifier( speedModifierID );
			speedAttribute.applyModifier( speedModifier );
		}
	}
	
	public static void resetSpeed( EntityPlayer player )
	{
		IAttributeInstance speedAttribute = player.getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
		speedAttribute.removeModifier( speedModifierID );
	}
	
	public static void updateHP( EntityPlayer player )
	{
		IAttributeInstance HPAttribute = player.getEntityAttribute( SharedMonsterAttributes.MAX_HEALTH );
		float enduranceLevel = XP.levelAtXp( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ).getCompoundTag( "skills" ).getFloat( "endurance" ) );
		int maxHP = (int) Math.floor( enduranceLevel / 10) * 2;
		AttributeModifier HPModifier = new AttributeModifier( HPModifierID, "Max HP Bonus thanks to Endurance Level", maxHP, 0 );
		HPAttribute.removeModifier( HPModifierID );
		HPAttribute.applyModifier( HPModifier );
	}
	
	public static void updateDamage( EntityPlayer player )
	{
		IAttributeInstance DamageAttribute = player.getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		float combatLevel = XP.levelAtXp( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ).getCompoundTag( "skills" ).getFloat( "combat" ) );
		int damageBoost = (int) Math.floor( combatLevel / 20 );
		AttributeModifier damageModifier = new AttributeModifier( DamageModifierID, "Damage Boost thanks to Combat Level", damageBoost, 0 );
		DamageAttribute.removeModifier( DamageModifierID );
		DamageAttribute.applyModifier( damageModifier );
	}
}
