package harmonised.pmmo.client.gui;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.LogicalSide;

public class XPOverlayGUI extends GuiComponent
{
	private static Core core = Core.get(LogicalSide.CLIENT);
	public static boolean configChanged = false;
	private static int levelGap = 0, 
		skillGap = 0;
	private static double skillListOffsetX = Config.SKILL_LIST_OFFSET_X.get(),
		skillListOffsetY = Config.SKILL_LIST_OFFSET_Y.get();
	private static boolean showSkillsListAtCorner = Config.SKILL_LIST_DISPLAY.get();
	//private final ResourceLocation bar = new ResourceLocation(Reference.MOD_ID, "textures/gui/xpbar.png");
	private static Minecraft mc = Minecraft.getInstance();
	private static Font fontRenderer = mc.font;
	private static int maxLevel = Config.MAX_LEVEL.get();
	private static PoseStack stack;


	public static void renderOverlay(RenderGameOverlayEvent event)
	{
		stack = event.getMatrixStack();
		stack.pushPose();
		RenderSystem.enableBlend();
		try
		{
			Window sr = mc.getWindow();
			if(configChanged)
			{
				refreshClientSettings();
				configChanged = false;
			}
			
			int skillListX = (int) (sr.getGuiScaledWidth() * skillListOffsetX);
			int skillListY = (int) (sr.getGuiScaledHeight() * skillListOffsetY);
			
			if(showSkillsListAtCorner)
				renderSkillList(skillListX, skillListY);
		}
		catch(Exception e)
		{
			MsLoggy.error("Error rendering PMMO GUI {}", e);
			e.printStackTrace();
		}
		//Causes black screen
//			RenderSystem.disableBlend();
//			RenderSystem.color(255, 255, 255);
		stack.popPose();
	}

	/*private void doRayTrace()
	{
		if(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK)
		{
			lookingAtBlock = true;

			blockPos = ((BlockHitResult) mc.hitResult).getBlockPos();
			blockState = mc.level.getBlockState(blockPos);

			if(lastBlockPos == null)
				updateLastBlock();

			if(!blockPos.equals(lastBlockPos) && XP.isPlayerSurvival(player))
				updateVein();

			if(lastBlockState.getBlock().equals(blockState.getBlock()))
				lastVeinBlockUpdate = System.nanoTime();
//
			if(!isVeining && System.nanoTime() - lastVeinBlockUpdate > 100000000L)
				updateLastBlock();

			canBreak = XP.checkReq(player, lastBlockRegKey, JType.REQ_BREAK);
		}
		else
			lookingAtBlock = false;
	}*/

	/*private void updateLastBlock()
	{
		lastBlockState = blockState;
		lastBlockPos = blockPos;
		if(lastBlockState.getBlock().getRegistryName() != null)
			lastBlockRegKey = lastBlockState.getBlock().getRegistryName().toString();
		canVein = WorldTickHandler.canVeinGlobal(lastBlockRegKey, player) && WorldTickHandler.canVeinDimension(lastBlockRegKey, player);
		lastBlockTransKey = lastBlockState.getBlock().getDescriptionId();
	}*/

	/*public static void updateVein()
	{
		if(blockState == null)
			return;
		veinShapeSet.clear();
		VeinInfo veinInfo = new VeinInfo(mc.level, blockState, blockPos, player.getMainHandItem());
		ArrayList<BlockPos> veinShape = WorldTickHandler.getVeinShape(veinInfo, WorldTickHandler.getVeinLeft(player), WorldTickHandler.getVeinCost(blockState, blockPos, player), false, false);
		int veinShapeSize = veinShape.size();
		int matches = 0;
		for(int i = 0; i < veinShapeSize; i++)
		{
			BlockPos pos = veinShape.get(i);
			if(pos.equals(blockPos))
				continue;
			if(++matches > breakAmount || matches > Config.getPreferencesMap(player).getOrDefault("maxVeinDisplay", (double) Config.forgeConfig.maxVeinDisplay.get()))
				break;
			veinShapeSet.add(pos);
		}
	}*/

