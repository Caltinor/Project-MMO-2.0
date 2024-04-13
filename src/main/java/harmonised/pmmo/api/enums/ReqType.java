package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.IExtensibleEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ReqType implements StringRepresentable, IExtensibleEnum, GuiEnumGroup {
    WEAR(true, false, false, "endurance", LangProvider.REQ_WEAR),				//PLAYER TICK
    USE_ENCHANTMENT(true, false, false, "magic", LangProvider.REQ_ENCHANT),		//PLAYER TICK
    TOOL(true, false, false, "mining", LangProvider.REQ_TOOL),					//IMPLEMENTED IN ACTIONS
    WEAPON(true, false, false, "combat", LangProvider.REQ_WEAPON),				//IMPLEMENTED IN ACTIONS
    USE(true, false, false, "crafting", LangProvider.REQ_USE),					//IMPLEMENTED IN ACTIONS
    PLACE(true, true, false, "building", LangProvider.REQ_PLACE),				//IMPLEMENTED IN ACTIONS
    BREAK(true, true, false, "mining", LangProvider.REQ_BREAK),					//IMPLEMENTED IN ACTIONS
    KILL(false, false, true, "combat", LangProvider.ENUM_KILL),					//IMPLEMENTED IN ACTIONS
    TRAVEL(false, false, false,"agility", LangProvider.ENUM_TRAVEL), 				//TRAVEL EVENT
    RIDE(false, false, true, "farming", LangProvider.ENUM_RIDE),				//IMPLEMENTED IN EVENT
    TAME(false, false, true, "taming", LangProvider.ENUM_TAME),					//IMPLEMENTED IN ACTIONS
    BREED(false, false, true, "farming", LangProvider.ENUM_BREED),				//IMPLEMENTED IN ACTIONS
    INTERACT(true, true, false, "engineering", LangProvider.REQ_INTERACT),			//IMPLEMENTED IN ACTOINS
    ENTITY_INTERACT(false, false, true, "farming", LangProvider.ENUM_ENTITY_INTERACT);		//IMPLEMENTED IN ACTIONS
	
	
	ReqType(boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String defaultSkill, LangProvider.Translation tooltipTranslation) {
		this.itemApplicable = itemApplicable;
		this.blockApplicable = blockApplicable;
		this.entityApplicable = entityApplicable;
		this.defaultSkill = defaultSkill;
		this.tooltipTranslation = tooltipTranslation;
	}
	
	public final boolean itemApplicable;
	public final boolean blockApplicable;
	public final boolean entityApplicable;
	public final String defaultSkill;
	public final LangProvider.Translation tooltipTranslation;
	
	public static final ReqType[] ITEM_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.itemApplicable).toArray(ReqType[]::new);
	public static final ReqType[] BLOCK_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.blockApplicable).toArray(ReqType[]::new);
	public static final ReqType[] ENTITY_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.entityApplicable).toArray(ReqType[]::new);
	public static final ReqType[] BLOCKITEM_APPLICABLE_EVENTS = Arrays.asList(ReqType.values()).stream().filter((type) -> type.itemApplicable || type.blockApplicable).toArray(ReqType[]::new);

	public static final Codec<ReqType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ReqType::values, ReqType::byName);
	private static final Map<String, ReqType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ReqType::getSerializedName, s -> {return s;}));
	public static ReqType byName(String name) {return BY_NAME.get(name);}
	@Override
	public String getSerializedName() {return this.name();}
	public static ReqType create(String name, boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String defaultSkill, LangProvider.Translation translation) {throw new IllegalStateException("Enum not extended");}

	@Override
	public String getName() {return name();}
}
