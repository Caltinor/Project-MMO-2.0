package harmonised.pmmo.skills;

import java.util.*;

import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.DP;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;

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
	public static Map<String, TextFormatting> skillTextFormat = new HashMap<>();
	private static Map<String, Long> lastAward = new HashMap<>();
	private static Map<String, BlockPos> lastPosPlaced = new HashMap<>();
	public static List<String> validSkills = new ArrayList<String>();
	public static float globalMultiplier = 1;
	public static float maxXp = xpAtLevel( 999 );
	public static Set<String> lapisDonators = new HashSet<>();
	public static Set<String> dandelionDonators = new HashSet<>();
	public static Set<String> ironDonators = new HashSet<>();

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
//		xpValues.setTag( Blocks.GRASS.getRegistryName(), 2.5f );
		xpValues.setTag( Blocks.AIR.getRegistryName(), 0.0f );
		xpValues.setTag( Blocks.DEADBUSH.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.WATERLILY.getRegistryName(), 15.0f );
		xpValues.setTag( Blocks.YELLOW_FLOWER.getRegistryName(), 15.0f );
		xpValues.setTag( Blocks.RED_FLOWER.getRegistryName(), 17.5f );
		xpValues.setTag( Blocks.DOUBLE_PLANT.getRegistryName(), 25.0f );
		xpValues.setTag( Blocks.TALLGRASS.getRegistryName(), 6.0f );
		xpValues.setTag( Blocks.WHEAT.getRegistryName(), 25.0f );
		xpValues.setTag( Blocks.POTATOES.getRegistryName(), 7.5f );
		xpValues.setTag( Blocks.CARROTS.getRegistryName(), 7.5f );
		xpValues.setTag( Blocks.COCOA.getRegistryName(), 30.0f );
		xpValues.setTag( Blocks.BEETROOTS.getRegistryName(), 35.0f );
		xpValues.setTag( Blocks.NETHER_WART.getRegistryName(), 10.0f );
		xpValues.setTag( Blocks.SAPLING.getRegistryName(), 10.0f );
//		xpValues.setTag( Blocks.END_ROD.getRegistryName(), 25.0f );
		xpValues.setTag( Blocks.LEAVES.getRegistryName(), 0.8f );
		xpValues.setTag( Blocks.LEAVES2.getRegistryName(), 0.8f );
		xpValues.setTag( Blocks.PUMPKIN.getRegistryName(), 35.0f );
		xpValues.setTag( Blocks.MELON_BLOCK.getRegistryName(), 7.0f );
		xpValues.setTag( Blocks.BROWN_MUSHROOM_BLOCK.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.BROWN_MUSHROOM.getRegistryName(), 15.0f );
		xpValues.setTag( Blocks.RED_MUSHROOM_BLOCK.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.RED_MUSHROOM.getRegistryName(), 15.0f );
		xpValues.setTag( Blocks.REEDS.getRegistryName(), 10.0f );
		xpValues.setTag( Blocks.CACTUS.getRegistryName(), 20.0f );
		xpValues.setTag( Blocks.CHORUS_PLANT.getRegistryName(), 2.5f );
		xpValues.setTag( Blocks.CHORUS_FLOWER.getRegistryName(), 35.0f );
		xpValues.setTag( Blocks.VINE.getRegistryName(), 3.5f );
		xpValues.setTag( Blocks.WEB.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.LOG.getRegistryName(), 28.0f );
		xpValues.setTag( Blocks.LOG2.getRegistryName(), 18.0f );
		xpValues.setTag( Blocks.PLANKS.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.BOOKSHELF.getRegistryName(), 100.0f );
		xpValues.setTag( Blocks.DIRT.getRegistryName(), 1.5f );
		xpValues.setTag( Blocks.GRASS.getRegistryName(), 2.5f );
		xpValues.setTag( Blocks.SAND.getRegistryName(), 1.0f );
		xpValues.setTag( Blocks.GRAVEL.getRegistryName(), 2.0f );
		xpValues.setTag( Blocks.MYCELIUM.getRegistryName(), 15.0f );
		xpValues.setTag( Blocks.SLIME_BLOCK.getRegistryName(), 50.0f );
		xpValues.setTag( Blocks.MONSTER_EGG.getRegistryName(), 200.0f );
//		xpValues.setTag( Blocks.STONE.getRegistryName(), 1.0f );
//		xpValues.setTag( Blocks.COBBLESTONE.getRegistryName(), 0.5f );
		xpValues.setTag( Blocks.COAL_ORE.getRegistryName(), 2.0f );
		xpValues.setTag( Blocks.IRON_ORE.getRegistryName(), 12.0f );
		xpValues.setTag( Blocks.REDSTONE_ORE.getRegistryName(), 1.5f );
		xpValues.setTag( Blocks.LIT_REDSTONE_ORE.getRegistryName(), 1.5f );
		xpValues.setTag( Blocks.GOLD_ORE.getRegistryName(), 22.0f );
		xpValues.setTag( Blocks.LAPIS_ORE.getRegistryName(), 5.0f );
		xpValues.setTag( Blocks.QUARTZ_ORE.getRegistryName(), 1.5f );
		xpValues.setTag( Blocks.DIAMOND_ORE.getRegistryName(), 35.0f );
		xpValues.setTag( Blocks.EMERALD_ORE.getRegistryName(), 60.0f );
		xpValues.setTag( Items.FISH.getRegistryName(), 50.0f );
		xpValues.setTag( Items.SADDLE.getRegistryName(), 250.0f );
		xpValues.setTag( Items.NAME_TAG.getRegistryName(), 350.0f );
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
		
		skillTextFormat.setTag( "mining", TextFormatting.AQUA );
		skillTextFormat.setTag( "building", TextFormatting.AQUA );
		skillTextFormat.setTag( "excavation", TextFormatting.GOLD );
		skillTextFormat.setTag( "woodcutting", TextFormatting.GOLD );
		skillTextFormat.setTag( "farming", TextFormatting.GREEN );
		skillTextFormat.setTag( "agility", TextFormatting.GREEN );
		skillTextFormat.setTag( "endurance", TextFormatting.DARK_RED );
		skillTextFormat.setTag( "combat", TextFormatting.RED);
		skillTextFormat.setTag( "archery", TextFormatting.YELLOW );
		skillTextFormat.setTag( "repairing", TextFormatting.GRAY );
		skillTextFormat.setTag( "flying", TextFormatting.GRAY );
		skillTextFormat.setTag( "swimming", TextFormatting.AQUA );
		skillTextFormat.setTag( "fishing", TextFormatting.AQUA );
		skillTextFormat.setTag( "crafting", TextFormatting.GOLD );
////////////////////////////////////LAPIS_DONATORS//////////////////////////////////////////////
		ironDonators.add( "didis54" );
