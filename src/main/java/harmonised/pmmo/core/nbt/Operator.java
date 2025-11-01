package harmonised.pmmo.core.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Operator implements StringRepresentable{
	EQUALS,
	GREATER_THAN,
	LESS_THAN,
	GREATER_THAN_OR_EQUAL,
	LESS_THAN_OR_EQUAL,
	EXISTS,
	CONTAINS;
	
	public static final Codec<Operator> CODEC = StringRepresentable.fromEnum(Operator::values);
	private static final Map<String, Operator> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Operator::getSerializedName, s -> s));
	public static Operator byName(String name) {return BY_NAME.get(name);}
	
	@Override
	public String getSerializedName() {return this.name();}
	public static Operator create(String name) {throw new IllegalStateException("Enum not extended");}
}
