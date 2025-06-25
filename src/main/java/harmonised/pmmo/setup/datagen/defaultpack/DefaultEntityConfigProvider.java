package harmonised.pmmo.setup.datagen.defaultpack;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class DefaultEntityConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public DefaultEntityConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/entities", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        get(EntityType.BOAT).addXpValues(EventType.RIDING, Map.of("sailing", 20L));
        get(EntityType.CHEST_BOAT).addXpValues(EventType.RIDING, Map.of("sailing", 20L));

        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private ObjectData.Builder get(EntityType<?> entity) {
        return data.computeIfAbsent(RegistryUtil.getId(entity), id -> ObjectData.build());
    }

    @Override
    public String getName() {
        return "Project MMO Default Entity Generator";
    }
}
