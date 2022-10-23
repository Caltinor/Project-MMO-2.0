package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum ModifierDataType implements StringRepresentable, IExtensibleEnum, GuiEnumGroup {
    BIOME,
    HELD,
    WORN,
    DIMENSION;
	
	public static final Codec<ModifierDataType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ModifierDataType::values, ModifierDataType::byName);
	private static final Map<String, ModifierDataType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ModifierDataType::getSerializedName, s -> {return s;}));
	public static ModifierDataType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static ModifierDataType create(String name) {
	     throw new IllegalStateException("Enum not extended");
	}

	@Override
	public String getName() {return name();}
}
