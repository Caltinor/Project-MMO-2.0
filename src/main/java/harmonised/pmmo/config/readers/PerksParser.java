package harmonised.pmmo.config.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.ProjectMMO;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLPaths;

public class PerksParser {
	
	//TODO convert this to a codec-based reader
	private static final String configFilePath = "pmmo/perks.json"; 
	private static final String srcFilePath = "/assets/pmmo/util/perks.json";
	
	private static final Type perkType = new TypeToken<Map<String, JsonObject>>(){}.getType();
	private static Gson gson = new Gson();

	
	public static void parsePerks() {
        File srcFile = FMLPaths.CONFIGDIR.get().resolve(configFilePath).toFile();

        if (!srcFile.exists())   //If no data file, create one
            createData(srcFile, srcFilePath);
		
        File file = FMLPaths.CONFIGDIR.get().resolve(configFilePath).toFile();
        Map<EventType, LinkedListMultimap<String, CompoundTag>> settings = new HashMap<>();
        try
            (
                InputStream input = new FileInputStream(file.getPath());
                Reader reader = new BufferedReader(new InputStreamReader(input))
           )
        {
        	Map<String, JsonObject> json = gson.fromJson(reader, perkType);
        	for (Map.Entry<String, JsonObject> map : json.entrySet()) {
        		EventType trigger = null;
        		//Find matching trigger for the raw strings entered. 
        		for (EventType pt :EventType.values()) {
        			if (pt.name().toUpperCase().equals(map.getKey()) ) {
        				trigger = pt;
        				break;
        			}
        		}
        		if (trigger == null) continue;
        		else {
        			MsLoggy.info("========================");
        			MsLoggy.info(trigger.name().toString());
        			MsLoggy.info("========================");
        			JsonObject entries = map.getValue();
        			LinkedListMultimap<String, CompoundTag> members = LinkedListMultimap.create();
        			for (String entryKey : entries.keySet()) {        				
        				JsonArray memList = entries.get(entryKey).getAsJsonArray();        				
        				for (int i = 0; i < memList.size(); i++) {
        					MsLoggy.info(entryKey+":"+memList.get(i).toString());
        					members.get(entryKey).add(tagFromJson(memList.get(i).getAsJsonObject()));
        				}
        			}
        			settings.put(trigger, members);    				
        		}
        	}
        	Core.get(LogicalSide.SERVER).getPerkRegistry().setSettings(settings);
        }
        catch(Exception e)
        {
            MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + configFilePath, e);
        }
    }
	
	private static void createData(File dataFile, String fileNameAndPath)
    {
        try     //create template data file
        {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
        }
        catch(IOException e)
        {
            MsLoggy.error("Could not create template json config!", dataFile.getPath(), e);
            return;
        }

        try(InputStream inputStream = ProjectMMO.class.getResourceAsStream(fileNameAndPath);
             FileOutputStream outputStream = new FileOutputStream(dataFile);)
        {
            MsLoggy.debug("Copying over perks.json json config to " + dataFile.getPath(), dataFile.getPath());
            IOUtils.copy(inputStream, outputStream);
        }
        catch(IOException e)
        {
            MsLoggy.error("Error copying over perks.json json config to " + dataFile.getPath(), dataFile.getPath(), e);
        }
    }
	
	
	private static CompoundTag tagFromJson(JsonObject json) {
		try {
			return TagParser.parseTag(json.toString());
		} catch(CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
}
