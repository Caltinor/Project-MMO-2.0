package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.core.Core;
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
		Entity player = t.getParamOrNull(LootContextParams.KILLER_ENTITY);
		if (player == null || skill == null) return false;
		
		long actualLevel = Core.get(player.level()).getData().getLevel(skill, player.getUUID());
		
		return (levelMin == null || actualLevel >= levelMin) && (levelMax == null || actualLevel <= levelMax);
	}

	public String getSkill() {return skill;}

	public Integer getLevelMin() {return levelMin;}

	public Integer getLevelMax() {return levelMax;}

	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.SKILL_KILL.get();
	}

	public static final Codec<SkillLootConditionKill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("level_min").forGetter(SkillLootConditionKill::getLevelMin),
			Codec.INT.fieldOf("level_max").forGetter(SkillLootConditionKill::getLevelMax),
			Codec.STRING.fieldOf("skill").forGetter(SkillLootConditionKill::getSkill)
	).apply(instance, SkillLootConditionKill::new));
}
