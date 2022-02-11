package harmonised.pmmo.core.nbt;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum Operator implements StringRepresentable, IExtensibleEnum{
	EQUALS,
	GREATER_THAN,
	LESS_THAN,
	GREATER_THAN_OR_EQUAL,
	LESS_THAN_OR_EQUAL,
	EXISTS;
	
	public static final Codec<Operator> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(Operator::values, Operator::byName);
	private static final Map<String, Operator> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Operator::getSerializedName, s -> s));
	public static Operator byName(String name) {return BY_NAME.get(name);}
	
	@Override
	public String getSerializedName() {return this.name();}
	public static Operator create(String name) {throw new IllegalStateException("Enum not extended");}
}
