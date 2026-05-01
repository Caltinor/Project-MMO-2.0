package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SkillTypeData(
		Optional<String> displayName,
		Optional<Integer> order,
		Optional<List<String>> skills,
		Optional<Integer> color
) {

	private static final String DISPLAY_NAME = "displayName";
	private static final String ORDER = "order";
	private static final String SKILLS = "skills";
	private static final String COLOR = "color";

	public static final Codec<SkillTypeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf(DISPLAY_NAME).forGetter(SkillTypeData::displayName),
			Codec.INT.optionalFieldOf(ORDER).forGetter(SkillTypeData::order),
			Codec.STRING.listOf().optionalFieldOf(SKILLS).forGetter(SkillTypeData::skills),
			Codec.INT.optionalFieldOf(COLOR).forGetter(SkillTypeData::color)
	).apply(instance, SkillTypeData::new));

	public MutableComponent getDisplayName(String key) {
		return displayName.filter(s -> !s.isEmpty())
				.<MutableComponent>map(Component::literal)
				.orElseGet(() -> Component.translatable("pmmo.type." + key));
	}
	public int getOrder() { return order.orElse(Integer.MAX_VALUE); }
	public List<String> getSkills() { return skills.orElse(List.of()); }
	public int getColor() { return color.orElse(16777215); }

	public static class Builder {
		private String displayName;
		private Integer order;
		private List<String> skills;
		private Integer color;

		private Builder() {
			this.skills = new ArrayList<>();
		}

		public static Builder start() { return new Builder(); }

		public Builder withDisplayName(String name) { this.displayName = name; return this; }
		public Builder withOrder(int order) { this.order = order; return this; }
		public Builder withSkills(List<String> skills) { this.skills = new ArrayList<>(skills); return this; }
		public Builder withColor(int color) { this.color = color; return this; }

		public SkillTypeData build() {
			return new SkillTypeData(
					Optional.ofNullable(displayName),
					Optional.ofNullable(order),
					skills == null || skills.isEmpty() ? Optional.empty() : Optional.of(skills),
					Optional.ofNullable(color)
			);
		}

		public SkillTypeData fromScripting(Map<String, String> values) {
			if (values.containsKey(DISPLAY_NAME)) this.withDisplayName(values.get(DISPLAY_NAME));
			if (values.containsKey(ORDER)) this.withOrder(Integer.parseInt(values.get(ORDER)));
			if (values.containsKey(COLOR)) this.withColor(Integer.parseInt(values.get(COLOR)));
			if (values.containsKey(SKILLS)) {
				List<String> parsed = Arrays.stream(values.get(SKILLS).split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toList();
				this.withSkills(parsed);
			}
			return this.build();
		}
	}
}
