package harmonised.pmmo.core.nbt;

import com.mojang.serialization.Codec;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Operator implements StringRepresentable{
	EQUALS(LangProvider.GLOSSARY_NBT_OP_EQ),
	GREATER_THAN(LangProvider.GLOSSARY_NBT_OP_GT),
	LESS_THAN(LangProvider.GLOSSARY_NBT_OP_LT),
	GREATER_THAN_OR_EQUAL(LangProvider.GLOSSARY_NBT_OP_GTOE),
	LESS_THAN_OR_EQUAL(LangProvider.GLOSSARY_NBT_OP_LTOE),
	EXISTS(LangProvider.GLOSSARY_NBT_OP_EXISTS),
	CONTAINS(LangProvider.GLOSSARY_NBT_OP_CONTAINS);

	public LangProvider.Translation translation;
	Operator(LangProvider.Translation translation) {this.translation = translation;}
	public static final Codec<Operator> CODEC = StringRepresentable.fromEnum(Operator::values);
	private static final Map<String, Operator> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Operator::getSerializedName, s -> s));
	public static Operator byName(String name) {return BY_NAME.get(name);}
	
	@Override
	public String getSerializedName() {return this.name();}
	public static Operator create(String name) {throw new IllegalStateException("Enum not extended");}
}
