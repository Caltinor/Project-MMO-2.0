package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum ReqType implements StringRepresentable, IExtensibleEnum {
    WEAR,
    USE_ENCHANTMENT,
    TOOL,
    WEAPON,
    USE,
    PLACE,
    BREAK,
    BIOME,
    KILL,
    TRAVEL, 
    RIDE,
    TAME,
    ENTITY_INTERACT;

	public static final Codec<ReqType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ReqType::values, ReqType::byName);
	private static final Map<String, ReqType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ReqType::getSerializedName, s -> {return s;}));
	public static ReqType byName(String name) {return BY_NAME.get(name);}
	@Override
	public String getSerializedName() {return this.name();}
	public static ReqType create(String name) {throw new IllegalStateException("Enum not extended");}
}