	/*private void doXpDrops(PoseStack stack)
	{
		if(xpDropsAttachedToBar)
		{
			if(cooldown <= 0)
				xpDropOffsetCap = -9;
			else if (barKey || xpLeftDisplayAlwaysOn)
			{
				if(aSkill.xp >= maxXp)
					xpDropOffsetCap = 25;
				else
					xpDropOffsetCap = 34;
			}
			else
				xpDropOffsetCap = 16;

			if(xpDropOffset > xpDropOffsetCap)
				xpDropOffset -= 1d * timeDiff / 10000000;

			if(xpDropOffset < xpDropOffsetCap)
				xpDropOffset = xpDropOffsetCap;
		}

		for(int i = 0; i < xpDrops.size(); i++)		//update Xp Drops
		{
			xpDrop = xpDrops.get(i);
			xpDrop.age += timeDiff / 5000000;
			decayRate = 0.75f + (1 * xpDrops.size() * 0.02f);			//Xp Drop Y
			decayAmount = decayRate * timeDiff / 10000000;

			if(!mc.isPaused())
			{
				if(((xpDrop.Y - decayAmount < 0) && xpDrop.age >= xpDropDecayAge) || !showXpDrops || (!xpDropsAttachedToBar && xpDrop.age >= xpDropDecayAge))
				{
					tempASkill = skills.get(xpDrop.skill);

					if(!xpDrop.skip)
						activeSkill = xpDrop.skill;

					decayRate = xpDrop.gainedXp * 0.03 * timeDiff / 10000000D;
					if(stackXpDrops)
					{
						if(decayRate < 0.1)
							decayRate = 0.1;
					}
					else
					if(decayRate < 1)
						decayRate = 1;

					if(xpDrop.gainedXp - (decayRate) < 0)
					{
						tempASkill.goalXp += xpDrop.gainedXp;
						xpDrop.gainedXp = 0;
					}
					else
					{
						tempASkill.goalXp += decayRate;
						xpDrop.gainedXp -= decayRate;
					}

					tempASkill.goalPos = XP.levelAtXpDecimal(tempASkill.goalXp);
				}
			}

			if(showXpDrops)
			{
				if(xpDropOffset == xpDropOffsetCap)
					xpDrop.Y -= decayAmount;

				if(xpDrop.Y < (i * 9) - xpDropYLimit)
					xpDrop.Y = (i * 9) - xpDropYLimit;

				tempInt = (int) Math.floor(xpDrop.Y * xpDropOpacityPerTime); //Opacity Loss

				if(tempInt < 0)
					tempInt = -tempInt;

				if(tempInt > xpDropMaxOpacity)
					tempAlpha = 0;
				else
					tempAlpha = (int) Math.floor(xpDropMaxOpacity - tempInt);

				if(tempAlpha > 3)
					drawCenteredString(stack, fontRenderer, "+" + DP.dprefix(xpDrop.gainedXp) + " " + new TranslatableComponent("pmmo." + xpDrop.skill).getString(), xpDropPosX + (barWidth / 2), (int) xpDrop.Y + (int) xpDropOffset + xpDropPosY, (tempAlpha << 24) |+ Skill.getSkillColor(xpDrop.skill));
			}
		}

		if(xpDrops.size() > 0 && xpDrops.get(0).gainedXp <= 0)
			xpDrops.remove(0);
	}*/

