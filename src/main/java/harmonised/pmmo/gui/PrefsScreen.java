package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerTickHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class PrefsScreen extends GuiScreen
{
    public static Map<String, Double> prefsMap;
    private final List<GuiButton> buttons = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private int i;
    private double value;
    private static TileButton exitButton;

    ScaledResolution sr = new ScaledResolution( Minecraft.getMinecraft() );
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX;
    private PrefsScrollPanel scrollPanel;
    private final JType jType;
    private ArrayList<PrefsEntry> prefsEntries;
    private ITextComponent title;
    private EntityPlayer player = Minecraft.getMinecraft().player;

    public PrefsScreen( ITextComponent titleIn, JType jType )
    {
        super();
        this.title = titleIn;
        this.jType = jType;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    public void initGui()
    {
        sr = new ScaledResolution( mc );
        prefsMap = FConfig.getPreferencesMap( player );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        exitButton = new TileButton( 1337, x + boxWidth - 24, y - 8, 7, 0, "", JType.STATS, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new PrefsChoiceScreen( new TextComponentTranslation( "pmmo.stats" ) ) );
        });
        prefsEntries = new ArrayList<>();

        switch( jType )
        {
            case SETTINGS:
                value = Math.min( Skill.BUILDING.getLevel( player ) / FConfig.getConfig( "levelsPerOneReach" ), FConfig.getConfig( "maxExtraReachBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraReachBoost", "", "", 0, value, prefsMap.getOrDefault( "maxExtraReachBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.ENDURANCE.getLevel( player ) / FConfig.getConfig( "levelsPerHeart" ), FConfig.getConfig( "maxExtraHeartBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraHeartBoost", "", "", 0, value, prefsMap.getOrDefault( "maxExtraHeartBoost", value ), value, false, true, true, false ) );
                value = Math.min( Skill.COMBAT.getLevel( player ) / FConfig.getConfig( "levelsPerDamageMelee" ), FConfig.getConfig( "maxExtraDamageBoostMelee" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamageBoostMelee", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamageBoostMelee", value ), value, false, true, true, false ) );
                value = Math.min( Skill.ARCHERY.getLevel( player ) / FConfig.getConfig( "levelsPerDamageMelee" ), FConfig.getConfig( "maxExtraDamageBoostArchery" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamageBoostArchery", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamageBoostArchery", value ), value, false, true, true, false ) );
                value = Math.min( Skill.MAGIC.getLevel( player ) / FConfig.getConfig( "levelsPerDamageMelee" ), FConfig.getConfig( "maxExtraDamageBoostMagic" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamageBoostMagic", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamageBoostMagic", value ), value, false, true, true, false ) );
                value = Math.min( Skill.AGILITY.getLevel( player ) * FConfig.getConfig( "speedBoostPerLevel" ), FConfig.getConfig( "maxSpeedBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSpeedBoost", "", "", 0, value, prefsMap.getOrDefault( "maxSpeedBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.AGILITY.getLevel( player ) * FConfig.getConfig( "levelsPerSprintJumpBoost" ), FConfig.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSprintJumpBoost", "", "", 0, value, prefsMap.getOrDefault( "maxSprintJumpBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.AGILITY.getLevel( player ) * FConfig.getConfig( "levelsPerCrouchJumpBoost" ), FConfig.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxCrouchJumpBoost", "", "", 0, value, prefsMap.getOrDefault( "maxCrouchJumpBoost", value ), value, true, true, true, false ) );
                prefsEntries.add( new PrefsEntry("wipeAllSkillsUponDeathPermanently", "", "", 0, 1, prefsMap.getOrDefault( "wipeAllSkillsUponDeathPermanently", 0D ), 0, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("spawnFireworksCausedByMe", "", "", 0, 1, prefsMap.getOrDefault( "spawnFireworksCausedByMe", 1D ), 1, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("spawnFireworksCausedByOthers", "", "", 0, 1, prefsMap.getOrDefault( "spawnFireworksCausedByOthers", 1D ), 1, false, true, false, true ) );
                break;

            case GUI_SETTINGS:
                prefsEntries.add( new PrefsEntry("barOffsetX", "", "", 0, 1, prefsMap.getOrDefault("barOffsetX", 0.5D ), 0.5D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("barOffsetY", "", "", 0, 1, prefsMap.getOrDefault( "barOffsetY", 0D ), 0D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("veinBarOffsetX", "", "", 0, 1, prefsMap.getOrDefault( "veinBarOffsetX", 0.5D ), 0.5D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("veinBarOffsetY", "", "", 0, 1, prefsMap.getOrDefault( "veinBarOffsetY", 0.65D ), 0.65D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOffsetX", "", "", 0, 1, prefsMap.getOrDefault( "xpDropOffsetX", 0.5D ), 0.5D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOffsetY", "", "", 0, 1, prefsMap.getOrDefault( "xpDropOffsetY", 0D ), 0D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("skillListOffsetX", "", "", 0, 1, prefsMap.getOrDefault( "skillListOffsetX", 0D ), 0D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("skillListOffsetY", "", "", 0, 1, prefsMap.getOrDefault( "skillListOffsetY", 0D ), 0D, true, true, false, false ) );
				prefsEntries.add( new PrefsEntry("xpDropSpawnDistance", "", "", 0, 1000, prefsMap.getOrDefault( "xpDropSpawnDistance", 50D ), 50D, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOpacityPerTime", "", "", 0, 255, prefsMap.getOrDefault( "xpDropOpacityPerTime", 5D ), 5D, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropMaxOpacity", "", "", 0, 255, prefsMap.getOrDefault( "xpDropMaxOpacity", 200D ), 200D, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropDecayAge", "", "", 0, 5000, prefsMap.getOrDefault( "xpDropDecayAge", 350D ), 350D, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("minXpGrow", "", "", 0.01, 100, prefsMap.getOrDefault( "minXpGrow", 1D ), 1D, true, true, false, false ) );

                prefsEntries.add( new PrefsEntry("showSkillsListAtCorner", "", "", 0, 1, prefsMap.getOrDefault( "showSkillsListAtCorner", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("showXpDrops", "", "", 0, 1, prefsMap.getOrDefault( "showXpDrops", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("stackXpDrops", "", "", 0, 1, prefsMap.getOrDefault( "stackXpDrops", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpDropsAttachedToBar", "", "", 0, 1, prefsMap.getOrDefault( "xpDropsAttachedToBar", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpBarAlwaysOn", "", "", 0, 1, prefsMap.getOrDefault( "xpBarAlwaysOn", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpLeftDisplayAlwaysOn", "", "", 0, 1, prefsMap.getOrDefault( "xpLeftDisplayAlwaysOn", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("lvlUpScreenshot", "", "", 0, 1, prefsMap.getOrDefault( "lvlUpScreenshot", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpDropsShowXpBar", "", "", 0, 1, prefsMap.getOrDefault( "xpDropsShowXpBar", 1D ), 1D, false, true, false, true ) );
                break;
        }

        i = 0;

        for( PrefsEntry prefEntry : prefsEntries )
        {
//            if( !prefEntry.isSwitch )
//            {
//                prefEntry.textField.setResponder( text ->
//                {
//                    try
//                    {
//                        value = Double.parseDouble( text );
//                        if( value > prefEntry.slider.maxValue )
//                            value = prefEntry.slider.maxValue;
//                        if( value < prefEntry.slider.minValue )
//                            value = prefEntry.slider.minValue;
//                        prefEntry.slider.setValue( value );
//                        prefEntry.slider.updateSlider();
//                    }
//                    catch( Exception e )
//                    {
////                        System.out.println( "wrong value" );
//                    }
//                });
//                prefEntry.textField.setValidator( text -> text.matches( "^[0-9]{0,3}[.]?[0-9]*$" ) && text.replace( ".", "" ).length() < 5 );
//            }
            //COUT
            prefEntry.slider.setResponder( slider ->
            {
                slider.precision = 4;
                prefsMap.put( slider.preference, slider.getValue() );
                if( prefEntry.removeIfMax && slider.getValue() == slider.maxValue )
                    prefsMap.remove( slider.preference );
                XPOverlayGUI.doInit();
                PlayerTickHandler.syncPrefs = true;
            });
            prefEntry.slider.updateSlider();
            prefEntry.setX( x + 24 );
            prefEntry.setY( y + 24 + 18 * i++ );
        }
        scrollPanel = new PrefsScrollPanel( Minecraft.getMinecraft(), boxWidth - 40, boxHeight - 21, scrollY, scrollX,  prefsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        addButton(exitButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );

        if( fontRenderer.getStringWidth( title.getUnformattedText() ) > 220 )
            drawCenteredString( fontRenderer, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( fontRenderer, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.drawScreen( mouseX, mouseY, partialTicks );

        GlStateManager.pushAttrib();
        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseX >= prefEntry.button.x && mouseX < prefEntry.button.x + prefEntry.button.getButtonWidth() && mouseY >= prefEntry.button.y && mouseY < prefEntry.button.y + prefEntry.button.getHeight() )
                drawHoveringText( prefEntry.isSwitch ? ( prefEntry.defaultVal == 1 ? "ON" : "OFF" ) : prefEntry.removeIfMax && prefEntry.defaultVal == prefEntry.slider.maxValue ? "MAX" : DP.dpSoft( prefEntry.defaultVal ), mouseX, mouseY );
        }
        GlStateManager.popAttrib();
        GlStateManager.enableBlend();

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawBackground(int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );

        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

//    @Override
//    public mouseScrolled(int mouseX, int mouseY, double scroll)
//    {
//        if( prefsEntries.size() >= 9 )
//            scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
//        super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        scrollPanel.scroll( Mouse.getEventDWheel() );
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if( button == 1 )
        {
            exitButton.onPress();
            return;
        }

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
            {
                prefEntry.mouseClicked( mouseX, mouseY );
//                if ( !prefEntry.isSwitch && prefEntry.textField.mouseClicked( mouseX, mouseY, button ) )
//                    this.setFocused( prefEntry.textField );
            }
        }

        scrollPanel.mouseClicked( mouseX, mouseY, button );
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
                prefEntry.mouseReleased( mouseX, mouseY );
        }

        scrollPanel.mouseReleased( mouseX, mouseY, button );
        super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
                prefEntry.mouseClickMove( mouseX, mouseY );
        }

        scrollPanel.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }

    public static TextComponentTranslation getTransComp( String translationKey, Double... args )
    {
        return new TextComponentTranslation( translationKey, args );
    }
}