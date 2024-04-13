package harmonised.pmmo.core.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record LogicEntry (
		BehaviorToPrevious behavior,
		boolean addCases,
		List<Case> cases) {	
	
	public static final Codec<LogicEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BehaviorToPrevious.CODEC.fieldOf("behavior_to_previous").forGetter(LogicEntry::behavior),
			Codec.BOOL.fieldOf("should_cases_add").forGetter(LogicEntry::addCases),
			Codec.list(Case.CODEC).fieldOf("cases").forGetter(LogicEntry::cases)
			).apply(instance, LogicEntry::new));
	
	
	public static record Case(List<String> paths, List<Criteria> criteria) {
		public static final Codec<Case> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.list(Codec.STRING).fieldOf("paths").forGetter(Case::paths),
				Codec.list(Criteria.CODEC).fieldOf("criteria").forGetter(Case::criteria)
				).apply(instance, Case::new));
	}
	public static record Criteria(Operator operator, Optional<List<String>> comparators, Map<String, Double> skillMap) {
		public static final Codec<Criteria> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Operator.CODEC.fieldOf("operator").forGetter(Criteria::operator),
				Codec.list(Codec.STRING).optionalFieldOf("comparators").forGetter(Criteria::comparators),
				Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("value").forGetter(Criteria::skillMap)
				).apply(instance, Criteria::new));
	} 
	
	
}