	/*private void doXpBar()
	{
		themePos += (2.5 + 7.5 * (aSkill.pos % Math.floor(aSkill.pos))) * (timeDiff / 1000000D);

		if(themePos > 10000)
			themePos =  themePos % 10000;

		if(cooldown > 0)				//Xp Bar
		{
			stack.pushPose();
			RenderSystem.enableBlend();
			try
			{
				RenderSystem.setShaderTexture(0, bar);

				blit(stack, barPosX, barPosY + 10, 0, 0, barWidth, barHeight);
				if(!Config.forgeConfig.xpBarTheme.get())
				{
					blit(stack, barPosX, barPosY + 10, 0, barHeight * 1, (int) Math.floor(barWidth * (aSkill.pos - Math.floor(aSkill.pos))), barHeight);
				}
				else
				{
					tempInt = (int) Math.floor((barWidth) * (aSkill.pos - Math.floor(aSkill.pos)));

					if(tempInt > 100)
						tempInt = 100;

					if(aSkill.pos >= maxLevel)
						tempInt = 100;

					blit(stack, barPosX, barPosY + 10, 0, barHeight*3, barWidth - 1, barHeight);
					blit(stack, barPosX + 1, barPosY + 10, 1 + (int)(Math.floor((double) themePos / 100)), barHeight*2, tempInt, barHeight);
				}
				if(aSkill.pos >= maxLevel)
					drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.levelDisplay", new TranslatableComponent("pmmo." + activeSkill.toLowerCase()).getString(), maxLevel).getString(), barPosX + (barWidth / 2), barPosY, Skill.getSkillColor(activeSkill));
				else
					drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.levelDisplay", new TranslatableComponent("pmmo." + activeSkill.toLowerCase()).getString(), DP.dp(Math.floor(aSkill.pos * 100D) / 100D)).getString(), barPosX + (barWidth / 2), barPosY, Skill.getSkillColor(activeSkill));

				if((barKey || xpLeftDisplayAlwaysOn))
				{
					if(aSkill.xp >= maxXp)
						drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.maxLevel").getString(), barPosX + (barWidth / 2), 17 + barPosY, Skill.getSkillColor(activeSkill));
					else
					{
						if(goalXp >= maxXp)
							goalXp =  maxXp;

						goalXp = XP.xpAtLevel(XP.levelAtXp(aSkill.xp) + 1);
						drawCenteredString(stack, fontRenderer, DP.dprefix(aSkill.xp) + " / " + DP.dprefix(goalXp), barPosX + (barWidth / 2), 17 + barPosY, Skill.getSkillColor(activeSkill));
						drawCenteredString(stack, fontRenderer,  new TranslatableComponent("pmmo.xpLeft", DP.dprefix(goalXp - aSkill.xp)).getString(), barPosX + (barWidth / 2), 26 + barPosY, Skill.getSkillColor(activeSkill));
					}
				}
			}
			catch(Exception e)
			{
				LOGGER.error("Error rendering PMMO GUI XP Bar", e);
			}
			RenderSystem.disableBlend();
			stack.popPose();
		}
	}*/

	/*private void doVein()
	{   // VEIN STUFF
		veinLeft = Config.getAbilitiesMap(player).getOrDefault("veinLeft", 0D);
		veinPosGoal = veinLeft / maxVeinCharge;
		addAmount = (veinPosGoal - veinPos) * (timeDiff / 200000000D);
		if(addAmount < 0.00003)
			addAmount = 0.00003;
		lossAmount = -(veinPosGoal - veinPos) * (timeDiff / 200000000D);

		if(veinPos < veinPosGoal)
		{
			veinPos += addAmount;
			if(veinPos > veinPosGoal)
				veinPos = veinPosGoal;
		}
		else if(veinPos > veinPosGoal)
		{
			veinPos -= lossAmount;
			if(veinPos < veinPosGoal)
				veinPos = veinPosGoal;
		}

		if(veinPos < 0 || veinPos > 1)
			veinPos = veinPosGoal;

		if(veinPos == 1D && lastVeinPos != 1D)
			player.displayClientMessage(new TranslatableComponent("pmmo.veinCharge", 100).setStyle(XP.textStyle.get("green")), true);

		lastVeinPos = veinPos;

//					System.out.println(veinPosGoal);

		veinBarPosX = (int) (sr.getGuiScaledWidth() * veinBarOffsetX - barWidth / 2);
		veinBarPosY = (int) (sr.getGuiScaledHeight() * veinBarOffsetY - barHeight / 2);


		if(veinKey)
		{
			if(XP.isPlayerSurvival(player))
			{
				RenderSystem.setShaderTexture(0, bar);

				blit(stack, veinBarPosX, veinBarPosY, 0, 0, barWidth, barHeight);
				blit(stack, veinBarPosX, veinBarPosY, 0, barHeight, (int) Math.floor(barWidth * veinPos), barHeight);
//						System.out.println(veinPos * maxVeinCharge);
				drawCenteredString(stack, fontRenderer, (int) Math.floor(veinPos * maxVeinCharge) + "/" + (int) Math.floor(maxVeinCharge) + " " + DP.dprefix(veinPos * 100D) + "%", veinBarPosX + (barWidth / 2), veinBarPosY - 8, 0x00ff00);

				metToolReq = XP.checkReq(player, player.getMainHandItem().getItem().getRegistryName(), JType.REQ_TOOL);

				if(!metToolReq)
				{
					drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.notSkilledEnoughToUseAsTool", new TranslatableComponent(player.getMainHandItem().getDescriptionId())).setStyle(XP.textStyle.get("red")), sr.getGuiScaledWidth() / 2, veinBarPosY + 6, 0xffffff);
					return;
				}

				if(lookingAtBlock && !canBreak)
				{
					drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.notSkilledEnoughToBreak", new TranslatableComponent(lastBlockTransKey)).setStyle(XP.textStyle.get("red")).getString(), sr.getGuiScaledWidth() / 2, veinBarPosY + 6, 0xffffff);
					return;
				}

				if(lastBlockState != null && canBreak && (lookingAtBlock || isVeining))
				{
					if(canVein)
					{
						breakAmount = (int) ((maxVeinCharge * veinPos) / WorldTickHandler.getVeinCost(lastBlockState, lastBlockPos, player));
						if(breakAmount > veinMaxBlocks)
							breakAmount = veinMaxBlocks;
						drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.canVein", breakAmount, new TranslatableComponent(lastBlockTransKey)).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0x00ff00);
					}
					else if(WorldTickHandler.canVeinDimension(lastBlockRegKey, player))
						drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.cannotVein", new TranslatableComponent(lastBlockTransKey).getString()).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0xff5454);
					else
						drawCenteredString(stack, fontRenderer, new TranslatableComponent("pmmo.cannotVeinDimension", new TranslatableComponent(lastBlockTransKey).getString()).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0xff5454);
				}
			}
		}
	}*/

