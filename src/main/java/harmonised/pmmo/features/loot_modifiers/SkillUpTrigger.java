package harmonised.pmmo.features.loot_modifiers;

import com.google.gson.JsonObject;

import harmonised.pmmo.util.Reference;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SkillUpTrigger extends SimpleCriterionTrigger<SkillUpTrigger.TriggerInstance>{	
	public static final SkillUpTrigger SKILL_UP = new SkillUpTrigger();
	private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "skill_up");
	
	public SkillUpTrigger() {}

	public ResourceLocation getId() {return ID;}

	@Override
	protected TriggerInstance createInstance(JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext) {
		return new TriggerInstance(optional);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, p -> true);
	}
	
	public static class TriggerInstance extends AbstractCriterionTriggerInstance {

		public TriggerInstance(Optional<ContextAwarePredicate> pPlayer) {
			super(pPlayer);
		}
		
	}
}
