package harmonised.pmmo.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class RegistryUtil {
	public static ResourceLocation getId(ItemStack stack) {
		return getId(stack.getItem());
	}

	public static ResourceLocation getId(Item item) {
		return ForgeRegistries.ITEMS.getKey(item);
	}

	public static ResourceLocation getId(BlockState blockState) {
		return getId(blockState.getBlock());
	}

	public static ResourceLocation getId(Block block) {
		return ForgeRegistries.BLOCKS.getKey(block);
	}

	public static ResourceLocation getId(Holder<Biome> biome) {
		return biome.unwrapKey().get().location();
	}

	public static ResourceLocation getId(SoundEvent sound) {
		return ForgeRegistries.SOUND_EVENTS.getKey(sound);
	}

	public static ResourceLocation getId(Entity entity) {
		return getId(entity.getType());
	}

	public static ResourceLocation getId(EntityType<?> entity) {
		return ForgeRegistries.ENTITY_TYPES.getKey(entity);
	}
	
	public static ResourceLocation getId(Enchantment enchant) {
		return ForgeRegistries.ENCHANTMENTS.getKey(enchant);
	}

	public static ResourceLocation getId(MobEffect effect) {
		return ForgeRegistries.MOB_EFFECTS.getKey(effect);
	}

	public static ResourceLocation getId(DamageSource source) {
		return source.typeHolder().unwrapKey().get().location();
	}

	public static <T> boolean isInTag(RegistryAccess access, ResourceKey<Registry<T>> registry, String objectID, String tagId) {
		return isInTag(access, registry, new ResourceLocation(objectID), tagId);
	}
	public static <T> boolean isInTag(RegistryAccess access, ResourceKey<Registry<T>> registry, ResourceLocation objectID, String tagId) {
		var reg = access.registryOrThrow(registry);
		var tag = TagKey.create(registry, new ResourceLocation(tagId));
		var holder = reg.getHolder(ResourceKey.create(registry, objectID));
		return holder.map(h -> h.is(tag)).orElse(false);
	}
}
