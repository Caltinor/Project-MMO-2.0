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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import harmonised.pmmo.ProjectMMO;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.CoreType;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.loading.FMLPaths;

public class CoreParser {
	private static final Gson gson = new Gson();

	public static final Type basicIntegerJsonType = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
	public static final Type basicDoubleJsonType = new TypeToken<Map<String, Map<String, Double>>>(){}.getType();
	
	public static final Type valueJsonType = new TypeToken<Map<String, Map<String, Map<String, Long>>>>(){}.getType();
	
	public static void init() {
		parseRequirements();
		parseXp_Values();
		parseCore();
	}
	
	private static void parseRequirements() {
		DATA_PATH path = DATA_PATH.REQS;
		String filename;
		File dataFile;
		for (ReqType type : ReqType.values()) {
			filename = type.name().toLowerCase() + ".json";
			dataFile = FMLPaths.CONFIGDIR.get().resolve(path.config_path + filename).toFile();
			
			if (!dataFile.exists())
				createData(dataFile, path, filename);
			
            try(InputStream input = new FileInputStream(dataFile.getPath());
                Reader reader = new BufferedReader(new InputStreamReader(input)))
            {
            		Map<String, Map<String, Integer>> rawMap = gson.fromJson(reader, basicIntegerJsonType);
            		for (Map.Entry<String, Map<String, Integer>> raw : rawMap.entrySet()) {
            			List<ResourceLocation> tagResults = new ArrayList<>();
            			if (raw.getKey().startsWith("#"))
            				tagResults = getTagMembers(raw.getKey().substring(1));
            			else
            				tagResults.add(new ResourceLocation(raw.getKey()));
            			for (ResourceLocation key : tagResults) {
            				SkillGates.setObjectSkillMap(type, key, raw.getValue());
            				MsLoggy.info(type.name()+": "+key.toString()+MsLoggy.mapToString(raw.getValue())+" loaded from config");
            			}
            		}
            }
            catch(Exception e)
            {
                MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
            }
        }
	}
	
	private static void parseXp_Values() {
		DATA_PATH path = DATA_PATH.EXP;
		String filename;
		File dataFile;
		for (XpValueDataType type : XpValueDataType.values()) {
			filename = type.name().toLowerCase() + ".json";
			dataFile = FMLPaths.CONFIGDIR.get().resolve(path.config_path + filename).toFile();
			
			if (!dataFile.exists())
				createData(dataFile, path, filename);
			
			try(InputStream input = new FileInputStream(dataFile.getPath());
	                Reader reader = new BufferedReader(new InputStreamReader(input)))
	            {
						//Distinguish between long and double type configs
						boolean isModifierType = false;
						Map<String, Map<String, Map<String, Long>>> rawValueMap = new HashMap<>();
						Map<String, Map<String, Double>> rawModifierMap = new HashMap<>();
						//Check for modifier types and fill the appropriate raw map for later evaluation
						for (XpValueDataType modType : XpValueDataType.modifierTypes) {
							if (modType.equals(type)) {
								rawModifierMap = gson.fromJson(reader, basicDoubleJsonType);
								isModifierType = true;
								break;
							}
						}
						if (!isModifierType)
							rawValueMap = new Gson().fromJson(reader, valueJsonType);
						/* Evaluate configs based on their types
						 * This works by only filling the maps of the appropriate type.
						 * The loop for the incorrect type will be empty and simply bypassed.
						 */
	            		for (Map.Entry<String, Map<String, Map<String, Long>>> raw : rawValueMap.entrySet()) {	
	            			List<ResourceLocation> tagResults = new ArrayList<>();
	            			if (raw.getKey().startsWith("#")) 
	            				tagResults = getTagMembers(raw.getKey().substring(1));
	            			else
	            				tagResults.add(new ResourceLocation(raw.getKey()));
	            			for (ResourceLocation key : tagResults) {
	            				//Validate the event type entries and skip out 
	            				for (Map.Entry<String, Map<String, Long>> subset : raw.getValue().entrySet()) {
	            					//validate the event type key from the json
	            					EventType validEventKey = null;
	            					for (EventType eType : EventType.values()) {
	            						if (subset.getKey().toUpperCase().equals(eType.name().toUpperCase())) {
	            							validEventKey = eType;
	            							break;
	            						}
	            					}
	            					if (validEventKey == null) continue;
	            					//enter the resulting data into the data map
	            					MsLoggy.info(validEventKey+": "+key.toString()+MsLoggy.mapToString(subset.getValue())+" loaded from config");
	            					XpUtils.setObjectXpGainMap(validEventKey, key, subset.getValue());
	            				}     				
	            			}
	            		}

	            		for (Map.Entry<String, Map<String, Double>> raw : rawModifierMap.entrySet()) {
	            			List<ResourceLocation> tagResults = new ArrayList<>();
	            			if (raw.getKey().startsWith("#"))
	            				tagResults = getTagMembers(raw.getKey().substring(1));
	            			else 
	            				tagResults.add(new ResourceLocation(raw.getKey()));
	            			for (ResourceLocation key : tagResults) {
	            				MsLoggy.info(key.toString()+MsLoggy.mapToString(raw.getValue())+" loaded from config");
	            				XpUtils.setObjectXpModifierMap(type, key, raw.getValue());
	            			}
	            		}
	            }
	            catch(Exception e)
	            {
	                MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
	            }
		}
	}
	
	private static void parseCore() {
		DATA_PATH path = DATA_PATH.CORE;
		String filename;
		File dataFile;
		for (CoreType type : CoreType.values()) {
			filename = type.name().toLowerCase() + ".json";
			dataFile = FMLPaths.CONFIGDIR.get().resolve(path.config_path + filename).toFile();
			
			if (!dataFile.exists())
				createData(dataFile, path, filename);
		}
	}
	
	public static List<ResourceLocation> getTagMembers(String tag)
	{
		ResourceLocation tagRL = new ResourceLocation(tag);
		List<ResourceLocation> results = new ArrayList<>();

		if (ItemTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Item element : ItemTags.getAllTags().getAllTags().get(tagRL).getValues())	{
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		if (BlockTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Block element : BlockTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		if(FluidTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Fluid element : FluidTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}
		
		if (EntityTypeTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(EntityType<?> element : EntityTypeTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		return results;
	}
	
	
	//======================FILE MANAGEMENT======================
	
	private static enum DATA_PATH {
		REQS("/assets/pmmo/util/requirements/", "pmmo/requirements/"),
		EXP("/assets/pmmo/util/xp_values/", "pmmo/xp_values/"),
		CORE("/assets/pmmo/util/core/", "pmmo/core/");
		
		public String src_path;
		public String config_path;
		
		DATA_PATH(String src_path, String config_path) {
			this.src_path = src_path;
			this.config_path = config_path;
		}
	}
	
	private static void createData(File dataFile, DATA_PATH path, String fileName)
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

        try(InputStream inputStream = ProjectMMO.class.getResourceAsStream(path.src_path + fileName);
             FileOutputStream outputStream = new FileOutputStream(dataFile);)
        {
            MsLoggy.debug("Copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath());
            IOUtils.copy(inputStream, outputStream);
        }
        catch(IOException e)
        {
            MsLoggy.error("Error copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath(), e);
        }
    }
}
