package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends ItemTagsProvider{

	public ItemTagProvider(PackOutput p_275343_, CompletableFuture<Provider> p_275729_,	CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
		super(p_275343_, p_275729_, p_275322_, Reference.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(Reference.BREWABLES)
			.add(Items.NETHER_WART)
			.add(Items.REDSTONE)
			.add(Items.GLOWSTONE_DUST)
			.add(Items.FERMENTED_SPIDER_EYE)
			.add(Items.GUNPOWDER)
			.add(Items.DRAGON_BREATH)
			.add(Items.SUGAR)
			.add(Items.RABBIT_FOOT)
			.add(Items.GLISTERING_MELON_SLICE)
			.add(Items.SPIDER_EYE)
			.add(Items.PUFFERFISH)
			.add(Items.MAGMA_CREAM)
			.add(Items.GOLDEN_CARROT)
			.add(Items.BLAZE_POWDER)
			.add(Items.GHAST_TEAR)
			.add(Items.TURTLE_HELMET)
			.add(Items.PHANTOM_MEMBRANE);
		
		tag(Reference.SMELTABLES)
			.addTag(Tags.Items.ORES)
			.addTag(Tags.Items.RAW_MATERIALS)
			.addTag(ItemTags.LOGS)
			.addTag(ItemTags.SAND)
			.addTag(ItemTags.TERRACOTTA)
			
			.add(Items.STONE)
			.add(Items.COBBLESTONE)
			.add(Items.CLAY_BALL)
			.add(Items.CLAY)
			.add(Items.NETHERRACK)
			.add(Items.NETHER_BRICKS)
			.add(Items.BASALT)
			.add(Items.QUARTZ_BLOCK)
			.add(Items.CACTUS)
			.add(Items.CHORUS_FRUIT)
			.add(Items.WET_SPONGE)
			.add(Items.SEA_PICKLE);
		
	}


}
