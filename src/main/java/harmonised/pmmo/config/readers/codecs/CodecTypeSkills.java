package harmonised.pmmo.config.readers.codecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CodecTypeSkills {
	private Optional<Integer> color;
	private Optional<Boolean> afkExempt;
	
	public static Codec<CodecTypeSkills> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color").forGetter(CodecTypeSkills::getColor),
			Codec.BOOL.optionalFieldOf("noAfkPenalty").forGetter(CodecTypeSkills::getAfkExempt)
			).apply(instance, CodecTypeSkills::new));
	
	public CodecTypeSkills(Optional<Integer> color, Optional<Boolean> afkExempt) {
		this.color = color;
		this.afkExempt = afkExempt;
	}
	public Optional<Integer> getColor() {return color;}
	public Optional<Boolean> getAfkExempt() {return afkExempt;}
	
	public static record SkillData(int color, boolean afkPentaltyIgnored) {
		public static final String COLOR = "color";
		public static final String AFK = "noAfkPenalty";
		public static SkillData getDefault() {return new SkillData(16777215, false);}
		public SkillData(CodecTypeSkills data) {
			this(data.color.orElseGet(() -> 16777215)
				,data.afkExempt.orElseGet(() -> false));
		}
	}
}
