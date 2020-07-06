package harmonised.pmmo.gui;

import java.util.*;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.network.MessageLevelUp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class XPOverlayGUI extends AbstractGui
{
	private static int barWidth = 102, barHeight = 5, barPosX, barPosY, veinBarPosX, veinBarPosY, xpDropPosX, xpDropPosY;
	private static int tempAlpha, levelGap = 0, skillGap, xpGap, halfscreen, tempInt, xpDropDecayAge = 0;
	private static ArrayList<Skill> skillsKeys;
	private static double xp, goalXp, cooldown;
	private static double lastTime, startLevel, timeDiff, bonus, level, decayRate, decayAmount, growAmount, xpDropOffset = 0, xpDropOffsetCap = 0, minXpGrow = 0.2;
	private static double barOffsetX = 0, barOffsetY = 0, veinBarOffsetX, veinBarOffsetY, xpDropOffsetX = 0, xpDropOffsetY = 0, xpDropSpawnDistance = 0, xpDropOpacityPerTime = 0, xpDropMaxOpacity = 0, biomePenaltyMultiplier = 0, maxVeinCharge = 64D;
	private static String tempString;
	private static int theme = 2, themePos = 1, listIndex = 0, xpDropYLimit = 0;
	private static String skillName = "none";
	private static boolean stackXpDrops = true, init = false, showXpDrops = true, guiKey = false, veinKey = false, guiPressed = false, xpDropsAttachedToBar = true, xpDropWasStacked, xpLeftDisplayAlwaysOn, xpBarAlwaysOn, lvlUpScreenshot, lvlUpScreenshotShowSkills;
	private final ResourceLocation bar = XP.getResLoc( Reference.MOD_ID, "textures/gui/xpbar.png" );
	private static ArrayList<XpDrop> xpDrops = new ArrayList<XpDrop>();
	private static Minecraft mc = Minecraft.getInstance();
	private static PlayerEntity player = mc.player;
	public static Map<Skill, ASkill> skills = new HashMap<>();
//	private static ArrayList<String> skillsKeys = new ArrayList<String>();
	private static ASkill aSkill;
	private static Skill skill = Skill.INVALID_SKILL, tempSkill = Skill.INVALID_SKILL;
	private static FontRenderer fontRenderer = mc.fontRenderer;
	private static int maxLevel, color, breakAmount, veinMaxBlocks;
	private static double maxXp;
	private static XpDrop xpDrop;
	private static long lastBonusUpdate = System.nanoTime(), lastVeinBlockUpdate = System.nanoTime();
	private static double itemBoost, biomeBoost;
	private static double tempDouble, veinPos = -1000, lastVeinPos = -1000, veinPosGoal, addAmount = 0, lossAmount = 0, veinLeft;
	private static BlockState blockState, lastBlockState;
	private static String lastBlockRegKey = "", lastBlockTransKey = "";
	private static Item lastToolHeld = Items.AIR;
	public static Set<String> screenshots = new HashSet<>();
	public static boolean guiWasOn = false, guiOn = false, isVeining = false, canBreak = true, canVein = false, lookingAtBlock = false, metToolReq = true;
	MainWindow sr;
	BlockPos blockPos, lastBlockPos;


	@SubscribeEvent
	public void renderOverlay( RenderGameOverlayEvent event )
	{
		if( !skill.equals( Skill.INVALID_SKILL ) )
		{
			if( event.getType() == RenderGameOverlayEvent.ElementType.TEXT )	//Xp Drops
			{
				player = Minecraft.getInstance().player;
				if( !init )
				{
					doInit();
					init = true;
				}

				RenderSystem.pushMatrix();
				RenderSystem.enableBlend();
				sr = mc.getMainWindow();

//				drawCenteredString( fontRenderer, "Most actions in the game award Xp!", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 + 10, 0xffffffff );
//				drawCenteredString( fontRenderer, "Level Restrictions for Wearing/Using/Breaking/Placing/Etc!", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 + 10, 0xffffffff );
//				drawCenteredString( fontRenderer, "Fully Customizable - Modpack Maker friendly!", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 + 10, 0xffffffff );
//				drawCenteredString( fontRenderer, "GUI that covers every feature of PMMO, including Modpack changes, Live!", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 + 10, 0xffffffff );

				barPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * barOffsetX );
				barPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * barOffsetY );
				xpDropPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * xpDropOffsetX );
				xpDropPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * xpDropOffsetY );

				aSkill = skills.get( skill );

				timeDiff = (System.nanoTime() - lastTime);
				lastTime = System.nanoTime();

				guiKey = ClientHandler.SHOW_GUI.isKeyDown();
				veinKey = ClientHandler.VEIN_KEY.isKeyDown();

				if( guiKey || xpBarAlwaysOn )
					cooldown = 1;

				if( guiKey )
				{
					if( !guiPressed )
					{
						guiOn = !guiOn;
						guiPressed = true;
					}
				}
				else
					guiPressed = false;

				if( !Minecraft.getInstance().isGamePaused() )
				{
					doRayTrace();
					doCrosshair();
					doVein();
					doSkills();
				}
				doXpDrops();
				doXpBar();
				doSkillList();

				if( cooldown > 0 )
					cooldown -= timeDiff / 1000000D;

				RenderSystem.disableBlend();
				RenderSystem.color3f( 255, 255, 255 );
				RenderSystem.popMatrix();
			}
		}
	}

	private void doRayTrace()
	{
		if( mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK )
		{
			lookingAtBlock = true;

			blockPos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
			blockState = mc.world.getBlockState( blockPos );

			if( lastBlockState == null )
				updateLastBlock();

			if( lastBlockState.getBlock().equals( blockState.getBlock() ) )
				lastVeinBlockUpdate = System.nanoTime();
//
			if( !isVeining && System.nanoTime() - lastVeinBlockUpdate > 100000000L )
				updateLastBlock();

			canBreak = XP.checkReq( player, lastBlockRegKey, JType.REQ_BREAK );
		}
		else
			lookingAtBlock = false;
	}

	private void updateLastBlock()
	{
		lastBlockState = blockState;
		lastBlockPos = blockPos;
		if( lastBlockState.getBlock().getRegistryName() != null )
			lastBlockRegKey = lastBlockState.getBlock().getRegistryName().toString();
		canVein = WorldTickHandler.canVeinGlobal( lastBlockRegKey, player ) && WorldTickHandler.canVeinDimension( lastBlockRegKey, player );
		lastBlockTransKey = lastBlockState.getBlock().getTranslationKey();
	}

	private void doXpDrops()
	{
		if( xpDropsAttachedToBar )
		{
			if( cooldown <= 0 )
				xpDropOffsetCap = -9;
			else if ( guiKey || xpLeftDisplayAlwaysOn )
			{
				if( skills.get( skill ).xp >= maxXp )
					xpDropOffsetCap = 25;
				else
					xpDropOffsetCap = 34;
			}
			else
				xpDropOffsetCap = 16;

			if( xpDropOffset > xpDropOffsetCap )
				xpDropOffset -= 1d * timeDiff / 10000000;

			if( xpDropOffset < xpDropOffsetCap )
				xpDropOffset = xpDropOffsetCap;
		}

		for( int i = 0; i < xpDrops.size(); i++ )		//update Xp Drops
		{
			xpDrop = xpDrops.get( i );
			xpDrop.age += timeDiff / 5000000;
			decayRate = 0.75f + ( 1 * xpDrops.size() * 0.02f );			//Xp Drop Y
			decayAmount = decayRate * timeDiff / 10000000;

			if( !mc.isGamePaused() )
			{
				if( ( ( xpDrop.Y - decayAmount < 0 ) && xpDrop.age >= xpDropDecayAge ) || !showXpDrops || ( !xpDropsAttachedToBar && xpDrop.age >= xpDropDecayAge ) )
				{
					aSkill = skills.get( xpDrop.skill );
					skill = xpDrop.skill;

					decayRate = xpDrop.gainedXp * 0.03 * timeDiff / 10000000;
					if( stackXpDrops )
					{
						if( decayRate < 0.1 )
							decayRate = 0.1;
					}
					else
					if( decayRate < 1 )
						decayRate = 1;

					if( xpDrop.gainedXp - ( decayAmount ) < 0 )
					{
						aSkill.goalXp += xpDrop.gainedXp;
						xpDrop.gainedXp = 0;
					}
					else
					{
						xpDrop.gainedXp -= decayRate;
						aSkill.goalXp += decayRate;
					}
					aSkill.goalPos = XP.levelAtXpDecimal( aSkill.goalXp );
				}
			}

			if( showXpDrops )
			{
				if( xpDropOffset == xpDropOffsetCap )
					xpDrop.Y -= decayAmount;

				if( xpDrop.Y < ( i * 9 ) - xpDropYLimit )
					xpDrop.Y = ( i * 9 ) - xpDropYLimit;

				tempInt = (int) Math.floor( xpDrop.Y * xpDropOpacityPerTime ); //Opacity Loss

				if( tempInt < 0 )
					tempInt = -tempInt;

				if( tempInt > xpDropMaxOpacity )
					tempAlpha = 0;
				else
					tempAlpha = (int) Math.floor( xpDropMaxOpacity - tempInt );

				if( tempAlpha > 3 )
					drawCenteredString( fontRenderer, "+" + DP.dprefix( xpDrop.gainedXp ) + " " + new TranslationTextComponent( "pmmo." + xpDrop.skill.name().toLowerCase() ).getString(), xpDropPosX + (barWidth / 2), (int) xpDrop.Y + (int) xpDropOffset + xpDropPosY, (tempAlpha << 24) |+ XP.getSkillColor( xpDrop.skill ) );
			}
		}

		if( xpDrops.size() > 0 && xpDrops.get( 0 ).gainedXp <= 0 )
			xpDrops.remove( 0 );
	}

	private void doSkills()
	{
		for( Map.Entry<Skill, ASkill> entry : skills.entrySet() )		//Update Skills
		{
			aSkill = entry.getValue();

			startLevel = Math.floor( aSkill.pos );

			growAmount = ( aSkill.goalPos - aSkill.pos ) * 50;

			minXpGrow = 25;

			if( growAmount < minXpGrow )
				growAmount = minXpGrow;

			if( aSkill.pos < aSkill.goalPos )
			{
				aSkill.pos += 0.00005d * growAmount;
				aSkill.xp   = XP.xpAtLevelDecimal( aSkill.pos );
			}

			if( aSkill.pos > aSkill.goalPos )
				aSkill.pos = aSkill.goalPos;

			if( startLevel < (int) aSkill.pos )
				sendLvlUp( (int) Math.floor( aSkill.pos ), entry.getKey() );

			if( aSkill.xp > aSkill.goalXp )
				aSkill.xp = aSkill.goalXp;
		}
	}

	private void doXpBar()
	{
		themePos += ( 2.5 + 7.5 * ( aSkill.pos % Math.floor( aSkill.pos ) ) ) * (timeDiff / 1000000);

		if( themePos > 10000 )
			themePos =  themePos % 10000;

		if( cooldown > 0 )				//Xp Bar
		{
			RenderSystem.pushMatrix();
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture( bar );
			RenderSystem.color3f( 255, 255, 255 );

			aSkill = skills.get( skill );

			blit( barPosX, barPosY + 10, 0, 0, barWidth, barHeight );
			if( theme == 1 )
			{
				blit( barPosX, barPosY + 10, 0, barHeight * 1, (int) Math.floor( barWidth * ( aSkill.pos - Math.floor( aSkill.pos ) ) ), barHeight );
			}
			else
			{
				tempInt = (int) Math.floor( ( barWidth ) * ( aSkill.pos - Math.floor( aSkill.pos ) ) );

				if( tempInt > 100 )
					tempInt = 100;

				if( aSkill.pos >= maxLevel )
					tempInt = 100;

				blit( barPosX, barPosY + 10, 0, barHeight*3, barWidth - 1, barHeight );
				blit( barPosX + 1, barPosY + 10, 1 + (int)( Math.floor( (double) themePos / 100 ) ), barHeight*2, tempInt, barHeight );
			}
			if( aSkill.pos >= maxLevel )
				drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + skill.name().toLowerCase() ).getString(), maxLevel ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( skill ) );
			else
				drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + skill.name().toLowerCase() ).getString(), DP.dp( Math.floor( aSkill.pos * 100D ) / 100D ) ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( skill ) );

			if( (guiKey || xpLeftDisplayAlwaysOn) && skills.get( skill ) != null )
			{
				if( skills.get( skill ).xp >= maxXp )
					drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.maxLevel" ).getString(), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( skill ) );
				else
				{
					if( goalXp >= maxXp )
						goalXp =  maxXp;

					goalXp = XP.xpAtLevel( XP.levelAtXp( aSkill.xp ) + 1 );
					drawCenteredString( fontRenderer, DP.dprefix( skills.get( skill ).xp ) + " / " + DP.dprefix( goalXp ), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( skill ) );
					drawCenteredString( fontRenderer,  new TranslationTextComponent( "pmmo.xpLeft", DP.dprefix( goalXp - aSkill.xp ) ).getString(), barPosX + (barWidth / 2), 26 + barPosY, XP.getSkillColor( skill ) );
				}
			}

			RenderSystem.disableBlend();
			RenderSystem.popMatrix();
		}
	}

	private void doVein()
	{   // VEIN STUFF
		veinLeft = XP.getAbilitiesTag( player ).getDouble( "veinLeft" );
		veinPosGoal = veinLeft / maxVeinCharge;
		addAmount = (veinPosGoal - veinPos) * (timeDiff / 200000000D);
		if( addAmount < 0.00003 )
			addAmount = 0.00003;
		lossAmount = -(veinPosGoal - veinPos) * (timeDiff / 200000000D);

		if( veinPos < veinPosGoal )
		{
			veinPos += addAmount;
			if( veinPos > veinPosGoal )
				veinPos = veinPosGoal;
		}
		else if( veinPos > veinPosGoal )
		{
			veinPos -= lossAmount;
			if( veinPos < veinPosGoal )
				veinPos = veinPosGoal;
		}

		if( veinPos < 0 || veinPos > 1 )
			veinPos = veinPosGoal;

		if( veinPos == 1D && lastVeinPos != 1D )
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.veinCharge", 100 ).func_240703_c_( XP.textStyle.get( "green" ) ), true );

		lastVeinPos = veinPos;

//					System.out.println( veinPosGoal );

		veinBarPosX = (int) (sr.getScaledWidth() * veinBarOffsetX - barWidth / 2 );
		veinBarPosY = (int) (sr.getScaledHeight() * veinBarOffsetY - barHeight / 2 );


		if( veinKey && XP.isPlayerSurvival( player ) )
		{
			Minecraft.getInstance().getTextureManager().bindTexture( bar );

			blit( veinBarPosX, veinBarPosY, 0, 0, barWidth, barHeight );
			blit( veinBarPosX, veinBarPosY, 0, barHeight, (int) Math.floor( barWidth * veinPos ), barHeight );
//						System.out.println( veinPos * maxVeinCharge );
			drawCenteredString( fontRenderer, (int) Math.floor( veinPos * maxVeinCharge ) + "/" + (int) Math.floor( maxVeinCharge ) + " " + DP.dprefix( veinPos * 100D ) + "%", veinBarPosX + (barWidth / 2), veinBarPosY - 8, 0x00ff00 );

			metToolReq = XP.checkReq( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL );

			if( !metToolReq )
			{
				drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.notSkilledEnoughToUseAsTool", new TranslationTextComponent( player.getHeldItemMainhand().getTranslationKey() ) ).func_240703_c_( XP.textStyle.get( "red" ) ).getFormattedText(), sr.getScaledWidth() / 2, veinBarPosY + 6, 0xffffff );
				return;
			}

			if( lookingAtBlock && !canBreak )
			{
				drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.notSkilledEnoughToBreak", new TranslationTextComponent( lastBlockTransKey ) ).func_240703_c_( XP.textStyle.get( "red" ) ).getFormattedText(), sr.getScaledWidth() / 2, veinBarPosY + 6, 0xffffff );
				return;
			}

			if( lastBlockState != null && canBreak && ( lookingAtBlock || isVeining ) )
			{
				if( canVein )
				{
					breakAmount = (int) ( ( maxVeinCharge * veinPos ) / WorldTickHandler.getVeinCost( lastBlockState, lastBlockPos, player ) );
					if( breakAmount > veinMaxBlocks )
						breakAmount = veinMaxBlocks;
					drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.canVein", breakAmount, new TranslationTextComponent( lastBlockTransKey ) ).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0x00ff00 );
				}
				else if( WorldTickHandler.canVeinDimension( lastBlockRegKey, player ) )
					drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.cannotVein", new TranslationTextComponent( lastBlockTransKey ).getString() ).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0xff5454 );
				else
					drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.cannotVeinDimension", new TranslationTextComponent( lastBlockTransKey ).getString() ).getString(), veinBarPosX + (barWidth / 2), veinBarPosY + 6, 0xff5454 );
			}
		}
	}

	private void doSkillList()
	{
		if( guiOn && !mc.gameSettings.showDebugInfo )
		{
			listIndex = 0;

			if( System.nanoTime() - lastBonusUpdate > 250000000 )
			{
				for( Map.Entry<Skill, ASkill> entry : skills.entrySet() )
				{
					tempSkill = entry.getKey();

					itemBoost = XP.getItemBoost( player, tempSkill );
					biomeBoost = XP.getBiomeBoost( player, tempSkill );

					if( itemBoost + biomeBoost >= -100 )
						skills.get( tempSkill ).bonus = itemBoost + biomeBoost;
					else
						skills.get( tempSkill ).bonus = -100;
				}
				lastBonusUpdate = System.nanoTime();
			}

			skillsKeys = new ArrayList<>( skills.keySet() );
			skillsKeys.sort( Comparator.comparingDouble( a -> ((Skill) a).getXp( player ) ).reversed() );

			for( Skill keySkill : skillsKeys )
			{
				aSkill = skills.get( keySkill );
				skillName = keySkill.name().toLowerCase();
				level = XP.levelAtXpDecimal( aSkill.xp );
				tempString = DP.dp( Math.floor( level * 100D ) / 100D );
				color = XP.getSkillColor( keySkill );
				if( level >= maxLevel )
					tempString = "" + maxLevel;
				drawRightAlignedString( fontRenderer, tempString, levelGap + 4, 3 + listIndex, color );
				drawString( fontRenderer, " | " + new TranslationTextComponent( "pmmo." + skillName ).getString(), levelGap + 4, 3 + listIndex, color );
				drawString( fontRenderer, " | " + DP.dprefix( aSkill.xp ), levelGap + skillGap + 13, 3 + listIndex, color );

				if( aSkill.bonus != 0 )
				{
					bonus = Math.floor( aSkill.bonus * 100 ) / 100;

					if( bonus > 0 )
						tempString = "+" + ( bonus % 1 == 0 ? (int) Math.floor( bonus ) : DP.dp( bonus ) ) + "%";
					else if ( bonus < 0 )
						tempString = ( bonus % 1 == 0 ? (int) Math.floor( bonus ) : DP.dp( bonus ) ) + "%";
					else
						tempString = "";

					drawString( fontRenderer, tempString, levelGap + skillGap + xpGap + 32, 3 + listIndex, color );
				}

				listIndex += 9;
			}
		}
	}

	private void doCrosshair()
	{
	}

	public static void doInit()
	{
		player = Minecraft.getInstance().player;

		CompoundNBT prefsTag = XP.getPreferencesTag( player );

		if( prefsTag.contains( "barOffsetX" ) )
			barOffsetX = prefsTag.getDouble( "barOffsetX" );
		else
			barOffsetX = Config.forgeConfig.barOffsetX.get();

		if( prefsTag.contains( "barOffsetY" ) )
			barOffsetY = prefsTag.getDouble( "barOffsetY" );
		else
			barOffsetY = Config.forgeConfig.barOffsetY.get();

		if( prefsTag.contains( "veinBarOffsetX" ) )
			veinBarOffsetX = prefsTag.getDouble( "veinBarOffsetX" );
		else
			veinBarOffsetX = Config.forgeConfig.veinBarOffsetX.get();

		if( prefsTag.contains( "veinBarOffsetY" ) )
			veinBarOffsetY = prefsTag.getDouble( "veinBarOffsetY" );
		else
			veinBarOffsetY = Config.forgeConfig.veinBarOffsetY.get();

		if( prefsTag.contains( "xpDropOffsetX" ) )
			xpDropOffsetX = prefsTag.getDouble( "xpDropOffsetX" );
		else
			xpDropOffsetX = Config.forgeConfig.xpDropOffsetX.get();

		if( prefsTag.contains( "xpDropOffsetY" ) )
			xpDropOffsetY = prefsTag.getDouble( "xpDropOffsetY" );
		else
			xpDropOffsetY = Config.forgeConfig.xpDropOffsetY.get();

		if( prefsTag.contains( "xpDropSpawnDistance" ) )
			xpDropSpawnDistance = prefsTag.getDouble( "xpDropSpawnDistance" );
		else
			xpDropSpawnDistance = Config.forgeConfig.xpDropSpawnDistance.get();

		if( prefsTag.contains( "xpDropOpacityPerTime" ) )
			xpDropOpacityPerTime = prefsTag.getDouble( "xpDropOpacityPerTime" );
		else
			xpDropOpacityPerTime = Config.forgeConfig.xpDropOpacityPerTime.get();

		if( prefsTag.contains( "xpDropMaxOpacity" ) )
			xpDropMaxOpacity = prefsTag.getDouble( "xpDropMaxOpacity" );
		else
			xpDropMaxOpacity = Config.forgeConfig.xpDropMaxOpacity.get();

		if( prefsTag.contains( "minXpGrow" ) )
			minXpGrow = prefsTag.getDouble( "minXpGrow" );
		else
			minXpGrow = Config.forgeConfig.minXpGrow.get();

		if( prefsTag.contains( "xpDropDecayAge" ) )
			xpDropDecayAge = (int) Math.floor( prefsTag.getDouble( "xpDropDecayAge" ) );
		else
			xpDropDecayAge = (int) Math.floor( Config.forgeConfig.xpDropDecayAge.get() );

		if( prefsTag.contains( "maxLevel" ) )
			maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );
		else
			maxLevel = (int) Math.floor( Config.forgeConfig.maxLevel.get() );

		if( prefsTag.contains( "maxXp" ) )
			maxXp = (int) Math.floor( Config.getConfig( "maxXp" ) );
		else
			maxXp = XP.xpAtLevel( maxLevel );

		if( prefsTag.contains( "xpDropsAttachedToBar" ) )
			xpDropsAttachedToBar = prefsTag.getDouble("xpDropsAttachedToBar") != 0;
		else
			xpDropsAttachedToBar = Config.forgeConfig.xpDropsAttachedToBar.get();

		if( prefsTag.contains( "xpBarAlwaysOn" ) )
			xpBarAlwaysOn = prefsTag.getDouble("xpBarAlwaysOn") != 0;
		else
			xpBarAlwaysOn = Config.forgeConfig.xpBarAlwaysOn.get();

		if( prefsTag.contains( "xpLeftDisplayAlwaysOn" ) )
			xpLeftDisplayAlwaysOn = prefsTag.getDouble("xpLeftDisplayAlwaysOn") != 0;
		else
			xpLeftDisplayAlwaysOn = Config.forgeConfig.xpLeftDisplayAlwaysOn.get();

		if( prefsTag.contains( "showXpDrops" ) )
			showXpDrops = prefsTag.getDouble("showXpDrops") != 0;
		else
			showXpDrops = Config.forgeConfig.showXpDrops.get();

		if( prefsTag.contains( "stackXpDrops" ) )
			stackXpDrops = prefsTag.getDouble("stackXpDrops") != 0;
		else
			stackXpDrops = Config.forgeConfig.stackXpDrops.get();

		if( prefsTag.contains( "lvlUpScreenshot" ) )
			lvlUpScreenshot = prefsTag.getDouble("lvlUpScreenshot") != 0;
		else
			lvlUpScreenshot = Config.forgeConfig.lvlUpScreenshot.get();

		if( prefsTag.contains( "lvlUpScreenshotShowSkills" ) )
			lvlUpScreenshotShowSkills = prefsTag.getDouble("lvlUpScreenshotShowSkills") != 0;
		else
			lvlUpScreenshotShowSkills = Config.forgeConfig.lvlUpScreenshotShowSkills.get();

		if( !xpDropsAttachedToBar )
			xpDropYLimit = 999999999;
		else
			xpDropYLimit = 0;

		if( barOffsetX < 0 || barOffsetX > 1 )
			barOffsetX = Config.forgeConfig.barOffsetX.get();

		if( barOffsetY < 0 || barOffsetY > 1 )
			barOffsetY = Config.forgeConfig.barOffsetY.get();

		if( xpDropOffsetX < 0 || xpDropOffsetX > 1 )
			xpDropOffsetX = Config.forgeConfig.xpDropOffsetX.get();

		if( xpDropOffsetY < 0 || xpDropOffsetY > 1 )
			xpDropOffsetY = Config.forgeConfig.xpDropOffsetY.get();

		if( veinBarOffsetX < 0 || veinBarOffsetX > 1 )
			veinBarOffsetX = Config.forgeConfig.veinBarOffsetX.get();

		if( veinBarOffsetY < 0 || veinBarOffsetY > 1 )
			veinBarOffsetY = Config.forgeConfig.veinBarOffsetY.get();

		if( xpDropSpawnDistance < 0 || xpDropSpawnDistance > 1000 )
			xpDropSpawnDistance = Config.forgeConfig.xpDropSpawnDistance.get();

		if( xpDropOpacityPerTime < 0 || xpDropOpacityPerTime > 255 )
			xpDropOpacityPerTime = Config.forgeConfig.xpDropOpacityPerTime.get();

		if( xpDropMaxOpacity < 0 || xpDropMaxOpacity > 255 )
			xpDropMaxOpacity = Config.forgeConfig.xpDropMaxOpacity.get();

		if( xpDropDecayAge < 0 || xpDropDecayAge > 5000 )
			xpDropDecayAge = (int) Math.floor( Config.forgeConfig.xpDropDecayAge.get() );

		if( minXpGrow < 0.01 || minXpGrow > 100 )
			minXpGrow = Config.getConfig( "minXpGrow" );

		biomePenaltyMultiplier = Config.getConfig( "biomePenaltyMultiplier" );
		maxVeinCharge = Config.getConfig( "maxVeinCharge" );
		veinMaxBlocks = (int) Config.getConfig( "veinMaxBlocks" );
	}

