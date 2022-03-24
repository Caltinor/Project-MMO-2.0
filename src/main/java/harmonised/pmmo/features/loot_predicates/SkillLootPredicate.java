package harmonised.pmmo.features.loot_predicates;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class SkillLootPredicate implements LootItemCondition{
	public static final LootItemConditionType SKILL_LEVEL_CONDITION = new LootItemConditionType(new SkillLootPredicate.Serializer());
	
	public static final LootContextParam<String> SKILL = new LootContextParam<>(new ResourceLocation(Reference.MOD_ID, "skill"));
	public static final LootContextParam<Integer> LEVEL_MIN = new LootContextParam<>(new ResourceLocation(Reference.MOD_ID,"level_min"));
	public static final LootContextParam<Integer> LEVEL_MAX = new LootContextParam<>(new ResourceLocation(Reference.MOD_ID,"level_max"));

	private String skill;
	private Integer levelMin, levelMax;
	
	public SkillLootPredicate(Integer levelMin, Integer levelMax, String skill) {
		this.levelMin = levelMin;
		this.levelMax = levelMax;
		this.skill = skill;
	}
	
	@Override
	public boolean test(LootContext t) {
		levelMin = t.getParamOrNull(LEVEL_MIN);
		levelMax = t.getParamOrNull(LEVEL_MAX);
		skill = t.getParamOrNull(SKILL);
		Entity player = t.getParamOrNull(LootContextParams.KILLER_ENTITY);
		if (player == null || skill == null || levelMin == null) return false;
		int actualLevel = Core.get(player.level).getData().getPlayerSkillLevel(skill, player.getUUID());
		boolean max = true;
		if (levelMax != null)
			max = actualLevel <= levelMax;
		
		return actualLevel >= levelMin && max;
	}

	@Override
	public LootItemConditionType getType() {
		return SKILL_LEVEL_CONDITION;
	}

	public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SkillLootPredicate> {
        public void serialize(JsonObject json, SkillLootPredicate itemCondition, @Nonnull JsonSerializationContext context) {
            json.addProperty("skill", itemCondition.skill);
            json.addProperty("level_min", itemCondition.levelMin);
            json.addProperty("level_max", itemCondition.levelMax);
        }

        @Nonnull
        public SkillLootPredicate deserialize(JsonObject json, @Nonnull JsonDeserializationContext context) {
        	Integer levelMin = GsonHelper.getAsInt(json, "level_min");
        	Integer levelMax = GsonHelper.getAsInt(json, "level_max");
        	String skill = GsonHelper.getAsString(json, "skill");
            return new SkillLootPredicate(levelMin, levelMax, skill);
        }
    }
}
