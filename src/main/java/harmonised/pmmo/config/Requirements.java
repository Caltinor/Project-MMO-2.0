package harmonised.pmmo.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class Requirements
{
    public static Map<String, Map<String, Map<String, Object>>> localData = new HashMap<>();
    public static Map<String, Map<String, Map<String, Object>>> data = new HashMap<>();

    private static ArrayList<String> validAttributes = new ArrayList<>();
    private static ArrayList<String> validFishEnchantInfo = new ArrayList<>();
    private static String dataPath = "pmmo/data.json";
    private static String templateDataPath = "pmmo/data_template.json";
    private static String defaultDataPath = "/assets/pmmo/util/default_data.json";
    private static final Logger LOGGER = LogManager.getLogger();
    private static Requirements defaultReq, customReq;
    private static Effect invalidEffect = ForgeRegistries.POTIONS.getValue( new ResourceLocation( "inexistantmodthatwillneverexist:potatochan" ) );
    private static Enchantment invalidEnchant = ForgeRegistries.ENCHANTMENTS.getValue( new ResourceLocation( "inexistantmodthatwillneverexist:potatochan" ) );

    public static void init()
    {
        validAttributes.add( "speedBonus" );
        validAttributes.add( "hpBonus" );
        validAttributes.add( "damageBonus" );

        File templateData = FMLPaths.CONFIGDIR.get().resolve( templateDataPath ).toFile();
        File data = FMLPaths.CONFIGDIR.get().resolve( dataPath ).toFile();

        initMaps();

        createData( templateData ); //always rewrite template data with hardcoded one
        if ( !data.exists() )   //If no data file, create one
            createData( data );

        defaultReq = Requirements.readFromFile( templateData.getPath() );
        customReq = Requirements.readFromFile( data.getPath() );

        if( Config.config.loadDefaultConfig.get() )
            updateFinal( defaultReq );
        updateFinal( customReq );
    }

    private static void initMaps()
    {
        initMap( localData );
        initMap( data );
    }

    private static void initMap( Map<String, Map<String, Map<String, Object>>> map )
    {
        map.put( "wearReq", new HashMap<>() );
        map.put( "toolReq", new HashMap<>() );
        map.put( "weaponReq", new HashMap<>() );
        map.put( "mobReq", new HashMap<>() );
        map.put( "useReq", new HashMap<>() );
        map.put( "placeReq", new HashMap<>() );
        map.put( "breakReq", new HashMap<>() );
        map.put( "biomeReq", new HashMap<>() );
        map.put( "biomeMultiplier", new HashMap<>() );
        map.put( "biomeEffect", new HashMap<>() );
        map.put( "biomeMobMultiplier", new HashMap<>() );
        map.put( "xpValue", new HashMap<>() );
        map.put( "xpValueCrafting", new HashMap<>() );
        map.put( "oreInfo", new HashMap<>() );
        map.put( "logInfo", new HashMap<>() );
        map.put( "plantInfo", new HashMap<>() );
        map.put( "salvageInfo", new HashMap<>() );
        map.put( "salvagesFrom", new HashMap<>() );
        map.put( "fishPool", new HashMap<>() );
        map.put( "fishEnchantPool", new HashMap<>() );
    }

    private static boolean checkValidSkills( Map<String, Object> theMap )
    {
        boolean anyValidSkills = false;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) != 0 )
                anyValidSkills = true;
            else
                LOGGER.info( "Invalid skill " + key + " level " + (double) theMap.get( key ) );
        }

        return anyValidSkills;
    }

    private static boolean checkValidEffects( Map<String, Object> theMap )
    {
        boolean anyValidEffects = false;

        for( String key : theMap.keySet() )
        {
            Effect effect = ForgeRegistries.POTIONS.getValue( new ResourceLocation( key ) );
            if( !effect.equals( invalidEffect ) )
                anyValidEffects = true;
            else
                LOGGER.info( "Invalid effect " + key );
        }

        return anyValidEffects;
    }

    private static void updateReqSkills( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( checkValidSkills( value.requirements ) )
            {
                if(  !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );

                for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        if( Skill.getInt( entry.getKey() ) != 0 )
                            outReq.get( key ).put( entry.getKey(), entry.getValue() );
                        else
                            LOGGER.error( entry.getKey() + " is either not a valid skill, or not 1 or above!" );
                    }
                        else
                        LOGGER.error( entry.getValue() + " is not a Double!" );
                }
            }
            else
                LOGGER.error( "No valid skills, cannot add " + key );
        });
    }

    private static void updateReqEffects( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( checkValidEffects( value.requirements ) )
            {
                if(  !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );

                for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        Potion potion = ForgeRegistries.POTION_TYPES.getValue( new ResourceLocation( entry.getKey() ) );

                        if( !potion.equals( invalidEffect ) && (double) entry.getValue() >= 0 && (double) entry.getValue() < 255 )
                            outReq.get( key ).put( entry.getKey(), entry.getValue() );
                        else
                            LOGGER.error( entry.getKey() + " is either not a effect skill, or below 0, or above 255!" );
                    }
                    else
                        LOGGER.error( entry.getValue() + " is not a Double!" );
                }
            }
            else
                LOGGER.error( "No valid effects, cannot add " + key );
        });
    }

    private static void updateReqExtra( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( !outReq.containsKey( key ) )
                outReq.put( key, new HashMap<>() );

            for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
            {
                if( entry.getValue() instanceof Double )
                {
                    if( entry.getKey().equals( "extraChance" ) && (double) entry.getValue() > 0 )
                        outReq.get( key ).put( entry.getKey(), entry.getValue() );
                    else
                        LOGGER.error( key + " is either not \"extraChance\", or not above 0!" );
                }
                else
                    LOGGER.error( key + " is not a Double!" );
            }
        });
    }

    private static void updateReqSalvage( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( !XP.getItem( key ).equals( Items.AIR ) )
            {
                boolean failed = false;
                Map<String, Object> inMap = value.requirements;

                if( !( inMap.containsKey( "salvageItem" ) && inMap.get( "salvageItem" ) instanceof String ) )
                {
                    LOGGER.error( "Failed to load Salvage Item " + key + " \"salvageItem\" is invalid" );
                    failed = true;
                }
                else if( XP.getItem( (String) inMap.get( "salvageItem" ) ).equals( Items.AIR ) )
                {
                    LOGGER.error( "Failed to load Salvage Item " + key + " \"salvageItem\" item does not exist" );
                    failed = true;
                }

                if( !( inMap.containsKey( "salvageMax" ) && inMap.get( "salvageMax" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"salvageMax\" is invalid, loading default value 1 item" );
                    inMap.put( "salvageMax", 1D );
                }

                if( !( inMap.containsKey( "baseChance" ) && inMap.get( "baseChance" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"baseChance\" is invalid, loading default value 50%" );
                    inMap.put( "baseChance", 50D );
                }

                if( !( inMap.containsKey( "chancePerLevel" ) && inMap.get( "chancePerLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"chancePerLevel\" is invalid, loading default value 0%" );
                    inMap.put( "chancePerLevel", 0D );
                }

                if( !( inMap.containsKey( "maxChance" ) && inMap.get( "maxChance" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"maxChance\" is invalid, loading default value 80%" );
                    inMap.put( "maxChance", 80D );
                }

                if( !( inMap.containsKey( "xpPerItem" ) && inMap.get( "xpPerItem" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"xpPerItem\" is invalid, loading default value 0xp" );
                    inMap.put( "xpPerItem", 0D );
                }

                if( !( inMap.containsKey( "levelReq" ) && inMap.get( "levelReq" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Salvage Item " + key + " \"levelReq\" is invalid, loading default value 1 level" );
                    inMap.put( "levelReq", 1D );
                }

                if( !failed )
                {
                    if( !outReq.containsKey( key ) )
                        outReq.put( key, new HashMap<>() );
                    Map<String, Object> outMap = outReq.get( key );
                    String salvageItem = (String) inMap.get( "salvageItem" );
                    double salvageMax = (double) inMap.get( "salvageMax" );
                    double levelReq = (double) inMap.get( "levelReq" );
                    double xpPerItem = (double) inMap.get( "xpPerItem" );
                    double baseChance = (double) inMap.get( "baseChance" );
                    double chancePerLevel = (double) inMap.get( "chancePerLevel" );
                    double maxChance = (double) inMap.get( "maxChance" );

                    outMap.put( "salvageItem", salvageItem );

                    if( salvageMax < 1 )
                        outMap.put( "salvageMax", 1 );
                    else
                        outMap.put( "salvageMax", salvageMax );

                    if( levelReq < 1 )
                        outMap.put( "levelReq", 1 );
                    else
                        outMap.put( "levelReq", levelReq );

                    if( xpPerItem < 0 )
                        outMap.put( "xpPerItem", 0 );
                    else
                        outMap.put( "xpPerItem", xpPerItem );

                    if( baseChance < 0 )
                        outMap.put( "baseChance", 0 );
                    else if( baseChance > 100 )
                        outMap.put( "baseChance", 100 );
                    else
                        outMap.put( "baseChance", baseChance );

                    if( chancePerLevel < 0 )
                        outMap.put( "chancePerLevel", 0 );
                    else if( chancePerLevel > 100 )
                        outMap.put( "chancePerLevel", 100 );
                    else
                        outMap.put( "chancePerLevel", chancePerLevel );

                    if( maxChance < 0 )
                        outMap.put( "maxChance", 0 );
                    else if( maxChance > 100 )
                        outMap.put( "maxChance", 100 );
                    else
                        outMap.put( "maxChance", maxChance );

                    Map<String, Map<String, Object>> localSalvagesFrom = localData.get( "salvagesFrom" );

                    if( !localSalvagesFrom.containsKey( salvageItem ) )
                        localSalvagesFrom.put( salvageItem, new HashMap<>() );

                    localSalvagesFrom.get( salvageItem ).put( key, salvageMax );
                }
            }
            else
                LOGGER.info( "Could not load inexistant item " + key );
        });
    }

    private static void updateReqFishPool( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            Item item = XP.getItem( key );
            if( !item.equals( Items.AIR ) )
            {
                Map<String, Object> inMap = value.requirements;

                if( !( inMap.containsKey( "startWeight" ) && inMap.get( "startWeight" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"startWeight\" is invalid, loading default value 1" );
                    inMap.put( "startWeight", 1D );
                }

                if( !( inMap.containsKey( "startLevel" ) && inMap.get( "startLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"startLevel\" is invalid, loading default value level 1" );
                    inMap.put( "startLevel", 1D );
                }

                if( !( inMap.containsKey( "endWeight" ) && inMap.get( "endWeight" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"endWeight\" is invalid, loading default value 1" );
                    inMap.put( "endWeight", 1D );
                }

                if( !( inMap.containsKey( "endLevel" ) && inMap.get( "endLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"endLevel\" is invalid, loading default value level 1" );
                    inMap.put( "endLevel", 1D );
                }

                if( !( inMap.containsKey( "minCount" ) && inMap.get( "minCount" ) instanceof Double ) )
                {
//                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"minCount\" is invalid, loading default value 1 item" );
                    inMap.put( "minCount", 1D );
                }
                else if( (double) inMap.get( "minCount" ) > item.getMaxStackSize() )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"minCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "minCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "maxCount" ) && inMap.get( "maxCount" ) instanceof Double ) )
                {
//                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"maxCount\" is invalid, loading default value 1" );
                    inMap.put( "maxCount", 1D );
                }
                else if( (double) inMap.get( "maxCount" ) > item.getMaxStackSize() )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"maxCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "maxCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "enchantLevelReq" ) && inMap.get( "enchantLevelReq" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"enchantLevelReq\" is invalid, loading default value level 1" );
                    inMap.put( "enchantLevelReq", 1D );
                }

                if( !( inMap.containsKey( "xp" ) && inMap.get( "xp" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + key + " \"xp\" is invalid, loading default value 1xp" );
                    inMap.put( "xp", 1D );
                }

                if( !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );

                Map<String, Object> outMap = outReq.get( key );
                double startWeight = (double) inMap.get( "startWeight" );
                double startLevel = (double) inMap.get( "startLevel" );
                double endWeight = (double) inMap.get( "endWeight" );
                double endLevel = (double) inMap.get( "endLevel" );
                double minCount = (double) inMap.get( "minCount" );
                double maxCount = (double) inMap.get( "maxCount" );
                double enchantLevelReq = (double) inMap.get( "enchantLevelReq" );
                double xp = (double) inMap.get( "xp" );

                if( endWeight < 1 )
                    outMap.put( "endWeight", 1D );
                else
                    outMap.put( "endWeight", endWeight );

                if( startWeight > endWeight )
                    startWeight = endWeight;

                if( startWeight < 1 )
                    outMap.put( "startWeight", 1D );
                else
                    outMap.put( "startWeight", startWeight );

                if( endLevel < 1 )
                    outMap.put( "endLevel", 1D );
                else
                    outMap.put( "endLevel", endLevel );

                if( startLevel > endLevel )
                    startLevel = endLevel;

                if( startLevel < 1 )
                    outMap.put( "startLevel", 1D );
                else
                    outMap.put( "startLevel", startLevel );

                if( maxCount < 1 )
                    outMap.put( "maxCount", 1D );
                else
                    outMap.put( "maxCount", maxCount );

                if( minCount > maxCount )
                    minCount = maxCount;

                if( minCount < 1 )
                    outMap.put( "minCount", 1D );
                else
                    outMap.put( "minCount", minCount );

                if( enchantLevelReq < 1 )
                    outMap.put( "enchantLevelReq", 1D );
                else
                    outMap.put( "enchantLevelReq", enchantLevelReq );

                if( xp < 0 )
                    outMap.put( "xp", 0D );
                else
                    outMap.put( "xp", xp );
            }
            else
                LOGGER.info( "Could not load inexistant item " + key );
        });
    }

    private static void updateReqFishEnchantPool( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue( new ResourceLocation( key ) );
            if( !enchant.equals( invalidEnchant ) )
            {
                Map<String, Object> inMap = value.requirements;

                if( !( inMap.containsKey( "levelReq" ) && inMap.get( "levelReq" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + key + " \"levelReq\" is invalid, loading default value 1" );
                    inMap.put( "levelReq", 1D );
                }

                if( !( inMap.containsKey( "levelPerLevel" ) && inMap.get( "levelPerLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + key + " \"levelPerLevel\" is invalid, loading default value 0" );
                    inMap.put( "levelPerLevel", 0D );
                }

                if( !( inMap.containsKey( "chancePerLevel" ) && inMap.get( "chancePerLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + key + " \"chancePerLevel\" is invalid, loading default value 0" );
                    inMap.put( "chancePerLevel", 0D );
                }

                if( !( inMap.containsKey( "maxChance" ) && inMap.get( "maxChance" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + key + " \"maxChance\" is invalid, loading default value 80%" );
                    inMap.put( "maxChance", 80D );
                }

                if( !( inMap.containsKey( "maxLevel" ) && inMap.get( "maxLevel" ) instanceof Double ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + key + " \"maxLevel\" is invalid, loading default value " + enchant.getMaxLevel() );
                    inMap.put( "maxLevel", (double) enchant.getMaxLevel() );
                }


                if( !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );
                Map<String, Object> outMap = outReq.get( key );
                double levelReq = (double) inMap.get( "levelReq" );
                double levelPerLevel = (double) inMap.get( "levelPerLevel" );
                double chancePerLevel = (double) inMap.get( "chancePerLevel" );
                double maxChance = (double) inMap.get( "maxChance" );
                double maxLevel = (double) inMap.get( "maxLevel" );

                if( levelReq < 1 )
                    outMap.put( "levelReq", 1D );
                else
                    outMap.put( "levelReq", levelReq );

                if( levelPerLevel < 0 )
                    outMap.put( "levelPerLevel", 0D );
                else
                    outMap.put( "levelPerLevel", levelPerLevel );

                if( chancePerLevel < 0 )
                    outMap.put( "chancePerLevel", 0D );
                else
                    outMap.put( "chancePerLevel", chancePerLevel );

                if( maxChance < 0 )
                    outMap.put( "maxChance", 0D );
                else if( maxChance > 100 )
                    outMap.put( "maxChance", 100D );
                else
                    outMap.put( "maxChance", maxChance );

                if( maxLevel < 1 )
                    outMap.put( "maxLevel", enchant.getMaxLevel() );
                else
                    outMap.put( "maxLevel", maxLevel );
            }
            else
                LOGGER.info( "Could not load inexistant enchant " + key );
        });
    }

    private static void updateReqAttributes( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( checkValidAttributes( value.requirements ) )
            {
                if( !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );

                for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
                {
                    if( validAttributes.contains( entry.getKey() ) )
                        outReq.get( key ).put( entry.getKey(), entry.getValue() );
                    else
                        LOGGER.error( "Invalid attribute " + entry.getKey() );
                }
            }
            else
                LOGGER.error( "No valid attributes, cannot add " + key );

        });
    }

    private static boolean checkValidAttributes( Map<String, Object> theMap )
    {
        boolean anyValidAttributes = false;

        for( String key : theMap.keySet() )
        {
            if( validAttributes.contains( key ) )
                anyValidAttributes = true;
            else
                LOGGER.info( "Invalid attribute " + key );
        }

        return anyValidAttributes;
    }

    private static void updateFinal( Requirements req )
    {
        if( Config.config.wearReqEnabled.get() )
            updateReqSkills( req.wears, localData.get( "wearReq" ) );

        if( Config.config.toolReqEnabled.get() )
            updateReqSkills( req.tools, localData.get( "toolReq" ) );

        if( Config.config.weaponReqEnabled.get() )
            updateReqSkills( req.weapons, localData.get( "weaponReq" ) );

//        updateReqSkills( req.mobs, mobReq );

        if( Config.config.useReqEnabled.get() )
            updateReqSkills( req.use, localData.get( "useReq" ) );

        if( Config.config.xpValueEnabled.get() )
            updateReqSkills( req.xpValues, localData.get( "xpValue" ) );

        if( Config.config.xpValueCraftingEnabled.get() )
            updateReqSkills( req.xpValuesCrafting, localData.get( "xpValueCrafting" ) );

        if( Config.config.placeReqEnabled.get() )
            updateReqSkills( req.placing, localData.get( "placeReq" ) );

        if( Config.config.breakReqEnabled.get() )
            updateReqSkills( req.breaking, localData.get( "breakReq" ) );

        if( Config.config.biomeReqEnabled.get() )
        {
            updateReqSkills( req.biome, localData.get( "biomeReq" ) );
            updateReqEffects( req.biomeeff, localData.get( "biomeEffect" ) );
        }

        if( Config.config.biomeMultiplierEnabled.get() )
            updateReqSkills( req.biomemultiplier, localData.get( "biomeMultiplier" ) );

        if( Config.config.biomeMobMultiplierEnabled.get() )
            updateReqAttributes( req.biomemobmultiplier, localData.get( "biomeMobMultiplier" ) );

        if( Config.config.oreEnabled.get() )
            updateReqExtra( req.ores, localData.get( "oreInfo" ) );

        if( Config.config.logEnabled.get() )
            updateReqExtra( req.logs, localData.get( "logInfo" ) );

        if( Config.config.plantEnabled.get() )
            updateReqExtra( req.plants, localData.get( "plantInfo" ) );

        if( Config.config.salvageEnabled.get() )
            updateReqSalvage( req.salvage, localData.get( "salvageInfo" ) );

        if( Config.config.fishPoolEnabled.get() )
            updateReqFishPool( req.fishpool, localData.get( "fishPool" ) );

        if( Config.config.fishEnchantPoolEnabled.get() )
            updateReqFishEnchantPool( req.fishenchantpool, localData.get( "fishEnchantPool" ) );

        data = localData;
    }

    private static void createData( File dataFile )
    {
        try     //create template data file
        {
            dataFile.getParentFile().mkdir();
            dataFile.createNewFile();
        }
        catch( IOException e )
        {
            LOGGER.error( "Could not create template json config!", dataFile.getPath(), e );
        }

        try( InputStream inputStream = ProjectMMOMod.class.getResourceAsStream( defaultDataPath );
             FileOutputStream outputStream = new FileOutputStream( dataFile ); )
        {
            IOUtils.copy( inputStream, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error copying over default json config to " + dataFile.getPath(), dataFile.getPath(), e );
        }
    }

    public static Requirements readFromFile( String path )
    {
        try (
                InputStream input = new FileInputStream( path );
                Reader reader = new BufferedReader(new InputStreamReader(input));
        )
        {
            return DESERIALIZER.fromJson(reader, Requirements.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not parse json from {}", path, e);

            // If couldn't read, just return an empty object. This may not be what you want.
            return new Requirements();
        }
    }

    public static class RequirementItem
    {
        private final Map<String, Object> requirements = Maps.newHashMap();

//        public HashMap<String, Object> getMap()
//        {
//            return new HashMap<>( requirements );
//        }

        public double getDouble(String registryName)
        {
            return (double) requirements.get(registryName);
        }
    }

    private final Map<String, RequirementItem> wears = Maps.newHashMap();
    private final Map<String, RequirementItem> tools = Maps.newHashMap();
    private final Map<String, RequirementItem> weapons = Maps.newHashMap();
    private final Map<String, RequirementItem> mobs = Maps.newHashMap();
    private final Map<String, RequirementItem> use = Maps.newHashMap();
    private final Map<String, RequirementItem> placing = Maps.newHashMap();
    private final Map<String, RequirementItem> breaking = Maps.newHashMap();
    private final Map<String, RequirementItem> biome = Maps.newHashMap();
    private final Map<String, RequirementItem> biomeeff = Maps.newHashMap();
    private final Map<String, RequirementItem> biomemultiplier = Maps.newHashMap();
    private final Map<String, RequirementItem> biomemobmultiplier = Maps.newHashMap();
    private final Map<String, RequirementItem> xpValues = Maps.newHashMap();
    private final Map<String, RequirementItem> xpValuesCrafting = Maps.newHashMap();
    private final Map<String, RequirementItem> ores = Maps.newHashMap();
    private final Map<String, RequirementItem> logs = Maps.newHashMap();
    private final Map<String, RequirementItem> plants = Maps.newHashMap();
    private final Map<String, RequirementItem> salvage = Maps.newHashMap();
    private final Map<String, RequirementItem> fishpool = Maps.newHashMap();
    private final Map<String, RequirementItem> fishenchantpool = Maps.newHashMap();


//    public Map<String, Object> getWear(String registryName)
//    {
//        if( wears.containsKey( registryName ) )
//            return wears.get( registryName ).getMap();
//        else
//            return null;
//    }
//
//    public Map<String, Object> getTool(String registryName)
//    {
//        if( tools.containsKey( registryName ) )
//            return tools.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Object> getWeapon(String registryName)
//    {
//        if( weapons.containsKey( registryName ) )
//            return weapons.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Object> getXp(String registryName)
//    {
//        if( xpValues.containsKey( registryName ) )
//            return xpValues.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }

    // -----------------------------------------------------------------------------
    //
    // GSON STUFFS BELOW
    //
    //

    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Requirements.class, new Deserializer())
            .registerTypeAdapter(RequirementItem.class, new EntryDeserializer())
            .create();

    private static class Deserializer implements JsonDeserializer<Requirements>
    {

        @Override
        public Requirements deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Requirements req = new Requirements();

            JsonObject obj = json.getAsJsonObject();
            deserializeGroup(obj, "wear_requirement", req.wears::put, context);
            deserializeGroup(obj, "tool_requirement", req.tools::put, context);
            deserializeGroup(obj, "weapon_requirement", req.weapons::put, context);
            deserializeGroup(obj, "mob_requirement", req.mobs::put, context);
            deserializeGroup(obj, "use_requirement", req.use::put, context);
            deserializeGroup(obj, "place_requirement", req.placing::put, context);
            deserializeGroup(obj, "break_requirement", req.breaking::put, context);
            deserializeGroup(obj, "biome_requirement", req.biome::put, context);
            deserializeGroup(obj, "biome_multiplier", req.biomemultiplier::put, context);
            deserializeGroup(obj, "biome_mob_multiplier", req.biomemobmultiplier::put, context);
            deserializeGroup(obj, "biome_effect", req.biomeeff::put, context);
            deserializeGroup(obj, "xp_value", req.xpValues::put, context);
            deserializeGroup(obj, "crafting_xp", req.xpValuesCrafting::put, context);
            deserializeGroup(obj, "ore", req.ores::put, context);
            deserializeGroup(obj, "log", req.logs::put, context);
            deserializeGroup(obj, "plant", req.plants::put, context);
            deserializeGroup(obj, "salvage", req.salvage::put, context);
            deserializeGroup(obj, "fish_pool", req.fishpool::put, context);
            deserializeGroup(obj, "fish_enchant_pool", req.fishenchantpool::put, context);

            return req;
        }

        private void deserializeGroup(JsonObject obj, String requirementGroupName, BiConsumer<String, RequirementItem> putter, JsonDeserializationContext context)
        {
            if (obj.has(requirementGroupName))
            {
                JsonObject wears = JSONUtils.getJsonObject(obj, requirementGroupName);
                for(Map.Entry<String, JsonElement> entries : wears.entrySet())
                {
                    String name = entries.getKey();
                    RequirementItem values = context.deserialize(entries.getValue(), RequirementItem.class);

                    putter.accept(name, values);
                }
            }
        }
    }

    private static class EntryDeserializer implements JsonDeserializer<RequirementItem>
    {

        @Override
        public RequirementItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            RequirementItem item = new RequirementItem();

            JsonObject obj = json.getAsJsonObject();
            for(Map.Entry<String, JsonElement> entries : obj.entrySet())
            {
                String name = entries.getKey();
                Object values;
                if( name.equals( "salvageItem" ) )
                    values = entries.getValue().getAsString();
                else
                    values = entries.getValue().getAsDouble();

                item.requirements.put( name, values );
            }

            return item;
        }
    }
}