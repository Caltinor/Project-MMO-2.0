package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.entity.EntityTypes;

import java.util.concurrent.CompletableFuture;


public class EntityTagProvider extends EntityTypeTagsProvider{

	public EntityTagProvider(PackOutput pOutput, CompletableFuture<Provider> pProvider) {
		super(pOutput, pProvider, Reference.MOD_ID);
	}
	
	@Override
	protected void addTags(Provider pProvider) {
		
		tag(Reference.BREEDABLE_TAG)
			.add(EntityTypeIds.AXOLOTL)
			.add(EntityTypeIds.BEE)
			.add(EntityTypeIds.CAT)
			.add(EntityTypeIds.CHICKEN)
			.add(EntityTypeIds.COW)
			.add(EntityTypeIds.DONKEY)
			.add(EntityTypeIds.FOX)
			.add(EntityTypeIds.FROG)
			.add(EntityTypeIds.GOAT)
			.add(EntityTypeIds.HOGLIN)
			.add(EntityTypeIds.HORSE)
			.add(EntityTypeIds.LLAMA)
			.add(EntityTypeIds.MOOSHROOM)
			.add(EntityTypeIds.OCELOT)
			.add(EntityTypeIds.PANDA)
			.add(EntityTypeIds.PIG)
			.add(EntityTypeIds.RABBIT)
			.add(EntityTypeIds.SHEEP)
			.add(EntityTypeIds.STRIDER)
			.add(EntityTypeIds.TRADER_LLAMA)
			.add(EntityTypeIds.TURTLE)
			.add(EntityTypeIds.WOLF);
		
		tag(Reference.RIDEABLE_TAG)
			.addTag(EntityTypeTags.BOAT)
			.add(EntityTypeIds.DONKEY)
			.add(EntityTypeIds.HORSE)
			.add(EntityTypeIds.MULE)
			.add(EntityTypeIds.PIG)
			.add(EntityTypeIds.SKELETON_HORSE)
			.add(EntityTypeIds.STRIDER);
		
		tag(Reference.TAMABLE_TAG)
			.add(EntityTypeIds.ALLAY)
			.add(EntityTypeIds.CAT)
			.add(EntityTypeIds.DONKEY)
			.add(EntityTypeIds.HORSE)
			.add(EntityTypeIds.LLAMA)
			.add(EntityTypeIds.MULE)
			.add(EntityTypeIds.OCELOT)
			.add(EntityTypeIds.SKELETON_HORSE)
			.add(EntityTypeIds.TRADER_LLAMA)
			.add(EntityTypeIds.WOLF);

		tag(Reference.MOB_TAG)
				.addTag(EntityTypeTags.RAIDERS)
				.addTag(EntityTypeTags.SKELETONS)
				.add(EntityTypeIds.BLAZE)
				.add(EntityTypeIds.CAVE_SPIDER)
				.add(EntityTypeIds.CREEPER)
				.add(EntityTypeIds.DROWNED)
				.add(EntityTypeIds.ELDER_GUARDIAN)
				.add(EntityTypeIds.ENDER_DRAGON)
				.add(EntityTypeIds.ENDERMAN)
				.add(EntityTypeIds.ENDERMITE)
				.add(EntityTypeIds.GHAST)
				.add(EntityTypeIds.GIANT)
				.add(EntityTypeIds.GUARDIAN)
				.add(EntityTypeIds.HOGLIN)
				.add(EntityTypeIds.HUSK)
				.add(EntityTypeIds.MAGMA_CUBE)
				.add(EntityTypeIds.PHANTOM)
				.add(EntityTypeIds.PIGLIN)
				.add(EntityTypeIds.PIGLIN_BRUTE)
				.add(EntityTypeIds.SHULKER)
				.add(EntityTypeIds.SILVERFISH)
				.add(EntityTypeIds.SKELETON_HORSE)
				.add(EntityTypeIds.SLIME)
				.add(EntityTypeIds.SPIDER)
				.add(EntityTypeIds.VEX)
				.add(EntityTypeIds.WARDEN)
				.add(EntityTypeIds.WITCH)
				.add(EntityTypeIds.WITHER)
				.add(EntityTypeIds.ZOGLIN)
				.add(EntityTypeIds.ZOMBIE)
				.add(EntityTypeIds.ZOMBIE_HORSE)
				.add(EntityTypeIds.ZOMBIE_VILLAGER)
				.add(EntityTypeIds.ZOMBIFIED_PIGLIN);
	}
}
