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
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EasyItemConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public EasyItemConfigProvider(PackOutput gen) {
        super(gen, "easy", "pmmo/items", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).getFoodProperties(null) != null)
                .map(ItemStack::new).forEach(item -> {
            FoodProperties props = item.getFoodProperties(null);
            long xp = (props.nutrition() * 10L) +
                    (long)(props.saturation() * 100f) +
                    ((long)props.effects().size() * 50L);
            this.get(item.getItem()).addXpValues(EventType.CONSUME, Map.of("endurance", xp));
            this.get(item.getItem()).addXpValues(EventType.CRAFT, Map.of("cooking", xp));
            this.get(item.getItem()).addXpValues(EventType.SMELT, Map.of("cooking", xp));
        });
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).isDamageableItem())
                .map(ItemStack::new).forEach(item -> {
            long xp = (item.getMaxDamage() / 4) * 100L;
            this.get(item.getItem()).addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", xp));
        });
    }

    private ObjectData.Builder get(Item item) {
        return data.computeIfAbsent(RegistryUtil.getId(item), i -> ObjectData.build());}

    @Override
    public String getName() {return "Project MMO Easy Item Generator";}
}
