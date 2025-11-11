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

import java.util.concurrent.CompletableFuture;

public class DamageTagProvider extends TagsProvider<DamageType> {

    public DamageTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Reference.MOD_ID);
    }

    @Override
    protected void addTags(Provider provider) {
        getOrCreateRawBuilder(Reference.FROM_ENVIRONMENT)
                .addTag(DamageTypeTags.IS_DROWNING.location())
                .addTag(DamageTypeTags.IS_EXPLOSION.location())
                .addTag(DamageTypeTags.IS_FIRE.location())
                .addTag(DamageTypeTags.IS_FREEZING.location())
                .addTag(DamageTypeTags.IS_LIGHTNING.location())
                .addElement(DamageTypes.CACTUS.location())
                .addElement(DamageTypes.CRAMMING.location())
                .addElement(DamageTypes.DROWN.location())
                .addElement(DamageTypes.FALLING_ANVIL.location())
                .addElement(DamageTypes.FALLING_BLOCK.location())
                .addElement(DamageTypes.FREEZE.location())
                .addElement(DamageTypes.IN_FIRE.location())
                .addElement(DamageTypes.LIGHTNING_BOLT.location())
                .addElement(DamageTypes.ON_FIRE.location())
                .addElement(DamageTypes.LAVA.location())
                .addElement(DamageTypes.HOT_FLOOR.location())
                .addElement(DamageTypes.IN_WALL.location())
                .addElement(DamageTypes.STARVE.location())
                .addElement(DamageTypes.SWEET_BERRY_BUSH.location());

        getOrCreateRawBuilder(Reference.FROM_IMPACT)
                .addTag(DamageTypeTags.IS_FALL.location())
                .addElement(DamageTypes.FLY_INTO_WALL.location());

        getOrCreateRawBuilder(Reference.FROM_MAGIC)
                .addElement(DamageTypes.MAGIC.location())
                .addElement(DamageTypes.INDIRECT_MAGIC.location())
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:fire_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:ice_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:lightning_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:holy_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:ender_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:blood_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:evocation_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:eldritch_magic"))
                .addOptionalElement(ResourceLocation.parse("irons_spellbooks:nature_magic"))
                .addOptionalElement(ResourceLocation.parse("ars_nouveau:spell"))
                .addOptionalElement(ResourceLocation.parse("ars_nouveau:frost"))
                .addOptionalElement(ResourceLocation.parse("ars_nouveau:flare"))
                .addOptionalElement(ResourceLocation.parse("ars_nouveau:crush"))
                .addOptionalElement(ResourceLocation.parse("ars_nouveau:windshear"))
                .addOptionalElement(ResourceLocation.parse("ars_elemental:spark"))
                .addOptionalElement(ResourceLocation.parse("ars_elemental:hellfire"))
                .addOptionalElement(ResourceLocation.parse("ars_elemental:beheading"))
                .addOptionalElement(ResourceLocation.parse("ars_elemental:poison"))
                .addOptionalElement(ResourceLocation.parse("spell_power:fire"))
                .addOptionalElement(ResourceLocation.parse("spell_power:arcane"))
                .addOptionalElement(ResourceLocation.parse("spell_power:frost"))
                .addOptionalElement(ResourceLocation.parse("spell_power:healing"))
                .addOptionalElement(ResourceLocation.parse("spell_power:lightning"))
                .addOptionalElement(ResourceLocation.parse("spell_power:soul"));
        getOrCreateRawBuilder(Reference.FROM_GUN)
                .addOptionalElement(ResourceLocation.parse("cgm:bullet"))
                .addOptionalElement(ResourceLocation.parse("scguns:bullet"));
    }
}
