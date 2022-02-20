package harmonised.pmmo.api.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum EventType implements StringRepresentable, IExtensibleEnum{
	ANVIL_REPAIR,  					//
	BLOCK_BREAK,					//IMPLEMENTED
		BREAK_SPEED,				//IMPLEMENTED
	BLOCK_PLACE,					//IMPLEMENTED
	BREATH_CHANGE,					//
	BREED,							//
	BREW,							//
	COOK,							//
	CRAFT,							//IMPLEMENTED
	RECEIVE_DAMAGE,					//IMPLEMENTED
		FROM_MOBS,					//IMPLEMENTED
		FROM_PLAYERS,				//IMPLEMENTED
		FROM_ANIMALS,				//IMPLEMENTED
		FROM_PROJECTILES,			//IMPLEMENTED
		FROM_MAGIC,					//IMPLEMENTED
		FROM_ENVIRONMENT,			//IMPLEMENTED
		FROM_IMPACT,				//IMPLEMENTED
	DEAL_MELEE_DAMAGE,				//IMPLEMENTED
		MELEE_TO_MOBS,				//IMPLEMENTED
		MELEE_TO_PLAYERS,			//IMPLEMENTED
		MELEE_TO_ANIMALS,			//IMPLEMENTED
	DEAL_RANGED_DAMAGE,				//IMPLEMENTED
		RANGED_TO_MOBS,				//IMPLEMENTED
		RANGED_TO_PLAYERS,			//IMPLEMENTED
		RANGED_TO_ANIMALS,			//IMPLEMENTED
	DEATH,							//
		KILLED_BY_MOBS,				//
		KILLED_BY_PLAYERS,			//
		KILLED_BY_ANIMALS,			//
		KILLED_BY_PROJECTILES,		//
		KILLED_BY_ENVIRONMENT,		//
	ENCHANT,						//
	FISH,							//
	SMELT,							//
	GROW,							//
	HEALTH_CHANGE,					//
	JUMP,							//IMPLEMENTED
		SPRINT_JUMP,				//
		CROUCH_JUMP,				//
	WORLD_CONNECT,					//IMPLEMENTED
	WORLD_DISCONNECT,				//
	INTERACT,						//
		LEFT_CLICK_BLOCK,			//
		LEFT_CLICK_ITEM,			//
		LEFT_CLICK,					//
		RIGHT_CLICK_BLOCK,			//
		RIGHT_CLICK_ITEM,			//
		RIGHT_CLICK,				//
		ENTITY,						//IMPLEMENTED
	RESPAWN,						//
	SKILL_UP,						//
	SLEEP,							//
	SPRINTING,						//
	SUBMERGED,						//
		SWIMMING,					//
		DIVING,						//
		SURFACING,					//
		SWIM_SPRINTING,				//
	TAMING,							//
	USE_ITEM,						//
	VEIN_MINE,						//
	DISABLE_PERK;					//IMPLEMENTED
	
	public static final Codec<EventType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(EventType::values, EventType::byName);
	private static final Map<String, EventType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EventType::getSerializedName, s -> {return s;}));
	public static EventType byName(String name) {return BY_NAME.get(name);} 
	
	@Override
	public String getSerializedName() {return this.name();}
	
	public static EventType create(String name) {throw new IllegalStateException("Enum not extended");}

}
