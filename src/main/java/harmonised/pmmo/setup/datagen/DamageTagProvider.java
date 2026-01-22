package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
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
                .addElement(DamageTypes.CACTUS.identifier())
                .addElement(DamageTypes.CRAMMING.identifier())
                .addElement(DamageTypes.DROWN.identifier())
                .addElement(DamageTypes.FALLING_ANVIL.identifier())
                .addElement(DamageTypes.FALLING_BLOCK.identifier())
                .addElement(DamageTypes.FREEZE.identifier())
                .addElement(DamageTypes.IN_FIRE.identifier())
                .addElement(DamageTypes.LIGHTNING_BOLT.identifier())
                .addElement(DamageTypes.ON_FIRE.identifier())
                .addElement(DamageTypes.LAVA.identifier())
                .addElement(DamageTypes.HOT_FLOOR.identifier())
                .addElement(DamageTypes.IN_WALL.identifier())
                .addElement(DamageTypes.STARVE.identifier())
                .addElement(DamageTypes.SWEET_BERRY_BUSH.identifier());

        getOrCreateRawBuilder(Reference.FROM_IMPACT)
                .addTag(DamageTypeTags.IS_FALL.location())
                .addElement(DamageTypes.FLY_INTO_WALL.identifier());

        getOrCreateRawBuilder(Reference.FROM_MAGIC)
                .addElement(DamageTypes.MAGIC.identifier())
                .addElement(DamageTypes.INDIRECT_MAGIC.identifier())
                .addOptionalElement(Identifier.parse("irons_spellbooks:fire_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:ice_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:lightning_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:holy_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:ender_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:blood_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:evocation_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:eldritch_magic"))
                .addOptionalElement(Identifier.parse("irons_spellbooks:nature_magic"))
                .addOptionalElement(Identifier.parse("ars_nouveau:spell"))
                .addOptionalElement(Identifier.parse("ars_nouveau:frost"))
                .addOptionalElement(Identifier.parse("ars_nouveau:flare"))
                .addOptionalElement(Identifier.parse("ars_nouveau:crush"))
                .addOptionalElement(Identifier.parse("ars_nouveau:windshear"))
                .addOptionalElement(Identifier.parse("ars_elemental:spark"))
                .addOptionalElement(Identifier.parse("ars_elemental:hellfire"))
                .addOptionalElement(Identifier.parse("ars_elemental:beheading"))
                .addOptionalElement(Identifier.parse("ars_elemental:poison"))
                .addOptionalElement(Identifier.parse("spell_power:fire"))
                .addOptionalElement(Identifier.parse("spell_power:arcane"))
                .addOptionalElement(Identifier.parse("spell_power:frost"))
                .addOptionalElement(Identifier.parse("spell_power:healing"))
                .addOptionalElement(Identifier.parse("spell_power:lightning"))
                .addOptionalElement(Identifier.parse("spell_power:soul"));
        getOrCreateRawBuilder(Reference.FROM_GUN)
                .addOptionalElement(Identifier.parse("cgm:bullet"))
                .addOptionalElement(Identifier.parse("scguns:bullet"));
    }
}
