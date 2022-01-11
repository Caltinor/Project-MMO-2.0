package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class EventHandler
{

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void blockBroken(BreakEvent event)
	{
		if (event.isCanceled()) return;
		BlockBrokenHandler.handleBroken(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void blockPlaced(BlockEvent.EntityMultiPlaceEvent event)
	{
		if (event.isCanceled()) return;
		if(event.getEntity() != null && BlockPlacedHandler.handlePlaced(event.getEntity(), event.getPlacedBlock(), (Level) event.getWorld(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void blockPlaced(BlockEvent.EntityPlaceEvent event)
	{
		if (event.isCanceled()) return;
		if(event.getEntity() != null && BlockPlacedHandler.handlePlaced(event.getEntity(), event.getPlacedBlock(), (Level) event.getWorld(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingHurt(LivingHurtEvent event)
	{
		if (event.isCanceled()) return;
		DamageHandler.handleDamage(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingDeath(LivingDeathEvent event)
	{
		if (event.isCanceled()) return;
		DeathHandler.handleDeath(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerTick(TickEvent.PlayerTickEvent event)
	{
		PlayerTickHandler.handlePlayerTick(event);
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		if (event.isCanceled()) return;
		PlayerRespawnHandler.handlePlayerRespawn(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingJump(LivingJumpEvent event)
	{
		if (event.isCanceled()) return;
		JumpHandler.handleJump(event);
	}

	@SubscribeEvent
	public static void PlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.isCanceled()) return;
		PlayerConnectedHandler.handlePlayerConnected(event);
	}

	@SubscribeEvent
	public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		PlayerDisconnectedHandler.handlerPlayerDisconnected(event);
	}

//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void playerClone(PlayerEvent.Clone event)
//	{
//		PlayerCloneHandler.handleClone(event);
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onAnvilRepair(AnvilRepairEvent event)
	{
		if (event.isCanceled()) return;
		AnvilRepairHandler.handleAnvilRepair(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onItemFished(ItemFishedEvent event)
	{
		if (event.isCanceled()) return;
		FishedHandler.handleFished(event);
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void itemCrafted(PlayerEvent.ItemCraftedEvent event)
	{
		if (event.isCanceled()) return;
		CraftedHandler.handleCrafted(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void breakSpeed(PlayerEvent.BreakSpeed event)
	{
		if (event.isCanceled()) return;
		BreakSpeedHandler.handleBreakSpeed(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCanceled()) return;
		PlayerInteractionHandler.handlePlayerInteract(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void livingSpawn(LivingSpawnEvent.SpecialSpawn event)
	{
		if (event.isCanceled()) return;
		SpawnHandler.handleSpawn(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void babySpawn(BabyEntitySpawnEvent event)
	{
		if (event.isCanceled()) return;
		BreedHandler.handleBreedEvent(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void animalTaming(AnimalTameEvent event)
	{
		if (event.isCanceled()) return;
		TameHandler.handleAnimalTaming(event);
	}

	@SubscribeEvent
	public static void worldTick(TickEvent.WorldTickEvent event)
	{
		WorldTickHandler.handleWorldTick(event);
	}

	@SubscribeEvent
	public static void serverStopping(ServerStoppingEvent event)
	{
		ServerStoppingHandler.handleServerStop(event);
	}

//	@SubscribeEvent
//	public static void itemThrown(EntityItemPickupEvent event)
//	{
//		PickupHandler.handlePickup(event);
//	}

//	@SubscribeEvent
//	public static void furnace(PlayerEvent.ItemSmeltedEvent)

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void sleepDone(SleepFinishedTimeEvent event)
	{
		SleepHandler.handleSleepFinished(event);
	}

	@SubscribeEvent
	public static void chunkDataLoad(ChunkDataEvent.Load event)
	{
		ChunkDataHandler.handleChunkDataLoad(event);
	}

	@SubscribeEvent
	public static void chunkDataSave(ChunkDataEvent.Save event)
	{
		ChunkDataHandler.handleChunkDataSave(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void pistonPush(PistonEvent event)
	{
		if (event.isCanceled()) return;
		PistonEventHandler.handlePistonPush(event);
	}

//	@SubscribeEvent
//	public static void pickUpEntity(EntityItemPickupEvent event)
//	{
//		ItemHandler.handleItemEntityPickup(event);
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void saplingGrow(SaplingGrowTreeEvent event)
	{
		if (event.isCanceled()) return;
		GrowHandler.handleSaplingGrow(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void cropGrow(BlockEvent.CropGrowEvent.Post event)
	{
		if (event.isCanceled()) return;
		GrowHandler.handleCropGrow(event);
	}

	@SubscribeEvent
	public static void travelDimension(EntityTravelToDimensionEvent event)
	{
		if (event.isCanceled()) return;
		if(event.getEntity() instanceof ServerPlayer)
		{
			ServerPlayer player = (ServerPlayer) event.getEntity();
			ResourceLocation destination = event.getDimension().location();
			Map<String, Double> reqMap = APIUtils.getXp(destination, JType.REQ_DIMENSION_TRAVEL);
			if(!XP.checkReq(player, reqMap))
			{
				event.setCanceled(true);
				player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToTravelToDimension", new TranslatableComponent(event.getDimension().location().toString())).setStyle(XP.textStyle.get("red")), true);
				player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToTravelToDimension", new TranslatableComponent(event.getDimension().location().toString())).setStyle(XP.textStyle.get("red")), false);
				XP.sendPlayerSkillList(player, reqMap);
			}
		}
	}
}