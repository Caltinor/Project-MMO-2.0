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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import harmonised.pmmo.ProjectMMO;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.CoreType;
import harmonised.pmmo.config.DataConfig;
import harmonised.pmmo.config.datapack.MergeableCodecDataManager;
import harmonised.pmmo.config.datapack.codecs.CodecMapLocation;
import harmonised.pmmo.config.datapack.codecs.CodecMapObject;
import harmonised.pmmo.config.datapack.codecs.CodecTypeSalvage;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.features.salvaging.SalvageLogic;
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
	private static final Logger DATA_LOGGER = LogManager.getLogger();
	private static final Gson gson = new Gson();	
	public static final Type valueJsonType = new TypeToken<Map<String, JsonObject>>(){}.getType();
	
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> ITEM_LOADER = new MergeableCodecDataManager<>(
			"pmmo/items", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(true, processed));
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> BLOCK_LOADER = new MergeableCodecDataManager<>(
			"pmmo/blocks", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(false, processed));
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> ENTITY_LOADER = new MergeableCodecDataManager<>(
			"pmmo/entities", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(false, processed));
	
	private static CodecMapObject.ObjectMapContainer mergeObjectTags(final List<CodecMapObject> raws) {
		CodecMapObject.ObjectMapContainer outObject = new CodecMapObject.ObjectMapContainer();
		for (int i = 0; i < raws.size(); i++) {
			outObject = CodecMapObject.ObjectMapContainer.combine(outObject, new CodecMapObject.ObjectMapContainer(raws.get(i)));			
		}
		return outObject;
	}	
	private static void finalizeObjectMaps(boolean isItem, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {
		data.forEach((rl, omc) -> {
			for (Map.Entry<EventType, Map<String, Long>> xpValues : omc.xpValues.entrySet()) {
				MsLoggy.info("XP_VALUES: "+xpValues.getKey().toString()+": "+rl.toString()+MsLoggy.mapToString(xpValues.getValue())+" loaded from config");
				XpUtils.setObjectXpGainMap(xpValues.getKey(), rl, xpValues.getValue());
			}			
			for (Map.Entry<ReqType, Map<String, Integer>> reqs : omc.reqs.entrySet()) {
				MsLoggy.info("REQS: "+reqs.getKey().toString()+": "+rl.toString()+MsLoggy.mapToString(reqs.getValue())+" loaded from config");
				SkillGates.setObjectSkillMap(reqs.getKey(), rl, reqs.getValue());
			}
			if (isItem) {
				for (Map.Entry<XpValueDataType, Map<String, Double>> modifiers : omc.modifiers.entrySet()) {
					MsLoggy.info("BONUSES: "+rl.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
					XpUtils.setObjectXpModifierMap(modifiers.getKey(), rl, modifiers.getValue());
				}
				for (Map.Entry<ResourceLocation, CodecTypeSalvage.SalvageData> salvage : omc.salvage.entrySet()) {
					MsLoggy.info("SALVAGE: "+rl.toString()+": "+salvage.getKey().toString()+salvage.getValue().toString());
					SalvageLogic.setSalvageData(rl, salvage.getKey(), salvage.getValue());
				}
			}
		});
	}
	
	public static final MergeableCodecDataManager<CodecMapLocation, CodecMapLocation.LocationMapContainer> BIOME_LOADER = new MergeableCodecDataManager<>(
			"pmmo/biomes", DATA_LOGGER, CodecMapLocation.CODEC, raws -> mergeLocationTags(raws), processed -> finalizeLocationMaps(processed));
	public static final MergeableCodecDataManager<CodecMapLocation, CodecMapLocation.LocationMapContainer> DIMENSION_LOADER = new MergeableCodecDataManager<>(
			"pmmo/dimensions", DATA_LOGGER, CodecMapLocation.CODEC, raws -> mergeLocationTags(raws), processed -> finalizeLocationMaps(processed));
	
	private static CodecMapLocation.LocationMapContainer mergeLocationTags(final List<CodecMapLocation> raws) {
		CodecMapLocation.LocationMapContainer outObject = new CodecMapLocation.LocationMapContainer();
		for (int i = 0; i < raws.size(); i++) {
			outObject = CodecMapLocation.LocationMapContainer.combine(outObject, new CodecMapLocation.LocationMapContainer(raws.get(i)));
		}
		return outObject;
	}
	private static void finalizeLocationMaps(Map<ResourceLocation, CodecMapLocation.LocationMapContainer> data) {
		data.forEach((rl, lmc) -> {
			for (Map.Entry<XpValueDataType, Map<String, Double>> modifiers : lmc.bonusMap.entrySet()) {
				MsLoggy.info("BONUSES: "+rl.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
				XpUtils.setObjectXpModifierMap(modifiers.getKey(), rl, modifiers.getValue());
			}
			for (Map.Entry<ResourceLocation, Map<String, Double>> mobMods : lmc.mobModifiers.entrySet()) {
				MsLoggy.info("MOB MODIFIERS: "+rl.toString()+mobMods.getKey().toString()+MsLoggy.mapToString(mobMods.getValue())+" loaded from config");
				DataConfig.setMobModifierData(rl, mobMods.getKey(), mobMods.getValue());
			}
			DataConfig.setLocationEffectData(CoreType.LOCATION_EFFECT_POSITIVE, rl, lmc.positive);
			DataConfig.setLocationEffectData(CoreType.LOCATION_EFFECT_NEGATIVE, rl, lmc.negative);
			DataConfig.setArrayData(rl, lmc.veinBlacklist);
			SkillGates.setObjectSkillMap(ReqType.REQ_TRAVEL, rl, lmc.travelReq);
		});
	}
	
	//TODO PLAYER_LOADER
	
	//TODO ENCHANTMENT_LOADER
	
	public static void init() {
		parseSkills();
		parseGlobals();
	}
	
	private static void parseSkills() {
		
	}
	
	private static void parseGlobals() {
		
	}
	
	/*private static void parseCore() {
		DATA_PATH path = DATA_PATH.CORE;
		String filename;
		File dataFile;
		for (CoreType type : CoreType.values()) {
			filename = type.name().toLowerCase() + ".json";
			dataFile = FMLPaths.CONFIGDIR.get().resolve(path.config_path + filename).toFile();
			
			if (!dataFile.exists())
				createData(dataFile, path, filename);
		
		
			try(InputStream input = new FileInputStream(dataFile.getPath());
                Reader reader = new BufferedReader(new InputStreamReader(input)))
            {
				if (isEnumArrayMember(type, CoreType.jsonTypes)) {
					Map<String, JsonObject> rawJsonData = gson.fromJson(reader, valueJsonType);
					for (Map.Entry<String, JsonObject> raw : rawJsonData.entrySet()) {
						List<ResourceLocation> tagResults = new ArrayList<>();
            			if (raw.getKey().startsWith("#"))
            				tagResults = getTagMembers(raw.getKey().substring(1));
            			else
            				tagResults.add(new ResourceLocation(raw.getKey()));
            			for (ResourceLocation key : tagResults) {
            				//TODO setup skills and globals
            				//DataConfig.setMobModifierData(type, key, raw.getValue());
            				MsLoggy.info(type.name()+": "+key.toString()+raw.getValue().toString()+" loaded from config");
            			}
					}
				}
            }
            catch(Exception e)
            {
                MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
            }
		}
	}*/
	
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
