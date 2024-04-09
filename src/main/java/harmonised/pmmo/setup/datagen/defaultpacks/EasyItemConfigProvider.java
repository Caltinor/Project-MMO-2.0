package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class EasyItemConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public EasyItemConfigProvider(PackOutput gen) {
        super(gen, "easy", "pmmo/items", ObjectData.CODEC);
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        BuiltInRegistries.ITEM.stream().filter(Item::isEdible).forEach(item -> {
            FoodProperties props = item.getFoodProperties();
            long xp = (props.getNutrition() * 10L) +
                    (long)(props.getSaturationModifier() * 100f) +
                    ((long)props.getEffects().size() * 50L);
            this.get(item).addXpValues(EventType.CONSUME, Map.of("endurance", xp));
        });
        BuiltInRegistries.ITEM.stream().filter(Item::canBeDepleted).forEach(item -> {
            long xp = (item.getMaxDamage() / 4) * 100L;
            this.get(item).addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", xp));
        });
    }

    private ObjectData.Builder get(Item item) {
        return data.computeIfAbsent(RegistryUtil.getId(item), i -> ObjectData.build());}

    @Override
    public String getName() {return "Project MMO Easy Item Generator";}
}
