package harmonised.pmmo.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class TutorialOverlayGUI implements IGuiOverlay{
	private Minecraft mc;
	private Font fontRenderer;
	private List<ClientTooltipComponent> lines = new ArrayList<>();
	private BlockHitResult bhr;

	@Override
	public void render(ForgeGui gui, PoseStack stack, float partialTick, int screenWidth, int screenHeight) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult))
			return;
		else
			bhr = (BlockHitResult) mc.hitResult;
		if (fontRenderer == null)
			fontRenderer = mc.font;
		int renderLeft = (screenWidth / 8) * 5;
		int renderTop = (screenHeight / 4);
		int tooltipWidth = 3 * (screenWidth/8);
		
		if(!mc.options.renderDebug){
			//IDENTIFY LINES
			if (mc.level.getBlockState(bhr.getBlockPos()).getBlock().equals(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Config.SALVAGE_BLOCK.get())))) {	
				lines = new ArrayList<>(ctc(mc, LangProvider.SALVAGE_TUTORIAL_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), tooltipWidth));
				if (mc.player.isCrouching() && (!mc.player.getMainHandItem().isEmpty() || !mc.player.getOffhandItem().isEmpty())) {
					ItemStack salvageStack = mc.player.getMainHandItem().isEmpty() ? mc.player.getOffhandItem() : mc.player.getMainHandItem();
					gatherSalvageData(salvageStack).forEach(line -> lines.addAll(ctc(mc, line, tooltipWidth)));
				}
				else 
					lines.addAll(ctc(mc, LangProvider.SALVAGE_TUTORIAL_USAGE.asComponent(), tooltipWidth));				
			}
			else
				return; //stop render if none of the viewing cases are met.			
			
			//RENDER
			stack.pushPose();
			RenderSystem.enableBlend();
			if (!lines.isEmpty()) {
		         int i = 0;
		         int j = lines.size() == 1 ? -2 : 0;

		         for(ClientTooltipComponent clienttooltipcomponent : lines) {
		            int k = clienttooltipcomponent.getWidth(fontRenderer);
		            if (k > i) {
		               i = k;
		            }

		            j += clienttooltipcomponent.getHeight();
		         }

		         int j2 = renderLeft;
		         int k2 = renderTop;
		         if (j2 + i > screenWidth) {
		            j2 -= 28 + i;
		         }

		         if (k2 + j + 6 > screenHeight) {
		            k2 = screenHeight - j - 6;
		         }

		         stack.pushPose();
		         Tesselator tesselator = Tesselator.getInstance();
		         BufferBuilder bufferbuilder = tesselator.getBuilder();
		         RenderSystem.setShader(GameRenderer::getPositionColorShader);
		         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		         Matrix4f matrix4f = stack.last().pose();
		         net.minecraftforge.client.event.RenderTooltipEvent.Color colorEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipColor(ItemStack.EMPTY, stack, j2, k2, fontRenderer, lines);
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 4, j2 + i + 3, k2 - 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundStart());
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 + j + 3, j2 + i + 3, k2 + j + 4, 400, colorEvent.getBackgroundEnd(), colorEvent.getBackgroundEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3, j2 + i + 3, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 - 4, k2 - 3, j2 - 3, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 + i + 3, k2 - 3, j2 + i + 4, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + j + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 + i + 2, k2 - 3 + 1, j2 + i + 3, k2 + j + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3, j2 + i + 3, k2 - 3 + 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderStart());
		         fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 + j + 2, j2 + i + 3, k2 + j + 3, 400, colorEvent.getBorderEnd(), colorEvent.getBorderEnd());
		         RenderSystem.enableDepthTest();
		         RenderSystem.disableTexture();
		         RenderSystem.enableBlend();
		         RenderSystem.defaultBlendFunc();
		         BufferUploader.drawWithShader(bufferbuilder.end());
		         RenderSystem.disableBlend();
		         RenderSystem.enableTexture();
		         MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		         stack.translate(0.0D, 0.0D, 400.0D);
		         int l1 = k2;

		         for(int i2 = 0; i2 < lines.size(); ++i2) {
		            ClientTooltipComponent clienttooltipcomponent1 = lines.get(i2);
		            clienttooltipcomponent1.renderText(fontRenderer, j2, l1, matrix4f, multibuffersource$buffersource);
		            l1 += clienttooltipcomponent1.getHeight() + (i2 == 0 ? 2 : 0);
		         }

		         multibuffersource$buffersource.endBatch();
		         stack.popPose();
		         l1 = k2;

		         for(int l2 = 0; l2 < lines.size(); ++l2) {
		            ClientTooltipComponent clienttooltipcomponent2 = lines.get(l2);
		            l1 += clienttooltipcomponent2.getHeight() + (l2 == 0 ? 2 : 0);
		         }
		      }
			stack.popPose();
		}
	}

	protected static void fillGradient(Matrix4f pMatrix, BufferBuilder pBuilder, int pX1, int pY1, int pX2, int pY2, int pBlitOffset, int pColorA, int pColorB) {
	      float f = (float)(pColorA >> 24 & 255) / 255.0F;
	      float f1 = (float)(pColorA >> 16 & 255) / 255.0F;
	      float f2 = (float)(pColorA >> 8 & 255) / 255.0F;
	      float f3 = (float)(pColorA & 255) / 255.0F;
	      float f4 = (float)(pColorB >> 24 & 255) / 255.0F;
	      float f5 = (float)(pColorB >> 16 & 255) / 255.0F;
	      float f6 = (float)(pColorB >> 8 & 255) / 255.0F;
	      float f7 = (float)(pColorB & 255) / 255.0F;
	      pBuilder.vertex(pMatrix, (float)pX2, (float)pY1, (float)pBlitOffset).color(f1, f2, f3, f).endVertex();
	      pBuilder.vertex(pMatrix, (float)pX1, (float)pY1, (float)pBlitOffset).color(f1, f2, f3, f).endVertex();
	      pBuilder.vertex(pMatrix, (float)pX1, (float)pY2, (float)pBlitOffset).color(f5, f6, f7, f4).endVertex();
	      pBuilder.vertex(pMatrix, (float)pX2, (float)pY2, (float)pBlitOffset).color(f5, f6, f7, f4).endVertex();
	}
	
	private List<ClientTooltipComponent> ctc(Minecraft mc, MutableComponent component, int width) {
		return mc.font.split(component, width).stream().map(fcs -> ClientTooltipComponent.create(fcs)).toList();
	}
	
	private List<MutableComponent> gatherSalvageData(ItemStack stack) {
		List<MutableComponent> outList = new ArrayList<>();
		for (Map.Entry<ResourceLocation, SalvageData> entry : Core.get(LogicalSide.CLIENT).getSalvageLogic().getSalvageData(RegistryUtil.getId(stack)).entrySet()) {
			outList.add(MutableComponent.create(new ItemStack(ForgeRegistries.ITEMS.getValue(entry.getKey())).getDisplayName().getContents()));
			SalvageData data = entry.getValue();
			if (!data.levelReq().isEmpty()) {
				outList.add(LangProvider.SALVAGE_LEVEL_REQ.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Integer> req : data.levelReq().entrySet()) {
					outList.add(Component.translatable("pmmo."+req.getKey()).append(Component.literal(": "+req.getValue())));
				}
			}
			outList.add(LangProvider.SALVAGE_CHANCE.asComponent(data.baseChance(), data.maxChance()).withStyle(ChatFormatting.UNDERLINE));
			outList.add(LangProvider.SALVAGE_MAX.asComponent(data.salvageMax()).withStyle(ChatFormatting.UNDERLINE));
			if (!data.chancePerLevel().isEmpty()) {
				outList.add(LangProvider.SALVAGE_CHANCE_MOD.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
					outList.add(Component.translatable("pmmo."+perLevel.getKey()).append(Component.literal(": "+perLevel.getValue())));
				}
			}
			if (!data.xpAward().isEmpty()) {
				outList.add(LangProvider.SALVAGE_XP_AWARD.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
					outList.add(Component.translatable("pmmo."+award.getKey()).append(Component.literal(": "+award.getValue())));
				}
			}
		}
		return outList;
	}
}
