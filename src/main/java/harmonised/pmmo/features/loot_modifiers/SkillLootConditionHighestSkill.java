package harmonised.pmmo.features.loot_modifiers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class SkillLootConditionHighestSkill implements LootItemCondition{

	private String targetSkill;
	private List<String> comparables;
	
	public SkillLootConditionHighestSkill(String skill, List<String> comparables) {
		this.targetSkill = skill;
		this.comparables = comparables;
	}
	
	@Override
	public boolean test(LootContext t) {
		if (!Config.TREASURE_ENABLED.get()) return false;
		Entity player = t.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (player == null || targetSkill == null || comparables.isEmpty()) return false;
		
		int targetLevel = Core.get(player.level()).getData().getPlayerSkillLevel(targetSkill, player.getUUID());
		for (String comparable : comparables) {
			if (comparable.equals(targetSkill)) continue;
			if (targetLevel < Core.get(player.level()).getData().getPlayerSkillLevel(comparable, player.getUUID()))
				return false;
		}
		
		return true;
	}

	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.HIGHEST_SKILL.get();
	}

	public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SkillLootConditionHighestSkill> {
        public void serialize(JsonObject json, SkillLootConditionHighestSkill itemCondition, @Nonnull JsonSerializationContext context) {
            json.addProperty("target_skill", itemCondition.targetSkill);
            JsonArray list = new JsonArray();
            for (String skill : itemCondition.comparables) {
            	list.add(skill);
            }
            json.add("comparable_skills", list);
        }

        @Nonnull
        public SkillLootConditionHighestSkill deserialize(JsonObject json, @Nonnull JsonDeserializationContext context) {
        	String targetSkill = GsonHelper.getAsString(json, "target_skill");
        	JsonArray list = GsonHelper.getAsJsonArray(json, "comparable_skills");
        	List<String> comparables = new ArrayList<>();
        	for (int i = 0; i < list.size(); i++) {
        		comparables.add(list.get(i).getAsString());
        	}
            return new SkillLootConditionHighestSkill(targetSkill, comparables);
        }
    }
}
