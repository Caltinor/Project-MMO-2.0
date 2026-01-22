package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SkillUpTrigger extends SimpleCriterionTrigger<SkillUpTrigger.TriggerInstance> {
	public static final SkillUpTrigger SKILL_UP = new SkillUpTrigger();

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}
	public void trigger(ServerPlayer player) {
		this.trigger(player, p -> true);
	}
	public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints level) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("level").forGetter(ti -> Optional.of(ti.level()))
		).apply(instance, (p,l) -> new TriggerInstance(p, l.orElse(MinMaxBounds.Ints.ANY))));
	}
}
