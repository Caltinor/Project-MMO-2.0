package harmonised.pmmo.events;

import harmonised.pmmo.util.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
public class EventHandler
{

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void blockBroken( BreakEvent event )
	{
		BlockBrokenHandler.handleBroken( event );
	}

	@SubscribeEvent
	public static void blockPlaced( BlockEvent.EntityPlaceEvent event )
	{
		BlockPlacedHandler.handlePlaced( event );
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingHurt( LivingDamageEvent event )
	{
		DamageHandler.handleDamage( event );
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingDeath( LivingDeathEvent event )
	{
//		DeathHandler.handleDeath( event );
		//COUT DEATH HANDLER
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
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

	@SubscribeEvent
	public static void playerClone( net.minecraftforge.event.entity.player.PlayerEvent.Clone event )
	{
		PlayerCloneHandler.handleClone( event );
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void breakSpeed( net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event )
	{
		BreakSpeedHandler.handleBreakSpeed( event );
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerInteract( PlayerInteractEvent event )
	{
		PlayerInteractionHandler.handlePlayerInteract( event );
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingSpawn( LivingSpawnEvent.EnteringChunk event )
	{
		SpawnHandler.handleSpawn( event );
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
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

//	@SubscribeEvent
//	public static void itemThrown( EntityItemPickupEvent event )
//	{
//		PickupHandler.handlePickup( event );
//	}

//	@SubscribeEvent
//	public static void furnace( PlayerEvent.ItemSmeltedEvent )

//	@SubscribeEvent(priority = EventPriority.HIGHEST)
//	public static void sleepDone( SleepFinishedTimeEvent event )
//	{
//		SleepHandler.handleSleepFinished( event );
//	}

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

//	@SubscribeEvent
//	public static void pistonPush( PistonEvent event )
//	{
//		PistonEventHandler.handlePistonPush( event );
//	}

//	@SubscribeEvent
//	public static void pickUpEntity( EntityItemPickupEvent event )
//	{
//		ItemHandler.handleItemEntityPickup( event );
//	}

	@SubscribeEvent
	public static void saplingGrow( SaplingGrowTreeEvent event )
	{
		GrowHandler.handleSaplingGrow( event );
	}

	@SubscribeEvent
	public static void cropGrow( BlockEvent.CropGrowEvent.Post event )
	{
		GrowHandler.handleCropGrow( event );
	}

//	@SubscribeEvent
//	public static void registerTasks( RegistryEvent.Register<TaskType> event )
//	{
//		event.getRegistry().register( SkillTask.SKILL = new TaskType( SkillTask::new ).setRegistryName( "skill" ).setIcon( Icon.getIcon("minecraft:item/dragon_egg") ) );
//	}

	@SubscribeEvent
	public static void onConfigChanged( final ConfigChangedEvent.OnConfigChangedEvent event )
	{
		if ( event.getModID().equals( Reference.MOD_ID ) )
			ConfigManager.sync( Reference.MOD_ID, Config.Type.INSTANCE );
	}
}