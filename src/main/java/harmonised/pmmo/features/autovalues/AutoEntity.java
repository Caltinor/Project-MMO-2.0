package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.features.autovalues.AutoValueConfig.AttributeKey;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoEntity {
	//public static final ReqType[] REQTYPES = {ReqType.RIDE, ReqType.TAME, ReqType.BREED, ReqType.ENTITY_INTERACT};
	public static final EventType[] EVENTTYPES = {EventType.BREED, EventType.FROM_MOBS, EventType.FROM_PLAYERS,	EventType.FROM_ANIMALS, 		
			EventType.MELEE_TO_MOBS, EventType.MELEE_TO_PLAYERS, EventType.MELEE_TO_ANIMALS, EventType.RANGED_TO_MOBS,
			EventType.RANGED_TO_PLAYERS, EventType.RANGED_TO_ANIMALS, EventType.DEATH, EventType.ENTITY, EventType.RIDING, 	
			EventType.SHIELD_BLOCK,	EventType.TAMING};

	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation entityID) {
		//exit early if not an applicable type
		//if (!type.entityApplicable)
			return new HashMap<>();
			
		/*Map<String, Integer> outMap = new HashMap<>();
		switch (type) {
		case RIDE: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.RIDEABLE_TAG)) {
				outMap.putAll(getReqMap(entityID, type));
			}
			break;
		}
		case TAME: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.TAMABLE_TAG)) {
				outMap.putAll(getReqMap(entityID, type));
			}
			break;
		}	
		case BREED: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.TAMABLE_TAG)) {
				outMap.putAll(getReqMap(entityID, type));
			}
			break;
		}	
		case ENTITY_INTERACT:{
			outMap.putAll(getReqMap(entityID, type));
			break;
		}
		default: }
		return outMap;*/
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation entityID) {
		//exit early if not an applicable type
		if (!type.entityApplicable)
			return new HashMap<>();
				
		Map<String, Long> outMap = new HashMap<>();
		switch (type) {		 				
		case FROM_PLAYERS:					 		
		case MELEE_TO_PLAYERS:						
		case RANGED_TO_PLAYERS:	{
			if (entityID.equals(new ResourceLocation("minecraft:player"))) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case RIDING: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.RIDEABLE_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case DEATH:		
		case ENTITY:			
		case SHIELD_BLOCK:		
		{
			outMap.putAll(getXpMap(entityID, type));
			break;
		}
		case BREED: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.BREEDABLE_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case TAMING: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.TAMABLE_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case FROM_ANIMALS: 
		case MELEE_TO_ANIMALS: 
		case RANGED_TO_ANIMALS: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.ANIMAL_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case FROM_MOBS: 
		case MELEE_TO_MOBS:
		case RANGED_TO_MOBS: {
			if (ForgeRegistries.ENTITIES.getValue(entityID).is(Reference.MOB_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		default: }
		return outMap;	
	}
	
	//========================GETTER METHODS==============================
	/*private static Map<String, Integer> getReqMap(ResourceLocation entity, ReqType type) {
		Map<String, Integer> outMap = new HashMap<>();
		double healthScale = 
				MsLoggy.DEBUG.logAndReturn(getAttribute(entity, Attributes.MAX_HEALTH), LOG_CODE.AUTO_VALUES, "Health Attribute: {}") * 
				MsLoggy.DEBUG.logAndReturn(AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.HEALTH.key, 0d), LOG_CODE.AUTO_VALUES, "Health Config Value: {}");
		double speedScale = 
				MsLoggy.DEBUG.logAndReturn(getAttribute(entity, Attributes.MOVEMENT_SPEED), LOG_CODE.AUTO_VALUES, "Speed Attribute: {}") * 
				MsLoggy.DEBUG.logAndReturn(AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.SPEED.key, 0d), LOG_CODE.AUTO_VALUES, "Speed Config Value: {}");
		double damageScale = 
				MsLoggy.DEBUG.logAndReturn(getAttribute(entity, Attributes.ATTACK_DAMAGE), LOG_CODE.AUTO_VALUES, "Damage Attribute: {}") * 
				MsLoggy.DEBUG.logAndReturn(AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.DMG.key, 0d), LOG_CODE.AUTO_VALUES, "Damage Config Value: {}");
		double scale = healthScale + speedScale + damageScale;
		
		AutoValueConfig.getEntityReq(type).forEach((skill, level) -> {
			outMap.put(skill, Double.valueOf((double)level * scale).intValue());
		});
		return outMap;
	}*/
	
	private static Map<String, Long> getXpMap(ResourceLocation entity, EventType type) {
		Map<String, Long> outMap = new HashMap<>();
		double healthScale = getAttribute(entity, Attributes.MAX_HEALTH) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.HEALTH.key, 0d);
		double speedScale = getAttribute(entity, Attributes.MOVEMENT_SPEED) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.SPEED.key, 0d);
		double damageScale = getAttribute(entity, Attributes.ATTACK_DAMAGE) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.DMG.key, 0d);
		double scale = healthScale + speedScale + damageScale;
		
		AutoValueConfig.getEntityXpAward(type).forEach((skill, value) -> {
			outMap.put(skill, Double.valueOf(value * scale).longValue());
		});
		return outMap;
	}
	
	//========================UTILITY METHODS=============================
	@SuppressWarnings("unchecked")
	private static double getAttribute(ResourceLocation entityID, Attribute attribute) {
		EntityType<? extends LivingEntity> entity = (EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(entityID);
		if (!DefaultAttributes.hasSupplier(entity)) return 0d;
		AttributeSupplier attSup = DefaultAttributes.getSupplier(entity);
		if (attSup == null) return 0d;
		return attSup.hasAttribute(attribute) ? attSup.getBaseValue(attribute) : 0d;
	}
}
