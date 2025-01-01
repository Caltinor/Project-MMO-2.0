package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class DefaultDimConfigProvider extends PmmoDataProvider<LocationData> {
    Map<ResourceLocation, LocationData.Builder> data = new HashMap<>();
    public DefaultDimConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/dimensions", LocationData.CODEC.codec());
    }

    @Override
    protected void start() {
        get(Level.OVERWORLD).addVeinBlacklist(RegistryUtil.getId(Blocks.BEDROCK))
                .addBonus(ModifierDataType.DIMENSION, Map.of(
                        "mining", 1.25,
                        "excavation", 1.25,
                        "woodcutting", 1.25,
                        "building", 1.25,
                        "farming", 1.25
                ));

        var endModifiers = new MobModifier[]{
                modifiers(Attributes.MOVEMENT_SPEED, 0.00025),
                modifiers(Attributes.MAX_HEALTH, 10.0),
                modifiers(Attributes.ATTACK_DAMAGE, 0.00025)};
        get(Level.END).addVeinBlacklist(RegistryUtil.getId(Blocks.BEDROCK))
                .addBonus(ModifierDataType.DIMENSION, Map.of(
                        "flying", 1.5,
                        "combat", 1.2,
                        "archery", 1.5,
                        "endurance", 1.2
                ))
                .addGlobalModifier(endModifiers);

        var netherModifiers = new MobModifier[]{
                modifiers(Attributes.MAX_HEALTH, 7.0),
                modifiers(Attributes.ATTACK_DAMAGE, 0.05)};
        get(Level.NETHER).addVeinBlacklist(RegistryUtil.getId(Blocks.BEDROCK))
                .addBonus(ModifierDataType.DIMENSION, Map.of(
                        "flying", 2.5,
                        "combat", 1.25,
                        "archery", 1.25,
                        "endurance", 1.25,
                        "farming", 0.75
                ))
                .addReq(Map.of("combat", 30L))
                .addGlobalModifier(netherModifiers);

        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private LocationData.Builder get(ResourceKey<?> key) {
        return data.computeIfAbsent(key.location(), i -> LocationData.build());
    }

    private MobModifier modifiers(Holder<Attribute> attribute, double value) {
        return new MobModifier(RegistryUtil.getId(attribute), value, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public String getName() {return "Project MMO Default Dimension Generator";}
}