//		lapisDonators.add( "Harmonised" );
////////////////////////////////////ORE_DOUBLE_VALUES//////////////////////////////////////////////
		oreDoubleValues.setTag( Blocks.COAL_ORE.getRegistryName(), 1.0f );
		oreDoubleValues.setTag( Items.COAL.getRegistryName(), 1.0f );
		
		oreDoubleValues.setTag( Blocks.IRON_ORE.getRegistryName(), 0.75f );
		oreDoubleValues.setTag( Items.IRON_INGOT.getRegistryName(), 0.75f );
		
		
		oreDoubleValues.setTag( Blocks.REDSTONE_ORE.getRegistryName(), 2.0f );
		oreDoubleValues.setTag( Blocks.LIT_REDSTONE_ORE.getRegistryName(), 2.0f );
		oreDoubleValues.setTag( Items.REDSTONE.getRegistryName(), 2.0f );
		
		oreDoubleValues.setTag( Blocks.GOLD_ORE.getRegistryName(), 0.50f );
		oreDoubleValues.setTag( Items.GOLD_INGOT.getRegistryName(), 0.50f );
		oreDoubleValues.setTag( Blocks.COAL_ORE.getRegistryName(), 0.5f );
		
		oreDoubleValues.setTag( Blocks.LAPIS_ORE.getRegistryName(), 1.50f );
		oreDoubleValues.setTag( Items.DYE.getRegistryName(), 1.50f );
		
		oreDoubleValues.setTag( Blocks.QUARTZ_ORE.getRegistryName(), 0.55f );
		oreDoubleValues.setTag( Items.QUARTZ.getRegistryName(), 0.55f );
		
		oreDoubleValues.setTag( Blocks.DIAMOND_ORE.getRegistryName(), 0.33f );
		oreDoubleValues.setTag( Items.DIAMOND.getRegistryName(), 0.33f );
		
		oreDoubleValues.setTag( Blocks.EMERALD_ORE.getRegistryName(), 0.75f );
		oreDoubleValues.setTag( Items.EMERALD.getRegistryName(), 0.75f );
////////////////////////////////////PLANT_DOUBLE_VALUES////////////////////////////////////////////
		plantDoubleValues.setTag( Items.WHEAT.getRegistryName(), 0.75f );
		plantDoubleValues.setTag( Items.WHEAT_SEEDS.getRegistryName(), 0.75f );
		
		plantDoubleValues.setTag( Items.CARROT.getRegistryName(), 1.50f );
		
		plantDoubleValues.setTag( Items.POISONOUS_POTATO.getRegistryName(), 1.50f );
		plantDoubleValues.setTag( Items.POTATO.getRegistryName(), 1.50f );
		
		plantDoubleValues.setTag( Items.BEETROOT.getRegistryName(), 1.00f );
		plantDoubleValues.setTag( Items.BEETROOT_SEEDS.getRegistryName(), 1.00f );
		
		plantDoubleValues.setTag( Items.REEDS.getRegistryName(), 0.33f );
		plantDoubleValues.setTag( Blocks.CACTUS.getRegistryName(), 0.45f );
		plantDoubleValues.setTag( Items.NETHER_WART.getRegistryName(), 0.75f );
		
		plantDoubleValues.setTag( Blocks.PUMPKIN.getRegistryName(), 0.60f );
		plantDoubleValues.setTag( Items.PUMPKIN_SEEDS.getRegistryName(), 0.60f );
		
		plantDoubleValues.setTag( Blocks.MELON_BLOCK.getRegistryName(), 0.75f );
		plantDoubleValues.setTag( Items.MELON.getRegistryName(), 0.75f );
		plantDoubleValues.setTag( Items.MELON_SEEDS.getRegistryName(), 0.75f );
		
		plantDoubleValues.setTag( Items.DYE.getRegistryName(), 1.50f );			// COCO
////////////////////////////////////NO_DROP_VALUES/////////////////////////////////////////////////
		noDropOres.setTag( Blocks.IRON_ORE.getRegistryName(), true );
		noDropOres.setTag( Blocks.GOLD_ORE.getRegistryName(), true );
////////////////////////////////////ORE_ITEMS_VALUES///////////////////////////////////////////////
		oreItems.setTag( Blocks.IRON_ORE.getRegistryName(), new ItemStack( Blocks.IRON_ORE, 0 ) );
		oreItems.setTag( Blocks.GOLD_ORE.getRegistryName(), new ItemStack( Blocks.GOLD_ORE, 0 ) );
		oreItems.setTag( Blocks.REDSTONE_ORE.getRegistryName(), new ItemStack( Items.REDSTONE, 0 ) );
		oreItems.setTag( Blocks.LIT_REDSTONE_ORE.getRegistryName(), new ItemStack( Items.REDSTONE, 0 ) );
		oreItems.setTag( Blocks.LAPIS_ORE.getRegistryName(), new ItemStack( Items.DYE, 4 ) );
		oreItems.setTag( Blocks.QUARTZ_ORE.getRegistryName(), new ItemStack( Items.QUARTZ, 0 ) );
		oreItems.setTag( Blocks.DIAMOND_ORE.getRegistryName(), new ItemStack( Items.DIAMOND, 0 ) );
		oreItems.setTag( Blocks.EMERALD_ORE.getRegistryName(), new ItemStack( Items.EMERALD, 0 ) );
////////////////////////////////////MATERIAL_HARVEST_TOOLS/////////////////////////////////////////
		materialHarvestTool.put( Material.ANVIL, "pickaxe" );				//PICKAXE
		materialHarvestTool.put( Material.GLASS, "pickaxe" );
		materialHarvestTool.put( Material.ICE, "pickaxe" );
		materialHarvestTool.put( Material.IRON, "pickaxe" );
		materialHarvestTool.put( Material.PACKED_ICE, "pickaxe" );
		materialHarvestTool.put( Material.PISTON, "pickaxe" );
		materialHarvestTool.put( Material.REDSTONE_LIGHT, "pickaxe" );
		materialHarvestTool.put( Material.ROCK, "pickaxe" );
		
		materialHarvestTool.put( Material.WOOD, "axe" );					//AXE
		materialHarvestTool.put( Material.LEAVES, "axe" );
		
		materialHarvestTool.put( Material.CLAY, "shovel" );					//SHOVEL
		materialHarvestTool.put( Material.GRASS, "shovel" );
		materialHarvestTool.put( Material.GROUND, "shovel" );
		materialHarvestTool.put( Material.SAND, "shovel" );
		materialHarvestTool.put( Material.CRAFTED_SNOW, "shovel" );
		materialHarvestTool.put( Material.SNOW, "shovel" );
		
		materialHarvestTool.put( Material.PLANTS, "hoe" );					//HOE
		materialHarvestTool.put( Material.VINE, "hoe" );
		materialHarvestTool.put( Material.CACTUS, "hoe" );
