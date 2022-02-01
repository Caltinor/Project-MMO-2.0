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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import harmonised.pmmo.ProjectMMO;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.CoreType;
import harmonised.pmmo.config.DataConfig;
import harmonised.pmmo.config.datapack.MergeableCodecDataManager;
import harmonised.pmmo.config.datapack.codecs.CodecMapLocation;
import harmonised.pmmo.config.datapack.codecs.CodecMapObject;
import harmonised.pmmo.config.datapack.codecs.CodecMapPlayer;
import harmonised.pmmo.config.datapack.codecs.CodecTypeReq;
import harmonised.pmmo.config.datapack.codecs.CodecTypeSalvage;
import harmonised.pmmo.config.readers.codecs.CodecMapGlobals;
import harmonised.pmmo.config.readers.codecs.CodecTypeSkills;
import harmonised.pmmo.config.readers.codecs.CodecTypeSkills.SkillData;
import harmonised.pmmo.core.NBTUtils;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.features.salvaging.SalvageLogic;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
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
	
	public static final MergeableCodecDataManager<CodecMapPlayer, CodecMapPlayer.PlayerData> PLAYER_LOADER = new MergeableCodecDataManager<>(
			"pmmo/players", DATA_LOGGER, CodecMapPlayer.CODEC, raws -> mergePlayerTags(raws), processed -> finalizePlayerMaps(processed));
	
	private static CodecMapPlayer.PlayerData mergePlayerTags(final List<CodecMapPlayer> raws) {
		CodecMapPlayer.PlayerData outObject = CodecMapPlayer.PlayerData.getDefault();
		for (int i = 0; i < raws.size(); i++) {
			outObject = CodecMapPlayer.PlayerData.combine(outObject, new CodecMapPlayer.PlayerData(raws.get(i)));
		}
		return outObject;
	}
	private static void finalizePlayerMaps(Map<ResourceLocation, CodecMapPlayer.PlayerData> data) {
		data.forEach((rl, pd) -> {
			DataConfig.setPlayerSpecificData(UUID.fromString(rl.getPath()), pd);
		});
	}
	
	public static final Codec<Map<Integer, Map<String, Integer>>> ENCHANTMENT_CODEC = Codec.unboundedMap(Codec.INT, CodecTypeReq.INTEGER_CODEC);
	public static final MergeableCodecDataManager<Map<Integer, Map<String, Integer>>, Map<Integer, Map<String, Integer>>> ENCHANTMENT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/enchantments", DATA_LOGGER, ENCHANTMENT_CODEC, raws -> mergeEnchantmentTags(raws), processed -> finalizeEnchantmentMaps(processed));
	private static Map<Integer, Map<String, Integer>> mergeEnchantmentTags(final List<Map<Integer, Map<String, Integer>>> raws) {
		Map<Integer, Map<String, Integer>> outMap = new HashMap<>();
		for (int i = 0; i < raws.size(); i++) {
			for (Map.Entry<Integer, Map<String, Integer>> map : raws.get(i).entrySet()) {
				outMap.computeIfAbsent(map.getKey(), (k) -> new HashMap<>());
				map.getValue().forEach((k, v) -> {
					outMap.get(map.getKey()).put(k, v);
				});
			}
		}
		return outMap;
	}
	private static void finalizeEnchantmentMaps(Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data) {
		data.forEach((rl, map) -> {
			SkillGates.setEnchantmentReqs(rl, map);
		});
	}
	
	public static void init() {
		parseSkills();
		parseGlobals();
	}
	
	private static final Codec<Map<String, CodecTypeSkills>> SKILLS_CODEC = Codec.unboundedMap(Codec.STRING, CodecTypeSkills.CODEC);
	private static void parseSkills() {
		String filename = "skills.json";
		File dataFile = FMLPaths.CONFIGDIR.get().resolve("pmmo/" + filename).toFile();
		
		if (!dataFile.exists())
			createData(dataFile, "/assets/pmmo/util/", filename);
	
	
		try(InputStream input = new FileInputStream(dataFile.getPath());
            Reader reader = new BufferedReader(new InputStreamReader(input)))
        {
			Map<String, CodecTypeSkills> readResult = new HashMap<>();
			SKILLS_CODEC.parse(JsonOps.INSTANCE, GsonHelper.fromJson(gson, reader, JsonElement.class))
				.resultOrPartial((s) -> MsLoggy.error(s))
				.ifPresent(readResult::putAll);
			for (Map.Entry<String, CodecTypeSkills> raw : readResult.entrySet()) {
				MsLoggy.info("Skills: "+raw.getKey()+raw.getValue().toString()+" loaded from config");
				DataConfig.setSkillData(raw.getKey(), new SkillData(raw.getValue()));
			}
        }
        catch(Exception e)
        {
            MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
        }
	}
	
	private static void parseGlobals() {
		String filename = "globals.json";
		File dataFile = FMLPaths.CONFIGDIR.get().resolve("pmmo/" + filename).toFile();
		
		if (!dataFile.exists())
			createData(dataFile, "/assets/pmmo/util/", filename);
	
	
		try(InputStream input = new FileInputStream(dataFile.getPath());
            Reader reader = new BufferedReader(new InputStreamReader(input)))
        {
			CodecMapGlobals.CODEC.parse(JsonOps.INSTANCE, GsonHelper.fromJson(gson, reader, JsonElement.class))
				.resultOrPartial((s) -> MsLoggy.error(s))
				.ifPresent(NBTUtils::setGlobals);
        }
        catch(Exception e)
        {
            MsLoggy.error("ERROR READING PROJECT MMO CONFIG: Invalid JSON Structure of " + filename, e);
        }
	}
	
	//======================FILE MANAGEMENT======================	
	private static void createData(File dataFile, String path, String fileName)
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

        try(InputStream inputStream = ProjectMMO.class.getResourceAsStream(path + fileName);
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
