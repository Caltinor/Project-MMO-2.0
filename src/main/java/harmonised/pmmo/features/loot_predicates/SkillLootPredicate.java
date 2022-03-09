package harmonised.pmmo.features.loot_predicates;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class SkillLootPredicate implements LootItemCondition{
	public static final LootItemConditionType BROKEN_BY_PLAYER = new LootItemConditionType(new SkillLootPredicate.Serializer());
	
	public static final LootContextParam<String> SKILL = new LootContextParam<>(new ResourceLocation(Reference.MOD_ID, "skill"));
	public static final LootContextParam<Integer> LEVEL = new LootContextParam<>(new ResourceLocation(Reference.MOD_ID,"level"));

	@Override
	public boolean test(LootContext t) {
		Integer level = t.getParamOrNull(LEVEL);
		String skill = t.getParamOrNull(SKILL);
		Entity player = t.getParamOrNull(LootContextParams.THIS_ENTITY);
		return false;
	}

	@Override
	public LootItemConditionType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SkillLootPredicate> {
        public void serialize(JsonObject json, SkillLootPredicate itemCondition, @Nonnull JsonSerializationContext context) {
            //json.addProperty("action", itemCondition.action.name());
        }

        @Nonnull
        public SkillLootPredicate deserialize(JsonObject json, @Nonnull JsonDeserializationContext context) {
            return new SkillLootPredicate();//ToolAction.get(json.get("action").getAsString()));
        }
    }
}
