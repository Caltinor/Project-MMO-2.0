package harmonised.pmmo.setup.datagen;

import harmonised.pmmo.features.loot_modifiers.GLMRegistry;
import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.SkillLootConditionKill;
import harmonised.pmmo.features.loot_modifiers.SkillLootConditionPlayer;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.features.loot_modifiers.ValidBlockCondition;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class GLMProvider extends GlobalLootModifierProvider{

	public GLMProvider(DataGenerator gen) {super(gen, Reference.MOD_ID);}

	@Override
	protected void start() {
		//===========TREASURE==================
		add("coal_ores", GLMRegistry.TREASURE_SERIALIZER, of(Tags.Blocks.ORES_COAL, Items.DIAMOND, 1, 0.01, "mining", 30));
		add("apple_from_leaves", GLMRegistry.TREASURE_SERIALIZER, of(BlockTags.LEAVES, Items.APPLE, 1, 0.025, "farming", 30));
		add("gapple_from_leaves", GLMRegistry.TREASURE_SERIALIZER,of(BlockTags.LEAVES, Items.GOLDEN_APPLE, 1, 0.01, "farming", 60));
		add("egapple_from_leaves", GLMRegistry.TREASURE_SERIALIZER,of(BlockTags.LEAVES, Items.ENCHANTED_GOLDEN_APPLE, 1, 0.01, "farming", 60));
		add("dirt_cake", GLMRegistry.TREASURE_SERIALIZER, of(BlockTags.DIRT, Items.CAKE, 1, 0.003251, "excavation", 0));
		add("dirt_bone", GLMRegistry.TREASURE_SERIALIZER, of(BlockTags.DIRT, Items.BONE, 1, 0.01, "excavation", 25));
		add("iron_dposits", GLMRegistry.TREASURE_SERIALIZER, of(Tags.Blocks.STONE, Items.IRON_NUGGET, 2, 0.001, "mining", 15));
		//add("pig_step", GLMRegistry.TREASURE_SERIALIZER, of(Tags.Blocks.BOOKSHELVES, Items.MUSIC_DISC_PIGSTEP, 1, 0.005, "building", 10));
		add("grass_grass", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.GRASS_BLOCK, Items.GRASS, 1, 0.01, "farming", 5));
		add("magma_to_cream", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.MAGMA_BLOCK, Items.MAGMA_CREAM, 1, 0.01, "mining", 25));
		add("nether_gold", GLMRegistry.TREASURE_SERIALIZER, of(Tags.Blocks.NETHERRACK, Items.GOLD_NUGGET, 2, 0.01, "mining", 20));
		add("berry_surprise", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.PODZOL, Items.SWEET_BERRIES, 2, 0.0, "farming", 15));
		add("tears_of_sand", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.SOUL_SAND, Items.GHAST_TEAR, 1, 0.001, "excavation", 40));
		add("tears_of_soil", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.SOUL_SOIL, Items.GHAST_TEAR, 1, 0.001, "excavation", 40));
		
		//===========EXTRA DROPS==================
		add("extra_logs", GLMRegistry.TREASURE_SERIALIZER, extra(BlockTags.LOGS, 1, 0.01, "woodcutting", 1));
		add("extra_coal", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_COAL, 1, 0.01, "mining", 1));
		add("extra_copper", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_COPPER, 1, 0.01, "mining", 5));
		add("extra_diamond", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_DIAMOND, 1, 0.0033, "mining", 30));
		add("extra_emerald", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_EMERALD, 1, 0.0075, "mining", 20));
		add("extra_debris", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS, 1, 0.01, "mining", 60));
		add("extra_gold", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_GOLD, 1, 0.005, "mining", 25));
		add("extra_iron", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_IRON, 1, 0.05, "mining", 20));
		add("extra_lapis", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_LAPIS,1, 0.0015,"mining", 30));
		add("extra_quartz", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_QUARTZ, 1, 0.005, "mining", 30));
		add("extra_redstone", GLMRegistry.TREASURE_SERIALIZER, extra(Tags.Blocks.ORES_REDSTONE, 3, 0.02, "mining", 20));
		add("extra_bamboo", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.BAMBOO, Items.BAMBOO, 1, 0.0035, "farming", 20));
		add("extra_beets", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.BEETROOTS, Items.BEETROOT, 1, 0.001, "farming",1));
		add("extra_cactus", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.CACTUS, Items.CACTUS, 1, 0.0045, "farming", 10));
		add("extra_carrot", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.CARROTS, Items.CARROT, 1, 0.015, "farming", 1));
		add("extra_cocoa", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.COCOA, Items.COCOA_BEANS, 1, 0.015, "farming", 15));
		add("extra_kelp", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.KELP, Items.KELP, 1, 0.025, "farming", 10));
		add("extra_kelp_plant", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.KELP_PLANT, Items.KELP, 1, 0.025, "farming", 10));
		add("extra_melon", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.MELON, Items.MELON_SLICE, 1, 0.01, "farming", 20));
		add("extra_wart", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.NETHER_WART, Items.NETHER_WART, 1, 0.075, "farming", 50));
		add("extra_potato", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.POTATOES, Items.POTATO, 1, 0.015, "farming", 1));
		add("extra_pumpkin", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.PUMPKIN, Items.PUMPKIN, 1, 0.006, "farming", 20));
		add("extra_pickle", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.SEA_PICKLE, Items.SEA_PICKLE, 1, 0.015, "farming", 10));
		add("extra_sugar", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.SUGAR_CANE, Items.SUGAR_CANE, 1, 0.033, "farming", 5));
		add("extra_wheat", GLMRegistry.TREASURE_SERIALIZER, of(Blocks.WHEAT, Items.WHEAT, 1, 0.05, "farming", 1));
		
		//===========RARE FISH POOL==================
		add("fish_bow", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.BOW, 1, 0.01, "fishing", 10));
		add("fish_rod", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.FISHING_ROD, 1, 0.05, "fishing", 1));
		add("fish_heart", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.HEART_OF_THE_SEA, 1, 0.001, "fishing", 30));
		add("fish_nautilus", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NAUTILUS_SHELL, 1, 0.005, "fishing", 25));
		add("fish_star", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHER_STAR, 1, 0.0001, "fishing", 60));
		add("fish_chain_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.CHAINMAIL_BOOTS, 1, 0.005, "fishing", 20));
		add("fish_chain_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.CHAINMAIL_CHESTPLATE, 1, 0.005, "fishing", 20));
		add("fish_chain_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.CHAINMAIL_HELMET, 1, 0.005, "fishing", 20));
		add("fish_chain_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.CHAINMAIL_LEGGINGS, 1, 0.005, "fishing", 20));
		add("fish_diamond_axe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_AXE, 1, 0.0001, "fishing", 50));
		add("fish_diamond_hoe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_HOE, 1, 0.0001, "fishing", 50));
		add("fish_diamond_pick", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_PICKAXE, 1, 0.0001, "fishing", 50));
		add("fish_diamond_shovel", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_SHOVEL, 1, 0.0001, "fishing", 50));
		add("fish_diamond_sword", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_SWORD, 1, 0.0001, "fishing", 50));
		add("fish_diamond_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_BOOTS, 1, 0.0001, "fishing", 50));
		add("fish_diamond_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_CHESTPLATE, 1, 0.0001, "fishing", 50));
		add("fish_diamond_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_HELMET, 1, 0.0001, "fishing", 50));
		add("fish_diamond_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.DIAMOND_LEGGINGS, 1, 0.0001, "fishing", 50));
		add("fish_gold_axe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_AXE, 1, 0.005, "fishing", 30));
		add("fish_gold_hoe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_HOE, 1, 0.005, "fishing", 30));
		add("fish_gold_pick", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_PICKAXE, 1, 0.005, "fishing", 30));
		add("fish_gold_shovel", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_SHOVEL, 1, 0.005, "fishing", 30));
		add("fish_gold_sword", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_SWORD, 1, 0.005, "fishing", 30));
		add("fish_gold_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_BOOTS, 1, 0.005, "fishing", 30));
		add("fish_gold_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_CHESTPLATE, 1, 0.005, "fishing", 30));
		add("fish_gold_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_HELMET, 1, 0.005, "fishing", 30));
		add("fish_gold_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.GOLDEN_LEGGINGS, 1, 0.005, "fishing", 30));
		add("fish_iron_axe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_AXE, 1, 0.01, "fishing", 20));
		add("fish_iron_hoe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_HOE, 1, 0.01, "fishing", 20));
		add("fish_iron_pick", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_PICKAXE, 1, 0.01, "fishing", 20));
		add("fish_iron_shovel", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_SHOVEL, 1, 0.01, "fishing", 20));
		add("fish_iron_sword", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_SWORD, 1, 0.01, "fishing", 20));
		add("fish_iron_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_BOOTS, 1, 0.01, "fishing", 20));
		add("fish_iron_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_CHESTPLATE, 1, 0.01, "fishing", 20));
		add("fish_iron_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_HELMET, 1, 0.01, "fishing", 20));
		add("fish_iron_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.IRON_LEGGINGS, 1, 0.01, "fishing", 20));	
		add("fish_leather_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.LEATHER_BOOTS, 1, 0.01, "fishing", 1));
		add("fish_leather_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.LEATHER_CHESTPLATE, 1, 0.01, "fishing", 1));
		add("fish_leather_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.LEATHER_HELMET, 1, 0.01, "fishing", 1));
		add("fish_leather_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.LEATHER_LEGGINGS, 1, 0.01, "fishing", 1));	
		add("fish_netherite_axe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_AXE, 1, 0.00001, "fishing", 70));
		add("fish_netherite_hoe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_HOE, 1, 0.00001, "fishing", 70));
		add("fish_netherite_pick", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_PICKAXE, 1, 0.00001, "fishing", 70));
		add("fish_netherite_shovel", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_SHOVEL, 1, 0.00001, "fishing", 70));
		add("fish_netherite_sword", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_SWORD, 1, 0.00001, "fishing", 70));
		add("fish_netherite_boots", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_BOOTS, 1, 0.00001, "fishing", 70));
		add("fish_netherite_plate", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_CHESTPLATE, 1, 0.00001, "fishing", 70));
		add("fish_netherite_helm", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_HELMET, 1, 0.00001, "fishing", 70));
		add("fish_netherite_pants", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.NETHERITE_LEGGINGS, 1, 0.00001, "fishing", 70));
		add("fish_wood_axe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.WOODEN_AXE, 1, 0.001, "fishing", 0, 15));
		add("fish_wood_hoe", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.WOODEN_HOE, 1, 0.001, "fishing", 0, 15));
		add("fish_wood_pick", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.WOODEN_PICKAXE, 1, 0.001, "fishing", 0, 15));
		add("fish_wood_shovel", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.WOODEN_SHOVEL, 1, 0.001, "fishing", 0, 15));
		add("fish_wood_sword", GLMRegistry.RARE_DROP_SERIALIZER, fish(Items.WOODEN_SWORD, 1, 0.001, "fishing", 0, 15));
		
		//===========RARE MOB DROPS==================
		add("mob_chicken", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.CHICKEN, Items.EGG, 1, 0.1, "breeding", 10));
		add("mob_dragon_head", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.ENDER_DRAGON, Items.DRAGON_HEAD, 1, 1.0, "slayer", 50));
		add("mob_dragon_egg", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.ENDER_DRAGON, Items.DRAGON_EGG, 1, 1.0, "slayer", 50));
		add("mob_sheep", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.SHEEP, Items.STRING, 1, 0.1, "breeding", 10));
		add("mob_slime", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.SLIME, Items.SLIME_BLOCK, 1, 1, "slayer", 30));
		add("mob_zombie", GLMRegistry.RARE_DROP_SERIALIZER, mob(EntityType.ZOMBIE, Items.BEETROOT, 1, 0.4, "combat", 20));
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
	
	private RareDropModifier fish(Item drop, int count, double chance, String skill, int minLevel, int maxLevel) {
		return new RareDropModifier(
				new LootItemCondition[] {
						LootTableIdCondition.builder(BuiltInLootTables.FISHING).build(),
						new SkillLootConditionKill(minLevel, maxLevel, skill)
				}, RegistryUtil.getId(drop), count, chance);
	}
	
	private RareDropModifier fish(Item drop, int count, double chance, String skill, int minLevel) {
		return fish(drop, count, chance, skill, minLevel, Integer.MAX_VALUE);
	}
	
	private RareDropModifier mob(EntityType<?> mob, Item drop, int count, double chance, String skill, int minLevel, int maxLevel) {
		return new RareDropModifier(
				new LootItemCondition[] {
						LootItemKilledByPlayerCondition.killedByPlayer().build(),
						LootTableIdCondition.builder(mob.getDefaultLootTable()).build(),
						new SkillLootConditionKill(minLevel, maxLevel, skill)
				}, RegistryUtil.getId(drop), count, chance);				
	}
	
	private RareDropModifier mob(EntityType<?> mob, Item drop, int count, double chance, String skill, int minLevel) {
		return mob(mob, drop, count, chance, skill, minLevel, Integer.MAX_VALUE);
	}
}
