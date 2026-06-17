package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

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
        List.of(EntityTypes.OAK_BOAT,
                EntityTypes.SPRUCE_BOAT,
                EntityTypes.BIRCH_BOAT,
                EntityTypes.JUNGLE_BOAT,
                EntityTypes.ACACIA_BOAT,
                EntityTypes.CHERRY_BOAT,
                EntityTypes.DARK_OAK_BOAT,
                EntityTypes.PALE_OAK_BOAT,
                EntityTypes.MANGROVE_BOAT,
                EntityTypes.BAMBOO_RAFT)
            .forEach(boat -> get(boat).addXpValues(EventType.RIDING, Map.of("sailing", 20L)));
        List.of(EntityTypes.OAK_CHEST_BOAT,
                EntityTypes.SPRUCE_CHEST_BOAT,
                EntityTypes.BIRCH_CHEST_BOAT,
                EntityTypes.JUNGLE_CHEST_BOAT,
                EntityTypes.ACACIA_CHEST_BOAT,
                EntityTypes.CHERRY_CHEST_BOAT,
                EntityTypes.DARK_OAK_CHEST_BOAT,
                EntityTypes.PALE_OAK_CHEST_BOAT,
                EntityTypes.MANGROVE_CHEST_BOAT,
                EntityTypes.BAMBOO_CHEST_RAFT)
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
