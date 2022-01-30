package harmonised.pmmo.config.readers;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum XpValueDataType implements StringRepresentable, IExtensibleEnum{
    BONUS_BIOME,
    BONUS_HELD,
    BONUS_WORN,
    BONUS_DIMENSION;
    
	public static final XpValueDataType[] modifierTypes = new XpValueDataType[]{BONUS_BIOME, BONUS_HELD, BONUS_WORN, BONUS_DIMENSION};
	
	public static final Codec<XpValueDataType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(XpValueDataType::values, XpValueDataType::byName);
	private static final Map<String, XpValueDataType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(XpValueDataType::getSerializedName, s -> {return s;}));
	public static XpValueDataType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static XpValueDataType create(String name) {
	     throw new IllegalStateException("Enum not extended");
	}
}
