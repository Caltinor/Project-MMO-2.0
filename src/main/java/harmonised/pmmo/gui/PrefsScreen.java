package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.ConfigHelper;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerTickHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class PrefsScreen extends Screen
{
    public static Map<String, Double> prefsMap;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private int i;
    private double value;
    private static Button exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    Window sr = minecraft.getWindow();
    Font font = minecraft.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX;
    private PrefsScrollPanel scrollPanel;
    private final JType jType;
    private ArrayList<PrefsEntry> prefsEntries;
    private Component title;
    private Player player = Minecraft.getInstance().player;

    public PrefsScreen( Component titleIn, JType jType )
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

        x = (sr.getGuiScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getGuiScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", JType.SKILLS, (button) ->
        {
            Minecraft.getInstance().setScreen( new PrefsChoiceScreen( new TranslatableComponent( "pmmo.stats" ) ) );
        });
        prefsEntries = new ArrayList<>();

        switch( jType )
        {
            case SETTINGS:
                value = Math.min( Skill.getLevel( Skill.BUILDING.toString(), player ) / Config.getConfig( "levelsPerOneReach" ), Config.getConfig( "maxExtraReachBoost" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxExtraReachBoost, "maxExtraReachBoost", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.ENDURANCE.toString(), player ) / Config.getConfig( "levelsPerHeart" ), Config.getConfig( "maxExtraHeartBoost" ) );
                addPrefsButtonInteger( Config.forgeConfig.maxExtraHeartBoost, "maxExtraHeartBoost", 0, value, false, true, true );
                value = Math.min( Skill.getLevel( Skill.COMBAT.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelMelee" ), Config.getConfig( "maxExtraDamagePercentageBoostMelee" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxExtraDamagePercentageBoostMelee, "maxExtraDamagePercentageBoostMelee", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.ARCHERY.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelArchery" ), Config.getConfig( "maxExtraDamagePercentageBoostArchery" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxExtraDamagePercentageBoostArchery, "maxExtraDamagePercentageBoostArchery", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.MAGIC.toString(), player ) * Config.getConfig( "damageBonusPercentPerLevelMagic" ), Config.getConfig( "maxExtraDamagePercentageBoostMagic" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxExtraDamagePercentageBoostMagic, "maxExtraDamagePercentageBoostMagic", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "speedBoostPerLevel" ), Config.getConfig( "maxSpeedBoost" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxSpeedBoost, "maxSpeedBoost", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "levelsPerSprintJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxJumpBoost, "maxSprintJumpBoost", 0, value, true, true, true );
                value = Math.min( Skill.getLevel( Skill.AGILITY.toString(), player ) * Config.getConfig( "levelsPerCrouchJumpBoost" ), Config.getConfig( "maxJumpBoost" ) );
                addPrefsButtonDouble( Config.forgeConfig.maxJumpBoost, "maxCrouchJumpBoost", 0, value, true, true, true );
                addPrefsButtonBool( Config.forgeConfig.wipeAllSkillsUponDeathPermanently, "wipeAllSkillsUponDeathPermanently", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.spawnFireworksCausedByMe, "spawnFireworksCausedByMe", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.spawnFireworksCausedByOthers, "spawnFireworksCausedByOthers", false, true, false );
                if( XP.isNightvisionUnlocked( player ) )
                    addPrefsButtonBool( Config.forgeConfig.underwaterNightVision, "underwaterNightVision", false, true, false );
                break;

            case GUI_SETTINGS:
                addPrefsButtonDouble( Config.forgeConfig.barOffsetX, "barOffsetX", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.barOffsetY, "barOffsetY", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.veinBarOffsetX, "veinBarOffsetX", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.veinBarOffsetY, "veinBarOffsetY", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropOffsetX, "xpDropOffsetX", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropOffsetY, "xpDropOffsetY", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.skillListOffsetX, "skillListOffsetX", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.skillListOffsetY, "skillListOffsetY", 0, 1, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropSpawnDistance, "xpDropSpawnDistance", 0, 1000, false, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropOpacityPerTime, "xpDropOpacityPerTime", 0, 255, false, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropMaxOpacity, "xpDropMaxOpacity", 0, 255, false, true, false );
                addPrefsButtonDouble( Config.forgeConfig.xpDropDecayAge, "xpDropDecayAge", 0, 5000, false, true, false );
                addPrefsButtonDouble( Config.forgeConfig.minXpGrow, "minXpGrow", 0.01, 100, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.worldXpDropsSizeMultiplier, "worldXpDropsSizeMultiplier", 0.01, 100, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.worldXpDropsDecaySpeedMultiplier, "worldXpDropsDecaySpeedMultiplier", 0.01, 100, true, true, false );
                addPrefsButtonDouble( Config.forgeConfig.worldXpDropsRotationCap, "worldXpDropsRotationCap", 0.01, 100, true, true, false );
                addPrefsButtonInteger( Config.forgeConfig.maxVeinDisplay, "maxVeinDisplay", 0, 10000, false, true, false );

                addPrefsButtonBool( Config.forgeConfig.showSkillsListAtCorner, "showSkillsListAtCorner", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.showXpDrops, "showXpDrops", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.stackXpDrops, "stackXpDrops", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.xpDropsAttachedToBar, "xpDropsAttachedToBar", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.xpBarAlwaysOn, "xpBarAlwaysOn", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.xpLeftDisplayAlwaysOn, "xpLeftDisplayAlwaysOn", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.lvlUpScreenshot, "lvlUpScreenshot", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.xpDropsShowXpBar, "xpDropsShowXpBar", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.showLevelUpUnlocks, "showLevelUpUnlocks", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.worldXpDropsEnabled, "worldXpDropsEnabled", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.worldXpDropsShowSkill, "worldXpDropsShowSkill", false, true, false );
                addPrefsButtonBool( Config.forgeConfig.showOthersWorldXpDrops, "showOthersWorldXpDrops", false, true, false );
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
                prefEntry.textField.setFilter( text -> text.matches( "^[0-9]{0,3}[.]?[0-9]*$" ) && text.replace( ".", "" ).length() < 5 );
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

        scrollPanel = new PrefsScrollPanel( new PoseStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX,  prefsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    private void addPrefsButtonBool( ConfigHelper.ConfigValueListener<Boolean> config, String key, boolean showDec, boolean showStr, boolean removeIfMax )
    {
        addPrefsButtonValue( key, config.get() ? 1D : 0D, 0D, 1D, showDec, showStr, removeIfMax, true );
    }

    private void addPrefsButtonInteger( ConfigHelper.ConfigValueListener<Integer> config, String key, double min, double max, boolean showDec, boolean showStr, boolean removeIfMax )
    {
        addPrefsButtonValue( key, config.get(), min, max, showDec, showStr, removeIfMax, false );
    }

    private void addPrefsButtonDouble( ConfigHelper.ConfigValueListener<Double> config, String key, double min, double max, boolean showDec, boolean showStr, boolean removeIfMax )
    {
        addPrefsButtonValue( key, config.get(), min, max, showDec, showStr, removeIfMax, false );
    }

    private void addPrefsButtonValue( String key, double value, double min, double max, boolean showDec, boolean showStr, boolean removeIfMax, boolean isSwitch )
    {
        prefsEntries.add( new PrefsEntry( key, "", "", min, max, prefsMap.getOrDefault( key,value ), value, showDec, showStr, removeIfMax, isSwitch ) );
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );

        if( font.width( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getString(), sr.getGuiScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getString(), sr.getGuiScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack,  mouseX, mouseY, partialTicks );

        for( PrefsEntry prefEntry : prefsEntries )
        {
            if( mouseX >= prefEntry.button.x && mouseX < prefEntry.button.x + prefEntry.button.getWidth() && mouseY >= prefEntry.button.y && mouseY < prefEntry.button.y + prefEntry.button.getHeight() )
                renderTooltip( stack, new TranslatableComponent( prefEntry.isSwitch ? ( prefEntry.defaultVal == 1 ? "ON" : "OFF" ) : prefEntry.removeIfMax && prefEntry.defaultVal == prefEntry.slider.maxValue ? "MAX" : DP.dpSoft( prefEntry.defaultVal ) ), mouseX, mouseY );
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( PoseStack stack, int p_renderBackground_1_)
    {
        if ( this.minecraft != null && !jType.equals( JType.GUI_SETTINGS ) )
        {
            boxHeight = 256;
            boxWidth = 256;
            Minecraft.getInstance().getTextureManager().bind( box );
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
                {
                    this.setInitialFocus( prefEntry.textField );
                    prefEntry.textField.setFocus( true );
                }
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

    public static TranslatableComponent getTransComp( String translationKey, Double... args )
    {
        return new TranslatableComponent( translationKey, args );
    }
}