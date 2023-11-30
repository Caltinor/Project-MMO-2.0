package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum EventType implements StringRepresentable, IExtensibleEnum, GuiEnumGroup {
	ANVIL_REPAIR(true, false, false, "smithing"),  			
	BLOCK_BREAK(false, true, false, "mining"),				
		BREAK_SPEED(false, true, false, "mining"),			
	BLOCK_PLACE(true, true, false, "building"),				
	BREATH_CHANGE(false, false, true, "swimming"),
	BREED(false, false, true, "taming"),					
	BREW(true, false, false, "alchemy"),
	CONSUME(true, false, false, "cooking"),					
	CRAFT(true, false, false, "crafting"),
	CROUCH(false, false, true, "endurance"),
	RECEIVE_DAMAGE(false, false, true, "endurance"),		
	DEAL_DAMAGE(true, false, true, "combat"),
	DEATH(false, false, true, "endurance"),					
	ENCHANT(true, false, false, "magic"),
	EFFECT(false, false, true, "magic"),
	FISH(true, false, false, "fishing"),					
	SMELT(true, false, false, "smithing"),
	GROW(false, true, false, "farming"),
	GIVEN_AS_TRADE(true, false, false, "charisma"),
	/**Use either of the INCREASE/DECREASE variants for future implementations.*/
	@Deprecated(forRemoval = true, since = "1.20")
	HEALTH_CHANGE(false, false, false, ""),
		HEALTH_INCREASE(false, false, false, ""),
		HEALTH_DECREASE(false, false, false, ""),
	JUMP(false, false, true, "agility"),					
		SPRINT_JUMP(false, false, true, "agility"),			
		CROUCH_JUMP(false, false, true, "agility"),
	HIT_BLOCK(false, true, false, "dexterity"),				
	ACTIVATE_BLOCK(false, true, false, "dexterity"),		
	ACTIVATE_ITEM(true, false, false, "dexterity"),			
	ENTITY(false, false, true, "charisma"),
	RECEIVED_AS_TRADE(true, false, false, "charisma"),
	RIDING(false, false, true, "taming"),						
	SHIELD_BLOCK(false, false, true, "combat"),				
	SKILL_UP(false, false, false, ""),								
	SPRINTING(false, false, true, "agility"),					
	SUBMERGED(false, false, true, "swimming"),					
		SWIMMING(false, false, true, "swimming"),				
		DIVING(false, false, true, "swimming"),					
		SURFACING(false, false, true, "swimming"),				
		SWIM_SPRINTING(false, false, true, "swimming"),			
	TAMING(false, false, true, "taming");					
	
	public final boolean itemApplicable;
	public final boolean blockApplicable;
	public final boolean entityApplicable;
	public final String autoValueSkill;
	EventType(boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String autoValueSkillDefault) {
		this.itemApplicable = itemApplicable;
		this.blockApplicable = blockApplicable;
		this.entityApplicable = entityApplicable;
		this.autoValueSkill = autoValueSkillDefault;
	}
	
	public static final EventType[] ITEM_APPLICABLE_EVENTS = Arrays.asList(EventType.values()).stream().filter((type) -> type.itemApplicable).toArray(EventType[]::new);
	public static final EventType[] BLOCK_APPLICABLE_EVENTS = Arrays.asList(EventType.values()).stream().filter((type) -> type.blockApplicable).toArray(EventType[]::new);
	public static final EventType[] ENTITY_APPLICABLE_EVENTS = Arrays.asList(EventType.values()).stream().filter((type) -> type.entityApplicable).toArray(EventType[]::new);
	public static final EventType[] BLOCKITEM_APPLICABLE_EVENTS = Arrays.asList(EventType.values()).stream().filter((type) -> type.itemApplicable || type.blockApplicable).toArray(EventType[]::new);
	
	
	public static final Codec<EventType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(EventType::values, EventType::byName);
	private static final Map<String, EventType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EventType::getSerializedName, s -> {return s;}));
	public static EventType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static EventType create(String name, boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String autoValueSkillDefault) {throw new IllegalStateException("Enum not extended");}

	@Override
	public String getName() {return name();}
}
