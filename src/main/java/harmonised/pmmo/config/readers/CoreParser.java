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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import harmonised.pmmo.ProjectMMO;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.api.enums.XpType;
import harmonised.pmmo.config.CoreType;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.loading.FMLPaths;

public class CoreParser {
	public static final Logger LOGGER = LogManager.getLogger();
	private static final Gson gson = new Gson();

	public static final Type basicIntegerJsonType = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
	public static final Type basicLongJsonType = new TypeToken<Map<String, Map<String, Long>>>(){}.getType();
	public static final Type basicDoubleJsonType = new TypeToken<Map<String, Map<String, Double>>>(){}.getType();
	
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
            			List<String> tagResults = List.of(raw.getKey());
            			if (tagResults.get(0).startsWith("#"))
            				tagResults = getTagMembers(tagResults.get(0));
            			for (String key : tagResults) {
            				SkillGates.setObjectSkillMap(type, new ResourceLocation(key), raw.getValue());
            			}
            		}
            }
            catch(Exception e)
            {
                LOGGER.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
            }
        }
	}
	
	private static void parseXp_Values() {
		DATA_PATH path = DATA_PATH.EXP;
		String filename;
		File dataFile;
		for (XpType type : XpType.values()) {
			filename = type.name().toLowerCase() + ".json";
			dataFile = FMLPaths.CONFIGDIR.get().resolve(path.config_path + filename).toFile();
			
			if (!dataFile.exists())
				createData(dataFile, path, filename);
			
			try(InputStream input = new FileInputStream(dataFile.getPath());
	                Reader reader = new BufferedReader(new InputStreamReader(input)))
	            {
						//Distinguish between long and double type configs
						boolean isDoubleType = false;
						Map<String, Map<String, Long>> rawLongMap = new HashMap<>();
						Map<String, Map<String, Double>> rawDoubleMap = new HashMap<>();
						//Check for modifier types and fill the appropriate raw map for later evaluation
						for (XpType modType : XpType.getModifierTypes()) {
							if (modType.equals(type)) {
								rawDoubleMap = gson.fromJson(reader, basicDoubleJsonType);
								isDoubleType = true;
								break;
							}
						}
						if (!isDoubleType)
							rawLongMap = new Gson().fromJson(reader, basicLongJsonType);
						/* Evaluate configs based on their types
						 * This works by only filling the maps of the appropriate type.
						 * The loop for the incorrect type will be empty and simply bypassed.
						 */
	            		for (Map.Entry<String, Map<String, Long>> raw : rawLongMap.entrySet()) {
	            			List<String> tagResults = List.of(raw.getKey());
	            			if (tagResults.get(0).startsWith("#"))
	            				tagResults = getTagMembers(tagResults.get(0));
	            			for (String key : tagResults) {
	            				XpUtils.setObjectXpGainMap(type, new ResourceLocation(key), raw.getValue());
	            			}
	            		}
	            		
	            		for (Map.Entry<String, Map<String, Double>> raw : rawDoubleMap.entrySet()) {
	            			List<String> tagResults = List.of(raw.getKey());
	            			if (tagResults.get(0).startsWith("#"))
	            				tagResults = getTagMembers(tagResults.get(0));
	            			for (String key : tagResults) {
	            				XpUtils.setObjectXpModifierMap(type, new ResourceLocation(key), raw.getValue());
	            			}
	            		}
	            }
	            catch(Exception e)
	            {
	                LOGGER.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
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
	
	public static List<String> getTagMembers(String tag)
	{
		List<String> results = new ArrayList<>();

		for(Map.Entry<ResourceLocation, Tag<Item>> namedTag : ItemTags.getAllTags().getAllTags().entrySet()) {
			if(namedTag.getKey().toString().startsWith(tag)) {
				for(Item element : namedTag.getValue().getValues())	{
					try	{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<Block>> namedTag : BlockTags.getAllTags().getAllTags().entrySet()) {
			if(namedTag.getKey().toString().equals(tag)) {
				for(Block element : namedTag.getValue().getValues()) {
					try	{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<Fluid>> namedTag : FluidTags.getAllTags().getAllTags().entrySet()){
			if(namedTag.getKey().toString().equals(tag)) {
				for(Fluid element : namedTag.getValue().getValues()) {
					try	{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<EntityType<?>>> namedTag : EntityTypeTags.getAllTags().getAllTags().entrySet())	{
			if(namedTag.getKey().toString().equals(tag)) {
				for(EntityType<?> element : namedTag.getValue().getValues()) {
					try	{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
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
            LOGGER.error("Could not create template json config!", dataFile.getPath(), e);
            return;
        }

        try(InputStream inputStream = ProjectMMO.class.getResourceAsStream(path.src_path + fileName);
             FileOutputStream outputStream = new FileOutputStream(dataFile);)
        {
            LOGGER.debug("Copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath());
            IOUtils.copy(inputStream, outputStream);
        }
        catch(IOException e)
        {
            LOGGER.error("Error copying over " + fileName + " json config to " + dataFile.getPath(), dataFile.getPath(), e);
        }
    }
}
