package harmonised.pmmo.setup.datagen.easypack;

import harmonised.pmmo.setup.datagen.GLMProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class EasyGLMProvider extends GLMProvider {
    private static final Path path = Path.of("resourcepacks/easy/data");
    public EasyGLMProvider(PackOutput gen, CompletableFuture<HolderLookup.Provider> registries) {
        super(gen, path, registries);
    }

    @Override
    protected void start() {
        //===========TREASURE==================
        add("coal_ores", of(Tags.Blocks.ORES_COAL, Items.DIAMOND, 1, 0.01, "mining", 300));
        add("apple_from_leaves", of(BlockTags.LEAVES, Items.APPLE, 1, 0.025, "farming", 300));
        add("gapple_from_leaves",of(BlockTags.LEAVES, Items.GOLDEN_APPLE, 1, 0.01, "farming", 600));
        add("egapple_from_leaves",of(BlockTags.LEAVES, Items.ENCHANTED_GOLDEN_APPLE, 1, 0.01, "farming", 600));
        add("dirt_cake", of(BlockTags.DIRT, Items.CAKE, 1, 0.003251, "excavation", 0));
        add("dirt_bone", of(BlockTags.DIRT, Items.BONE, 1, 0.01, "excavation", 250));
        add("iron_deposits", of(Tags.Blocks.STONES, Items.IRON_NUGGET, 2, 0.001, "mining", 150));
        add("pig_step", of(Tags.Blocks.BOOKSHELVES, Items.MUSIC_DISC_PIGSTEP, 1, 0.005, "building", 100));
        add("grass_grass", of(Blocks.GRASS_BLOCK, Items.GRASS_BLOCK, 1, 0.01, "farming", 50));
        add("magma_to_cream", of(Blocks.MAGMA_BLOCK, Items.MAGMA_CREAM, 1, 0.01, "mining", 250));
        add("nether_gold", of(Tags.Blocks.NETHERRACKS, Items.GOLD_NUGGET, 2, 0.01, "mining", 200));
        add("berry_surprise", of(Blocks.PODZOL, Items.SWEET_BERRIES, 2, 0.0, "farming", 150));
        add("tears_of_sand", of(Blocks.SOUL_SAND, Items.GHAST_TEAR, 1, 0.001, "excavation", 400));
        add("tears_of_soil", of(Blocks.SOUL_SOIL, Items.GHAST_TEAR, 1, 0.001, "excavation", 400));

        //===========EXTRA DROPS==================
        add("extra_logs", extra(BlockTags.LOGS, 1, 0.01, "woodcutting", 1));
        add("extra_coal", extra(Tags.Blocks.ORES_COAL, 1, 0.01, "mining", 1));
        add("extra_copper", extra(Tags.Blocks.ORES_COPPER, 1, 0.01, "mining", 50));
        add("extra_diamond", extra(Tags.Blocks.ORES_DIAMOND, 1, 0.0033, "mining", 300));
        add("extra_emerald", extra(Tags.Blocks.ORES_EMERALD, 1, 0.0075, "mining", 200));
        add("extra_debris", of(Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS, 1, 0.01, "mining", 6000));
        add("extra_gold", extra(Tags.Blocks.ORES_GOLD, 1, 0.005, "mining", 250));
        add("extra_iron", extra(Tags.Blocks.ORES_IRON, 1, 0.05, "mining", 200));
        add("extra_lapis",extra(Tags.Blocks.ORES_LAPIS,1, 0.0015,"mining", 300));
        add("extra_quartz", extra(Tags.Blocks.ORES_QUARTZ, 1, 0.005, "mining", 300));
        add("extra_redstone", extra(Tags.Blocks.ORES_REDSTONE, 3, 0.02, "mining", 200));
        add("extra_bamboo", of(Blocks.BAMBOO, Items.BAMBOO, 1, 0.0035, "farming", 200));
        add("extra_beets", of(Blocks.BEETROOTS, Items.BEETROOT, 1, 0.001, "farming",1));
        add("extra_cactus", of(Blocks.CACTUS, Items.CACTUS, 1, 0.0045, "farming", 100));
        add("extra_carrot", of(Blocks.CARROTS, Items.CARROT, 1, 0.015, "farming", 1));
        add("extra_cocoa", of(Blocks.COCOA, Items.COCOA_BEANS, 1, 0.015, "farming", 150));
        add("extra_kelp", of(Blocks.KELP, Items.KELP, 1, 0.025, "farming", 100));
        add("extra_kelp_plant", of(Blocks.KELP_PLANT, Items.KELP, 1, 0.025, "farming", 100));
        add("extra_melon", of(Blocks.MELON, Items.MELON_SLICE, 1, 0.01, "farming", 200));
        add("extra_wart", of(Blocks.NETHER_WART, Items.NETHER_WART, 1, 0.075, "farming", 500));
        add("extra_potato", of(Blocks.POTATOES, Items.POTATO, 1, 0.015, "farming", 1));
        add("extra_pumpkin", of(Blocks.PUMPKIN, Items.PUMPKIN, 1, 0.006, "farming", 200));
        add("extra_pickle", of(Blocks.SEA_PICKLE, Items.SEA_PICKLE, 1, 0.015, "farming", 100));
        add("extra_sugar", of(Blocks.SUGAR_CANE, Items.SUGAR_CANE, 1, 0.033, "farming", 50));
        add("extra_wheat", of(Blocks.WHEAT, Items.WHEAT, 1, 0.05, "farming", 1));

        //===========RARE FISH POOL==================
        add("fish_bow", fish(Items.BOW, 1, 0.01, "fishing", 100));
        add("fish_rod", fish(Items.FISHING_ROD, 1, 0.05, "fishing", 1));
        add("fish_heart", fish(Items.HEART_OF_THE_SEA, 1, 0.001, "fishing", 300));
        add("fish_nautilus", fish(Items.NAUTILUS_SHELL, 1, 0.005, "fishing", 250));
        add("fish_star", fish(Items.NETHER_STAR, 1, 0.0001, "fishing", 6000));
        add("fish_chain_boots", fish(Items.CHAINMAIL_BOOTS, 1, 0.005, "fishing", 200));
        add("fish_chain_plate", fish(Items.CHAINMAIL_CHESTPLATE, 1, 0.005, "fishing", 200));
        add("fish_chain_helm", fish(Items.CHAINMAIL_HELMET, 1, 0.005, "fishing", 200));
        add("fish_chain_pants", fish(Items.CHAINMAIL_LEGGINGS, 1, 0.005, "fishing", 200));
        add("fish_diamond_axe", fish(Items.DIAMOND_AXE, 1, 0.0001, "fishing", 500));
        add("fish_diamond_hoe", fish(Items.DIAMOND_HOE, 1, 0.0001, "fishing", 500));
        add("fish_diamond_pick", fish(Items.DIAMOND_PICKAXE, 1, 0.0001, "fishing", 500));
        add("fish_diamond_shovel", fish(Items.DIAMOND_SHOVEL, 1, 0.0001, "fishing", 500));
        add("fish_diamond_sword", fish(Items.DIAMOND_SWORD, 1, 0.0001, "fishing", 500));
        add("fish_diamond_boots", fish(Items.DIAMOND_BOOTS, 1, 0.0001, "fishing", 500));
        add("fish_diamond_plate", fish(Items.DIAMOND_CHESTPLATE, 1, 0.0001, "fishing", 500));
        add("fish_diamond_helm", fish(Items.DIAMOND_HELMET, 1, 0.0001, "fishing", 500));
        add("fish_diamond_pants", fish(Items.DIAMOND_LEGGINGS, 1, 0.0001, "fishing", 500));
        add("fish_gold_axe", fish(Items.GOLDEN_AXE, 1, 0.005, "fishing", 300));
        add("fish_gold_hoe", fish(Items.GOLDEN_HOE, 1, 0.005, "fishing", 300));
        add("fish_gold_pick", fish(Items.GOLDEN_PICKAXE, 1, 0.005, "fishing", 300));
        add("fish_gold_shovel", fish(Items.GOLDEN_SHOVEL, 1, 0.005, "fishing", 300));
        add("fish_gold_sword", fish(Items.GOLDEN_SWORD, 1, 0.005, "fishing", 300));
        add("fish_gold_boots", fish(Items.GOLDEN_BOOTS, 1, 0.005, "fishing", 300));
        add("fish_gold_plate", fish(Items.GOLDEN_CHESTPLATE, 1, 0.005, "fishing", 300));
        add("fish_gold_helm", fish(Items.GOLDEN_HELMET, 1, 0.005, "fishing", 300));
        add("fish_gold_pants", fish(Items.GOLDEN_LEGGINGS, 1, 0.005, "fishing", 300));
        add("fish_iron_axe", fish(Items.IRON_AXE, 1, 0.01, "fishing", 200));
        add("fish_iron_hoe", fish(Items.IRON_HOE, 1, 0.01, "fishing", 200));
        add("fish_iron_pick", fish(Items.IRON_PICKAXE, 1, 0.01, "fishing", 200));
        add("fish_iron_shovel", fish(Items.IRON_SHOVEL, 1, 0.01, "fishing", 200));
        add("fish_iron_sword", fish(Items.IRON_SWORD, 1, 0.01, "fishing", 200));
        add("fish_iron_boots", fish(Items.IRON_BOOTS, 1, 0.01, "fishing", 200));
        add("fish_iron_plate", fish(Items.IRON_CHESTPLATE, 1, 0.01, "fishing", 200));
        add("fish_iron_helm", fish(Items.IRON_HELMET, 1, 0.01, "fishing", 200));
        add("fish_iron_pants", fish(Items.IRON_LEGGINGS, 1, 0.01, "fishing", 200));
        add("fish_leather_boots", fish(Items.LEATHER_BOOTS, 1, 0.01, "fishing", 1));
        add("fish_leather_plate", fish(Items.LEATHER_CHESTPLATE, 1, 0.01, "fishing", 1));
        add("fish_leather_helm", fish(Items.LEATHER_HELMET, 1, 0.01, "fishing", 1));
        add("fish_leather_pants", fish(Items.LEATHER_LEGGINGS, 1, 0.01, "fishing", 1));
        add("fish_netherite_axe", fish(Items.NETHERITE_AXE, 1, 0.00001, "fishing", 700));
        add("fish_netherite_hoe", fish(Items.NETHERITE_HOE, 1, 0.00001, "fishing", 700));
        add("fish_netherite_pick", fish(Items.NETHERITE_PICKAXE, 1, 0.00001, "fishing", 700));
        add("fish_netherite_shovel", fish(Items.NETHERITE_SHOVEL, 1, 0.00001, "fishing", 700));
        add("fish_netherite_sword", fish(Items.NETHERITE_SWORD, 1, 0.00001, "fishing", 700));
        add("fish_netherite_boots", fish(Items.NETHERITE_BOOTS, 1, 0.00001, "fishing", 700));
        add("fish_netherite_plate", fish(Items.NETHERITE_CHESTPLATE, 1, 0.00001, "fishing", 700));
        add("fish_netherite_helm", fish(Items.NETHERITE_HELMET, 1, 0.00001, "fishing", 700));
        add("fish_netherite_pants", fish(Items.NETHERITE_LEGGINGS, 1, 0.00001, "fishing", 700));
        add("fish_wood_axe", fish(Items.WOODEN_AXE, 1, 0.001, "fishing", 0, 150));
        add("fish_wood_hoe", fish(Items.WOODEN_HOE, 1, 0.001, "fishing", 0, 150));
        add("fish_wood_pick", fish(Items.WOODEN_PICKAXE, 1, 0.001, "fishing", 0, 150));
        add("fish_wood_shovel", fish(Items.WOODEN_SHOVEL, 1, 0.001, "fishing", 0, 150));
        add("fish_wood_sword", fish(Items.WOODEN_SWORD, 1, 0.001, "fishing", 0, 150));

        //===========RARE MOB DROPS==================
        add("mob_chicken", mob(EntityType.CHICKEN, Items.EGG, 1, 0.1, "combat", 100));
        add("mob_dragon_head", mob(EntityType.ENDER_DRAGON, Items.DRAGON_HEAD, 1, 1.0, "combat", 5000));
        add("mob_dragon_egg", mob(EntityType.ENDER_DRAGON, Items.DRAGON_EGG, 1, 1.0, "combat", 5000));
        add("mob_sheep", mob(EntityType.SHEEP, Items.STRING, 1, 0.1, "farming", 100));
        add("mob_slime", mob(EntityType.SLIME, Items.SLIME_BLOCK, 1, 1, "combat", 300));
        add("mob_zombie", mob(EntityType.ZOMBIE, Items.BEETROOT, 1, 0.4, "combat", 200));
    }
}
