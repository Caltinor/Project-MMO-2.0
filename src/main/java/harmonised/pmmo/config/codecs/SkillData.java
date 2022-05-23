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
}