	private static void renderSkillList(int skillListX, int skillListY)
	{
		if(!mc.options.renderDebug)
		{
			Map<String, Double> modifiers = core.getConsolidatedModifierMap(mc.player);
			List<String> skillsKeys = new ArrayList<>(); 
			DataMirror.getSkillMap().keySet().stream().forEach(entry -> skillsKeys.add(entry));
			skillsKeys.sort(Comparator.<String>comparingLong(a -> DataMirror.getXpForSkill(a)).reversed());

			
			for(int i = 0; i < skillsKeys.size(); i++) {
				String skillKey = skillsKeys.get(i);
				skillGap = fontRenderer.width(new TranslatableComponent("pmmo." + skillKey).getString()) > skillGap 
						? fontRenderer.width(new TranslatableComponent("pmmo." + skillKey).getString()) 
						: skillGap;
				long currentXP = DataMirror.getXpForSkill(skillKey);
				double level = DataMirror.getXpWithPercentToNextLevel(DataMirror.getXpForSkill(skillKey));
				String tempString = DP.dp(Math.floor(level * 100D) / 100D);
				int color = core.getDataConfig().getSkillColor(skillKey);
				
				if(level >= maxLevel)
					tempString = "" + maxLevel;
				
				int listIndex = i * 9;
				levelGap = fontRenderer.width(tempString);
				drawString(stack, fontRenderer, tempString, skillListX + 4, skillListY + 3 + listIndex, color);
				drawString(stack, fontRenderer, " | " + new TranslatableComponent("pmmo." + skillKey).getString(), skillListX + levelGap + 4, skillListY + 3 + listIndex, color);
				drawString(stack, fontRenderer, " | " + DP.dprefix(currentXP), skillListX + levelGap + skillGap + 13, skillListY + 3 + listIndex, color);
				if (modifiers.getOrDefault(skillKey, 1d) != 1d) {
					double bonus = (Math.max(0, modifiers.get(skillKey)) -1) * 100;
					tempString = (bonus >= 0 ? "+" : "-")+DP.dp(bonus)+"%";
					drawString(stack, fontRenderer, tempString, skillListX + levelGap + skillGap + 38, skillListY + 3 + listIndex, color);
				}
			}
		}
	}

