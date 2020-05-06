package harmonised.pmmo.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class XPOverlayGUI extends AbstractGui
{
	private static int barWidth = 102, barHeight = 5, barPosX, barPosY, xpDropPosX, xpDropPosY;
	private static int cooldown, tempAlpha, levelGap = 0, skillGap, xpGap, halfscreen, tempInt, xpDropDecayAge = 0;
	private static double xp, goalXp;
	private static double lastTime, startLevel, timeDiff, tempDouble, tempDouble2, xpDropOffset = 0, xpDropOffsetCap = 0;
	private static double barOffsetX = 0, barOffsetY = 0, xpDropOffsetX = 0, xpDropOffsetY = 0, xpDropSpawnDistance = 0, xpDropOpacityPerTime = 0, xpDropMaxOpacity = 0, biomePenaltyMultiplier = 0;
	private static String tempString;
	private static int theme = 2, themePos = 1, listIndex = 0, xpDropYLimit = 0;
	private static String skillName = "none";
	private static boolean metBiomeReq = true, stackXpDrops = true, init = false, showXpDrops = true, guiKey = false, guiPressed = false, guiOn = true, xpDropsAttachedToBar = true, xpDropWasStacked, xpLeftDisplayAlwaysOn, xpBarAlwaysOn;
	private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/xpbar.png" );
	private static ArrayList<XpDrop> xpDrops = new ArrayList<XpDrop>();
	private static Minecraft minecraft = Minecraft.getInstance();
	private static PlayerEntity player = minecraft.player;
	public static Map<Skill, ASkill> skills = new HashMap<>();
//	private static ArrayList<String> skillsKeys = new ArrayList<String>();
	private static ASkill aSkill;
	private static Skill skill = Skill.INVALID_SKILL, tempSkill = Skill.INVALID_SKILL;
	private static FontRenderer fontRenderer = minecraft.fontRenderer;
	private static int maxLevel = (int) Math.floor( XP.getConfig( "maxLevel" ) );
	private static double maxXp = XP.getConfig( "maxXp" );
	private static XpDrop xpDrop;
	private static int color;

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
				MainWindow sr = minecraft.getMainWindow();
				barPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * barOffsetX );
				barPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * barOffsetY );
				xpDropPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * xpDropOffsetX );
				xpDropPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * xpDropOffsetY );

				aSkill = skills.get( skill );

				timeDiff = (System.nanoTime() - lastTime);
				lastTime = System.nanoTime();

				if( cooldown > 0 )
					cooldown -= timeDiff / 1000000;
				themePos += ( 2.5 + 7.5 * ( aSkill.pos % Math.floor( aSkill.pos ) ) ) * (timeDiff / 1000000);

				if( themePos > 10000 )
					themePos =  themePos % 10000;

				guiKey = ClientHandler.SHOW_GUI.isKeyDown();
