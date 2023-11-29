package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GLMRegistry {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE.key(), Reference.MOD_ID);
	
	public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<TreasureLootModifier>> TREASURE = GLM.register("treasure", () -> TreasureLootModifier.CODEC);
	public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<RareDropModifier>> RARE_DROP = GLM.register("rare_drop", () -> RareDropModifier.CODEC);
	
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> SKILL_PLAYER = CONDITIONS.register("skill_level",
			() -> new LootItemConditionType(SkillLootConditionPlayer.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> SKILL_KILL = CONDITIONS.register("skill_level_kill",
			() -> new LootItemConditionType(SkillLootConditionKill.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> HIGHEST_SKILL = CONDITIONS.register("highest_skill",
			() -> new LootItemConditionType(SkillLootConditionHighestSkill.CODEC));
	public static final DeferredHolder<LootItemConditionType, LootItemConditionType> VALID_BLOCK = CONDITIONS.register("valid_block",
			() -> new LootItemConditionType(ValidBlockCondition.CODEC));
}
