package harmonised.pmmo.config.readers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapEnchantment;
import harmonised.pmmo.config.codecs.CodecMapLocation;
import harmonised.pmmo.config.codecs.CodecMapObject;
import harmonised.pmmo.config.codecs.CodecMapPlayer;
import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.registry.ConfigurationRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;

public class CoreParser {
	
	private static final Logger DATA_LOGGER = LogManager.getLogger();	
	public static final Type valueJsonType = new TypeToken<Map<String, JsonObject>>(){}.getType();
	
	public static final MergeableCodecDataManager<Byte, Byte> RELOADER = new MergeableCodecDataManager<>(
			"dummy/dont/use/please/stop/this/will/break", DATA_LOGGER, Codec.BYTE, raws -> {return Byte.MIN_VALUE;}, func -> {
				Core.get(LogicalSide.SERVER).resetDataForReload();
			});
	
	public static final MergeableCodecDataManager<Byte, Byte> DEFAULT_CONFIG = new MergeableCodecDataManager<>(
			"dummy/dont/use/please/stop/this/will/break", DATA_LOGGER, Codec.BYTE, raws -> {return Byte.MIN_VALUE;}, func -> {
				MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Defaults from API Applied");
				ConfigurationRegistry.get().applyDefaults(Core.get(LogicalSide.SERVER));
			});
	
	public static final MergeableCodecDataManager<Byte, Byte> OVERRIDE_CONFIG = new MergeableCodecDataManager<>(
			"dummy/dont/use/please/stop/this/will/break", DATA_LOGGER, Codec.BYTE, raws -> {return Byte.MIN_VALUE;}, func -> {
				MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Overrides from API Applied");
				ConfigurationRegistry.get().applyOverrides(Core.get(LogicalSide.SERVER));
			});
	
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> ITEM_LOADER = new MergeableCodecDataManager<>(
			"pmmo/items", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(ObjectType.ITEM, processed));
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> BLOCK_LOADER = new MergeableCodecDataManager<>(
			"pmmo/blocks", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(ObjectType.BLOCK, processed));
	public static final MergeableCodecDataManager<CodecMapObject, CodecMapObject.ObjectMapContainer> ENTITY_LOADER = new MergeableCodecDataManager<>(
			"pmmo/entities", DATA_LOGGER, CodecMapObject.CODEC, raws -> mergeObjectTags(raws), processed -> finalizeObjectMaps(ObjectType.ENTITY, processed));
	