	public static void refreshClientSettings()
	{
		skillListOffsetX = Config.SKILL_LIST_OFFSET_X.get();
		skillListOffsetY = Config.SKILL_LIST_OFFSET_Y.get();
		showSkillsListAtCorner = Config.SKILL_LIST_DISPLAY.get();
		maxLevel = Config.MAX_LEVEL.get();
	}
/*
//	@SubscribeEvent
//	public void renderWorldDrops(RenderLevelLastEvent event)
//	{
//		PoseStack matrixStack = event.getPoseStack();
//		String theText = "test";
//
//		double d0 = this.renderManager.squareDistanceTo(entityIn);
//		if (!(d0 > 4096.0D)) {
//			boolean flag = !entityIn.isDiscrete();
//			float f = entityIn.getHeight() + 0.5F;
//			int i = "deadmau5".equals(displayNameIn) ? -10 : 0;
//			matrixStack.push();
//			matrixStack.translate(0.0D, (double)f, 0.0D);
//			matrixStack.multiply(this.renderManager.getCameraOrientation());
//			matrixStack.scale(-0.025F, -0.025F, 0.025F);
//			Matrix4f matrix4f = matrixStackIn.getLast().getPositionMatrix();
//			float f1 = Minecraft.getInstance().gameSettings.getTextBackgroundOpacity(0.25F);
//			int j = (int)(f1 * 255.0F) << 24;
//			FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
//			float f2 = (float)(-fontrenderer.getStringWidth(theText) / 2);
//			fontrenderer.renderString(theText, f2, (float)i, 553648127, false, matrix4f, bufferIn, flag, j, packedLightIn);
//			if (flag) {
//				fontrenderer.renderString(theText, f2, (float)i, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
//			}
//
//			matrixStackIn.pop();
//		}
//	}*/

	/*public static void sendLvlUp(int level, String skill)
	{
		player = Minecraft.getInstance().player;

		Map<String, Double> configMap = Config.getConfigMap();

		if(level < 1 || (configMap.containsKey("maxLevel") && level > configMap.get("maxLevel")))
			return;

		//double percentDmgBoostPerLevel;
		//double maxExtraDamageBoost;
		//double damageBoost;
		TranslatableComponent msg;

		// this needs to iterate through perks.
		// on second thought maybe this just stops existing.  
		// will deliberate and decide.  might be too hard to
		// keep in since you'd need to ask every single perk
		// during registration to give a new stat bonus specifically
		// for the LEVEL_UP trigger, which may not apply.
		switch(skill)
		{
			/*case "building":
				double levelsPerOneReach = configMap.get("levelsPerOneReach");
				double maxExtraReachBoost = configMap.get("maxExtraReachBoost");
				double reachBoost = Math.min(maxExtraReachBoost, level / levelsPerOneReach);
				msg = new TranslatableComponent("pmmo.levelUpReachBoost", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), DP.dpSoft(reachBoost));
				break;

			case "combat":
				percentDmgBoostPerLevel= configMap.get("damageBonusPercentPerLevelMelee");
				maxExtraDamageBoost = configMap.get("maxExtraDamagePercentageBoostMelee");
				damageBoost = Math.min(maxExtraDamageBoost, level * percentDmgBoostPerLevel) * 100;
				msg = new TranslatableComponent("pmmo.levelUpDamageBoostPercentMelee", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), DP.dpSoft(damageBoost));
				break;

			case "archery":
				percentDmgBoostPerLevel = configMap.get("damageBonusPercentPerLevelArchery");
				maxExtraDamageBoost = configMap.get("maxExtraDamagePercentageBoostArchery");
				damageBoost = Math.min(maxExtraDamageBoost, level * percentDmgBoostPerLevel) * 100;
				msg = new TranslatableComponent("pmmo.levelUpDamageBoostPercentArchery", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), DP.dpSoft(damageBoost));
				break;

			case "magic":
				percentDmgBoostPerLevel = configMap.get("damageBonusPercentPerLevelMagic");
				maxExtraDamageBoost = configMap.get("maxExtraDamagePercentageBoostMagic");
				damageBoost = Math.min(maxExtraDamageBoost, level * percentDmgBoostPerLevel) * 100;
				msg = new TranslatableComponent("pmmo.levelUpDamageBoostPercentMagic", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), DP.dpSoft(damageBoost));
				break;

			case "endurance":
				double endurancePerLevel = configMap.get("endurancePerLevel");
				double maxEndurance = configMap.get("maxEndurance");
				double enduranceBoost = Math.min(maxEndurance, level * endurancePerLevel);
				double levelsPerHeart = configMap.get("levelsPerHeart");
				double maxExtraHeartBoost = configMap.get("maxExtraHeartBoost");
				int heartBoost = Math.min((int) maxExtraHeartBoost, (int) Math.floor(level / levelsPerHeart));
				if(level % (int) levelsPerHeart == 0 && heartBoost <= (int) maxExtraHeartBoost)
					player.displayClientMessage(new TranslatableComponent("pmmo.gainedExtraHeart").setStyle(Skill.getSkillStyle(skill)), false);

				msg = new TranslatableComponent("pmmo.levelUpEnduranceBoost", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), enduranceBoost);
				break;

			case "agility":
				msg = new TranslatableComponent("pmmo.levelUpSprintSpeedBonus");//, level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString(), DP.dpSoft(AttributeHandler.getSpeedBoostMultiplier(level) * 100) + "%");
				break;

			default:
				msg = new TranslatableComponent("pmmo.levelUp", level, new TranslatableComponent("pmmo." + skill.toLowerCase()).getString());
				break;
		}

		player.displayClientMessage(msg.setStyle(Skill.getSkillStyle(skill)), false);

		Map<String, Double> skillsMap = new HashMap<>(XP.getOfflineXpMap(player.getUUID()));
		skillsMap.put(skill, XP.xpAtLevel(level));
		int totalLevel = XP.getTotalLevelFromMap(skillsMap);
		if((totalLevel % Config.forgeConfig.levelsPerTotalLevelMilestone.get()) == 0)
		{
			player.displayClientMessage(new TranslatableComponent("pmmo.totalLevelUp", totalLevel).setStyle(XP.getColorStyle(0x00ff00)), true);
			player.displayClientMessage(new TranslatableComponent("pmmo.totalLevelUp", totalLevel).setStyle(XP.getColorStyle(0x00ff00)), false);
		}

		if(showLevelUpUnlocks)
			checkUnlocks(level, skill, player);

		//int nightvisionUnlockLevel = (int) Config.getConfig("nightvisionUnlockLevel");
		int dualSalvageUnlockLevel = (int) Config.getConfig("dualSalvageSmithingLevelReq");

		//if(Skill.SWIMMING.equals(skill) && level - 1 < nightvisionUnlockLevel && level >= nightvisionUnlockLevel)
		//	player.displayClientMessage(new TranslatableComponent("pmmo.underwaterNightVisionUnLocked", level).setStyle(Skill.getSkillStyle(skill)), false);

		if(Skill.SMITHING.equals(skill) && level - 1 < dualSalvageUnlockLevel && level >= dualSalvageUnlockLevel)
			player.displayClientMessage(new TranslatableComponent("pmmo.dualSalvageUnLocked", level).setStyle(Skill.getSkillStyle(skill)), false);

		listWasOn = barOn;

		if(lvlUpScreenshotShowSkills)
			barOn = true;

		if(lvlUpScreenshot)
			screenshots.add(player.getDisplayName().getString() + " " + skill.toLowerCase() + " " + level);

//		XP.scanUnlocks(level, skill);

		NetworkHandler.sendToServer(new MessageLevelUp(skill, level));
	}*/

