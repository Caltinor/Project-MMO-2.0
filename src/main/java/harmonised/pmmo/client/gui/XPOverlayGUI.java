package harmonised.pmmo.client.gui;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.LogicalSide;

public class XPOverlayGUI
{
	private static Core core = Core.get(LogicalSide.CLIENT);
	private static int skillGap = 0;
	//private final ResourceLocation bar = new ResourceLocation(Reference.MOD_ID, "textures/gui/xpbar.png");
	private static Minecraft mc = Minecraft.getInstance();
	private static Font fontRenderer = mc.font;
	
	private static int ticksElapsed = 0;
	
	public static void tickGUI() {ticksElapsed++;}	
	private static boolean isRefreshTick() {return ticksElapsed >= 15;}
	private static void resetTicks() {ticksElapsed = 0;}

	public static void renderOverlay(RenderGameOverlayEvent event)
	{
		if(!mc.options.renderDebug){	
			PoseStack stack = event.getPoseStack();
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
		if (isRefreshTick()) {resetTicks();}
	}
	
	private static Map<String, Double> modifiers = new HashMap<>();
	private static List<String> skillsKeys = new ArrayList<>();

	private static void renderSkillList(PoseStack stack, int skillListX, int skillListY)
	{
		if (isRefreshTick()) {
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
	
	private static int maxCharge = 0;
	private static int currentCharge = 0;
	
	private static void renderVeinGauge(PoseStack stack, int gaugeX, int guageY) {
		if (isRefreshTick()) {
			maxCharge = VeinMiningLogic.getMaxChargeFromAllItems(mc.player);
			if (maxCharge > 0)
				currentCharge = VeinMiningLogic.getChargeFromAllItems(mc.player);
		}
		if (maxCharge > 0)
			GuiComponent.drawString(stack, fontRenderer, Component.translatable("pmmo.veinCharge", currentCharge, maxCharge), gaugeX, guageY, 0xFFFFFF);
	}
	
	private static List<GainEntry> xpGains = new ArrayList<>();
	
	public static void addToGainList(String skill, long amount) {
		SkillData skillData = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault());
		if (Config.GAIN_BLACKLIST.get().contains(skill) 
				|| (skillData.isSkillGroup() && skillData.getGroup().containsKey(skill)))
			return;
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
			GuiComponent.drawString(stack, fontRenderer, xpGains.get(i).display, listX, 3+listY+ (i*9), i);
		}
	}
	
	private static class GainEntry {
		int duration;
		final Component display;
		public GainEntry(String skill, long value) {
			this.duration = MsLoggy.DEBUG.logAndReturn(Config.GAIN_LIST_LINGER_DURATION.get()
								, LOG_CODE.GUI, "Gain Duration Set as: {}");
			display = Component.literal("+"+value+" ")
					.append(Component.translatable("pmmo."+skill))
					.setStyle(Core.get(LogicalSide.CLIENT).getDataConfig().getSkillStyle(skill));
		}
		public void downTick() {
			duration--;
		}
	}
}