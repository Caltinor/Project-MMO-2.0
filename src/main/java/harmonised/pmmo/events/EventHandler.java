package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.EnchantEvent;
import harmonised.pmmo.api.events.FurnaceBurnEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.events.impl.*;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

/**This class manages the dictation of logic to methods
 * designed to handle the method on the server or the 
 * client.  for methods that do not have sidedness, the
 * sole implementation is called.
 * 
 * @author Caltinor
 *
 */
@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	//==========================================================
	//                 CORE MOD EVENTS
	//==========================================================
	@SubscribeEvent(priority= EventPriority.LOW)
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		LoginHandler.handle(event);
	}
	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		PartyUtils.removeFromParty(event.getEntity());
	}
	@SubscribeEvent
	public static void onGamemodeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
		if (event.getNewGameMode().isCreative()) {
			AttributeInstance reachAttribute = event.getEntity().getAttribute(NeoForgeMod.BLOCK_REACH.value());
			if(reachAttribute.getModifier(Reference.CREATIVE_REACH_ATTRIBUTE) == null || reachAttribute.getModifier(Reference.CREATIVE_REACH_ATTRIBUTE).getAmount() != Config.CREATIVE_REACH.get())
			{
				reachAttribute.removeModifier(Reference.CREATIVE_REACH_ATTRIBUTE);
				reachAttribute.addPermanentModifier(new AttributeModifier(Reference.CREATIVE_REACH_ATTRIBUTE, "PMMO Creative Reach Bonus", Config.CREATIVE_REACH.get(), AttributeModifier.Operation.ADDITION));
			}
		}
		else {
			event.getEntity().getAttribute(NeoForgeMod.BLOCK_REACH.value()).removeModifier(Reference.CREATIVE_REACH_ATTRIBUTE);
		}
	}
	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		Core core = Core.get(event.getEntity().level()); 
		core.getPerkRegistry().executePerk(EventType.SKILL_UP, event.getEntity(),
				TagBuilder.start().withString(APIUtils.SKILLNAME, "respawn").build());
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onSleep(SleepFinishedTimeEvent event) {
		SleepHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onPistonMove(PistonEvent.Pre event) {
		if (event.isCanceled())
			return;
		PistonHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.HIGH)
	public static void tickPerks(TickEvent.LevelTickEvent event) {
		Core.get(event.level).getPerkRegistry().executePerkTicks(event);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void filterExplosions(ExplosionEvent.Detonate event) {
		ExplosionHandler.handle(event);
	}
	
	//==========================================================
	//                 GAMEPLAY EVENTS
	//==========================================================
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.isCanceled())
			return;
		//NOTE Fires only on server
		BreakHandler.handle(event);
	}	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.isCanceled())
			return;
		PlaceHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.isCanceled())
			return;
		//NOTE fires on both sides
		BreakSpeedHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
		//NOTE fires on both sides
		CraftHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onDamage(LivingHurtEvent event) {
		if (event.isCanceled())
			return;
		DamageReceivedHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onDealDamage(LivingAttackEvent event) {
		if (event.isCanceled())
			return;
		DamageDealtHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onDealDamage(LivingDamageEvent event) {
		if (event.isCanceled())
			return;
		DamageDealtHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.isCanceled())
			return;
		EntityInteractHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onJump(LivingEvent.LivingJumpEvent event) {
		JumpHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		if (event.getEntity() instanceof Player)
			PlayerDeathHandler.handle(event);
		DeathHandler.handle(event);
	}
	@SubscribeEvent
	public static void onPotionCollect(PlayerBrewedPotionEvent event) {
		PotionHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBreed(BabyEntitySpawnEvent event) {
		if (event.isCanceled())
			return;
		BreedHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onTame(AnimalTameEvent event) {
		if (event.isCanceled())
			return;
		TameHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST) 
	public static void onFish(ItemFishedEvent event){
		if (event.isCanceled())
			return;
		FishHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST) 
	public static void onCropGrow(BlockEvent.CropGrowEvent.Post event){
		CropGrowHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onMount(EntityMountEvent event) {
		if (event.isCanceled())
			return;
		MountHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onShieldBlock(ShieldBlockEvent event) {
		if (event.isCanceled())
			return;
		ShieldBlockHandler.handle(event);
	}
	@SubscribeEvent
	public static void onAnvilRepar(AnvilRepairEvent event) {
		AnvilRepairHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockHit(PlayerInteractEvent.LeftClickBlock event) {
		PlayerClickHandler.leftClickBlock(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockActivate(PlayerInteractEvent.RightClickBlock event) {
		PlayerClickHandler.rightClickBlock(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onItemActivate(PlayerInteractEvent.RightClickItem event) {
		PlayerClickHandler.rightClickItem(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		PlayerTickHandler.handle(event);
	}
	@SubscribeEvent
	public static void onPlayerDimTravel(EntityTravelToDimensionEvent event) {
		DimensionTravelHandler.handle(event);
	}
	@SubscribeEvent
	public static void onFoodEat(LivingEntityUseItemEvent.Finish event) {
		FoodEatHandler.handle(event);
	}
	@SubscribeEvent
	public static void onSmelt(FurnaceBurnEvent event) {
		FurnaceHandler.handle(event);
	}
	@SubscribeEvent
	public static void onEnchant(EnchantEvent event) {
		EnchantHandler.handle(event);
	}
	@SubscribeEvent
	public static void onTrade(TradeWithVillagerEvent event) {TradeHandler.handle(event);}
}
