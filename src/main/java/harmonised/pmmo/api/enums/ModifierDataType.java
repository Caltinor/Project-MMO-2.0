package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.IExtensibleEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ModifierDataType implements StringRepresentable, IExtensibleEnum, GuiEnumGroup {
    BIOME(LangProvider.BIOME_HEADER),
    HELD(LangProvider.BOOST_HELD),
    WORN(LangProvider.BOOST_WORN),
    DIMENSION(LangProvider.DIMENSION_HEADER);

	public final LangProvider.Translation tooltip;
	ModifierDataType(LangProvider.Translation tooltip) {this.tooltip = tooltip;}
	public static final Codec<ModifierDataType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ModifierDataType::values, ModifierDataType::byName);
	private static final Map<String, ModifierDataType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ModifierDataType::getSerializedName, s -> {return s;}));
	public static ModifierDataType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static ModifierDataType create(String name, LangProvider.Translation translation) {
	     throw new IllegalStateException("Enum not extended");
	}

	@Override
	public String getName() {return name();}
}
