package harmonised.pmmo.setup.datagen.defaultpacks;

import com.mojang.serialization.Codec;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.biome.Biomes;

import java.util.HashMap;
import java.util.Map;

public class DefaultBiomeConfigProvider extends PmmoDataProvider<LocationData> {
    Map<ResourceLocation, LocationData.Builder> data = new HashMap<>();
    public DefaultBiomeConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/biomes", LocationData.CODEC.codec());
    }

    @Override
    protected void start() {
        get(Biomes.BAMBOO_JUNGLE)
                .addReq(Map.of("endurance", 20L))
                .addBonus(ModifierDataType.BIOME, Map.of(
                    "agility", 5.0,
                    "swimming", 5.0,
                    "fishing", 5.0,
                    "combat", 1.10,
                    "archery", 1.35,
                    "woodcutting", 0.75,
                    "farming", 0.75))
                .addNegativeEffect(MobEffects.SLOWNESS, 0)
                .addNegativeEffect(MobEffects.WEAKNESS, 0)
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, 0.005),
                        modifiers(Attributes.MAX_HEALTH, 5.0)
                );
        get(Biomes.BEACH)
                .addBonus(ModifierDataType.BIOME, Map.of("fishing", 1.05))
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, -0.0075),
                        modifiers(Attributes.MAX_HEALTH, -7.0),
                        modifiers(Attributes.ATTACK_DAMAGE, -0.0075)
                );
        get(Biomes.COLD_OCEAN)
                .addBonus(ModifierDataType.BIOME, Map.of(
                        "swimming", 1.5,
                        "fishing", 1.3
                ))
                .addNegativeEffect(MobEffects.SLOWNESS, 1)
                .addNegativeEffect(MobEffects.WEAKNESS, 0)
                .addReq(Map.of("swimming", 25L, "endurance", 15L));
        get(Biomes.DEEP_FROZEN_OCEAN)
                .addBonus(ModifierDataType.BIOME, Map.of(
                        "swimming", 2.0,
                        "fishing", 1.5
                ))
                .addNegativeEffect(MobEffects.SLOWNESS, 3)
                .addNegativeEffect(MobEffects.WEAKNESS, 2)
                .addReq(Map.of("swimming", 35L, "endurance", 25L));
        get(Biomes.DEEP_OCEAN)
                .addBonus(ModifierDataType.BIOME, Map.of(
                        "swimming", 1.5,
                        "fishing", 1.2
                ))
                .addNegativeEffect(MobEffects.SLOWNESS, 0)
                .addReq(Map.of("swimming", 15L));
        get(Biomes.DESERT)
                .addBonus(ModifierDataType.BIOME, Map.of("excavation", 1.1, "farming", 0.85))
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, -0.0075),
                        modifiers(Attributes.MAX_HEALTH, -7.0),
                        modifiers(Attributes.ATTACK_DAMAGE, -0.0075)
                );
        get(Biomes.FROZEN_OCEAN)
                .addBonus(ModifierDataType.BIOME, Map.of(
                        "swimming", 1.75,
                        "fishing", 1.4
                ))
                .addNegativeEffect(MobEffects.SLOWNESS, 2)
                .addNegativeEffect(MobEffects.WEAKNESS, 1)
                .addReq(Map.of("swimming", 35L, "endurance", 15L));
        get(Biomes.JUNGLE)
                .addReq(Map.of("endurance", 20L))
                .addBonus(ModifierDataType.BIOME, Map.of(
                        "agility", 1.25,
                        "swimming", 1.25,
                        "fishing", 1.25,
                        "combat", 1.10,
                        "archery", 1.35,
                        "woodcutting", 0.75,
                        "farming", 0.75))
                .addNegativeEffect(MobEffects.SLOWNESS, 0)
                .addNegativeEffect(MobEffects.WEAKNESS, 0)
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, 0.005),
                        modifiers(Attributes.MAX_HEALTH, 5.0)
                );
        get(Biomes.MEADOW)
                .addBonus(ModifierDataType.BIOME, Map.of("farming", 1.05))
                .addPositiveEffect(MobEffects.SPEED, 0)
                .addReq(Map.of("agility", 0L))
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, -0.0075),
                        modifiers(Attributes.MAX_HEALTH, -7.0),
                        modifiers(Attributes.ATTACK_DAMAGE, -0.0075)
                );
        get(Biomes.OCEAN).addBonus(ModifierDataType.BIOME, Map.of("swimming", 1.25, "fishing", 1.1));
        get(Biomes.PLAINS)
                .addBonus(ModifierDataType.BIOME, Map.of("farming", 1.05))
                .addPositiveEffect(MobEffects.SPEED, 0)
                .addReq(Map.of("agility", 0L))
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, -0.0075),
                        modifiers(Attributes.MAX_HEALTH, -7.0),
                        modifiers(Attributes.ATTACK_DAMAGE, -0.0075)
                );
        get(Biomes.RIVER)
                .addBonus(ModifierDataType.BIOME, Map.of("fishing", 1.05))
                .addGlobalModifier(
                        modifiers(Attributes.MOVEMENT_SPEED, -0.0075),
                        modifiers(Attributes.MAX_HEALTH, -7.0),
                        modifiers(Attributes.ATTACK_DAMAGE, -0.0075)
                );

        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private LocationData.Builder get(ResourceKey<?> key) {
        return data.computeIfAbsent(key.location(), i -> LocationData.build());
    }

    private MobModifier modifiers(Holder<Attribute> attribute, double value) {
        return new MobModifier(RegistryUtil.getId(attribute), value, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public String getName() {
        return "Project MMO Default Biome Generator";
    }
}
