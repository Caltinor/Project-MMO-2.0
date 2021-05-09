package harmonised.pmmo.network;

import com.google.gson.JsonObject;
import net.minecraft.client.util.JSONException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class WebHandler
{
    private static String readAll(Reader rd) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException
    {
        InputStream is = new URL( url ).openStream();
        try
        {
            BufferedReader rd = new BufferedReader(new InputStreamReader( is, Charset.forName("UTF-8" ) ) );
            String jsonText = readAll( rd );
//            JsonObject json = Js;
            return jsonText;
        } finally {
            is.close();
        }
    }

    public static void main(String[] args) throws IOException, JSONException
    {
        String json = readJsonFromUrl("https://graph.facebook.com/19292868552");
    }
}