package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GLMRegistry {
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.key(), Reference.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE.key(), Reference.MOD_ID);
	
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<TreasureLootModifier>> TREASURE = GLM.register("treasure", () -> TreasureLootModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RareDropModifier>> RARE_DROP = GLM.register("rare_drop", () -> RareDropModifier.CODEC);
	
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> SKILL_PLAYER = CONDITIONS.register("skill_level",
			() -> new LootItemConditionType(SkillLootConditionPlayer.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> SKILL_KILL = CONDITIONS.register("skill_level_kill",
			() -> new LootItemConditionType(SkillLootConditionKill.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> HIGHEST_SKILL = CONDITIONS.register("highest_skill",
			() -> new LootItemConditionType(SkillLootConditionHighestSkill.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> VALID_BLOCK = CONDITIONS.register("valid_block",
			() -> new LootItemConditionType(ValidBlockCondition.CODEC));
}