//				guiKey = true;

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
				
				for( int i = 0; i < xpDrops.size(); i++ )				//update Xp Drops
				{
					xpDrop = xpDrops.get( i );
					xpDrop.age += timeDiff / 5000000;

					if( ( ( xpDrop.Y - tempDouble * timeDiff / 10000000 < 0 ) && xpDrop.age >= xpDropDecayAge ) || !showXpDrops || ( !xpDropsAttachedToBar && xpDrop.age >= xpDropDecayAge ) )
					{
						aSkill = skills.get( xpDrop.skill );

						if( !xpDrop.skip )
							skill = xpDrop.skill;

						tempDouble2 = xpDrop.gainedXp * 0.03 * timeDiff / 10000000;
						if( stackXpDrops )
						{
							if( tempDouble2 < 0.1 )
								tempDouble2 = 0.1;
						}
						else
							if( tempDouble2 < 1 )
								tempDouble2 = 1;

						if( xpDrop.gainedXp - ( tempDouble2 * timeDiff / 10000000 ) < 0 )
						{
							aSkill.goalXp += xpDrop.gainedXp;
							xpDrop.gainedXp = 0;
						}
						else
						{
							xpDrop.gainedXp -= ( tempDouble2 * timeDiff / 10000000 );
							aSkill.goalXp += ( tempDouble2 * timeDiff / 10000000 );
						}
						aSkill.goalPos = XP.levelAtXpDecimal( aSkill.goalXp );
					}

					if( showXpDrops )
					{

						tempDouble = 0.75f + ( 1 * xpDrops.size() * 0.02f );			//Xp Drop Y

						if( xpDropOffset == xpDropOffsetCap )
							xpDrop.Y -= tempDouble * timeDiff / 10000000;

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
							drawCenteredString( fontRenderer, "+" + DP.dprefix( xpDrop.gainedXp ) + " " + new TranslationTextComponent( "pmmo.text." + xpDrop.skill.name().toLowerCase() ).getString(), xpDropPosX + (barWidth / 2), (int) xpDrop.Y + (int) xpDropOffset + xpDropPosY, (tempAlpha << 24) |+ XP.getSkillColor( xpDrop.skill ) );
					}
				}

				if( xpDrops.size() > 0 && xpDrops.get( 0 ).gainedXp <= 0 )
					xpDrops.remove( 0 );

				RenderSystem.disableBlend();
				RenderSystem.color3f( 255, 255, 255 );
				RenderSystem.popMatrix();
				
				for( Map.Entry<Skill, ASkill> entry : skills.entrySet() )		//Update Skills
				{
					aSkill = entry.getValue();
					
					startLevel = Math.floor( aSkill.pos );
					
					tempDouble = ( aSkill.goalPos - aSkill.pos ) * 50;
					if( tempDouble < 0.2 )
						tempDouble = 0.2;
					
					if( aSkill.pos < aSkill.goalPos )
					{
						aSkill.pos += 0.00005d * tempDouble;
						aSkill.xp   = XP.xpAtLevelDecimal( aSkill.pos );
						
//						if( cooldown < 10000 )
//							cooldown = 10000;
					}
					else if( aSkill.pos > aSkill.goalPos )
						aSkill.pos = aSkill.goalPos;
					
					if( startLevel < Math.floor( aSkill.pos ) )
						sendLvlUp( (int) Math.floor( aSkill.pos ), entry.getKey() );
					
					if( aSkill.xp > aSkill.goalXp )
						aSkill.xp = aSkill.goalXp;
				}
				
				if( cooldown > 0 )				//Xp Bar
				{
					RenderSystem.pushMatrix();
					RenderSystem.enableBlend();
					Minecraft.getInstance().getTextureManager().bindTexture( bar );

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
						blit( barPosX + 1, barPosY + 10, 1 + (int)( Math.floor( themePos / 100 ) ), barHeight*2, tempInt, barHeight );
					}
					if( aSkill.pos >= maxLevel )
						drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + skill.name().toLowerCase() ).getString(), maxLevel ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( skill ) );
					else
						drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + skill.name().toLowerCase() ).getString(), DP.dp( Math.floor( aSkill.pos * 100 ) / 100 ) ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( skill ) );
					
					if( (guiKey || xpLeftDisplayAlwaysOn) && skills.get( skill ) != null )
					{
						if( skills.get( skill ).xp >= maxXp )
							drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.maxLevel" ).getString(), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( skill ) );
						else
						{
							if( goalXp >= maxXp )
								goalXp =  maxXp;
							
							goalXp = XP.xpAtLevel( XP.levelAtXp( aSkill.xp ) + 1 );
							drawCenteredString( fontRenderer, DP.dprefix( skills.get( skill ).xp ) + " / " + DP.dprefix( goalXp ), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( skill ) );
							drawCenteredString( fontRenderer,  new TranslationTextComponent( "pmmo.text.xpLeft", DP.dprefix( goalXp - aSkill.xp ) ).getString(), barPosX + (barWidth / 2), 26 + barPosY, XP.getSkillColor( skill ) );
						}
					}
					RenderSystem.disableBlend();
					RenderSystem.color3f( 255, 255, 255 );
					RenderSystem.popMatrix();
				}

				if( guiOn )
				{
						listIndex = 0;
						for( Map.Entry<Skill, ASkill> entry : skills.entrySet() )
						{
							skillName = entry.getKey().name().toLowerCase();
							tempDouble = XP.levelAtXpDecimal( entry.getValue().xp );
							tempString = DP.dp( tempDouble );
							color = XP.getSkillColor( entry.getKey() );
							if( tempDouble >= maxLevel )
								tempString = "" + maxLevel;
							drawString( fontRenderer, tempString, 3, 3 + listIndex, color );
							drawString( fontRenderer, " | " + new TranslationTextComponent( "pmmo.text." + skillName ).getString(), levelGap + 4, 3 + listIndex, color );
							drawString( fontRenderer, " | " + DP.dprefix( entry.getValue().xp ), levelGap + skillGap + 13, 3 + listIndex, color );

							ResourceLocation resLoc = player.world.getBiome( player.getPosition() ).getRegistryName();
							String biomeKey = resLoc.toString();
							metBiomeReq = XP.checkReq( player, resLoc, "biome" );

							if( !metBiomeReq )
							{
								if( biomePenaltyMultiplier < 1 )
								{
									tempDouble = (biomePenaltyMultiplier - 1) * 100;
									tempDouble = Math.floor( tempDouble * 100 ) / 100;
									tempString = ( tempDouble % 1 == 0 ? (int) Math.floor( tempDouble ) : DP.dp( tempDouble ) ) + "%";
									drawString( fontRenderer, tempString, levelGap + skillGap + xpGap + 25, 3 + listIndex, XP.getSkillColor( skill ) );
								}
							}
							else if( Requirements.data.get( "biomeMultiplier" ).containsKey( biomeKey ) )
							{
								if( Requirements.data.get( "biomeMultiplier" ).get( biomeKey ).containsKey( skillName ) )
								{
									tempDouble = ( (double) Requirements.data.get( "biomeMultiplier" ).get( biomeKey ).get( skillName ) - 1 ) * 100;
									tempDouble = Math.floor( tempDouble * 100 ) / 100;

									if( tempDouble > 0 )
										tempString = "+" + ( tempDouble % 1 == 0 ? (int) Math.floor( tempDouble ) : DP.dp( tempDouble ) ) + "%";
									else if ( tempDouble < 0 )
										tempString = ( tempDouble % 1 == 0 ? (int) Math.floor( tempDouble ) : DP.dp( tempDouble ) ) + "%";
									else
										tempString = "";

									drawString( fontRenderer, tempString, levelGap + skillGap + xpGap + 25, 3 + listIndex, XP.getSkillColor( skill ) );
								}
							}
							

							listIndex += 9;
						}
				}
			}
		}
	}

	public static void doInit()
	{
		player = Minecraft.getInstance().player;

		if( player != null )
		{
			CompoundNBT prefsTag = XP.getPreferencesTag( player );

			if( prefsTag.contains( "barOffsetX" ) )
				barOffsetX = prefsTag.getDouble( "barOffsetX" );
			else
				barOffsetX = Config.config.barOffsetX.get();

			if( prefsTag.contains( "barOffsetY" ) )
				barOffsetY = prefsTag.getDouble( "barOffsetY" );
			else
				barOffsetY = Config.config.barOffsetY.get();

			if( prefsTag.contains( "xpDropOffsetX" ) )
				xpDropOffsetX = prefsTag.getDouble( "xpDropOffsetX" );
			else
				xpDropOffsetX = Config.config.xpDropOffsetX.get();

			if( prefsTag.contains( "xpDropOffsetY" ) )
				xpDropOffsetY = prefsTag.getDouble( "xpDropOffsetY" );
			else
				xpDropOffsetY = Config.config.xpDropOffsetY.get();

			if( prefsTag.contains( "xpDropSpawnDistance" ) )
				xpDropSpawnDistance = prefsTag.getDouble( "xpDropSpawnDistance" );
			else
				xpDropSpawnDistance = Config.config.xpDropSpawnDistance.get();

			if( prefsTag.contains( "xpDropOpacityPerTime" ) )
				xpDropOpacityPerTime = prefsTag.getDouble( "xpDropOpacityPerTime" );
			else
				xpDropOpacityPerTime = Config.config.xpDropOpacityPerTime.get();

			if( prefsTag.contains( "xpDropMaxOpacity" ) )
				xpDropMaxOpacity = prefsTag.getDouble( "xpDropMaxOpacity" );
			else
				xpDropMaxOpacity = Config.config.xpDropMaxOpacity.get();

			if( prefsTag.contains( "xpDropDecayAge" ) )
				xpDropDecayAge = (int) Math.floor( prefsTag.getDouble( "xpDropDecayAge" ) );
			else
				xpDropDecayAge = (int) Math.floor( Config.config.xpDropDecayAge.get() );

			if( prefsTag.contains( "xpDropsAttachedToBar" ) )
			{
				if( prefsTag.getDouble( "xpDropsAttachedToBar" ) == 0 )
					xpDropsAttachedToBar = false;
				else
					xpDropsAttachedToBar = true;
			}
			else
				xpDropsAttachedToBar = Config.config.xpDropsAttachedToBar.get();

			if( prefsTag.contains( "xpBarAlwaysOn" ) )
			{
				if( prefsTag.getDouble( "xpBarAlwaysOn" ) == 0 )
					xpBarAlwaysOn = false;
				else
					xpBarAlwaysOn = true;
			}
			else
				xpBarAlwaysOn = Config.config.xpBarAlwaysOn.get();

			if( prefsTag.contains( "xpLeftDisplayAlwaysOn" ) )
			{
				if( prefsTag.getDouble( "xpLeftDisplayAlwaysOn" ) == 0 )
					xpLeftDisplayAlwaysOn = false;
				else
					xpLeftDisplayAlwaysOn = true;
			}
			else
				xpLeftDisplayAlwaysOn = Config.config.xpLeftDisplayAlwaysOn.get();

			if( prefsTag.contains( "showXpDrops" ) )
			{
				if( prefsTag.getDouble( "showXpDrops" ) == 0 )
					showXpDrops = false;
				else
					showXpDrops = true;
			}
			else
				showXpDrops = Config.config.showXpDrops.get();

			if( prefsTag.contains( "stackXpDrops" ) )
			{
				if( prefsTag.getDouble( "stackXpDrops" ) == 0 )
					stackXpDrops = false;
				else
					stackXpDrops = true;
			}
			else
				stackXpDrops = Config.config.stackXpDrops.get();

			if( !xpDropsAttachedToBar )
				xpDropYLimit = 999999999;
			else
				xpDropYLimit = 0;

			if( barOffsetX < 0 || barOffsetX > 1 )
				barOffsetX = Config.config.barOffsetX.get();

			if( barOffsetY < 0 || barOffsetY > 1 )
				barOffsetY = Config.config.barOffsetY.get();

			if( xpDropOffsetX < 0 || xpDropOffsetX > 1 )
				xpDropOffsetX = Config.config.xpDropOffsetX.get();

			if( xpDropOffsetY < 0 || xpDropOffsetY > 1 )
				xpDropOffsetY = Config.config.xpDropOffsetY.get();

			if( xpDropSpawnDistance < 0 || xpDropSpawnDistance > 1000 )
				xpDropSpawnDistance = Config.config.xpDropSpawnDistance.get();

			if( xpDropOpacityPerTime < 0 || xpDropOpacityPerTime > 255 )
				xpDropOpacityPerTime = Config.config.xpDropOpacityPerTime.get();

			if( xpDropMaxOpacity < 0 || xpDropMaxOpacity > 255 )
				xpDropMaxOpacity = Config.config.xpDropMaxOpacity.get();

			if( xpDropDecayAge < 0 || xpDropDecayAge > 5000 )
				xpDropDecayAge = (int) Math.floor( Config.config.xpDropDecayAge.get() );

			biomePenaltyMultiplier = Config.config.biomePenaltyMultiplier.get();
		}
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

//		switch( name )
//		{
//			case "swimming":
//				if( level - 1 < 25 && level >= 25 )
//					tempString = level + " swimming level up! Underwater Night Vision Unlocked!";
//				else
//					tempString = level + " swimming level up!";
//				break;
//		}
		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelUp", level, new TranslationTextComponent( "pmmo.text." + skill.name().toLowerCase() ).getString() ).setStyle( new Style().setColor( XP.skillTextFormat.get( skill.name().toLowerCase() ) ) ), false);
		if( skill == Skill.SWIMMING && level - 1 < Config.config.nightvisionUnlockLevel.get() && level >= Config.config.nightvisionUnlockLevel.get() )
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.nightVisionUnlocked" ).setStyle( new Style().setColor( XP.skillTextFormat.get( skill.name().toLowerCase() ) ) ), true );
	}
	
	public static void makeXpDrop( double xp, Skill skill, int cooldown, double gainedXp, boolean skip )
	{
		tempSkill = skill;
		xpDropWasStacked = false;

		if( XPOverlayGUI.skill.equals( Skill.INVALID_SKILL ) )
			XPOverlayGUI.skill = tempSkill;

		if( skills.get( tempSkill ) == null )				//Handle client xp tracker
			skills.put( tempSkill, new ASkill( xp, XP.levelAtXpDecimal( xp ), xp, XP.levelAtXpDecimal( xp ) ) );
		
		aSkill = skills.get( tempSkill );
		
		if( gainedXp == 0 )					//awardXp will NEVER award xp if the award is 0.
		{
			aSkill.pos = XP.levelAtXpDecimal( xp );
			aSkill.goalPos = aSkill.pos;
			aSkill.xp = xp;
			aSkill.goalXp = xp;
			XPOverlayGUI.cooldown = cooldown;

			for( int i = 0; i < xpDrops.size(); i++ )
			{
				if( xpDrops.get( i ).skill == tempSkill )
				{
					xpDrops.remove( i );
					i = 0;
				}
			}

			System.out.println( minecraft.player.getDisplayName().getFormattedText() + " " + skill.name() + " has been set to: " + xp );
		}
		else if( stackXpDrops && xpDrops.size() > 0 )
		{
			for( XpDrop xpDrop : xpDrops )
			{
				if( xpDrop.skill == tempSkill && (xpDrop.age < xpDropDecayAge || xpDrop.Y > 0) )
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
				xpDrops.add( new XpDrop( 0, xpDrops.get( xpDrops.size() - 1 ).Y + 25, tempSkill, xp, gainedXp, skip ) );
			else
				xpDrops.add( new XpDrop( 0, xpDropSpawnDistance, tempSkill, xp, gainedXp, skip ) );
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

			if( skillGap < fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo.text." + thisSkill.name().toLowerCase() ).getString() ) )
				skillGap = fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo.text." + thisSkill.name().toLowerCase() ).getString() );

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
