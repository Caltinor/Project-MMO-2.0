package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

public class IndicatorsOverlayGUI implements IIngameOverlay{
	private static final ResourceLocation ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/overlay_icons.png");
	private Minecraft mc;
	private BlockHitResult bhr;
	
	@Override
	public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult))
			return;
		else
			bhr = (BlockHitResult) mc.hitResult;
		
		if(!mc.options.renderDebug && VeinTracker.isLookingAtVeinTarget(bhr)){
			RenderSystem.setShaderTexture(0, ICONS);
			float iconIndex = VeinTracker.mode.ordinal() * 16;
			GuiComponent.blit(poseStack, (screenWidth/2)-16, (screenHeight/2)-8, iconIndex, 0f, 16, 16, 48, 16);
		}
	}

}
