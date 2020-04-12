package harmonised.pmmo.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.proxy.ClientProxy;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XPOverlayGUI extends Gui
{
	private static int guiWidth = 102, guiHeight = 5;
	private static int cooldown, tempAlpha, halfScreen = 0;
	private static float xp, pos = 1, goalXp = 83, goalPos = 1;
	private static double lastTime, startLevel, timeDiff, tempDouble, tempDouble2, dropOffset, dropOffsetCap = 0;
	private static String tempString;
	private static int theme = 2, themePos = 1, listIndex = 0;
	private static String name = "none";
	private static Minecraft mc = Minecraft.getMinecraft();
	private static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
	private static boolean guiKey = false;
	private static boolean guiPressed = false;
	private static boolean guiOn = true;
	private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/xpbar.png" );
	private static ArrayList<XpDrop> xpDrops = new ArrayList<XpDrop>();
	private static Minecraft minecraft = Minecraft.getMinecraft();
	private static EntityPlayer player = minecraft.player;
	public static Map<String, Skill> skills = new HashMap<>();
	private static ArrayList<String> skillsKeys = new ArrayList<String>();
	private static Skill skill;
	
	@SubscribeEvent
	public void renderOverlay( RenderGameOverlayEvent event )
	{
		if( !name.equals( "none" ) )
		{
			if( event.getType() == RenderGameOverlayEvent.ElementType.TEXT )	//Xp Drops
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				ScaledResolution sr = new ScaledResolution( mc );
				halfScreen = ( sr.getScaledWidth() - guiWidth ) / 2;
				
				timeDiff = (System.nanoTime() - lastTime);
				lastTime = System.nanoTime();
				
				pos = skill.pos;
				if( cooldown > 0 )
					cooldown -= timeDiff / 1000000;
				themePos += 2.5 + 7.5 * ( pos % Math.floor( pos ) ) * timeDiff / 1000000;
				
				if( themePos > 10000 )
					themePos =  themePos % 10000;
				
				skill = skills.get( name );
				
				guiKey = ClientProxy.SHOW_GUI.isKeyDown();
				
				if( guiKey )
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
			
				if( cooldown <= 0 )
					dropOffsetCap = -9;
				else if ( guiKey )
					dropOffsetCap = 34;
				else
					dropOffsetCap = 16;
				
				if( dropOffset > dropOffsetCap )
					dropOffset -= 1d * timeDiff / 10000000;
				
				if( dropOffset < dropOffsetCap )
					dropOffset = dropOffsetCap;
				
				for( int i = 0; i < xpDrops.size(); i++ )				//update Xp Drops
				{
					XpDrop xpDrop = xpDrops.get( i );
					xpDrop.age += timeDiff / 5000000;
					
					if( ( xpDrop.Y - tempDouble * timeDiff / 10000000 < 0 ) && xpDrop.age >= 500 )
					{
						skill = skills.get( xpDrop.name );
						
						if( !xpDrop.skip )
							name = xpDrop.name;
						
						tempDouble2 = xpDrop.gainedXp * 0.03 * timeDiff / 10000000;
						if( tempDouble2 < 0.1 )
							tempDouble2 = 0.1;
						
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
						
						if( xpDrop.gainedXp <= 0 )
							xpDrops.remove( i );
						
						skill.goalPos = XP.levelAtXpDecimal( skill.goalXp );
						
						
					}
					
					tempDouble = 0.75f + ( 1 * xpDrops.size() * 0.02f );			//Xp Drop Y
					
					if( dropOffset == dropOffsetCap )
						xpDrop.Y -= tempDouble * timeDiff / 10000000;
					
					if( xpDrop.Y < ( i * 9 ) )
						xpDrop.Y = ( i * 9 );
					
					if( xpDrop.Y * 2 > 200 )
						tempAlpha = 0;
					else
						tempAlpha = (int) Math.floor(200 - xpDrop.Y * 2 );
					
					
					if( tempAlpha > 3 )
					{
						drawCenteredString( fontRenderer, "+" + DP.dprefix( xpDrop.gainedXp ) + " in " + xpDrop.name, halfScreen + (guiWidth / 2), (int) xpDrop.Y + (int) dropOffset, (tempAlpha << 24) |+ XP.getSkillColor( xpDrop.name ) );
					}
				}
				GlStateManager.disableBlend();
				GlStateManager.color( 255, 255, 255 );
				GlStateManager.popMatrix();
				
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
					else if( pos >= goalPos )
						pos = goalPos;
					
					if( startLevel < Math.floor( skill.pos ) )
						sendLvlUp( (int) Math.floor( skill.pos ), tag );
					
					if( skill.xp > skill.goalXp )
						skill.xp = skill.goalXp;
					
					if( goalPos == 999.99f )
						goalPos = 999.991f;
				}
				
				if( cooldown > 0 )				//Xp Bar
				{
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					mc.renderEngine.bindTexture( bar );
					
					
					skill = skills.get( name );
					
					drawTexturedModalRect( halfScreen, 10, 0, 0, guiWidth, guiHeight );
					if( theme == 1 )
					{
						drawTexturedModalRect( halfScreen, 10, 0, guiHeight * 1, (int) Math.floor( guiWidth * ( skill.pos - Math.floor( skill.pos ) ) ), guiHeight );
					}
					else
					{
						drawTexturedModalRect( halfScreen, 10, 0, guiHeight*3, guiWidth - 1, guiHeight );
						drawTexturedModalRect( halfScreen + 1, 10, 1 + (int)( Math.floor( themePos / 100 ) ), guiHeight*2, (int) Math.floor( ( guiWidth - 1 ) * ( skill.pos - Math.floor( skill.pos ) ) ), guiHeight );
					}
					drawCenteredString( fontRenderer, name + " " + DP.dp( skill.pos ), halfScreen + (guiWidth / 2), 0, XP.getSkillColor( name ) );

					if( guiKey && skills.get( name ) != null )
					{
						if( skills.get( name ).xp >= XP.maxXp )
						{
							drawCenteredString( fontRenderer, "MAX LEVEL", halfScreen + (guiWidth / 2), 17, XP.getSkillColor( name ) );
						}
						else
						{
							if( goalXp >= XP.maxXp )
								goalXp =  XP.maxXp;

							goalXp = XP.xpAtLevel( XP.levelAtXp( skill.xp ) + 1 );
							drawCenteredString( fontRenderer, DP.dprefix( skills.get( name ).xp ) + " / " + DP.dprefix( goalXp ), halfScreen + (guiWidth / 2), 17, XP.getSkillColor( name ) );
							drawCenteredString( fontRenderer, DP.dprefix( goalXp - skill.xp ) + " left", halfScreen + (guiWidth / 2), 26, XP.getSkillColor( name ) );
						}
					}

					GlStateManager.disableBlend();
					GlStateManager.color( 255, 255, 255 );
					GlStateManager.popMatrix();
				}

				if( guiOn )
				{
					listIndex = 0;
					for( String tag : skillsKeys )
					{
						tempDouble = XP.levelAtXpDecimal( skills.get( tag ).xp );
						tempString = DP.dp( tempDouble );
						drawString( fontRenderer, tempString, 3, 3 + listIndex, XP.getSkillColor( tag ) );
						drawString( fontRenderer, " | " + tag, 32, 3 + listIndex, XP.getSkillColor( tag ) );
						drawString( fontRenderer, " | " + DP.dprefix( skills.get( tag ).xp ), 102, 3 + listIndex, XP.getSkillColor( tag ) );
						listIndex += 9;
					}
				}
			}
		}
	}
	
	public static void sendLvlUp( int level, String name )
	{
		player = minecraft.player;
		
		switch( name )
		{
			case "agility":
				float saveChance = level * 0.64f;
				if( saveChance > 64 )
					saveChance = 64;
				
				float speedPercent = level / 2000f;
				if( speedPercent > 0.10f )
					speedPercent = 0.10f;
				speedPercent = speedPercent / 0.10f * 100f;
				
				tempString = level + " agility level up! " + DP.dp( saveChance ) + "% save chance! + " + DP.dp( speedPercent ) + "% speed boost!";
				break;
		
			case "building":
				tempString = level + " building level up!";
				break;
		
			case "endurance":
				float endurancePercent = level * 0.25f;
				if( endurancePercent > 50 )
					endurancePercent = 50;
				tempString = level + " endurance level up! " + DP.dp( endurancePercent ) + "% endurance! +" + (int) Math.floor( level / 10 ) + " Max Heart" + ( Math.floor( level/10 ) == 1 ? "" : "s" ) + "!";
				break;
				
			case "combat":
				tempString = level + " combat level up! " + (int) Math.floor( level / 20 ) + " Bonus Damage!";
				break;
				
			case "repairing":
				tempString = level + " repairing level up! " + (int) Math.floor( 50 - ( level / 4 ) ) + " max anvil cost!";
				break;
				
			case "swimming":
				if( level - 1 < 25 && level >= 25 )
					tempString = level + " swimming level up! Underwater Night Vision Unlocked!";
				else
					tempString = level + " swimming level up!";
				break;
				
			default:
				tempString = level + " " + name + " level up!";
				break;
		}
		player.sendStatusMessage( new TextComponentString( tempString ).setStyle( new Style().setColor( XP.skillTextFormat.get( name ) ) ), false);
	}
	
	public static void makeXpDrop( float xp, String name, int cooldown, float gainedXp, boolean skip )
	{
		if( XPOverlayGUI.name.equals( "none" ) )
			XPOverlayGUI.name = name;
		
		if( skills.get( name ) == null )				//Handle client xp tracker
		{
			skills.put( name, new Skill( xp, XP.levelAtXpDecimal( xp ), xp, XP.levelAtXpDecimal( xp ) ) );
			skillsKeys.add( name );
		}
		
		skill = skills.get( name );
		
		if( gainedXp == 0 )					//awardXp will NEVER award xp if the award is 0.
		{
			skill.pos = XP.levelAtXpDecimal( xp );
			skill.goalPos = pos;
			System.out.println( Minecraft.getMinecraft().player.getName() + " " + skill + " has been set to: " + xp );
			skill.xp = xp;
			skill.goalXp = xp;
			XPOverlayGUI.cooldown = cooldown;
			return;
		}
		
		if( xpDrops.size() > 0 && xpDrops.get( xpDrops.size() - 1 ).name.equals( name ) && xpDrops.get( xpDrops.size() - 1 ).age < 500 )
		{
			xpDrops.get( xpDrops.size() - 1).gainedXp += gainedXp;
			if( xpDrops.get( xpDrops.size() - 1).age > 475 )
				xpDrops.get( xpDrops.size() - 1).age = 475;
		}
		else
		{
			if( xpDrops.size() > 0 && xpDrops.get( xpDrops.size() - 1 ).Y > 75 )
				xpDrops.add( new XpDrop( 0, xpDrops.get( xpDrops.size() - 1 ).Y + 25, name, xp, gainedXp, skip ) );
			else
				xpDrops.add( new XpDrop( 0, 100, name, xp, gainedXp, skip ) );
		}
		
		if( !skip )
			XPOverlayGUI.cooldown = cooldown;
	}
	
	public static void clearXP()
	{
		skills = new HashMap<>();
		skillsKeys = new ArrayList<>();
		xpDrops = new ArrayList<>();
		xp = 0;
		pos = 1;
		goalPos = 1;
		name = "none";
	}
	
//	@SubscribeEvent
//	public void onUpdate( ClientTickEvent event )
//	{
//		
//	}
}
