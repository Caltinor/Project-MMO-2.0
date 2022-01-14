package harmonised.pmmo.events;

import harmonised.pmmo.events.impl.BreakHandler;
import harmonised.pmmo.events.impl.BreakSpeedHandler;
import harmonised.pmmo.events.impl.PlaceHandler;
import harmonised.pmmo.util.Reference;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
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

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onBlockBreak(BreakEvent event) {
		if (event.isCanceled())
			return;
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
		BreakSpeedHandler.handle(event);
	}
}
