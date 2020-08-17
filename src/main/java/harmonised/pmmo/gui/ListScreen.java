package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
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
    private static double passiveMobHunterXp = Config.forgeConfig.passiveMobHunterXp.get();
    private static double aggresiveMobSlayerXp = Config.forgeConfig.aggresiveMobSlayerXp.get();
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static final double defaultBreedingXp = Config.forgeConfig.defaultBreedingXp.get();
    private static final double defaultTamingXp = Config.forgeConfig.defaultTamingXp.get();
    private static final double defaultCraftingXp = Config.forgeConfig.defaultCraftingXp.get();
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

    public ListScreen(UUID uuid, ITextComponent titleIn, JType jType, PlayerEntity player )
    {
        super(titleIn);
        this.title = titleIn;
        this.player = player;
        this.jType = jType;
        this.uuid = uuid;
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

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", "", (button) ->
        {
            switch( jType )
            {
                case STATS:
                    Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, getTransComp( "pmmo.potato" ) ) );
                    break;

                default:
                    Minecraft.getInstance().displayGuiScreen( new GlossaryScreen( uuid, getTransComp( "pmmo.skills" ) ) );
                    break;
            }
        });

        Map<String, Map<String, Object>> reqMap = JsonConfig.data.get( jType );

        ArrayList<ListButton> tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        switch( jType )      //How it's made: Buttons!
        {
            case REQ_BIOME:
            {
                Map<String, Map<String, Object>> bonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME );
                Map<String, Map<String, Object>> scaleMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER );
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

                biomesToAdd.sort( Comparator.comparingInt( b -> getReqCount( b, JType.REQ_BIOME ) ) );

                for( String regKey : biomesToAdd )
                {
                    if ( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ) != null )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 8, regKey, jType, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case DIMENSION:
            {
                Map<String, Map<String, Object>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                if( veinBlacklist == null )
                    break;

                if( veinBlacklist.containsKey( "all_dimensions" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "all_dimensions", jType, "", button -> ((ListButton) button).clickAction() ) );
                }


                if( veinBlacklist.containsKey( "minecraft:overworld" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:overworld", jType, "", button -> ((ListButton) button).clickAction() ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_nether" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:the_nether", jType, "", button -> ((ListButton) button).clickAction() ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_end" ) )
                {
                    tempList.add( new ListButton( 0, 0, 3, 8, "minecraft:the_end", jType, "", button -> ((ListButton) button).clickAction() ) );
                }

                for( Map.Entry<String, Map<String, Object>> entry : veinBlacklist.entrySet() )
                {
                    if ( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( entry.getKey() ) ) != null )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 8, entry.getKey(), jType, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case REQ_KILL:
            {
                Map<String, Map<String, Object>> killXpMap = JsonConfig.data.get( JType.XP_VALUE_KILL );
                Map<String, Map<String, Object>> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP );
                ArrayList<String> mobsToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( killXpMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : killXpMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( rareDropMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : rareDropMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                for( String regKey : mobsToAdd )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, regKey, jType, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case FISH_ENCHANT_POOL:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENCHANTMENTS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 25, entry.getKey(), jType, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
                break;
            }

            case STATS:
            {
                Set<String> skills;
                if( XP.skills.containsKey( uuid ) )
                    skills = XP.skills.get( uuid ).keySet();
                else
                    skills = new HashSet<>();

                for( String skill : skills )
                {
                    listButtons.add( new ListButton( 0, 0, 3, 6, skill, jType, "", button -> ((ListButton) button).clickAction() ) );
                }
            }
                break;

            default:
            {

                if( reqMap == null )
                    return;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( XP.getItem( entry.getKey() ) != Items.AIR )
                    {
                        tempList.add( new ListButton( 0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickAction() ) );
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
                case REQ_BIOME:
                {
                    if ( reqMap.containsKey( button.regKey ) )
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);

                    Map<String, Object> biomeBonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME ).get(button.regKey);
                    Map<String, Object> biomeMobMultiplierMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get(button.regKey);
                    Map<String, Object> biomeEffectsMap = JsonConfig.data.get( JType.BIOME_EFFECT ).get(button.regKey);

                    if ( biomeBonusMap != null )
                    {
                        for (Map.Entry<String, Object> entry : biomeBonusMap.entrySet()) {
                            if ( (double) entry.getValue() > 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getFormattedText());
                            if ( (double) entry.getValue() < 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").setStyle(XP.getSkillStyle(Skill.getSkill(entry.getKey()))).getFormattedText());
                        }
                    }

                    if ( biomeMobMultiplierMap != null )
                    {
                        for ( Map.Entry<String, Object> entry : biomeMobMultiplierMap.entrySet() )
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
                        for ( Map.Entry<String, Object> entry : biomeEffectsMap.entrySet() )
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
                {
                    button.text.add( "" );
                    Map<String, Object> breakMap = JsonConfig.data.get( JType.REQ_BREAK ).get( button.regKey );
                    Map<String, Double> infoMap = XP.getReqMap( button.regKey, jType );
                    List<String> infoText = new ArrayList<>();
                    String transKey = "pmmo." + jType.toString().replace( "info_", "" ) + "ExtraDrop";
                    double extraDroppedPerLevel = infoMap.get( "extraChance" ) / 100;
                    double extraDropped = XP.getExtraChance( player, button.regKey, jType ) / 100;

                    if ( extraDroppedPerLevel <= 0 )
                        infoText.add( getTransComp( "pmmo.extraDropPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                    else
                        infoText.add( getTransComp( "pmmo.extraDropPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("green") ).getFormattedText() );

                    if ( extraDropped <= 0 )
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    else
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                    if ( infoText.size() > 0 )
                        button.text.addAll( infoText );

                    if ( breakMap != null )
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

                case FISH_ENCHANT_POOL:
                {
                    Map<String, Object> enchantMap = reqMap.get( button.regKey );

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
                    Map<String, Object> killXpMap = JsonConfig.data.get( JType.XP_VALUE_KILL ).get( button.regKey );
                    Map<String, Object> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP ).get(button.regKey);
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
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( passiveMobHunterXp ) ).setStyle( color ).getFormattedText() );
                        else if( button.entity instanceof MobEntity)
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( aggresiveMobSlayerXp ) ).setStyle( color ).getFormattedText() );
                    }

                    if ( rareDropMap != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.rareDrops" ).setStyle( color ).getFormattedText() );
                        for( Map.Entry<String, Object> entry : rareDropMap.entrySet() )
                        {
                            button.text.add( " " + new StringTextComponent( getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).getFormattedText() + ": " + getTransComp( "pmmo.dropChance", DP.dpSoft( (double) entry.getValue() ) ).getFormattedText() ).setStyle( color ).getFormattedText() );
                        }
                    }
                }
                    break;

                case DIMENSION:
                {
                    Map<String, Map<String, Object>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                    if( veinBlacklist != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.veinBlacklist" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        for ( Map.Entry<String, Object> entry : veinBlacklist.get( button.regKey ).entrySet() )
                        {
                            button.text.add( " " + getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        }
                    }
                }
                    break;

                case FISH_POOL:
                {
                    Map<String, Object> fishPoolMap = reqMap.get( button.regKey );

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

                case SALVAGE_FROM:
                {
                    Map<String, Map<String, Object>> salvagesToMap = JsonConfig.data.get( JType.SALVAGE_TO );
                    Map<String, Double> levelReqs = new HashMap<>();
                    List<String> sortedItems = new ArrayList<>();
                    if( reqMap == null || salvagesToMap == null )
                        return;
                    double smithLevel = Skill.SMITHING.getLevelDecimal( player );

                    boolean anyPassed = false;

                    for( Map.Entry<String, Object> entry : reqMap.get( button.regKey ).entrySet() )
                    {
                        double levelReq = (double) salvagesToMap.get( entry.getKey() ).get( "levelReq" );
                        levelReqs.put( entry.getKey(), levelReq );
                        sortedItems.add( entry.getKey() );
                    }

                    sortedItems.sort( Comparator.comparingDouble( a -> (double) reqMap.get( button.regKey ).get( a ) ) );
                    sortedItems.sort( Comparator.comparingDouble( levelReqs::get ) );

                    for( String key : sortedItems )
                    {
                        double levelReq = levelReqs.get( key );
                        String itemName = getTransComp( XP.getItem( key ).getTranslationKey() ).getFormattedText();

                        double chance = (smithLevel - levelReq) * (double) salvagesToMap.get( key ).get( "chancePerLevel" );
                        double maxChance = (double) salvagesToMap.get( key ).get( "maxChance" );

                        if( chance > maxChance )
                            chance = maxChance;

                        boolean passed = levelReq <= smithLevel && chance > 0;

                        if( passed )
                        {
                            anyPassed = true;
                            button.text.add( " " + getTransComp( "pmmo.valueValueChance", DP.dpSoft( (double) reqMap.get( button.regKey ).get( key ) ), itemName, DP.dpSoft( chance ) ).setStyle( XP.textStyle.get( chance > 0 ? "green" : "red" ) ).getFormattedText() );
                        }
                        else
                            button.text.add( " " + getTransComp( "pmmo.salvagesFromLevelFromItem", DP.dpSoft( (double) reqMap.get( button.regKey ).get( key ) ), DP.dpSoft( levelReq ), itemName ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    }

                    button.unlocked = anyPassed;
                }
                    break;

                case SALVAGE_TO:
                {
                    if( reqMap == null )
                        return;
                    double smithLevel = Skill.SMITHING.getLevelDecimal( player );
                    String outputName = getTransComp( XP.getItem( (String) reqMap.get( button.regKey ).get( "salvageItem" ) ).getTranslationKey() ).getString();
                    double levelReq = (double) reqMap.get( button.regKey ).get( "levelReq" );
                    double salvageMax = (double) reqMap.get( button.regKey ).get( "salvageMax" );
                    double baseChance = (double) reqMap.get( button.regKey ).get( "baseChance" );
                    double chancePerLevel = (double) reqMap.get( button.regKey ).get( "chancePerLevel" );
                    double maxChance = (double) reqMap.get( button.regKey ).get( "maxChance" );
                    double xpPerItem = (double) reqMap.get( button.regKey ).get( "xpPerItem" );

                    double chance = 0;
                    button.unlocked = levelReq <= smithLevel;
                    if( button.unlocked )
                        chance = baseChance + ( smithLevel - levelReq ) * chancePerLevel;
                    if( chance > maxChance )
                        chance = maxChance;

                    Style color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.canBeSalvagedFromLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.valueValue", DP.dpSoft( salvageMax ), outputName ).setStyle( color ).getFormattedText() );
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.baseChance", DP.dpSoft( baseChance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.maxChancePerItem", DP.dpSoft( maxChance ) ).setStyle( color ).getFormattedText() );
                }
                    break;

                case STATS:
                {
                    Skill skill = Skill.getSkill( button.regKey );

                    double curXp = XP.getXpOffline( skill, uuid );
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
                case XP_VALUE_CRAFT:
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

            case SALVAGE_TO:
                listButtons.sort( Comparator.comparingDouble( b -> (double) reqMap.get( b.regKey ).get( "levelReq" ) ) );
                break;

            case STATS:
                listButtons.sort( Comparator.comparingDouble( b -> XP.getXpOffline( Skill.getSkill( ((ListButton) b).regKey ), uuid ) ).reversed() );
                break;

            default:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, jType ) ) );
                break;
        }

        switch( jType ) //default buttons
        {
            case XP_VALUE_BREED:
                ListButton otherAnimalsBreedButton = new ListButton( 0, 0, 3, 20, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickAction() );
                otherAnimalsBreedButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.farming" ), DP.dpSoft( defaultBreedingXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsBreedButton );
                break;

            case XP_VALUE_TAME:
            {
                ListButton otherAnimalsTameButton = new ListButton( 0, 0, 3, 21, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickAction() );
                otherAnimalsTameButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.taming" ), DP.dpSoft( defaultTamingXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsTameButton );
            }
                break;

            case XP_VALUE_KILL:
            {
                ListButton otherAggresiveMobsButton = new ListButton( 0, 0, 3, 26, "pmmo.otherAggresiveMobs", jType, "", button -> ((ListButton) button).clickAction() );
                otherAggresiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( aggresiveMobSlayerXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAggresiveMobsButton );

                ListButton otherPassiveMobsButton = new ListButton( 0, 0, 3, 26, "pmmo.otherPassiveMobs", jType, "", button -> ((ListButton) button).clickAction() );
                otherPassiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( passiveMobHunterXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherPassiveMobsButton );
            }
                break;

            case XP_VALUE_CRAFT:
            {
                ListButton otherCraftsButton = new ListButton( 0, 0, 3, 22, "pmmo.otherCrafts", jType, "", button -> ((ListButton) button).clickAction() );
                otherCraftsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.crafting" ), DP.dpSoft( defaultCraftingXp ) ).setStyle( greenColor ).getFormattedText() );
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

    private static void addLevelsToButton( ListButton button, Map<String, Object> map, PlayerEntity player, boolean ignoreReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            if( !ignoreReq && Skill.getSkill( inEntry.getKey() ).getLevelDecimal( player ) < (double) inEntry.getValue() )
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Object> map )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Object> map, JType jType, PlayerEntity player )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( XP.checkReq( player, button.regKey, jType ) ? "green" : "red" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addPercentageToButton( ListButton button, Map<String, Object> map, boolean metReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
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
                if( jType.equals( JType.REQ_BIOME ) || jType.equals( JType.XP_VALUE_KILL ) || jType.equals( JType.XP_VALUE_BREED ) || jType.equals( JType.XP_VALUE_TAME ) || jType.equals( JType.DIMENSION ) || jType.equals( JType.FISH_ENCHANT_POOL ) || jType.equals( JType.STATS ) || button.regKey.equals( "pmmo.otherCrafts" ) )
                    renderTooltip( button.title, mouseX, mouseY );
                else if( button.itemStack != null )
                    renderTooltip( button.itemStack, mouseX, mouseY );
            }

            accumulativeHeight += button.getHeight();
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
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