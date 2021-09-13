package harmonised.pmmo.network;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class WebHandler
{
    public static final Logger LOGGER = LogManager.getLogger();
    private static JsonStructure data;

    private static String readAll(Reader rd) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ( (cp = rd.read() ) != -1)
        {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static <T> T readJsonFromUrl( String url, Class<T> type ) throws IOException
    {
        InputStream is = new URL( url ).openStream();
        try
        {
            BufferedReader rd = new BufferedReader(new InputStreamReader( is, Charset.forName("UTF-8" ) ) );
            String jsonText = readAll( rd );
            return JsonConfig.gson.fromJson( jsonText, type );
        }
        finally
        {
            is.close();
        }
    }

    public class JsonStructure
    {
        public final Map<String, Object> objs = new HashMap<>();
    }

    public static void updateInfo()
    {
        try
        {
            JsonStructure newData = WebHandler.readJsonFromUrl( "https://raw.githubusercontent.com/Harmonised7/db/master/pmmo.json", WebHandler.JsonStructure.class );
            data = newData; //If didn't crash
        }
        catch( IOException e )
        {
            LOGGER.warn( "Could not connect to PMMO data server" );
        }
    }

    public static JsonStructure getWebData()
    {
        return data;
    }

    public static String getLatestVersion()
    {
        try
        {
            return (String) WebHandler.getWebData().objs.get( "latestVersion" + Reference.MC_VERSION );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    public static String getLatestMessage()
    {
        try
        {
            return (String) WebHandler.getWebData().objs.get( "latestMessage" + Reference.MC_VERSION );
        }
        catch( Exception e )
        {
            return null;
        }
    }
}