package harmonised.pmmo.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.config.Config;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class XPOverlayGUI extends AbstractGui
{
	private static int barWidth = 102, barHeight = 5, barPosX, barPosY, xpDropPosX, xpDropPosY;
	private static int cooldown, tempAlpha, levelGap = 0, skillGap, halfscreen, tempInt, xpDropDecayAge = 0;
	private static double xp, goalXp;
	private static double lastTime, startLevel, timeDiff, tempDouble, tempDouble2, xpDropOffset = 0, xpDropOffsetCap = 0;
	private static double barOffsetX = 0, barOffsetY = 0, xpDropOffsetX = 0, xpDropOffsetY = 0, xpDropSpawnDistance = 0, xpDropOpacityPerTime = 0, xpDropMaxOpacity = 0;
	private static String tempString;
	private static int theme = 2, themePos = 1, listIndex = 0, xpDropYLimit = 0;
	private static String name = "none", tempName = "none";
	private static boolean stackXpDrops = true, init = false, showXpDrops = true, guiKey = false, guiPressed = false, guiOn = true, xpDropsAttachedToBar = true, xpDropWasStacked, xpLeftDisplayAlwaysOn, xpBarAlwaysOn;
	private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/xpbar.png" );
	private static ArrayList<XpDrop> xpDrops = new ArrayList<XpDrop>();
	private static Minecraft minecraft = Minecraft.getInstance();
	private static PlayerEntity player = minecraft.player;
	public static Map<String, ASkill> skills = new HashMap<>();
	private static ArrayList<String> skillsKeys = new ArrayList<String>();
	private static ASkill skill;
	private static FontRenderer fontRenderer = minecraft.fontRenderer;

	@SubscribeEvent
	public void renderOverlay( RenderGameOverlayEvent event )
	{
		if( !name.equals( "none" ) )
		{
			if( event.getType() == RenderGameOverlayEvent.ElementType.TEXT )	//Xp Drops
			{
				if( !init )
				{
					barOffsetX = Config.config.barOffsetX.get();
					barOffsetY = Config.config.barOffsetY.get();
					xpDropOffsetX = Config.config.xpDropOffsetX.get();
					xpDropOffsetY = Config.config.xpDropOffsetY.get();
					xpDropsAttachedToBar = Config.config.xpDropsAttachedToBar.get();
					showXpDrops = Config.config.showXpDrops.get();
					stackXpDrops = Config.config.stackXpDrops.get();
					xpDropSpawnDistance = Config.config.xpDropSpawnDistance.get();
					xpDropOpacityPerTime = Config.config.xpDropOpacityPerTime.get();
					xpDropMaxOpacity = Config.config.xpDropMaxOpacity.get();
					xpDropDecayAge = Config.config.xpDropDecayAge.get();
					xpBarAlwaysOn = Config.config.xpBarAlwaysOn.get();
					xpLeftDisplayAlwaysOn = Config.config.xpLeftDisplayAlwaysOn.get();

					if( !xpDropsAttachedToBar )
						xpDropYLimit = 999999999;
					else
						xpDropYLimit = 0;

					init = true;
				}
				RenderSystem.pushMatrix();
				RenderSystem.enableBlend();
				MainWindow sr = minecraft.getMainWindow();
				barPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * barOffsetX );
				barPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * barOffsetY );
				xpDropPosX = (int) ( ( sr.getScaledWidth() - barWidth ) * xpDropOffsetX );
				xpDropPosY = (int) ( ( sr.getScaledHeight() - barHeight ) * xpDropOffsetY );

				skill = skills.get( name );

				timeDiff = (System.nanoTime() - lastTime);
				lastTime = System.nanoTime();

				if( cooldown > 0 )
					cooldown -= timeDiff / 1000000;
				themePos += 2.5 + 7.5 * ( skill.pos % Math.floor( skill.pos ) ) * timeDiff / 1000000;

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
						if( skills.get( name ).xp >= XP.maxXp )
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
					XpDrop xpDrop = xpDrops.get( i );
					xpDrop.age += timeDiff / 5000000;

					if( ( ( xpDrop.Y - tempDouble * timeDiff / 10000000 < 0 ) && xpDrop.age >= xpDropDecayAge ) || !showXpDrops || ( !xpDropsAttachedToBar && xpDrop.age >= xpDropDecayAge ) )
					{
						skill = skills.get( xpDrop.name );

						if( !xpDrop.skip )
							name = xpDrop.name;

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
							skill.goalXp += xpDrop.gainedXp;
							xpDrop.gainedXp = 0;
						}
						else
						{
							xpDrop.gainedXp -= ( tempDouble2 * timeDiff / 10000000 );
							skill.goalXp += ( tempDouble2 * timeDiff / 10000000 );
						}
						skill.goalPos = XP.levelAtXpDecimal( skill.goalXp );
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
							drawCenteredString( fontRenderer, "+" + DP.dprefix( xpDrop.gainedXp ) + " " + new TranslationTextComponent( "pmmo.text." + xpDrop.name ).getString(), xpDropPosX + (barWidth / 2), (int) xpDrop.Y + (int) xpDropOffset + xpDropPosY, (tempAlpha << 24) |+ XP.getSkillColor( xpDrop.name ) );
					}
				}

				if( xpDrops.size() > 0 && xpDrops.get( 0 ).gainedXp <= 0 )
					xpDrops.remove( 0 );

				RenderSystem.disableBlend();
				RenderSystem.color3f( 255, 255, 255 );
				RenderSystem.popMatrix();
				
				for( String tag : skillsKeys )		//Update Skills
				{
					skill = skills.get( tag );
					
					startLevel = Math.floor( skill.pos );
					
					tempDouble = ( skill.goalPos - skill.pos ) * 50;
					if( tempDouble < 0.2 )
						tempDouble = 0.2;
					
					if( skill.pos < skill.goalPos )
					{
						skill.pos += 0.00005d * tempDouble;
						skill.xp   = XP.xpAtLevelDecimal( skill.pos );
						
//						if( cooldown < 10000 )
//							cooldown = 10000;
					}
					else if( skill.pos > skill.goalPos )
						skill.pos = skill.goalPos;
					
					if( startLevel < Math.floor( skill.pos ) )
						sendLvlUp( (int) Math.floor( skill.pos ), tag );
					
					if( skill.xp > skill.goalXp )
						skill.xp = skill.goalXp;
				}
				
				if( cooldown > 0 )				//Xp Bar
				{
					RenderSystem.pushMatrix();
					RenderSystem.enableBlend();
					Minecraft.getInstance().getTextureManager().bindTexture( bar );

					skill = skills.get( name );
					
					blit( barPosX, barPosY + 10, 0, 0, barWidth, barHeight );
					if( theme == 1 )
					{
						blit( barPosX, barPosY + 10, 0, barHeight * 1, (int) Math.floor( barWidth * ( skill.pos - Math.floor( skill.pos ) ) ), barHeight );
					}
					else
					{
						tempInt = (int) Math.floor( ( barWidth ) * ( skill.pos - Math.floor( skill.pos ) ) );

						if( tempInt > 100 )
							tempInt = 100;

						if( skill.pos >= XP.maxLevel )
							tempInt = 100;

						blit( barPosX, barPosY + 10, 0, barHeight*3, barWidth - 1, barHeight );
						blit( barPosX + 1, barPosY + 10, 1 + (int)( Math.floor( themePos / 100 ) ), barHeight*2, tempInt, barHeight );
					}
					if( skill.pos >= XP.maxLevel )
						drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + name ).getString(), XP.maxLevel ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( name ) );
					else
						drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + name ).getString(), DP.dp( Math.floor( skill.pos * 100 ) / 100 ) ).getString(), barPosX + (barWidth / 2), barPosY, XP.getSkillColor( name ) );
					
					if( (guiKey || xpLeftDisplayAlwaysOn) && skills.get( name ) != null )
					{
						if( skills.get( name ).xp >= XP.maxXp )
							drawCenteredString( fontRenderer, new TranslationTextComponent( "pmmo.text.maxLevel" ).getString(), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( name ) );
						else
						{
							if( goalXp >= XP.maxXp )
								goalXp =  XP.maxXp;
							
							goalXp = XP.xpAtLevel( XP.levelAtXp( skill.xp ) + 1 );
							drawCenteredString( fontRenderer, DP.dprefix( skills.get( name ).xp ) + " / " + DP.dprefix( goalXp ), barPosX + (barWidth / 2), 17 + barPosY, XP.getSkillColor( name ) );
							drawCenteredString( fontRenderer,  new TranslationTextComponent( "pmmo.text.xpLeft", DP.dprefix( goalXp - skill.xp ) ).getString(), barPosX + (barWidth / 2), 26 + barPosY, XP.getSkillColor( name ) );
						}
					}
					RenderSystem.disableBlend();
					RenderSystem.color3f( 255, 255, 255 );
					RenderSystem.popMatrix();
				}

				if( guiOn )
				{
						listIndex = 0;
						for( String tag : skillsKeys )
						{
							tempDouble = XP.levelAtXpDecimal( skills.get( tag ).xp );
							tempString = DP.dp( tempDouble );
							if( tempDouble >= XP.maxLevel )
								tempString = "" + XP.maxLevel;
							drawString( fontRenderer, tempString, 3, 3 + listIndex, XP.getSkillColor( tag ) );
							drawString( fontRenderer, " | " + new TranslationTextComponent( "pmmo.text." + tag ).getString(), levelGap + 4, 3 + listIndex, XP.getSkillColor( tag ) );
							drawString( fontRenderer, " | " + DP.dprefix( skills.get( tag ).xp ), levelGap + skillGap + 13, 3 + listIndex, XP.getSkillColor( tag ) );
							listIndex += 9;
						}
				}
			}
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
	
	public static void sendLvlUp( int level, String name )
	{
		player = minecraft.player;
		
//		switch( name )
//		{
//			case "swimming":
//				if( level - 1 < 25 && level >= 25 )
//					tempString = level + " swimming level up! Underwater Night Vision Unlocked!";
//				else
//					tempString = level + " swimming level up!";
//				break;
//		}
		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelUp", level, new TranslationTextComponent( "pmmo.text." + name ).getString() ).setStyle( new Style().setColor( XP.skillTextFormat.get( name ) ) ), false);
		if( name.equals( "swimming" ) && level - 1 < Config.config.nightvisionUnlockLevel.get() && level >= Config.config.nightvisionUnlockLevel.get() )
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.nightVisionUnlocked" ).setStyle( new Style().setColor( XP.skillTextFormat.get( name ) ) ), true );
	}
	
	public static void makeXpDrop( double xp, int id, int cooldown, double gainedXp, boolean skip )
	{
		tempName = Skill.getString( id );
		xpDropWasStacked = false;

		if( XPOverlayGUI.name.equals( "none" ) )
			XPOverlayGUI.name = tempName;

		if( skills.get( tempName ) == null )				//Handle client xp tracker
		{
			skills.put( tempName, new ASkill( xp, XP.levelAtXpDecimal( xp ), xp, XP.levelAtXpDecimal( xp ) ) );
			skillsKeys.add( tempName );
		}
		
		skill = skills.get( tempName );
		
		if( gainedXp == 0 )					//awardXp will NEVER award xp if the award is 0.
		{
			skill.pos = XP.levelAtXpDecimal( xp );
			skill.goalPos = skill.pos;
			skill.xp = xp;
			skill.goalXp = xp;
			XPOverlayGUI.cooldown = cooldown;


			System.out.println( minecraft.player.getDisplayName().getFormattedText() + " " + tempName + " has been set to: " + xp );
		}
		else if( stackXpDrops && xpDrops.size() > 0 )
		{
			for( XpDrop xpDrop : xpDrops )
			{
				if( xpDrop.name.equals( tempName ) && (xpDrop.age < xpDropDecayAge || xpDrop.Y > 0) )
				{
					xpDrop.gainedXp += gainedXp;
					xpDrop.startXp += gainedXp;
					if( xpDrops.get( xpDrops.size() - 1).age > xpDropDecayAge - 25 )
						xpDrops.get( xpDrops.size() - 1).age = xpDropDecayAge - 25;

					xpDropWasStacked = true;
				}
			}
		}

		if( !xpDropWasStacked )
		{
			if( xpDrops.size() > 0 && xpDrops.get( xpDrops.size() - 1 ).Y > 75 )
				xpDrops.add( new XpDrop( 0, xpDrops.get( xpDrops.size() - 1 ).Y + 25, tempName, xp, gainedXp, skip ) );
			else
				xpDrops.add( new XpDrop( 0, xpDropSpawnDistance, tempName, xp, gainedXp, skip ) );
		}
		
		if( !skip )
			XPOverlayGUI.cooldown = cooldown;

		levelGap = 0;
		skillGap = 0;

		skillsKeys.forEach( key ->
		{
			if( skills.get( key ).pos >= XP.maxLevel )
			{
				if( levelGap < fontRenderer.getStringWidth( "" + XP.maxLevel ) )
					levelGap = fontRenderer.getStringWidth( "" + XP.maxLevel );
			}
			else
			{
				if( levelGap < fontRenderer.getStringWidth( DP.dp( XP.levelAtXpDecimal( skills.get( key ).goalXp ) ) ) )
					levelGap = fontRenderer.getStringWidth( DP.dp( XP.levelAtXpDecimal( skills.get( key ).goalXp ) ) );
			}

			if( skillGap < fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo.text." + key ).getString() ) )
				skillGap = fontRenderer.getStringWidth( new TranslationTextComponent( "pmmo.text." + key ).getString() );
		});
	}
	
	public static void clearXP()
	{
		skills = new HashMap<>();
		skillsKeys = new ArrayList<>();
		xpDrops = new ArrayList<>();
		xp = 0;
		name = "none";
		levelGap = 0;
		skillGap = 0;
	}
}
