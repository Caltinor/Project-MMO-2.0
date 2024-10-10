package harmonised.pmmo.core.nbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import harmonised.pmmo.config.GlobalsConfig;
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
		//TODO add src null check to fix #576.  
		return translateToInt(evaluateEntries(nbt, new LinkedHashSet<>(logic)));
	}
	
	public static Map<String, Double> getBonuses(List<LogicEntry> logic, CompoundTag nbt) {
		return evaluateEntries(nbt, new LinkedHashSet<>(logic));
	}
	
	private record LogicTier (BehaviorToPrevious behavior, boolean isSummative, List<Result> results) {}
	
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
        for (LogicTier logicTier : logicSequence) {
            Map<String, Double> combinedMap = new HashMap<>();
            List<Result> data = logicTier.results;
            boolean isSummative = logicTier.isSummative;
            for (Result r : data) {
                if (r == null) continue;
                if (!r.compares()) continue;
                Map<String, Double> value = r.values();
                for (Map.Entry<String, Double> val : value.entrySet()) {
                    combinedMap.merge(val.getKey(), val.getValue(), isSummative ? Double::sum : Double::max);
                }
            }
            interMap.add(combinedMap);
        }
		//this section iterates through the logical tiers and processes the relational attribute
		for (int i = 0; i < logicSequence.size(); i++) {
			switch (logicSequence.get(i).behavior()) {
			case SUB_FROM -> {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					if (output.getOrDefault(value.getKey(), 0d) - value.getValue() <= 0)
						output.remove(value.getKey());
					else 
						output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue - newValue);
				}
			}
			case HIGHEST -> {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), Double::max);
				}
			}
			case REPLACE -> output.putAll(interMap.get(i));
			default -> { //Includes ADD_TO by default
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), Double::sum);
				}
			}}
		}
		cache.put(Pair.of(nbt, logic), output);
		return output;
	}
	
	private static List<Result> processCases(List<Case> cases, CompoundTag nbt) {
		List<Result> results = new ArrayList<>();
        for (Case caseIterant : cases) {
            for (String path : caseIterant.paths()) {
                for (Criteria critObj : caseIterant.criteria()) {
                    Map<String, Double> values = critObj.skillMap();
                    Operator operator = critObj.operator();
                    List<String> comparison = PathReader.getNBTValues(getActualPath(path), nbt);
                    for (String compare : comparison) {
                        if (!operator.equals(Operator.EXISTS)) {
                            for (String comparators : critObj.comparators().orElseGet(ArrayList::new)) {
                                String comparator = getActualConstant(comparators);
                                results.add(new Result(operator, comparator, compare, values));
                            }
                        } else results.add(new Result(operator, "", compare, values));
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
