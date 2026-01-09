package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.core.Core;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;

import java.util.Optional;

public class SkillUpTrigger extends SimpleCriterionTrigger<SkillUpTrigger.TriggerInstance>{
	public static final SkillUpTrigger SKILL_UP = new SkillUpTrigger();

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}
	public void trigger(ServerPlayer player) {
		this.trigger(player, ti -> {
			long level = Core.get(LogicalSide.SERVER).getData().getLevel(ti.skill(), player.getUUID());
			return ti.level.matches(level);
		});
	}
	public record TriggerInstance(Optional<ContextAwarePredicate> player, Longs level, String skill) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				Longs.CODEC.optionalFieldOf("level").forGetter(ti -> Optional.of(ti.level())),
				Codec.STRING.fieldOf("skill").forGetter(TriggerInstance::skill)
		).apply(instance, (p,l,s) -> new TriggerInstance(p, l.orElse(Longs.ANY), s)));
	}

	public record Longs(Optional<Long> min, Optional<Long> max) implements MinMaxBounds<Long> {
		public static final Longs ANY = new Longs(Optional.empty(), Optional.empty());
		public static Codec<Longs> CODEC = MinMaxBounds.createCodec(Codec.LONG, Longs::new);

		public boolean matches(long value) {
			return min.orElse(0L) >= value && value <= max.orElse(Long.MAX_VALUE);
		}

		public static Longs between(long min, long max) {return new Longs(Optional.of(min), Optional.of(max));}
	}
}
