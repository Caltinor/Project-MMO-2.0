package harmonised.pmmo.util;

import java.util.UUID;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class Reference {
	public static final String MOD_ID = "pmmo";
	
	public static final String API_MAP_SERIALIZER_KEY = "key";
	public static final String API_MAP_SERIALIZER_VALUE = "value";
	
	public static final UUID CREATIVE_REACH_ATTRIBUTE = UUID.fromString("c97b8776-05c8-4dbe-835c-10211ad4aba6");
	
	public static final TagKey<EntityType<?>> MOB_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("pmmo:mobs"));
	public static final TagKey<EntityType<?>> ANIMAL_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("pmmo:animals"));
}