//	@SubscribeEvent
//	public void renderWorldDrops( RenderWorldLastEvent event )
//	{
//		MatrixStack matrixStack = event.getMatrixStack();
//		String theText = "test";
//
//		double d0 = this.renderManager.squareDistanceTo(entityIn);
//		if (!(d0 > 4096.0D)) {
//			boolean flag = !entityIn.isDiscrete();
//			float f = entityIn.getHeight() + 0.5F;
//			int i = "deadmau5".equals(displayNameIn) ? -10 : 0;
//			matrixStack.push();
//			matrixStack.translate(0.0D, (double)f, 0.0D);
//			matrixStack.rotate(this.renderManager.getCameraOrientation());
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
//	}
	
	public static void sendLvlUp( int level, Skill skill )
	{
		player = Minecraft.getInstance().player;
		player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelUp", level, new TranslationTextComponent( "pmmo." + skill.name().toLowerCase() ).getString() ).func_240703_c_( XP.skillStyle.get( skill ) ), false);
		if( skill == Skill.SWIMMING && level - 1 < Config.forgeConfig.nightvisionUnlockLevel.get() && level >= Config.forgeConfig.nightvisionUnlockLevel.get() )
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.nightvisionUnlocked" ).func_240703_c_( XP.skillStyle.get( skill ) ), true );

		guiWasOn = guiOn;

		if( lvlUpScreenshotShowSkills )
			guiOn = true;

		if( lvlUpScreenshot )
			screenshots.add( player.getDisplayName().getString() + " " + skill.name().toLowerCase() + " " + level );

//		XP.scanUnlocks( level, skill );

		NetworkHandler.sendToServer( new MessageLevelUp( skill.getValue(), level ) );
	}
	
	public static void makeXpDrop( double xp, Skill skillIn, int cooldown, double gainedXp, boolean skip )
	{
		xpDropWasStacked = false;

		if( XPOverlayGUI.skill.equals( Skill.INVALID_SKILL ) )
			XPOverlayGUI.skill = skillIn;

		if( skills.get( skillIn ) == null )				//Handle client xp tracker
			skills.put( skillIn, new ASkill( xp, XP.levelAtXpDecimal( xp ), xp, XP.levelAtXpDecimal( xp ) ) );
		
		aSkill = skills.get( skillIn );
		
		if( gainedXp == 0 )					//awardXp will NEVER award xp if the award is 0.
		{
			aSkill.pos = XP.levelAtXpDecimal( xp );
			aSkill.goalPos = aSkill.pos;
			aSkill.xp = xp;
			aSkill.goalXp = xp;
			XPOverlayGUI.cooldown = cooldown;

			for( int i = 0; i < xpDrops.size(); i++ )
			{
				if( xpDrops.get( i ).skill == skillIn )
				{
					xpDrops.remove( i );
					i = 0;
				}
			}

//			System.out.println( mc.player.getDisplayName().getFormattedText() + " " + skill.name() + " has been set to: " + xp );
		}
		else if( stackXpDrops && xpDrops.size() > 0 )
		{
			for( XpDrop xpDrop : xpDrops )
			{
				if( xpDrop.skill == skillIn && (xpDrop.age < xpDropDecayAge || xpDrop.Y > 0) )
				{
					xpDrop.gainedXp += gainedXp;
					xpDrop.startXp += gainedXp;
					if( xpDrops.get( xpDrops.size() - 1).age > xpDropDecayAge - 25 )
						xpDrops.get( xpDrops.size() - 1).age = xpDropDecayAge - 25;

					xpDropWasStacked = true;
				}
			}
		}

		if( !xpDropWasStacked && gainedXp != 0 )
		{
			if( xpDrops.size() > 0 && xpDrops.get( xpDrops.size() - 1 ).Y > 75 )
				xpDrops.add( new XpDrop( 0, xpDrops.get( xpDrops.size() - 1 ).Y + 25, skillIn, xp, gainedXp, skip ) );
			else
				xpDrops.add( new XpDrop( 0, xpDropSpawnDistance, skillIn, xp, gainedXp, skip ) );
		}
		
		if( !skip )
			XPOverlayGUI.cooldown = cooldown;

		levelGap = 0;
		skillGap = 0;
		xpGap = 0;

		skills.forEach( (thisSkill, thisASkill) ->
		{
			if( skills.get( thisSkill ).pos >= maxLevel )
			{
				if( levelGap < fontRenderer.getStringWidth( "" + maxLevel ) )
					levelGap = fontRenderer.getStringWidth( "" + maxLevel );
			}
			else
			{
				if( levelGap < fontRenderer.getStringWidth( DP.dp( XP.levelAtXpDecimal( skills.get( thisSkill ).goalXp ) ) ) )
					levelGap = fontRenderer.getStringWidth( DP.dp( XP.levelAtXpDecimal( skills.get( thisSkill ).goalXp ) ) );
			}

			if( skillGap < fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo." + thisSkill.name().toLowerCase() ).getString() ) )
				skillGap = fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo." + thisSkill.name().toLowerCase() ).getString() );

			if( xpGap < fontRenderer.getStringWidth( DP.dp( skills.get( thisSkill ).goalXp ) ) )
				xpGap = fontRenderer.getStringWidth( DP.dprefix( skills.get( thisSkill ).goalXp ) );
		});
	}
	
	public static void clearXP()
	{
		skills = new HashMap<>();
//		skillsKeys = new ArrayList<>();
		xpDrops = new ArrayList<>();
		xp = 0;
		skill = Skill.INVALID_SKILL;
		levelGap = 0;
		skillGap = 0;
		xpGap = 0;
	}
}