	private static CodecMapObject.ObjectMapContainer mergeObjectTags(final List<CodecMapObject> raws) {
		CodecMapObject.ObjectMapContainer outObject = new CodecMapObject.ObjectMapContainer();
		for (int i = 0; i < raws.size(); i++) {
			outObject = CodecMapObject.ObjectMapContainer.combine(outObject, new CodecMapObject.ObjectMapContainer(raws.get(i)));			
		}
		return outObject;
	}	
	private static void finalizeObjectMaps(ObjectType type, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {
		data.forEach((rl, omc) -> {
			if (!ModList.get().isLoaded(rl.getNamespace()))
				return;
			Core core = Core.get(LogicalSide.SERVER);
			List<ResourceLocation> tagValues = List.of(rl);
			if (omc.tagValues().size() > 0) tagValues = omc.tagValues();
			for (ResourceLocation tag : tagValues) {
				for (Map.Entry<EventType, Map<String, Long>> xpValues : omc.xpValues().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "XP_VALUES: "+xpValues.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(xpValues.getValue())+" loaded from config");
					core.getXpUtils().setObjectXpGainMap(xpValues.getKey(), tag, xpValues.getValue());
				}			
				for (Map.Entry<ReqType, Map<String, Integer>> reqs : omc.reqs().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "REQS: "+reqs.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(reqs.getValue())+" loaded from config");
					core.getSkillGates().setObjectSkillMap(reqs.getKey(), tag, reqs.getValue());
				}
				if (!omc.veinData().equals(VeinData.EMPTY)) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "VEIN DATA: "+omc.veinData().toString());				
					core.getVeinData().setVeinData(tag, omc.veinData());
				}
				switch (type) {
				case ITEM: {
					for (Map.Entry<ModifierDataType, Map<String, Double>> modifiers : omc.modifiers().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "BONUSES: "+tag.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
						core.getXpUtils().setObjectXpModifierMap(modifiers.getKey(), tag, modifiers.getValue());
					}
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setItemReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<String, Integer> reqEffects : omc.reqNegativeEffect().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "REQ NEG EFFECTS: "+reqEffects.getKey()+", "+reqEffects.getValue());
						core.getDataConfig().setReqEffectData(tag, new ResourceLocation(reqEffects.getKey()), reqEffects.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					for (Map.Entry<ModifierDataType, List<LogicEntry>> nbtBonus : omc.nbtBonuses().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT BONUS: "+nbtBonus.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setBonuses(nbtBonus.getKey(), tag, nbtBonus.getValue());
					}
					for (Map.Entry<ResourceLocation, SalvageData> salvage : omc.salvage().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "SALVAGE: "+tag.toString()+": "+salvage.getKey().toString()+salvage.getValue().toString());
						core.getSalvageLogic().setSalvageData(tag, salvage.getKey(), salvage.getValue());
					}
					break;
				}
				case BLOCK: {
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setBlockReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					break;
				}
				case ENTITY: {
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setEntityReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						core.getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					break;
				}
				default: {}
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
			if (!ModList.get().isLoaded(rl.getNamespace()))
				return;
			List<ResourceLocation> tagValues = List.of(rl);
			if (lmc.tagValues().size() > 0) tagValues = lmc.tagValues();
			for (ResourceLocation tag : tagValues) {
				for (Map.Entry<ModifierDataType, Map<String, Double>> modifiers : lmc.bonusMap().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "BONUSES: "+tag.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
					Core.get(LogicalSide.SERVER).getXpUtils().setObjectXpModifierMap(modifiers.getKey(), tag, modifiers.getValue());
				}
				for (Map.Entry<ResourceLocation, Map<String, Double>> mobMods : lmc.mobModifiers().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "MOB MODIFIERS: "+tag.toString()+mobMods.getKey().toString()+MsLoggy.mapToString(mobMods.getValue())+" loaded from config");
					Core.get(LogicalSide.SERVER).getDataConfig().setMobModifierData(tag, mobMods.getKey(), mobMods.getValue());
				}
				MsLoggy.INFO.log(LOG_CODE.DATA, "POSITIVE EFFECTS: "+tag.toString()+MsLoggy.mapToString(lmc.positive())+" loaded from config");
				Core.get(LogicalSide.SERVER).getDataConfig().setLocationEffectData(true, tag, lmc.positive());
				MsLoggy.INFO.log(LOG_CODE.DATA, "NEGATIVE EFFECTS: "+tag.toString()+MsLoggy.mapToString(lmc.negative())+" loaded from config");
				Core.get(LogicalSide.SERVER).getDataConfig().setLocationEffectData(false, tag, lmc.negative());
				MsLoggy.INFO.log(LOG_CODE.DATA, "VEIN BLACKLIST: "+tag.toString()+MsLoggy.listToString(lmc.veinBlacklist())+" loaded from config");
				Core.get(LogicalSide.SERVER).getDataConfig().setArrayData(tag, lmc.veinBlacklist());
				MsLoggy.INFO.log(LOG_CODE.DATA, "TRAVEl REQ: "+tag.toString()+MsLoggy.mapToString(lmc.travelReq())+" loaded from config");
				Core.get(LogicalSide.SERVER).getSkillGates().setObjectSkillMap(ReqType.TRAVEL, tag, lmc.travelReq());
			}
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
			MsLoggy.INFO.log(LOG_CODE.DATA, "PLAYER: ID:"+rl.getPath()+pd.toString());
			Core.get(LogicalSide.SERVER).getDataConfig().setPlayerSpecificData(UUID.fromString(rl.getPath()), pd);
		});
	}
	
	public static final MergeableCodecDataManager<CodecMapEnchantment, Map<Integer, Map<String, Integer>>> ENCHANTMENT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/enchantments", DATA_LOGGER, CodecMapEnchantment.CODEC, raws -> mergeEnchantmentTags(raws), processed -> finalizeEnchantmentMaps(processed));
	private static Map<Integer, Map<String, Integer>> mergeEnchantmentTags(final List<CodecMapEnchantment> raws) {
		CodecMapEnchantment mergedObject = new CodecMapEnchantment(false, new ArrayList<>());
		for (CodecMapEnchantment entry : raws) {
			mergedObject = CodecMapEnchantment.combine(mergedObject, entry);
		}
		
		Map<Integer, Map<String, Integer>> outMap = new HashMap<>();
		for (int i = 0; i < mergedObject.skillArray().size(); i++) {
			outMap.put(i+1, mergedObject.skillArray().get(i));
		}
 		return outMap;
	}
	private static void finalizeEnchantmentMaps(Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data) {
		data.forEach((rl, map) -> {
			if (!ModList.get().isLoaded(rl.getNamespace()))
				return;
			map.forEach((k, v) -> MsLoggy.INFO.log(LOG_CODE.DATA, "ENCHANTMENT:"+rl.toString()+" Level:"+k+MsLoggy.mapToString(v)));
			Core.get(LogicalSide.SERVER).getSkillGates().setEnchantmentReqs(rl, map);
		});
	}
}