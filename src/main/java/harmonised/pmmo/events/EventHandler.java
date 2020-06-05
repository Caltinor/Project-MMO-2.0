package harmonised.pmmo.events;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod.EventBusSubscriber
public class EventHandler
{
	@SubscribeEvent
    public static void blockBroken( BreakEvent event )
    {
		BlockBrokenHandler.handleBroken( event );
    }

	@SubscribeEvent
	public static void blockPlaced( BlockEvent.EntityPlaceEvent event )
	{
		BlockPlacedHandler.handlePlaced( event );
	}
	
	@SubscribeEvent
	public static void livingHurt( LivingDamageEvent event )
	{
		DamageHandler.handleDamage( event );
	}

	@SubscribeEvent
	public static void livingDeath( LivingDeathEvent event )
	{
		DeathHandler.handleDeath( event );
	}

	@SubscribeEvent
	public static void playerTick( TickEvent.PlayerTickEvent event )
	{
		PlayerTickHandler.handlePlayerTick( event );
	}
	
	@SubscribeEvent
	public static void playerRespawn( PlayerEvent.PlayerRespawnEvent event )
	{
		PlayerRespawnHandler.handlePlayerRespawn( event );
	}
	
	@SubscribeEvent
	public static void livingJump( LivingJumpEvent event )
	{
		JumpHandler.handleJump( event );
	}
	
	@SubscribeEvent
	public static void PlayerLogin( PlayerEvent.PlayerLoggedInEvent event)
	{
		PlayerConnectedHandler.handlePlayerConnected( event );
	}

    @SubscribeEvent
	public static void playerClone( PlayerEvent.Clone event )
	{
		PlayerCloneHandler.handleClone( event );
	}

	@SubscribeEvent
	public static void onAnvilRepair( AnvilRepairEvent event )
	{
		AnvilRepairHandler.handleAnvilRepair( event );
	}
	
	@SubscribeEvent
	public static void onItemFished( ItemFishedEvent event )
	{
		FishedHandler.handleFished( event );
	}

	
	@SubscribeEvent
	public static void itemCrafted( PlayerEvent.ItemCraftedEvent event )
	{
		CraftedHandler.handleCrafted( event );
	}

	@SubscribeEvent
	public static void breakSpeed( PlayerEvent.BreakSpeed event )
	{
		BreakSpeedHandler.handleBreakSpeed( event );
	}

	@SubscribeEvent
	public static void itemUsed( PlayerInteractEvent event )
	{
		PlayerInteractionHandler.handleItemUse( event );
	}

	@SubscribeEvent
	public static void livingSpawn( LivingSpawnEvent.EnteringChunk event )
	{
		SpawnHandler.handleSpawn( event );
	}

	@SubscribeEvent
	public static void babySpawn( BabyEntitySpawnEvent event )
	{
		BreedHandler.handleBreedEvent( event );
	}

	@SubscribeEvent
	public static void animalTaming( AnimalTameEvent event )
	{
		TameHandler.handleAnimalTaming( event );
	}

	@SubscribeEvent
	public static void worldTick( TickEvent.WorldTickEvent event )
	{
		WorldTickHandler.handleWorldTick( event );
	}

	@SubscribeEvent
	public static void serverStopping( FMLServerStoppingEvent event )
	{
		ServerStoppingHandler.handleServerStop( event );
	}

//	@SubscribeEvent
//	public static void itemThrown( EntityItemPickupEvent event )
//	{
//		PickupHandler.handlePickup( event );
//	}

//	@SubscribeEvent
//	public static void furnace( PlayerEvent.ItemSmeltedEvent )

	@SubscribeEvent
	public static void sleepDone( SleepFinishedTimeEvent event )
	{
		SleepHandler.handleSleepFinished( event );
	}
}
