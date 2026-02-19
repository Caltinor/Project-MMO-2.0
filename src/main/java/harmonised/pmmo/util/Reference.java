package harmonised.pmmo.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Reference {
	public static final String MOD_ID = "pmmo";

	public static Identifier rl(String path) {return Identifier.fromNamespaceAndPath(MOD_ID, path);}
	public static Identifier rl(String name, String path) {return Identifier.fromNamespaceAndPath(name, path);}
	public static Identifier mc(String path) {return Identifier.withDefaultNamespace(path);}
	public static Identifier of(String path) {return Identifier.tryParse(path);}
	
	public static final UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public static final String API_MAP_SERIALIZER_KEY = "key";
	public static final String API_MAP_SERIALIZER_VALUE = "value";
	
	public static final Identifier CREATIVE_REACH_ATTRIBUTE = Reference.rl("creative_reach");

	public static final TagKey<EntityType<?>> MOB_TAG = TagKey.create(Registries.ENTITY_TYPE, rl("mobs"));
	public static final TagKey<EntityType<?>> TAMABLE_TAG = TagKey.create(Registries.ENTITY_TYPE, rl("tamable"));
	public static final TagKey<EntityType<?>> BREEDABLE_TAG = TagKey.create(Registries.ENTITY_TYPE, rl("breedable"));
	public static final TagKey<EntityType<?>> RIDEABLE_TAG = TagKey.create(Registries.ENTITY_TYPE, rl("rideable"));
	public static final TagKey<EntityType<?>> NO_XP_DAMAGE_DEALT = TagKey.create(Registries.ENTITY_TYPE, rl("noxp_damage_dealt"));
	public static final TagKey<Block> CROPS = TagKey.create(Registries.BLOCK, rl("crops"));
	public static final TagKey<Block> CASCADING_BREAKABLES = TagKey.create(Registries.BLOCK, rl("cascading_breakables"));
	public static final TagKey<Block> MINABLE_AXE = TagKey.create(Registries.BLOCK, mc("mineable/axe"));
	public static final TagKey<Block> MINABLE_HOE = TagKey.create(Registries.BLOCK, mc("mineable/hoe"));
	public static final TagKey<Block> MINABLE_SHOVEL = TagKey.create(Registries.BLOCK, mc("mineable/shovel"));
	public static final TagKey<Item> BREWABLES = TagKey.create(Registries.ITEM, rl("brewables"));
	public static final TagKey<Item> SMELTABLES = TagKey.create(Registries.ITEM, rl("smeltables"));
	public static final TagKey<DamageType> FROM_ENVIRONMENT = TagKey.create(Registries.DAMAGE_TYPE, rl("environment"));
	public static final TagKey<DamageType> FROM_IMPACT = TagKey.create(Registries.DAMAGE_TYPE, rl("impact"));
	public static final TagKey<DamageType> FROM_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, rl("magic"));
	public static final TagKey<DamageType> FROM_GUN = TagKey.create(Registries.DAMAGE_TYPE, rl("gun"));

	public static final List<EquipmentSlot> ARMOR_SLOTS = Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).toList();
}
