package harmonised.pmmo.features.loot_modifiers;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GLMRegistry {
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(Registry.LOOT_CONDITION_TYPE.key(), Reference.MOD_ID);
	
	public static final RegistryObject<TreasureLootModifier.Serializer> TREASURE = GLM.register("treasure", TreasureLootModifier.Serializer::new);
	public static final RegistryObject<RareDropModifier.Serializer> RARE_DROP = GLM.register("rare_drop", RareDropModifier.Serializer::new);
	
	public static final RegistryObject<LootItemConditionType> SKILL_PLAYER = CONDITIONS.register("skill_level", 
			() -> new LootItemConditionType(new SkillLootConditionPlayer.Serializer()));
	public static final RegistryObject<LootItemConditionType> SKILL_KILL = CONDITIONS.register("skill_level_kill", 
			() -> new LootItemConditionType(new SkillLootConditionKill.Serializer()));
	public static final RegistryObject<LootItemConditionType> HIGHEST_SKILL = CONDITIONS.register("highest_skill", 
			() -> new LootItemConditionType(new SkillLootConditionHighestSkill.Serializer()));
	public static final RegistryObject<LootItemConditionType> VALID_BLOCK = CONDITIONS.register("valid_block", 
			() -> new LootItemConditionType(new ValidBlockCondition.Serializer()));
}
