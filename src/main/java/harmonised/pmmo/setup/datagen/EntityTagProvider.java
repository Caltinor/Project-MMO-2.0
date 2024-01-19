package harmonised.pmmo.setup.datagen;

import java.util.concurrent.CompletableFuture;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;


public class EntityTagProvider extends EntityTypeTagsProvider{

	public EntityTagProvider(PackOutput pOutput, CompletableFuture<Provider> pProvider,	@Nullable ExistingFileHelper existingFileHelper) {
		super(pOutput, pProvider, Reference.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void addTags(Provider pProvider) {
		tag(Reference.BREEDABLE_TAG)
			.add(EntityType.AXOLOTL)
			.add(EntityType.BEE)
			.add(EntityType.CAT)
			.add(EntityType.CHICKEN)
			.add(EntityType.COW)
			.add(EntityType.DONKEY)
			.add(EntityType.FOX)
			.add(EntityType.FROG)
			.add(EntityType.GOAT)
			.add(EntityType.HOGLIN)
			.add(EntityType.HORSE)
			.add(EntityType.LLAMA)
			.add(EntityType.MOOSHROOM)
			.add(EntityType.OCELOT)
			.add(EntityType.PANDA)
			.add(EntityType.PIG)
			.add(EntityType.RABBIT)
			.add(EntityType.SHEEP)
			.add(EntityType.STRIDER)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.TURTLE)
			.add(EntityType.WOLF);
		
		tag(Reference.RIDEABLE_TAG)
			.add(EntityType.BOAT)
			.add(EntityType.DONKEY)
			.add(EntityType.HORSE)
			.add(EntityType.MULE)
			.add(EntityType.PIG)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.STRIDER);
		
		tag(Reference.TAMABLE_TAG)
			.add(EntityType.ALLAY)
			.add(EntityType.CAT)
			.add(EntityType.DONKEY)
			.add(EntityType.HORSE)
			.add(EntityType.LLAMA)
			.add(EntityType.MULE)
			.add(EntityType.OCELOT)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.WOLF);
	}
}
