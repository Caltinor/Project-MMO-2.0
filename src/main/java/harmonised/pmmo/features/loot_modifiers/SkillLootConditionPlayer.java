package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.core.Core;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class SkillLootConditionPlayer implements LootItemCondition{
	public String skill;
	public Integer levelMin, levelMax;
	
	public SkillLootConditionPlayer(Integer levelMin, Integer levelMax, String skill) {
		this.levelMin = levelMin;
		this.levelMax = levelMax;
		this.skill = skill;
	}
	
	@Override
	public boolean test(LootContext t) {
		Entity player = t.getOptionalParameter(LootContextParams.THIS_ENTITY);
		if (player == null || skill == null) return false;
		
		long actualLevel = Core.get(player.level()).getData().getLevel(skill, player.getUUID());
		
		return (levelMin == null || actualLevel >= levelMin) && (levelMax == null || actualLevel <= levelMax);
	}

	public String getSkill() {return skill;}

	public Integer getLevelMin() {return levelMin;}

	public Integer getLevelMax() {return levelMax;}
	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.SKILL_PLAYER.get();
	}

	public static final MapCodec<SkillLootConditionPlayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("level_min").forGetter(SkillLootConditionPlayer::getLevelMin),
			Codec.INT.fieldOf("level_max").forGetter(SkillLootConditionPlayer::getLevelMax),
			Codec.STRING.fieldOf("skill").forGetter(SkillLootConditionPlayer::getSkill)
	).apply(instance, SkillLootConditionPlayer::new));
}
