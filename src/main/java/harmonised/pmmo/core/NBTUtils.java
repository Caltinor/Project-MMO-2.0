package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.codecs.CodecMapGlobals;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.LogicEntry.Case;
import harmonised.pmmo.core.nbt.LogicEntry.Criteria;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.core.nbt.PathReader;
import harmonised.pmmo.core.nbt.Result;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NBTUtils {
	private static Map<String, String> globalPaths = new HashMap<>();
	private static Map<String, String> globalConst = new HashMap<>();
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> itemReqLogic = new HashMap<>();
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> blockReqLogic = new HashMap<>();
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> entityReqLogic = new HashMap<>();
	private static Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> itemXpGainLogic = new HashMap<>();
	private static Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> blockXpGainLogic = new HashMap<>();
	private static Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> entityXpGainLogic = new HashMap<>();
	
	//======================SETTERS=================================
	public static void setGlobals(CodecMapGlobals data) {
		globalPaths = data.getPaths();
		globalConst = data.getConstants();
	}
	
	//======================GETTERS=================================
	public static Map<String, Integer> getReqMap(ReqType reqType, ItemStack stack) {
		return evaluateEntries(stack.getTag(), itemReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(stack.getItem().getRegistryName()));
	}
	public static Map<String, Integer> getReqMap(ReqType reqType, BlockEntity tile) {
		return evaluateEntries(tile.getTileData(), blockReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(tile.getBlockState().getBlock().getRegistryName()));
	}
	public static Map<String, Integer> getReqMap(ReqType reqType, Entity entity) {
		//TODO verify this NBT getter is correct for our purposes
		return evaluateEntries(entity.getPersistentData(), entityReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(new ResourceLocation(entity.getEncodeId())));
	}
	public static Map<String, Long> getXpMap(EventType reqType, ItemStack stack) {
		return translate(evaluateEntries(stack.getTag(), itemXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(stack.getItem().getRegistryName())));
	}
	public static Map<String, Long> getXpMap(EventType reqType, BlockEntity tile) {
		return translate(evaluateEntries(tile.getTileData(), blockXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(tile.getBlockState().getBlock().getRegistryName())));
	}
	public static Map<String, Long> getXpMap(EventType reqType, Entity entity) {
		//TODO verify this NBT getter is correct for our purposes
		return translate(evaluateEntries(entity.getPersistentData(), entityXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(new ResourceLocation(entity.getEncodeId()))));
	}
	
	//======================INTERNAL GETTERS========================
	private static String getActualPath(String key) {
		return key.contains("#") ? globalPaths.getOrDefault(key.replace("#", ""), "") : key;
	}
	private static String getActualConstant(String key) {
		return key.contains("#") ? globalConst.getOrDefault(key.replace("#", ""), "") : key;
	}
	
	//======================LOGICAL METHODS=========================
	private static record LogicTier (BehaviorToPrevious behavior, boolean isSummative, List<Result> results) {}
	
	private static Map<String, Integer> evaluateEntries(CompoundTag nbt, List<LogicEntry> logic) {
		Map<String, Integer> output = new HashMap<>();
		//cancels the evaluation if NBT has no data or never existed
		if (nbt.isEmpty() || nbt == null) return output;
		//this section cycles through the logic and generates usable result objects
		List<LogicTier> logicSequence = new ArrayList<>();
		for (int i = 0; i < logic.size(); i++) {
			LogicEntry entry = logic.get(i);
			logicSequence.add(new LogicTier(entry.behavior(), entry.addCases(), processCases(entry.cases(), nbt)));
		}
		//This section iterates through the logical tiers and processes the summative attribute
		List<Map<String, Integer>> interMap = new ArrayList<>();
		for (int i = 0; i < logicSequence.size(); i++) {
			Map<String, Integer> combinedMap = new HashMap<>();
			List<Result> data = logicSequence.get(i).results;
			boolean isSummative = logicSequence.get(i).isSummative;
			for (Result r : data) {
				if (r == null) continue;
				if (!r.compares()) continue;
				Map<String, Integer> value = r.values();					
				for (Map.Entry<String, Integer> val : value.entrySet()) {
					combinedMap.merge(val.getKey(), val.getValue(), (in1, in2) -> {
						return isSummative ? (in1 + in2)
								: (in1 > in2 ? in1 : in2);});
				}
			}
			interMap.add(combinedMap);
		}
		//this section iterates through the logical tiers and processes the relational attribute
		for (int i = 0; i < logicSequence.size(); i++) {
			switch (logicSequence.get(i).behavior()) {
			case SUB_FROM: {
				for (Map.Entry<String, Integer> value : interMap.get(i).entrySet()) {
					if (output.getOrDefault(value.getKey(), 0) - value.getValue() <= 0) output.remove(value.getKey());
					else 
						output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue - newValue);
				}
				break;
			}
			case HIGHEST: {
				for (Map.Entry<String, Integer> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue > newValue ? oldValue : newValue);
				}
				break;
			}
			case REPLACE: {
				for (Map.Entry<String, Integer> value : interMap.get(i).entrySet()) {
					output.put(value.getKey(), value.getValue());
				}
				break;
			}
			case ADD_TO: default:{
				for (Map.Entry<String, Integer> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue + newValue);
				}
				break;
			}
			}
		}
		return output;
	}
	
	private static List<Result> processCases(List<Case> cases, CompoundTag nbt) {
		List<Result> results = new ArrayList<>();
		for (int i = 0; i < cases.size(); i++) {
			Case caseIterant = cases.get(i);
			List<String> paths = caseIterant.paths();
			List<Criteria> criteria = caseIterant.criteria();
			for (int p = 0; p < paths.size(); p++) {
				for (int c = 0; c < criteria.size(); c++) {
					Criteria critObj = criteria.get(c);
					Map<String, Integer> values = critObj.skillMap();
					Operator operator = critObj.operator();					
					List<String> comparison = PathReader.getNBTValues(getActualPath(paths.get(p)), nbt);
					for (int j = 0; j < comparison.size(); j++) {
						List<String> comparators = new ArrayList<>();
						if (!operator.equals(Operator.EXISTS)) {
							comparators = critObj.comparators();
							for (int l = 0; l < comparators.size(); l++) {
								String comparator = getActualConstant(comparators.get(l));
								results.add(new Result(operator, comparator, comparison.get(j), values));
							}
						}
						else results.add(new Result(operator, "", comparison.get(j), values));
					}
					
				}
			}
		}
		return results;
	}
	
	private static Map<String, Long> translate(Map<String, Integer> src) {
		Map<String, Long> output = new HashMap<>();
		src.forEach((k, v) -> output.put(k, (long)v));
		return output;
	}
}
