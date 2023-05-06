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
				renderSkillList(stack, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
			if(Config.VEIN_ENABLED.get() && Config.VEIN_GAUGE_DISPLAY.get())
				renderVeinGauge(stack, Config.VEIN_GAUGE_OFFSET_X.get(), Config.VEIN_GAUGE_OFFSET_Y.get());
			if(Config.GAIN_LIST_DISPLAY.get()) {
				if (ClientTickHandler.xpGains.size() >= 1 && ClientTickHandler.xpGains.get(0).duration <= 0)
					ClientTickHandler.xpGains.remove(0);
				renderGains(stack, Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
			}
			stack.popPose();
		}
		if (ClientTickHandler.isRefreshTick()) {ClientTickHandler.resetTicks();}
	}
	
	private Map<String, Double> modifiers = new HashMap<>();
	private List<String> skillsKeys = new ArrayList<>();
	private LinkedHashMap<String, SkillLine> lineRenderers = new LinkedHashMap<>();

	private void renderSkillList(PoseStack stack, double skillListX, double skillListY)
	{
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
					.map(skill -> fontRenderer.width(new TranslatableComponent("pmmo."+skill).getString()))
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
			line.render(stack, renderX, renderY, fontRenderer);
		});
	}
	
	private int maxCharge = 0;
	private int currentCharge = 0;
	
	private void renderVeinGauge(PoseStack stack, double gaugeX, double gaugeY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * gaugeX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* gaugeY);
		if (ClientTickHandler.isRefreshTick()) {
			maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
			if (maxCharge > 0)
				currentCharge = VeinTracker.getCurrentCharge();
		}
		if (currentCharge > 0) {
			GuiComponent.drawString(stack, fontRenderer, LangProvider.VEIN_LIMIT.asComponent(Config.VEIN_LIMIT.get()), renderX, renderY-11, 0xFFFFFF);
			GuiComponent.drawString(stack, fontRenderer, LangProvider.VEIN_CHARGE.asComponent(currentCharge, maxCharge), renderX, renderY, 0xFFFFFF);
		}
	}
	
	private void renderGains(PoseStack stack, double listX, double listY) {
		final int renderX = (int)((double)mc.getWindow().getGuiScaledWidth() * listX);
		final int renderY = (int)((double)mc.getWindow().getGuiScaledHeight()* listY);
		for (int i = 0; i < ClientTickHandler.xpGains.size(); i++) {			
			GuiComponent.drawString(stack, fontRenderer, ClientTickHandler.xpGains.get(i).display, renderX, 3+renderY+ (i*9), i);
		}
	}
	
	private record SkillLine(String xpRaw, MutableComponent skillName, String bonusLine, long xpValue, int color, int yOffset, int skillGap) {
		public static SkillLine DEFAULT = new SkillLine("", new TextComponent(""), "", -1, 0xFFFFFF, 0, 0);
		public SkillLine(String skillName, double bonus, long xpValue, int yOffset, int skillGap) {
			this(rawXpLine(xpValue, skillName), 
				new TranslatableComponent("pmmo."+skillName), 
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
		
		public void render(PoseStack stack, int skillListX, int skillListY, Font fontRenderer) {
			int levelGap = fontRenderer.width(xpRaw());
			GuiComponent.drawString(stack, fontRenderer, xpRaw(), skillListX, skillListY + 3 + yOffset(), color());
			GuiComponent.drawString(stack, fontRenderer, " | " + skillName.getString(), skillListX + levelGap, skillListY + 3 + yOffset(), color());
			GuiComponent.drawString(stack, fontRenderer, " | " + DP.dprefix(xpValue()), skillListX + levelGap + skillGap() + 9, skillListY + 3 + yOffset(), color());
			GuiComponent.drawString(stack, fontRenderer, bonusLine, skillListX + levelGap + skillGap() + 46, skillListY + 3 + yOffset(), color());
		}
	}
}