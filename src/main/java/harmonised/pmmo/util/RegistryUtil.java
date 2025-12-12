package harmonised.pmmo.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RegistryUtil {
	public static ResourceLocation getId(RegistryAccess access, ItemStack stack) {
		return getId(access, stack.getItem());
	}

	public static ResourceLocation getId(RegistryAccess access, Item item) {return getId(access, Registries.ITEM, item);}

	public static ResourceLocation getId(RegistryAccess access, Entity entity) {return getId(access, Registries.ENTITY_TYPE, entity.getType());}

	public static ResourceLocation getId(BlockState blockState) {
		return getId(blockState.getBlock());
	}

	public static ResourceLocation getId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	public static ResourceLocation getId(Holder<?> holder) {
		return holder.unwrapKey().get().location();
	}

	public static ResourceLocation getId(SoundEvent sound) {
		return BuiltInRegistries.SOUND_EVENT.getKey(sound);
	}

	public static ResourceLocation getId(Entity entity) {
		return getId(entity.getType());
	}

	public static ResourceLocation getId(EntityType<?> entity) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity);
	}

	public static ResourceLocation getId(MobEffect effect) {
		return BuiltInRegistries.MOB_EFFECT.getKey(effect);
	}

	public static <T> ResourceLocation getId(RegistryAccess access, ResourceKey<Registry<T>> registry, T source) {
		return access.registryOrThrow(registry).getKey(source);
	}

	public static ResourceLocation getAttributeId(Holder<Attribute> attribute) {
		return BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
	}

	public static <T> boolean isInTag(RegistryAccess access, ResourceKey<Registry<T>> registry, String objectID, String tagId) {
		return isInTag(access, registry, Reference.of(objectID), tagId);
	}
	public static <T> boolean isInTag(RegistryAccess access, ResourceKey<Registry<T>> registry, ResourceLocation objectID, String tagId) {
		var reg = access.registryOrThrow(registry);
		var tag = TagKey.create(registry, Reference.of(tagId));
		var holder = reg.getHolder(ResourceKey.create(registry, objectID));
		return holder.map(h -> h.is(tag)).orElse(false);
	}
}