	/*public static void makeXpDrop(double xp, String skillIn, int cooldown, double gainedXp, boolean skip)
	{
		xpDropWasStacked = false;

		if(xp + gainedXp <= 0 || skillIn.equals(Skill.INVALID_SKILL.toString()))
		{
			skills.remove(skillIn);
			return;
		}
		else if(skills.get(skillIn) == null)				//Handle client xp tracker
			skills.put(skillIn, new ASkill(xp, XP.levelAtXpDecimal(xp), xp, XP.levelAtXpDecimal(xp)));

		if(!skip)
		{
			XPOverlayGUI.activeSkill = skillIn;
			XPOverlayGUI.aSkill = skills.get(activeSkill);
		}

		tempASkill = skills.get(skillIn);

		if(gainedXp <= 0)
		{
			tempASkill.xp = xp + gainedXp;
			tempASkill.goalXp = tempASkill.xp;
			tempASkill.pos = XP.levelAtXpDecimal(tempASkill.xp);
			tempASkill.goalPos = tempASkill.pos;

			if(gainedXp == 0)					//awardXp will NEVER award xp if the award is 0.
			{
				if(xpDropsShowXpBar)
					XPOverlayGUI.cooldown = cooldown;

				for(int i = 0; i < xpDrops.size(); i++)
				{
					if(xpDrops.get(i).skill.equals(skillIn))
					{
						xpDrops.remove(i);
						i = 0;
					}
				}
			}

//			System.out.println(mc.player.getDisplayName().getString() + " " + skill + " has been set to: " + xp);
		}
		else if(stackXpDrops && xpDrops.size() > 0)
		{
			for(XpDrop xpDrop : xpDrops)
			{
				if(xpDrop.skill.equals(skillIn) && (xpDrop.age < xpDropDecayAge || xpDrop.Y > 0))
				{
					xpDrop.gainedXp += gainedXp;
					xpDrop.startXp += gainedXp;
					if(xpDrops.get(xpDrops.size() - 1).age > xpDropDecayAge - 25)
						xpDrops.get(xpDrops.size() - 1).age = xpDropDecayAge - 25;

					xpDropWasStacked = true;
				}
			}
		}

		if(!xpDropWasStacked && gainedXp != 0)
		{
			if(xpDrops.size() > 0)
				xpDrops.add(new XpDrop(0, xpDrops.get(xpDrops.size() - 1).Y + 15, skillIn, xp, gainedXp, skip));
			else
				xpDrops.add(new XpDrop(0, xpDropSpawnDistance, skillIn, xp, gainedXp, skip));
		}

		if(xpDropsShowXpBar && !skip)
			XPOverlayGUI.cooldown = cooldown;

		levelGap = 0;
		skillGap = 0;
		xpGap = 0;

		skills.forEach((thisSkill, thisASkill) ->
		{
			if(skills.get(thisSkill).pos >= maxLevel)
			{
				if(levelGap < fontRenderer.width("" + maxLevel))
					levelGap = fontRenderer.width("" + maxLevel);
			}
			else
			{
				if(levelGap < fontRenderer.width(DP.dp(XP.levelAtXpDecimal(skills.get(thisSkill).goalXp))))
					levelGap = fontRenderer.width(DP.dp(XP.levelAtXpDecimal(skills.get(thisSkill).goalXp)));
			}

			if(skillGap < fontRenderer.width(new TranslatableComponent("pmmo." + thisSkill.toLowerCase()).getString()))
				skillGap = fontRenderer.width(new TranslatableComponent("pmmo." + thisSkill.toLowerCase()).getString());

			if(xpGap < fontRenderer.width(DP.dp(skills.get(thisSkill).goalXp)))
				xpGap = fontRenderer.width(DP.dprefix(skills.get(thisSkill).goalXp));
		});
	}*/

