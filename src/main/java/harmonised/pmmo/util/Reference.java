package harmonised.pmmo.util;

import java.util.UUID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class Reference {
	public static final String MOD_ID = "pmmo";
	
	public static final UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public static final String API_MAP_SERIALIZER_KEY = "key";
	public static final String API_MAP_SERIALIZER_VALUE = "value";
	
	public static final UUID CREATIVE_REACH_ATTRIBUTE = UUID.fromString("c97b8776-05c8-4dbe-835c-10211ad4aba6");
	
	public static final TagKey<EntityType<?>> MOB_TAG = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(MOD_ID, "mobs"));
	public static final TagKey<EntityType<?>> ANIMAL_TAG = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(MOD_ID, "animals"));
	public static final TagKey<EntityType<?>> TAMABLE_TAG = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(MOD_ID, "tamable"));
	public static final TagKey<EntityType<?>> BREEDABLE_TAG = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(MOD_ID, "breedable"));
	public static final TagKey<EntityType<?>> RIDEABLE_TAG = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(MOD_ID, "rideable"));
	public static final TagKey<Block> CROPS = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MOD_ID, "crops"));
	public static final TagKey<Block> CASCADING_BREAKABLES = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MOD_ID, "cascading_breakables"));
	public static final TagKey<Block> MINABLE_AXE = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("mineable/axe"));
	public static final TagKey<Block> MINABLE_HOE = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("mineable/hoe"));
	public static final TagKey<Block> MINABLE_SHOVEL = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("mineable/shovel"));
	public static final TagKey<Item> BREWABLES = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MOD_ID, "brewables"));
	public static final TagKey<Item> SMELTABLES = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MOD_ID, "smeltables"));
	public static final TagKey<DamageType> FROM_PLAYER = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "player"));
	public static final TagKey<DamageType> FROM_ENVIRONMENT = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "environment"));
	public static final TagKey<DamageType> FROM_IMPACT = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "impact"));
	public static final TagKey<DamageType> FROM_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "magic"));
	public static final TagKey<DamageType> FROM_MELEE = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "melee"));
	public static final TagKey<DamageType> FROM_RANGED = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "ranged"));
}
