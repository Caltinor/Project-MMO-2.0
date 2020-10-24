package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.BlockBrokenHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ListScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static final Style greenColor = XP.textStyle.get( "green" );
    private static Button exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY, accumulativeHeight, buttonsSize, buttonsLoaded, futureHeight, minCount, maxCount;
    private ListScrollPanel scrollPanel;
    private final PlayerEntity player;
    private final JType jType;
    private final double baseXp = Config.getConfig( "baseXp" );
    private ArrayList<ListButton> listButtons = new ArrayList<>();
    private UUID uuid;
    private ITextComponent title;
    private String type;

    public ListScreen(UUID uuid, ITextComponent titleIn, String type, JType jType, PlayerEntity player )
    {
        super(titleIn);
        this.title = titleIn;
        this.player = player;
        this.jType = jType;
        this.uuid = uuid;
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
        buttonX = scrollX + 4;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", JType.NONE, (button) ->
        {
            switch( jType )
            {
                case STATS:
                    Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, getTransComp( "pmmo.potato" ) ) );
                    break;

                default:
                    Minecraft.getInstance().displayGuiScreen( new GlossaryScreen( uuid, getTransComp( "pmmo.skills" ), false ) );
                    break;
            }
        });

        Map<String, Map<String, Double>> reqMap = JsonConfig.data.get( jType );
        Map<String, Map<String, Map<String, Double>>> reqMap2 = JsonConfig.data2.get( jType );

        ArrayList<ListButton> tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        switch( jType )      //How it's made: Buttons!
        {
            case REQ_BIOME:
            {
                Map<String, Map<String, Double>> bonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME );
                Map<String, Map<String, Double>> scaleMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER );
                List<String> biomesToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( bonusMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : bonusMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( scaleMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : scaleMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                biomesToAdd.sort( Comparator.comparingInt( b -> getReqCount( b, JType.REQ_BIOME ) ) );

                for( String regKey : biomesToAdd )
                {
                    if ( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ) != null )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 8, regKey, jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
            }
            break;

            case DIMENSION:
            {
                Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                if( veinBlacklist == null )
                    break;

                if( veinBlacklist.containsKey( "all_dimensions" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "all_dimensions", jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                }


                if( veinBlacklist.containsKey( "minecraft:overworld" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:overworld", jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_nether" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:the_nether", jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_end" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:the_end", jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                }

                for( Map.Entry<String, Map<String, Double>> entry : veinBlacklist.entrySet() )
                {
                    if ( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( entry.getKey() ) ) != null )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 8, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
            }
            break;

            case REQ_KILL:
            {
                Map<String, Map<String, Double>> killXpMap = JsonConfig.data.get( JType.REQ_KILL );
                Map<String, Map<String, Double>> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP );
                ArrayList<String> mobsToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( killXpMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : killXpMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( rareDropMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : rareDropMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                for( String regKey : mobsToAdd )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, regKey, jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
            }
            break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
            }
            break;

            case FISH_ENCHANT_POOL:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENCHANTMENTS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 25, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
                break;
            }

            case STATS:
            {
                Set<Skill> skills = XP.getOfflineXpMap( uuid ).keySet();

                for( Skill skill : skills )
                {
                    listButtons.add( new ListButton( 0, 0, 3, 6, skill.toString(), jType, "", button -> ((ListButton) button).clickActionSkills() ) );
                }
            }
            break;

            case HISCORE:
            {
                Skill theSkill = Skill.getSkill( type );
                if( !theSkill.equals( Skill.INVALID_SKILL ) )
                {
                    System.out.println( "Yegurl" );
                }
            }
            break;

            case SALVAGE:
            case SALVAGE_FROM:
            case TREASURE:
            case TREASURE_FROM:
            {
                if( reqMap2 == null )
                    break;
                for( Map.Entry<String, Map<String, Map<String, Double>>> salvageFromItemEntry : reqMap2.entrySet() )
                {
                    tempList.add( new ListButton( 0, 0, 3, 0, salvageFromItemEntry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                }
            }
            break;

            default:
            {

                if( reqMap == null )
                    return;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( XP.getItem( entry.getKey() ) != Items.AIR )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary() ) );
                    }
                }
            }
            break;
        }

        for( String keyWord : keyWords )
        {
            for( ListButton button : tempList)
            {
                if( button.regKey.contains( keyWord ) )
                {
                    if( !listButtons.contains(button) )
                        listButtons.add( button );
                }
            }
        }

        for( ListButton button : tempList)
        {
            if( !listButtons.contains( button ) )
                listButtons.add( button );
        }

        for( ListButton button : listButtons )
        {
            List<String> skillText = new ArrayList<>();
            List<String> scaleText = new ArrayList<>();
            List<String> effectText = new ArrayList<>();

            switch (jType)   //Individual Button Handling
            {
                case DIMENSION:
                {
                    Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                    Map<String, Double> dimensionBonusMap = JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).get(button.regKey);

                    if( veinBlacklist != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.veinBlacklist" ).setStyle( XP.textStyle.get( "red" ) ).getString() );
                        for ( Map.Entry<String, Double> entry : veinBlacklist.get( button.regKey ).entrySet() )
                        {
                            button.text.add( new StringTextComponent( " " + getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).getString() ).setStyle( XP.textStyle.get( "red" ) ).getString() );
                        }
                    }

                    if ( dimensionBonusMap != null )
                    {
                        for (Map.Entry<String, Double> entry : dimensionBonusMap.entrySet())
                        {
                            if ( entry.getValue() > 0 )
                                skillText.add( new StringTextComponent( " " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").getString() ).setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getString());
                            if ( entry.getValue() < 0 )
                                skillText.add( new StringTextComponent( " " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").getString() ).setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getString());
                        }
                    }
                }
                break;

                case REQ_BIOME:
                {
                    if ( reqMap.containsKey( button.regKey ) )
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);

                    Map<String, Double> biomeBonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME ).get(button.regKey);
                    Map<String, Double> biomeMobMultiplierMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get(button.regKey);
                    Map<String, Double> biomeEffectsMap = JsonConfig.data.get( JType.BIOME_EFFECT_NEGATIVE ).get(button.regKey);

                    if ( biomeBonusMap != null )
                    {
                        for (Map.Entry<String, Double> entry : biomeBonusMap.entrySet()) {
                            if ( (double) entry.getValue() > 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getFormattedText());
                            if ( (double) entry.getValue() < 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getFormattedText());
                        }
                    }

                    if ( biomeMobMultiplierMap != null )
                    {
                        for ( Map.Entry<String, Double> entry : biomeMobMultiplierMap.entrySet() )
                        {
                            Style styleColor = new Style();

                            if ( (double) entry.getValue() > 1 )
                                styleColor = XP.textStyle.get("red");
                            else if ( (double) entry.getValue() < 1 )
                                styleColor = XP.textStyle.get("green");

                            switch ( entry.getKey() )
                            {
                                case "damageBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleDamage", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "hpBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleHp", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "speedBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleSpeed", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;
                            }
                        }
                    }

                    if ( biomeEffectsMap != null )
                    {
                        for ( Map.Entry<String, Double> entry : biomeEffectsMap.entrySet() )
                        {
                            if ( ForgeRegistries.POTIONS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                            {
                                Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );
                                if ( effect != null )
                                    effectText.add( " " + getTransComp( effect.getDisplayName().getFormattedText() + " " + (int) ( (double) entry.getValue() + 1) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                            }
                        }
                    }
                }
                break;

                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case INFO_SMELT:
                case INFO_COOK:
                case INFO_BREW:
                {
                    button.text.add( "" );
                    Map<String, Double> breakMap = JsonConfig.data.get( JType.REQ_BREAK ).get( button.regKey );
                    Map<String, Double> infoMap = XP.getReqMap( button.regKey, jType );
                    List<String> infoText = new ArrayList<>();
                    String transKey = "pmmo." + jType.toString().replace( "info_", "" ) + "ExtraDrop";
                    double extraDroppedPerLevel = infoMap.get( "extraChance" ) / 100;
                    double extraDropped = XP.getExtraChance( player.getUniqueID(), button.regKey, jType, true ) / 100;

                    if ( extraDropped <= 0 )
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    else
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                    if ( extraDroppedPerLevel <= 0 )
                        infoText.add( getTransComp( "pmmo.extraPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                    else
                        infoText.add( getTransComp( "pmmo.extraPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("green") ).getFormattedText() );

                    if ( infoText.size() > 0 )
                        button.text.addAll( infoText );

                    if ( breakMap != null && ( jType.equals( JType.INFO_ORE ) || jType.equals( JType.INFO_LOG ) || jType.equals( JType.INFO_PLANT ) ) )
                    {
                        if ( XP.checkReq( player, button.regKey, JType.REQ_BREAK ) )
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        addLevelsToButton( button, breakMap, player, false );
                    }
                }
                break;

                case XP_BONUS_WORN:
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), XP.checkReq( player, button.regKey, JType.REQ_WEAR ) );
                }
                break;

                case XP_BONUS_HELD:
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), true );
                }
                break;

                case XP_VALUE_BREED:
                case XP_VALUE_TAME:
                case XP_VALUE_SMELT:
                case XP_VALUE_COOK:
                case XP_VALUE_BREW:
                {
                    addXpToButton( button, reqMap.get( button.regKey ) );
                }
                break;

                case XP_VALUE_BREAK:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_BREAK, player );
                }
                break;

                case XP_VALUE_CRAFT:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_CRAFT, player );
                }
                break;

                case XP_VALUE_GROW:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_PLACE, player );
                }
                break;

                case FISH_ENCHANT_POOL:
                {
                    Map<String, Double> enchantMap = reqMap.get( button.regKey );

                    double fishLevel = Skill.FISHING.getLevelDecimal( player );

                    double levelReq = (double) enchantMap.get( "levelReq" );
                    double chancePerLevel = (double) enchantMap.get( "chancePerLevel" );
                    double maxChance = (double) enchantMap.get( "maxChance" );
                    double maxLevel = (int) (double) enchantMap.get( "maxLevel" );
                    double levelsPerTier = (double) enchantMap.get( "levelPerLevel" );
                    double maxLevelAvailable;
                    if( levelsPerTier == 0 )
                        maxLevelAvailable = maxLevel;
                    else
                        maxLevelAvailable = Math.floor( ( fishLevel - levelReq ) / levelsPerTier );
                    if( maxLevelAvailable < 0 )
                        maxLevelAvailable = 0;
                    if( maxLevelAvailable > maxLevel )
                        maxLevelAvailable = maxLevel;

                    double curChance = (fishLevel - levelReq) * chancePerLevel;
                    if( curChance > maxChance )
                        curChance = maxChance;
                    if( curChance < 0 )
                        curChance = 0;

                    button.unlocked = maxLevelAvailable > 0;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    button.text.add( "" );

                    button.text.add( " " + getTransComp( "pmmo.currentChance", DP.dpSoft( curChance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.maxEnchantLevel", (int) maxLevelAvailable ).setStyle( color ).getFormattedText() );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).getFormattedText() );
                    if( maxLevel > 1 )
                        button.text.add( " " + getTransComp( "pmmo.levelsPerTier", DP.dpSoft( levelsPerTier ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.maxEnchantLevel", (int) maxLevel ).getFormattedText() );
                }
                break;

                case REQ_KILL:
                {
                    Map<String, Double> killXpMap = JsonConfig.data.get( JType.REQ_KILL ).get( button.regKey );
                    Map<String, Double> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP ).get(button.regKey);
                    button.unlocked = XP.checkReq( player, button.regKey, jType );
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    if ( reqMap.containsKey( button.regKey ) )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.toHarm" ).setStyle( color ).getFormattedText() );
                        addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                    }

                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.xpValue" ).setStyle( color ).getFormattedText() );
                    if ( killXpMap != null )
                        addXpToButton( button, killXpMap, jType, player );
                    else
                    {
                        if( button.entity instanceof AnimalEntity )
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( Config.forgeConfig.passiveMobHunterXp.get() ) ).setStyle( color ).getFormattedText() );
                        else if( button.entity instanceof MobEntity)
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( Config.forgeConfig.aggresiveMobSlayerXp.get() ) ).setStyle( color ).getFormattedText() );
                    }

                    if ( rareDropMap != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.rareDrops" ).setStyle( color ).getFormattedText() );
                        for( Map.Entry<String, Double> entry : rareDropMap.entrySet() )
                        {
                            button.text.add( " " + new StringTextComponent( getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).getFormattedText() + ": " + getTransComp( "pmmo.dropChance", DP.dpSoft( entry.getValue() ) ).getFormattedText() ).setStyle( color ).getFormattedText() );
                        }
                    }
                }
                break;

                case FISH_POOL:
                {
                    Map<String, Double> fishPoolMap = reqMap.get( button.regKey );

                    double level = Skill.FISHING.getLevelDecimal( player );
                    double weight = XP.getWeight( (int) level, fishPoolMap );
                    button.unlocked = weight > 0;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    minCount = (int) (double) fishPoolMap.get( "minCount" );
                    maxCount = (int) (double) fishPoolMap.get( "maxCount" );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.currentWeight", weight ).setStyle( color ).getFormattedText() );

                    if ( minCount == maxCount )
                        button.text.add( " " + getTransComp( "pmmo.caughtAmount", minCount ).setStyle( color ).getFormattedText() );
                    else
                        button.text.add( " " + getTransComp( "pmmo.caughtAmountRange", minCount, maxCount ).setStyle( color ).getFormattedText() );

                    button.text.add( " " + getTransComp( "pmmo.xpEach", DP.dpSoft( (double) fishPoolMap.get("xp") ) ).setStyle( color ).getFormattedText() );

                    if ( button.itemStack.isEnchantable() )
                    {
                        if( (double) fishPoolMap.get( "enchantLevelReq" ) <= level && button.unlocked )
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    }

                    button.text.add( "" );
//                    button.text.add( getTransComp( "pmmo.info" ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startWeight", DP.dpSoft( (double) fishPoolMap.get("startWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( (double) fishPoolMap.get("startLevel") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endWeight", DP.dpSoft( (double) fishPoolMap.get("endWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endLevel", DP.dpSoft( (double) fishPoolMap.get("endLevel") ) ).getFormattedText() );
                }
                break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                {
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo." + jType.toString().replace( "req_", "" ) ).setStyle( XP.textStyle.get( XP.checkReq( player, button.regKey, jType ) ? "green" : "red" ) ).getFormattedText() );
                    addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                }
                break;

                case SALVAGE:
                case SALVAGE_FROM:
                {
                    if( reqMap2 == null )
                        return;
                    int smithLevel = (int) Skill.SMITHING.getLevelDecimal( player );
                    String outputName;
                    double chance, levelReq, salvageMax, baseChance, chancePerLevel, maxChance, xpPerItem;
                    Map<String, Double> salvageToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>( reqMap2.get( button.regKey ).keySet() );
                    toItemsList.sort(Comparator.comparingInt( key -> (int) (double) reqMap2.get( button.regKey ).get( key ).get( "levelReq" ) ) );

                    for( String salvageToItemKey : toItemsList )
                    {
                        salvageToItemMap = reqMap2.get( button.regKey ).get( salvageToItemKey );
                        outputName       = getTransComp( XP.getItem( salvageToItemKey ).getTranslationKey() ).getString();
                        levelReq         = salvageToItemMap.get( "levelReq" );
                        salvageMax       = salvageToItemMap.get( "salvageMax" );
                        baseChance       = salvageToItemMap.get( "baseChance" );
                        chancePerLevel   = salvageToItemMap.get( "chancePerLevel" );
                        maxChance        = salvageToItemMap.get( "maxChance" );
                        xpPerItem        = salvageToItemMap.get( "xpPerItem" );

                        chance = baseChance + ( chancePerLevel * ( smithLevel - levelReq ) );

                        if( chance < 0 )
                            chance = 0;
                        if( chance > maxChance )
                            chance = maxChance;

                        color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                        if( chance > 0 && smithLevel >= levelReq )
                            button.unlocked = true;

                        if( i++ == 0 )
                            button.text.add( new TranslationTextComponent( jType == JType.SALVAGE ? "pmmo.salvagesInto" : "pmmo.canBeSalvagedFrom" ).getString() );
                        button.text.add( new StringTextComponent( "____________________________" ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( new StringTextComponent( getTransComp( jType == JType.SALVAGE ? "pmmo.valueValue" : "pmmo.valueFromValue", DP.dpSoft( salvageMax ), outputName ).getString() ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.canBeSalvagedFromLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( getTransComp( "pmmo.baseChance", DP.dpSoft( baseChance ) ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.maxChancePerItem", DP.dpSoft( maxChance ) ).setStyle( color ).getString() );
                    }
                }
                break;

                case TREASURE:
                case TREASURE_FROM:
                {
                    if( reqMap2 == null )
                        return;
                    int excavationLevel = (int) Skill.EXCAVATION.getLevelDecimal( player );
                    int startLevel, endLevel, minCount, maxCount;
                    String outputName;
                    double chance, startChance, endChance, xpPerItem;
                    Map<String, Double> treasureToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>( reqMap2.get( button.regKey ).keySet() );
                    toItemsList.sort(Comparator.comparingDouble( key -> BlockBrokenHandler.getTreasureItemChance( excavationLevel, reqMap2.get( button.regKey ).get( key ) ) ) );

                    for( String treasureToItemKey : toItemsList )
                    {
                        treasureToItemMap = reqMap2.get( button.regKey ).get( treasureToItemKey );
                        outputName      = getTransComp( XP.getItem( treasureToItemKey ).getTranslationKey() ).getString();
                        startLevel      = (int) (double) treasureToItemMap.get( "startLevel" );
                        endLevel        = (int) (double) treasureToItemMap.get( "endLevel" );
                        startChance     = treasureToItemMap.get( "startChance" );
                        endChance       = treasureToItemMap.get( "endChance" );
                        xpPerItem       = treasureToItemMap.get( "xpPerItem" );
                        minCount        = (int) (double) treasureToItemMap.get( "minCount" );
                        maxCount        = (int) (double) treasureToItemMap.get( "maxCount" );

                        chance = BlockBrokenHandler.getTreasureItemChance( excavationLevel, treasureToItemMap );

                        color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                        if( chance > 0  )
                            button.unlocked = true;

                        if( i++ == 0 )
                            button.text.add( new TranslationTextComponent( jType == JType.TREASURE ? "pmmo.containsTreasure" : "pmmo.treasureFrom" ).getString() );
                        button.text.add( new StringTextComponent( "____________________________" ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( new StringTextComponent( outputName ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( getTransComp( "pmmo.minCount", minCount ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.maxCount", maxCount ).setStyle( color ).getString() );
                        button.text.add( new StringTextComponent( "" ).getString() );
                        button.text.add( getTransComp( "pmmo.startChance", startChance ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.startLevel", startLevel ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.endChance", endChance ).setStyle( color ).getString() );
                        button.text.add( getTransComp( "pmmo.endLevel", endLevel ).setStyle( color ).getString() );
                    }
                }
                break;

                case STATS:
                {
                    Skill skill = Skill.getSkill( button.regKey );

                    double curXp = XP.getOfflineXp( skill, uuid );
                    double nextXp = XP.xpAtLevel( XP.levelAtXp( curXp ) + 1 );

                    button.title = getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + button.regKey ), DP.dpSoft( XP.levelAtXpDecimal( curXp ) ) ).setStyle( XP.getSkillStyle(Skill.getSkill( button.regKey ) ) ).getFormattedText();

                    button.text.add( " " + getTransComp( "pmmo.currentXp", DP.dpSoft( curXp ) ).getFormattedText() );
                    if( skill.getLevel( player ) != Config.getConfig( "maxLevel" ) )
                    {
                        button.text.add( " " + getTransComp( "pmmo.nextLevelXp", DP.dpSoft( nextXp ) ).getFormattedText() );
                        button.text.add( " " + getTransComp( "pmmo.RemainderXp", DP.dpSoft( nextXp - curXp ) ).getFormattedText() );
                    }
                }
                break;

                default:
                    break;
            }

            if( skillText.size() > 0 )
            {
                button.text.add( "" );
                skillText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.xpModifiers" ).getFormattedText() );
                button.text.addAll( skillText );
            }

            if( scaleText.size() > 0 )
            {
                if( skillText.size() > 0 )
                    button.text.add( "" );

                scaleText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.enemyScaling" ).getFormattedText() );
                button.text.addAll( scaleText );
            }

            if( effectText.size() > 0 )
            {
                if( skillText.size() > 0 || scaleText.size() > 0 )
                    button.text.add( "" );

                effectText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.biomeEffects" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                button.text.addAll( effectText );
            }

            switch( jType )  //Unlock/Lock buttons if necessary
            {
                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case XP_VALUE_BREAK:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_BREAK );
                    break;

                case XP_BONUS_WORN:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_WEAR );
                    break;

                case FISH_POOL:

                    break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                case REQ_BIOME:
                    button.unlocked = XP.checkReq( player, button.regKey, jType );
                    break;

                case XP_VALUE_CRAFT:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_CRAFT );
                    break;

                case XP_VALUE_GROW:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_PLACE );
                    break;

                default:
                    break;
            }
//            children.add( button );
        }

        //SORT BUTTONS

        switch( jType )
        {
            case INFO_ORE:
            case INFO_LOG:
            case INFO_PLANT:
            case XP_VALUE_BREAK:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, JType.REQ_BREAK ) ) );
                break;

            case REQ_WEAR:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, JType.REQ_WEAR ) ) );
                break;

            case SALVAGE:
            case SALVAGE_FROM:
                listButtons.sort( Comparator.comparingDouble( b -> (double) getLowestSalvageReq( reqMap2.get( b.regKey ) ) ) );
                break;

            case STATS:
                listButtons.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( Skill.getSkill( ((ListButton) b).regKey ), uuid ) ).reversed() );
                break;

            default:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, jType ) ) );
                break;
        }

        switch( jType ) //default buttons
        {
            case XP_VALUE_BREED:
                ListButton otherAnimalsBreedButton = new ListButton( 0, 0, 3, 20, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickActionGlossary() );
                otherAnimalsBreedButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.farming" ), DP.dpSoft( Config.forgeConfig.defaultBreedingXp.get() ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsBreedButton );
                break;

            case XP_VALUE_TAME:
            {
                ListButton otherAnimalsTameButton = new ListButton( 0, 0, 3, 21, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickActionGlossary() );
                otherAnimalsTameButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.taming" ), DP.dpSoft( Config.forgeConfig.defaultTamingXp.get() ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsTameButton );
            }
            break;

            case REQ_KILL:
            {
                ListButton otherAggresiveMobsButton = new ListButton( 0, 0, 3, 26, "pmmo.otherAggresiveMobs", jType, "", button -> ((ListButton) button).clickActionGlossary() );
                otherAggresiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( Config.forgeConfig.aggresiveMobSlayerXp.get() ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAggresiveMobsButton );

                ListButton otherPassiveMobsButton = new ListButton( 0, 0, 3, 26, "pmmo.otherPassiveMobs", jType, "", button -> ((ListButton) button).clickActionGlossary() );
                otherPassiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( Config.forgeConfig.passiveMobHunterXp.get() ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherPassiveMobsButton );
            }
            break;

            case XP_VALUE_CRAFT:
            {
                ListButton otherCraftsButton = new ListButton( 0, 0, 3, 22, "pmmo.otherCrafts", jType, "", button -> ((ListButton) button).clickActionGlossary() );
                otherCraftsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.crafting" ), DP.dpSoft( Config.forgeConfig.defaultCraftingXp.get() ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherCraftsButton );
            }
            break;
        }
        scrollPanel = new ListScrollPanel( Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX, jType, player, listButtons );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton( exitButton );
    }

    private static void addLevelsToButton( ListButton button, Map<String, Double> map, PlayerEntity player, boolean ignoreReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            if( !ignoreReq && Skill.getSkill( inEntry.getKey() ).getLevelDecimal( player ) < (double) inEntry.getValue() )
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Double> map )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Double> map, JType jType, PlayerEntity player )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( XP.checkReq( player, button.regKey, jType ) ? "green" : "red" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addPercentageToButton( ListButton button, Map<String, Double> map, boolean metReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            double value = (double) inEntry.getValue();

            if( metReq )
            {
                if( value > 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), "+" + value + "%" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                else if( value < 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            }
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static int getTextInt( String comp )
    {
        String number = comp.replaceAll("\\D+","");

        if( number.length() > 0 && !Double.isNaN( Double.parseDouble( number ) ) )
            return (int) Double.parseDouble( number );
        else
            return 0;
    }

    private static int getReqCount( String regKey, JType jType )
    {
        Map<String, Double> map = XP.getReqMap( regKey, jType );

        if( map == null )
            return 0;
        else
            return map.size();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        if( jType.equals( JType.STATS ) )
            title = getTransComp( "pmmo.playerStats", XP.playerNames.get( uuid ) );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( mouseX, mouseY, partialTicks );

        accumulativeHeight = 0;
        buttonsSize = listButtons.size();

        super.render(mouseX, mouseY, partialTicks);

        for( ListButton button : listButtons )
        {
            buttonX = mouseX - button.x;
            buttonY = mouseY - button.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                if( jType.equals( JType.REQ_BIOME ) || jType.equals( JType.REQ_KILL ) || jType.equals( JType.XP_VALUE_BREED ) || jType.equals( JType.XP_VALUE_TAME ) || jType.equals( JType.DIMENSION ) || jType.equals( JType.FISH_ENCHANT_POOL ) || jType.equals( JType.STATS ) || button.regKey.equals( "pmmo.otherCrafts" ) )
                    renderTooltip( button.title, mouseX, mouseY );
                else if( button.itemStack != null )
                    renderTooltip( button.itemStack, mouseX, mouseY );
            }

            accumulativeHeight += button.getHeight();
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
    }

    public static double getLowestSalvageReq( Map<String, Map<String, Double>> map )
    {
        Integer lowestReq = null;
        int levelReq;

        for( Map.Entry<String, Map<String, Double>> entry : map.entrySet() )
        {
            levelReq = (int) (double) entry.getValue().get( "levelReq" );
            if( lowestReq == null || levelReq < lowestReq )
                lowestReq = levelReq;
        }

        return lowestReq;
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled( double mouseX, double mouseY, double scroll )
    {
        accumulativeHeight = 0;
        for( ListButton listButton : listButtons )
        {
            accumulativeHeight += listButton.getHeight();
            if( accumulativeHeight > scrollPanel.getBottom() - scrollPanel.getTop() )
            {
                scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
                break;
            }
        }
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

        for( ListButton a : listButtons )
        {
            int buttonX = (int) mouseX - a.x;
            int buttonY = (int) mouseY- a.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                a.onClick( mouseX, mouseY );
            }
        }
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public static TranslationTextComponent getTransComp( String translationKey, Object... args )
    {
        return new TranslationTextComponent( translationKey, args );
    }

}