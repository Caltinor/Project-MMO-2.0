package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GLMRegistry {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE.key(), Reference.MOD_ID);
	
	public static final RegistryObject<Codec<TreasureLootModifier>> TREASURE = GLM.register("treasure", () -> TreasureLootModifier.CODEC);
	public static final RegistryObject<Codec<RareDropModifier>> RARE_DROP = GLM.register("rare_drop", () -> RareDropModifier.CODEC);
	
	public static final RegistryObject<LootItemConditionType> SKILL_PLAYER = CONDITIONS.register("skill_level", 
			() -> new LootItemConditionType(new SkillLootConditionPlayer.Serializer()));
	public static final RegistryObject<LootItemConditionType> SKILL_KILL = CONDITIONS.register("skill_level_kill", 
			() -> new LootItemConditionType(new SkillLootConditionKill.Serializer()));
	public static final RegistryObject<LootItemConditionType> HIGHEST_SKILL = CONDITIONS.register("highest_skill", 
			() -> new LootItemConditionType(new SkillLootConditionHighestSkill.Serializer()));
	public static final RegistryObject<LootItemConditionType> VALID_BLOCK = CONDITIONS.register("valid_block", 
			() -> new LootItemConditionType(new ValidBlockCondition.Serializer()));
}
