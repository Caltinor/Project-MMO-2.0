package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ModifierDataType implements StringRepresentable, GuiEnumGroup {
    BIOME(LangProvider.BIOME_HEADER),
    HELD(LangProvider.BOOST_HELD),
    WORN(LangProvider.BOOST_WORN),
    DIMENSION(LangProvider.DIMENSION_HEADER);

	public final LangProvider.Translation tooltip;
	ModifierDataType(LangProvider.Translation tooltip) {this.tooltip = tooltip;}
	public static final Codec<ModifierDataType> CODEC = StringRepresentable.fromEnum(ModifierDataType::values);
	private static final Map<String, ModifierDataType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ModifierDataType::getSerializedName, s -> {return s;}));
	public static ModifierDataType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}

	@Override
	public String getName() {return name();}
}
