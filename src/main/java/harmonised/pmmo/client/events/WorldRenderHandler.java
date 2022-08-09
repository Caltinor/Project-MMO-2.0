package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.VeinRenderer;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class WorldRenderHandler {

	@SubscribeEvent
	public static void onWorldRender(RenderLevelStageEvent event) {
		Minecraft mc = Minecraft.getInstance();

		if (VeinTracker.isLookingAtVeinTarget(mc.hitResult) && event.getStage() == Stage.AFTER_SOLID_BLOCKS) {
			VeinTracker.updateVein(mc.player);
			VeinRenderer.drawBoxHighlights(event.getPoseStack(), VeinTracker.getVein());
		}
	}
}
