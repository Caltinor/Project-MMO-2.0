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
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoEntity {
	public static final EventType[] EVENTTYPES = {EventType.BREED, EventType.DEATH, EventType.ENTITY, EventType.RIDING,
			EventType.SHIELD_BLOCK,	EventType.TAMING};

	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation entityID) {
		return new HashMap<>();
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation entityID) {
		//exit early if not an applicable type
		if (!type.entityApplicable)
			return new HashMap<>();
				
		Map<String, Long> outMap = new HashMap<>();
		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityID);
		switch (type) {
		case RIDING: {
			if (entityType.is(Reference.RIDEABLE_TAG)) {
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
			if (entityType.is(Reference.BREEDABLE_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		case TAMING: {
			if (entityType.is(Reference.TAMABLE_TAG)) {
				outMap.putAll(getXpMap(entityID, type));
			}
			break;
		}
		default: }
		return outMap;	
	}
	
	//========================GETTER METHODS==============================
	private static Map<String, Long> getXpMap(ResourceLocation entityID, EventType type) {
		EntityType<? extends LivingEntity> entity = (EntityType<? extends LivingEntity>) ForgeRegistries.ENTITY_TYPES.getValue(entityID);
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
	private static double getAttribute(EntityType<? extends LivingEntity> entity, Attribute attribute) {
		if (!DefaultAttributes.hasSupplier(entity)) return 0d;
		AttributeSupplier attSup = DefaultAttributes.getSupplier(entity);
		if (attSup == null) return 0d;
		return attSup.hasAttribute(attribute) ? attSup.getBaseValue(attribute) : 0d;
	}
}
