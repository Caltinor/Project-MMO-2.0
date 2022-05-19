package harmonised.pmmo.events;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.EatFoodEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.events.impl.AnvilRepairHandler;
import harmonised.pmmo.events.impl.BreakHandler;
import harmonised.pmmo.events.impl.BreakSpeedHandler;
import harmonised.pmmo.events.impl.BreedHandler;
import harmonised.pmmo.events.impl.CraftHandler;
import harmonised.pmmo.events.impl.CropGrowHandler;
import harmonised.pmmo.events.impl.DamageDealtHandler;
import harmonised.pmmo.events.impl.DamageReceivedHandler;
import harmonised.pmmo.events.impl.DeathHandler;
import harmonised.pmmo.events.impl.DimensionTravelHandler;
import harmonised.pmmo.events.impl.EntityInteractHandler;
import harmonised.pmmo.events.impl.FishHandler;
import harmonised.pmmo.events.impl.FoodEatHandler;
import harmonised.pmmo.events.impl.JumpHandler;
import harmonised.pmmo.events.impl.LoginHandler;
import harmonised.pmmo.events.impl.MountHandler;
import harmonised.pmmo.events.impl.PistonHandler;
import harmonised.pmmo.events.impl.PlaceHandler;
import harmonised.pmmo.events.impl.PlayerClickHandler;
import harmonised.pmmo.events.impl.PlayerDeathHandler;
import harmonised.pmmo.events.impl.PlayerTickHandler;
import harmonised.pmmo.events.impl.PotionHandler;
import harmonised.pmmo.events.impl.ShieldBlockHandler;
import harmonised.pmmo.events.impl.SleepHandler;
import harmonised.pmmo.events.impl.TameHandler;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Reference;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangeGameModeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**This class manages the dictation of logic to methods
 * designed to handle the method on the server or the 
 * client.  for methods that do not have sidedness, the
 * sole implementation is called.
 * 
 * @author Caltinor
 *
 */
@EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	//==========================================================
	//                 CORE MOD EVENTS
	//==========================================================
	@SubscribeEvent(priority=EventPriority.LOW)
	public static void onPlayerJoin(PlayerLoggedInEvent event) {
		LoginHandler.handle(event);
	}
	@SubscribeEvent
	public static void onPlayerLeave(PlayerLoggedOutEvent event) {
		PartyUtils.removeFromParty(event.getPlayer());
	}
	@SubscribeEvent
	public static void onGamemodeChange(PlayerChangeGameModeEvent event) {
		if (event.getNewGameMode().isCreative()) {
			AttributeInstance reachAttribute = event.getPlayer().getAttribute(ForgeMod.REACH_DISTANCE.get());
			if(reachAttribute.getModifier(Reference.CREATIVE_REACH_ATTRIBUTE) == null || reachAttribute.getModifier(Reference.CREATIVE_REACH_ATTRIBUTE).getAmount() != Config.CREATIVE_REACH.get())
			{
				reachAttribute.removeModifier(Reference.CREATIVE_REACH_ATTRIBUTE);
				reachAttribute.addPermanentModifier(new AttributeModifier(Reference.CREATIVE_REACH_ATTRIBUTE, "PMMO Creative Reach Bonus", Config.CREATIVE_REACH.get(), AttributeModifier.Operation.ADDITION));
			}
		}
		else {
			event.getPlayer().getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(Reference.CREATIVE_REACH_ATTRIBUTE);
		}
	}
	@SubscribeEvent
	public static void onRespawn(PlayerRespawnEvent event) {
		Core core = Core.get(event.getPlayer().level); 
		core.getPerkRegistry().executePerk(EventType.SKILL_UP, event.getPlayer(), core.getSide());
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
	
	//==========================================================
	//                 GAMEPLAY EVENTS
	//==========================================================
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockBreak(BreakEvent event) {
		if (event.isCanceled())
			return;
		//NOTE Fires only on server
		BreakHandler.handle(event);
	}	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockPlace(EntityPlaceEvent event) {
		if (event.isCanceled())
			return;
		PlaceHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBreakSpeed(BreakSpeed event) {
		if (event.isCanceled())
			return;
		//NOTE fires on both sides
		BreakSpeedHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onCraft(ItemCraftedEvent event) {
		if (event.isCanceled())
			return;
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
	public static void onEntityInteract(EntityInteract event) {
		if (event.isCanceled())
			return;
		EntityInteractHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onJump(LivingJumpEvent event) {
		if (event.isCanceled())
			return;
		JumpHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		if (event.getEntityLiving() instanceof Player)
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
	public static void onCropGrow(CropGrowEvent.Post event){
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
	public static void onBlockHit(LeftClickBlock event) {
		PlayerClickHandler.leftClickBlock(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockActivate(RightClickBlock event) {
		PlayerClickHandler.rightClickBlock(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onItemActivate(RightClickItem event) {
		PlayerClickHandler.rightClickItem(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onPlayerTick(PlayerTickEvent event) {
		PlayerTickHandler.handle(event);
	}
	@SubscribeEvent
	public static void onPlayerDimTravel(EntityTravelToDimensionEvent event) {
		DimensionTravelHandler.handle(event);
	}
	@SubscribeEvent
	public static void onFoodEat(EatFoodEvent event) {
		FoodEatHandler.handle(event);
	}
}
