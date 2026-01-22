package harmonised.pmmo.client.gui;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class IndicatorsOverlayGUI implements GuiLayer {
	private static final Identifier ICONS = Reference.rl("textures/gui/overlay_icons.png");
	private Minecraft mc;
	private BlockHitResult bhr;
	
	@Override
	public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult))
			return;
		else
			bhr = (BlockHitResult) mc.hitResult;
		if(!mc.getDebugOverlay().showDebugScreen() && VeinTracker.isLookingAtVeinTarget(bhr)
				&& !mc.player.level().getBlockState(bhr.getBlockPos()).isAir()){
			float iconIndex = (float)VeinTracker.mode.ordinal() * 0.333f;
			guiGraphics.blit(ICONS,
					(guiGraphics.guiWidth()/2)-16,
					(guiGraphics.guiHeight()/2)-8,
					(guiGraphics.guiWidth()/2),
					(guiGraphics.guiHeight()/2)+8,
					iconIndex, iconIndex+0.333f, 0, 1f);
		}
	}

}
