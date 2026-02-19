package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEntityConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<Identifier, ObjectData.Builder> data = new HashMap<>();
    public DefaultEntityConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/entities", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        List.of(EntityType.OAK_BOAT,
                EntityType.SPRUCE_BOAT,
                EntityType.BIRCH_BOAT,
                EntityType.JUNGLE_BOAT,
                EntityType.ACACIA_BOAT,
                EntityType.CHERRY_BOAT,
                EntityType.DARK_OAK_BOAT,
                EntityType.PALE_OAK_BOAT,
                EntityType.MANGROVE_BOAT,
                EntityType.BAMBOO_RAFT)
            .forEach(boat -> get(boat).addXpValues(EventType.RIDING, Map.of("sailing", 20L)));
        List.of(EntityType.OAK_CHEST_BOAT,
                EntityType.SPRUCE_CHEST_BOAT,
                EntityType.BIRCH_CHEST_BOAT,
                EntityType.JUNGLE_CHEST_BOAT,
                EntityType.ACACIA_CHEST_BOAT,
                EntityType.CHERRY_CHEST_BOAT,
                EntityType.DARK_OAK_CHEST_BOAT,
                EntityType.PALE_OAK_CHEST_BOAT,
                EntityType.MANGROVE_CHEST_BOAT,
                EntityType.BAMBOO_CHEST_RAFT)
            .forEach(boat -> get(boat).addXpValues(EventType.RIDING, Map.of("sailing", 20L)));

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
