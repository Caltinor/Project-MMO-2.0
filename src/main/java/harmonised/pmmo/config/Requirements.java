package harmonised.pmmo.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.item.Items;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class Requirements
{
    private static Map<String, Map<String, Object>> localWearReq = new HashMap<>();
    public static Map<String, Map<String, Object>> wearReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localToolReq = new HashMap<>();
    public static Map<String, Map<String, Object>> toolReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localWeaponReq = new HashMap<>();
    public static Map<String, Map<String, Object>> weaponReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localMobReq = new HashMap<>();
    public static Map<String, Map<String, Object>> mobReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localUseReq = new HashMap<>();
    public static Map<String, Map<String, Object>> useReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localPlaceReq = new HashMap<>();
    public static Map<String, Map<String, Object>> placeReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localBreakReq = new HashMap<>();
    public static Map<String, Map<String, Object>> breakReq = new HashMap<>();

    private static Map<String, Map<String, Object>> localXpValue = new HashMap<>();
    public static Map<String, Map<String, Object>> xpValue = new HashMap<>();

    private static Map<String, Map<String, Object>> localOreInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> oreInfo = new HashMap<>();

    private static Map<String, Map<String, Object>> locallogInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> logInfo = new HashMap<>();

    private static Map<String, Map<String, Object>> localPlantInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> plantInfo = new HashMap<>();

    private static Map<String, Map<String, Object>> localSalvageInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> salvageInfo = new HashMap<>();

    public static Map<String, Map<String, Object>> localSalvagesFrom = new HashMap<>();
    public static Map<String, Map<String, Object>> salvagesFrom = new HashMap<>();

    private static Map<String, Object> tempMap;
    private static String dataPath = "pmmo/data.json";
    private static String templateDataPath = "pmmo/data_template.json";
    private static String defaultDataPath = "/assets/pmmo/util/default_data.json";
    private static final Logger LOGGER = LogManager.getLogger();
    private static Requirements defaultReq, customReq;

    public static void init()
    {
        File templateData = FMLPaths.CONFIGDIR.get().resolve( templateDataPath ).toFile();
        File data = FMLPaths.CONFIGDIR.get().resolve( dataPath ).toFile();

        createData( templateData ); //always rewrite template data with hardcoded one
        if ( !data.exists() )   //If no data file, create one
            createData( data );

        defaultReq = Requirements.readFromFile( templateData.getPath() );
        customReq = Requirements.readFromFile( data.getPath() );

        if( Config.config.loadDefaultConfig.get() )
            updateFinal( defaultReq );
        updateFinal( customReq );
    }

    private static boolean checkValidSkills( Map<String, Object> theMap )
    {
        boolean anyValidSkills = false;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) != 0 )
                anyValidSkills = true;
        }

        return anyValidSkills;
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
                        if( Skill.getInt( entry.getKey() ) != 0 && (double) entry.getValue() > 0 )
                            outReq.get( key ).put( entry.getKey(), entry.getValue() );
                    }
                }
            }
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
                }
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

                    if( !localSalvagesFrom.containsKey( salvageItem ) )
                        localSalvagesFrom.put( salvageItem, new HashMap<>() );

                    localSalvagesFrom.get( salvageItem ).put( key, salvageMax );
                }
            }
        });
    }

    private static void updateFinal( Requirements req )
    {
        if( Config.config.wearReqEnabled.get() )
            updateReqSkills( req.wears, localWearReq );

        if( Config.config.toolReqEnabled.get() )
            updateReqSkills( req.tools, localToolReq );

        if( Config.config.weaponReqEnabled.get() )
            updateReqSkills( req.weapons, localWeaponReq );

//        updateReqSkills( req.mobs, mobReq );

        if( Config.config.useReqEnabled.get() )
            updateReqSkills( req.use, localUseReq );

        if( Config.config.xpValueEnabled.get() )
            updateReqSkills( req.xpValues, localXpValue );

        if( Config.config.placeReqEnabled.get() )
            updateReqSkills( req.placing, localPlaceReq );

        if( Config.config.breakReqEnabled.get() )
            updateReqSkills( req.breaking, localBreakReq );

        if( Config.config.oreEnabled.get() )
            updateReqExtra( req.ores, localOreInfo );

        if( Config.config.logEnabled.get() )
            updateReqExtra( req.logs, locallogInfo );

        if( Config.config.plantEnabled.get() )
            updateReqExtra( req.plants, localPlantInfo );

        if( Config.config.salvageEnabled.get() )
            updateReqSalvage( req.salvage, localSalvageInfo );

        resetRequirements();
    }

    public static void resetRequirements()
    {
        wearReq = localWearReq;
        toolReq = localToolReq;
        weaponReq = localWeaponReq;
        useReq = localUseReq;
        xpValue = localXpValue;
        placeReq = localPlaceReq;
        breakReq = localBreakReq;
        oreInfo = localOreInfo;
        logInfo = locallogInfo;
        plantInfo = localPlantInfo;
        salvageInfo = localSalvageInfo;
        salvagesFrom = localSalvagesFrom;
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
    private final Map<String, RequirementItem> xpValues = Maps.newHashMap();
    private final Map<String, RequirementItem> ores = Maps.newHashMap();
    private final Map<String, RequirementItem> logs = Maps.newHashMap();
    private final Map<String, RequirementItem> plants = Maps.newHashMap();
    private final Map<String, RequirementItem> salvage = Maps.newHashMap();

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
            deserializeGroup(obj, "xp_value", req.xpValues::put, context);
            deserializeGroup(obj, "ore", req.ores::put, context);
            deserializeGroup(obj, "log", req.logs::put, context);
            deserializeGroup(obj, "plant", req.plants::put, context);
            deserializeGroup(obj, "salvage", req.salvage::put, context);

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