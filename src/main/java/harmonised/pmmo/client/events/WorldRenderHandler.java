package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.VeinRenderer;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;


@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME, value= Dist.CLIENT)
public class WorldRenderHandler {

	@SubscribeEvent
	public static void onWorldRender(RenderLevelStageEvent event) {
		if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS))
			return;
		Minecraft mc = Minecraft.getInstance();

		if (Config.server().veinMiner().enabled()
				&& VeinTracker.isLookingAtVeinTarget(mc.hitResult)
				&& !mc.player.level().getBlockState(((BlockHitResult)mc.hitResult).getBlockPos()).isAir()) {
			VeinTracker.updateVein(mc.player);
			VeinRenderer.drawBoxHighlights(event.getPoseStack(), VeinTracker.getVein());
		}
	}
}
