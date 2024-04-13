package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.IExtensibleEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum ObjectType implements StringRepresentable, IExtensibleEnum {
	ITEM,
	BLOCK,
	ENTITY,
	DIMENSION,
	BIOME,
	ENCHANTMENT,
	EFFECT,
	PLAYER;
	
	public static final Codec<ObjectType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ObjectType::values, ObjectType::byName);
	private static final Map<String, ObjectType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ObjectType::getSerializedName, s -> s));
	public static ObjectType byName(String name) {return BY_NAME.get(name);}
	
	@Override
	public String getSerializedName() {return this.name();}
	public static ObjectType create(String name) {throw new IllegalStateException("Enum not extended");}
}
