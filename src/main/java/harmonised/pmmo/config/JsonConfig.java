package harmonised.pmmo.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;

public class JsonConfig
{
    public static Gson gson = new Gson();
    public static final Type mapType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();

    public static Set<JType> jTypes;
    private static Map<JType, Map<String, Map<String, Object>>> rawData = new HashMap<>();
    public static Map<JType, Map<String, Map<String, Object>>> localData = new HashMap<>();
    public static Map<JType, Map<String, Map<String, Object>>> data = new HashMap<>();

    private static final ArrayList<String> validAttributes = new ArrayList<>();
    private static final ArrayList<String> validFishEnchantInfo = new ArrayList<>();
    private static final String dataPath = "pmmo/";
    private static final String hardDataPath = "/assets/pmmo/util/";
    private static final Effect invalidEffect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( "inexistantmodthatwillneverexist:potatochan" ) );
    private static final Enchantment invalidEnchant = ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( "inexistantmodthatwillneverexist:potatochan" ) );

//    private static Entity invalidEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( "inexistantmodthatwillneverexist:potatochan" ) );

    public static void init()
    {
        validAttributes.add( "speedBonus" );
        validAttributes.add( "hpBonus" );
        validAttributes.add( "damageBonus" );

        initMaps();         //populate maps
        initJTypes();       //check which JTypes should be read from data
        initData();         //copy over defaults if files aren't found (to do: check if valid json)
        readRawData();      //read in the data from /config/pmmo/
        processRawData();   //turn Raw data into Usable data
        
        JsonConfig.data = localData;
    }

    private static void initMaps()
    {
        initMap( localData );
        initMap( data );
    }

    private static void initMap( Map<JType, Map<String, Map<String, Object>>> map )
    {
        for( Map.Entry<JType, Integer> entry : JType.jTypeMap.entrySet() )
        {
            if( map.containsKey( entry.getKey() ) )
                map.replace( entry.getKey(), new HashMap<>() );
            else
                map.put( entry.getKey(), new HashMap<>() );
        }
    }

    private static void initJTypes()
    {
        jTypes = new HashSet<>();

        if( Config.forgeConfig.wearReqEnabled.get() )
            jTypes.add( JType.REQ_WEAR );

        if( Config.forgeConfig.toolReqEnabled.get() )
            jTypes.add( JType.REQ_TOOL );

        if( Config.forgeConfig.weaponReqEnabled.get() )
            jTypes.add( JType.REQ_WEAPON );

        if( Config.forgeConfig.useReqEnabled.get() )
            jTypes.add( JType.REQ_USE );

        if( Config.forgeConfig.xpValueGeneralEnabled.get() )
            jTypes.add( JType.XP_VALUE_GENERAL );

        if( Config.forgeConfig.xpValueBreakingEnabled.get() )
            jTypes.add( JType.XP_VALUE_BREAK );

        if( Config.forgeConfig.xpValueCraftingEnabled.get() )
            jTypes.add( JType.XP_VALUE_CRAFT );

        if( Config.forgeConfig.breedingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_BREED );

        if( Config.forgeConfig.tamingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_TAME );

        if( Config.forgeConfig.smeltingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_SMELT );

        if( Config.forgeConfig.cookingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_COOK );

        jTypes.add( JType.XP_VALUE_TRIGGER );

        if( Config.forgeConfig.placeReqEnabled.get() )
            jTypes.add( JType.REQ_PLACE );

        if( Config.forgeConfig.breakReqEnabled.get() )
            jTypes.add( JType.REQ_BREAK );

        if( Config.forgeConfig.biomeReqEnabled.get() )
            jTypes.add( JType.REQ_BIOME );

        if( Config.forgeConfig.biomeEffectEnabled.get() )
            jTypes.add( JType.BIOME_EFFECT );

        if( Config.forgeConfig.biomeXpBonusEnabled.get() )
            jTypes.add( JType.XP_BONUS_BIOME );

        if( Config.forgeConfig.biomeMobMultiplierEnabled.get() )
            jTypes.add( JType.BIOME_MOB_MULTIPLIER );

        if( Config.forgeConfig.oreEnabled.get() )
            jTypes.add( JType.INFO_ORE );

        if( Config.forgeConfig.logEnabled.get() )
            jTypes.add( JType.INFO_LOG );

        if( Config.forgeConfig.plantEnabled.get() )
            jTypes.add( JType.INFO_PLANT );

        if( Config.forgeConfig.smeltingEnabled.get() )
            jTypes.add( JType.INFO_SMELT );

        if( Config.forgeConfig.cookingEnabled.get() )
            jTypes.add( JType.INFO_COOK );

        if( Config.forgeConfig.salvageEnabled.get() )
            jTypes.add( JType.SALVAGE_TO );

        if( Config.forgeConfig.fishPoolEnabled.get() )
            jTypes.add( JType.FISH_POOL );

        if( Config.forgeConfig.fishEnchantPoolEnabled.get() )
            jTypes.add( JType.FISH_ENCHANT_POOL );

        if( Config.forgeConfig.killReqEnabled.get() )
            jTypes.add( JType.REQ_KILL );

        if( Config.forgeConfig.killXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_KILL );

        if( Config.forgeConfig.mobRareDropEnabled.get() )
            jTypes.add( JType.MOB_RARE_DROP );

        if( Config.forgeConfig.levelUpCommandEnabled.get() )
            jTypes.add( JType.LEVEL_UP_COMMAND );

        if( Config.forgeConfig.heldItemXpBoostEnabled.get() )
            jTypes.add( JType.XP_BONUS_HELD );

        if( Config.forgeConfig.wornItemXpBoostEnabled.get() )
            jTypes.add( JType.XP_BONUS_WORN );

        jTypes.add( JType.BLOCK_SPECIFIC );
        jTypes.add( JType.PLAYER_SPECIFIC );
        jTypes.add( JType.VEIN_BLACKLIST );

        if( Config.forgeConfig.craftReqEnabled.get() )
            jTypes.add( JType.REQ_CRAFT );
    }
    
    private static void initData()
    {
        String fileName;

        for( JType jType : jTypes )
        {
            fileName = jType.name().toLowerCase() + ".json";
            File dataFile = FMLPaths.CONFIGDIR.get().resolve( dataPath + fileName ).toFile();

            if ( !dataFile.exists() )   //If no data file, create one
                createData( dataFile, fileName );
        }
    }

    private static void readRawData()
    {
        rawData = new HashMap<>();
        File file;
        String fileName;

        for( JType jType : jTypes )
        {
            fileName = jType.name().toLowerCase() + ".json";
            file = FMLPaths.CONFIGDIR.get().resolve( dataPath + fileName ).toFile();

            try
            (
                InputStream input = new FileInputStream( file.getPath() );
                Reader reader = new BufferedReader( new InputStreamReader( input ) );
            )
            {
                rawData.put( jType, gson.fromJson( reader, mapType ) );
            }
            catch (IOException e)
            {
                LogHandler.LOGGER.error( "ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + dataPath + fileName, e );
                rawData.put( jType, new HashMap<>() );
            }
        }
    }

    private static void processRawData()
    {
        if( jTypes.contains( JType.REQ_WEAR ) )
            updateReqSkills( rawData.get( JType.REQ_WEAR ), localData.get( JType.REQ_WEAR ) );

        if( jTypes.contains( JType.REQ_TOOL ) )
            updateReqSkills( rawData.get( JType.REQ_TOOL ), localData.get( JType.REQ_TOOL ) );

        if( jTypes.contains( JType.REQ_WEAPON ) )
            updateReqSkills( rawData.get( JType.REQ_WEAPON ), localData.get( JType.REQ_WEAPON ) );

        if( jTypes.contains( JType.REQ_USE ) )
            updateReqSkills( rawData.get( JType.REQ_USE ), localData.get( JType.REQ_USE ) );

        if( jTypes.contains( JType.REQ_PLACE ) )
            updateReqSkills( rawData.get( JType.REQ_PLACE ), localData.get( JType.REQ_PLACE ) );

        if( jTypes.contains( JType.REQ_BREAK ) )
            updateReqSkills( rawData.get( JType.REQ_BREAK ), localData.get( JType.REQ_BREAK ) );

        if( jTypes.contains( JType.REQ_BIOME ) )
            updateReqSkills( rawData.get( JType.REQ_BIOME ), localData.get( JType.REQ_BIOME ) );

        if( jTypes.contains( JType.REQ_KILL ) )
            updateReqSkills( rawData.get( JType.REQ_KILL ), localData.get( JType.REQ_KILL ) );

        if( jTypes.contains( JType.XP_VALUE_GENERAL ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_GENERAL ), localData.get( JType.XP_VALUE_GENERAL ) );

        if( jTypes.contains( JType.XP_VALUE_BREAK ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_BREAK ), localData.get( JType.XP_VALUE_BREAK ) );

        if( jTypes.contains( JType.XP_VALUE_CRAFT ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_CRAFT ), localData.get( JType.XP_VALUE_CRAFT ) );

        if( jTypes.contains( JType.XP_VALUE_BREED ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_BREED ), localData.get( JType.XP_VALUE_BREED ) );

        if( jTypes.contains( JType.XP_VALUE_TAME ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_TAME ), localData.get( JType.XP_VALUE_TAME ) );

        if( jTypes.contains( JType.XP_VALUE_SMELT ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_SMELT ), localData.get( JType.XP_VALUE_SMELT ) );

        if( jTypes.contains( JType.XP_VALUE_COOK ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_COOK ), localData.get( JType.XP_VALUE_COOK ) );

        if( jTypes.contains( JType.XP_VALUE_KILL ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_KILL ), localData.get( JType.XP_VALUE_KILL ) );

        if( jTypes.contains( JType.XP_VALUE_TRIGGER ) )
            updateReqSkills( rawData.get( JType.XP_VALUE_TRIGGER ), localData.get( JType.XP_VALUE_TRIGGER ) );

        if( jTypes.contains( JType.INFO_ORE ) )
            updateReqExtra( rawData.get( JType.INFO_ORE ), localData.get( JType.INFO_ORE ) );

        if( jTypes.contains( JType.INFO_LOG ) )
            updateReqExtra( rawData.get( JType.INFO_LOG ), localData.get( JType.INFO_LOG ) );

        if( jTypes.contains( JType.INFO_PLANT ) )
            updateReqExtra( rawData.get( JType.INFO_PLANT ), localData.get( JType.INFO_PLANT ) );

        if( jTypes.contains( JType.INFO_SMELT ) )
            updateReqExtra( rawData.get( JType.INFO_SMELT ), localData.get( JType.INFO_SMELT ) );

        if( jTypes.contains( JType.INFO_COOK ) )
            updateReqExtra( rawData.get( JType.INFO_COOK ), localData.get( JType.INFO_COOK ) );

        if( jTypes.contains( JType.BIOME_EFFECT ) )
            updateReqEffects( rawData.get( JType.BIOME_EFFECT ), localData.get( JType.BIOME_EFFECT ) );

        if( jTypes.contains( JType.BIOME_MOB_MULTIPLIER ) )
            updateReqAttributes( rawData.get( JType.BIOME_MOB_MULTIPLIER ), localData.get( JType.BIOME_MOB_MULTIPLIER ) );

        if( jTypes.contains( JType.XP_BONUS_BIOME ) )
            updateReqSkills( rawData.get( JType.XP_BONUS_BIOME ), localData.get( JType.XP_BONUS_BIOME ) );

        if( jTypes.contains( JType.XP_BONUS_HELD ) )
            updateReqSkills( rawData.get( JType.XP_BONUS_HELD ), localData.get( JType.XP_BONUS_HELD ) );

        if( jTypes.contains( JType.XP_BONUS_WORN ) )
            updateReqSkills( rawData.get( JType.XP_BONUS_WORN ), localData.get( JType.XP_BONUS_WORN ) );

        if( jTypes.contains( JType.SALVAGE_TO ) )
            updateReqSalvage( rawData.get( JType.SALVAGE_TO ), localData.get( JType.SALVAGE_TO ) );

        if( jTypes.contains( JType.FISH_POOL ) )
            updateReqfishPool( rawData.get( JType.FISH_POOL ), localData.get( JType.FISH_POOL ) );

        if( jTypes.contains( JType.FISH_ENCHANT_POOL ) )
            updateReqFishEnchantPool( rawData.get( JType.FISH_ENCHANT_POOL ), localData.get( JType.FISH_ENCHANT_POOL ) );

        if( jTypes.contains( JType.MOB_RARE_DROP ) )
            updateEntityItem( rawData.get( JType.MOB_RARE_DROP ), localData.get( JType.MOB_RARE_DROP ) );

        if( jTypes.contains( JType.LEVEL_UP_COMMAND ) )
            updateCommand( rawData.get( JType.LEVEL_UP_COMMAND ), localData.get( JType.LEVEL_UP_COMMAND ) );

        if( jTypes.contains( JType.PLAYER_SPECIFIC ) )
            updateSpecific( rawData.get( JType.PLAYER_SPECIFIC ), localData.get( JType.PLAYER_SPECIFIC ) );

        if( jTypes.contains( JType.BLOCK_SPECIFIC ) )
            updateSpecific( rawData.get( JType.BLOCK_SPECIFIC ), localData.get( JType.BLOCK_SPECIFIC ) );

        if( jTypes.contains( JType.VEIN_BLACKLIST ) )
            updateReqVein( rawData.get( JType.VEIN_BLACKLIST ), localData.get( JType.VEIN_BLACKLIST ) );

        if( jTypes.contains( JType.REQ_CRAFT ) )
            updateReqSkills( rawData.get( JType.REQ_CRAFT ), localData.get( JType.REQ_CRAFT ) );
    }

    private static void createData( File dataFile, String fileName )
    {
        try     //create template data file
        {
            dataFile.getParentFile().mkdir();
            dataFile.createNewFile();
        }
        catch( IOException e )
        {
            LogHandler.LOGGER.error( "Could not create template json config!", dataFile.getPath(), e );
        }

        try( InputStream inputStream = ProjectMMOMod.class.getResourceAsStream( hardDataPath + fileName );
             FileOutputStream outputStream = new FileOutputStream( dataFile ); )
        {
            IOUtils.copy( inputStream, outputStream );
        }
        catch( IOException e )
        {
            LogHandler.LOGGER.error( "Error copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath(), e );
        }
    }

    private static boolean checkValidSkills( Map<String, Object> theMap )
    {
        boolean anyValidSkills = false;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) != 0 )
                anyValidSkills = true;
            else
                LogHandler.LOGGER.info( "Invalid skill " + key + " level " + (double) theMap.get( key ) );
        }

        return anyValidSkills;
    }

    private static boolean checkValidEffects( Map<String, Object> theMap )
    {
        boolean anyValidEffects = false;

        for( String key : theMap.keySet() )
        {
            Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( key ) );
            if( !effect.equals( invalidEffect ) )
                anyValidEffects = true;
            else
                LogHandler.LOGGER.info( "Invalid effect " + key );
        }

        return anyValidEffects;
    }

    private static void updateReqSkills( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( checkValidSkills( element.getValue() ) )
            {
                if(  !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        if( Skill.getInt( entry.getKey() ) != 0 )
                            output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                        else
                            LogHandler.LOGGER.error( entry.getKey() + " is either not a valid skill, or not 1 or above!" );
                    }
                    else
                        LogHandler.LOGGER.error( entry.getValue() + " is not a Double!" );
                }
            }
            else
                LogHandler.LOGGER.error( "No valid skills, cannot add " + element.getKey() );
        };
    }

    private static void updateReqEffects( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( checkValidEffects( element.getValue() ) )
            {
                if(  !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        Potion potion = ForgeRegistries.POTION_TYPES.getValue( XP.getResLoc( entry.getKey() ) );

                        if( !potion.equals( invalidEffect ) && (double) entry.getValue() >= 0 && (double) entry.getValue() < 255 )
                            output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                        else
                            LogHandler.LOGGER.error( entry.getKey() + " is either not a effect skill, or below 0, or above 255!" );
                    }
                    else
                        LogHandler.LOGGER.error( entry.getValue() + " is not a Double!" );
                }
            }
            else
                LogHandler.LOGGER.error( "No valid effects, cannot add " + element.getKey() );
        };
    }

    private static void updateReqExtra( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( !XP.getItem( element.getKey() ).equals( Items.AIR ) )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        if( entry.getKey().equals( "extraChance" ) && (double) entry.getValue() > 0 )
                            output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                        else
                            LogHandler.LOGGER.error( element.getKey() + " is either not \"extraChance\", or not above 0!" );
                    }
                    else
                        LogHandler.LOGGER.error( element.getKey() + " is not a Double!" );
                }
            }
            else
                LogHandler.LOGGER.info( "Could not load inexistant item " + element.getKey() );
        };
    }

    private static void updateReqSalvage( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( !XP.getItem( element.getKey() ).equals( Items.AIR ) )
            {
                boolean failed = false;
                Map<String, Object> inMap = element.getValue();

                if( !( inMap.containsKey( "salvageItem" ) && inMap.get( "salvageItem" ) instanceof String ) )
                {
                    LogHandler.LOGGER.error( "Failed to load Salvage Item " + element.getKey() + " \"salvageItem\" is invalid" );
                    failed = true;
                }
                else if( XP.getItem( (String) inMap.get( "salvageItem" ) ).equals( Items.AIR ) )
                {
                    LogHandler.LOGGER.error( "Failed to load Salvage Item " + element.getKey() + " \"salvageItem\" item does not exist" );
                    failed = true;
                }

                if( !( inMap.containsKey( "salvageMax" ) && inMap.get( "salvageMax" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"salvageMax\" is invalid, loading default value 1 item" );
                    inMap.put( "salvageMax", 1D );
                }

                if( !( inMap.containsKey( "baseChance" ) && inMap.get( "baseChance" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"baseChance\" is invalid, loading default value 50%" );
                    inMap.put( "baseChance", 50D );
                }

                if( !( inMap.containsKey( "chancePerLevel" ) && inMap.get( "chancePerLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"chancePerLevel\" is invalid, loading default value 0%" );
                    inMap.put( "chancePerLevel", 0D );
                }

                if( !( inMap.containsKey( "maxChance" ) && inMap.get( "maxChance" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"maxChance\" is invalid, loading default value 80%" );
                    inMap.put( "maxChance", 80D );
                }

                if( !( inMap.containsKey( "xpPerItem" ) && inMap.get( "xpPerItem" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"xpPerItem\" is invalid, loading default value 0xp" );
                    inMap.put( "xpPerItem", 0D );
                }

                if( !( inMap.containsKey( "levelReq" ) && inMap.get( "levelReq" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Salvage Item " + element.getKey() + " \"levelReq\" is invalid, loading default value 1 level" );
                    inMap.put( "levelReq", 1D );
                }

                if( !failed )
                {
                    if( !output.containsKey( element.getKey() ) )
                        output.put( element.getKey(), new HashMap<>() );
                    Map<String, Object> outMap = output.get( element.getKey() );
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

                    Map<String, Map<String, Object>> localSalvagesFrom = localData.get( JType.SALVAGE_FROM );

                    if( !localSalvagesFrom.containsKey( salvageItem ) )
                        localSalvagesFrom.put( salvageItem, new HashMap<>() );

                    localSalvagesFrom.get( salvageItem ).put( element.getKey(), salvageMax );
                }
            }
            else
                LogHandler.LOGGER.info( "Could not load inexistant item " + element.getKey() );
        };
    }

    private static void updateReqVein( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            Map<String, Object> inMap = element.getValue();

            if( !output.containsKey( element.getKey() ) )
                output.put( element.getKey(), new HashMap<>() );

            for( Map.Entry<String, Object> entry : inMap.entrySet() )
            {
                if( XP.getItem( entry.getKey() ).equals( Items.AIR ) )
                    LogHandler.LOGGER.info( "Could not load inexistant item " + entry.getKey() + " into Vein Blacklist" );
                else
                    output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
            }
        };
    }

    private static void updateEntityItem( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            output.put( element.getKey(), new HashMap<>() );
            for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
            {
                if( !XP.getItem( entry.getKey() ).equals( Items.AIR ) )
                    output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                else
                    LogHandler.LOGGER.info( "Could not load inexistant item " + element.getKey() );
            }
        };
    }

    private static void updateReqfishPool( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            Item item = XP.getItem( element.getKey() );
            if( !item.equals( Items.AIR ) )
            {
                Map<String, Object> inMap = element.getValue();

                if( !( inMap.containsKey( "startWeight" ) && inMap.get( "startWeight" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"startWeight\" is invalid, loading default value 1" );
                    inMap.put( "startWeight", 1D );
                }

                if( !( inMap.containsKey( "startLevel" ) && inMap.get( "startLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"startLevel\" is invalid, loading default value level 1" );
                    inMap.put( "startLevel", 1D );
                }

                if( !( inMap.containsKey( "endWeight" ) && inMap.get( "endWeight" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"endWeight\" is invalid, loading default value 1" );
                    inMap.put( "endWeight", 1D );
                }

                if( !( inMap.containsKey( "endLevel" ) && inMap.get( "endLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"endLevel\" is invalid, loading default value level 1" );
                    inMap.put( "endLevel", 1D );
                }

                if( !( inMap.containsKey( "minCount" ) && inMap.get( "minCount" ) instanceof Double ) )
                {
//                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"minCount\" is invalid, loading default value 1 item" );
                    inMap.put( "minCount", 1D );
                }
                else if( (double) inMap.get( "minCount" ) > item.getMaxStackSize() )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"minCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "minCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "maxCount" ) && inMap.get( "maxCount" ) instanceof Double ) )
                {
//                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"maxCount\" is invalid, loading default value 1" );
                    inMap.put( "maxCount", 1D );
                }
                else if( (double) inMap.get( "maxCount" ) > item.getMaxStackSize() )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"maxCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "maxCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "enchantLevelReq" ) && inMap.get( "enchantLevelReq" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"enchantLevelReq\" is invalid, loading default value level 1" );
                    inMap.put( "enchantLevelReq", 1D );
                }

                if( !( inMap.containsKey( "xp" ) && inMap.get( "xp" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"xp\" is invalid, loading default value 1xp" );
                    inMap.put( "xp", 1D );
                }

                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                Map<String, Object> outMap = output.get( element.getKey() );
                double startWeight = (double) inMap.get( "startWeight" );
                double startLevel = (double) inMap.get( "startLevel" );
                double endWeight = (double) inMap.get( "endWeight" );
                double endLevel = (double) inMap.get( "endLevel" );
                double minCount = (double) inMap.get( "minCount" );
                double maxCount = (double) inMap.get( "maxCount" );
                double enchantLevelReq = (double) inMap.get( "enchantLevelReq" );
                double xp = (double) inMap.get( "xp" );

                if( endWeight < 0 )
                    outMap.put( "endWeight", 0D );
                else
                    outMap.put( "endWeight", endWeight );

                if( startWeight > endWeight )
                    startWeight = endWeight;

                if( startWeight < 0 )
                    outMap.put( "startWeight", 0D );
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
                LogHandler.LOGGER.info( "Could not load inexistant item " + element.getKey() );
        };
    }

    private static void updateReqFishEnchantPool( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( element.getKey() ) );
            if( !enchant.equals( invalidEnchant ) )
            {
                Map<String, Object> inMap = element.getValue();

                if( !( inMap.containsKey( "levelReq" ) && inMap.get( "levelReq" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"levelReq\" is invalid, loading default value 1" );
                    inMap.put( "levelReq", 1D );
                }

                if( !( inMap.containsKey( "levelPerLevel" ) && inMap.get( "levelPerLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"levelPerLevel\" is invalid, loading default value 0" );
                    inMap.put( "levelPerLevel", 0D );
                }

                if( !( inMap.containsKey( "chancePerLevel" ) && inMap.get( "chancePerLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"chancePerLevel\" is invalid, loading default value 0" );
                    inMap.put( "chancePerLevel", 0D );
                }

                if( !( inMap.containsKey( "maxChance" ) && inMap.get( "maxChance" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"maxChance\" is invalid, loading default value 80%" );
                    inMap.put( "maxChance", 80D );
                }

                if( !( inMap.containsKey( "maxLevel" ) && inMap.get( "maxLevel" ) instanceof Double ) )
                {
                    LogHandler.LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"maxLevel\" is invalid, loading default value " + enchant.getMaxLevel() );
                    inMap.put( "maxLevel", (double) enchant.getMaxLevel() );
                }


                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );
                Map<String, Object> outMap = output.get( element.getKey() );
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
                LogHandler.LOGGER.info( "Could not load inexistant enchant " + element.getKey() );
        };
    }

    private static void updateReqAttributes( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( checkValidAttributes( element.getValue() ) )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
                {
                    if( validAttributes.contains( entry.getKey() ) )
                        output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                    else
                        LogHandler.LOGGER.error( "Invalid attribute " + entry.getKey() );
                }
            }
            else
                LogHandler.LOGGER.error( "No valid attributes, cannot add " + element.getKey() );

        };
    }

    private static void updateCommand( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( Skill.getInt( element.getKey() ) != 0 )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        if( (double) entry.getValue() >= 1 )
                            output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                        else
                            output.get( element.getKey() ).put( entry.getKey(), 1D );
                    }
                    else
                        LogHandler.LOGGER.error( "Invalid level " + entry.getValue() );
                }
            }
            else
                LogHandler.LOGGER.error( "Invalid skill \"" + element.getKey() + "\" in Level Up Command" );
        };
    }

    private static void updateSpecific( Map<String, Map<String, Object>> input, Map<String, Map<String, Object>> output )
    {
        for( Map.Entry<String, Map<String, Object>> element : input.entrySet() )
        {
            if( !output.containsKey( element.getKey() ) )
                output.put( element.getKey(), new HashMap<>() );

            for( Map.Entry<String, Object> entry : element.getValue().entrySet() )
            {
                output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
            }
        };
    }

    private static boolean checkValidAttributes( Map<String, Object> theMap )
    {
        boolean anyValidAttributes = false;

        for( String key : theMap.keySet() )
        {
            if( validAttributes.contains( key ) )
                anyValidAttributes = true;
            else
                LogHandler.LOGGER.info( "Invalid attribute " + key );
        }

        return anyValidAttributes;
    }
}