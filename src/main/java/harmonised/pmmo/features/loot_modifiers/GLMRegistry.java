package harmonised.pmmo.features.loot_modifiers;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
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
	
	public static GlobalLootModifierSerializer<TreasureLootModifier> TREASURE_SERIALIZER = new TreasureLootModifier.Serializer().setRegistryName(new ResourceLocation(Reference.MOD_ID, "treasure"));
	public static GlobalLootModifierSerializer<RareDropModifier> RARE_DROP_SERIALIZER = new RareDropModifier.Serializer().setRegistryName(new ResourceLocation(Reference.MOD_ID, "rare_drop"));
	
	@SubscribeEvent
	public static void registerSerializer(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
	    event.getRegistry().register(TREASURE_SERIALIZER);
	    event.getRegistry().register(RARE_DROP_SERIALIZER);
	}
}
