package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.features.loot_modifiers.SkillLootConditionPlayer;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.features.loot_modifiers.ValidBlockCondition;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class GLMProvider extends GlobalLootModifierProvider{

	public GLMProvider(DataGenerator gen) {super(gen, Reference.MOD_ID);}

	@Override
	protected void start() {
		//===========TREASURE==================
		add("coal_ores", of(Tags.Blocks.ORES_COAL, Items.DIAMOND, 1, 0.1, "mining", 30));
		add("apple_from_leaves", of(BlockTags.LEAVES, Items.APPLE, 1, 0.25, "farming", 30));
		add("gapple_from_leaves",of(BlockTags.LEAVES, Items.GOLDEN_APPLE, 1, 0.1, "farming", 60));
		add("egapple_from_leaves",of(BlockTags.LEAVES, Items.ENCHANTED_GOLDEN_APPLE, 1, 0.01, "farming", 60));
		add("dirt_cake", of(BlockTags.DIRT, Items.CAKE, 1, 0.003251, "excavation", 0));
		add("dirt_bone", of(BlockTags.DIRT, Items.BONE, 1, 0.05, "excavation", 25));
		add("iron_dposits", of(Tags.Blocks.STONE, Items.IRON_NUGGET, 2, 0.01, "mining", 15));
		add("pig_step", of(Tags.Blocks.BOOKSHELVES, Items.MUSIC_DISC_PIGSTEP, 1, 0.05, "building", 10));
		add("grass_grass", of(Blocks.GRASS_BLOCK, Items.GRASS, 1, 0.1, "farming", 5));
		add("magma_to_cream", of(Blocks.MAGMA_BLOCK, Items.MAGMA_CREAM, 1, 0.1, "mining", 25));
		add("nether_gold", of(Tags.Blocks.NETHERRACK, Items.GOLD_NUGGET, 2, 0.1, "mining", 20));
		add("berry_surprise", of(Blocks.PODZOL, Items.SWEET_BERRIES, 2, 0.1, "farming", 15));
		add("tears_of_sand", of(Blocks.SOUL_SAND, Items.GHAST_TEAR, 1, 0.01, "excavation", 40));
		add("tears_of_soil", of(Blocks.SOUL_SOIL, Items.GHAST_TEAR, 1, 0.01, "excavation", 40));
		
		//===========EXTRA DROPS==================
		add("extra_logs", extra(BlockTags.LOGS, 1, 0.01, "woodcutting", 1));
		add("extra_coal", extra(Tags.Blocks.ORES_COAL, 1, 0.01, "mining", 1));
		add("extra_copper", extra(Tags.Blocks.ORES_COPPER, 1, 0.01, "mining", 5));
		add("extra_diamond", extra(Tags.Blocks.ORES_DIAMOND, 1, 0.0033, "mining", 30));
		add("extra_emerald", extra(Tags.Blocks.ORES_EMERALD, 1, 0.0075, "mining", 20));
		add("extra_debris", of(Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS, 1, 0.01, "mining", 60));
		add("extra_gold", extra(Tags.Blocks.ORES_GOLD, 1, 0.05, "mining", 25));
		add("extra_iron", extra(Tags.Blocks.ORES_IRON, 1, 0.05, "mining", 20));
		add("extra_lapis",extra(Tags.Blocks.ORES_LAPIS,1, 0.015,"mining", 30));
		add("extra_quartz", extra(Tags.Blocks.ORES_QUARTZ, 1, 0.005, "mining", 30));
		add("extra_redstone", extra(Tags.Blocks.ORES_REDSTONE, 3, 0.02, "mining", 20));
	}

	private TreasureLootModifier of(TagKey<Block> validBlocks, Item drop, int count, double chance, String skill, int minLevel) {
		return new TreasureLootModifier(
				new LootItemCondition[] {
					new SkillLootConditionPlayer(minLevel, Integer.MAX_VALUE, skill),
					new ValidBlockCondition(validBlocks)
				}, RegistryUtil.getId(drop), count, chance);
	}
	
	private TreasureLootModifier of(Block validBlocks, Item drop, int count, double chance, String skill, int minLevel) {
		return new TreasureLootModifier(
				new LootItemCondition[] {
					new SkillLootConditionPlayer(minLevel, Integer.MAX_VALUE, skill),
					new ValidBlockCondition(validBlocks)
				}, RegistryUtil.getId(drop), count, chance);
	}
	
	/**Used to specify that a tag member should drop extra of itself.
	 * If not using tags, {@link #of(Block, Item, int, double, String, int) of()}
	 * should be used since this is a one-to-one relationship.
	 * 
	 * @param validBlocks the tag containing all applicable blocks
	 * @param count how many of the self should drop
	 * @param chance probablity of the extra drop
	 * @param skill skill associated with this extra drop
	 * @param minLevel level required to enable the extra drop
	 * @return modifier to be generated
	 */
	private TreasureLootModifier extra(TagKey<Block> validBlocks, int count, double chance, String skill, int minLevel) {
		return new TreasureLootModifier(
				new LootItemCondition[] {
					new SkillLootConditionPlayer(minLevel, Integer.MAX_VALUE, skill),
					new ValidBlockCondition(validBlocks)
				}, RegistryUtil.getId(Blocks.AIR), count, chance);
	}
}
