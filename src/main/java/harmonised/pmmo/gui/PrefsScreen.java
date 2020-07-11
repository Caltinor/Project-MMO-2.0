package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerTickHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class PrefsScreen extends Screen
{
    public static CompoundNBT prefsTag;
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private int i;
    private double value;
    private static Button exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX;
    private PrefsScrollPanel scrollPanel;
    private final JType jType;
    private ArrayList<PrefsEntry> prefsEntries;
    private ITextComponent title;
    private PlayerEntity player = Minecraft.getInstance().player;

    public PrefsScreen( ITextComponent titleIn, JType jType )
    {
        super(titleIn);
        this.title = titleIn;
        this.jType = jType;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        prefsTag = XP.getPreferencesTag( player );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new PrefsChoiceScreen( new TranslationTextComponent( "pmmo.stats" ) ) );
        });
        prefsEntries = new ArrayList<>();

        switch( jType )
        {
            case SETTINGS:

                value = Math.min( Skill.BUILDING.getLevel( player ) / Config.getConfig( "levelsPerOneReach" ), Config.getConfig( "maxExtraReachBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraReachBoost", "", "", 0, value, prefsTag.contains( "maxExtraReachBoost" ) ? prefsTag.getDouble( "maxExtraReachBoost" ) : value, value, true, true, true, false ) );

                value = Math.min( Skill.ENDURANCE.getLevel( player ) / Config.getConfig( "levelsPerHeart" ), Config.getConfig( "maxExtraHeartBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraHeartBoost", "", "", 0, value, prefsTag.contains( "maxExtraHeartBoost" ) ? prefsTag.getDouble( "maxExtraHeartBoost" ) : value, value, false, true, true, false ) );

                value = Math.min( Skill.COMBAT.getLevel( player ) / Config.getConfig( "levelsPerDamage" ), Config.getConfig( "maxExtraDamageBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamageBoost", "", "", 0, value, prefsTag.contains( "maxExtraDamageBoost" ) ? prefsTag.getDouble( "maxExtraDamageBoost" ) : value, value, false, true, true, false ) );

                value = Math.min( Skill.AGILITY.getLevel( player ) * Config.getConfig( "speedBoostPerLevel" ), Config.getConfig( "maxSpeedBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSpeedBoost", "", "", 0, value, prefsTag.contains( "maxSpeedBoost" ) ? prefsTag.getDouble( "maxSpeedBoost" ) : value, value, true, true, true, false ) );

                value = Math.min( Skill.AGILITY.getLevel( player ) * Config.getConfig( "levelsPerSprintJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSprintJumpBoost", "", "", 0, value, prefsTag.contains( "maxSprintJumpBoost" ) ? prefsTag.getDouble( "maxSprintJumpBoost" ) : value, value, true, true, true, false ) );

                value = Math.min( Skill.AGILITY.getLevel( player ) * Config.getConfig( "levelsPerCrouchJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxCrouchJumpBoost", "", "", 0, value, prefsTag.contains( "maxCrouchJumpBoost" ) ? prefsTag.getDouble( "maxCrouchJumpBoost" ) : value, value, true, true, true, false ) );

                prefsEntries.add( new PrefsEntry("wipeAllSkillsUponDeathPermanently", "", "", 0, 1, prefsTag.contains( "wipeAllSkillsUponDeathPermanently" ) ? prefsTag.getDouble( "wipeAllSkillsUponDeathPermanently" ) : 0, 0, false, true, false, true ) );
                break;

            case GUI_SETTINGS:
                prefsEntries.add( new PrefsEntry("barOffsetX", "", "", 0, 1, prefsTag.contains( "barOffsetX" ) ? prefsTag.getDouble( "barOffsetX" ) : 0.5, 0.5, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("barOffsetY", "", "", 0, 1, prefsTag.contains( "barOffsetY" ) ? prefsTag.getDouble( "barOffsetY " ) : 0, 0, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("veinBarOffsetX", "", "", 0, 1, prefsTag.contains( "veinBarOffsetX" ) ? prefsTag.getDouble( "veinBarOffsetX" ) : 0.5, 0.5, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("veinBarOffsetY", "", "", 0, 1, prefsTag.contains( "veinBarOffsetY" ) ? prefsTag.getDouble( "veinBarOffsetY" ) : 0.65, 0.65, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOffsetX", "", "", 0, 1, prefsTag.contains( "xpDropOffsetX" ) ? prefsTag.getDouble( "xpDropOffsetX" ) : 0.5, 0.5, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOffsetY", "", "", 0, 1, prefsTag.contains( "xpDropOffsetY" ) ? prefsTag.getDouble( "xpDropOffsetY" ) : 0, 0, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropSpawnDistance", "", "", 0, 1000, prefsTag.contains( "xpDropSpawnDistance" ) ? prefsTag.getDouble( "xpDropSpawnDistance" ) : 50, 50, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropOpacityPerTime", "", "", 0, 255, prefsTag.contains( "xpDropOpacityPerTime" ) ? prefsTag.getDouble( "xpDropOpacityPerTime" ) : 5, 5, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropMaxOpacity", "", "", 0, 255, prefsTag.contains( "xpDropMaxOpacity" ) ? prefsTag.getDouble( "xpDropMaxOpacity" ) : 200, 200, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("xpDropDecayAge", "", "", 0, 5000, prefsTag.contains( "xpDropDecayAge" ) ? prefsTag.getDouble( "xpDropDecayAge" ) : 350, 350, false, true, false, false ) );
                prefsEntries.add( new PrefsEntry("minXpGrow", "", "", 0.01, 100, prefsTag.contains( "minXpGrow" ) ? prefsTag.getDouble( "minXpGrow" ) : 1, 1, true, true, false, false ) );

                prefsEntries.add( new PrefsEntry("showXpDrops", "", "", 0, 1, prefsTag.contains( "showXpDrops" ) ? prefsTag.getDouble( "showXpDrops" ) : 1, 1, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("stackXpDrops", "", "", 0, 1, prefsTag.contains( "stackXpDrops" ) ? prefsTag.getDouble( "stackXpDrops" ) : 1, 1, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpDropsAttachedToBar", "", "", 0, 1, prefsTag.contains( "xpDropsAttachedToBar" ) ? prefsTag.getDouble( "xpDropsAttachedToBar" ) : 1, 1, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpBarAlwaysOn", "", "", 0, 1, prefsTag.contains( "xpBarAlwaysOn" ) ? prefsTag.getDouble( "xpBarAlwaysOn" ) : 0, 0, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpLeftDisplayAlwaysOn", "", "", 0, 1, prefsTag.contains( "xpLeftDisplayAlwaysOn" ) ? prefsTag.getDouble( "xpLeftDisplayAlwaysOn" ) : 0, 0, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("lvlUpScreenshot", "", "", 0, 1, prefsTag.contains( "lvlUpScreenshot" ) ? prefsTag.getDouble( "lvlUpScreenshot" ) : 0, 0, false, true, false, true ) );
                break;
        }

        i = 0;

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( !prefEntry.isSwitch )
            {
                prefEntry.textField.setResponder( text ->
                {
                    try
                    {
                        value = Double.parseDouble( text );
                        if( value > prefEntry.slider.maxValue )
                            value = prefEntry.slider.maxValue;
                        if( value < prefEntry.slider.minValue )
                            value = prefEntry.slider.minValue;
                        prefEntry.slider.setValue( value );
                        prefEntry.slider.updateSlider();
                    }
                    catch( Exception e )
                    {
//                        System.out.println( "wrong value" );
                    }
                });
                prefEntry.textField.setValidator( text -> text.matches( "^[0-9]{0,3}[.]?[0-9]*$" ) && text.replace( ".", "" ).length() < 5 );
            }
            prefEntry.slider.setResponder( slider ->
            {
                slider.precision = 4;
                prefsTag.putDouble( slider.preference, slider.getValue() );
                if( prefEntry.removeIfMax && slider.getValue() == slider.maxValue )
                    prefsTag.remove( slider.preference );
                XPOverlayGUI.doInit();
                PlayerTickHandler.syncPrefs = true;
            });
            prefEntry.slider.updateSlider();
            prefEntry.setX( x + 24 );
            prefEntry.setY( y + 24 + 18 * i++ );
        }

//        new Slider()
//        new TextFieldWidget()

        scrollPanel = new PrefsScrollPanel( Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX,  prefsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render( MatrixStack stack,  int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( MatrixStack stack,  1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack,  mouseX, mouseY, partialTicks );

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseX >= prefEntry.button.x && mouseX < prefEntry.button.x + prefEntry.button.getWidth() && mouseY >= prefEntry.button.y && mouseY < prefEntry.button.y + prefEntry.button.getHeight() )
                renderTooltip( prefEntry.isSwitch ? ( prefEntry.defaultVal == 1 ? "ON" : "OFF" ) : prefEntry.removeIfMax && prefEntry.defaultVal == prefEntry.slider.maxValue ? "MAX" : DP.dpSoft( prefEntry.defaultVal ), mouseX, mouseY );
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if( prefsEntries.size() >= 9 )
            scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return true;
        }

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
            {
                prefEntry.mouseClicked( mouseX, mouseY, button );
                if ( !prefEntry.isSwitch && prefEntry.textField.mouseClicked( mouseX, mouseY, button ) )
                    this.setFocused( prefEntry.textField );
            }
        }

        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
                prefEntry.mouseReleased( mouseX, mouseY, button );
        }

        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double deltaX, double deltaY )
    {
        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() )
                prefEntry.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        }

        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

    public static TranslationTextComponent getTransComp( String translationKey, Object... args )
    {
        return new TranslationTextComponent( translationKey, args );
    }

}