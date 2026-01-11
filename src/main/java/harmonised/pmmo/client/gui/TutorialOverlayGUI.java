package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.LogicalSide;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TutorialOverlayGUI implements LayeredDraw.Layer {
	private Minecraft mc;
	private Font fontRenderer;
	private List<Component> lines = new ArrayList<>();
	private BlockHitResult bhr;

	@Override
	public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult) || mc.screen != null)
			return;
		else
			bhr = (BlockHitResult) mc.hitResult;
		if (fontRenderer == null)
			fontRenderer = mc.font;
		int renderLeft = (guiGraphics.guiWidth() / 8) * 5;
		int renderTop = (guiGraphics.guiHeight() / 4);

		if (!mc.getDebugOverlay().showDebugScreen() && Config.SALVAGE_HIGHLIGHTS.get()) {
			// IDENTIFY LINES
			if (mc.level.getBlockState(bhr.getBlockPos()).getBlock()
					.equals(BuiltInRegistries.BLOCK.get(Config.server().general().salvageBlock()))) {
				lines = new ArrayList<>();
				lines.add(LangProvider.SALVAGE_TUTORIAL_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
				if (mc.player.isCrouching()
						&& (!mc.player.getMainHandItem().isEmpty() || !mc.player.getOffhandItem().isEmpty())) {
					ItemStack salvageStack = mc.player.getMainHandItem().isEmpty() ? mc.player.getOffhandItem()
							: mc.player.getMainHandItem();
					lines.addAll(gatherSalvageData(salvageStack));
				} else
					lines.add(LangProvider.SALVAGE_TUTORIAL_USAGE.asComponent());
			} else
				return; // stop render if none of the viewing cases are met.

			if (!lines.isEmpty()) {
				guiGraphics.renderComponentTooltip(mc.font, lines, renderLeft, renderTop);
			}
		}
	}

	private List<MutableComponent> gatherSalvageData(ItemStack stack) {
		List<MutableComponent> outList = new ArrayList<>();
		for (Map.Entry<ResourceLocation, SalvageData> entry : Core.get(LogicalSide.CLIENT).getLoader().ITEM_LOADER
				.getData(RegistryUtil.getId(mc.level.registryAccess(), stack)).salvage().entrySet()) {
			outList.add(MutableComponent.create(
					new ItemStack(BuiltInRegistries.ITEM.get(entry.getKey())).getDisplayName().getContents()));
			SalvageData data = entry.getValue();
			if (!data.levelReq().isEmpty()) {
				outList.add(LangProvider.SALVAGE_LEVEL_REQ.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Long> req : data.levelReq().entrySet()) {
					outList.add(Component.translatable("pmmo." + req.getKey())
							.append(Component.literal(": " + req.getValue())));
				}
			}
			outList.add(LangProvider.SALVAGE_CHANCE.asComponent(data.baseChance(), data.maxChance())
					.withStyle(ChatFormatting.UNDERLINE));
			outList.add(LangProvider.SALVAGE_MAX.asComponent(data.salvageMax()).withStyle(ChatFormatting.UNDERLINE));
			if (!data.chancePerLevel().isEmpty()) {
				outList.add(LangProvider.SALVAGE_CHANCE_MOD.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
					outList.add(Component.translatable("pmmo." + perLevel.getKey())
							.append(Component.literal(": " + perLevel.getValue())));
				}
			}
			if (!data.xpAward().isEmpty()) {
				outList.add(LangProvider.SALVAGE_XP_AWARD.asComponent().withStyle(ChatFormatting.UNDERLINE));
				for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
					outList.add(Component.translatable("pmmo." + award.getKey())
							.append(Component.literal(": " + award.getValue())));
				}
			}
		}
		return outList;
	}
}