	/*private static void addItemsWithSameLevel(int level, String skill, JType jType, Map<JType, Map<String, Map<String, Double>>> output)
	{
		output.put(jType, new HashMap<>());
		Map<String, Map<String, Double>> outputJMap = output.get(jType);
		for(Map.Entry<String, Map<String, Double>> element : JsonConfig.data.getOrDefault(jType, new HashMap<>()).entrySet())
		{
			if(element.getValue().getOrDefault(skill, -1D) == level)
				outputJMap.put(element.getKey(), element.getValue());
		}
	}*/

	/*public static void checkUnlocks(int level, String skill, Player player)
	{
		Map<JType, Map<String, Map<String, Double>>> itemsWithReqs = new HashMap<>();

		addItemsWithSameLevel(level, skill, JType.REQ_WEAR, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_WEAPON, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_TOOL, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_PLACE, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_BREAK, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_USE, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_USE_ENCHANTMENT, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_BIOME, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_KILL, itemsWithReqs);
		addItemsWithSameLevel(level, skill, JType.REQ_CRAFT, itemsWithReqs);

		String jTypeName, unlockName;
		Item item;

		for(Map.Entry<JType, Map<String, Map<String, Double>>> element : itemsWithReqs.entrySet())
		{
			jTypeName = new TranslatableComponent("pmmo." + element.getKey().toString().replace("req_", "").replaceAll("_", " ")).getString();
			for(Map.Entry<String, Map<String, Double>> itemElement : element.getValue().entrySet())
			{
				item = XP.getItem(itemElement.getKey());
				if(!item.equals(Items.AIR))
					unlockName = new TranslatableComponent(item.getDescriptionId()).getString();
				else
					unlockName = itemElement.getKey();
				if(XP.checkReq(player, itemElement.getValue()))
					player.displayClientMessage(new TranslatableComponent("pmmo.levelUpFeatureUnlock", unlockName, jTypeName).setStyle(XP.getColorStyle(0x00ff00)), false);
				else
					player.displayClientMessage(new TranslatableComponent("pmmo.levelUpPartialFeatureUnlock", unlockName, jTypeName).setStyle(XP.getColorStyle(0xffff00)), false);
			}
		}
	}*/
}