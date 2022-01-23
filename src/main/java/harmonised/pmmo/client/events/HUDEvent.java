package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.util.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HUDEvent {
	@SubscribeEvent
	public static void onOverlay(RenderGameOverlayEvent event) {
		XPOverlayGUI.renderOverlay(event);
	}
}
