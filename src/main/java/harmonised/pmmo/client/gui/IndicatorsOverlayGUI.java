package harmonised.pmmo.client.gui;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class IndicatorsOverlayGUI implements IGuiOverlay{
	private static final ResourceLocation ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/overlay_icons.png");
	private Minecraft mc;
	private BlockHitResult bhr;
	
	@Override
	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult))
			return;
		else
			bhr = (BlockHitResult) mc.hitResult;
		
		if(!mc.options.renderDebug && VeinTracker.isLookingAtVeinTarget(bhr)
				&& !mc.player.level().getBlockState(bhr.getBlockPos()).isAir()){
			float iconIndex = VeinTracker.mode.ordinal() * 16;
			guiGraphics.blit(ICONS, (screenWidth/2)-16, (screenHeight/2)-8, iconIndex, 0f, 16, 16, 48, 16);
		}
	}

}
