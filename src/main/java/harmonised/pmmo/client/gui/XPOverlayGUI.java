package harmonised.pmmo.client.gui;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.fml.LogicalSide;

public class XPOverlayGUI implements IIngameOverlay
{
	private Core core = Core.get(LogicalSide.CLIENT);
	private int skillGap = 0;
	private Minecraft mc;
	private Font fontRenderer;

	@Override
	public void render(ForgeIngameGui gui, PoseStack stack, float partialTick, int width, int height){
		if (mc == null)
			mc = Minecraft.getInstance();
		if (fontRenderer == null)
			fontRenderer = mc.font;
		
		if(!mc.options.renderDebug){	
			stack.pushPose();
			RenderSystem.enableBlend();
			
			if(Config.SKILL_LIST_DISPLAY.get())
				renderSkillList(mc.getWindow(), stack, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
			if(Config.VEIN_ENABLED.get() && Config.VEIN_GAUGE_DISPLAY.get())
				renderVeinGauge(mc.getWindow(), stack, Config.VEIN_GAUGE_OFFSET_X.get(), Config.VEIN_GAUGE_OFFSET_Y.get());
			if(Config.GAIN_LIST_DISPLAY.get()) {
				if (ClientTickHandler.xpGains.size() >= 1 && ClientTickHandler.xpGains.get(0).duration <= 0)
					ClientTickHandler.xpGains.remove(0);
				renderGains(mc.getWindow(), stack, Config.GAIN_LIST_CENTERED.get(), Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
			}
			stack.popPose();
		}
		if (ClientTickHandler.isRefreshTick()) {ClientTickHandler.resetTicks();}
	}
	
	private Map<String, Double> modifiers = new HashMap<>();
	private List<String> skillsKeys = new ArrayList<>();
	private LinkedHashMap<String, SkillLine> lineRenderers = new LinkedHashMap<>();

	private void renderSkillList(com.mojang.blaze3d.platform.Window window, PoseStack stack, float offsetX, float offsetY)
	{
		int posX = (int) Math.round(offsetX * window.getGuiScaledWidth());
		int posY = (int) Math.round(offsetY * window.getGuiScaledHeight());
		int lineHeight = fontRenderer.lineHeight;
		if (ClientTickHandler.isRefreshTick()) {
			modifiers = core.getConsolidatedModifierMap(mc.player);	
			skillsKeys = core.getData().getXpMap(null).keySet().stream()
					.sorted(Comparator.<String>comparingLong(a -> core.getData().getXpRaw(null, a)).reversed())
					.toList();
			var holderMap = lineRenderers;
			lineRenderers.clear();
			AtomicInteger line = new AtomicInteger(0);
			skillGap = skillsKeys.stream()
					.map(skill -> fontRenderer.width(new TranslatableComponent("pmmo."+skill).getString()))
					.max(Comparator.comparingInt(t -> t)).orElse(0);
			skillsKeys.forEach((skillKey)-> {
				var xpRaw = core.getData().getXpRaw(null, skillKey);
				lineRenderers.put(skillKey, 
					xpRaw != holderMap.getOrDefault(skillKey, SkillLine.DEFAULT).xpValue()
					? new SkillLine(skillKey, modifiers.getOrDefault(skillKey, 1.0), xpRaw, line.get(), lineHeight, skillGap)
					: new SkillLine(holderMap.get(skillKey), line.get(), lineHeight));
				line.getAndIncrement();
			});
		}
		
		lineRenderers.forEach((skill, line) -> {
			line.render(stack, posX, posY, fontRenderer);
		});
	}
	
	private int maxCharge = 0;
	private int currentCharge = 0;
	
	private void renderVeinGauge(com.mojang.blaze3d.platform.Window window, PoseStack stack, float offsetX, float offsetY) {
		int posX = (int) Math.round(offsetX * window.getGuiScaledWidth());
		int posY = (int) Math.round((1.0 - offsetY) * window.getGuiScaledHeight());
		int lineHeight = (int) Math.round(fontRenderer.lineHeight * 1.2);
		if (ClientTickHandler.isRefreshTick()) {
			maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
			if (maxCharge > 0)
				currentCharge = VeinTracker.getCurrentCharge();
		}
		int percentCharged = (int) Math.round((float) currentCharge / (float) maxCharge);
		int veinLimit = Config.VEIN_LIMIT.get();
		int maxBlocks = Math.min(currentCharge, veinLimit) / Config.DEFAULT_CONSUME.get();
		if (currentCharge > 0 && maxCharge > 0) {
			GuiComponent.drawString(stack, fontRenderer, LangProvider.VEIN_CHARGE.asComponent(percentCharged, maxBlocks), posX, posY - lineHeight, 0xFFFFFF);
		}
	}
	
	private void renderGains(com.mojang.blaze3d.platform.Window window, PoseStack stack, boolean centered, float offsetX, float offsetY) {
		int posX = (int) Math.round(offsetX * window.getGuiScaledWidth());
		int posY = (int) Math.round(offsetY * window.getGuiScaledHeight());
		int lineHeight = (int) Math.round(fontRenderer.lineHeight * 1.2);
		for (int i = 0; i < ClientTickHandler.xpGains.size(); i++) {
			if (centered) {
				GuiComponent.drawCenteredString(stack, fontRenderer, ClientTickHandler.xpGains.get(i).display, posX, posY + (i * lineHeight), i);
			} else {
				GuiComponent.drawString(stack, fontRenderer, ClientTickHandler.xpGains.get(i).display, posX, posY + (i * lineHeight), i);
			}
		}
	}
	
	private record SkillLine(String xpRaw, MutableComponent skillName, String bonusLine, long xpValue, int color, int line, int skillGap) {
		public static SkillLine DEFAULT = new SkillLine("", new TextComponent(""), "", -1, 0xFFFFFF, 0, 0);
		public SkillLine(String skillName, double bonus, long xpValue, int line, int lineHeight, int skillGap) {
			this(rawXpLine(xpValue, skillName), 
				new TranslatableComponent("pmmo."+skillName), 
				bonusLine(bonus), 
				xpValue,
				CoreUtils.getSkillColor(skillName),
					line * lineHeight,
				skillGap);
		}
		public SkillLine(SkillLine src, int line, int lineHeight) {
			this(src.xpRaw(), src.skillName(), src.bonusLine(), src.xpValue(), src.color, line * lineHeight, src.skillGap());
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
		
		public void render(PoseStack stack, int skillListX, int skillListY, Font fontRenderer) {
			int levelGap = fontRenderer.width(xpRaw());
			GuiComponent.drawString(stack, fontRenderer, xpRaw(), skillListX + 4, skillListY + 3 + line(), color());
			GuiComponent.drawString(stack, fontRenderer, " | " + skillName.getString(), skillListX + levelGap + 4, skillListY + 3 + line(), color());
			GuiComponent.drawString(stack, fontRenderer, " | " + DP.dprefix(xpValue()), skillListX + levelGap + skillGap() + 13, skillListY + 3 + line(), color());
			GuiComponent.drawString(stack, fontRenderer, bonusLine, skillListX + levelGap + skillGap() + 50, skillListY + 3 + line(), color());
		}
	}
}
