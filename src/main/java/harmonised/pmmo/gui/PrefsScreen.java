package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerTickHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    public static Map<String, Double> prefsMap;
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private int i;
    private double value;
    private static Button exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    MainWindow sr = minecraft.getMainWindow();
    FontRenderer font = minecraft.fontRenderer;
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
        prefsMap = Config.getPreferencesMap( player );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", JType.SKILLS, (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new PrefsChoiceScreen( new TranslationTextComponent( "pmmo.stats" ) ) );
        });
        prefsEntries = new ArrayList<>();

        switch( jType )
        {
            case SETTINGS:
                value = Math.min( Skill.getLevel( Skill.BUILDING.toString(), player ) / Config.getConfig( "levelsPerOneReach" ), Config.getConfig( "maxExtraReachBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraReachBoost", "", "", 0, value, prefsMap.getOrDefault( "maxExtraReachBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.ENDURANCE.toString(), player ) / Config.getConfig( "levelsPerHeart" ), Config.getConfig( "maxExtraHeartBoost" ) );
                prefsEntries.add( new PrefsEntry("maxExtraHeartBoost", "", "", 0, value, prefsMap.getOrDefault( "maxExtraHeartBoost", value ), value, false, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.COMBAT.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelMelee" ), Config.getConfig( "maxExtraDamagePercentageBoostMelee" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamagePercentageBoostMelee", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamagePercentageBoostMelee", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.ARCHERY.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelArchery" ), Config.getConfig( "maxExtraDamagePercentageBoostArchery" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamagePercentageBoostArchery", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamagePercentageBoostArchery", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.MAGIC.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelMagic" ), Config.getConfig( "maxExtraDamagePercentageBoostMagic" ) );
                prefsEntries.add( new PrefsEntry("maxExtraDamagePercentageBoostMagic", "", "", 0, value, prefsMap.getOrDefault( "maxExtraDamagePercentageBoostMagic", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "speedBoostPerLevel" ), Config.getConfig( "maxSpeedBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSpeedBoost", "", "", 0, value, prefsMap.getOrDefault( "maxSpeedBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "levelsPerSprintJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxSprintJumpBoost", "", "", 0, value, prefsMap.getOrDefault( "maxSprintJumpBoost", value ), value, true, true, true, false ) );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "levelsPerCrouchJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                prefsEntries.add( new PrefsEntry("maxCrouchJumpBoost", "", "", 0, value, prefsMap.getOrDefault( "maxCrouchJumpBoost", value ), value, true, true, true, false ) );
                prefsEntries.add( new PrefsEntry("wipeAllSkillsUponDeathPermanently", "", "", 0, 1, prefsMap.getOrDefault( "wipeAllSkillsUponDeathPermanently", 0D ), 0, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("spawnFireworksCausedByMe", "", "", 0, 1, prefsMap.getOrDefault( "spawnFireworksCausedByMe", 1D ), 1, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("spawnFireworksCausedByOthers", "", "", 0, 1, prefsMap.getOrDefault( "spawnFireworksCausedByOthers", 1D ), 1, false, true, false, true ) );
                if( XP.isNightvisionUnlocked( player ) )
                    prefsEntries.add( new PrefsEntry("underwaterNightVision", "", "", 0, 1, prefsMap.getOrDefault( "underwaterNightVision", 1D ), 1, false, true, false, true ) );
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
                prefsEntries.add( new PrefsEntry("worldXpDropsSizeMultiplier", "", "", 0.01, 100, prefsMap.getOrDefault( "worldXpDropsSizeMultiplier", 1D ), 1D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("worldXpDropsDecaySpeedMultiplier", "", "", 0.01, 100, prefsMap.getOrDefault( "worldXpDropsDecaySpeedMultiplier", 1D ), 1D, true, true, false, false ) );
                prefsEntries.add( new PrefsEntry("worldXpDropsRotationCap", "", "", 0.01, 100, prefsMap.getOrDefault( "worldXpDropsRotationCap", 25D ), 25D, true, true, false, false ) );

                prefsEntries.add( new PrefsEntry("showSkillsListAtCorner", "", "", 0, 1, prefsMap.getOrDefault( "showSkillsListAtCorner", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("showXpDrops", "", "", 0, 1, prefsMap.getOrDefault( "showXpDrops", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("stackXpDrops", "", "", 0, 1, prefsMap.getOrDefault( "stackXpDrops", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpDropsAttachedToBar", "", "", 0, 1, prefsMap.getOrDefault( "xpDropsAttachedToBar", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpBarAlwaysOn", "", "", 0, 1, prefsMap.getOrDefault( "xpBarAlwaysOn", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpLeftDisplayAlwaysOn", "", "", 0, 1, prefsMap.getOrDefault( "xpLeftDisplayAlwaysOn", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("lvlUpScreenshot", "", "", 0, 1, prefsMap.getOrDefault( "lvlUpScreenshot", 0D ), 0D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("xpDropsShowXpBar", "", "", 0, 1, prefsMap.getOrDefault( "xpDropsShowXpBar", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("showLevelUpUnlocks", "", "", 0, 1, prefsMap.getOrDefault( "showLevelUpUnlocks", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("worldXpDropsEnabled", "", "", 0, 1, prefsMap.getOrDefault( "worldXpDropsEnabled", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("worldXpDropsShowSkill", "", "", 0, 1, prefsMap.getOrDefault( "worldXpDropsShowSkill", 1D ), 1D, false, true, false, true ) );
                prefsEntries.add( new PrefsEntry("showOthersWorldXpDrops", "", "", 0, 1, prefsMap.getOrDefault( "showOthersWorldXpDrops", 0D ), 0D, false, true, false, true ) );
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

        scrollPanel = new PrefsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX,  prefsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack,  mouseX, mouseY, partialTicks );

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseX >= prefEntry.button.x && mouseX < prefEntry.button.x + prefEntry.button.getWidth() && mouseY >= prefEntry.button.y && mouseY < prefEntry.button.y + prefEntry.button.getHeightRealms() )
                renderTooltip( stack, new TranslationTextComponent( prefEntry.isSwitch ? ( prefEntry.defaultVal == 1 ? "ON" : "OFF" ) : prefEntry.removeIfMax && prefEntry.defaultVal == prefEntry.slider.maxValue ? "MAX" : DP.dpSoft( prefEntry.defaultVal ) ), mouseX, mouseY );
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if ( this.minecraft != null && !jType.equals( JType.GUI_SETTINGS ) )
        {
            boxHeight = 256;
            boxWidth = 256;
            Minecraft.getInstance().getTextureManager().bindTexture( box );
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
            this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
        }
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
                    this.setFocusedDefault( prefEntry.textField );
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

    public static TranslationTextComponent getTransComp( String translationKey, Double... args )
    {
        return new TranslationTextComponent( translationKey, args );
    }
}