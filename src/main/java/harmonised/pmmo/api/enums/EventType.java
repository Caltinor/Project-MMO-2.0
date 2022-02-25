package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum EventType implements StringRepresentable, IExtensibleEnum{
	ANVIL_REPAIR(true, false, false, "smithing"),  			//
	BLOCK_BREAK(false, true, false, "mining"),				//IMPLEMENTED
		BREAK_SPEED(false, true, false, "mining"),			//IMPLEMENTED
	BLOCK_PLACE(true, true, false, "building"),				//IMPLEMENTED
	BREATH_CHANGE(false, false, true, "swimming"),			//
	BREED(false, false, true, "taming"),					//
	BREW(true, false, false, "alchemy"),					//
	COOK(true, false, false, "cooking"),					//
	CRAFT(true, false, false, "crafting"),					//IMPLEMENTED
	RECEIVE_DAMAGE(false, false, true, "endurance"),		//IMPLEMENTED
		FROM_MOBS(false, false, true, "endurance"),			//IMPLEMENTED
		FROM_PLAYERS(false, false, true, "endurance"),		//IMPLEMENTED
		FROM_ANIMALS(false, false, true, "endurance"),		//IMPLEMENTED
		FROM_PROJECTILES(false, false, true, "endurance"),	//IMPLEMENTED
		FROM_MAGIC(false, false, true, "endurance"),		//IMPLEMENTED
		FROM_ENVIRONMENT(false, false, true, "endurance"),	//IMPLEMENTED
		FROM_IMPACT(false, false, true, "endurance"),		//IMPLEMENTED
	DEAL_MELEE_DAMAGE(false, false, true, "combat"),		//IMPLEMENTED
		MELEE_TO_MOBS(false, false, true, "combat"),		//IMPLEMENTED
		MELEE_TO_PLAYERS(false, false, true, "combat"),		//IMPLEMENTED
		MELEE_TO_ANIMALS(false, false, true, "combat"),		//IMPLEMENTED
	DEAL_RANGED_DAMAGE(false, false, true, "archery"),		//IMPLEMENTED
		RANGED_TO_MOBS(false, false, true, "archery"),		//IMPLEMENTED
		RANGED_TO_PLAYERS(false, false, true, "archery"),	//IMPLEMENTED
		RANGED_TO_ANIMALS(false, false, true, "archery"),	//IMPLEMENTED
	DEATH(false, false, true, "endurance"),					//IMPLEMENTED
	ENCHANT(true, false, false, "magic"),					//
	FISH(true, false, false, "fishing"),					//
	SMELT(true, false, false, "smithing"),					//
	GROW(false, true, false, "farming"),					//
	HEALTH_CHANGE(false, false, false, ""),					//
	JUMP(false, false, true, "agility"),					//IMPLEMENTED
		SPRINT_JUMP(false, false, true, "agility"),			//IMPLEMENTED
		CROUCH_JUMP(false, false, true, "agility"),			//IMPLEMENTED
	WORLD_CONNECT(false, false, false, ""),					//IMPLEMENTED
	WORLD_DISCONNECT(false, false, false, ""),				//
	INTERACT(false, false, true, "dexterity"),				//
		LEFT_CLICK_BLOCK(false, true, false, "dexterity"),	//
		LEFT_CLICK_ITEM(true, false, false, "dexterity"),	//
		RIGHT_CLICK_BLOCK(false, true, false, "dexterity"),	//
		RIGHT_CLICK_ITEM(true, false, false, "dexterity"),	//
		ENTITY(false, false, true, "charisma"),				//IMPLEMENTED
	RESPAWN(false, false, false, ""),						//
	RIDING(false, false, true, "taming"),					//
	SKILL_UP(false, false, false, ""),						//IMPLEMENTED
	SLEEP(false, false, true, "endurance"),					//
	SPRINTING(false, false, true, "agility"),				//
	SUBMERGED(false, false, true, "swimming"),				//
		SWIMMING(false, false, true, "swimming"),			//
		DIVING(false, false, true, "swimming"),				//
		SURFACING(false, false, true, "swimming"),			//
		SWIM_SPRINTING(false, false, true, "swimming"),		//
	TAMING(false, false, true, "taming"),					//
	USE_ITEM(true, false, false, "dexterity"),				//
	VEIN_MINE(false, false, false, ""),						//
	DISABLE_PERK(false, false, false, "");					//IMPLEMENTED
	
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
	
	
	public static final Codec<EventType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(EventType::values, EventType::byName);
	private static final Map<String, EventType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EventType::getSerializedName, s -> {return s;}));
	public static EventType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static EventType create(String name, boolean itemApplicable, boolean blockApplicable, boolean entityApplicable, String autoValueSkillDefault) {throw new IllegalStateException("Enum not extended");}
}
