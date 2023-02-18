package harmonised.pmmo.features.loot_modifiers;

import javax.annotation.Nonnull;

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

public class SkillLootConditionKill implements LootItemCondition{

	private String skill;
	private Integer levelMin, levelMax;
	
	public SkillLootConditionKill(Integer levelMin, Integer levelMax, String skill) {
		this.levelMin = levelMin;
		this.levelMax = levelMax;
		this.skill = skill;
	}
	
	@Override
	public boolean test(LootContext t) {
		if (!Config.TREASURE_ENABLED.get()) return false;
		Entity player = t.getParamOrNull(LootContextParams.KILLER_ENTITY);
		if (player == null || skill == null) return false;
		
		int actualLevel = Core.get(player.level).getData().getPlayerSkillLevel(skill, player.getUUID());
		
		return (levelMin == null || actualLevel >= levelMin) && (levelMax == null || actualLevel <= levelMax);
	}

	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.SKILL_KILL.get();
	}

	public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SkillLootConditionKill> {
        public void serialize(JsonObject json, SkillLootConditionKill itemCondition, @Nonnull JsonSerializationContext context) {
            json.addProperty("skill", itemCondition.skill);
            json.addProperty("level_min", itemCondition.levelMin);
            json.addProperty("level_max", itemCondition.levelMax);
        }

        @Nonnull
        public SkillLootConditionKill deserialize(JsonObject json, @Nonnull JsonDeserializationContext context) {
        	Integer levelMin = GsonHelper.getAsInt(json, "level_min");
        	Integer levelMax = GsonHelper.getAsInt(json, "level_max");
        	String skill = GsonHelper.getAsString(json, "skill");
            return new SkillLootConditionKill(levelMin, levelMax, skill);
        }
    }
}
