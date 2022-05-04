package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum ReqType implements StringRepresentable, IExtensibleEnum {
    WEAR(true, false, false, "endurance"),				//PLAYER TICK
    USE_ENCHANTMENT(true, false, false, "magic"),		//PLAYER TICK
    TOOL(true, false, false, "mining"),					//IMPLEMENTED IN ACTIONS
    WEAPON(true, false, false, "combat"),				//IMPLEMENTED IN ACTIONS
    USE(true, true, false, "crafting"),					//IMPLEMENTED IN ACTIONS
    PLACE(true, true, false, "building"),				//IMPLEMENTED IN ACTIONS
    BREAK(true, true, false, "mining"),					//IMPLEMENTED IN ACTIONS
    KILL(false, false, true, "combat"),					//IMPLEMENTED IN ACTIONS
    TRAVEL(false, false, false,"agility"), 				//TRAVEL EVENT
    RIDE(false, false, true, "farming"),				//IMPLEMENTED IN EVENT
    TAME(false, false, true, "taming"),					//IMPLEMENTED IN ACTIONS
    BREED(false, false, true, "farming"),				//IMPLEMENTED IN ACTIONS
    INTERACT(true, true, false, "engineering"),			//IMPLEMENTED IN ACTOINS
    ENTITY_INTERACT(false, false, true, "farming");		//IMPLEMENTED IN ACTIONS
	
	
	ReqType(boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String defaultSkill) {
		this.itemApplicable = itemApplicable;
		this.blockApplicable = blockApplicable;
		this.entityApplicable = entityApplicable;
		this.defaultSkill = defaultSkill;
	}
	
	public final boolean itemApplicable;
	public final boolean blockApplicable;
	public final boolean entityApplicable;
	public final String defaultSkill;
	
	public static final ReqType[] ITEM_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.itemApplicable).toArray(ReqType[]::new);
	public static final ReqType[] BLOCK_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.blockApplicable).toArray(ReqType[]::new);
	public static final ReqType[] ENTITY_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.entityApplicable).toArray(ReqType[]::new);

	public static final Codec<ReqType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ReqType::values, ReqType::byName);
	private static final Map<String, ReqType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ReqType::getSerializedName, s -> {return s;}));
	public static ReqType byName(String name) {return BY_NAME.get(name);}
	@Override
	public String getSerializedName() {return this.name();}
	public static ReqType create(String name, boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String defaultSkill) {throw new IllegalStateException("Enum not extended");}
}
