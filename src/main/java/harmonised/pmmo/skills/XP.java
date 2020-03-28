package harmonised.pmmo.skills;

import java.util.*;
import java.util.function.Consumer;

import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.DP;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class XP
{
	private static Map<ResourceLocation, Float> xpValues = new HashMap<>();
	private static Map<ResourceLocation, Float> oreDoubleValues = new HashMap<>();
	private static Map<ResourceLocation, Float> plantDoubleValues = new HashMap<>();
	private static Map<ResourceLocation, Float> salvageBaseValue = new HashMap<>();
	private static Map<ResourceLocation, Float> salvageValuePerLevel = new HashMap<>();
	private static Map<ResourceLocation, Float> repairXp = new HashMap<>();
	private static Map<ResourceLocation, Float> salvageXp = new HashMap<>();
	private static Map<ResourceLocation, Boolean> noDropOres = new HashMap<>();
	private static Map<ResourceLocation, ItemStack> oreItems = new HashMap<>();
	private static Map<ResourceLocation, ItemStack> toolItems = new HashMap<>();
	private static Map<ResourceLocation, Integer> toolItemAmount = new HashMap<>();
	private static Map<Material, String> materialHarvestTool = new HashMap<>();
	private static Map<String, Integer> skillColors = new HashMap<>();
	private static Map<String, Long> lastAward = new HashMap<>();
	private static Map<String, BlockPos> lastPosPlaced = new HashMap<>();
	public static Map<String, TextFormatting> skillTextFormat = new HashMap<>();
	public static List<String> validSkills = new ArrayList<String>();
	public static double baseXp, xpIncreasePerLevel;
	public static double globalMultiplier = Config.config.globalMultiplier.get();
	public static int maxLevel = Config.config.maxLevel.get();
	public static float maxXp = xpAtLevel( maxLevel );

	public static void initValues()
	{
////////////////////////////////////VALID_SKILLS///////////////////////////////////////////////////
		validSkills.add( "mining" );
		validSkills.add( "building" );
		validSkills.add( "excavation" );
		validSkills.add( "woodcutting" );
		validSkills.add( "farming" );
		validSkills.add( "agility" );
		validSkills.add( "endurance" );
		validSkills.add( "combat" );
		validSkills.add( "archery" );
		validSkills.add( "repairing" );
		validSkills.add( "flying" );
		validSkills.add( "swimming" );
		validSkills.add( "fishing" );
		validSkills.add( "crafting" );
////////////////////////////////////XP_VALUES//////////////////////////////////////////////////////
//		xpValues.put( Blocks.GRASS.getRegistryName(), 2.5f );
		xpValues.put( Blocks.AIR.getRegistryName(), 0.0f );
//		xpValues.put( Blocks.DOUBLE_PLANT.getRegistryName(), 25.0f );
		xpValues.put( Blocks.TALL_GRASS.getRegistryName(), 6.0f );
		xpValues.put( Blocks.WHEAT.getRegistryName(), 15.0f );
		xpValues.put( Blocks.POTATOES.getRegistryName(), 7.5f );
		xpValues.put( Blocks.CARROTS.getRegistryName(), 7.5f );
		xpValues.put( Blocks.COCOA.getRegistryName(), 30.0f );
		xpValues.put( Blocks.BEETROOTS.getRegistryName(), 20.0f );
		xpValues.put( Blocks.NETHER_WART.getRegistryName(), 5.0f );
		xpValues.put( Blocks.OAK_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.ACACIA_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.BAMBOO_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.BIRCH_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.DARK_OAK_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.JUNGLE_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.SPRUCE_SAPLING.getRegistryName(), 10.0f );
		xpValues.put( Blocks.END_ROD.getRegistryName(), 25.0f );
		xpValues.put( Blocks.ACACIA_LEAVES.getRegistryName(), 1.1f );
		xpValues.put( Blocks.BIRCH_LEAVES.getRegistryName(), 0.7f );
		xpValues.put( Blocks.DARK_OAK_LEAVES.getRegistryName(), 0.6f );
		xpValues.put( Blocks.JUNGLE_LEAVES.getRegistryName(), 0.9f );
		xpValues.put( Blocks.OAK_LEAVES.getRegistryName(), 0.5f );
		xpValues.put( Blocks.SPRUCE_LEAVES.getRegistryName(), 1.5f );
		xpValues.put( Blocks.PUMPKIN.getRegistryName(), 35.0f );
		xpValues.put( Blocks.MELON.getRegistryName(), 7.0f );
		xpValues.put( Blocks.SUGAR_CANE.getRegistryName(), 8.0f );
		xpValues.put( Blocks.CACTUS.getRegistryName(), 15.0f );
		xpValues.put( Blocks.KELP_PLANT.getRegistryName(), 4.0f );
		xpValues.put( Blocks.SEA_PICKLE.getRegistryName(), 2.0f );
		xpValues.put( Blocks.DEAD_BUSH.getRegistryName(), 5.0f );
		xpValues.put( Blocks.LILY_PAD.getRegistryName(), 15.0f );
		xpValues.put( Blocks.DANDELION.getRegistryName(), 15.0f );
		xpValues.put( Blocks.POPPY.getRegistryName(), 17.5f );
		xpValues.put( Blocks.BLUE_ORCHID.getRegistryName(), 20.0f );
		xpValues.put( Blocks.POPPY.getRegistryName(), 17.5f );
		xpValues.put( Blocks.ALLIUM.getRegistryName(), 12.5f );
		xpValues.put( Blocks.AZURE_BLUET.getRegistryName(), 14.5f );
		xpValues.put( Blocks.ORANGE_TULIP.getRegistryName(), 16.5f );
		xpValues.put( Blocks.PINK_TULIP.getRegistryName(), 16.0f );
		xpValues.put( Blocks.RED_TULIP.getRegistryName(), 17.5f );
		xpValues.put( Blocks.WHITE_TULIP.getRegistryName(), 18.5f );
		xpValues.put( Blocks.OXEYE_DAISY.getRegistryName(), 14.5f );
		xpValues.put( Blocks.CORNFLOWER.getRegistryName(), 15.5f );
		xpValues.put( Blocks.LILY_OF_THE_VALLEY.getRegistryName(), 19.5f );
		xpValues.put( Blocks.WITHER_ROSE.getRegistryName(), 35.0f );
		xpValues.put( Blocks.BROWN_MUSHROOM.getRegistryName(), 10.0f );
		xpValues.put( Blocks.BROWN_MUSHROOM_BLOCK.getRegistryName(), 3.0f );
		xpValues.put( Blocks.RED_MUSHROOM.getRegistryName(), 12.0f );
		xpValues.put( Blocks.RED_MUSHROOM_BLOCK.getRegistryName(), 4.0f );
		xpValues.put( Blocks.CHORUS_PLANT.getRegistryName(), 2.5f );
		xpValues.put( Blocks.CHORUS_FLOWER.getRegistryName(), 35.0f );
		xpValues.put( Blocks.VINE.getRegistryName(), 3.5f );
		xpValues.put( Blocks.COBWEB.getRegistryName(), 5.0f );
		xpValues.put( Blocks.ACACIA_LOG.getRegistryName(), 35.0f );
		xpValues.put( Blocks.BIRCH_LOG.getRegistryName(), 28.0f );
		xpValues.put( Blocks.DARK_OAK_LOG.getRegistryName(), 15.0f );
		xpValues.put( Blocks.JUNGLE_LOG.getRegistryName(), 18.0f );
		xpValues.put( Blocks.OAK_LOG.getRegistryName(), 28.0f );
		xpValues.put( Blocks.SPRUCE_LOG.getRegistryName(), 30.0f );
		xpValues.put( Blocks.STRIPPED_ACACIA_LOG.getRegistryName(), 35.0f );
		xpValues.put( Blocks.STRIPPED_BIRCH_LOG.getRegistryName(), 28.0f );
		xpValues.put( Blocks.STRIPPED_DARK_OAK_LOG.getRegistryName(), 15.0f );
		xpValues.put( Blocks.STRIPPED_JUNGLE_LOG.getRegistryName(), 18.0f );
		xpValues.put( Blocks.STRIPPED_OAK_LOG.getRegistryName(), 28.0f );
		xpValues.put( Blocks.STRIPPED_SPRUCE_LOG.getRegistryName(), 30.0f );
		xpValues.put( Blocks.ACACIA_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.BIRCH_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.DARK_OAK_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.JUNGLE_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.OAK_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.SPRUCE_PLANKS.getRegistryName(), 5.0f );
		xpValues.put( Blocks.BOOKSHELF.getRegistryName(), 100.0f );
		xpValues.put( Blocks.DIRT.getRegistryName(), 1.5f );
		xpValues.put( Blocks.GRASS.getRegistryName(), 2.5f );
		xpValues.put( Blocks.SAND.getRegistryName(), 1.0f );
		xpValues.put( Blocks.GRAVEL.getRegistryName(), 2.0f );
		xpValues.put( Blocks.MYCELIUM.getRegistryName(), 15.0f );
		xpValues.put( Blocks.SLIME_BLOCK.getRegistryName(), 50.0f );
//		xpValues.put( Blocks.MONSTER_EGG.getRegistryName(), 200.0f );
//		xpValues.put( Blocks.STONE.getRegistryName(), 1.0f );
//		xpValues.put( Blocks.COBBLESTONE.getRegistryName(), 0.5f );
		xpValues.put( Blocks.COAL_ORE.getRegistryName(), 2.0f );
		xpValues.put( Blocks.IRON_ORE.getRegistryName(), 12.0f );
		xpValues.put( Blocks.REDSTONE_ORE.getRegistryName(), 1.5f );
//		xpValues.put( Blocks.LIT_REDSTONE_ORE.getRegistryName(), 1.5f );
		xpValues.put( Blocks.GOLD_ORE.getRegistryName(), 22.0f );
		xpValues.put( Blocks.LAPIS_ORE.getRegistryName(), 5.0f );
		xpValues.put( Blocks.NETHER_QUARTZ_ORE.getRegistryName(), 1.5f );
		xpValues.put( Blocks.DIAMOND_ORE.getRegistryName(), 35.0f );
		xpValues.put( Blocks.EMERALD_ORE.getRegistryName(), 60.0f );
		xpValues.put(Items.SALMON.getRegistryName(), 60.0f );
		xpValues.put(Items.COD.getRegistryName(), 50.0f );
		xpValues.put(Items.TROPICAL_FISH.getRegistryName(), 70.0f );
		xpValues.put(Items.PUFFERFISH.getRegistryName(), 35.0f );

		xpValues.put( Items.SADDLE.getRegistryName(), 250.0f );
		xpValues.put( Items.NAME_TAG.getRegistryName(), 350.0f );
////////////////////////////////////COLOR_VALUES///////////////////////////////////////////////////
		skillColors.put( "mining", 0x00ffff );
		skillColors.put( "building", 0x00ffff );
		skillColors.put( "excavation", 0xe69900 );
		skillColors.put( "woodcutting", 0xffa31a );
		skillColors.put( "farming", 0x00e600 );
		skillColors.put( "agility", 0x66cc66 );
		skillColors.put( "endurance", 0xcc0000 );
		skillColors.put( "combat", 0xff3300 );
		skillColors.put( "archery", 0xffff00 );
		skillColors.put( "repairing", 0xf0f0f0 );
		skillColors.put( "flying", 0xccccff );
		skillColors.put( "swimming", 0x3366ff );
		skillColors.put( "fishing", 0x00ccff );
		skillColors.put( "crafting", 0xff9900 );

		skillTextFormat.put( "mining", TextFormatting.AQUA );
		skillTextFormat.put( "building", TextFormatting.AQUA );
		skillTextFormat.put( "excavation", TextFormatting.GOLD );
		skillTextFormat.put( "woodcutting", TextFormatting.GOLD );
		skillTextFormat.put( "farming", TextFormatting.GREEN );
		skillTextFormat.put( "agility", TextFormatting.GREEN );
		skillTextFormat.put( "endurance", TextFormatting.DARK_RED );
		skillTextFormat.put( "combat", TextFormatting.RED);
		skillTextFormat.put( "archery", TextFormatting.YELLOW );
		skillTextFormat.put( "repairing", TextFormatting.GRAY );
		skillTextFormat.put( "flying", TextFormatting.GRAY );
		skillTextFormat.put( "swimming", TextFormatting.AQUA );
		skillTextFormat.put( "fishing", TextFormatting.AQUA );
		skillTextFormat.put( "crafting", TextFormatting.GOLD );
////////////////////////////////////ORE_DOUBLE_VALUES//////////////////////////////////////////////
		oreDoubleValues.put( Blocks.COAL_ORE.getRegistryName(), 1.0f );
		oreDoubleValues.put( Items.COAL.getRegistryName(), 1.0f );

		oreDoubleValues.put( Blocks.IRON_ORE.getRegistryName(), 0.75f );
		oreDoubleValues.put( Items.IRON_INGOT.getRegistryName(), 0.75f );


		oreDoubleValues.put( Blocks.REDSTONE_ORE.getRegistryName(), 2.0f );
//		oreDoubleValues.put( Blocks.LIT_REDSTONE_ORE.getRegistryName(), 2.0f );
		oreDoubleValues.put( Items.REDSTONE.getRegistryName(), 2.0f );

		oreDoubleValues.put( Blocks.GOLD_ORE.getRegistryName(), 0.50f );
		oreDoubleValues.put( Items.GOLD_INGOT.getRegistryName(), 0.50f );
		oreDoubleValues.put( Blocks.COAL_ORE.getRegistryName(), 0.5f );

		oreDoubleValues.put( Blocks.LAPIS_ORE.getRegistryName(), 1.50f );
		oreDoubleValues.put( Items.INK_SAC.getRegistryName(), 1.50f );

		oreDoubleValues.put( Blocks.NETHER_QUARTZ_ORE.getRegistryName(), 0.55f );
		oreDoubleValues.put( Items.QUARTZ.getRegistryName(), 0.55f );

		oreDoubleValues.put( Blocks.DIAMOND_ORE.getRegistryName(), 0.33f );
		oreDoubleValues.put( Items.DIAMOND.getRegistryName(), 0.33f );

		oreDoubleValues.put( Blocks.EMERALD_ORE.getRegistryName(), 0.75f );
		oreDoubleValues.put( Items.EMERALD.getRegistryName(), 0.75f );
////////////////////////////////////PLANT_DOUBLE_VALUES////////////////////////////////////////////
		plantDoubleValues.put( Items.WHEAT.getRegistryName(), 0.75f );
		plantDoubleValues.put( Items.WHEAT_SEEDS.getRegistryName(), 0.75f );

		plantDoubleValues.put( Items.CARROT.getRegistryName(), 1.50f );

		plantDoubleValues.put( Items.POISONOUS_POTATO.getRegistryName(), 1.50f );
		plantDoubleValues.put( Items.POTATO.getRegistryName(), 1.50f );

		plantDoubleValues.put( Items.BEETROOT.getRegistryName(), 1.00f );
		plantDoubleValues.put( Items.BEETROOT_SEEDS.getRegistryName(), 1.00f );

		plantDoubleValues.put( Items.SUGAR_CANE.getRegistryName(), 0.33f );
		plantDoubleValues.put( Blocks.CACTUS.getRegistryName(), 0.45f );
		plantDoubleValues.put( Items.NETHER_WART.getRegistryName(), 0.75f );

		plantDoubleValues.put( Blocks.PUMPKIN.getRegistryName(), 0.60f );
		plantDoubleValues.put( Items.PUMPKIN_SEEDS.getRegistryName(), 0.60f );

		plantDoubleValues.put( Blocks.MELON.getRegistryName(), 0.75f );
		plantDoubleValues.put( Items.MELON.getRegistryName(), 0.75f );
		plantDoubleValues.put( Items.MELON_SEEDS.getRegistryName(), 0.75f );

		plantDoubleValues.put( Items.COCOA_BEANS.getRegistryName(), 1.50f );
		plantDoubleValues.put( Items.KELP.getRegistryName(), 0.25f );
		plantDoubleValues.put( Items.SEA_PICKLE.getRegistryName(), 1.50f );

////////////////////////////////////NO_DROP_VALUES/////////////////////////////////////////////////
		noDropOres.put( Blocks.IRON_ORE.getRegistryName(), true );
		noDropOres.put( Blocks.GOLD_ORE.getRegistryName(), true );
////////////////////////////////////ORE_ITEMS_VALUES///////////////////////////////////////////////
		oreItems.put( Blocks.IRON_ORE.getRegistryName(), new ItemStack( Blocks.IRON_ORE ) );
		oreItems.put( Blocks.GOLD_ORE.getRegistryName(), new ItemStack( Blocks.GOLD_ORE ) );
		oreItems.put( Blocks.REDSTONE_ORE.getRegistryName(), new ItemStack( Items.REDSTONE) );
//		oreItems.put( Blocks.LIT_REDSTONE_ORE.getRegistryName(), new ItemStack( Items.REDSTONE, 0 ) );
		oreItems.put( Blocks.LAPIS_ORE.getRegistryName(), new ItemStack( Items.LAPIS_LAZULI ) );
		oreItems.put( Blocks.NETHER_QUARTZ_ORE.getRegistryName(), new ItemStack( Items.QUARTZ ) );
		oreItems.put( Blocks.DIAMOND_ORE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		oreItems.put( Blocks.EMERALD_ORE.getRegistryName(), new ItemStack( Items.EMERALD ) );
////////////////////////////////////MATERIAL_HARVEST_TOOLS/////////////////////////////////////////
		materialHarvestTool.put( Material.ANVIL, "pickaxe" );				//PICKAXE
		materialHarvestTool.put( Material.GLASS, "pickaxe" );
		materialHarvestTool.put( Material.ICE, "pickaxe" );
		materialHarvestTool.put( Material.IRON, "pickaxe" );
		materialHarvestTool.put( Material.PACKED_ICE, "pickaxe" );
		materialHarvestTool.put( Material.PISTON, "pickaxe" );
		materialHarvestTool.put( Material.REDSTONE_LIGHT, "pickaxe" );
		materialHarvestTool.put( Material.ROCK, "pickaxe" );
		materialHarvestTool.put( Material.SHULKER, "pickaxe" );
		materialHarvestTool.put( Material.BARRIER, "pickaxe" );

		materialHarvestTool.put( Material.WOOD, "axe" );					//AXE
		materialHarvestTool.put( Material.LEAVES, "axe" );

		materialHarvestTool.put( Material.CLAY, "shovel" );					//SHOVEL
		materialHarvestTool.put( Material.EARTH, "shovel" );
		materialHarvestTool.put( Material.GOURD, "shovel" );
		materialHarvestTool.put( Material.SAND, "shovel" );
		materialHarvestTool.put( Material.SNOW, "shovel" );
		materialHarvestTool.put( Material.SEA_GRASS, "shovel" );
		materialHarvestTool.put( Material.SNOW_BLOCK, "shovel" );
		materialHarvestTool.put( Material.ORGANIC, "shovel" );

		materialHarvestTool.put( Material.PLANTS, "hoe" );					//HOE
		materialHarvestTool.put( Material.OCEAN_PLANT, "hoe" );
		materialHarvestTool.put( Material.CACTUS, "hoe" );
		materialHarvestTool.put( Material.CORAL, "hoe" );
		materialHarvestTool.put( Material.TALL_PLANTS, "hoe" );
////////////////////////////////////TOOL_ITEM//////////////////////////////////////////////////////
		toolItems.put( Items.DIAMOND_HELMET.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_CHESTPLATE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_LEGGINGS.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_BOOTS.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_AXE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_HOE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_PICKAXE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_SHOVEL.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.put( Items.DIAMOND_SWORD.getRegistryName(), new ItemStack( Items.DIAMOND ) );

		toolItems.put( Items.GOLDEN_HELMET.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_CHESTPLATE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_LEGGINGS.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_BOOTS.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_AXE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_HOE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_PICKAXE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_SHOVEL.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.put( Items.GOLDEN_SWORD.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );

		toolItems.put( Items.IRON_HELMET.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_CHESTPLATE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_LEGGINGS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_BOOTS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_AXE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_HOE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_PICKAXE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_SHOVEL.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.IRON_SWORD.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );

		toolItems.put( Items.LEATHER_HELMET.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.put( Items.LEATHER_CHESTPLATE.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.put( Items.LEATHER_LEGGINGS.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.put( Items.LEATHER_BOOTS.getRegistryName(), new ItemStack( Items.LEATHER ) );

		toolItems.put( Items.STONE_AXE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.put( Items.STONE_HOE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.put( Items.STONE_PICKAXE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.put( Items.STONE_SHOVEL.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.put( Items.STONE_SWORD.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );

		toolItems.put( Items.WOODEN_AXE.getRegistryName(), new ItemStack( Blocks.OAK_PLANKS ) );
		toolItems.put( Items.WOODEN_HOE.getRegistryName(), new ItemStack( Blocks.OAK_PLANKS ) );
		toolItems.put( Items.WOODEN_PICKAXE.getRegistryName(), new ItemStack( Blocks.OAK_PLANKS ) );
		toolItems.put( Items.WOODEN_SHOVEL.getRegistryName(), new ItemStack( Blocks.OAK_PLANKS ) );
		toolItems.put( Items.WOODEN_SWORD.getRegistryName(), new ItemStack( Blocks.OAK_PLANKS ) );

		toolItems.put( Items.SHEARS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.put( Items.BOW.getRegistryName(), new ItemStack( Items.STRING ) );
		toolItems.put( Items.FISHING_ROD.getRegistryName(), new ItemStack( Items.STRING ) );
		toolItems.put( Items.ELYTRA.getRegistryName(), new ItemStack( Items.LEATHER ) );
////////////////////////////////////TOOL_ITEM_AMOUNT///////////////////////////////////////////////
		toolItemAmount.put( Items.DIAMOND_HELMET.getRegistryName(), 5 );
		toolItemAmount.put( Items.DIAMOND_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.put( Items.DIAMOND_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.put( Items.DIAMOND_BOOTS.getRegistryName(), 4 );
		toolItemAmount.put( Items.DIAMOND_AXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.DIAMOND_HOE.getRegistryName(), 2 );
		toolItemAmount.put( Items.DIAMOND_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.DIAMOND_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.put( Items.DIAMOND_SWORD.getRegistryName(), 2 );

		toolItemAmount.put( Items.GOLDEN_HELMET.getRegistryName(), 5 );
		toolItemAmount.put( Items.GOLDEN_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.put( Items.GOLDEN_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.put( Items.GOLDEN_BOOTS.getRegistryName(), 4 );
		toolItemAmount.put( Items.GOLDEN_AXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.GOLDEN_HOE.getRegistryName(), 2 );
		toolItemAmount.put( Items.GOLDEN_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.GOLDEN_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.put( Items.GOLDEN_SWORD.getRegistryName(), 2 );

		toolItemAmount.put( Items.IRON_HELMET.getRegistryName(), 5 );
		toolItemAmount.put( Items.IRON_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.put( Items.IRON_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.put( Items.IRON_BOOTS.getRegistryName(), 4 );
		toolItemAmount.put( Items.IRON_AXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.IRON_HOE.getRegistryName(), 2 );
		toolItemAmount.put( Items.IRON_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.IRON_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.put( Items.IRON_SWORD.getRegistryName(), 2 );

		toolItemAmount.put( Items.LEATHER_HELMET.getRegistryName(), 5 );
		toolItemAmount.put( Items.LEATHER_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.put( Items.LEATHER_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.put( Items.LEATHER_BOOTS.getRegistryName(), 4 );

		toolItemAmount.put( Items.STONE_AXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.STONE_HOE.getRegistryName(), 2 );
		toolItemAmount.put( Items.STONE_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.STONE_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.put( Items.STONE_SWORD.getRegistryName(), 2 );

		toolItemAmount.put( Items.WOODEN_AXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.WOODEN_HOE.getRegistryName(), 2 );
		toolItemAmount.put( Items.WOODEN_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.put( Items.WOODEN_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.put( Items.WOODEN_SWORD.getRegistryName(), 2 );

		toolItemAmount.put( Items.SHEARS.getRegistryName(), 2 );
		toolItemAmount.put( Items.BOW.getRegistryName(), 3 );
		toolItemAmount.put( Items.FISHING_ROD.getRegistryName(), 2 );
		toolItemAmount.put( Items.ELYTRA.getRegistryName(), 7 );
////////////////////////////////////SALVAGE_BASE_VALUE/////////////////////////////////////////////
		salvageBaseValue.put( Blocks.COBBLESTONE.getRegistryName(), 50.0f );
		salvageBaseValue.put( Blocks.OAK_PLANKS.getRegistryName(), 70.0f );
		salvageBaseValue.put( Items.STRING.getRegistryName(), 65.0f );
		salvageBaseValue.put( Items.IRON_INGOT.getRegistryName(), 20.0f );
		salvageBaseValue.put( Items.GOLD_INGOT.getRegistryName(), 35.0f );
		salvageBaseValue.put( Items.DIAMOND.getRegistryName(), 0.0f );
		salvageBaseValue.put( Items.LEATHER.getRegistryName(), 60.0f );
////////////////////////////////////SALVAGE_VALUE_PER_LEVEL////////////////////////////////////////
		salvageValuePerLevel.put( Blocks.COBBLESTONE.getRegistryName(), 2.0f );	// 20
		salvageValuePerLevel.put( Blocks.OAK_PLANKS.getRegistryName(), 0.8f );  // 25
		salvageValuePerLevel.put( Items.STRING.getRegistryName(), 1.0f );		// 30
		salvageValuePerLevel.put( Items.IRON_INGOT.getRegistryName(), 0.8f );	//100
		salvageValuePerLevel.put( Items.GOLD_INGOT.getRegistryName(), 1.6f );	// 50
		salvageValuePerLevel.put( Items.DIAMOND.getRegistryName(), 0.666f );	//150
		salvageValuePerLevel.put( Items.LEATHER.getRegistryName(), 1.0f );		// 30
////////////////////////////////////REPAIR_XP//////////////////////////////////////////////////////
		repairXp.put( Items.IRON_INGOT.getRegistryName(), 35.0f );
		repairXp.put( Items.GOLD_INGOT.getRegistryName(), 666.0f );
		repairXp.put( Items.DIAMOND.getRegistryName(), 75.0f );
		repairXp.put( Items.STRING.getRegistryName(), 20.0f );
		repairXp.put( Items.LEATHER.getRegistryName(), 40.0f );
////////////////////////////////////SALVAGE_XP/////////////////////////////////////////////////////
		salvageXp.put( Items.IRON_INGOT.getRegistryName(), 20.0f );
		salvageXp.put( Items.GOLD_INGOT.getRegistryName(), 25.0f );
		salvageXp.put( Items.DIAMOND.getRegistryName(), 50.0f );
		salvageXp.put( Items.STRING.getRegistryName(), 2.0f );
		salvageXp.put( Items.LEATHER.getRegistryName(), 5.0f );
	}

	private static Skill getSkill( String tool )
	{
		if( tool == null )
			return Skill.INVALID_SKILL;

		switch( tool )
		{
			case "pickaxe":
				return Skill.MINING;

			case "shovel":
				return Skill.EXCAVATION;

			case "axe":
				return Skill.WOODCUTTING;

			case "hoe":
				return Skill.FARMING;

			default:
				return Skill.INVALID_SKILL;
		}
	}

	private static float getXp( ResourceLocation registryName )
	{
		if( xpValues.get( registryName ) != null )
			return xpValues.get( registryName );
		else
			return 0.0f;
	}

	private static float getOreDoubleChance( ResourceLocation registryName )
	{
		if( oreDoubleValues.get( registryName ) != null )
			return oreDoubleValues.get( registryName );
		else
			return 0.0f;
	}

	private static float getPlantDoubleChance( ResourceLocation registryName )
	{
		if( plantDoubleValues.get( registryName ) != null )
			return plantDoubleValues.get( registryName );
		else
			return 0.0f;
	}

	private static boolean getNoDropOre( ResourceLocation registryName )
	{
		if( noDropOres.get( registryName ) != null )
			return true;
		else
			return false;
	}

	public static Integer getSkillColor( String skill )
	{
		if( skillColors.get( skill ) != null )
			return skillColors.get( skill );
		else
            return 0xffffff;
	}

	private static String correctHarvestTool( Material material )
	{
		if( materialHarvestTool.get( material ) != null )
			return materialHarvestTool.get( material );
		else
			return "none";
	}

	private static ItemStack getToolItem( ResourceLocation name )
	{
		if( toolItems.get( name ) != null )
			return toolItems.get( name );
		else
			return null;
	}

	private static Integer getToolItemAmount( ResourceLocation name )
	{
		if( toolItemAmount.get( name ) != null )
			return toolItemAmount.get( name );
		else
			return 0;
	}

	private static float getSalvageBaseValue( ResourceLocation registryName )
	{
		if( salvageBaseValue.get( registryName ) != null )
			return salvageBaseValue.get( registryName );
		else
			return 0.0f;
	}

	private static float getSalvageValuePerLevel( ResourceLocation registryName )
	{
		if( salvageValuePerLevel.get( registryName ) != null )
			return salvageValuePerLevel.get( registryName );
		else
			return 0.0f;
	}

	private static float getRepairXp( ResourceLocation registryName )
	{
		if( repairXp.get( registryName ) != null )
			return repairXp.get( registryName );
		else
			return 0.0f;
	}

	private static float getSalvageXp( ResourceLocation registryName )
	{
		if( salvageXp.get( registryName ) != null )
			return salvageXp.get( registryName );
		else
			return 0.0f;
	}

//	private static boolean isPlant( ResourceLocation block )
//	{
//		if( plantItems.get( block ) != null )
//			return true;
//		else
//			return false;
//	}

	public static void handlePlaced( BlockEvent.EntityPlaceEvent event )
	{
		if( event.getEntity() instanceof PlayerEntity )
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if ( !player.isCreative() )
			{
				double blockHardnessLimit = Config.config.blockHardnessLimit.get();
				Block block = event.getPlacedBlock().getBlock();
				float blockHardness = block.getBlockHardness(block.getDefaultState(), event.getWorld(), event.getPos());
				if ( blockHardness > blockHardnessLimit )
					blockHardness = (float) blockHardnessLimit;
				String playerName = player.getName().toString();
				BlockPos blockPos = event.getPos();

				if (!lastPosPlaced.containsKey(playerName) || !lastPosPlaced.get(playerName).equals(blockPos))
				{
					if (block.equals(Blocks.FARMLAND))
						awardXp(player, Skill.FARMING, "tilting dirt", blockHardness, false);
					else
						{
//							for( int i = 0; i < 1000; i++ )
//						{
							awardXp(player, Skill.BUILDING, "placing a block", blockHardness, false);
//						}
					}
				}

				if (lastPosPlaced.containsKey(playerName))
					lastPosPlaced.replace(playerName, event.getPos());
				else
					lastPosPlaced.put(playerName, blockPos);

//				int newBuildLevel = levelAtXp( skillsTag.getFloat( "building" ) );

//				if( player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE ).getBaseValue() < ( 4.09D + ( buildLevel / 25D ) ) )
//				player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE ).setBaseValue( 4.09D + ( buildLevel / 25D ) );
//
				if (getXp(block.getRegistryName()) != 0)
					PlacedBlocks.orePlaced(event.getWorld().getWorld(), event.getPos());
			}
		}
	}

	public static void handleBroken( BreakEvent event )
	{
		if( event.getPlayer() instanceof PlayerEntity )
		{
			PlayerEntity player = event.getPlayer();
			if( !player.isCreative() )
			{
				double blockHardnessLimit = Config.config.blockHardnessLimit.get();
				CompoundNBT skillsTag = getSkillsTag( player );
				Block block = event.getState().getBlock();
				Material material = event.getState().getMaterial();
				World world = event.getWorld().getWorld();
				ItemStack toolUsed = player.getHeldItemMainhand();
//				Skill skill = getSkill( correctHarvestTool( material ) );
				float hardness = block.getBlockHardness( block.getDefaultState(), event.getWorld(), event.getPos() );
				if( hardness > blockHardnessLimit )
					hardness = (float) blockHardnessLimit;

				float award = hardness;

				Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
				int fortune = 0;
				if( enchants.get( Enchantments.FORTUNE ) != null )
					fortune = enchants.get( Enchantments.FORTUNE );

				LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
						.withRandom(world.rand)
						.withParameter( LootParameters.POSITION, event.getPos() )
						.withParameter( LootParameters.TOOL, toolUsed )
						.withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
				if (fortune > 0)
				{
					builder.withLuck(fortune);
				}
				List<ItemStack> drops = block.getDrops( event.getState(), builder );

//				System.out.println( drops );

				boolean wasPlaced = PlacedBlocks.isPlayerPlaced( event.getWorld().getWorld(), event.getPos() );
				Block sugarCane = Blocks.SUGAR_CANE;
				Block cactus = Blocks.CACTUS;
				Block kelp = Blocks.KELP_PLANT;

				if( block.equals( sugarCane ) || block.equals( cactus ) || block.equals( kelp ) ) //Handle Sugar Cane / Cactus
				{
					int currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
					Block baseBlock = event.getState().getBlock();
					BlockPos baseBlockPos = event.getPos();

					float extraChance = getPlantDoubleChance( baseBlock.getRegistryName() ) * currLevel;
					int rewardable, guaranteedDrop, extraDrop, totalDrops;
					rewardable = guaranteedDrop = extraDrop = totalDrops = 0;

					if( ( extraChance / 100 ) > 1 )
					{
						guaranteedDrop = (int)Math.floor( extraChance / 100 );
						extraChance = (float)( ( extraChance / 100 ) - Math.floor( extraChance / 100 ) ) * 100;
					}
					if( !wasPlaced )
						rewardable++;

					int height = 1;
					BlockPos currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
					block =  world.getBlockState( currBlockPos ).getBlock();
					for( ; ( block.equals( baseBlock ) ); )
					{
						wasPlaced = PlacedBlocks.isPlayerPlaced( world, currBlockPos );
						if( !wasPlaced )
						{
							rewardable++;

							if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
								extraDrop++;
						}
						height++;
						currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
						block =  world.getBlockState( currBlockPos ).getBlock();
					}

					int dropsLeft = guaranteedDrop + extraDrop;
					if( guaranteedDrop + extraDrop > 0 )
					{
						while( dropsLeft > 64 )
						{
							if( baseBlock == Blocks.CACTUS )
								baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Blocks.CACTUS, dropsLeft ) );
							else if( baseBlock == Blocks.SUGAR_CANE )
								baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Items.SUGAR_CANE, dropsLeft ) );
							else
								baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Items.KELP, dropsLeft ) );
							dropsLeft -= 64;
						}

						if( baseBlock == Blocks.CACTUS )
						{
							baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Blocks.CACTUS, dropsLeft ) );
							player.sendStatusMessage( new StringTextComponent( ( guaranteedDrop * height ) + extraDrop + " Extra Cactus Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						else if( baseBlock == Blocks.SUGAR_CANE )
						{
							baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Items.SUGAR_CANE, dropsLeft ) );
							player.sendStatusMessage( new StringTextComponent( ( guaranteedDrop * height ) + extraDrop + " Extra Sugar Cane Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						else
						{
							baseBlock.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), new ItemStack( Items.KELP, dropsLeft ) );
							player.sendStatusMessage( new StringTextComponent( ( guaranteedDrop * height ) + extraDrop + " Extra Kelp Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
					}

					totalDrops = rewardable + guaranteedDrop + extraDrop;
					award += ( getXp( baseBlock.getRegistryName() ) * totalDrops ) + ( hardness * totalDrops );
	//				System.out.println( "Height: " + height );
					awardXp( player, Skill.FARMING, "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra", award, false );
				}
				else if( ( material.equals( Material.PLANTS ) || material.equals( Material.OCEAN_PLANT ) || material.equals( Material.TALL_PLANTS ) ) && drops.size() > 0 ) //IS PLANT
				{
					ItemStack theDropItem = drops.get( 0 );

					BlockState state = event.getState();
					int age = -1;
					int maxAge = -1;
					award += getXp( block.getRegistryName() ) * theDropItem.getCount();

					if( state.has( BlockStateProperties.AGE_0_1 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_1 );
						maxAge = 1;
					}
					else if( state.has( BlockStateProperties.AGE_0_2 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_2 );
						maxAge = 2;
					}
					else if( state.has( BlockStateProperties.AGE_0_3 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_3 );
						maxAge = 3;
					}
					else if( state.has( BlockStateProperties.AGE_0_5 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_5 );
						maxAge = 5;
					}
					else if( state.has( BlockStateProperties.AGE_0_7 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_7 );
						maxAge = 7;
					}
					else if( state.has( BlockStateProperties.AGE_0_15) )
					{
						age = state.get( BlockStateProperties.AGE_0_15 );
						maxAge = 15;
					}
					else if( state.has( BlockStateProperties.AGE_0_25 ) )
					{
						age = state.get( BlockStateProperties.AGE_0_25 );
						maxAge = 25;
					}
					else if( state.has( BlockStateProperties.PICKLES_1_4 ) )
					{
						age = state.get( BlockStateProperties.PICKLES_1_4 );
						maxAge = 4;
						if( wasPlaced )
							return;
					}

					if( age == maxAge && age >= 0 || block instanceof SeaPickleBlock )
					{
						int currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
						float extraChance = getPlantDoubleChance( theDropItem.getItem().getRegistryName() ) * currLevel;
						int guaranteedDrop = 0;
						int extraDrop = 0;

						if( ( extraChance / 100 ) > 1 )
						{
							guaranteedDrop = (int) Math.floor( extraChance / 100 );
							extraChance = (float) ( ( extraChance / 100 ) - Math.floor( extraChance / 100 ) ) * 100;
						}

						if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
							extraDrop = 1;

						drops.add( new ItemStack( theDropItem.getItem(), guaranteedDrop + extraDrop ) );

						if ( guaranteedDrop + extraDrop > 0 )
							player.sendStatusMessage( new StringTextComponent( Integer.toString( guaranteedDrop + extraDrop ) + " Extra " + theDropItem.getDisplayName().getFormattedText() + " Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );

//						System.out.println( theDropItem );
//						System.out.println( guaranteedDrop );
//						System.out.println( extraDrop );

						award += getXp( block.getRegistryName() ) * ( theDropItem.getCount() + guaranteedDrop + extraDrop );
						awardXp( player, Skill.FARMING, "harvesting " + ( theDropItem.getCount() ) + " + " + ( guaranteedDrop + extraDrop ) + " crops", award, false );
					}

					awardXp( player, Skill.FARMING, "breaking a plant", award * drops.get(0).getCount(), false );
				}
				else if( getOreDoubleChance( block.getRegistryName() ) != 0.0f )		//IS ORE
				{
//					System.out.println( "IS ORE" );
					boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;
					boolean noDropOre = getNoDropOre( block.getRegistryName() );

					if( !wasPlaced && !isSilk )
						award += getXp( block.getRegistryName() ) * drops.get( 0 ).getCount();

					if( noDropOre && !wasPlaced || !noDropOre && !isSilk )			//EXTRA DROPS
					{
						int currLevel = levelAtXp( skillsTag.getFloat( "mining" ) );
						float extraChance = getOreDoubleChance( block.getRegistryName() ) * currLevel;
						//					float extraChance = 180.0f;

						int guaranteedDrop = 0;
						int extraDrop = 0;

						if( ( extraChance / 100 ) > 1 )
						{
							guaranteedDrop = (int)Math.floor( extraChance / 100 );
							extraChance = (float)( ( extraChance / 100 ) - Math.floor( extraChance / 100 ) ) * 100;
						}

						if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
							extraDrop = 1;

						if( !noDropOre && wasPlaced )
							award += getXp( block.getRegistryName() ) * ( drops.get( 0 ).getCount() );

						String awardMessage = "mining a block";

						if( guaranteedDrop + extraDrop > 0 )
						{
							award += getXp( block.getRegistryName() ) * ( guaranteedDrop + extraDrop );
							ItemStack theDrop = new ItemStack( drops.get( 0 ).getItem(), guaranteedDrop + extraDrop );
							block.spawnAsEntity( event.getWorld().getWorld(), event.getPos(), theDrop );
							awardMessage = "mining  and " + ( guaranteedDrop + extraDrop ) + " extra drop";
							player.sendStatusMessage( new StringTextComponent( ( guaranteedDrop + extraDrop ) + " Extra " + theDrop.getDisplayName().getFormattedText() + " Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						awardXp( player, Skill.MINING, awardMessage, award, false );

					}
					else
						awardXp( player, Skill.MINING, "mining a block", award, false );
				}
				else
				{
					switch( getSkill( correctHarvestTool( material ) ) )
					{
						case MINING:
							awardXp( player, Skill.MINING, "mining a block", award, false );
							break;

						case WOODCUTTING:
							awardXp( player, Skill.WOODCUTTING, "cutting a block", award, false );
							break;

						case EXCAVATION:
							awardXp( player, Skill.EXCAVATION, "digging a block", award, false );
							break;

						case FARMING:
							awardXp( player, Skill.FARMING, "harvesting", award, false );
							break;

						default:
//							System.out.println( "INVALID SKILL ON BREAK" );
							break;
					}
				}
			}
		}
	}

	public static void handleSmelted( PlayerEvent.ItemSmeltedEvent event )
	{
		System.out.println( "SMELTED" );
	}

	public static void handleDamage( LivingDamageEvent event )
	{
		float damage = event.getAmount();
		float startDmg = damage;
		LivingEntity target = event.getEntityLiving();
		if( target instanceof PlayerEntity )
		{
			PlayerEntity player = (PlayerEntity) target;
			CompoundNBT skillsTag = getSkillsTag( player );
			float agilityXp = 0;
			float enduranceXp = 0;
			boolean hideEndurance = false;

///////////////////////////////////////////////////////////////////////ENDURANCE//////////////////////////////////////////////////////////////////////////////////////////
			int enduranceLevel = levelAtXp( skillsTag.getFloat( "endurance" ) );
			double endurancePerLevel = Config.config.endurancePerLevel.get();
			double maxEndurance = Config.config.maxEndurance.get();
			double endurePercent = (enduranceLevel * endurancePerLevel);
			if( endurePercent > maxEndurance )
				endurePercent = maxEndurance;
			endurePercent /= 100;

			float endured = damage * (float) endurePercent;
			if( endured < 0 )
				endured = 0;

			damage -= endured;

			enduranceXp = ( damage * 5 ) + ( endured * 10 );
///////////////////////////////////////////////////////////////////////FALL//////////////////////////////////////////////////////////////////////////////////////////////
			if( event.getSource().getDamageType().equals( "fall" ) )
			{
				float award = startDmg;
//				float savedExtra = 0;
				int agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
				int saved = 0;

				double maxFallSaveChance = Config.config.maxFallSaveChance.get();
				double saveChancePerLevel = Config.config.saveChancePerLevel.get() / 100;

				double chance = agilityLevel * saveChancePerLevel;
				if( chance > maxFallSaveChance )
					chance = maxFallSaveChance;
				for( int i = 0; i < damage; i++ )
				{
					if( Math.ceil( Math.random() * 100 ) <= chance )
					{
						saved++;
					}
				}
				damage -= saved;

				if( saved != 0 && player.getHealth() > damage )
					player.sendStatusMessage( new StringTextComponent( "Saved " + (int) saved + " damage!" ), true );

				award = saved * 30;

				agilityXp = award;
			}

			event.setAmount( damage );

			if( player.getHealth() > damage )
			{
				if( agilityXp > 0 )
					hideEndurance = true;

				if( event.getSource().getTrueSource() != null )
					awardXp( player, Skill.ENDURANCE, event.getSource().getTrueSource().getDisplayName().getFormattedText(), enduranceXp, hideEndurance );
				else
					awardXp( player, Skill.ENDURANCE, event.getSource().getDamageType(), enduranceXp, hideEndurance );

				if( agilityXp > 0 )
					awardXp( player, Skill.AGILITY, "surviving " + startDmg + " fall damage", agilityXp, false );
			}
		}

		if ( target instanceof LivingEntity && event.getSource().getTrueSource() instanceof PlayerEntity )
		{
			PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
			if( !player.isCreative() )
			{
				float amount = 0;
				float playerHealth = player.getHealth();
				float targetHealth = target.getHealth();
				float targetMaxHealth = target.getMaxHealth();
				float lowHpBonus = 1.0f;

				if( damage > targetHealth )		//no overkill xp
					damage = targetHealth;

				amount += damage * 3;

				if ( startDmg >= targetHealth )	//kill reduce xp
					amount /= 2;

				if( startDmg >= targetMaxHealth )	//max hp kill reduce xp
					amount /= 1.5;

//				player.setHealth( 1f );

				if( target instanceof AnimalEntity )		//reduce xp if passive mob
					amount /= 2;
				else if( playerHealth <= 10 )				//if aggresive mob and low hp
				{
					lowHpBonus += ( 11 - playerHealth ) / 5;
					if( playerHealth <= 2 )
						lowHpBonus += 1;
				}

				damage = event.getAmount();
				if( event.getSource().damageType.equals( "arrow" ) )
				{
					double distance = event.getEntity().getDistance( player );
					if( distance > 16 )
						distance -= 16;
					else
						distance = 0;

					amount += (float) ( Math.pow( distance, 1.25 ) * ( damage / target.getMaxHealth() ) * ( startDmg >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp

					amount *= lowHpBonus;
					awardXp( player, Skill.ARCHERY, player.getHeldItemMainhand().getDisplayName().toString(), amount, false );
				}
				else
				{
					amount *= lowHpBonus;
					awardXp( player, Skill.COMBAT, player.getHeldItemMainhand().getDisplayName().toString(), amount, false );
				}
			}
		}
	}

	public static void handleJump( LivingJumpEvent event )
	{
		if( event.getEntityLiving() instanceof PlayerEntity )
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();

//			if( !player.world.isRemote )
//				System.out.println( player.getPersistentData() );

			if( !player.isCreative() )
			{
				CompoundNBT skillsTag = getSkillsTag(player);

				double agilityLevel = 1;
				double jumpBoost = 0;

				if (player.world.isRemote)
				{
					if (XPOverlayGUI.skills.get("agility") == null)
						agilityLevel = 1;
					else
						agilityLevel = levelAtXp( XPOverlayGUI.skills.get("agility").xp );
				} else
					agilityLevel = levelAtXp(skillsTag.getFloat("agility"));

				if (player.isCrouching())
					jumpBoost = -0.011 + agilityLevel * (0.14 / 33);

				if (player.isSprinting())
					jumpBoost = -0.013 + agilityLevel * (0.14 / 50);

				if (jumpBoost > 0.33)
					jumpBoost = 0.33;

				if (player.world.isRemote)
				{
					if( jumpBoost > 0 )
						player.setMotion( player.getMotion().x, player.getMotion().y + jumpBoost, player.getMotion().z );
				}
				else if (!player.isInWater())
					awardXp( player, Skill.AGILITY, "jumping", (float) (jumpBoost * 10 + 1), true );
			}
		}
	}

//	public static void handleLivingDeath( LivingDeathEvent event )
//	{
//	}

	public static void handlePlayerRespawn( PlayerEvent.PlayerRespawnEvent event )
	{
		PlayerEntity player = event.getPlayer();

		AttributeHandler.updateReach( player );
		AttributeHandler.updateHP( player );
		AttributeHandler.updateDamage( player );
	}

	public static void handleClone( PlayerEvent.Clone event )
	{
		event.getPlayer().getPersistentData().put( "pmmo", event.getOriginal().getPersistentData().getCompound( "pmmo" ) );
	}

	public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
	{
		PlayerEntity player = event.getPlayer();
		CompoundNBT skillsTag = getSkillsTag( player );
		Set<String> keySet = skillsTag.keySet();

		AttributeHandler.updateReach( player );
		NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0f, true ), (ServerPlayerEntity) player );

		for( String tag : keySet )
		{
			if( Skill.getInt( tag ) == 0 )
			{
				if( XP.validSkills.contains( tag.toLowerCase() ) )
					skillsTag.put( tag.toLowerCase(), skillsTag.get(tag) );

				System.out.println( "REMOVING INVALID SKILL " + tag );
				skillsTag.remove( tag );
			}
			else
				NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getFloat( tag ), Skill.getInt( tag ), 0, true ), (ServerPlayerEntity) player );
		}
	}

	public static void handleRightClickItem( RightClickItem event )
	{
	}

	public static void handleRightClickBlock( RightClickBlock event )
	{
		if( !event.getWorld().isRemote )
		{
			PlayerEntity player = event.getPlayer();
			CompoundNBT skillsTag = getSkillsTag( player );
			ItemStack itemStack = event.getItemStack();
			Item item = itemStack.getItem();
			String itemDisplayName = itemStack.getDisplayName().getFormattedText();
			Block block = event.getWorld().getBlockState( event.getPos() ).getBlock();
			Block anvil 		=	Blocks.ANVIL;
			Block ironBlock		= 	Blocks.IRON_BLOCK;
			Block goldBlock 	= 	Blocks.GOLD_BLOCK;
			Block diamondBlock 	= 	Blocks.DIAMOND_BLOCK;
			if( player.isCrouching() )
			{
				if( block.equals( ironBlock ) || block.equals( anvil ) )
				{
					if( event.getHand() == Hand.MAIN_HAND )
					{
						int currLevel;
						float baseChance = 0;
						float extraChance = 0;

						if( getPlantDoubleChance( item.getRegistryName() ) != 0 )
						{
							currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
							baseChance = getPlantDoubleChance( item.getRegistryName() );
							extraChance = baseChance* currLevel;
						}
						else if( getOreDoubleChance( item.getRegistryName() ) != 0 )
						{
							currLevel = levelAtXp( skillsTag.getFloat( "mining" ) );
							baseChance = getOreDoubleChance( item.getRegistryName() );
							extraChance = baseChance * currLevel;
						}

						if( extraChance != 0 )
							player.sendStatusMessage( new StringTextComponent( itemStack.getDisplayName() + " " + DP.dp( extraChance ) + "% extra chance, " + DP.dp( baseChance ) + "% per level" ), false );
					}
					else
						return;
				}

				if( block.equals( goldBlock ) || block.equals( anvil ) )
				{
					if( item.isDamageable() )
					{
						if( getToolItem( item.getRegistryName() ) != null )
						{
							Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( itemStack );
							ItemStack salvageItemStack = getToolItem( item.getRegistryName() );
							Item salvageItem = salvageItemStack.getItem();
							ServerWorld serverWorld = (ServerWorld) event.getWorld();
							int currLevel = levelAtXp( skillsTag.getFloat( "repairing" ) );
							float baseValue = getSalvageBaseValue( salvageItem.getRegistryName() );
							float valuePerLevel = getSalvageValuePerLevel( salvageItem.getRegistryName() );
							float chance = baseValue + ( valuePerLevel * currLevel );
							if( chance > 80 )
								chance = 80;

							float enchantChance = currLevel * 0.9f;
							if( enchantChance > 90 )
								enchantChance = 90;

							int itemPotential = getToolItemAmount( item.getRegistryName() );

							float startDmg = itemStack.getDamage();
							float maxDmg = itemStack.getMaxDamage();
							float award = 0;
							float displayDurabilityPercent = ( 1.00f - ( startDmg / maxDmg ) ) * 100;
							float durabilityPercent = ( 1.00f - ( startDmg / maxDmg ) );
							int potentialReturnAmount = (int) Math.floor( itemPotential * durabilityPercent );

							if( event.getHand() == Hand.OFF_HAND )
							{
								if( !player.isCreative() )
								{
									int returnAmount = 0;

									for( int i = 0; i < potentialReturnAmount; i++ )
									{
										if( Math.ceil( Math.random() * 100 ) <= chance )
											returnAmount++;
									}
									award += getSalvageXp( salvageItem.getRegistryName() ) * returnAmount;

									String replyMsg = "Salvaged " + returnAmount + "/" + itemPotential + " " + new ItemStack( salvageItem ).getDisplayName().getFormattedText();
									replyMsg += ( returnAmount > 1 && !salvageItem.equals( Items.STRING ) && !salvageItem.equals( Items.LEATHER ) && !( salvageItem instanceof BlockItem ) ? "s" : "" ) + "!";
									BlockPos spawnPos = event.getPos();
									if( returnAmount > 0 )
										block.spawnAsEntity( event.getWorld(), event.getPos(), new ItemStack( salvageItem, returnAmount ) );

									if( award > 0 )
										awardXp( player, Skill.REPAIRING, "salvaging " + returnAmount + "/" + itemPotential + " from an item", award, false  );

									player.sendStatusMessage( new StringTextComponent( replyMsg ), true );

									if( enchants.size() > 0 )
									{
										ItemStack salvagedBook = new ItemStack( Items.KNOWLEDGE_BOOK );
										Set<Enchantment> enchantKeys = enchants.keySet();
										int enchantLevel;
										boolean fullEnchants = true;

										for( Enchantment enchant : enchantKeys )
										{
											enchantLevel = 0;
											for( int i = 1; i <= enchants.get( enchant ); i++ )
											{
												if( Math.floor( Math.random() * 100 ) < enchantChance )
													enchantLevel = i;
												else
												{
													fullEnchants = false;
													i = enchants.get( enchant ) + 1;
												}
											}
											if( enchantLevel > 0 )
												salvagedBook.addEnchantment( enchant, enchantLevel );
										}
										if( salvagedBook.isEnchanted() )
										{
											block.spawnAsEntity( event.getWorld(), event.getPos(), salvagedBook );
											if( fullEnchants )
												player.sendStatusMessage( new StringTextComponent( "You managed to save all enchants!" ), true );
											else
												player.sendStatusMessage( new StringTextComponent( "You managed to save some enchants!" ), true );
										}
									}
									player.inventory.offHandInventory.set( 0, new ItemStack( Items.AIR, 0 ) );
									player.sendBreakAnimation(Hand.OFF_HAND );
								}
								else
									player.sendStatusMessage( new StringTextComponent( "Only available in Survival Mode!" ), true );
							}
							else
							{
//								itemStack.damageItem( 750, player, (a) -> a.sendBreakAnimation(Hand.OFF_HAND ) );
								player.sendStatusMessage( new StringTextComponent( "Off-Hand to Disassemble!" ), true );
								player.sendStatusMessage( new StringTextComponent( "_________________________________" ), false );
								player.sendStatusMessage( new StringTextComponent( itemDisplayName + " " + DP.dp( displayDurabilityPercent ) + "%" ), false );
								player.sendStatusMessage( new StringTextComponent( DP.dp( chance ) + "% per material, " + potentialReturnAmount + " max items returned" ), false );
								player.sendStatusMessage( new StringTextComponent( DP.dp( enchantChance ) + "% enchant return, " + itemStack.getRepairCost() + " repair cost" ), false );
							}
						}
						else
						{
							player.sendStatusMessage( new StringTextComponent( "UNACCOUNTED FOR DAMAGEABLE ITEM! Please Report!" ), false );
						}
					}
				}

				if( block.equals( diamondBlock ) && event.getHand() == Hand.MAIN_HAND )
				{
					int agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
					int enduranceLevel = levelAtXp( skillsTag.getFloat( "endurance" ) );
					int combatLevel = levelAtXp( skillsTag.getFloat( "combat" ) );
					int swimLevel = levelAtXp( skillsTag.getFloat( "swimming" ) );

					double maxFallSaveChance = Config.config.maxFallSaveChance.get();
					double saveChancePerLevel = Config.config.saveChancePerLevel.get() / 100;

					double reach = AttributeHandler.getReach( player );
					double agilityChance = agilityLevel * saveChancePerLevel;
					double damageReduction = enduranceLevel * 0.25f;
					double extraDamage = Math.floor( combatLevel / 20 );

					float speedPercent = agilityLevel / 2000f;

					if( agilityChance > maxFallSaveChance )
						agilityChance = maxFallSaveChance;

					if( damageReduction > 50 )
						damageReduction = 50;

					if( speedPercent > 0.10f )
						speedPercent = 0.10f;
					speedPercent = speedPercent / 0.10f * 100f;

					player.sendStatusMessage( new StringTextComponent( "_________________________________" ), false );
					player.sendStatusMessage( new StringTextComponent( DP.dp( reach ) + " player reach" ), false );
					player.sendStatusMessage( new StringTextComponent( DP.dp( agilityChance ) + "% fall damage save chance" ), false );
					player.sendStatusMessage( new StringTextComponent( DP.dp( damageReduction ) + "% damage reduction" ), false );
					player.sendStatusMessage( new StringTextComponent( DP.dp( extraDamage ) + " extra damage" ), false );
					player.sendStatusMessage( new StringTextComponent( DP.dp( speedPercent ) + "% sprint speed boost" ), false );

					if( swimLevel >= 25 )
						player.sendStatusMessage( new StringTextComponent( "Underwater night vision is unlocked!" ), false );
					else
						player.sendStatusMessage( new StringTextComponent( "Underwater night vision is locked until level 25 Swimming" ), false );
				}
			}
		}
	}

	public static void handleAnvilRepair( AnvilRepairEvent event )
	{
		PlayerEntity player = event.getPlayer();
		if( !player.world.isRemote && event.getItemInput().getItem().isDamageable() )
		{
			CompoundNBT skillsTag = getSkillsTag( player );
			double anvilCostReductionPerLevel = Config.config.anvilCostReductionPerLevel.get();
			double extraChanceToNotBreakAnvilPerLevel = Config.config.extraChanceToNotBreakAnvilPerLevel.get() / 100;
			double anvilFinalItemBonusRepaired = Config.config.anvilFinalItemBonusRepaired.get() / 100;
			int anvilFinalItemMaxCostToAnvil = Config.config.anvilFinalItemMaxCostToAnvil.get();

			int currLevel = levelAtXp( skillsTag.getFloat( "repairing" ) );
			float bonusRepair = (float) anvilFinalItemBonusRepaired * currLevel;
			int maxCost = (int) Math.floor( 50 - ( currLevel * anvilCostReductionPerLevel ) );
			if( maxCost < anvilFinalItemMaxCostToAnvil )
				maxCost = anvilFinalItemMaxCostToAnvil;

			event.setBreakChance( event.getBreakChance() / ( 1f + (float) extraChanceToNotBreakAnvilPerLevel * currLevel ) );

//			ItemStack rItem = event.getIngredientInput();
			ItemStack lItem = event.getItemInput();
			ItemStack oItem = event.getItemResult();

			if( oItem.getRepairCost() > maxCost )
				oItem.setRepairCost( maxCost );

			float repaired = oItem.getDamage() - lItem.getDamage();
			if( repaired < 0 )
				repaired = -repaired;

			oItem.setDamage( (int) Math.floor( oItem.getDamage() - repaired * bonusRepair ) );

			float award = (float) ( ( ( repaired + repaired * bonusRepair * 2.5 ) / 100 ) * ( 1 + lItem.getRepairCost() * 0.025 ) );
			award *= getRepairXp( getToolItem( lItem.getItem().getRegistryName() ).getItem().getRegistryName() );

			if( award > 0 )
			{
				player.sendStatusMessage( new StringTextComponent( "repaired " + (int) repaired + " + " + (int) ( repaired * bonusRepair ) + " extra!" ), true );
				awardXp( player, Skill.REPAIRING, "repairing an item by: " + repaired, award, false );
			}
		}
	}

	public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
	{
		if( !event.getPlayer().world.isRemote )
		{
			awardXp( event.getPlayer(), Skill.CRAFTING, "crafting " + event.getCrafting().getDisplayName(), + 10.0f, false );
		}
	}

	public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
	{
		PlayerEntity player = event.getPlayer();

		CompoundNBT skills = getSkillsTag( player );

		int mining = 1;
		int woodcutting = 1;
		int excavation = 1;

		double miningPercent = Config.config.miningBonusSpeed.get() / 100;
		double woodcuttingPercent = Config.config.woodcuttingBonusSpeed.get() / 100;
		double excavationPercent = Config.config.excavationBonusSpeed.get() / 100;

		if( player.world.isRemote() )
		{
			if( XPOverlayGUI.skills.get( "mining" ) != null )
				mining = levelAtXp( XPOverlayGUI.skills.get( "mining" ).xp );
			if( XPOverlayGUI.skills.get( "woodcutting" ) != null )
				woodcutting = levelAtXp( XPOverlayGUI.skills.get( "woodcutting" ).xp );
			if( XPOverlayGUI.skills.get( "excavation" ) != null )
				excavation = levelAtXp( XPOverlayGUI.skills.get( "excavation" ).xp );
		}
		else
		{
			mining = levelAtXp( skills.getFloat("mining") );
			woodcutting = levelAtXp( skills.getFloat("woodcutting") );
			excavation = levelAtXp( skills.getFloat("excavation") );
		}

		switch ( correctHarvestTool(event.getState().getMaterial()) )
		{
			case "pickaxe":
				float height = event.getPos().getY();
				if (height < 0)
					height = -height;

				double blocksToUnbreakableY = Config.config.blocksToUnbreakableY.get();
				double heightMultiplier = 1 - ( height / blocksToUnbreakableY );

				if ( heightMultiplier < Config.config.minBreakSpeed.get() )
					heightMultiplier = Config.config.minBreakSpeed.get();

				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + mining * (float) miningPercent ) * ( (float) heightMultiplier) );
			break;
			case "axe":
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + woodcutting * (float) woodcuttingPercent ) );
			break;
			case "shovel":
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + excavation * (float) excavationPercent ) );
			break;
			default:
				event.setNewSpeed( event.getOriginalSpeed() );
			break;
		}
	}

	public static CompoundNBT getSkillsTag( PlayerEntity player )
	{
		CompoundNBT persistTag = player.getPersistentData();
		CompoundNBT pmmoTag = null;
		CompoundNBT skillsTag = null;

		if( !persistTag.contains( "pmmo" ) )						//if Player doesn't have pmmo tag, make it
		{
			pmmoTag = new CompoundNBT();
			persistTag.put( "pmmo", pmmoTag );
		}
		else
		{
			pmmoTag = persistTag.getCompound( "pmmo" );	//if Player has skills tag, use it
		}

		if( !pmmoTag.contains( "skills" ) )						//if Player doesn't have skills tag, make it
		{
			skillsTag = new CompoundNBT();
			pmmoTag.put( "skills", skillsTag );
		}
		else
		{
			skillsTag = pmmoTag.getCompound( "skills" );	//if Player has skills tag, use it
		}

		return skillsTag;
	}

	public static void awardXp( PlayerEntity player, Skill skill, String sourceName, float amount, boolean skip )
	{
		if( amount <= 0.0f || player.world.isRemote )
			return;

		if( skill == skill.INVALID_SKILL )
		{
			System.out.println( "INVALID SKILL" );
			return;
		}

		String skillName = skill.name().toLowerCase();
		double skillMultiplier = 1;
		double difficultyMultiplier = 1;


		for( char letter : player.getDisplayName().getFormattedText().toCharArray() )
		{
			if( !( letter >= 'a' && letter <= 'z' ) && !( letter >= 'A' && letter <= 'Z' ) && !( letter >= '0' && letter <= '9' ) && !( letter == '\u00a7' ) && !( letter == '_' ) )
				return;
		}

		if( skillName.equals( "combat" ) || skillName.equals( "archery" ) || skillName.equals( "endurance" ) )
		{
			switch( player.world.getDifficulty() )
			{
			case PEACEFUL:
				difficultyMultiplier = Config.config.peacefulMultiplier.get();
				break;

			case EASY:
				difficultyMultiplier = Config.config.easyMultiplier.get();
				break;

			case NORMAL:
				difficultyMultiplier = Config.config.normalMultiplier.get();
				break;

			case HARD:
				difficultyMultiplier = Config.config.hardMultiplier.get();
				break;

				default:
					break;
			}
		}

		switch( skill )
		{
			case MINING:
				skillMultiplier = Config.config.miningMultiplier.get();
				break;

			case BUILDING:
				skillMultiplier = Config.config.buildingMultiplier.get();
				break;

			case EXCAVATION:
				skillMultiplier = Config.config.excavationMultiplier.get();
				break;

			case WOODCUTTING:
				skillMultiplier = Config.config.woodcuttingMultiplier.get();
				break;

			case FARMING:
				skillMultiplier = Config.config.farmingMultiplier.get();
				break;

			case AGILITY:
				skillMultiplier = Config.config.agilityMultiplier.get();
				break;

			case ENDURANCE:
				skillMultiplier = Config.config.enduranceMultiplier.get();
				break;

			case COMBAT:
				skillMultiplier = Config.config.combatMultiplier.get();
				break;

			case ARCHERY:
				skillMultiplier = Config.config.archeryMultiplier.get();
				break;

			case REPAIRING:
				skillMultiplier = Config.config.repairingMultiplier.get();
				break;

			case FLYING:
				skillMultiplier = Config.config.flyingMultiplier.get();
				break;

			case SWIMMING:
				skillMultiplier = Config.config.swimmingMultiplier.get();
				break;

			case FISHING:
				skillMultiplier = Config.config.fishingMultiplier.get();
				break;

			case CRAFTING:
				skillMultiplier = Config.config.craftingMultiplier.get();
				break;

			default:
				break;
		}

		amount *= skillMultiplier;
		amount *= difficultyMultiplier;
		amount *= globalMultiplier;

		if( amount == 0 )
			return;

		String playerName = player.getDisplayName().getFormattedText();
		int startLevel;
		int currLevel;
		float startXp = 0;

		CompoundNBT skillsTag = getSkillsTag( player );

		if( !skillsTag.contains( skillName ) )
		{
			skillsTag.putFloat( skillName, amount );
			startLevel = 1;
		}
		else
		{
			startLevel = levelAtXp( skillsTag.getFloat( skillName ) );
			startXp = skillsTag.getFloat( skillName );

			if( startXp >= 2000000000 )
				return;

			if( startXp + amount >= 2000000000 )
			{
				player.sendStatusMessage( new StringTextComponent( skillName + " cap of 2b xp reached, you fucking psycho!" ).setStyle( new Style().setColor( TextFormatting.LIGHT_PURPLE ) ), false);
				System.out.println( player.getName() + " " + skillName + " 2b cap reached" );
				amount = 2000000000 - startXp;
			}

			skillsTag.putFloat( skillName, startXp + amount );
		}

		currLevel = levelAtXp( skillsTag.getFloat( skillName ) );

		if( startLevel != currLevel )
		{
			switch( skill )
			{

				case BUILDING:
					AttributeHandler.updateReach( player );
					break;

				case ENDURANCE:
					AttributeHandler.updateHP( player );
					break;

				case COMBAT:
					AttributeHandler.updateDamage( player );
					break;

				default:
					break;
			}
			System.out.println( playerName + " " + currLevel + " " + skillName + " level up!" );
		}

		if( player instanceof ServerPlayerEntity )
			NetworkHandler.sendToPlayer( new MessageXp( startXp, skill.getValue(), amount, skip ), (ServerPlayerEntity) player );

//		persistTag.putString( "lastSkill", skillName );
		if( !skip )
			System.out.println( playerName + " +" + amount + "xp in "  + skillName + " for " + sourceName + " total xp: " + skillsTag.getFloat( skillName ) );

		if( startXp + amount >= maxXp && startXp <= maxXp )
		{
			player.sendStatusMessage( new StringTextComponent( skillName + " max level reached, you psycho!" ).setStyle( new Style().setColor( TextFormatting.LIGHT_PURPLE ) ), false);
			System.out.println( player.getName() + " " + skillName + " max level reached" );
		}
	}

	public static void setXp( PlayerEntity player, String skillName, float newXp )
	{
		skillName = skillName.toLowerCase();
		CompoundNBT skillsTag = getSkillsTag( player );
		skillsTag.putFloat( skillName, newXp );

		switch( skillName )
		{
			case "building":
				AttributeHandler.updateReach( player );
				break;

			case "endurance":
				AttributeHandler.updateHP( player );
				break;

			case "combat":
				AttributeHandler.updateDamage( player );
				break;

			default:

				break;
		}

		NetworkHandler.sendToPlayer( new MessageXp( newXp, Skill.getInt( skillName ), 0, false ), (ServerPlayerEntity) player );
	}

	public static void handlePlayerTick( TickEvent.PlayerTickEvent event )
	{
		PlayerEntity player = event.player;

		if( !player.world.isRemote )
		{
			String name = player.getName().getString();

			if( player.isSprinting() )
				AttributeHandler.updateSpeed( player );
			else
				AttributeHandler.resetSpeed( player );

			if( !lastAward.containsKey( name ) )
				 lastAward.put( name, System.currentTimeMillis() );

			if( !player.isCreative() && player.isAlive() )
			{
				long gap = System.currentTimeMillis() - lastAward.get( name );
				if( gap > 1000 )
				{
					CompoundNBT skillsTag = getSkillsTag( player );

					int swimLevel = levelAtXp( skillsTag.getFloat( "swimming" ) );
					int flyLevel = levelAtXp( skillsTag.getFloat( "flying" ) );
					int agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
					if( agilityLevel > 200 )
						agilityLevel = 200;

					float swimAward = ( 3 + swimLevel    / 10.00f ) * ( gap / 1000f );
					float flyAward  = ( 1 + flyLevel     / 30.77f ) * ( gap / 1000f );
					float runAward  = ( 1 + agilityLevel / 30.77f ) * ( gap / 1000f );

					lastAward.replace( name, System.currentTimeMillis() );
					Block waterBlock = Blocks.WATER;
					BlockPos playerPos = player.getPosition();
					boolean waterBelow = true;


					for( int i = -1; i <= 1; i++ )
					{
						for( int j = -1; j <= 1; j++ )
						{
							if( !player.getEntityWorld().getBlockState( playerPos.down().east( i ).north( j ) ).getBlock().equals( waterBlock ) )
								waterBelow = false;
						}
					}

					boolean waterAbove = player.getEntityWorld().getBlockState( playerPos.up()   ).getBlock().equals( waterBlock );

					if( swimLevel >= 25 && player.isInWater() && waterAbove )
					{
						player.addPotionEffect( new EffectInstance( Effects.NIGHT_VISION, 250, 0, true, false ) );
					}
					else if( player.isPotionActive( Effects.NIGHT_VISION ) && player.getActivePotionEffect( Effects.NIGHT_VISION ).isAmbient() )
						player.removePotionEffect( Effects.NIGHT_VISION );

					if( player.isSprinting() )
					{
						if( player.isInWater() && ( waterAbove || waterBelow ) )
							awardXp( player, Skill.SWIMMING, "swimming fast", swimAward * 1.25f, true );
						else
							awardXp( player, Skill.AGILITY, "running", runAward, true );
					}

					if( player.isInWater() && ( waterAbove || waterBelow ) )
						{
						if( !player.isSprinting() )
							awardXp( player, Skill.SWIMMING, "swimming", swimAward, true );
					}
					else if( player.isElytraFlying() )
						awardXp( player, Skill.FLYING, "flying", flyAward, true );

					if( (player.getRidingEntity() instanceof BoatEntity ) && player.isInWater() )
						awardXp( player, Skill.SWIMMING, "swimming in a boat", swimAward / 5, true );
				}
			}
		}
	}

	public static void handleFished( ItemFishedEvent event )
	{
		PlayerEntity player = event.getPlayer();
		NonNullList<ItemStack> items = event.getDrops();
		float award = getXp( items.get( 0 ).getItem().getRegistryName() );
		if( award == 0 )
			award = 10.0f;
		awardXp( player, Skill.FISHING, "catching " + items, award, false );
	}

	public static int levelAtXp( float xp )
	{
		return levelAtXp( (double) xp );
	}
	public static int levelAtXp( double xp )
	{
		baseXp = Config.config.baseXp.get();
		xpIncreasePerLevel = Config.config.xpIncreasePerLevel.get();

		int theXp = 0;

		for( int level = 0; ; level++ )
		{
			if( xp < theXp || level >= maxLevel )
			{
				return level;
			}
			theXp += baseXp + level * xpIncreasePerLevel;
		}
	}

	public static float levelAtXpDecimal( float xp )
	{
		return (float) levelAtXpDecimal( (double) xp );
	}
	public static double levelAtXpDecimal( double xp )
	{
		if( levelAtXp( xp ) == maxLevel )
			xp = xpAtLevel( maxLevel );
		int startLevel = levelAtXp( xp );
		int startXp = xpAtLevel( startLevel );
		int goalXp = xpAtLevel( startLevel + 1 );

		if( startXp == goalXp )
			return maxLevel;
		else
			return startLevel + ( (xp - startXp) / (goalXp - startXp) );
	}

	public static int xpAtLevel( float givenLevel )
	{
		return xpAtLevel( (double) givenLevel );
	}
	public static int xpAtLevel( double givenLevel )
	{
		baseXp = Config.config.baseXp.get();
		xpIncreasePerLevel = Config.config.xpIncreasePerLevel.get();

		int theXp = 0;
		if( givenLevel > maxLevel )
			givenLevel = maxLevel;

		for( int level = 1; level < givenLevel; level++ )
		{
			theXp += baseXp + (level - 1) * xpIncreasePerLevel;
		}
		return theXp;
	}

	public static float xpAtLevelDecimal( float givenLevel )
	{
		return (float) xpAtLevelDecimal( (double) givenLevel );
	}
	public static double xpAtLevelDecimal( double givenLevel )
	{
		double startXp = xpAtLevel( Math.floor( givenLevel ) );
		double endXp   = xpAtLevel( Math.floor( givenLevel + 1 ) );
		double pos = givenLevel - Math.floor( givenLevel );

		return startXp + ( ( endXp - startXp ) * pos );
	}
}

