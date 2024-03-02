package harmonised.pmmo.features.loot_modifiers;

import java.util.List;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
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
		Entity player = t.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (player == null || targetSkill == null || comparables.isEmpty()) return false;
		
		long targetLevel = Core.get(player.level()).getData().getLevel(targetSkill, player.getUUID());
		for (String comparable : comparables) {
			if (comparable.equals(targetSkill)) continue;
			if (targetLevel < Core.get(player.level()).getData().getLevel(comparable, player.getUUID()))
				return false;
		}
		
		return true;
	}

	public String getTargetSkill() {return targetSkill;}

	public List<String> getComparables() {return comparables;}
	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.HIGHEST_SKILL.get();
	}

	public static final Codec<SkillLootConditionHighestSkill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("target_skill").forGetter(SkillLootConditionHighestSkill::getTargetSkill),
			Codec.list(Codec.STRING).fieldOf("comparable_skills").forGetter(SkillLootConditionHighestSkill::getComparables)
	).apply(instance, SkillLootConditionHighestSkill::new));
}
