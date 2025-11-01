package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class XPOverlayGUI implements GuiLayer {
	private final Core core = Core.get(LogicalSide.CLIENT);
	private int skillGap = 0;
	private Minecraft mc;
	private Font fontRenderer;

	@Override
	public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (fontRenderer == null)
			fontRenderer = mc.font;
		
		if(!mc.getDebugOverlay().showDebugScreen()){
//			guiGraphics.pose().pushPose();
//			RenderSystem.enableBlend();
			
			if(Config.SKILL_LIST_DISPLAY.get())
				renderSkillList(guiGraphics, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
			if(Config.server().veinMiner().enabled() && Config.VEIN_GAUGE_DISPLAY.get())
				renderVeinGauge(guiGraphics, Config.VEIN_GAUGE_OFFSET_X.get(), Config.VEIN_GAUGE_OFFSET_Y.get());
			if(Config.GAIN_LIST_DISPLAY.get()) {
				if (!ClientTickHandler.xpGains.isEmpty() && ClientTickHandler.xpGains.get(0).duration <= 0)
					ClientTickHandler.xpGains.remove(0);
				renderGains(guiGraphics, Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
			}
//			guiGraphics.pose().popPose();
		}
		if (ClientTickHandler.isRefreshTick()) {ClientTickHandler.resetTicks();}
	}
	
	private Map<String, Double> modifiers = new HashMap<>();
	private final LinkedHashMap<String, SkillLine> lineRenderers = new LinkedHashMap<>();

	private void renderSkillList(GuiGraphics graphics, double skillListX, double skillListY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * skillListX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* skillListY);
		if (ClientTickHandler.isRefreshTick()) {
			modifiers = core.getConsolidatedModifierMap(mc.player);
			List<String> skillsKeys = core.getData().getXpMap(null).keySet().stream()
					.filter(a -> Config.skills().skills().getOrDefault(a, SkillData.Builder.getDefault()).getShowInList())
					.sorted(Comparator.<String>comparingLong(a -> core.getData().getLevel(a, null)).reversed())
					.toList();
			var holderMap = lineRenderers;
			lineRenderers.clear();
			AtomicInteger yOffset = new AtomicInteger(0);
			skillGap = skillsKeys.stream()
					.map(skill -> fontRenderer.width(Component.translatable("pmmo."+skill).getString()))
					.max(Comparator.comparingInt(t -> t)).orElse(0);
			skillsKeys.forEach((skillKey)-> {
				var xpRaw = core.getData().getXpMap(null).getOrDefault(skillKey, new Experience());
				lineRenderers.put(skillKey, 
					xpRaw != holderMap.getOrDefault(skillKey, SkillLine.DEFAULT).xpValue()
					? new SkillLine(skillKey, modifiers.getOrDefault(skillKey, 1.0), xpRaw, yOffset.get(), skillGap)
					: new SkillLine(holderMap.get(skillKey), yOffset.get()));
				yOffset.getAndIncrement();
			});
		}
		
		lineRenderers.forEach((skill, line) -> {
			line.render(graphics, renderX, renderY, fontRenderer);
		});
	}
	
	private int maxCharge = 0;
	private int currentCharge = 0;
	
	private void renderVeinGauge(GuiGraphics graphics, double gaugeX, double gaugeY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * gaugeX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* gaugeY);
		if (ClientTickHandler.isRefreshTick()) {
			maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
			if (maxCharge > 0)
				currentCharge = (int)mc.player.getAttribute(CommonSetup.VEIN_AMOUNT).getValue();
		}
		if (currentCharge > 0) {
			graphics.drawString(fontRenderer, LangProvider.VEIN_LIMIT.asComponent(Config.VEIN_LIMIT.get()), renderX, renderY-11, 0xFFFFFFFF);
			graphics.drawString(fontRenderer, LangProvider.VEIN_CHARGE.asComponent(currentCharge, maxCharge), renderX, renderY, 0xFFFFFFFF);
		}
	}
	
	private void renderGains(GuiGraphics graphics, double listX, double listY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * listX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* listY);
		for (int i = 0; i < ClientTickHandler.xpGains.size(); i++) {
			ClientTickHandler.GainEntry entry = ClientTickHandler.xpGains.get(i);
			graphics.drawString(fontRenderer, entry.display(), renderX, 3+renderY+ (i*9), entry.getColor());
		}
	}
	
	private record SkillLine(String xpRaw, ResourceLocation icon, int iconSize, MutableComponent skillName, String bonusLine, Experience xpValue, int color, int yOffset, int skillGap) {
		public static SkillLine DEFAULT = new SkillLine("", Reference.mc("missing"), 16, Component.literal(""), "", new Experience(), 0xFFFFFF, 0, 0);
		public SkillLine(String skillName, double bonus, Experience xpValue, int yOffset, int skillGap) {
			this(rawXpLine(xpValue, skillName),
				Config.skills().skills().getOrDefault(skillName, SkillData.Builder.getDefault()).getIcon(),
					Config.skills().skills().getOrDefault(skillName, SkillData.Builder.getDefault()).getIconSize(),
				Component.translatable("pmmo."+skillName), 
				bonusLine(bonus), 
				xpValue,
				CoreUtils.getSkillColor(skillName),
				yOffset * 9,
				skillGap);
		}
		public SkillLine(SkillLine src, int yOffset) {
			this(src.xpRaw(), src.icon(), src.iconSize(), src.skillName(), src.bonusLine(), src.xpValue(), src.color, yOffset * 9, src.skillGap());
		}
		
		private static String rawXpLine(Experience xpValue, String skillKey) {
			double level = ((DataMirror)Core.get(LogicalSide.CLIENT).getData()).getXpWithPercentToNextLevel(xpValue);
			if (level > Config.skills().get(skillKey).getMaxLevel())
				return "" + Config.skills().get(skillKey).getMaxLevel();
			if (level > Config.server().levels().maxLevel())
				return "" + Config.server().levels().maxLevel();
			else
				return DP.dpCustom(Math.floor(level * 100D) / 100D, 2);
		}
		private static String bonusLine(double bonus) {
			if (bonus != 1d) {
				bonus = (Math.max(0, bonus) -1) * 100d;
				return (bonus >= 0 ? "+" : "-")+DP.dp(bonus)+"%";
			}
			else return "";
		}
		
		public void render(GuiGraphics graphics, int skillListX, int skillListY, Font fontRenderer) {
			int levelGap = fontRenderer.width(xpRaw());
			graphics.drawString(fontRenderer, xpRaw(), skillListX, skillListY + 3 + yOffset(), color());
			if (Config.SKILL_LIST_USE_ICONS.get())
				graphics.blit(icon(),
						skillListX + levelGap + 2,  skillListY + 3 + yOffset(),
						skillListX + levelGap + 11, skillListY + 12 + yOffset(),
						0, 1,0, 1);
			else
				graphics.drawString(fontRenderer, " | " + skillName.getString(), skillListX + levelGap, skillListY + 3 + yOffset(), color());
			graphics.drawString(fontRenderer, bonusLine, skillListX + levelGap + (Config.SKILL_LIST_USE_ICONS.get() ? 6 : skillGap()) + 9, skillListY + 3 + yOffset(), color());
		}
	}
}