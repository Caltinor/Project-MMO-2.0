package harmonised.pmmo.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Requirements
{
    public static Map<String, Map<String, Double>> wearReq = new HashMap<>();
    public static Map<String, Map<String, Double>> toolReq = new HashMap<>();
    public static Map<String, Map<String, Double>> weaponReq = new HashMap<>();
    public static Map<String, Map<String, Double>> mobReq = new HashMap<>();
    public static Map<String, Map<String, Double>> placeReq = new HashMap<>();
    public static Map<String, Map<String, Double>> breakReq = new HashMap<>();
    public static Map<String, Map<String, Double>> xpValue = new HashMap<>();
    public static Map<String, Map<String, Double>> oreInfo = new HashMap<>();
    public static Map<String, Map<String, Double>> logInfo = new HashMap<>();
    public static Map<String, Map<String, Double>> plantInfo = new HashMap<>();

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
        updateFinal( defaultReq );
        updateFinal( customReq );
    }
    
    private static boolean checkInvalidSkills( Map<String, Double> theMap )
    {
        boolean accepted = true;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) == 0 )
                accepted = false;
        }

        return accepted;
    }

    private static void updateFinal( Requirements req )
    {
        req.wears.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( wearReq.containsKey( key ) )
                    wearReq.replace( key, value.requirements );
                else
                    wearReq.put( key, value.requirements );
            }
        });

        req.tools.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( toolReq.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    toolReq.replace( key, value.requirements );
                else
                    toolReq.put( key, value.requirements );
            }
        });

        req.weapons.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( weaponReq.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    weaponReq.replace( key, value.requirements );
                else
                    weaponReq.put( key, value.requirements );
            }
        });

        req.mobs.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( mobReq.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    mobReq.replace( key, value.requirements );
                else
                    mobReq.put( key, value.requirements );
            }
        });

        req.placing.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( placeReq.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    placeReq.replace( key, value.requirements );
                else
                    placeReq.put( key, value.requirements );
            }
        });

        req.breaking.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( breakReq.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    breakReq.replace( key, value.requirements );
                else
                    breakReq.put( key, value.requirements );
            }
        });

        req.xpValues.forEach( (key, value) ->
        {
            if( checkInvalidSkills( value.requirements ) )
            {
                if( xpValue.containsKey( key ) && checkInvalidSkills( value.requirements ) )
                    xpValue.replace( key, value.requirements );
                else
                    xpValue.put( key, value.requirements );
            }
        });

        req.ores.forEach( (key, value) ->
        {
            if( oreInfo.containsKey( key ) )
                oreInfo.replace( key, value.requirements );
            else
                oreInfo.put( key, value.requirements );
        });

        req.logs.forEach( (key, value) ->
        {
            if( logInfo.containsKey( key ) )
                logInfo.replace( key, value.requirements );
            else
                logInfo.put( key, value.requirements );
        });

        req.plants.forEach( (key, value) ->
        {
            if( plantInfo.containsKey( key ) )
                plantInfo.replace( key, value.requirements );
            else
                plantInfo.put( key, value.requirements );
        });
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
        private final Map<String, Double> requirements = Maps.newHashMap();

        public HashMap<String, Double> getMap()
        {
            return new HashMap<>( requirements );
        }

        public double get(String registryName)
        {
            return requirements.get(registryName);
        }
    }

    private final Map<String, RequirementItem> wears = Maps.newHashMap();
    private final Map<String, RequirementItem> tools = Maps.newHashMap();
    private final Map<String, RequirementItem> weapons = Maps.newHashMap();
    private final Map<String, RequirementItem> mobs = Maps.newHashMap();
    private final Map<String, RequirementItem> placing = Maps.newHashMap();
    private final Map<String, RequirementItem> breaking = Maps.newHashMap();
    private final Map<String, RequirementItem> xpValues = Maps.newHashMap();
    private final Map<String, RequirementItem> ores = Maps.newHashMap();
    private final Map<String, RequirementItem> logs = Maps.newHashMap();
    private final Map<String, RequirementItem> plants = Maps.newHashMap();

//    public Map<String, Double> getWear(String registryName)
//    {
//        if( wears.containsKey( registryName ) )
//            return wears.get( registryName ).getMap();
//        else
//            return null;
//    }
//
//    public Map<String, Double> getTool(String registryName)
//    {
//        if( tools.containsKey( registryName ) )
//            return tools.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Double> getWeapon(String registryName)
//    {
//        if( weapons.containsKey( registryName ) )
//            return weapons.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Double> getXp(String registryName)
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
            deserializeGroup(obj, "place_requirement", req.placing::put, context);
            deserializeGroup(obj, "break_requirement", req.breaking::put, context);
            deserializeGroup(obj, "xp_value", req.xpValues::put, context);
            deserializeGroup(obj, "ore", req.ores::put, context);
            deserializeGroup(obj, "log", req.logs::put, context);
            deserializeGroup(obj, "plant", req.plants::put, context);

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
                Double values = entries.getValue().getAsDouble();
                item.requirements.put(name, values);
            }

            return item;
        }
    }
}