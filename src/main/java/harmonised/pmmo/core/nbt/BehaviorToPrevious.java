package harmonised.pmmo.core.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum BehaviorToPrevious implements StringRepresentable{
	ADD_TO,
	SUB_FROM,
	HIGHEST,
	REPLACE;
	
	public static final Codec<BehaviorToPrevious> CODEC = StringRepresentable.fromEnum(BehaviorToPrevious::values);
	private static final Map<String, BehaviorToPrevious> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(BehaviorToPrevious::getSerializedName, s -> s));
	public static BehaviorToPrevious byName(String name) {return BY_NAME.get(name);}
	
	@Override
	public String getSerializedName() {return this.name();}
	public static BehaviorToPrevious create(String name) {throw new IllegalStateException("Enum not extended");}
}
