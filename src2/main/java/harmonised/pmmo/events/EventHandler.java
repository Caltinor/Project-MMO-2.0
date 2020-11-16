package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.XP;

import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class EventHandler
{
	@SubscribeEvent
    public static void blockBroken( BreakEvent event )
    {
		XP.handleBroken( event );
    }
	
	@SubscribeEvent
	public static void blockHarvested( HarvestDropsEvent event )
	{
		XP.handleHarvested(event);
	}
	
	@SubscribeEvent
	public static void blockPlaced( PlaceEvent event )
	{
		XP.handlePlaced( event );
	}
	
	@SubscribeEvent
	public static void livingHurt( LivingDamageEvent event )
	{
		XP.handleDamage( event );
	}
	
	@SubscribeEvent
	public static void playerTick( PlayerTickEvent event )
	{
		XP.handlePlayerTick( event );
	}
	
	@SubscribeEvent
	public static void playerRespawn( PlayerRespawnEvent event )
	{
		XP.handlePlayerRespawn( event );
	}
	
	@SubscribeEvent
	public static void livingJump( LivingJumpEvent event )
	{
		XP.handleLivingJump( event );
	}
	
	@SubscribeEvent
	public static void onPlayerLogin( PlayerLoggedInEvent event)
	{
		AttributeHandler.updateReach( event.player );
		
		XP.handlePlayerConnected( event );
	}
	
	@SubscribeEvent
	public static void onRightClickBlock( RightClickBlock event )
	{
		XP.handleRightClickBlock( event );
	}
	
	@SubscribeEvent
	public static void onAnvilRepair( AnvilRepairEvent event )
	{
		XP.handleAnvilRepair( event );
	}
	
	@SubscribeEvent
	public static void onItemFished( ItemFishedEvent event )
	{
		XP.handleFished( event );
	}

	@SubscribeEvent
	public static void breakSpeed( PlayerEvent.BreakSpeed event )
	{
		XP.handleBreakSpeed( event );
	}
	
//	@SubscribeEvent
//	public static void onRightClickItem( RightClickItem event )
//	{
//		XP.handleRightClickItem( event );
//	}
	
//	@SubscribeEvent
//	public static void itemSmelted( ItemSmeltedEvent event )
//	{
//		XP.handleSmelt( event );
//	}
	
//	@SubscribeEvent
//	public static void livingDeath( LivingDeathEvent event )
//	{
//		
//	}
	
	@SubscribeEvent
	public static void itemCrafted( ItemCraftedEvent event )
	{
		XP.handleCrafted( event );
	}
	
//	@SubscribeEvent
//	public void playerData( PlayerEvent.PlayerLoggedInEvent event )
//	{
//	}
}
