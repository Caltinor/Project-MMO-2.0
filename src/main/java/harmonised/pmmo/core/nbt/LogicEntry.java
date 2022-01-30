package harmonised.pmmo.core.nbt;

import java.util.List;
import java.util.Map;

public record LogicEntry (
		BehaviorToPrevious behavior,
		boolean addCases,
		List<Case> cases) {	
	public static record Case(List<String> paths, List<Criteria> criteria) {}
	public static record Criteria(Operator operator, List<String> comparators, Map<String, Integer> skillMap) {} 
}
