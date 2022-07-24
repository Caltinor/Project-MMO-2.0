package harmonised.pmmo.client.gui;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;

import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.LogicalSide;

public class XPOverlayGUI implements IGuiOverlay
{
	private Core core = Core.get(LogicalSide.CLIENT);
	private int skillGap = 0;
	private Minecraft mc;
	private Font fontRenderer;

	@Override
	public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height){
		if (mc == null)
			mc = Minecraft.getInstance();
		if (fontRenderer == null)
			fontRenderer = mc.font;
		
		if(!mc.options.renderDebug){	
			stack.pushPose();
			RenderSystem.enableBlend();
			
			if(Config.SKILL_LIST_DISPLAY.get())
				renderSkillList(stack, Config.SKILL_LIST_OFFSET_X.get(), Config.SKILL_LIST_OFFSET_Y.get());
			if(Config.VEIN_GAUGE_DISPLAY.get())
				renderVeinGauge(stack, Config.VEIN_GAUGE_OFFSET_X.get(), mc.getWindow().getGuiScaledHeight() - Config.VEIN_GAUGE_OFFSET_Y.get());
			if(Config.GAIN_LIST_DISPLAY.get()) {
				if (ClientTickHandler.xpGains.size() >= 1 && ClientTickHandler.xpGains.get(0).duration <= 0)
					ClientTickHandler.xpGains.remove(0);
				renderGains(stack, mc.getWindow().getGuiScaledWidth()/2 + Config.GAIN_LIST_OFFSET_X.get(), Config.GAIN_LIST_OFFSET_Y.get());
			}
			stack.popPose();
		}
		if (ClientTickHandler.isRefreshTick()) {ClientTickHandler.resetTicks();}
	}
	
	private Map<String, Double> modifiers = new HashMap<>();
	private List<String> skillsKeys = new ArrayList<>();

	private void renderSkillList(PoseStack stack, int skillListX, int skillListY)
	{
		if (ClientTickHandler.isRefreshTick()) {
			modifiers = core.getConsolidatedModifierMap(mc.player);	
			skillsKeys = new ArrayList<>();
			core.getData().getXpMap(null).keySet().stream().forEach(entry -> skillsKeys.add(entry));
			skillsKeys.sort(Comparator.<String>comparingLong(a -> core.getData().getXpRaw(null, a)).reversed());
		}
			
		for(int i = 0; i < skillsKeys.size(); i++) {
			String skillKey = skillsKeys.get(i);
			skillGap = fontRenderer.width(Component.translatable("pmmo." + skillKey).getString()) > skillGap 
					? fontRenderer.width(Component.translatable("pmmo." + skillKey).getString()) 
					: skillGap;
			long currentXP = core.getData().getXpRaw(null, skillKey);
			double level = ((DataMirror)core.getData()).getXpWithPercentToNextLevel(core.getData().getXpRaw(null, skillKey));
			int skillMaxLevel = SkillsConfig.SKILLS.get().getOrDefault(skillKey, SkillData.Builder.getDefault()).getMaxLevel();
			level = level > skillMaxLevel ? skillMaxLevel : level;
			String tempString = DP.dp(Math.floor(level * 100D) / 100D);
			int color = core.getDataConfig().getSkillColor(skillKey);
			
			if(level >= Config.MAX_LEVEL.get())
				tempString = "" + Config.MAX_LEVEL.get();
			
			int listIndex = i * 9;
			int levelGap = fontRenderer.width(tempString);
			GuiComponent.drawString(stack, fontRenderer, tempString, skillListX + 4, skillListY + 3 + listIndex, color);
			GuiComponent.drawString(stack, fontRenderer, " | " + Component.translatable("pmmo." + skillKey).getString(), skillListX + levelGap + 4, skillListY + 3 + listIndex, color);
			GuiComponent.drawString(stack, fontRenderer, " | " + DP.dprefix(currentXP), skillListX + levelGap + skillGap + 13, skillListY + 3 + listIndex, color);
			if (modifiers.getOrDefault(skillKey, 1d) != 1d) {
				double bonus = (Math.max(0, modifiers.get(skillKey)) -1) * 100;
				tempString = (bonus >= 0 ? "+" : "-")+DP.dp(bonus)+"%";
				GuiComponent.drawString(stack, fontRenderer, tempString, skillListX + levelGap + skillGap + 50, skillListY + 3 + listIndex, color);
			}
		}
	}
	
	private int maxCharge = 0;
	private int currentCharge = 0;
	
	private void renderVeinGauge(PoseStack stack, int gaugeX, int gaugeY) {
		if (ClientTickHandler.isRefreshTick()) {
			maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
			if (maxCharge > 0)
				currentCharge = VeinTracker.getCurrentCharge();
		}
		if (currentCharge > 0) {
			GuiComponent.drawString(stack, fontRenderer, Component.translatable("pmmo.veinLimit", Config.VEIN_LIMIT.get()), gaugeX, gaugeY-11, 0xFFFFFF);
			GuiComponent.drawString(stack, fontRenderer, Component.translatable("pmmo.veinCharge", currentCharge, maxCharge), gaugeX, gaugeY, 0xFFFFFF);
		}
	}
	
	private void renderGains(PoseStack stack, int listX, int listY) {
		for (int i = 0; i < ClientTickHandler.xpGains.size(); i++) {			
			GuiComponent.drawString(stack, fontRenderer, ClientTickHandler.xpGains.get(i).display, listX, 3+listY+ (i*9), i);
		}
	}
	
	
}