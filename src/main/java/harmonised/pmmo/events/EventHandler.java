package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.XP;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventHandler
{
	@SubscribeEvent
    public static void blockBroken( BreakEvent event )
    {
		XP.handleBroken( event );
    }

	@SubscribeEvent
	public static void blockPlaced( BlockEvent.EntityPlaceEvent event )
	{
		XP.handlePlaced( event );
	}
	
	@SubscribeEvent
	public static void livingHurt( LivingDamageEvent event )
	{
		XP.handleDamage( event );
	}
	
	@SubscribeEvent
	public static void playerTick( TickEvent.PlayerTickEvent event )
	{
		XP.handlePlayerTick( event );
	}
	
	@SubscribeEvent
	public static void playerRespawn( PlayerEvent.PlayerRespawnEvent event )
	{
		XP.handlePlayerRespawn( event );
	}
	
	@SubscribeEvent
	public static void livingJump( LivingJumpEvent event )
	{
		XP.handleJump( event );
	}
	
	@SubscribeEvent
	public static void PlayerLogin( PlayerEvent.PlayerLoggedInEvent event)
	{
		XP.handlePlayerConnected( event );
	}

	@SubscribeEvent
	public static void itemSmelted( PlayerEvent.ItemSmeltedEvent event )
	{
		XP.handleSmelted( event );
	}

    @SubscribeEvent
	public static void playerClone( PlayerEvent.Clone event )
	{
		XP.handleClone( event );
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
	public static void itemCrafted( PlayerEvent.ItemCraftedEvent event )
	{
		XP.handleCrafted( event );
	}

	@SubscribeEvent
	public static void breakSpeed( PlayerEvent.BreakSpeed event )
	{
		XP.handleBreakSpeed( event );
	}
	
//	@SubscribeEvent
//	public void playerData( Playerevent.getPlayer()LoggedInEvent event )
//	{
//	}
}
