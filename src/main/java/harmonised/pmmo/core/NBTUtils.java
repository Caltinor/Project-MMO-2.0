package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.core.nbt.PathReader;
import harmonised.pmmo.core.nbt.Result;
import harmonised.pmmo.core.nbt.LogicEntry.Case;
import harmonised.pmmo.core.nbt.LogicEntry.Criteria;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;

public class NBTUtils {
	
	public static Map<String, Long> getExperienceAward(List<LogicEntry> logic, CompoundTag nbt) {
		return translateToLong(evaluateEntries(nbt, new LinkedHashSet<>(logic)));
	}
	
	public static Map<String, Integer> getRequirement(List<LogicEntry> logic, CompoundTag nbt) {
		return translateToInt(evaluateEntries(nbt, new LinkedHashSet<>(logic)));
	}
	
	public static Map<String, Double> getBonuses(List<LogicEntry> logic, CompoundTag nbt) {
		return evaluateEntries(nbt, new LinkedHashSet<>(logic));
	}
	
	private static record LogicTier (BehaviorToPrevious behavior, boolean isSummative, List<Result> results) {}
	
	private static final Map<Pair<CompoundTag, Set<LogicEntry>>, Map<String, Double>> cache = new HashMap<>();
	
	private static Map<String, Double> evaluateEntries(CompoundTag nbt, LinkedHashSet<LogicEntry> logic) {		
		Map<String, Double> output = new HashMap<>();
		//cancels the evaluation if NBT has no data or never existed
		if (nbt == null || nbt.isEmpty()) return output;
		//if the compound matches a cached result, return that instead
		if (cache.containsKey(Pair.of(nbt, logic))) return MsLoggy.DEBUG.logAndReturn(cache.get(Pair.of(nbt, logic)), LOG_CODE.DATA, "NBT Cache Used");
		//this section cycles through the logic and generates usable result objects
		List<LogicTier> logicSequence = new ArrayList<>();
		for (LogicEntry entry : logic) {
			logicSequence.add(new LogicTier(entry.behavior(), entry.addCases(), processCases(entry.cases(), nbt)));
		}
		//This section iterates through the logical tiers and processes the summative attribute
		List<Map<String, Double>> interMap = new ArrayList<>();
		for (int i = 0; i < logicSequence.size(); i++) {
			Map<String, Double> combinedMap = new HashMap<>();
			List<Result> data = logicSequence.get(i).results;
			boolean isSummative = logicSequence.get(i).isSummative;
			for (Result r : data) {
				if (r == null) continue;
				if (!r.compares()) continue;
				Map<String, Double> value = r.values();					
				for (Map.Entry<String, Double> val : value.entrySet()) {
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
			case SUB_FROM -> {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					if (output.getOrDefault(value.getKey(), 0d) - value.getValue() <= 0) output.remove(value.getKey());
					else 
						output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue - newValue);
				}
			}
			case HIGHEST -> {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue > newValue ? oldValue : newValue);
				}
			}
			case REPLACE -> {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.put(value.getKey(), value.getValue());
				}
			}
			default -> { //Includes ADD_TO by default
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue + newValue);
				}
			}}
		}
		cache.put(Pair.of(nbt, logic), output);
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
					Map<String, Double> values = critObj.skillMap();
					Operator operator = critObj.operator();					
					List<String> comparison = PathReader.getNBTValues(getActualPath(paths.get(p)), nbt);
					for (int j = 0; j < comparison.size(); j++) {
						List<String> comparators = new ArrayList<>();
						if (!operator.equals(Operator.EXISTS)) {
							comparators = critObj.comparators().orElseGet(() -> new ArrayList<>());
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
	
	private static Map<String, Long> translateToLong(Map<String, Double> src) {
		Map<String, Long> output = new HashMap<>();
		src.forEach((k, v) -> output.put(k, v.longValue()));
		return output;
	}
	private static Map<String, Integer> translateToInt(Map<String, Double> src) {
		Map<String, Integer> output = new HashMap<>();
		src.forEach((k, v) -> output.put(k, v.intValue()));
		return output;
	}
	
	private static String getActualPath(String key) {
		return key.contains("#") ? GlobalsConfig.PATHS.get().getOrDefault(key.replace("#", ""), "") : key;
	}
	private static String getActualConstant(String key) {
		return key.contains("#") ? GlobalsConfig.CONSTANTS.get().getOrDefault(key.replace("#", ""), "") : key;
	}
}
