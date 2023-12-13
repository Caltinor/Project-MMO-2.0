package harmonised.pmmo.client.gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import harmonised.pmmo.setup.datagen.LangProvider;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

public class XPOverlayGUI implements IGuiOverlay
{
	private Core core = Core.get(LogicalSide.CLIENT);
	private int skillGap = 0;
	private Minecraft mc;
	private Font fontRenderer;

	@Override
	public void render(ExtendedGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (mc == null)
			mc = Minecraft.getInstance();
		if (fontRenderer == null)
			fontRenderer = mc.font;
		
		if(!mc.getDebugOverlay().showDebugScreen()){
			guiGraphics.pose().pushPose();
			RenderSystem.enableBlend();
			
			if(Config.SKILL_LIST_DISPLAY.get())
				renderSkillList(guiGraphics, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
			if(Config.VEIN_ENABLED.get() && Config.VEIN_GAUGE_DISPLAY.get())
				renderVeinGauge(guiGraphics, Config.VEIN_GAUGE_OFFSET_X.get(), Config.VEIN_GAUGE_OFFSET_Y.get());
			if(Config.GAIN_LIST_DISPLAY.get()) {
				if (ClientTickHandler.xpGains.size() >= 1 && ClientTickHandler.xpGains.get(0).duration <= 0)
					ClientTickHandler.xpGains.remove(0);
				renderGains(guiGraphics, Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
			}
			guiGraphics.pose().popPose();
		}
		if (ClientTickHandler.isRefreshTick()) {ClientTickHandler.resetTicks();}
	}
	
	private Map<String, Double> modifiers = new HashMap<>();
	private List<String> skillsKeys = new ArrayList<>();
	private LinkedHashMap<String, SkillLine> lineRenderers = new LinkedHashMap<>();

	private void renderSkillList(GuiGraphics graphics, double skillListX, double skillListY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * skillListX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* skillListY);
		if (ClientTickHandler.isRefreshTick()) {
			modifiers = core.getConsolidatedModifierMap(mc.player);	
			skillsKeys = core.getData().getXpMap(null).keySet().stream()
					.sorted(Comparator.<String>comparingLong(a -> core.getData().getXpRaw(null, a)).reversed())
					.toList();
			var holderMap = lineRenderers;
			lineRenderers.clear();
			AtomicInteger yOffset = new AtomicInteger(0);
			skillGap = skillsKeys.stream()
					.map(skill -> fontRenderer.width(Component.translatable("pmmo."+skill).getString()))
					.max(Comparator.comparingInt(t -> t)).orElse(0);
			skillsKeys.forEach((skillKey)-> {
				var xpRaw = core.getData().getXpRaw(null, skillKey);
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
				currentCharge = VeinTracker.getCurrentCharge();
		}
		if (currentCharge > 0) {
			graphics.drawString(fontRenderer, LangProvider.VEIN_LIMIT.asComponent(Config.VEIN_LIMIT.get()), renderX, renderY-11, 0xFFFFFF);
			graphics.drawString(fontRenderer, LangProvider.VEIN_CHARGE.asComponent(currentCharge, maxCharge), renderX, renderY, 0xFFFFFF);
		}
	}
	
	private void renderGains(GuiGraphics graphics, double listX, double listY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * listX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* listY);
		for (int i = 0; i < ClientTickHandler.xpGains.size(); i++) {
			ClientTickHandler.GainEntry entry = ClientTickHandler.xpGains.get(i);
			graphics.drawString(fontRenderer, entry.display(), renderX, 3+renderY+ (i*9), entry.display().getStyle().getColor().getValue());
		}
	}
	
	private record SkillLine(String xpRaw, MutableComponent skillName, String bonusLine, long xpValue, int color, int yOffset, int skillGap) {
		public static SkillLine DEFAULT = new SkillLine("", Component.literal(""), "", -1, 0xFFFFFF, 0, 0);
		public SkillLine(String skillName, double bonus, long xpValue, int yOffset, int skillGap) {
			this(rawXpLine(xpValue, skillName), 
				Component.translatable("pmmo."+skillName), 
				bonusLine(bonus), 
				xpValue,
				CoreUtils.getSkillColor(skillName),
				yOffset * 9,
				skillGap);
		}
		public SkillLine(SkillLine src, int yOffset) {
			this(src.xpRaw(), src.skillName(), src.bonusLine(), src.xpValue(), src.color, yOffset * 9, src.skillGap());
		}
		
		private static String rawXpLine(long xpValue, String skillKey) {
			double level = ((DataMirror)Core.get(LogicalSide.CLIENT).getData()).getXpWithPercentToNextLevel(xpValue);
			int skillMaxLevel = SkillsConfig.SKILLS.get().getOrDefault(skillKey, SkillData.Builder.getDefault()).getMaxLevel();
			level = level > skillMaxLevel ? skillMaxLevel : level;
			return level >= Config.MAX_LEVEL.get() 
					? "" + Config.MAX_LEVEL.get() 
					: DP.dp(Math.floor(level * 100D) / 100D);
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
			graphics.drawString(fontRenderer, " | " + skillName.getString(), skillListX + levelGap, skillListY + 3 + yOffset(), color());
			graphics.drawString(fontRenderer, " | " + DP.dprefix(xpValue()), skillListX + levelGap + skillGap() + 9, skillListY + 3 + yOffset(), color());
			graphics.drawString(fontRenderer, bonusLine, skillListX + levelGap + skillGap() + 46, skillListY + 3 + yOffset(), color());
		}
	}
}