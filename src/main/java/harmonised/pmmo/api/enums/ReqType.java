package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum ReqType implements StringRepresentable, IExtensibleEnum {
    WEAR(true, false, false),
    USE_ENCHANTMENT(true, false, false),
    TOOL(true, false, false),
    WEAPON(true, false, false),
    USE(true, true, false),
    PLACE(true, true, false),
    BREAK(true, true, false),
    BIOME(false, false, false),
    KILL(false, false, true),
    TRAVEL(false, false, false), 
    RIDE(false, false, true),
    TAME(false, false, true),
    BREED(false, false, true),
    ENTITY_INTERACT(false, false, true);
	
	
	ReqType(boolean itemApplicable, boolean blockApplicable, boolean entityApplicable) {
		this.itemApplicable = itemApplicable;
		this.blockApplicable = blockApplicable;
		this.entityApplicable = entityApplicable;
	}
	
	public final boolean itemApplicable;
	public final boolean blockApplicable;
	public final boolean entityApplicable;
	
	public static final ReqType[] ITEM_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.itemApplicable).toArray(ReqType[]::new);
	public static final ReqType[] BLOCK_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.blockApplicable).toArray(ReqType[]::new);
	public static final ReqType[] ENTITY_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.entityApplicable).toArray(ReqType[]::new);

	public static final Codec<ReqType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ReqType::values, ReqType::byName);
	private static final Map<String, ReqType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ReqType::getSerializedName, s -> {return s;}));
	public static ReqType byName(String name) {return BY_NAME.get(name);}
	@Override
	public String getSerializedName() {return this.name();}
	public static ReqType create(String name, boolean itemApplicable, boolean blockApplicable, boolean entityApplicable) {throw new IllegalStateException("Enum not extended");}
}
