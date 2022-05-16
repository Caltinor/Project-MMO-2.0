package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.autovalues.AutoValueConfig.AttributeKey;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoEntity {

	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation entityID) {
		//exit early if not an applicable type
		if (!type.entityApplicable)
			return new HashMap<>();
			
		EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(entityID);
		Entity e = entity.create(((PmmoSavedData)Core.get(LogicalSide.SERVER).getData()).getServer().overworld());
		LivingEntity livingEntity = e instanceof LivingEntity ? (LivingEntity)e : null;
		if (livingEntity == null) return new HashMap<>();
		Map<String, Integer> outMap = new HashMap<>();
		switch (type) {
		case KILL: case RIDE: case TAME: case BREED: case ENTITY_INTERACT:{
			outMap.putAll(getReqMap(livingEntity, type));
			break;
		}
		default: }
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation entityID) {
		//exit early if not an applicable type
		if (!type.entityApplicable)
			return new HashMap<>();
				
		EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(entityID);
		Entity e = entity.create(((PmmoSavedData)Core.get(LogicalSide.SERVER).getData()).getServer().overworld());
		LivingEntity livingEntity = e instanceof LivingEntity ? (LivingEntity)e : null;
		if (livingEntity == null) return new HashMap<>();
		Map<String, Long> outMap = new HashMap<>();
		switch (type) {
		case BREED: 		
		case FROM_MOBS:		
		case FROM_PLAYERS:		
		case FROM_ANIMALS: 		
		case MELEE_TO_MOBS: 		
		case MELEE_TO_PLAYERS:		
		case MELEE_TO_ANIMALS: 		
		case RANGED_TO_MOBS:		
		case RANGED_TO_PLAYERS:		
		case RANGED_TO_ANIMALS: 	
		case DEATH:		
		case ENTITY:		
		case RIDING: 	
		case SHIELD_BLOCK:		
		case TAMING: {
			outMap.putAll(getXpMap(livingEntity, type));
			break;
		}
		default: }
		return outMap;	
	}
	
	//========================GETTER METHODS==============================
	private static Map<String, Integer> getReqMap(LivingEntity entity, ReqType type) {
		Map<String, Integer> outMap = new HashMap<>();
		float healthScale = getMaxHealth(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.HEALTH, 0d).floatValue();
		float speedScale = getSpeed(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.SPEED, 0d).floatValue();
		double damageScale = getDamage(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.DMG, 0d).floatValue();
		double scale = healthScale + speedScale + damageScale;
		
		AutoValueConfig.getEntityReq(type).forEach((skill, level) -> {
			outMap.put(skill, Double.valueOf((double)level * scale).intValue());
		});
		return outMap;
	}
	
	private static Map<String, Long> getXpMap(LivingEntity entity, EventType type) {
		Map<String, Long> outMap = new HashMap<>();
		float healthScale = getMaxHealth(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.HEALTH, 0d).floatValue();
		float speedScale = getSpeed(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.SPEED, 0d).floatValue();
		double damageScale = getDamage(entity) * AutoValueConfig.ENTITY_ATTRIBUTES.get().getOrDefault(AttributeKey.DMG, 0d).floatValue();
		double scale = healthScale + speedScale + damageScale;
		
		AutoValueConfig.getEntityXpAward(type).forEach((skill, value) -> {
			outMap.put(skill, Double.valueOf(value * scale).longValue());
		});
		return outMap;
	}
	
	//========================UTILITY METHODS=============================
	private static float getMaxHealth(LivingEntity entity) {
		AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
		return (float) (entity.getMaxHealth() + attribute.getValue());
	}
	private static float getSpeed(LivingEntity entity) {
		AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		return (float) (entity.getSpeed() + attribute.getValue());
	}
	private static double getDamage(LivingEntity entity) {
		AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
		return attribute != null ? attribute.getValue() : 0d;
	}
}
