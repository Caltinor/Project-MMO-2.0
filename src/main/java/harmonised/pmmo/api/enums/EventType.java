package harmonised.pmmo.api.enums;

import com.mojang.serialization.Codec;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum EventType implements StringRepresentable, GuiEnumGroup {
	ANVIL_REPAIR(true, false, false, "smithing", LangProvider.XP_VALUE_ANVIL),
	BLOCK_BREAK(false, true, false, "mining", LangProvider.XP_VALUE_BREAK),
		BREAK_SPEED(false, true, false, "mining", LangProvider.ENUM_BREAK_SPEED),
	BLOCK_PLACE(true, true, false, "building", LangProvider.XP_VALUE_PLACE),
	BREATH_CHANGE(false, false, true, "swimming", LangProvider.ENUM_BREATH_CHANGE),
	BREED(false, false, true, "taming", LangProvider.ENUM_BREED),
	BREW(true, false, false, "alchemy", LangProvider.XP_VALUE_BREW),
	CONSUME(true, false, false, "cooking", LangProvider.XP_VALUE_CONSUME),
	CRAFT(true, false, false, "crafting", LangProvider.XP_VALUE_CRAFT),
	CROUCH(false, false, true, "endurance", LangProvider.ENUM_CROUCH),
	TOOL_BREAKING(true, false, false, "smithing", LangProvider.ENUM_TOOL_BREAKING),
	RECEIVE_DAMAGE(false, false, true, "endurance", LangProvider.ENUM_RECEIVE_DAMAGE),
	DEAL_DAMAGE(true, false, true, "combat", LangProvider.XP_VALUE_DEAL_DAMAGE),
	MITIGATE_DAMAGE(false, false, true, "endurance", LangProvider.XP_VALUE_MITIGATE_DAMAGE),
	DEATH(false, false, true, "endurance", LangProvider.ENUM_DEATH),
	ENCHANT(true, false, false, "magic", LangProvider.XP_VALUE_ENCHANT),
	EFFECT(false, false, true, "magic", LangProvider.ENUM_EFFECT),
	FISH(true, false, false, "fishing", LangProvider.XP_VALUE_FISH),
	SMELT(true, false, false, "smithing", LangProvider.XP_VALUE_SMELT),
	SMELTED(true, false, false, "smithing", LangProvider.XP_VALUE_SMELTED),
	GROW(false, true, false, "farming", LangProvider.XP_VALUE_GROW),
	GIVEN_AS_TRADE(true, false, false, "charisma", LangProvider.XP_VALUE_TRADE_GIVE),
	HEALTH_INCREASE(false, false, false, "", LangProvider.ENUM_HEALTH_CHANGE),
	HEALTH_DECREASE(false, false, false, "", LangProvider.ENUM_HEALTH_CHANGE),
	JUMP(false, false, true, "agility", LangProvider.ENUM_JUMP),
		SPRINT_JUMP(false, false, true, "agility", LangProvider.ENUM_SPRINT_JUMP),
		CROUCH_JUMP(false, false, true, "agility", LangProvider.ENUM_CROUCH_JUMP),
	HIT_BLOCK(false, true, false, "dexterity", LangProvider.XP_VALUE_HIT_BLOCK),
	ACTIVATE_BLOCK(false, true, false, "dexterity", LangProvider.XP_VALUE_ACTIVATE_BLOCK),
	ACTIVATE_ITEM(true, false, false, "dexterity", LangProvider.XP_VALUE_USE),
	ENTITY(false, false, true, "charisma", LangProvider.ENUM_ENTITY),
	RECEIVED_AS_TRADE(true, false, false, "charisma", LangProvider.XP_VALUE_TRADE_GET),
	RIDING(false, false, true, "taming", LangProvider.ENUM_RIDE),
	SHIELD_BLOCK(false, false, true, "combat", LangProvider.ENUM_SHIELD_BLOCK),
	SKILL_UP(false, false, false, "", LangProvider.ENUM_SKILL_UP),
	SKILL_DOWN(false, false, false, "", LangProvider.ENUM_SKILL_DOWN),
	SPRINTING(false, false, true, "agility", LangProvider.ENUM_SPRINTING),
	SUBMERGED(false, false, true, "swimming", LangProvider.ENUM_SUBMERGED),
		SWIMMING(false, false, true, "swimming", LangProvider.ENUM_SWIMMING),
		DIVING(false, false, true, "swimming", LangProvider.ENUM_DIVING),
		SURFACING(false, false, true, "swimming", LangProvider.ENUM_SURFACING),
		SWIM_SPRINTING(false, false, true, "swimming", LangProvider.ENUM_SWIM_SPRINTING),
	TAMING(false, false, true, "taming", LangProvider.ENUM_TAMING);
	
	public final boolean itemApplicable;
	public final boolean blockApplicable;
	public final boolean entityApplicable;
	public final String autoValueSkill;
	public final LangProvider.Translation tooltipTranslation;
	EventType(boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String autoValueSkillDefault, LangProvider.Translation tooltipTranslation) {
		this.itemApplicable = itemApplicable;
		this.blockApplicable = blockApplicable;
		this.entityApplicable = entityApplicable;
		this.autoValueSkill = autoValueSkillDefault;
		this.tooltipTranslation = tooltipTranslation;
	}
	
	public static final EventType[] ITEM_APPLICABLE_EVENTS = Arrays.stream(EventType.values()).filter((type) -> type.itemApplicable).toArray(EventType[]::new);
	public static final EventType[] BLOCK_APPLICABLE_EVENTS = Arrays.stream(EventType.values()).filter((type) -> type.blockApplicable).toArray(EventType[]::new);
	public static final EventType[] ENTITY_APPLICABLE_EVENTS = Arrays.stream(EventType.values()).filter((type) -> type.entityApplicable).toArray(EventType[]::new);
	public static final EventType[] BLOCKITEM_APPLICABLE_EVENTS = Arrays.stream(EventType.values()).filter((type) -> type.itemApplicable || type.blockApplicable).toArray(EventType[]::new);
	public static final EventType[] DAMAGE_TYPES = Arrays.stream(EventType.values()).filter(type -> type == RECEIVE_DAMAGE || type == DEAL_DAMAGE || type == MITIGATE_DAMAGE).toArray(EventType[]::new);

	public static boolean is(EventType[] from, EventType type) {return Arrays.asList(from).contains(type);}
	
	public static final Codec<EventType> CODEC = StringRepresentable.fromEnum(EventType::values);
	private static final Map<String, EventType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EventType::getSerializedName, s -> {return s;}));
	public static EventType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}

	@Override
	public String getName() {return name();}
}
