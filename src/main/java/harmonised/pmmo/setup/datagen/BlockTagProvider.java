package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider{

	public BlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
		super(output, lookupProvider, Reference.MOD_ID);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(Reference.CASCADING_BREAKABLES)
			.add(BlockItemIds.SUGAR_CANE.block())
			.add(BlockItemIds.BAMBOO.block())
			.add(BlockItemIds.CACTUS.block())
			.add(BlockItemIds.CHORUS_PLANT.block())
			.add(BlockItemIds.POINTED_DRIPSTONE.block())
			.add(BlockItemIds.KELP.block())
			.add(BlockIds.KELP_PLANT)
			.add(BlockItemIds.TWISTING_VINES.block())
			.add(BlockIds.TWISTING_VINES_PLANT)
			.add(BlockItemIds.WEEPING_VINES.block())
			.add(BlockIds.WEEPING_VINES_PLANT)
			.add(BlockIds.CAVE_VINES_PLANT)
			.add(BlockIds.CAVE_VINES_PLANT);
		
		tag(Reference.CROPS)
			.addTag(BlockTags.CROPS)
			.add(BlockItemIds.MELON.block())
			.add(BlockItemIds.PUMPKIN.block())
			.add(BlockItemIds.BAMBOO.block())
			.add(BlockItemIds.COCOA_CROP.block())
			.add(BlockItemIds.SUGAR_CANE.block())
			.add(BlockItemIds.SWEET_BERRY_CROP.block())
			.add(BlockItemIds.CACTUS.block())
			.add(BlockItemIds.RED_MUSHROOM.block())
			.add(BlockItemIds.BROWN_MUSHROOM.block())
			.add(BlockItemIds.KELP.block())
			.add(BlockIds.KELP_PLANT)
			.add(BlockItemIds.NETHER_WART.block())
			.add(BlockItemIds.SEA_PICKLE.block())
			.add(BlockItemIds.CHORUS_PLANT.block())
			.add(BlockItemIds.TWISTING_VINES.block())
			.add(BlockIds.TWISTING_VINES_PLANT)
			.add(BlockItemIds.WEEPING_VINES.block())
			.add(BlockIds.WEEPING_VINES_PLANT)
			.add(BlockIds.CAVE_VINES_PLANT);
	}

}
