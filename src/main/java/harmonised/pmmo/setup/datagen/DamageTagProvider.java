package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class DamageTagProvider extends TagsProvider<DamageType>{

    public DamageTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        tag(Reference.FROM_ENVIRONMENT)
                .addTag(DamageTypeTags.IS_DROWNING)
                .addTag(DamageTypeTags.IS_EXPLOSION)
                .addTag(DamageTypeTags.IS_FIRE)
                .addTag(DamageTypeTags.IS_FREEZING)
                .addTag(DamageTypeTags.IS_LIGHTNING)
                .add(DamageTypes.CACTUS)
                .add(DamageTypes.CRAMMING)
                .add(DamageTypes.DROWN)
                .add(DamageTypes.FALLING_ANVIL)
                .add(DamageTypes.FALLING_BLOCK)
                .add(DamageTypes.FREEZE)
                .add(DamageTypes.IN_FIRE)
                .add(DamageTypes.LIGHTNING_BOLT)
                .add(DamageTypes.ON_FIRE)
                .add(DamageTypes.LAVA)
                .add(DamageTypes.HOT_FLOOR)
                .add(DamageTypes.IN_WALL)
                .add(DamageTypes.STARVE)
                .add(DamageTypes.SWEET_BERRY_BUSH);

        tag(Reference.FROM_IMPACT)
                .addTag(DamageTypeTags.IS_FALL)
                .add(DamageTypes.FLY_INTO_WALL);

        tag(Reference.FROM_MAGIC)
                .add(DamageTypes.MAGIC)
                .add(DamageTypes.INDIRECT_MAGIC)
                .addOptional(ResourceLocation.parse("irons_spellbooks:fire_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:ice_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:lightning_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:holy_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:ender_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:blood_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:evocation_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:eldritch_magic"))
                .addOptional(ResourceLocation.parse("irons_spellbooks:nature_magic"))
                .addOptional(ResourceLocation.parse("ars_nouveau:spell"))
                .addOptional(ResourceLocation.parse("ars_nouveau:frost"))
                .addOptional(ResourceLocation.parse("ars_nouveau:flare"))
                .addOptional(ResourceLocation.parse("ars_nouveau:crush"))
                .addOptional(ResourceLocation.parse("ars_nouveau:windshear"));
        tag(Reference.FROM_GUN)
                .addOptional(ResourceLocation.parse("cgm:bullet"))
                .addOptional(ResourceLocation.parse("scguns:bullet"));
    }
}