////////////////////////////////////TOOL_ITEM//////////////////////////////////////////////////////
		toolItems.setTag( Items.DIAMOND_HELMET.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_CHESTPLATE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_LEGGINGS.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_BOOTS.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_AXE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_HOE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_PICKAXE.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_SHOVEL.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		toolItems.setTag( Items.DIAMOND_SWORD.getRegistryName(), new ItemStack( Items.DIAMOND ) );
		
		toolItems.setTag( Items.GOLDEN_HELMET.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_CHESTPLATE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_LEGGINGS.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_BOOTS.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_AXE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_HOE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_PICKAXE.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_SHOVEL.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		toolItems.setTag( Items.GOLDEN_SWORD.getRegistryName(), new ItemStack( Items.GOLD_INGOT ) );
		
		toolItems.setTag( Items.IRON_HELMET.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_CHESTPLATE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_LEGGINGS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_BOOTS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_AXE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_HOE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_PICKAXE.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_SHOVEL.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.IRON_SWORD.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		
		toolItems.setTag( Items.LEATHER_HELMET.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.setTag( Items.LEATHER_CHESTPLATE.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.setTag( Items.LEATHER_LEGGINGS.getRegistryName(), new ItemStack( Items.LEATHER ) );
		toolItems.setTag( Items.LEATHER_BOOTS.getRegistryName(), new ItemStack( Items.LEATHER ) );

		toolItems.setTag( Items.STONE_AXE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.setTag( Items.STONE_HOE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.setTag( Items.STONE_PICKAXE.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.setTag( Items.STONE_SHOVEL.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		toolItems.setTag( Items.STONE_SWORD.getRegistryName(), new ItemStack( Blocks.COBBLESTONE ) );
		
		toolItems.setTag( Items.WOODEN_AXE.getRegistryName(), new ItemStack( Blocks.PLANKS ) );
		toolItems.setTag( Items.WOODEN_HOE.getRegistryName(), new ItemStack( Blocks.PLANKS ) );
		toolItems.setTag( Items.WOODEN_PICKAXE.getRegistryName(), new ItemStack( Blocks.PLANKS ) );
		toolItems.setTag( Items.WOODEN_SHOVEL.getRegistryName(), new ItemStack( Blocks.PLANKS ) );
		toolItems.setTag( Items.WOODEN_SWORD.getRegistryName(), new ItemStack( Blocks.PLANKS ) );
		
		toolItems.setTag( Items.SHEARS.getRegistryName(), new ItemStack( Items.IRON_INGOT ) );
		toolItems.setTag( Items.BOW.getRegistryName(), new ItemStack( Items.STRING ) );
		toolItems.setTag( Items.FISHING_ROD.getRegistryName(), new ItemStack( Items.STRING ) );
		toolItems.setTag( Items.ELYTRA.getRegistryName(), new ItemStack( Items.LEATHER ) );
////////////////////////////////////TOOL_ITEM_AMOUNT///////////////////////////////////////////////
		toolItemAmount.setTag( Items.DIAMOND_HELMET.getRegistryName(), 5 );
		toolItemAmount.setTag( Items.DIAMOND_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.setTag( Items.DIAMOND_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.setTag( Items.DIAMOND_BOOTS.getRegistryName(), 4 );
		toolItemAmount.setTag( Items.DIAMOND_AXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.DIAMOND_HOE.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.DIAMOND_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.DIAMOND_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.setTag( Items.DIAMOND_SWORD.getRegistryName(), 2 );
		
		toolItemAmount.setTag( Items.GOLDEN_HELMET.getRegistryName(), 5 );
		toolItemAmount.setTag( Items.GOLDEN_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.setTag( Items.GOLDEN_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.setTag( Items.GOLDEN_BOOTS.getRegistryName(), 4 );
		toolItemAmount.setTag( Items.GOLDEN_AXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.GOLDEN_HOE.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.GOLDEN_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.GOLDEN_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.setTag( Items.GOLDEN_SWORD.getRegistryName(), 2 );
		
		toolItemAmount.setTag( Items.IRON_HELMET.getRegistryName(), 5 );
		toolItemAmount.setTag( Items.IRON_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.setTag( Items.IRON_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.setTag( Items.IRON_BOOTS.getRegistryName(), 4 );
		toolItemAmount.setTag( Items.IRON_AXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.IRON_HOE.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.IRON_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.IRON_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.setTag( Items.IRON_SWORD.getRegistryName(), 2 );
		
		toolItemAmount.setTag( Items.LEATHER_HELMET.getRegistryName(), 5 );
		toolItemAmount.setTag( Items.LEATHER_CHESTPLATE.getRegistryName(), 8 );
		toolItemAmount.setTag( Items.LEATHER_LEGGINGS.getRegistryName(), 7 );
		toolItemAmount.setTag( Items.LEATHER_BOOTS.getRegistryName(), 4 );

		toolItemAmount.setTag( Items.STONE_AXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.STONE_HOE.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.STONE_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.STONE_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.setTag( Items.STONE_SWORD.getRegistryName(), 2 );
		
		toolItemAmount.setTag( Items.WOODEN_AXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.WOODEN_HOE.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.WOODEN_PICKAXE.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.WOODEN_SHOVEL.getRegistryName(), 1 );
		toolItemAmount.setTag( Items.WOODEN_SWORD.getRegistryName(), 2 );
		
		toolItemAmount.setTag( Items.SHEARS.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.BOW.getRegistryName(), 3 );
		toolItemAmount.setTag( Items.FISHING_ROD.getRegistryName(), 2 );
		toolItemAmount.setTag( Items.ELYTRA.getRegistryName(), 7 );
////////////////////////////////////SALVAGE_BASE_VALUE/////////////////////////////////////////////
		salvageBaseValue.setTag( Blocks.COBBLESTONE.getRegistryName(), 50.0f );
		salvageBaseValue.setTag( Blocks.PLANKS.getRegistryName(), 70.0f );
		salvageBaseValue.setTag( Items.STRING.getRegistryName(), 65.0f );
		salvageBaseValue.setTag( Items.IRON_INGOT.getRegistryName(), 20.0f );
		salvageBaseValue.setTag( Items.GOLD_INGOT.getRegistryName(), 35.0f );
		salvageBaseValue.setTag( Items.DIAMOND.getRegistryName(), 0.0f );
		salvageBaseValue.setTag( Items.LEATHER.getRegistryName(), 60.0f );
////////////////////////////////////SALVAGE_VALUE_PER_LEVEL////////////////////////////////////////
		salvageValuePerLevel.setTag( Blocks.COBBLESTONE.getRegistryName(), 2.0f );	// 20
		salvageValuePerLevel.setTag( Blocks.PLANKS.getRegistryName(), 0.8f );		// 25
		salvageValuePerLevel.setTag( Items.STRING.getRegistryName(), 1.0f );		// 30
		salvageValuePerLevel.setTag( Items.IRON_INGOT.getRegistryName(), 0.8f );	//100
		salvageValuePerLevel.setTag( Items.GOLD_INGOT.getRegistryName(), 1.6f );	// 50
		salvageValuePerLevel.setTag( Items.DIAMOND.getRegistryName(), 0.666f );	//150
		salvageValuePerLevel.setTag( Items.LEATHER.getRegistryName(), 1.0f );		// 30
////////////////////////////////////REPAIR_XP//////////////////////////////////////////////////////
		repairXp.setTag( Items.IRON_INGOT.getRegistryName(), 35.0f );
		repairXp.setTag( Items.GOLD_INGOT.getRegistryName(), 666.0f );
		repairXp.setTag( Items.DIAMOND.getRegistryName(), 75.0f );
		repairXp.setTag( Items.STRING.getRegistryName(), 20.0f );
		repairXp.setTag( Items.LEATHER.getRegistryName(), 40.0f );
////////////////////////////////////SALVAGE_XP/////////////////////////////////////////////////////
		salvageXp.setTag( Items.IRON_INGOT.getRegistryName(), 20.0f );
		salvageXp.setTag( Items.GOLD_INGOT.getRegistryName(), 25.0f );
		salvageXp.setTag( Items.DIAMOND.getRegistryName(), 50.0f );
		salvageXp.setTag( Items.STRING.getRegistryName(), 2.0f );
		salvageXp.setTag( Items.LEATHER.getRegistryName(), 5.0f );
	}
	
	private static String getSkill( String tool )
	{
		if( tool == null )
			return "none";
		
		switch( tool )
		{
			case "pickaxe":
				return "mining";
				
			case "shovel":
				return "excavation";
				
			case "axe":
				return "woodcutting";
				
			case "hoe":
				return "farming";
				
			default:
				return "none";
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
	
	public static void handlePlaced( PlaceEvent event )
	{
		EntityPlayer player = event.getEntityPlayer();
		if( !player.isCreative() )
		{
			Block block = event.getPlacedBlock().getBlock();
			NBTTagCompound persistTag = getPersistTag( player );
			NBTTagCompound skillsTag = getSkillsTag( persistTag );
			float blockHardness;

			try
			{
				blockHardness = block.getBlockHardness( block.getDefaultState(), event.getWorld(), event.getPos());
				if( blockHardness > 50 )
					blockHardness = 50;
			}
			catch( Exception e )
			{
				blockHardness = 1;
			}
//			int buildLevel = levelAtXp( skillsTag.getFloat( "building" ) );
			String playerName = player.getName();
			BlockPos blockPos = event.getPos();
			
			if( !lastPosPlaced.containsKey( playerName ) || !lastPosPlaced.get( playerName ).equals( blockPos ) )
			{
				if( block.equals( Blocks.FARMLAND ) )
					awardXp( player, "farming", "tilting dirt", blockHardness, false );
				else
				{
//					for( int i = 0; i < 1000; i++ )
//					{
						awardXp( player, "building", block.getLocalizedName(), blockHardness, false );
//					}
				}
			}
			
			if( lastPosPlaced.containsKey( playerName ) )
				lastPosPlaced.replace( player.getName(), event.getPos() );
			else
				lastPosPlaced.setTag( playerName, blockPos );
			
//			int newBuildLevel = levelAtXp( skillsTag.getFloat( "building" ) );
			
//			if( player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE ).getBaseValue() < ( 4.09D + ( buildLevel / 25D ) ) )
//			player.getAttributeMap().getAttributeInstance( player.REACH_DISTANCE ).setBaseValue( 4.09D + ( buildLevel / 25D ) );
//			
			if( getXp( block.getRegistryName() ) != 0 )
				PlacedBlocks.orePlaced( event.getWorld(), event.getPos() );
		}
	}
	
	public static void handleHarvested( HarvestDropsEvent event )
	{
		if( event.getHarvester() instanceof EntityPlayer && !event.getWorld().isRemote )
		{
			EntityPlayer player = event.getHarvester();
			
			if( !player.isCreative() )
			{
				NBTTagCompound persistTag = getPersistTag( player );
				NBTTagCompound skillsTag = getSkillsTag( persistTag );
				Block block = event.getState().getBlock();
				if( block == Blocks.REEDS || block == Blocks.CACTUS )
					return;
				ResourceLocation blockRegistry = block.getRegistryName();
				Material material = block.getMaterial( block.getDefaultState() );
				boolean wasPlaced = PlacedBlocks.isPlayerPlaced( event.getWorld(), event.getPos() );
				float blockHardness;
				try
				{
					blockHardness = block.getBlockHardness( block.getDefaultState(), event.getWorld(), event.getPos());
					if( blockHardness > 50 )
						blockHardness = 50;
				}
				catch( Exception e )
				{
					blockHardness = 1;
				}
				float award = blockHardness;
				List<ItemStack> drops = event.getDrops();		
				if( material.equals( Material.PLANTS ) && drops.size() > 0 ) //IS PLANT
				{
					ItemStack theDrop = new ItemStack( drops.get( 0 ).getItem(), 1, drops.get( 0 ).getMetadata() );
					Item theDropItem = drops.get( 0 ).getItem();
					
					if( block instanceof BlockFlower )
					{
						if( !wasPlaced )
							award += getXp( block.getRegistryName() ) * drops.size();	//IS FLOWER
						if( award > 0 )
							awardXp( player, "farming", "harvesting " + theDrop.getDisplayName(), award, false );
						return;
					}
					IIBlockState = event.getState();
					int age = -1;
					int maxAge = -1;
					int matches = 0;
					
					try
					{					
						if( block instanceof BlockBeetroot )
						{
							age = state.getValue( BlockBeetroot.BEETROOT_AGE );
							maxAge = 3;
						}
						else if( block instanceof BlockCrops )
						{
							age = state.getValue( BlockCrops.AGE );
							maxAge = 7;
						}
						else if( block instanceof BlockNetherWart )
						{
							age = state.getValue( BlockNetherWart.AGE );
							maxAge = 3;
						}
						else if( block instanceof BlockCocoa )
						{
							age = state.getValue( BlockCocoa.AGE );
							maxAge = 2;
						}
						else if ( block instanceof BlockStem )
						{
							age = state.getValue( BlockStem.AGE );
							maxAge = 7;
						}
						else
						{
							return;
						}
					}
					catch( IllegalArgumentException err )
					{
						return;
					}
					
					if( age == maxAge && age >= 0 )
					{
						int currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
						float extraChance = getPlantDoubleChance( theDropItem.getRegistryName() ) * currLevel;
						int guaranteedDrop = 0;
						int extraDrop = 0;
						
						if( ( extraChance / 100 ) > 1 )
						{
							guaranteedDrop = (int)Math.floor( extraChance / 100 );
							extraChance = (float)( ( extraChance / 100 ) - Math.floor( extraChance / 100 ) ) * 100;
						}
						
						if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
							extraDrop = 1;
						
						for( int i = 0; i < guaranteedDrop + extraDrop; i++ )
						{
							drops.add( theDrop );
						}
						
						for( ItemStack drop : drops )
						{
							if( drop.getItem().equals ( theDropItem ) )
								matches++;
						}
						
						if ( guaranteedDrop + extraDrop > 0 )
							player.sendStatusMessage( new TextComponentString( Integer.toString( guaranteedDrop + extraDrop ) + " Extra " + theDrop.getDisplayName() + " Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						
						award += getXp( block.getRegistryName() ) * matches;
						awardXp( player, "farming", "harvesting " + ( matches - guaranteedDrop - extraDrop ) + " + " + ( guaranteedDrop + extraDrop ) + " " + theDrop.getDisplayName(), award, false );
//						player.sendStatusMessage( new TextComponentString( getXp( block.getRegistryName() ) + " xp, award: " + award ), false );
					}
				}
				else if( getOreDoubleChance( block.getRegistryName() ) != 0.0f )		//IS ORE
				{
					Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
					boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;
					boolean noDropOre = getNoDropOre( block.getRegistryName() );
					
					if( !wasPlaced && !isSilk )
						award += getXp( block.getRegistryName() ) * drops.size();
					
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
							award += getXp( block.getRegistryName() ) * ( drops.size() );
						
						String awardMessage = "mining " + block.getLocalizedName();
						
						if( guaranteedDrop + extraDrop > 0 )
						{
							award += getXp( block.getRegistryName() ) * ( guaranteedDrop + extraDrop );
							ItemStack theDrop = new ItemStack( drops.get( 0 ).getItem(), guaranteedDrop + extraDrop, drops.get( 0 ).getMetadata() );
							drops.add( theDrop );
							awardMessage = "mining " + block.getLocalizedName() + " and " + ( guaranteedDrop + extraDrop ) + " extra " + drops.get( 0 ).getDisplayName() + " drop";
							player.sendStatusMessage( new TextComponentString( Integer.toString( guaranteedDrop + extraDrop ) + " Extra " + theDrop.getDisplayName() + " Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						awardXp( player, "mining", awardMessage, award, false );
						
					}
					else
						awardXp( player, "mining", "breaking " + block.getLocalizedName(), award, false );
				}
				else
				{
					if( !wasPlaced )
						award += getXp( block.getRegistryName() ) * drops.size();
					String harvestTool = block.getHarvestTool( block.getDefaultState() );
					if( harvestTool == null || !XP.correctHarvestTool( material ).equals( "none" ) )
						harvestTool = XP.correctHarvestTool( material );
					String skill = XP.getSkill( harvestTool );
					
					if( skill != "none" )										//if correct skill is matched
						awardXp( player, skill, "removing " + block.getLocalizedName(), award, false );
				}
			}
		}
		PlacedBlocks.removeOre( event.getWorld(), event.getPos() );
	}
	
	public static void handleBroken( BreakEvent event )
	{
		if( event.getEntityPlayer() instanceof EntityPlayer )
		{
			EntityPlayer player = event.getEntityPlayer();
			if( !player.isCreative() )
			{
				NBTTagCompound persistTag = getPersistTag( player );
				NBTTagCompound skillsTag = getSkillsTag( persistTag );
				Block currBlock = event.getState().getBlock();
				boolean wasPlaced = PlacedBlocks.isPlayerPlaced( event.getWorld(), event.getPos() );
				Block reeds = Blocks.REEDS;
				Block cactus = Blocks.CACTUS;
							
				if( currBlock.equals( reeds ) || currBlock.equals( cactus ) )
				{
					int currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
					World world = event.getWorld();
					Block baseBlock = event.getState().getBlock();
					BlockPos baseBlockPos = event.getPos();
					float hardness;
					try
					{
						hardness = baseBlock.getBlockHardness( baseBlock.getDefaultState(), event.getWorld(), event.getPos());
						if( hardness > 50 )
							hardness = 50;
					}
					catch( Exception e )
					{
						hardness = 1;
					}
					float award = 0;
					float rewardable = 0;
					float extraChance = getPlantDoubleChance( baseBlock.getRegistryName() ) * currLevel;
					int guaranteedDrop = 0;
					int extraDrop = 0;
					
					if( ( extraChance / 100 ) > 1 )
					{
						guaranteedDrop = (int)Math.floor( extraChance / 100 );
						extraChance = (float)( ( extraChance / 100 ) - Math.floor( extraChance / 100 ) ) * 100;
					}
					if( !wasPlaced )
						rewardable++;
					
					int height = 1;
					BlockPos currBlockPos = new BlockPos( baseBlockPos.x, baseBlockPos.y + height, baseBlockPos.z );
					currBlock =  world.getBlockState( currBlockPos ).getBlock();
					for( ; ( currBlock.equals( baseBlock ) ); )
					{
						wasPlaced = PlacedBlocks.isPlayerPlaced( world, currBlockPos );
						if( !wasPlaced )
						{
//							rewardable += 1;
							rewardable++;
								
							if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
								extraDrop++;
						}
						height++;
						currBlockPos = new BlockPos( baseBlockPos.x, baseBlockPos.y + height, baseBlockPos.z );
						currBlock =  world.getBlockState( currBlockPos ).getBlock();
					}
					
					int dropsLeft = guaranteedDrop + extraDrop;
					if( guaranteedDrop + extraDrop > 0 )
					{
						while( dropsLeft > 64 )
						{
							if( baseBlock == Blocks.CACTUS )
								baseBlock.spawnAsEntity( event.getWorld(), event.getPos(), new ItemStack( Blocks.CACTUS, dropsLeft ) );
							else
								baseBlock.spawnAsEntity( event.getWorld(), event.getPos(), new ItemStack( Items.REEDS, dropsLeft ) );
							dropsLeft -= 64;
						}
						if( baseBlock == Blocks.CACTUS )
						{
							baseBlock.spawnAsEntity( event.getWorld(), event.getPos(), new ItemStack( Blocks.CACTUS, dropsLeft ) );
							player.sendStatusMessage( new TextComponentString( ( guaranteedDrop * height ) + extraDrop + " Extra Cactus Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						else
						{
							baseBlock.spawnAsEntity( event.getWorld(), event.getPos(), new ItemStack( Items.REEDS, dropsLeft ) );
							player.sendStatusMessage( new TextComponentString( ( guaranteedDrop * height ) + extraDrop + " Extra Sugar Cane Dropped!" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), true );
						}
						
					}
						
					award += getXp( baseBlock.getRegistryName() ) * ( rewardable + guaranteedDrop + extraDrop) + hardness * ( rewardable + guaranteedDrop + extraDrop);
	//				System.out.println( "Height: " + height );
					awardXp( player, "farming", "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " " + baseBlock.getLocalizedName(), award, false );
				}
			}
		}
	}
	
	public static void handleDamage( LivingDamageEvent event )
	{
		float damage = event.getAmount();
		float startDmg = damage;
		EntityLivingBase target = event.getEntityLiving();
		if( target instanceof EntityPlayer )
		{
			EntityPlayer player = (EntityPlayer) target;
			NBTTagCompound persistTag = getPersistTag( player );
			NBTTagCompound skillsTag = getSkillsTag( persistTag );
			float agilityXp = 0;
			float enduranceXp = 0;
			boolean hideEndurance = false;
			
///////////////////////////////////////////////////////////////////////ENDURANCE//////////////////////////////////////////////////////////////////////////////////////////
			int enduranceLevel = levelAtXp( skillsTag.getFloat( "endurance" ) );
			float endured = 0;
			float endurePercent = enduranceLevel * 0.25f;
			if( endurePercent > 50 )
				endurePercent = 50;
			endured = damage * ( endurePercent / 100 );
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
				float chance = agilityLevel * 0.50f;
				if( chance > 64 )
					chance = 64;
				for( int i = 0; i < damage; i++ )
				{
					if( Math.ceil( Math.random() * 100 ) <= chance )
					{
						saved++;
					}
				}
				damage -= saved;
				
				if( saved != 0 && player.getHealth() > damage )
					player.sendStatusMessage( new TextComponentString( "Saved " + (int) saved + " damage!" ), true );
				
				award = saved * 30;
					
				agilityXp = award;
			}
			
			event.setAmount( damage );
			
			if( player.getHealth() > damage )
			{
				if( agilityXp > 0 )
					hideEndurance = true;
				
				if( event.getSource().getTrueSource() != null )
					awardXp( player, "endurance", event.getSource().getTrueSource().getName(), enduranceXp, hideEndurance );
				else
					awardXp( player, "endurance", event.getSource().getDamageType(), enduranceXp, hideEndurance );
				
				if( agilityXp > 0 )
					awardXp( player, "agility", "surviving " + startDmg + " fall damage", agilityXp, false );
			}
		}
		
		if ( target instanceof EntityLivingBase && event.getSource().getTrueSource() instanceof EntityPlayer )
		{
			EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
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
				
				if( target instanceof EntityAnimal )		//reduce xp if passive mob
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
					double distance = event.getEntity().getDistance( player.getPosition().x, player.getPosition().y, player.getPosition().z );
					if( distance > 16 )
						distance -= 16;
					else
						distance = 0;
					
					amount += (float) ( Math.pow( distance, 1.25 ) * ( damage / target.getMaxHealth() ) * ( startDmg >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp

					amount *= lowHpBonus;
					awardXp( player, "archery", player.getHeldItemMainhand().getDisplayName(), amount, false );
				}
				else
				{
					amount *= lowHpBonus;
					awardXp( player, "combat", player.getHeldItemMainhand().getDisplayName(), amount, false );
				}
			}
		}
	}
	
	public static void handleLivingJump( LivingJumpEvent event )
	{
		if( event.getEntityLiving() instanceof EntityPlayer )
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			NBTTagCompound persistTag = getPersistTag( player );
			NBTTagCompound skillsTag = getSkillsTag( persistTag );
			
			
			
			double agilityLevel;
			double jumpBoost = 0;
			
			if( player.world.isRemote )
			{
				if( XPOverlayGUI.skills.get( "agility" ) == null )
					agilityLevel = 1;
				else
					agilityLevel = levelAtXp( XPOverlayGUI.skills.get( "agility" ).xp );
			}
			else
				agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
			
			if( player.isSneaking() )
				jumpBoost = -0.011 + agilityLevel * ( 0.14/33 );
			
			if( player.isSprinting() )
				jumpBoost = -0.013 + agilityLevel * ( 0.14/50 );
			
			if( jumpBoost > 0.33 )
				jumpBoost = 0.33;
			
			if( player.world.isRemote )
			{			
				if( jumpBoost > 0 )
					event.getEntityLiving().motionY += jumpBoost;
			}
			else if( !player.isInWater() )
				awardXp( player, "agility", "jumping", (float) ( jumpBoost * 10 + 1 ), true );
		}
	}
	
	public static void handlePlayerRespawn( PlayerRespawnEvent event )
	{
		EntityPlayer player = event.player;
		
		AttributeHandler.updateReach( player );
		AttributeHandler.updateHP( player );
		AttributeHandler.updateDamage( player );
	}
	
	public static void handlePlayerConnected( PlayerLoggedInEvent event )
	{
		EntityPlayer player = event.player;
		NBTTagCompound persistTag = getPersistTag( player );
		NBTTagCompound skillsTag = getSkillsTag( persistTag );
		Set<String> keySet = skillsTag.getKeySet();

		if( lapisDonators.contains( player.getDisplayNameString() ) )
		{
			player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
			{
				thePlayer.sendStatusMessage( new TextComponentString( "Welcome, PMMO Lapis Tier Patreon " + player.getDisplayNameString() + "!" ).setStyle( new Style().setColor( TextFormatting.BLUE ) ), false );
			});
		}
		else if( dandelionDonators.contains( player.getDisplayNameString() ) )
			player.sendStatusMessage( new TextComponentString( "Welcome, PMMO Dandelion Tier Patreon " + player.getDisplayNameString() + "!" ).setStyle( new Style().setColor( TextFormatting.YELLOW ) ), false );
		else if( ironDonators.contains( player.getDisplayNameString() ) )
			player.sendStatusMessage( new TextComponentString( "Welcome, PMMO Iron Tier Patreon " + player.getDisplayNameString() + "!" ).setStyle( new Style().setColor( TextFormatting.GRAY ) ), false );

		NetworkHandler.sendToPlayer( new MessageXp( 0f, "CLEAR", 0f, true ), (EntityPlayerMP) player );

		for( String tag : keySet )
		{
			NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getFloat( tag ), tag, 0, true ), (EntityPlayerMP) player );
		}
	}
	
	public static void handleRightClickItem( RightClickItem event )
	{
	}
	
	public static void handleRightClickBlock( RightClickBlock event )
	{
		if( !event.getWorld().isRemote )
		{
			EntityPlayer player = event.getEntityPlayer();
			NBTTagCompound persistTag = getPersistTag( player );
			NBTTagCompound skillsTag = getSkillsTag( persistTag );
			ItemStack itemStack = event.getItemStack();
			Item item = itemStack.getItem();
			String itemDisplayName = itemStack.getDisplayName();
			Block block = event.getWorld().getBlockState( event.getPos() ).getBlock();
			Block anvil 		=	Blocks.ANVIL;
			Block ironBlock		= 	Blocks.IRON_BLOCK;
			Block goldBlock 	= 	Blocks.GOLD_BLOCK;
			Block diamondBlock 	= 	Blocks.DIAMOND_BLOCK;
			if( player.isSneaking() )
			{
				if( block.equals( ironBlock ) || block.equals( anvil ) )
				{
					if( event.getHand() == EnumEnumHand.MAIN_HAND )
					{
						int currLevel;
						float baseChance = 0;
						float extraChance = 0;
						
						if( getPlantDoubleChance( item.getRegistryName() ) != 0 && itemStack.getMetadata() != 4 )
						{
							currLevel = levelAtXp( skillsTag.getFloat( "farming" ) );
							baseChance = getPlantDoubleChance( item.getRegistryName() );
							extraChance = baseChance* currLevel;
						}
						else if( getOreDoubleChance( item.getRegistryName() ) != 0 && itemStack.getMetadata() != 3 )
						{
							currLevel = levelAtXp( skillsTag.getFloat( "mining" ) );
							baseChance = getOreDoubleChance( item.getRegistryName() );
							extraChance = baseChance * currLevel;
						}
						
						if( item == Items.DYE && itemStack.getMetadata() != 3 && itemStack.getMetadata() != 4 )
							return;
						
						if( extraChance != 0 )
							player.sendStatusMessage( new TextComponentString( itemStack.getDisplayName() + " " + DP.dp( extraChance ) + "% extra chance, " + DP.dp( baseChance ) + "% per level" ), false );
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
							
							float startDmg = itemStack.getItemDamage();
							float maxDmg = itemStack.getMaxDamage();
							float award = 0;
							float displayDurabilityPercent = ( 1.00f - ( startDmg / maxDmg ) ) * 100;
							float durabilityPercent = ( 1.00f - ( startDmg / maxDmg ) );
							int potentialReturnAmount = (int) Math.floor( itemPotential * durabilityPercent );
							
							if( event.getHand() == EnumEnumHand.OFF_HAND )
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
									
									itemStack.damageItem( item.getMaxDamage() * 10, player );
									
									String replyMsg = "Salvaged " + returnAmount + "/" + itemPotential + " " + new ItemStack( salvageItem ).getDisplayName();
									replyMsg += ( returnAmount > 1 && !salvageItem.equals( Items.STRING ) && !salvageItem.equals( Items.LEATHER ) && !( salvageItem instanceof ItemBlock ) ? "s" : "" ) + "!";
									BlockPos spawnPos = event.getPos();
									if( returnAmount > 0 )
										event.getWorld().spawnEntity( new EntityItem( event.getWorld(), spawnPos.x + 0.5d, spawnPos.y + 1.0d, spawnPos.z + 0.5d, new ItemStack( salvageItem, returnAmount ) ) );
									
									if( award > 0 )
										awardXp( player, "repairing", "salvaging " + returnAmount + "/" + itemPotential + " " + new ItemStack( salvageItem ).getDisplayName() + " from salvaging " + itemDisplayName, award, false  );
									player.sendStatusMessage( new TextComponentString( replyMsg ), true );
									
									if( enchants.size() > 0 )
									{
										ItemStack salvagedBook = new ItemStack( Items.KNOWLEDGE_BOOK );
										Set<Enchantment> enchantKeys = enchants.getKeySet();
										int enchantLevel;
										for( Enchantment enchant : enchantKeys )
										{
											enchantLevel = 0;
											for( int i = 1; i <= enchants.get( enchant ); i++ )
											{
												if( Math.floor( Math.random() * 100 ) < enchantChance )
													enchantLevel = i;
												else
													i = enchants.get( enchant ) + 1;
											}
											if( enchantLevel > 0 )
												salvagedBook.addEnchantment( enchant, enchantLevel );
										}
										if( salvagedBook.isItemEnchanted() )
										{
											event.getWorld().spawnEntity( new EntityItem( event.getWorld(), spawnPos.x + 0.5d, spawnPos.y + 1.0d, spawnPos.z + 0.5d, salvagedBook ) );
											player.sendStatusMessage( new TextComponentString( "You managed to save some enchants!" ), true );
										}
									}
								}
								else
									player.sendStatusMessage( new TextComponentString( "Only available in Survival Mode!" ), true );
							}
							else
							{
//								itemStack.damageItem( 200, player );
								player.sendStatusMessage( new TextComponentString( "Off-Hand to Disassemble!" ), true );
								player.sendStatusMessage( new TextComponentString( "_________________________________" ), false );
								player.sendStatusMessage( new TextComponentString( itemDisplayName + " " + DP.dp( displayDurabilityPercent ) + "%" ), false );
								player.sendStatusMessage( new TextComponentString( DP.dp( chance ) + "% per material, " + potentialReturnAmount + " max items returned" ), false );
								player.sendStatusMessage( new TextComponentString( DP.dp( enchantChance ) + "% enchant return, " + itemStack.getRepairCost() + " repair cost" ), false );
							}
						}
						else
						{
							player.sendStatusMessage( new TextComponentString( "UNACCOUNTED FOR DAMAGEABLE ITEM! Please Report!" ), false );
						}
					}
				}
				
				if( block.equals( diamondBlock ) && event.getHand() == EnumEnumHand.MAIN_HAND )
				{					
					int agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
					int enduranceLevel = levelAtXp( skillsTag.getFloat( "endurance" ) );
					int combatLevel = levelAtXp( skillsTag.getFloat( "combat" ) );
					int swimLevel = levelAtXp( skillsTag.getFloat( "swimming" ) );
					
					double reach = AttributeHandler.getReach( player );
					double agilityChance = agilityLevel * 0.64f;
					double damageReduction = enduranceLevel * 0.25f;
					double extraDamage = Math.floor( combatLevel / 20 );
					
					float speedPercent = agilityLevel / 2000f;
					
					if( agilityChance > 64 )
						agilityChance = 64;
					
					if( damageReduction > 50 )
						damageReduction = 50;
					
					if( speedPercent > 0.10f )
						speedPercent = 0.10f;
					speedPercent = speedPercent / 0.10f * 100f;
					
					player.sendStatusMessage( new TextComponentString( "_________________________________" ), false );
					player.sendStatusMessage( new TextComponentString( DP.dp( reach ) + " player reach" ), false );
					player.sendStatusMessage( new TextComponentString( DP.dp( agilityChance ) + "% fall damage save chance" ), false );
					player.sendStatusMessage( new TextComponentString( DP.dp( damageReduction ) + "% damage reduction" ), false );
					player.sendStatusMessage( new TextComponentString( DP.dp( extraDamage ) + " extra damage" ), false );
					player.sendStatusMessage( new TextComponentString( DP.dp( speedPercent ) + "% sprint speed boost" ), false );
					
					if( swimLevel >= 25 )
						player.sendStatusMessage( new TextComponentString( "Underwater night vision is unlocked!" ), false );
					else
						player.sendStatusMessage( new TextComponentString( "Underwater night vision is locked until level 25 Swimming" ), false );
				}
			}
		}
	}
	
	public static void handleAnvilRepair( AnvilRepairEvent event )
	{
		try
		{
			EntityPlayer player = event.getEntityPlayer();
			if( !player.world.isRemote && event.getItemInput().getItem().isDamageable() )
			{
				NBTTagCompound persistTag = getPersistTag( player );
				NBTTagCompound skillsTag = getSkillsTag( persistTag );

				int currLevel = levelAtXp( skillsTag.getFloat( "repairing" ) );
				float bonusRepair = 0.0025f * currLevel / ( 1f + 0.01f * currLevel );
				int maxCost = (int) Math.floor( 50 - ( currLevel / 4 ) );
				if( maxCost < 20 )
					maxCost = 20;

				event.setBreakChance( event.getBreakChance() / ( 1f + 0.01f * currLevel ) );

//				ItemStack rItem = event.getIngredientInput();
				ItemStack lItem = event.getItemInput();
				ItemStack oItem = event.getItemResult();

				if( oItem.getRepairCost() > maxCost )
					oItem.setRepairCost( maxCost );

				float repaired = oItem.getItemDamage() - lItem.getItemDamage();
				if( repaired < 0 )
					repaired = -repaired;

				oItem.setItemDamage( (int) Math.floor( oItem.getItemDamage() - repaired * bonusRepair ) );

				float award = (float) ( ( ( repaired + repaired * bonusRepair * 2.5 ) / 100 ) * ( 1 + lItem.getRepairCost() * 0.025 ) );
				award *= getRepairXp( getToolItem( lItem.getItem().getRegistryName() ).getItem().getRegistryName() );

				if( award > 0 )
				{
					player.sendStatusMessage( new TextComponentString( "repaired " + (int) repaired + " + " + (int) ( repaired * bonusRepair ) + " extra!" ), true );
					awardXp( player, "repairing", "repairing " + oItem.getDisplayName() + " by: " + repaired, award, false );
				}
			}
		}
		catch( Exception e )
		{
			System.out.println( e );
		}
	}
	
	public static void handleCrafted( ItemCraftedEvent event )
	{
		if( !event.player.world.isRemote )
		{
			awardXp( event.player, "crafting", "crafting " + event.crafting.getDisplayName(), + 10.0f, false );
		}
	}
	
	public static NBTTagCompound getPersistTag( EntityPlayer player )
	{
		NBTTagCompound playerNBT = player.getEntityData();
		NBTTagCompound persistTag = null;
		if( !playerNBT.hasKey( player.PERSISTED_NBT_TAG ) )				//if Player doesn't have persist tag, make it
		{
			persistTag = new NBTTagCompound();
			playerNBT.setTag( player.PERSISTED_NBT_TAG, persistTag );
		}
		else
		{
			persistTag = playerNBT.getCompoundTagTag( player.PERSISTED_NBT_TAG );
		}
		return persistTag;
	}
	
	public static NBTTagCompound getSkillsTag( NBTTagCompound persistTag )
	{
		NBTTagCompound skillsTag = null;
		if( !persistTag.hasKey( "skills" ) )						//if Player doesn't have skills tag, make it
		{
			skillsTag = new NBTTagCompound();
			persistTag.setTag( "skills", skillsTag );
		}
		else
		{
			skillsTag = persistTag.getCompoundTagTag( "skills" );	//if Player has skills tag, use it
		}
		return skillsTag;
	}
	
	public static void awardXp( EntityPlayer player, String skill, String sourceName, float amount, boolean skip )
	{
		if( player instanceof FakePlayer )
			return;

		if( amount <= 0.0f || player.world.isRemote )
			return;
		
		if( !validSkills.contains( skill ) )
		{
			System.out.println( "INVALID SKILL" );
			return;
		}
		
//		System.out.println( amount );
		
		if( skill.equals( "combat" ) || skill.equals( "archery" ) || skill.equals( "endurance" ) )
		{
			switch( player.world.getDifficulty() )
			{
			case PEACEFUL:
				amount *= 1f/3f;
				break;
					
			case EASY:
				amount *= 2f/3f;
				break;
				
			case NORMAL:
				break;
				
			case HARD:
				amount *= 4f/3f;
				break;
				
				default:
					break;
			}
		}
		
//		System.out.println( amount );
		
		amount *= globalMultiplier;
		
		String playerName = player.getName();
		int startLevel;
		int currLevel;
		float startXp = 0;
		
		NBTTagCompound persistTag = getPersistTag( player );		
		NBTTagCompound skillsTag = getSkillsTag( persistTag );
		
		if( !skillsTag.hasKey( skill ) )						//same but for skills
		{
			skillsTag.setFloat( skill, amount );
			startLevel = 1;
		}
		else
		{
			startLevel = levelAtXp( skillsTag.getFloat( skill ) );
			startXp = skillsTag.getFloat( skill );
			
			if( startXp >= 2000000000 )
				return;
			
			if( startXp + amount >= 2000000000 )
			{
				player.sendStatusMessage( new TextComponentString( skill + " cap of 2b xp reached, you fucking psycho!" ).setStyle( new Style().setColor( TextFormatting.LIGHT_PURPLE ) ), false);
				System.out.println( player.getName() + " " + skill + " 2b cap reached" );
				amount = 2000000000 - startXp;
			}
				
			skillsTag.setFloat( skill, startXp + amount );
		}
		
		currLevel = levelAtXp( skillsTag.getFloat( skill ) );
		
		if( startLevel != currLevel )
		{
			switch( skill )
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

			if( Loader.isModLoaded( "compatskills" ) )
			{
				if( !player.world.isRemote )
                {
                    if( skill.equals( "mining" ) || skill.equals( "building" ) || skill.equals( "farming" ) || skill.equals( "agility" ) )
                        player.getServer().commandManager.executeCommand( player, "/reskillable incrementskill " + playerName + " reskillable." + skill + " 1" );
                    else
					    player.getServer().commandManager.executeCommand( player, "/reskillable incrementskill " + playerName + " compatskills." + skill + " 1" );
			    }
            }

			System.out.println( player.getName() + " " + currLevel + " " + skill + " level up!" );
		}
		
		if( player instanceof EntityPlayerMP )
			NetworkHandler.sendToPlayer( new MessageXp( startXp, skill, amount, skip ), (EntityPlayerMP) player );
		
		persistTag.setString( "lastSkill", skill);
		System.out.println( playerName + " +" + amount + "xp in "  + skill + " for " + sourceName + " total xp: " + skillsTag.getFloat( skill ) );
		
		if( startXp + amount >= maxXp && startXp <= maxXp )
		{
			player.sendStatusMessage( new TextComponentString( skill + " max level reached, you psycho!" ).setStyle( new Style().setColor( TextFormatting.LIGHT_PURPLE ) ), false);
			System.out.println( player.getName() + " " + skill + " max level reached" );
		}
	}
	
	public static void setXp( EntityPlayer player, String skill, float newXp )
	{		
		NBTTagCompound persistTag = getPersistTag( player );		
		NBTTagCompound skillsTag = getSkillsTag( persistTag );
		skillsTag.setFloat( skill, newXp );
		
		switch( skill )
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
		
		NetworkHandler.sendToPlayer( new MessageXp( newXp, skill, 0, false ), (EntityPlayerMP) player );
	}
	
	public static void handlePlayerTick( PlayerTickEvent event )
	{
		EntityPlayer player = event.player;
		
		if( !player.world.isRemote )
		{
			String name = player.getName();
			
			if( player.isSprinting() )
				AttributeHandler.updateSpeed( player );
			else
				AttributeHandler.resetSpeed( player );
			
			if( !lastAward.containsKey( name ) )
				 lastAward.setTag( name, System.currentTimeMillis() );
			
			if( !player.isCreative() && player.isEntityAlive() )
			{
				long gap = System.currentTimeMillis() - lastAward.get( name );
				if( gap > 1000 )
				{
					NBTTagCompound persistTag = getPersistTag( player );
					NBTTagCompound skillsTag = getSkillsTag( persistTag );
					
					int swimLevel = levelAtXp( skillsTag.getFloat( "swimming" ) );
					int flyLevel = levelAtXp( skillsTag.getFloat( "flying" ) );
					int agilityLevel = levelAtXp( skillsTag.getFloat( "agility" ) );
					if( agilityLevel > 200 )
						agilityLevel = 200;
					
					float swimAward = (float) ( 3 + swimLevel    / 10.00f ) * ( gap / 1000 );
					float flyAward  = (float) ( 1 + flyLevel     / 30.77f ) * ( gap / 1000 );
					float runAward  = (float) ( 1 + agilityLevel / 30.77f ) * ( gap / 1000 );
					
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
						player.addPotionEffect( new PotionEffect( MobEffects.NIGHT_VISION, 250, 0, true, false ) );
					}
//					else if( player.isPotionActive( MobEffects.NIGHT_VISION ) && player.getActivePotionEffect( MobEffects.NIGHT_VISION ).getIsAmbient() )
//						player.removePotionEffect( MobEffects.NIGHT_VISION );
					
					if( player.isSprinting() )
					{
						if( player.isInWater() && ( waterAbove || waterBelow ) )
							awardXp( player, "swimming", "swimming fast", swimAward * 1.25f, true );
						else
							awardXp( player, "agility", "running", runAward, true );
					}
					
					if( player.isInWater() && ( waterAbove || waterBelow ) )
						{
						if( !player.isSprinting() )
							awardXp( player, "swimming", "swimming", swimAward, true );
					}
					else if( player.isElytraFlying() )
						awardXp( player, "flying", "flying", flyAward, true );
					
					if( (player.getRidingEntity() instanceof EntityBoat) && player.isOverWater() )
						awardXp( player, "swimming", "swimming in a boat", swimAward / 5, true );
				}
			}
		}
	}
	
	public static void handleFished( ItemFishedEvent event )
	{
		EntityPlayer player = event.getEntityPlayer();
		NonNullList<ItemStack> items = event.getDrops();
		float award = getXp( items.get( 0 ).getItem().getRegistryName() );
		if( award == 0 )
			award = 10.0f;
		awardXp( player, "fishing", "catching " + items, award, false );
	}

	public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
	{
		EntityPlayer player = event.getEntityPlayer();
		NBTTagCompound skills = getSkillsTag( getPersistTag( player ) );

		int mining = 1;
		int woodcutting = 1;
		int excavation = 1;

		if( player.world.isRemote )
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

		switch( correctHarvestTool( event.getState().getMaterial() ) )
		{
			case "pickaxe":
				float height = event.getPos().y;
				if( height < 0 )
					height = -height;

				float heightMultiplier = 1 - ( height / 1000) ;

				if( heightMultiplier < 0.5f )
					heightMultiplier = 0.5f;

				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + mining * 0.01f ) * ( heightMultiplier ) );
				break;

			case "axe":
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + woodcutting * 0.01f ) );
				break;

			case "shovel":
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + excavation * 0.01f ) );
				break;

			default:
				event.setNewSpeed( event.getOriginalSpeed() );
				break;
		}
	}
	
	public static int levelAtXp( float xp )
	{
		int theXp = 0;
		
		for( int level = 0; ; level++ )
		{
			if( xp < theXp || level >= 999 )
			{
//				System.out.println( level );
				return level;
			}
			theXp += 250 + level * 50.00f;
		}
	}
	
	public static float levelAtXpDecimal( float xp )
	{
		if( levelAtXp( xp ) == 999 )
			xp = xpAtLevel( 999 );
		int startLevel = levelAtXp( xp );
		int startXp = xpAtLevel( startLevel );
		int goalXp = xpAtLevel( startLevel + 1 );
		
		if( startXp == goalXp )
			return 999.99f;
		else
			return startLevel + ( (xp - startXp) / (goalXp - startXp) );
	}
	
	public static int xpAtLevel( float givenLevel )
	{
		int theXp = 0;
		if( givenLevel > 999 )
			givenLevel = 999;
		
		for( int level = 1; level < givenLevel; level++ )
		{
			theXp += 250 + (level - 1) * 50.00f;
		}
		return theXp;
	}
	
	public static float xpAtLevelDecimal( float givenLevel )
	{
		float startXp = xpAtLevel( (float) Math.floor( givenLevel ) );
		float endXp   = xpAtLevel( (float) Math.floor( givenLevel + 1 ) );
		float pos = givenLevel - (float) Math.floor( givenLevel );
		
		return startXp + ( ( endXp - startXp ) * pos );
	}
}

