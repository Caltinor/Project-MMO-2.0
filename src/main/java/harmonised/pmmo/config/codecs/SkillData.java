package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkillData (
	Optional<Integer> color,
	Optional<Boolean> afkExempt,
	Optional<Map<String, Double>> groupedSkills) {
	public SkillData(int color) {this(color, false);}
	public SkillData(int color, boolean afkExempt) {this(Optional.of(color), Optional.of(afkExempt), Optional.empty());}
	
	public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color").forGetter(SkillData::color),
			Codec.BOOL.optionalFieldOf("noAfkPenalty").forGetter(SkillData::afkExempt),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("groupFor").forGetter(SkillData::groupedSkills)
			).apply(instance, SkillData::new));
	
	public static SkillData getDefault() {return new SkillData(Optional.of(16777215), Optional.of(false), Optional.empty());}

	public int getColor() {return color.orElse(16777215);}
	public boolean getAfkExempt() {return afkExempt.orElse(false);}
	
	public boolean isSkillGroup() {return !getGroup().isEmpty();}
	public Map<String, Double> getGroup() {return groupedSkills.orElse(new HashMap<>());}
	
	public Map<String, Long> getGroupXP(long xp) {
		Map<String, Long> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, Double.valueOf((ratio / denominator) * xp).longValue());
		});
		return outMap;
	}
	
	public Map<String, Integer> getGroupReq(int level) {
		Map<String, Integer> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, (int)((ratio / denominator) * (double)level));
		});
		return outMap;
	}
	
	public Map<String, Double> getGroupBonus(double bonus) {
		Map<String, Double> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		double gainLossModifier = bonus >= 1d ? 1d : 0d;
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, gainLossModifier + (ratio / denominator) * bonus);
		});
		return outMap;
	}
}
