package harmonised.pmmo.features.loot_modifiers;

import com.google.gson.JsonObject;

import harmonised.pmmo.util.Reference;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SkillUpTrigger extends SimpleCriterionTrigger<SkillUpTrigger.TriggerInstance>{	
	public static final SkillUpTrigger SKILL_UP = new SkillUpTrigger();
	private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "skill_up");
	
	public SkillUpTrigger() {}

	@Override
	public ResourceLocation getId() {return ID;}

	@Override
	protected TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPlayer, DeserializationContext pContext) {
		return new TriggerInstance(getId(), pPlayer);
	}
	
	public void trigger(ServerPlayer player) {
		this.trigger(player, p -> true);
	}
	
	public static class TriggerInstance extends AbstractCriterionTriggerInstance {

		public TriggerInstance(ResourceLocation pCriterion, ContextAwarePredicate pPlayer) {
			super(pCriterion, pPlayer);
		}
		
	}
}
