package harmonised.pmmo.features.autovalues;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.features.autovalues.AutoValueConfig.AttributeKey;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.HashMap;
import java.util.Map;

public class AutoEntity {
	public static final EventType[] EVENTTYPES = {EventType.BREED, EventType.DEATH, EventType.ENTITY, EventType.RIDING,
			EventType.SHIELD_BLOCK,	EventType.TAMING};

	public static Map<String, Long> processReqs(ReqType type, Identifier entityID) {
		return new HashMap<>();
	}
	
	public static Map<String, Long> processXpGains(EventType type, Identifier entityID) {
		//exit early if not an applicable type
		if (!type.entityApplicable || !Config.autovalue().xpEnabled(type))
			return new HashMap<>();
				
		Map<String, Long> outMap = new HashMap<>();
		EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityID);
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
	private static Map<String, Long> getXpMap(Identifier entityID, EventType type) {
		EntityType<? extends LivingEntity> entity = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(entityID);
		Map<String, Long> outMap = new HashMap<>();
		double healthScale = getAttribute(entity, Attributes.MAX_HEALTH) * Config.autovalue().tweaks().entityTweaks().getOrDefault(AttributeKey.HEALTH.key, 0d);
		double speedScale = getAttribute(entity, Attributes.MOVEMENT_SPEED) * Config.autovalue().tweaks().entityTweaks().getOrDefault(AttributeKey.SPEED.key, 0d);
		double damageScale = getAttribute(entity, Attributes.ATTACK_DAMAGE) * Config.autovalue().tweaks().entityTweaks().getOrDefault(AttributeKey.DMG.key, 0d);
		double scale = healthScale + speedScale + damageScale;

		Config.autovalue().xpAwards().entity(type).forEach((skill, value) -> {
			outMap.put(skill, Double.valueOf(value * scale).longValue());
		});
		return outMap;
	}
	
	//========================UTILITY METHODS=============================
	@SuppressWarnings("unchecked")
	private static double getAttribute(EntityType<? extends LivingEntity> entity, Holder<Attribute> attribute) {
		if (!DefaultAttributes.hasSupplier(entity)) return 0d;
		AttributeSupplier attSup = DefaultAttributes.getSupplier(entity);
		if (attSup == null) return 0d;
		return attSup.hasAttribute(attribute) ? attSup.getBaseValue(attribute) : 0d;
	}
}
