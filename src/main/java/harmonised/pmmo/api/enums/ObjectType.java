package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import harmonised.pmmo.config.writers.PackGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum ObjectType implements StringRepresentable{
	ITEM(Registries.ITEM, PackGenerator.Category.ITEMS),
	BLOCK(Registries.BLOCK, PackGenerator.Category.BLOCKS),
	ENTITY(Registries.ENTITY_TYPE, PackGenerator.Category.ENTITIES),
	DIMENSION(Registries.DIMENSION, PackGenerator.Category.DIMENSIONS),
	BIOME(Registries.BIOME, PackGenerator.Category.BIOMES),
	ENCHANTMENT(Registries.ENCHANTMENT, PackGenerator.Category.ENCHANTMENTS),
	EFFECT(Registries.MOB_EFFECT, PackGenerator.Category.EFFECTS),
	PLAYER(Registries.ENTITY_TYPE, PackGenerator.Category.ENTITIES);

	public final ResourceKey<? extends Registry<?>> key;
	public final PackGenerator.Category category;
	ObjectType(ResourceKey<? extends Registry<?>> key, PackGenerator.Category category) {
		this.key = key;
		this.category = category;
	}
	public static final Codec<ObjectType> CODEC = StringRepresentable.fromEnum(ObjectType::values);
	private static final Map<String, ObjectType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ObjectType::getSerializedName, s -> s));
	public static ObjectType byName(String name) {return BY_NAME.get(name);}

	@Override
	public String getSerializedName() {return this.name();}
}
