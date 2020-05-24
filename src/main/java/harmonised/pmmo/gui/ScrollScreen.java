package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ScrollScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = new ResourceLocation( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY;
    private Button exitButton;
    private MyScrollPanel scrollPanel;
    private PlayerEntity player;
    private String type;
    private ArrayList<ListButton> tempList;
    private ArrayList<ListButton> listButtons = new ArrayList<>();

    public ScrollScreen( ITextComponent titleIn, String type, PlayerEntity player )
    {
        super(titleIn);
        this.player = player;
        this.type = type;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        ArrayList<String> keyWords = new ArrayList<>();
        keyWords.add( "helmet" );
        keyWords.add( "chestplate" );
        keyWords.add( "leggings" );
        keyWords.add( "boots" );
        keyWords.add( "pickaxe" );
        keyWords.add( "axe" );
        keyWords.add( "shovel" );
        keyWords.add( "hoe" );
        keyWords.add( "sword" );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;

        exitButton = new TileButton( x + boxWidth - 24, y - 8, 0, 7, I18n.format("" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new SkillsScreen( new TranslationTextComponent( "pmmo.skills" ) ) );
        });

        Map<String, Map<String, Object>> reqMap = XP.getFullReqMap( type );

        tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        if( type.equals( "biome" ) )
        {
            Map<String, Map<String, Object>> bonusMap = JsonConfig.data.get( "biomeXpBonus" );
            Map<String, Map<String, Object>> scaleMap = JsonConfig.data.get( "biomeMobMultiplier" );
            List<String> biomesToAdd = new ArrayList<>();

            if( reqMap != null )
            {
                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( !biomesToAdd.contains( entry.getKey() ) )
                        biomesToAdd.add( entry.getKey() );
                }
            }

            if( bonusMap != null )
            {
                for( Map.Entry<String, Map<String, Object>> entry : bonusMap.entrySet() )
                {
                    if( !biomesToAdd.contains( entry.getKey() ) )
                        biomesToAdd.add( entry.getKey() );
                }
            }

            if( scaleMap != null )
            {
                for( Map.Entry<String, Map<String, Object>> entry : scaleMap.entrySet() )
                {
                    if( !biomesToAdd.contains( entry.getKey() ) )
                        biomesToAdd.add( entry.getKey() );
                }
            }

            biomesToAdd.sort( Comparator.comparingInt( b -> getBiomeReqInt( b, "biome" ) ) );

            for( String regKey : biomesToAdd )
            {
                if ( ForgeRegistries.BIOMES.getValue( new ResourceLocation( regKey ) ) != null )
                {
                    tempList.add( new ListButton( 0, 0, 1, 9, regKey, type, "", button ->
                    {
                        System.out.println( "clicc" );
                    }));
                }
            }
        }
        else
        {
            for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
            {
                if( XP.getItem( entry.getKey() ) != Items.AIR )
                {
                    tempList.add( new ListButton( 0, 0, 1, 0, entry.getKey(), type, "", button ->
                    {
                        System.out.println( "clicc" );
                    }));
                }
            }
        }

        for( String keyWord : keyWords )
        {
            for( ListButton button : tempList )
            {
                if( button.regKey.contains( keyWord ) )
                {
                    if( !listButtons.contains(button) )
                        listButtons.add( button );
                }
            }
        }

        for( ListButton button : tempList )
        {
            if( !listButtons.contains( button ) )
                listButtons.add( button );
        }

        for( ListButton button : listButtons )
        {
            List<String> skillText = new ArrayList<>();
            List<String> scaleText = new ArrayList<>();
            List<String> effectText = new ArrayList<>();

            if( type.equals( "biome" ) )
            {
                button.tooltipText.add( button.title );

                if( reqMap.containsKey( button.regKey ) )
                    addLevelsToButton( button, reqMap.get( button.regKey ), player, false );

                Map<String, Object> biomeBonusMap = JsonConfig.data.get( "biomeXpBonus" ).get( button.regKey );
                Map<String, Object> biomeMobMultiplierMap = JsonConfig.data.get( "biomeMobMultiplier" ).get( button.regKey );
                Map<String, Object> biomeEffectsMap = JsonConfig.data.get( "biomeEffect" ).get( button.regKey );

                if( biomeBonusMap != null )
                {
                    for( Map.Entry<String, Object> entry : biomeBonusMap.entrySet() )
                    {
                        if( (double) entry.getValue() > 0 )
                            skillText.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "+" + entry.getValue() + "%" ).setStyle( XP.skillStyle.get( Skill.getSkill( entry.getKey() ) ) ).getFormattedText() );
                        if( (double) entry.getValue() < 0 )
                            skillText.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), entry.getValue() + "%" ).setStyle( XP.skillStyle.get( Skill.getSkill( entry.getKey() ) ) ).getFormattedText() );
                    }
                }

                if( biomeMobMultiplierMap != null )
                {
                    for( Map.Entry<String, Object> entry : biomeMobMultiplierMap.entrySet() )
                    {
                        Style styleColor = new Style();

                        if( (double) entry.getValue() > 1 )
                            styleColor = XP.textStyle.get( "red" );
                        else if( (double) entry.getValue() < 1 )
                            styleColor = XP.textStyle.get( "green" );

                        switch( entry.getKey() )
                        {
                            case "damageBonus":
                                scaleText.add( " " + new TranslationTextComponent( "pmmo.enemyScaleDamage", DP.dp( (double) entry.getValue() * 100 ) ).setStyle( styleColor ).getFormattedText() );
                                break;

                            case "hpBonus":
                                scaleText.add( " " + new TranslationTextComponent( "pmmo.enemyScaleHp", DP.dp( (double) entry.getValue() * 100 ) ).setStyle( styleColor ).getFormattedText() );
                                break;

                            case "speedBonus":
                                scaleText.add( " " + new TranslationTextComponent( "pmmo.enemyScaleSpeed", DP.dp( (double) entry.getValue() * 100 ) ).setStyle( styleColor ).getFormattedText() );
                                break;
                        }
                    }
                }

                if( biomeEffectsMap != null )
                {
                    for( Map.Entry<String, Object> entry : biomeEffectsMap.entrySet() )
                    {
                        if( ForgeRegistries.POTIONS.containsKey( new ResourceLocation( entry.getKey() ) ) )
                        {
                            Effect effect = ForgeRegistries.POTIONS.getValue( new ResourceLocation( entry.getKey() ) );
                            if( effect != null )
                                effectText.add( " " + new TranslationTextComponent( effect.getDisplayName().getFormattedText() + " " + (int) ( (double) entry.getValue() + 1) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        }
                    }
                }

//                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.itemStack.getItem().getRegistryName().toString(), type) ) );
            }
            else if( type.equals( "ore" ) || type.equals( "log" ) || type.equals( "plant" ) )
            {
                Map<String, Object> breakMap = JsonConfig.data.get( "breakReq" ).get( button.regKey );
                Map<String, Double> infoMap = XP.getReqMap( button.regKey, type );
                List<String> infoText = new ArrayList<>();
                String transKey = "pmmo." + type + "ExtraDrop";
                double extraDroppedPerLevel = infoMap.get( "extraChance" );
                double extraDropped = XP.getExtraChance( player, button.regKey, type );

                if( extraDroppedPerLevel <= 0 )
                    infoText.add( new TranslationTextComponent( "pmmo.extraDropPerLevel", DP.dp( extraDroppedPerLevel ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                else
                    infoText.add( new TranslationTextComponent( "pmmo.extraDropPerLevel", DP.dp( extraDroppedPerLevel ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                if( extraDropped <= 0 )
                    infoText.add( new TranslationTextComponent( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                else
                    infoText.add( new TranslationTextComponent( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                if( infoText.size() > 0 )
                    button.text.addAll( infoText );

                if( breakMap != null )
                {
                    if( XP.checkReq( player, button.regKey, "break" ) )
                        button.text.add( new TranslationTextComponent( "pmmo.break" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                    else
                        button.text.add( new TranslationTextComponent( "pmmo.break" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    addLevelsToButton( button, breakMap, player, false );
                }
            }
            else if( type.equals( "held" ) || type.equals( "worn" ) )
                addLevelsBonusToButton( button, reqMap.get( button.regKey ), player );
            else if( type.equals( "breedXp" ) || type.equals( "tameXp" ) || type.equals( "craftXp" ) )
                addLevelsToButton( button, reqMap.get( button.regKey ), player, true );
            else
                addLevelsToButton( button, reqMap.get( button.regKey ), player, false );

            if( skillText.size() > 0 )
            {
//                skillText.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );
                button.text.add( new TranslationTextComponent( "pmmo.xpModifiers" ).getFormattedText() );
                button.text.addAll( skillText );
            }

            if( scaleText.size() > 0 )
            {
                scaleText.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );
                button.text.add( new TranslationTextComponent( "pmmo.enemyScaling" ).getFormattedText() );
                button.text.addAll( scaleText );
            }

            if( effectText.size() > 0 )
            {
                effectText.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );
                button.text.add( new TranslationTextComponent( "pmmo.biomeEffects" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                button.text.addAll( effectText );
            }
        }

        scrollPanel = new MyScrollPanel( Minecraft.getInstance(), boxWidth - 42, boxHeight - 21, scrollY, scrollX, type, player, listButtons );

        children.add( scrollPanel );

        addButton( exitButton );
    }

    private static void addLevelsToButton( ListButton button, Map<String, Object> map, PlayerEntity player, boolean ignoreReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            String valueText;

            double value = (double) inEntry.getValue();

            if( value % 1 == 0 )
                valueText = Integer.toString( (int) Math.floor( value ) );
            else
                valueText = DP.dp( value );

            if( !ignoreReq && Skill.getSkill( inEntry.getKey() ).getLevel( player ) < value )
                levelsToAdd.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), valueText ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            else
                levelsToAdd.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), valueText ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static void addLevelsBonusToButton( ListButton button, Map<String, Object> map, PlayerEntity player )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            double value = (double) inEntry.getValue();

            if( value > 0 )
                levelsToAdd.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), "+" + value + "%" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
            else if( value < 0 )
                levelsToAdd.add( " " + new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static int getTextLevel( String comp )
    {
        String number = comp.replaceAll("\\D+","");

        if( number.length() > 0 && !Double.isNaN( Double.parseDouble( number ) ) )
            return (int) Double.parseDouble( number );
        else
            return 0;
    }

    private static int getBiomeReqInt( String regKey, String type )
    {
        Map<String, Double> map = XP.getReqMap( regKey, type );

        if( map == null )
            return 0;
        else
            return map.size();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        scrollPanel.render( mouseX, mouseY, partialTicks );

        int startI = (int) Math.floor( scrollPanel.getScroll() / 36D );
        if( startI < 0 )
            startI = 0;

        ListButton button;

        for( int i = startI; i < startI + 7; i++ )
        {
            if( listButtons.size() - 1 >= i )
            {
                button = listButtons.get( i );
                buttonX = mouseX - button.x;
                buttonY = mouseY - button.y;

//                renderTooltip( mouseX + " " + mouseY, mouseX, mouseY );

                if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
                {
                    if( button.elementTwo == 0 )
                        renderTooltip( button.itemStack, mouseX, mouseY );
                    else if( type.equals( "biome" ) )
                        renderTooltip( button.tooltipText, mouseX, mouseY );
                }
            }
        }

//        renderTooltip( mouseX + " " + mouseY, mouseX, mouseY );
//        drawCenteredString(Minecraft.getInstance().fontRenderer, player.getDisplayName().getString() + " " + type,x + boxWidth / 2, y + boxHeight / 2, 50000 );
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}