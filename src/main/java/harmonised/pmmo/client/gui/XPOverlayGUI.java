package harmonised.pmmo.client.gui;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.LogicalSide;

public class XPOverlayGUI
{
	private static Core core = Core.get(LogicalSide.CLIENT);
	private static int levelGap = 0, 
		skillGap = 0;
	//private final ResourceLocation bar = new ResourceLocation(Reference.MOD_ID, "textures/gui/xpbar.png");
	private static Minecraft mc = Minecraft.getInstance();
	private static Font fontRenderer = mc.font;

	public static void renderOverlay(RenderGameOverlayEvent event)
	{
		PoseStack stack = event.getMatrixStack();
		stack.pushPose();
		RenderSystem.enableBlend();
		
		if(Config.SKILL_LIST_DISPLAY.get())
			renderSkillList(stack, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
		if(Config.VEIN_GAUGE_DISPLAY.get())
			renderVeinGauge(stack, Config.VEIN_GAUGE_OFFSET_X.get(), event.getWindow().getGuiScaledHeight() - Config.VEIN_GAUGE_OFFSET_Y.get());
		if(Config.GAIN_LIST_DISPLAY.get()) {
			if (xpGains.size() >= 1 && xpGains.get(0).duration <= 0)
				xpGains.remove(0);
			renderGains(stack, event.getWindow().getGuiScaledWidth()/2 + Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
		}
		stack.popPose();
	}

	private static void renderSkillList(PoseStack stack, int skillListX, int skillListY)
	{
		if(!mc.options.renderDebug)
		{
			Map<String, Double> modifiers = core.getConsolidatedModifierMap(mc.player);
			List<String> skillsKeys = new ArrayList<>(); 
			core.getData().getXpMap(null).keySet().stream().forEach(entry -> skillsKeys.add(entry));
			skillsKeys.sort(Comparator.<String>comparingLong(a -> core.getData().getXpRaw(null, a)).reversed());

			
			for(int i = 0; i < skillsKeys.size(); i++) {
				String skillKey = skillsKeys.get(i);
				skillGap = fontRenderer.width(new TranslatableComponent("pmmo." + skillKey).getString()) > skillGap 
						? fontRenderer.width(new TranslatableComponent("pmmo." + skillKey).getString()) 
						: skillGap;
				long currentXP = core.getData().getXpRaw(null, skillKey);
				double level = ((DataMirror)core.getData()).getXpWithPercentToNextLevel(core.getData().getXpRaw(null, skillKey));
				String tempString = DP.dp(Math.floor(level * 100D) / 100D);
				int color = core.getDataConfig().getSkillColor(skillKey);
				
				if(level >= Config.MAX_LEVEL.get())
					tempString = "" + Config.MAX_LEVEL.get();
				
				int listIndex = i * 9;
				levelGap = fontRenderer.width(tempString);
				GuiComponent.drawString(stack, fontRenderer, tempString, skillListX + 4, skillListY + 3 + listIndex, color);
				GuiComponent.drawString(stack, fontRenderer, " | " + new TranslatableComponent("pmmo." + skillKey).getString(), skillListX + levelGap + 4, skillListY + 3 + listIndex, color);
				GuiComponent.drawString(stack, fontRenderer, " | " + DP.dprefix(currentXP), skillListX + levelGap + skillGap + 13, skillListY + 3 + listIndex, color);
				if (modifiers.getOrDefault(skillKey, 1d) != 1d) {
					double bonus = (Math.max(0, modifiers.get(skillKey)) -1) * 100;
					tempString = (bonus >= 0 ? "+" : "-")+DP.dp(bonus)+"%";
					GuiComponent.drawString(stack, fontRenderer, tempString, skillListX + levelGap + skillGap + 50, skillListY + 3 + listIndex, color);
				}
			}
		}
	}
	
	private static void renderVeinGauge(PoseStack stack, int gaugeX, int guageY) {
		int maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
		if (maxCharge > 0) {
			int currentCharge = VeinMiningLogic.getChargeFromAllItems(mc.player);
			GuiComponent.drawString(stack, fontRenderer, new TranslatableComponent("pmmo.veinCharge", currentCharge, maxCharge), gaugeX, guageY, 0xFFFFFF);
		}
	}
	
	private static List<GainEntry> xpGains = new ArrayList<>();
	public static void addToGainList(String skill, long amount) {
		if (xpGains.size() >= Config.GAIN_LIST_SIZE.get()) 
			xpGains.remove(0);
		xpGains.add(new GainEntry(skill, amount));
	}
	
	public static void tickDownGainList() {
		for (GainEntry gain : xpGains) {
			gain.downTick();
		}
	}
	
	private static void renderGains(PoseStack stack, int listX, int listY) {
		for (int i = 0; i < xpGains.size(); i++) {
			TextComponent gain = new TextComponent("+"+String.valueOf(xpGains.get(i).value)+" ");
			gain.append(new TranslatableComponent("pmmo."+xpGains.get(i).skill));
			gain.setStyle(Core.get(LogicalSide.CLIENT).getDataConfig().getSkillStyle(xpGains.get(i).skill));
			GuiComponent.drawString(stack, fontRenderer, gain, listX, 3+listY+ (i*9), i);
		}
	}
	
	private static class GainEntry {
		int duration;
		String skill;
		long value;
		public GainEntry(String skill, long value) {
			this.skill = skill;
			this.value = value;
			this.duration = MsLoggy.DEBUG.logAndReturn(Config.GAIN_LIST_LINGER_DURATION.get()
								, LOG_CODE.GUI, "Gain Duration Set as: {}");
		}
		public void downTick() {
			duration--;
		}
	}
}