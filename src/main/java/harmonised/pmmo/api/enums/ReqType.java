package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum ReqType implements StringRepresentable, IExtensibleEnum {
    REQ_WEAR,
    //REQ_USE_ENCHANTMENT,   move this to an NBT config
    REQ_TOOL,
    REQ_WEAPON,
    REQ_USE,
    REQ_PLACE,
    REQ_BREAK,
    REQ_BIOME,
    REQ_KILL,
    REQ_CRAFT,
    REQ_TRAVEL,
    REQ_ENTITY_INTERACT;

	public static final Codec<ReqType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ReqType::values, ReqType::byName);
	private static final Map<String, ReqType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ReqType::getSerializedName, s -> {return s;}));
	public static ReqType byName(String name) {return BY_NAME.get(name);}
	@Override
	public String getSerializedName() {return this.name();}
	public static ReqType create(String name) {throw new IllegalStateException("Enum not extended");}
}
