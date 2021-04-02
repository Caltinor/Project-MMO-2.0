package harmonised.pmmo.events;

import harmonised.pmmo.util.Reference;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class EventHandler
{

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void blockBroken( BreakEvent event )
	{
		BlockBrokenHandler.handleBroken( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void blockPlaced( BlockEvent.EntityMultiPlaceEvent event )
	{
		if( event.getEntity() != null && BlockPlacedHandler.handlePlaced( event.getEntity(), event.getPlacedBlock(), (World) event.getWorld(), event.getPos() ) )
			event.setCanceled( true );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void blockPlaced( BlockEvent.EntityPlaceEvent event )
	{
		if( event.getEntity() != null && BlockPlacedHandler.handlePlaced( event.getEntity(), event.getPlacedBlock(), (World) event.getWorld(), event.getPos() ) )
			event.setCanceled( true );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void livingHurt( LivingDamageEvent event )
	{
		DamageHandler.handleDamage( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void livingDeath( LivingDeathEvent event )
	{
		DeathHandler.handleDeath( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void playerTick( TickEvent.PlayerTickEvent event )
	{
		PlayerTickHandler.handlePlayerTick( event );
	}

	@SubscribeEvent
	public static void playerRespawn( PlayerEvent.PlayerRespawnEvent event )
	{
		PlayerRespawnHandler.handlePlayerRespawn( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
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
	public static void playerLogout( PlayerEvent.PlayerLoggedOutEvent event )
	{
		PlayerDisconnectedHandler.handlerPlayerDisconnected( event );
	}

//	@SubscribeEvent( priority = EventPriority.LOWEST )
//	public static void playerClone( PlayerEvent.Clone event )
//	{
//		PlayerCloneHandler.handleClone( event );
//	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void onAnvilRepair( AnvilRepairEvent event )
	{
		AnvilRepairHandler.handleAnvilRepair( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void onItemFished( ItemFishedEvent event )
	{
		FishedHandler.handleFished( event );
	}


	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void itemCrafted( PlayerEvent.ItemCraftedEvent event )
	{
		CraftedHandler.handleCrafted( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void breakSpeed( PlayerEvent.BreakSpeed event )
	{
		BreakSpeedHandler.handleBreakSpeed( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void playerInteract( PlayerInteractEvent event )
	{
		PlayerInteractionHandler.handlePlayerInteract( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void livingSpawn( LivingSpawnEvent.EnteringChunk event )
	{
		SpawnHandler.handleSpawn( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void babySpawn( BabyEntitySpawnEvent event )
	{
		BreedHandler.handleBreedEvent( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
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

	@SubscribeEvent( priority = EventPriority.HIGHEST )
	public static void sleepDone( SleepFinishedTimeEvent event )
	{
		SleepHandler.handleSleepFinished( event );
	}

	@SubscribeEvent
	public static void chunkDataLoad( ChunkDataEvent.Load event )
	{
		ChunkDataHandler.handleChunkDataLoad( event );
	}

	@SubscribeEvent
	public static void chunkDataSave( ChunkDataEvent.Save event )
	{
		ChunkDataHandler.handleChunkDataSave( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void pistonPush( PistonEvent event )
	{
		PistonEventHandler.handlePistonPush( event );
	}

//	@SubscribeEvent
//	public static void pickUpEntity( EntityItemPickupEvent event )
//	{
//		ItemHandler.handleItemEntityPickup( event );
//	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void saplingGrow( SaplingGrowTreeEvent event )
	{
		GrowHandler.handleSaplingGrow( event );
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public static void cropGrow( BlockEvent.CropGrowEvent.Post event )
	{
		GrowHandler.handleCropGrow( event );
	}
}