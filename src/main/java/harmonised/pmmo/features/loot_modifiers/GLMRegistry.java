package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.MapCodec;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GLMRegistry {
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.key(), Reference.MOD_ID);
	public static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE.key(), Reference.MOD_ID);
	
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<TreasureLootModifier>> TREASURE = GLM.register("treasure", () -> TreasureLootModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RareDropModifier>> RARE_DROP = GLM.register("rare_drop", () -> RareDropModifier.CODEC);
	
	public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> SKILL_PLAYER = CONDITIONS.register("skill_level",
			() -> SkillLootConditionPlayer.CODEC);
	public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> SKILL_KILL = CONDITIONS.register("skill_level_kill",
			() -> SkillLootConditionKill.CODEC);
	public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> HIGHEST_SKILL = CONDITIONS.register("highest_skill",
			() -> SkillLootConditionHighestSkill.CODEC);
	public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> VALID_BLOCK = CONDITIONS.register("valid_block",
			() -> ValidBlockCondition.CODEC);
}
