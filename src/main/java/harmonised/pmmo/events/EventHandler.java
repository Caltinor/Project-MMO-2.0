package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.events.impl.BreakHandler;
import harmonised.pmmo.events.impl.BreakSpeedHandler;
import harmonised.pmmo.events.impl.CraftHandler;
import harmonised.pmmo.events.impl.DamageDealtHandler;
import harmonised.pmmo.events.impl.DamageReceivedHandler;
import harmonised.pmmo.events.impl.DeathHandler;
import harmonised.pmmo.events.impl.EntityInteractHandler;
import harmonised.pmmo.events.impl.JumpHandler;
import harmonised.pmmo.events.impl.LoginHandler;
import harmonised.pmmo.events.impl.PlaceHandler;
import harmonised.pmmo.events.impl.PlayerDeathHandler;
import harmonised.pmmo.features.mobscaling.MobAttributeHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.DifficultyChangeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangeGameModeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
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
	@SuppressWarnings("resource")
	@SubscribeEvent(priority=EventPriority.LOW)
	public static void onPlayerJoin(PlayerLoggedInEvent event) {
		if (event.getPlayer().getLevel().isClientSide)
			return;
		else
			LoginHandler.handle(event);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST) 
	public static void onDifficultyChange(DifficultyChangeEvent event ) {
		if (event.isCanceled()) 
			return;
		MobAttributeHandler.updateMobDifficulty(event.getDifficulty());
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
}
