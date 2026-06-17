package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends ItemTagsProvider {

	public ItemTagProvider(PackOutput p_275343_, CompletableFuture<Provider> p_275729_) {
		super(p_275343_, p_275729_, Reference.MOD_ID);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(Reference.BREWABLES)
			.add(BlockItemIds.NETHER_WART.item())
			.add(ItemIds.GLOWSTONE_DUST)
			.add(ItemIds.FERMENTED_SPIDER_EYE)
			.add(ItemIds.GUNPOWDER)
			.add(ItemIds.DRAGON_BREATH)
			.add(ItemIds.SUGAR)
			.add(ItemIds.RABBIT_FOOT)
			.add(ItemIds.GLISTERING_MELON_SLICE)
			.add(ItemIds.SPIDER_EYE)
			.add(ItemIds.PUFFERFISH)
			.add(ItemIds.MAGMA_CREAM)
			.add(ItemIds.GOLDEN_CARROT)
			.add(ItemIds.BLAZE_POWDER)
			.add(ItemIds.GHAST_TEAR)
			.add(ItemIds.TURTLE_HELMET)
			.add(ItemIds.PHANTOM_MEMBRANE);
		
		tag(Reference.SMELTABLES)
			.addTag(Tags.Items.ORES)
			.addTag(Tags.Items.RAW_MATERIALS)
			.addTag(ItemTags.LOGS)
			.addTag(ItemTags.SAND)
			.addTag(ItemTags.TERRACOTTA)
			
			.add(BlockItemIds.STONE.item())
			.add(BlockItemIds.COBBLESTONE.item())
			.add(ItemIds.CLAY_BALL)
			.add(BlockItemIds.CLAY.item())
			.add(BlockItemIds.NETHERRACK.item())
			.add(BlockItemIds.NETHER_BRICKS.item())
			.add(BlockItemIds.BASALT.item())
			.add(BlockItemIds.QUARTZ_BLOCK.item())
			.add(BlockItemIds.CACTUS.item())
			.add(ItemIds.CHORUS_FRUIT)
			.add(BlockItemIds.WET_SPONGE.item())
			.add(BlockItemIds.SEA_PICKLE.item());
		
	}


}
