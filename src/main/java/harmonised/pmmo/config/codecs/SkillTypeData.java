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

/**
 * Configuration for a single "skill type" — a named grouping that controls how
 * a set of skills is rendered in the inventory side panel.
 * <p>
 * The display name is always sourced from the translation key
 * {@code pmmo.type.<typeKey>}; localization belongs in the resource pack rather
 * than in the data file.
 *
 * @param order  vertical sort key among types (lower numbers render first; ties broken alphabetically by key)
 * @param skills the skills that belong to this type, rendered in the listed order
 * @param color  the accent color used for the type's header bar and the colored frame around its rows
 */
public record SkillTypeData(
		Optional<Integer> order,
		Optional<List<String>> skills,
		Optional<Integer> color
) {

	private static final String ORDER = "order";
	private static final String SKILLS = "skills";
	private static final String COLOR = "color";

	public static final Codec<SkillTypeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf(ORDER).forGetter(SkillTypeData::order),
			Codec.STRING.listOf().optionalFieldOf(SKILLS).forGetter(SkillTypeData::skills),
			Codec.INT.optionalFieldOf(COLOR).forGetter(SkillTypeData::color)
	).apply(instance, SkillTypeData::new));

	/**
	 * Translation-backed display name. Authors customize the visible name by
	 * adding a {@code pmmo.type.<typeKey>} entry to a resource pack lang file;
	 * fallbacks to the literal key if no translation is registered.
	 */
	public MutableComponent getDisplayName(String key) {
		return Component.translatable("pmmo.type." + key);
	}
	/** Defaults to {@code Integer.MAX_VALUE} so unordered types fall to the bottom. */
	public int getOrder() { return order.orElse(Integer.MAX_VALUE); }
	public List<String> getSkills() { return skills.orElse(List.of()); }
	/** Defaults to white when no color is set. */
	public int getColor() { return color.orElse(16777215); }

	/**
	 * Mutable builder used by code-side defaults and by the {@code .pmmo} scripting parser
	 * to assemble a {@link SkillTypeData} from a partial set of fields.
	 */
	public static class Builder {
		private Integer order;
		private List<String> skills;
		private Integer color;

		private Builder() {
			this.skills = new ArrayList<>();
		}

		public static Builder start() { return new Builder(); }

		public Builder withOrder(int order) { this.order = order; return this; }
		public Builder withSkills(List<String> skills) { this.skills = new ArrayList<>(skills); return this; }
		public Builder withColor(int color) { this.color = color; return this; }

		public SkillTypeData build() {
			return new SkillTypeData(
					Optional.ofNullable(order),
					skills == null || skills.isEmpty() ? Optional.empty() : Optional.of(skills),
					Optional.ofNullable(color)
			);
		}

		/**
		 * Populates the builder from a {@code .pmmo} script's parsed key→value map.
		 * Unknown keys (including the {@code skillType} dispatch flag) are ignored.
		 * The {@code skills} value is parsed as a comma-separated list of skill keys.
		 */
		public SkillTypeData fromScripting(Map<String, String> values) {
			if (values.containsKey(ORDER)) this.withOrder(Integer.parseInt(values.get(ORDER)));
			if (values.containsKey(COLOR)) this.withColor(Integer.parseInt(values.get(COLOR)));
			if (values.containsKey(SKILLS)) {
				List<String> parsed = Arrays.stream(values.get(SKILLS).split(","))
						.map(String::trim)
						.filter(skillKey -> !skillKey.isEmpty())
						.toList();
				this.withSkills(parsed);
			}
			return this.build();
		}
	}
}
