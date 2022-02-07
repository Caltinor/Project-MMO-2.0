package harmonised.pmmo.config.codecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodecMapSkills (
	Optional<Integer> color,
	Optional<Boolean> afkExempt) {
	
	public static Codec<CodecMapSkills> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color").forGetter(CodecMapSkills::color),
			Codec.BOOL.optionalFieldOf("noAfkPenalty").forGetter(CodecMapSkills::afkExempt)
			).apply(instance, CodecMapSkills::new));
	
	public static record SkillData(int color, boolean afkPenaltyIgnored) {
		public static final String COLOR = "color";
		public static final String AFK = "noAfkPenalty";
		public static SkillData getDefault() {return new SkillData(16777215, false);}
		public SkillData(CodecMapSkills data) {
			this(data.color.orElseGet(() -> 16777215)
				,data.afkExempt.orElseGet(() -> false));
		}
		
		public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf(COLOR).forGetter(SkillData::color),
				Codec.BOOL.fieldOf(AFK).forGetter(SkillData::afkPenaltyIgnored)
				).apply(instance, SkillData::new));
	}
}
