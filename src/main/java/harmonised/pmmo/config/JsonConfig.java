package harmonised.pmmo.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class JsonConfig
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static Gson gson = new Gson();
    public static final Type mapType = new TypeToken<Map<String, Map<String, Double>>>(){}.getType();
    public static final Type mapType2 = new TypeToken<Map<String, Map<String, Map<String, Double>>>>(){}.getType();

    public static Set<JType> jTypes;
    private static Map<JType, Map<String, Map<String, Double>>> rawData = new HashMap<>();
    private static Map<JType, Map<String, Map<String, Map<String, Double>>>> rawData2 = new HashMap<>();

    public static Map<JType, Map<String, Map<String, Double>>> localData = new HashMap<>();
    public static Map<JType, Map<String, Map<String, Map<String, Double>>>> localData2 = new HashMap<>();

    public static Map<JType, Map<String, Map<String, Double>>> data = new HashMap<>();
    public static Map<JType, Map<String, Map<String, Map<String, Double>>>> data2 = new HashMap<>();

    private static final ArrayList<String> validAttributes = new ArrayList<>();
    private static final ArrayList<String> validFishEnchantInfo = new ArrayList<>();
    private static final String dataPath = "pmmo/";
    private static final String hardDataPath = "/assets/pmmo/util/";
    public static final Set<JType> jTypes2 = new HashSet<>();

    public static void init()
    {
        jTypes2.add( JType.SALVAGE );
        jTypes2.add( JType.SALVAGE_FROM );
        jTypes2.add( JType.TREASURE );
        jTypes2.add( JType.TREASURE_FROM );

        validAttributes.add( "speedBonus" );
        validAttributes.add( "hpBonus" );
        validAttributes.add( "damageBonus" );

        initMaps();         //populate maps
        initJTypes();       //check (from config) which JTypes should be read from data
        initData();         //copy over defaults if files aren't found (to do: check if valid json)
        readRawData();      //read in the data from /config/pmmo/
        processRawData();   //turn Raw data into Usable data

        JsonConfig.data = localData;
        JsonConfig.data2 = localData2;
    }

    private static void initMaps()
    {
        initMap( localData );
        initMap( data );
        initMap2( localData2 );
        initMap2( data2 );
    }

    public static void initMap( Map<JType, Map<String, Map<String, Double>>> map )
    {
        for( Map.Entry<JType, Integer> entry : JType.jTypeMap.entrySet() )
        {
            if( !jTypes2.contains( entry.getKey() ) )
                map.put( entry.getKey(), new HashMap<>() );
        }
    }

    public static void initMap2( Map<JType, Map<String, Map<String, Map<String, Double>>>> map )
    {
        for( JType jType2 : jTypes2 )
        {
            map.put( jType2, new HashMap<>() );
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

        if( Config.forgeConfig.brewingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_BREW );

        if( Config.forgeConfig.growingXpEnabled.get() )
            jTypes.add( JType.XP_VALUE_GROW );

        if( Config.forgeConfig.placeReqEnabled.get() )
            jTypes.add( JType.REQ_PLACE );

        if( Config.forgeConfig.breakReqEnabled.get() )
            jTypes.add( JType.REQ_BREAK );

        if( Config.forgeConfig.biomeReqEnabled.get() )
            jTypes.add( JType.REQ_BIOME );

        if( Config.forgeConfig.negativeBiomeEffectEnabled.get() )
            jTypes.add( JType.BIOME_EFFECT_NEGATIVE );

        if( Config.forgeConfig.positiveBiomeEffectEnabled.get() )
            jTypes.add( JType.BIOME_EFFECT_POSITIVE );

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

        if( Config.forgeConfig.brewingEnabled.get() )
            jTypes.add( JType.INFO_BREW );

        if( Config.forgeConfig.salvageEnabled.get() )
            jTypes.add( JType.SALVAGE );

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

        if( Config.forgeConfig.craftReqEnabled.get() )
            jTypes.add( JType.REQ_CRAFT );

        if( Config.forgeConfig.treasureEnabled.get() )
            jTypes.add( JType.TREASURE );

        jTypes.add( JType.BLOCK_SPECIFIC );
        jTypes.add( JType.PLAYER_SPECIFIC );
        jTypes.add( JType.VEIN_BLACKLIST );
        jTypes.add( JType.XP_VALUE_TRIGGER );
        jTypes.add( JType.XP_BONUS_DIMENSION );
        jTypes.add( JType.XP_MULTIPLIER_DIMENSION );
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
        rawData2 = new HashMap<>();
        File file;
        String fileName;

        for( JType jType : jTypes )
        {
            fileName = jType.name().toLowerCase() + ".json";
            file = FMLPaths.CONFIGDIR.get().resolve( dataPath + fileName ).toFile();

            try
            (
                InputStream input = new FileInputStream( file.getPath() );
                Reader reader = new BufferedReader( new InputStreamReader( input ) )
            )
            {
                if( jTypes2.contains( jType ) )
                    rawData2.put( jType, gson.fromJson( reader, mapType2 ) );
                else
                    rawData.put( jType, gson.fromJson( reader, mapType ) );
            }
            catch (Exception e)
            {
                LOGGER.error( "ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + dataPath + fileName, e );
                if( jTypes2.contains( jType ) )
                    rawData2.put( jType, new HashMap<>() );
                else
                    rawData.put( jType, new HashMap<>() );
            }
        }
    }

    private static void processRawData()
    {
        if( jTypes.contains( JType.REQ_WEAR ) )
            updateDataSkills( JType.REQ_WEAR, false );

        if( jTypes.contains( JType.REQ_TOOL ) )
            updateDataSkills( JType.REQ_TOOL, false );

        if( jTypes.contains( JType.REQ_WEAPON ) )
            updateDataSkills( JType.REQ_WEAPON, false );

        if( jTypes.contains( JType.REQ_USE ) )
            updateDataSkills( JType.REQ_USE, false );

        if( jTypes.contains( JType.REQ_PLACE ) )
            updateDataSkills( JType.REQ_PLACE, false );

        if( jTypes.contains( JType.REQ_BREAK ) )
            updateDataSkills( JType.REQ_BREAK, false );

        if( jTypes.contains( JType.REQ_BIOME ) )
            updateDataSkills( JType.REQ_BIOME, false );

        if( jTypes.contains( JType.REQ_KILL ) )
            updateDataSkills( JType.REQ_KILL, false );

        if( jTypes.contains( JType.REQ_CRAFT ) )
            updateDataSkills( JType.REQ_CRAFT, false );

        if( jTypes.contains( JType.XP_VALUE_GENERAL ) )
            updateDataSkills( JType.XP_VALUE_GENERAL, false );

        if( jTypes.contains( JType.XP_VALUE_BREAK ) )
            updateDataSkills( JType.XP_VALUE_BREAK, false );

        if( jTypes.contains( JType.XP_VALUE_CRAFT ) )
            updateDataSkills( JType.XP_VALUE_CRAFT, false );

        if( jTypes.contains( JType.XP_VALUE_BREED ) )
            updateDataSkills( JType.XP_VALUE_BREED, false );

        if( jTypes.contains( JType.XP_VALUE_TAME ) )
            updateDataSkills( JType.XP_VALUE_TAME, false );

        if( jTypes.contains( JType.XP_VALUE_SMELT ) )
            updateDataSkills( JType.XP_VALUE_SMELT, false );

        if( jTypes.contains( JType.XP_VALUE_COOK ) )
            updateDataSkills( JType.XP_VALUE_COOK, false );

        if( jTypes.contains( JType.XP_VALUE_KILL ) )
            updateDataSkills( JType.XP_VALUE_KILL, false );

        if( jTypes.contains( JType.XP_VALUE_BREW ) )
            updateDataSkills( JType.XP_VALUE_BREW, false );

        if( jTypes.contains( JType.XP_VALUE_GROW ) )
            updateDataSkills( JType.XP_VALUE_GROW, false );

        if( jTypes.contains( JType.XP_VALUE_TRIGGER ) )
            updateDataSkills( JType.XP_VALUE_TRIGGER, true );

        if( jTypes.contains( JType.INFO_ORE ) )
            updateDataExtra( rawData.get( JType.INFO_ORE ), localData.get( JType.INFO_ORE ) );

        if( jTypes.contains( JType.INFO_LOG ) )
            updateDataExtra( rawData.get( JType.INFO_LOG ), localData.get( JType.INFO_LOG ) );

        if( jTypes.contains( JType.INFO_PLANT ) )
            updateDataExtra( rawData.get( JType.INFO_PLANT ), localData.get( JType.INFO_PLANT ) );

        if( jTypes.contains( JType.INFO_SMELT ) )
            updateDataExtra( rawData.get( JType.INFO_SMELT ), localData.get( JType.INFO_SMELT ) );

        if( jTypes.contains( JType.INFO_COOK ) )
            updateDataExtra( rawData.get( JType.INFO_COOK ), localData.get( JType.INFO_COOK ) );

        if( jTypes.contains( JType.INFO_BREW ) )
            updateDataExtra( rawData.get( JType.INFO_BREW ), localData.get( JType.INFO_BREW ) );

        if( jTypes.contains( JType.BIOME_EFFECT_NEGATIVE ) )
            updateDataEffects( rawData.get( JType.BIOME_EFFECT_NEGATIVE ), localData.get( JType.BIOME_EFFECT_NEGATIVE ) );

        if( jTypes.contains( JType.BIOME_EFFECT_POSITIVE ) )
            updateDataEffects( rawData.get( JType.BIOME_EFFECT_POSITIVE ), localData.get( JType.BIOME_EFFECT_POSITIVE ) );

        if( jTypes.contains( JType.BIOME_MOB_MULTIPLIER ) )
            updateDataAttributes( rawData.get( JType.BIOME_MOB_MULTIPLIER ), localData.get( JType.BIOME_MOB_MULTIPLIER ) );

        if( jTypes.contains( JType.XP_BONUS_BIOME ) )
            updateDataSkills( JType.XP_BONUS_BIOME, false );

        if( jTypes.contains( JType.XP_BONUS_HELD ) )
            updateDataSkills( JType.XP_BONUS_HELD, false );

        if( jTypes.contains( JType.XP_BONUS_WORN ) )
            updateDataSkills( JType.XP_BONUS_WORN, false );

        if( jTypes.contains( JType.SALVAGE ) )
            updateDataSalvage( rawData2.get( JType.SALVAGE ), localData2.get( JType.SALVAGE ) );

        if( jTypes.contains( JType.FISH_POOL ) )
            updateDataFishPool( rawData.get( JType.FISH_POOL ), localData.get( JType.FISH_POOL ) );

        if( jTypes.contains( JType.FISH_ENCHANT_POOL ) )
            updateDataFishEnchantPool( rawData.get( JType.FISH_ENCHANT_POOL ), localData.get( JType.FISH_ENCHANT_POOL ) );

        if( jTypes.contains( JType.MOB_RARE_DROP ) )
            updateDataEntityItem( rawData.get( JType.MOB_RARE_DROP ), localData.get( JType.MOB_RARE_DROP ) );

        if( jTypes.contains( JType.LEVEL_UP_COMMAND ) )
            updateDataCommand( rawData.get( JType.LEVEL_UP_COMMAND ), localData.get( JType.LEVEL_UP_COMMAND ) );

        if( jTypes.contains( JType.TREASURE ) )
            updateDataTreasure( rawData2.get( JType.TREASURE ), localData2.get( JType.TREASURE ) );

        if( jTypes.contains( JType.BLOCK_SPECIFIC ) )
            updateDataSpecific( rawData.get( JType.BLOCK_SPECIFIC ), localData.get( JType.BLOCK_SPECIFIC ) );

        if( jTypes.contains( JType.PLAYER_SPECIFIC ) )
            updateDataSpecific( rawData.get( JType.PLAYER_SPECIFIC ), localData.get( JType.PLAYER_SPECIFIC ) );

        if( jTypes.contains( JType.VEIN_BLACKLIST ) )
            updateDataVein( rawData.get( JType.VEIN_BLACKLIST ), localData.get( JType.VEIN_BLACKLIST ) );

        if( jTypes.contains(  JType.XP_BONUS_DIMENSION ) )
            updateDataSkills( JType.XP_BONUS_DIMENSION, true );

        if( jTypes.contains(  JType.XP_MULTIPLIER_DIMENSION ) )
            updateDataSkills( JType.XP_MULTIPLIER_DIMENSION, true );
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
            LOGGER.error( "Could not create template json config!", dataFile.getPath(), e );
        }

        try( InputStream inputStream = ProjectMMOMod.class.getResourceAsStream( hardDataPath + fileName );
             FileOutputStream outputStream = new FileOutputStream( dataFile ); )
        {
            LOGGER.debug( "Copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath() );
            IOUtils.copy( inputStream, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath(), e );
        }
    }

    private static boolean checkValidSkills( Map<String, Double> theMap )
    {
        boolean anyValidSkills = false;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) != 0 )
                anyValidSkills = true;
            else
                LOGGER.info( "Invalid skill " + key + " level " + theMap.get( key ) );
        }

        return anyValidSkills;
    }

    private static boolean checkValidEffects( Map<String, Double> theMap )
    {
        boolean anyValidEffects = false;

        for( String key : theMap.keySet() )
        {
            Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( key ) );
            if( effect != null )
                anyValidEffects = true;
            else
                LOGGER.info( "Invalid effect " + key );
        }

        return anyValidEffects;
    }

    private static void updateDataSkills( JType jType, boolean ignoreValidCheck )
    {
        Map<String, Map<String, Double>> input = rawData.get( jType );
        Map<String, Map<String, Double>> output = localData.get( jType );

        LOGGER.debug( "Processing PMMO Data: Skills, Type: " + jType );
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( ignoreValidCheck || !XP.getItem( element.getKey() ).equals( Items.AIR ) || validEntity( element.getKey() ) || validBiome( element.getKey() ) ) //skip items that don't exist in current modlist
            {
                if( checkValidSkills( element.getValue() ) )
                {
                    if(  !output.containsKey( element.getKey() ) )
                        output.put( element.getKey(), new HashMap<>() );

                    for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
                    {
                        if( Skill.getInt( entry.getKey() ) != 0 )
                            output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                        else
                            LOGGER.error( entry.getKey() + " is either not a valid skill, or not 1 or above!" );
                    }
                }
                else
                    LOGGER.error( "No valid skills, cannot add " + element.getKey() );
            }
            else
                LOGGER.error( "Inexistant key, cannot add " + element.getKey() );
        }
    }

    private static void updateDataEffects( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( checkValidEffects( element.getValue() ) )
            {
                if(  !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
                {
                    Potion potion = ForgeRegistries.POTION_TYPES.getValue( XP.getResLoc( entry.getKey() ) );
                    if( potion != null && entry.getValue() >= 0 && entry.getValue() < 255 )
                        output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                    else
                        LOGGER.error( entry.getKey() + " is either not a effect skill, or below 0, or above 255!" );
                }
            }
            else
                LOGGER.error( "No valid effects, cannot add " + element.getKey() );
        }
    }

    private static void updateDataExtra( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( !XP.getItem( element.getKey() ).equals( Items.AIR ) )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
                {
                    if( entry.getKey().equals( "extraChance" ) && entry.getValue() > 0 )
                        output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                    else
                        LOGGER.error( element.getKey() + " is either not \"extraChance\", or not above 0!" );
                }
            }
            else
                LOGGER.info( "Could not load inexistant item " + element.getKey() );
        }
    }

    private static void updateDataVein( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            Map<String, Double> inMap = element.getValue();

            if( !output.containsKey( element.getKey() ) )
                output.put( element.getKey(), new HashMap<>() );

            for( Map.Entry<String, Double> entry : inMap.entrySet() )
            {
                if( XP.getItem( entry.getKey() ).equals( Items.AIR ) )
                    LOGGER.info( "Could not load inexistant item " + entry.getKey() + " into Vein Blacklist" );
                else
                    output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
            }
        }
    }

    private static void updateDataEntityItem( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            output.put( element.getKey(), new HashMap<>() );
            for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
            {
                if( !XP.getItem( entry.getKey() ).equals( Items.AIR ) )
                    output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                else
                    LOGGER.info( "Could not load inexistant item " + element.getKey() );
            }
        }
    }

    private static void updateDataFishPool( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            Item item = XP.getItem( element.getKey() );
            if( !item.equals( Items.AIR ) )
            {
                Map<String, Double> inMap = element.getValue();

                if( !( inMap.containsKey( "startWeight" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"startWeight\" is invalid, loading default value 1" );
                    inMap.put( "startWeight", 1D );
                }

                if( !( inMap.containsKey( "startLevel" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"startLevel\" is invalid, loading default value level 1" );
                    inMap.put( "startLevel", 1D );
                }

                if( !( inMap.containsKey( "endWeight" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"endWeight\" is invalid, loading default value 1" );
                    inMap.put( "endWeight", 1D );
                }

                if( !( inMap.containsKey( "endLevel" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"endLevel\" is invalid, loading default value level 1" );
                    inMap.put( "endLevel", 1D );
                }

                if( !( inMap.containsKey( "minCount" ) ) )
                {
//                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"minCount\" is invalid, loading default value 1 item" );
                    inMap.put( "minCount", 1D );
                }
                else if( inMap.get( "minCount" ) > item.getMaxStackSize() )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"minCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "minCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "maxCount" ) ) )
                {
//                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"maxCount\" is invalid, loading default value 1" );
                    inMap.put( "maxCount", 1D );
                }
                else if( inMap.get( "maxCount" ) > item.getMaxStackSize() )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"maxCount\" is above Max Stack Size, loading default value 1 item" );
                    inMap.put( "maxCount", (double) item.getMaxStackSize() );
                }

                if( !( inMap.containsKey( "enchantLevelReq" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"enchantLevelReq\" is invalid, loading default value level 1" );
                    inMap.put( "enchantLevelReq", 1D );
                }

                if( !( inMap.containsKey( "xp" ) ) )
                {
                    LOGGER.info( "Error loading Fish Pool Item " + element.getKey() + " \"xp\" is invalid, loading default value 1xp" );
                    inMap.put( "xp", 1D );
                }

                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                Map<String, Double> outMap = output.get( element.getKey() );
                double startWeight = inMap.get( "startWeight" );
                double startLevel = inMap.get( "startLevel" );
                double endWeight = inMap.get( "endWeight" );
                double endLevel = inMap.get( "endLevel" );
                double minCount = inMap.get( "minCount" );
                double maxCount = inMap.get( "maxCount" );
                double enchantLevelReq = inMap.get( "enchantLevelReq" );
                double xp = inMap.get( "xp" );

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
                LOGGER.info( "Could not load inexistant item " + element.getKey() );
        }
    }

    private static void updateDataFishEnchantPool( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( element.getKey() ) );
            if( enchant != null )
            {
                Map<String, Double> inMap = element.getValue();

                if( !( inMap.containsKey( "levelReq" ) ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"levelReq\" is invalid, loading default value 1" );
                    inMap.put( "levelReq", 1D );
                }

                if( !( inMap.containsKey( "levelPerLevel" ) ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"levelPerLevel\" is invalid, loading default value 0" );
                    inMap.put( "levelPerLevel", 0D );
                }

                if( !( inMap.containsKey( "chancePerLevel" ) ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"chancePerLevel\" is invalid, loading default value 0" );
                    inMap.put( "chancePerLevel", 0D );
                }

                if( !( inMap.containsKey( "maxChance" ) ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"maxChance\" is invalid, loading default value 80%" );
                    inMap.put( "maxChance", 80D );
                }

                if( !( inMap.containsKey( "maxLevel" ) ) )
                {
                    LOGGER.info( "Error loading Fish Enchant Pool Item " + element.getKey() + " \"maxLevel\" is invalid, loading default value " + enchant.getMaxLevel() );
                    inMap.put( "maxLevel", (double) enchant.getMaxLevel() );
                }


                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );
                Map<String, Double> outMap = output.get( element.getKey() );
                double levelReq = inMap.get( "levelReq" );
                double levelPerLevel = inMap.get( "levelPerLevel" );
                double chancePerLevel = inMap.get( "chancePerLevel" );
                double maxChance = inMap.get( "maxChance" );
                double maxLevel = inMap.get( "maxLevel" );

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
                    outMap.put( "maxLevel", (double) enchant.getMaxLevel() );
                else
                    outMap.put( "maxLevel", maxLevel );
            }
            else
                LOGGER.info( "Could not load inexistant enchant " + element.getKey() );
        }
    }

    private static void updateDataAttributes( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( checkValidAttributes( element.getValue() ) )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
                {
                    if( validAttributes.contains( entry.getKey() ) )
                        output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                    else
                        LOGGER.error( "Invalid attribute " + entry.getKey() );
                }
            }
            else
                LOGGER.error( "No valid attributes, cannot add " + element.getKey() );

        }
    }

    private static void updateDataCommand( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( Skill.getInt( element.getKey() ) != 0 )
            {
                if( !output.containsKey( element.getKey() ) )
                    output.put( element.getKey(), new HashMap<>() );

                for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
                {
                    if( entry.getValue() >= 1 )
                        output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
                    else
                        output.get( element.getKey() ).put( entry.getKey(), 1D );
                }
            }
            else
                LOGGER.error( "Invalid skill \"" + element.getKey() + "\" in Level Up Command" );
        }
    }

    private static void updateDataSpecific( Map<String, Map<String, Double>> input, Map<String, Map<String, Double>> output )
    {
        for( Map.Entry<String, Map<String, Double>> element : input.entrySet() )
        {
            if( !output.containsKey( element.getKey() ) )
                output.put( element.getKey(), new HashMap<>() );

            for( Map.Entry<String, Double> entry : element.getValue().entrySet() )
            {
                output.get( element.getKey() ).put( entry.getKey(), entry.getValue() );
            }
        }
    }

    private static boolean checkValidAttributes( Map<String, Double> theMap )
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

    private static void updateDataTreasure( Map<String, Map<String, Map<String, Double>>> input, Map<String, Map< String, Map<String, Double>>> output )
    {
        ResourceLocation blockResLoc;
        String itemResLoc;
        Map<String, Map<String, Double>> outputBlockMap;
        Map<String, Double> outputItemMap;
        Map<String, Double> inputItemMap;
        Item item;

        for( Map.Entry<String, Map<String, Map<String, Double>>> blockEntry : input.entrySet() )
        {
            blockResLoc = XP.getResLoc( blockEntry.getKey() );
            if( ForgeRegistries.BLOCKS.containsKey( blockResLoc ) )
            {
                output.put( blockEntry.getKey(), new HashMap<>() );
                outputBlockMap = output.get( blockEntry.getKey() );
                for( Map.Entry<String, Map<String, Double>> itemEntry : blockEntry.getValue().entrySet() )
                {
                    item = XP.getItem( itemEntry.getKey() );
                    if( !item.equals( Items.AIR ) )
                    {
                        itemResLoc = item.getRegistryName().toString();
                        inputItemMap = itemEntry.getValue();

                        if( !( inputItemMap.containsKey( "startChance" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing startChance Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 0.1" );
                            inputItemMap.put( "startChance", 0.1D );
                        }
                        if( !( inputItemMap.containsKey( "endChance" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing endChance Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 1" );
                            inputItemMap.put( "endChance", 1D );
                        }
                        if( !( inputItemMap.containsKey( "startLevel" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing startLevel Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 1" );
                            inputItemMap.put( "startLevel", 1D );
                        }
                        if( !( inputItemMap.containsKey( "endLevel" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing endLevel Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 100" );
                            inputItemMap.put( "endLevel", 100D );
                        }
                        if( !( inputItemMap.containsKey( "minCount" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing minCount Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 1" );
                            inputItemMap.put( "minCount", 1D );
                        }
                        if( !( inputItemMap.containsKey( "maxCount" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing maxCount Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 1" );
                            inputItemMap.put( "maxCount", 1D );
                        }
                        if( !( inputItemMap.containsKey( "xpPerItem" ) ) )
                        {
                            LOGGER.debug( "Invalid or Missing xpPerItem Block:" + blockEntry.getKey() + ", Item: " + itemEntry.getKey() + " in Treasure. Loading Default Value 1" );
                            inputItemMap.put( "xpPerItem", 1D );
                        }

                        outputItemMap = new HashMap<>();
                        double startChance = inputItemMap.get( "startChance" );
                        double endChance   = inputItemMap.get( "endChance" );
                        double startLevel  = inputItemMap.get( "startLevel" );
                        double endLevel    = inputItemMap.get( "endLevel" );
                        int minCount = (int) Math.floor( inputItemMap.get( "minCount" ) );
                        int maxCount = (int) Math.floor( inputItemMap.get( "maxCount" ) );
                        double xpPerItem = inputItemMap.get( "xpPerItem" );

                        outputItemMap.put( "startChance", startChance );
                        outputItemMap.put( "endChance", endChance );
                        outputItemMap.put( "startLevel", startLevel );
                        outputItemMap.put( "endLevel", endLevel );
                        outputItemMap.put( "minCount", (double) minCount );
                        outputItemMap.put( "maxCount", (double) maxCount );
                        outputItemMap.put( "xpPerItem", xpPerItem );

                        outputBlockMap.put( itemEntry.getKey(), outputItemMap );

                        Map<String, Map<String, Map<String, Double>>> localSalvagesFrom = localData2.get( JType.TREASURE_FROM );

                        if( !localSalvagesFrom.containsKey( itemResLoc ) )
                            localSalvagesFrom.put( itemResLoc, new HashMap<>() );
                        localSalvagesFrom.get( itemResLoc ).put( blockResLoc.toString(), outputItemMap );
                    }
                    else
                        LOGGER.debug( "Inexistant Item " + itemEntry.getKey() + " in Treasure" );
                }
            }
            else
                LOGGER.debug( "Inexistant Block " + blockEntry.getKey() + " in Treasure" );
        }
    }

    private static void updateDataSalvage( Map<String, Map<String, Map<String, Double>>> input, Map<String, Map< String, Map<String, Double>>> output )
    {
        Map<String, Map<String, Double>> outputSalvageFromItemMap;
        ResourceLocation salvageFromItemResLoc;
        String salvageToItemResLoc;
        Map<String, Double> salvageToItemMap;
        Item item;

        for( Map.Entry<String, Map<String, Map<String, Double>>> inputSalvageFromItemEntry : input.entrySet() )
        {
            salvageFromItemResLoc = XP.getResLoc( inputSalvageFromItemEntry.getKey() );
            if( !XP.getItem( salvageFromItemResLoc ).equals( Items.AIR ) )
            {
                output.put( salvageFromItemResLoc.toString(), new HashMap<>() );
                outputSalvageFromItemMap = output.get( salvageFromItemResLoc.toString() );
                for( Map.Entry<String, Map<String, Double>> inputSalvageToItemEntry : inputSalvageFromItemEntry.getValue().entrySet() )
                {
                    item = XP.getItem( inputSalvageToItemEntry.getKey() );
                    salvageToItemResLoc = item.getRegistryName().toString();
                    if( !item.equals( Items.AIR ) )
                    {
                        salvageToItemMap = inputSalvageToItemEntry.getValue();

                        if( !( salvageToItemMap.containsKey( "salvageMax" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"salvageMax\" is invalid, loading default value 1 item" );
                            salvageToItemMap.put( "salvageMax", 1D );
                        }

                        if( !( salvageToItemMap.containsKey( "baseChance" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"baseChance\" is invalid, loading default value 50%" );
                            salvageToItemMap.put( "baseChance", 50D );
                        }

                        if( !( salvageToItemMap.containsKey( "chancePerLevel" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"chancePerLevel\" is invalid, loading default value 0%" );
                            salvageToItemMap.put( "chancePerLevel", 0D );
                        }

                        if( !( salvageToItemMap.containsKey( "maxChance" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"maxChance\" is invalid, loading default value 80%" );
                            salvageToItemMap.put( "maxChance", 80D );
                        }

                        if( !( salvageToItemMap.containsKey( "xpPerItem" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"xpPerItem\" is invalid, loading default value 0xp" );
                            salvageToItemMap.put( "xpPerItem", 0D );
                        }

                        if( !( salvageToItemMap.containsKey( "levelReq" ) ) )
                        {
                            LOGGER.info( "Error loading Salvage Item " + inputSalvageToItemEntry.getKey() + " \"levelReq\" is invalid, loading default value 1 level" );
                            salvageToItemMap.put( "levelReq", 1D );
                        }

                        if( !outputSalvageFromItemMap.containsKey( inputSalvageToItemEntry.getKey() ) )
                            outputSalvageFromItemMap.put( inputSalvageToItemEntry.getKey(), new HashMap<>() );
                        Map<String, Double> outMap = outputSalvageFromItemMap.get( inputSalvageToItemEntry.getKey() );
                        double salvageMax = salvageToItemMap.get( "salvageMax" );
                        double levelReq = salvageToItemMap.get( "levelReq" );
                        double xpPerItem = salvageToItemMap.get( "xpPerItem" );
                        double baseChance = salvageToItemMap.get( "baseChance" );
                        double chancePerLevel = salvageToItemMap.get( "chancePerLevel" );
                        double maxChance = salvageToItemMap.get( "maxChance" );

                        if( salvageMax < 1 )
                            outMap.put( "salvageMax", 1D );
                        else
                            outMap.put( "salvageMax", salvageMax );

                        if( levelReq < 1 )
                            outMap.put( "levelReq", 1D );
                        else
                            outMap.put( "levelReq", levelReq );

                        if( xpPerItem < 0 )
                            outMap.put( "xpPerItem", 0D );
                        else
                            outMap.put( "xpPerItem", xpPerItem );

                        if( baseChance < 0 )
                            outMap.put( "baseChance", 0D );
                        else if( baseChance > 100 )
                            outMap.put( "baseChance", 100D );
                        else
                            outMap.put( "baseChance", baseChance );

                        if( chancePerLevel < 0 )
                            outMap.put( "chancePerLevel", 0D );
                        else if( chancePerLevel > 100 )
                            outMap.put( "chancePerLevel", 100D );
                        else
                            outMap.put( "chancePerLevel", chancePerLevel );

                        if( maxChance < 0 )
                            outMap.put( "maxChance", 0D );
                        else if( maxChance > 100 )
                            outMap.put( "maxChance", 100D );
                        else
                            outMap.put( "maxChance", maxChance );

                        Map<String, Map<String, Map<String, Double>>> localSalvagesFrom = localData2.get( JType.SALVAGE_FROM );

                        if( !localSalvagesFrom.containsKey( salvageToItemResLoc ) )
                            localSalvagesFrom.put( salvageToItemResLoc, new HashMap<>() );
                        localSalvagesFrom.get( salvageToItemResLoc ).put(  salvageFromItemResLoc.toString(), outMap );
                    }
                    else
                        LOGGER.debug( "Inexistant To Item " + inputSalvageToItemEntry.getKey() + " in Salvage" );
                }
            }
            else
                LOGGER.debug( "Inexistant From Item " + inputSalvageFromItemEntry.getKey() + " in Salvage" );
        }
    }

    public static boolean validEntity( String regKey )
    {
        return ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) );
    }

    public static boolean validBiome( String regKey )
    {
        return ForgeRegistries.BIOMES.containsKey( XP.getResLoc( regKey ) );
    }
}