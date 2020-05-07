package harmonised.pmmo.skills;

import java.util.*;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.*;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class XP
{
	private static Map<Material, String> materialHarvestTool = new HashMap<>();
	private static Map<Skill, Integer> skillColors = new HashMap<>();
	private static Map<UUID, Long> lastAward = new HashMap<>();
	private static Map<UUID, BlockPos> lastPosPlaced = new HashMap<>();
	private static Map<UUID, String> lastBiome = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger();
	public static Set<UUID> isCrawling = new HashSet<>();
	public static Map<String, TextFormatting> skillTextFormat = new HashMap<>();
	public static Map<String, Style> textStyle = new HashMap<>();
	private static Set<UUID> lapisDonators = new HashSet<>();
	private static Set<UUID> dandelionDonators = new HashSet<>();
	private static Set<UUID> ironDonators = new HashSet<>();
	private static double globalMultiplier = Config.config.globalMultiplier.get();
	private static double deathXpPenaltyMultiplier = Config.config.deathXpPenaltyMultiplier.get();
	private static boolean showWelcome = Config.config.showWelcome.get();
    private static boolean showDonatorWelcome = Config.config.showDonatorWelcome.get();
    private static boolean broadcastMilestone = Config.config.broadcastMilestone.get();

	public static Map<String, Double> localConfig = new HashMap<>();
    public static Map<String, Double> config = new HashMap<>();

	public static void initValues()
	{
////////////////////////////////////COLOR_VALUES///////////////////////////////////////////////////

		localConfig.put( "baseXp", (double) Config.config.baseXp.get() );
		localConfig.put( "xpIncreasePerLevel", (double) Config.config.xpIncreasePerLevel.get() );
		localConfig.put( "maxLevel", (double) Config.config.maxLevel.get() );
		localConfig.put( "maxXp", xpAtLevel( Config.config.maxLevel.get() ) );
		localConfig.put( "biomePenaltyMultiplier", Config.config.biomePenaltyMultiplier.get() );
		localConfig.put( "nightvisionUnlockLevel", (double) Config.config.nightvisionUnlockLevel.get() );
		localConfig.put( "speedBoostPerLevel", Config.config.speedBoostPerLevel.get() );
		localConfig.put( "maxSpeedBoost", Config.config.maxSpeedBoost.get() );
		localConfig.put( "maxJumpBoost", Config.config.maxJumpBoost.get() );
		localConfig.put( "levelsCrouchJumpBoost", (double) Config.config.levelsCrouchJumpBoost.get() );
		localConfig.put( "levelsSprintJumpBoost", (double) Config.config.levelsSprintJumpBoost.get() );

		if( Config.config.crawlingAllowed.get() )
			localConfig.put( "crawlingAllowed", 1D );
		else
			localConfig.put( "crawlingAllowed", 0D );

////////////////////////////////////COLOR_VALUES///////////////////////////////////////////////////
		skillColors.put( Skill.MINING, 0x00ffff );
		skillColors.put( Skill.BUILDING, 0x00ffff );
		skillColors.put( Skill.EXCAVATION, 0xe69900 );
		skillColors.put( Skill.WOODCUTTING, 0xffa31a );
		skillColors.put( Skill.FARMING, 0x00e600 );
		skillColors.put( Skill.AGILITY, 0x66cc66 );
		skillColors.put( Skill.ENDURANCE, 0xcc0000 );
		skillColors.put( Skill.COMBAT, 0xff3300 );
		skillColors.put( Skill.ARCHERY, 0xffff00 );
		skillColors.put( Skill.SMITHING, 0xf0f0f0 );
		skillColors.put( Skill.FLYING, 0xccccff );
		skillColors.put( Skill.SWIMMING, 0x3366ff );
		skillColors.put( Skill.FISHING, 0x00ccff );
		skillColors.put( Skill.CRAFTING, 0xff9900 );
		skillColors.put( Skill.MAGIC, 0x0000ff );

		skillTextFormat.put( "mining", TextFormatting.AQUA );
		skillTextFormat.put( "building", TextFormatting.AQUA );
		skillTextFormat.put( "excavation", TextFormatting.GOLD );
		skillTextFormat.put( "woodcutting", TextFormatting.GOLD );
		skillTextFormat.put( "farming", TextFormatting.GREEN );
		skillTextFormat.put( "agility", TextFormatting.GREEN );
		skillTextFormat.put( "endurance", TextFormatting.DARK_RED );
		skillTextFormat.put( "combat", TextFormatting.RED);
		skillTextFormat.put( "archery", TextFormatting.YELLOW );
		skillTextFormat.put( "smithing", TextFormatting.GRAY );
		skillTextFormat.put( "flying", TextFormatting.GRAY );
		skillTextFormat.put( "swimming", TextFormatting.AQUA );
		skillTextFormat.put( "fishing", TextFormatting.AQUA );
		skillTextFormat.put( "crafting", TextFormatting.GOLD );
		skillTextFormat.put( "magic", TextFormatting.BLUE );
		skillTextFormat.put( "slayer", TextFormatting.DARK_GRAY );
		skillTextFormat.put( "fletching", TextFormatting.DARK_GREEN );
		skillTextFormat.put( "power", TextFormatting.AQUA );
////////////////////////////////////Style//////////////////////////////////////////////
		textStyle.put( "red", new Style().setColor( TextFormatting.RED ) );
		textStyle.put( "green", new Style().setColor( TextFormatting.GREEN ) );
		textStyle.put( "yellow", new Style().setColor( TextFormatting.YELLOW ) );
		textStyle.put( "grey", new Style().setColor( TextFormatting.GRAY ) );
////////////////////////////////////LAPIS_DONATORS//////////////////////////////////////////////
		lapisDonators.add( UUID.fromString( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" ) );
		dandelionDonators.add( UUID.fromString( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" ) );
		ironDonators.add( UUID.fromString( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" ) );
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
		materialHarvestTool.put( Material.MISCELLANEOUS, "pickaxe" );

		materialHarvestTool.put( Material.WOOD, "axe" );					//AXE
		materialHarvestTool.put( Material.LEAVES, "axe" );
		materialHarvestTool.put( Material.GOURD, "axe" );

		materialHarvestTool.put( Material.CLAY, "shovel" );					//SHOVEL
		materialHarvestTool.put( Material.EARTH, "shovel" );
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
		materialHarvestTool.put( Material.BAMBOO, "hoe" );
		materialHarvestTool.put( Material.BAMBOO_SAPLING, "hoe" );
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

	private static Map<String, Double> getXp( ResourceLocation registryName )
	{
		Map<String, Double> theMap = new HashMap<>();

		if( Requirements.data.get( "xpValue" ).containsKey( registryName.toString() ) )
		{
			for( Map.Entry<String, Object> entry : Requirements.data.get( "xpValue" ).get( registryName.toString() ).entrySet() )
			{
				if( entry.getValue() instanceof Double )
					theMap.put( entry.getKey(), (double) entry.getValue() );
			}
		}

		return theMap;
	}

    private static Map<String, Double> getXpCrafting( ResourceLocation registryName )
    {
        Map<String, Double> theMap = new HashMap<>();

        if( Requirements.data.get( "xpValueCrafting" ).containsKey( registryName.toString() ) )
        {
            for( Map.Entry<String, Object> entry : Requirements.data.get( "xpValueCrafting" ).get( registryName.toString() ).entrySet() )
            {
                if( entry.getValue() instanceof Double )
                    theMap.put( entry.getKey(), (double) entry.getValue() );
            }
        }

        return theMap;
    }

	public static Integer getSkillColor( Skill skill )
	{
		if( skillColors.get( skill ) != null )
			return skillColors.get( skill );
		else
            return 0xffffff;
	}

	public static String correctHarvestTool(Material material)
	{
		if( material == null )
			return "none";

		if( materialHarvestTool.get( material ) != null )
			return materialHarvestTool.get( material );
		else
			return "none";
	}

	public static String checkMaterial( Material material )
	{
		if( material.equals( Material.AIR ) )
			return "AIR";
		else if( material.equals( Material.STRUCTURE_VOID ) )
			return "STRUCTURE_VOID";
		else if( material.equals( Material.PORTAL ) )
			return "PORTAL";
		else if( material.equals( Material.CARPET ) )
			return "CARPET";
		else if( material.equals( Material.PLANTS ) )
			return "PLANTS";
		else if( material.equals( Material.OCEAN_PLANT ) )
			return "OCEAN_PLANT";
		else if( material.equals( Material.TALL_PLANTS ) )
			return "TALL_PLANTS";
		else if( material.equals( Material.SEA_GRASS ) )
			return "SEA_GRASS";
		else if( material.equals( Material.WATER ) )
			return "WATER";
		else if( material.equals( Material.BUBBLE_COLUMN ) )
			return "BUBBLE_COLUMN";
		else if( material.equals( Material.LAVA ) )
			return "LAVA";
		else if( material.equals( Material.SNOW ) )
			return "SNOW";
		else if( material.equals( Material.FIRE ) )
			return "FIRE";
		else if( material.equals( Material.MISCELLANEOUS ) )
			return "MISCELLANEOUS";
		else if( material.equals( Material.WEB ) )
			return "WEB";
		else if( material.equals( Material.REDSTONE_LIGHT ) )
			return "REDSTONE_LIGHT";
		else if( material.equals( Material.CLAY ) )
			return "CLAY";
		else if( material.equals( Material.EARTH ) )
			return "EARTH";
		else if( material.equals( Material.ORGANIC ) )
			return "ORGANIC";
		else if( material.equals( Material.PACKED_ICE ) )
			return "PACKED_ICE";
		else if( material.equals( Material.SAND ) )
			return "SAND";
		else if( material.equals( Material.SPONGE ) )
			return "SPONGE";
		else if( material.equals( Material.SHULKER ) )
			return "SHULKER";
		else if( material.equals( Material.WOOD ) )
			return "WOOD";
		else if( material.equals( Material.BAMBOO_SAPLING ) )
			return "BAMBOO_SAPLING";
		else if( material.equals( Material.BAMBOO ) )
			return "BAMBOO";
		else if( material.equals( Material.WOOL ) )
			return "WOOL";
		else if( material.equals( Material.TNT ) )
			return "TNT";
		else if( material.equals( Material.LEAVES ) )
			return "LEAVES";
		else if( material.equals( Material.GLASS ) )
			return "GLASS";
		else if( material.equals( Material.ICE ) )
			return "ICE";
		else if( material.equals( Material.CACTUS ) )
			return "CACTUS";
		else if( material.equals( Material.ROCK ) )
			return "ROCK";
		else if( material.equals( Material.IRON ) )
			return "IRON";
		else if( material.equals( Material.SNOW_BLOCK ) )
			return "SNOW_BLOCK";
		else if( material.equals( Material.ANVIL ) )
			return "ANVIL";
		else if( material.equals( Material.BARRIER ) )
			return "BARRIER";
		else if( material.equals( Material.PISTON ) )
			return "PISTON";
		else if( material.equals( Material.CORAL ) )
			return "CORAL";
		else if( material.equals( Material.GOURD ) )
			return "GOURD";
		else if( material.equals( Material.DRAGON_EGG ) )
			return "DRAGON_EGG";
		else if( material.equals( Material.CAKE ) )
			return "CAKE";
		else
			return "UNKNOWN";
	}

	public static double getConfig( String key )
	{
		if( config.containsKey( key ) )
			return config.get( key );
		else if( localConfig.containsKey( key ) )
			return localConfig.get( key );
		else
		{
			LOGGER.error( "UNABLE TO READ PMMO CONFIG \"" + key + "\" PLEASE REPORT" );
			return -1;
		}
	}

	public static void sendMessage( String msg, boolean bar, PlayerEntity player )
	{
		player.sendStatusMessage( new StringTextComponent( msg ), bar );
	}

	public static void sendMessage( String msg, boolean bar, PlayerEntity player, TextFormatting format )
	{
		player.sendStatusMessage( new StringTextComponent( msg ).setStyle( new Style().setColor( format ) ), bar );
	}

	public static Map<String, Double> multiplyMap( Map<String, Double> mapOne, double multiplier )
	{
		for( String key : mapOne.keySet() )
		{
			mapOne.replace( key, mapOne.get( key ) * multiplier );
		}

		return mapOne;
	}

	public static Map<String, Double> addMaps( Map<String, Double> mapOne, Map<String, Double> mapTwo )
	{
		for( String key : mapTwo.keySet() )
		{
			if( mapOne.containsKey( key ) )
				mapOne.replace( key, mapOne.get( key ) + mapTwo.get( key ) );
			else
				mapOne.put( key, mapTwo.get( key ) );

		}

		return mapOne;
	}

	public static void handlePlaced( EntityPlaceEvent event )
	{
		if( event.getEntity() instanceof PlayerEntity && !(event.getEntity() instanceof FakePlayer) )
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();

			if ( !player.isCreative() )
			{
				Block block = event.getPlacedBlock().getBlock();

				if( block.equals( Blocks.WATER ) )
				{
					awardXp( player, Skill.MAGIC, "Walking on water -gasp-", 0.075, true );
					return;
				}

				if( checkReq( player, block.getRegistryName(), "place" ) )
				{
					double blockHardnessLimit = Config.config.blockHardnessLimit.get();
					double blockHardness = block.getBlockHardness(block.getDefaultState(), event.getWorld(), event.getPos());
					if ( blockHardness > blockHardnessLimit )
						blockHardness = blockHardnessLimit;
					String playerName = player.getName().toString();
					BlockPos blockPos = event.getPos();
					UUID playerUUID = player.getUniqueID();

					if (!lastPosPlaced.containsKey(playerUUID) || !lastPosPlaced.get(playerUUID).equals(blockPos))
					{
						if (block.equals(Blocks.FARMLAND))
							awardXp(player, Skill.FARMING, "tilting dirt", blockHardness, false);
						else
							{
//								for( int i = 0; i < 1000; i++ )
//							{
								awardXp(player, Skill.BUILDING, "placing a block", blockHardness, false);
//							}
						}
					}

					if (lastPosPlaced.containsKey(playerName))
						lastPosPlaced.replace(playerUUID, event.getPos());
					else
						lastPosPlaced.put(playerUUID, blockPos);

					if ( getXp(block.getRegistryName()) != null )
						PlacedBlocks.orePlaced(event.getWorld().getWorld(), event.getPos());
				}
				else
				{
					ItemStack mainItemStack = player.getHeldItemMainhand();
					ItemStack offItemStack = player.getHeldItemOffhand();

					if( mainItemStack.getItem() instanceof BlockItem )
						NetworkHandler.sendToPlayer( new MessageGrow( 0, mainItemStack.getCount() ), (ServerPlayerEntity) player );
					if( offItemStack.getItem() instanceof BlockItem )
						NetworkHandler.sendToPlayer( new MessageGrow( 1, offItemStack.getCount() ), (ServerPlayerEntity) player );

					if( Requirements.data.get( "plantInfo" ).containsKey( block.getRegistryName().toString() ) || block instanceof IPlantable )
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toPlant", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
					else
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toPlaceDown", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );

					event.setCanceled( true );
				}
			}
		}
	}

	public static int getLevel( Skill skill, PlayerEntity player )
	{
		return levelAtXp( getXp( skill, player ) );
	}

	public static double getXp( Skill skill, PlayerEntity player )
	{
		double startXp = 0;

		if( player.world.isRemote() )
		{
			if( XPOverlayGUI.skills.containsKey( skill ) )
				startXp = XPOverlayGUI.skills.get( skill ).goalXp;
		}
		else
			startXp = getSkillsTag( player ).getDouble( skill.name().toLowerCase() );

		return startXp;
	}

	public static double getLevelDecimal( String skill, PlayerEntity player )
	{
		double startLevel = 1;

		if( player.world.isRemote() )
		{
			if( XPOverlayGUI.skills.containsKey( skill ) )
				startLevel = levelAtXpDecimal( XPOverlayGUI.skills.get( skill ).goalXp );
		}
		else
			startLevel = levelAtXpDecimal( getSkillsTag( player ).getDouble( skill ) );

		return startLevel;
	}

	private static int doubleObjectToInt( Object object )
	{
		return (int) Math.floor( (double) object );
	}

	public static double getExtraChance( PlayerEntity player, ResourceLocation resLoc, String type )
	{
		String regKey = resLoc.toString();
		double extraChancePerLevel = 0;
		double extraChance;
		int highestReq = 1;
		if( Requirements.data.get( "breakReq" ).containsKey( resLoc.toString() ) )
			highestReq = Requirements.data.get( "breakReq" ).get( resLoc.toString() ).entrySet().stream().map( a -> doubleObjectToInt( a.getValue() ) ).reduce( 0, Math::max );
		int startLevel = 1;

		switch( type )
		{
			case "ore":
				if( Requirements.data.get( "oreInfo" ).containsKey( regKey ) && Requirements.data.get( "oreInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( Requirements.data.get( "oreInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) Requirements.data.get( "oreInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.MINING, player );
				break;

			case "log":
				if( Requirements.data.get( "logInfo" ).containsKey( regKey ) && Requirements.data.get( "logInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( Requirements.data.get( "logInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) Requirements.data.get( "logInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.WOODCUTTING, player );
				break;

			case "plant":
				if( Requirements.data.get( "plantInfo" ).containsKey( regKey ) && Requirements.data.get( "plantInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( Requirements.data.get( "plantInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) Requirements.data.get( "plantInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.FARMING, player );
				break;

			default:
				System.out.println( "WRONG EXTRA CHANCE TYPE! PLEASE REPORT!" );
				return 0;
		}

		extraChance = (startLevel - highestReq) * extraChancePerLevel;
		if( extraChance < 0 )
			extraChance = 0;

		return extraChance;
	}

	private static void dropItems( int dropsLeft, Item item, World world, BlockPos pos )
	{
		if( dropsLeft > 0 )
		{
			while( dropsLeft > 64 )
			{
				Block.spawnAsEntity( world, pos, new ItemStack( item, 64 ) );
				dropsLeft -= 64;
			}
			Block.spawnAsEntity( world, pos, new ItemStack( item, dropsLeft ) );
		}
	}

	private static void dropItemStack( ItemStack itemStack, World world, BlockPos pos )
	{
		Block.spawnAsEntity( world, pos, itemStack );
	}

	public static void handleBroken( BreakEvent event )
	{
		if( event.getPlayer() instanceof PlayerEntity && !(event.getPlayer() instanceof FakePlayer) )
		{
			PlayerEntity player = event.getPlayer();

			if( !player.isCreative() )
			{
				BlockState state = event.getState();
				Block block = state.getBlock();
				World world = event.getWorld().getWorld();
				Material material = event.getState().getMaterial();

				Block blockAbove = world.getBlockState( event.getPos().up() ).getBlock();
				boolean passedBreakReq = false;

				if( Requirements.data.get( "plantInfo" ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable );
					passedBreakReq = checkReq( player, blockAbove.getRegistryName(), "break" );

				if( !passedBreakReq )
					block = blockAbove;
				else
					passedBreakReq = checkReq( player, block.getRegistryName(), "break" );

				if( passedBreakReq )
				{
					double blockHardnessLimit = Config.config.blockHardnessLimit.get();
					boolean wasPlaced = PlacedBlocks.isPlayerPlaced( event.getWorld().getWorld(), event.getPos() );
					ItemStack toolUsed = player.getHeldItemMainhand();
					String skill = getSkill( correctHarvestTool( material ) ).name().toLowerCase();
//					String regKey = block.getRegistryName().toString();
					double hardness = block.getBlockHardness( block.getDefaultState(), event.getWorld(), event.getPos() );
					if( hardness > blockHardnessLimit )
						hardness = blockHardnessLimit;

					String awardMsg = "";
					Map<String, Double> award = new HashMap<>();
					award.put( skill, hardness );

					Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
					int fortune = 0;
					if( enchants.get( Enchantments.FORTUNE ) != null )
						fortune = enchants.get( Enchantments.FORTUNE );

					List<ItemStack> drops;

					if( world instanceof ServerWorld )
					{
						LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
								.withRandom(world.rand)
								.withParameter( LootParameters.POSITION, event.getPos() )
								.withParameter( LootParameters.TOOL, toolUsed )
								.withParameter( LootParameters.THIS_ENTITY, player )
								.withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
						if (fortune > 0)
						{
							builder.withLuck(fortune);
						}
						drops = block.getDrops( event.getState(), builder );
					}
					else
						drops = new ArrayList<>();

					Block sugarCane = Blocks.SUGAR_CANE;
					Block cactus = Blocks.CACTUS;
					Block kelp = Blocks.KELP_PLANT;
					Block bamboo = Blocks.BAMBOO;

					if( block.equals( sugarCane ) || block.equals( cactus ) || block.equals( kelp ) || block.equals( bamboo ) ) //Handle Sugar Cane / Cactus
					{
						Block baseBlock = event.getState().getBlock();
						BlockPos baseBlockPos = event.getPos();

						double extraChance = XP.getExtraChance( player, block.getRegistryName(), "plant" ) / 100;
						int rewardable, guaranteedDrop, extraDrop, totalDrops, guaranteedDropEach;
						rewardable = extraDrop = guaranteedDrop = totalDrops = 0;

						guaranteedDropEach = (int) Math.floor( extraChance );
						extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;

						int height = 0;
						BlockPos currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
						block =  world.getBlockState( currBlockPos ).getBlock();
						for( ; ( block.equals( baseBlock ) ); )
						{
							wasPlaced = PlacedBlocks.isPlayerPlaced( world, currBlockPos );
							if( !wasPlaced )
							{
								rewardable++;
								guaranteedDrop += guaranteedDropEach;

								if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
									extraDrop++;
							}
							height++;
							currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
							block =  world.getBlockState( currBlockPos ).getBlock();
						}

						int dropsLeft = guaranteedDrop + extraDrop;

						if( dropsLeft > 0 )
						{
							dropItems( dropsLeft, block.asItem(), world, event.getPos() );
							NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.extraDrop", "" + dropsLeft, drops.get( 0 ).getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
						}

						totalDrops = rewardable + dropsLeft;
						award = addMaps( award, multiplyMap( getXp( baseBlock.getRegistryName() ), totalDrops ) );

						awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra";
					}
					else if( ( material.equals( Material.PLANTS ) || material.equals( Material.OCEAN_PLANT ) || material.equals( Material.TALL_PLANTS ) ) && drops.size() > 0 ) //IS PLANT
					{
						ItemStack theDropItem = drops.get( 0 );

						int age = -1;
						int maxAge = -1;

						if( !wasPlaced )
							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), theDropItem.getCount() ) );

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
							award = addMaps( award, getXp( block.getRegistryName() ) );

							double extraChance = XP.getExtraChance( player, block.getRegistryName(), "plant" ) / 100;
							int guaranteedDrop = 0;
							int extraDrop = 0;

							guaranteedDrop = (int) Math.floor( extraChance );
							extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;

							if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
								extraDrop = 1;

							if( guaranteedDrop + extraDrop > 0 )
							{
								dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
							}

							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), guaranteedDrop + extraDrop ) );
							awardMsg = "harvesting " + ( theDropItem.getCount() ) + " + " + ( guaranteedDrop + extraDrop ) + " crops";
						}
						else if( !wasPlaced )
							awardMsg = "breaking a plant";
					}
					else if( XP.getExtraChance( player, block.getRegistryName(), "ore" ) > 0 )		//IS ORE
					{
						boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;
						boolean noDropOre = false;
						if( drops.size() > 0 )
							noDropOre = block.asItem().equals( drops.get(0).getItem() );

						if( !wasPlaced && !isSilk )
							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), drops.get( 0 ).getCount() ) );

						if( noDropOre && !wasPlaced || !noDropOre && !isSilk )			//EXTRA DROPS
						{
							double extraChance = XP.getExtraChance( player, block.getRegistryName(), "ore" ) / 100;

							int guaranteedDrop = 0;
							int extraDrop = 0;

							guaranteedDrop = (int)Math.floor( extraChance );
							extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;


							if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
								extraDrop = 1;

							if( !noDropOre && wasPlaced )
								award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), ( drops.get( 0 ).getCount() ) ) );

							awardMsg = "mining a block";

							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), ( guaranteedDrop + extraDrop ) ) );
							if( guaranteedDrop + extraDrop > 0 )
							{
								dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
							}
						}
						else
							awardMsg = "mining a block";
					}
					else if( XP.getExtraChance( player, block.getRegistryName(), "log" ) > 0 )
					{
						if( !wasPlaced )			//EXTRA DROPS
						{
							double extraChance = XP.getExtraChance( player, block.getRegistryName(), "log" ) / 100;

							int guaranteedDrop = 0;
							int extraDrop = 0;

							if( ( extraChance ) > 1 )
							{
								guaranteedDrop = (int)Math.floor( extraChance );
								extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;
							}

							extraChance *= 100;

							if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
								extraDrop = 1;

							if( guaranteedDrop + extraDrop > 0 )
							{
								dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
							}

							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), ( drops.get( 0 ).getCount() + guaranteedDrop + extraDrop ) ) );

							awardMsg = "cutting a block";
						}
						else
							awardMsg = "cutting a block";
					}
					else
					{
						if( !wasPlaced )
						{
							award = addMaps( award, multiplyMap( getXp( block.getRegistryName() ), drops.size() ) );
							PlacedBlocks.removeOre( event.getWorld().getWorld(), event.getPos() );
						}

						switch( getSkill( correctHarvestTool( material ) ) )
						{
							case MINING:
								awardMsg = "mining a block";
								break;

							case WOODCUTTING:
								awardMsg = "cutting a block";
								break;

							case EXCAVATION:
								awardMsg = "digging a block";
								break;

							case FARMING:
								awardMsg = "harvesting";
								break;

							default:
//								System.out.println( "INVALID SKILL ON BREAK" );
								break;
						}
					}

					int gap = getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), "tool" );

					if( gap > 0 )
						player.getHeldItemMainhand().damageItem( gap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );

					for( String skillName : award.keySet() )
					{
						awardXp( player, Skill.getSkill( skillName ), awardMsg, award.get( skillName ) / (gap + 1), false );
					}
				}
				else
				{
					int startLevel;

					if( correctHarvestTool( material ).equals( "axe" ) )
					{
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toChop", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toChop", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
					}
					else if( Requirements.data.get( "plantInfo" ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable )
					{
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toHarvest", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toHarvest", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
					}
					else
					{
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toBreak", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toBreak", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
					}

					for( Map.Entry<String, Object> entry : Requirements.data.get( "breakReq" ).get( block.getRegistryName().toString() ).entrySet() )
					{
						startLevel = getLevel( Skill.getSkill( entry.getKey() ), player );

						double entryValue = 1;
						if( entry.getValue() instanceof Double )
							entryValue = (double) entry.getValue();

						if( startLevel < entryValue )
							NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.levelDisplay", "pmmo.text." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 2 ), (ServerPlayerEntity) player );
						else
							NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.levelDisplay", "pmmo.text." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 1 ), (ServerPlayerEntity) player );
					}

					event.setCanceled( true );
				}
			}
		}
	}

	public static void handleDamage( LivingDamageEvent event )
	{
		if( !(event.getEntityLiving() instanceof FakePlayer || event.getEntity() instanceof FakePlayer) )
		{
			float damage = event.getAmount();
			float startDmg = damage;
			LivingEntity target = event.getEntityLiving();
			if( target instanceof PlayerEntity )		//player hurt
			{
				PlayerEntity player = (PlayerEntity) target;
				CompoundNBT skillsTag = getSkillsTag( player );
				double agilityXp = 0;
				double enduranceXp = 0;
				boolean hideEndurance = false;

///////////////////////////////////////////////////////////////////////ENDURANCE//////////////////////////////////////////////////////////////////////////////////////////
				int enduranceLevel = levelAtXp( skillsTag.getDouble( "endurance" ) );
				double endurancePerLevel = Config.config.endurancePerLevel.get();
				double maxEndurance = Config.config.maxEndurance.get();
				double endurePercent = (enduranceLevel * endurancePerLevel);
				if( endurePercent > maxEndurance )
					endurePercent = maxEndurance;
				endurePercent /= 100;

				double endured = damage * endurePercent;
				if( endured < 0 )
					endured = 0;

				damage -= endured;

				enduranceXp = ( damage * 5 ) + ( endured * 7.5 );
///////////////////////////////////////////////////////////////////////FALL//////////////////////////////////////////////////////////////////////////////////////////////
				if( event.getSource().getDamageType().equals( "fall" ) )
				{
					double award = startDmg;
//					float savedExtra = 0;
					int agilityLevel = levelAtXp( skillsTag.getDouble( "agility" ) );
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
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.savedFall", saved ), true );

					award = saved * 25;

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

///////////////////////////////////////Attacking////////////////////////////////////////////////////////////

			if ( target instanceof LivingEntity && event.getSource().getTrueSource() instanceof PlayerEntity )
			{
//				IAttributeInstance test = target.getAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
//				if( !(target instanceof AnimalEntity) )
//					System.out.println( test.getValue() + " " + test.getBaseValue() );

				PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();

				if( player.getHeldItemMainhand().getItem().equals( Items.DEBUG_STICK ) )
					player.sendStatusMessage( new StringTextComponent( target.getEntityString() ), false );

				if( !player.isCreative() )
				{
					int weaponGap = getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), "weapon" );
					if( weaponGap > 0 )
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.toUseAsWeapon", player.getHeldItemMainhand().getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );

					int slayerGap = getSkillReqGap( player, new ResourceLocation( target.getEntityString() ), "slayer" );
					if( slayerGap > 0 )
					{
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toDamage", new TranslationTextComponent( target.getType().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toDamage", new TranslationTextComponent( target.getType().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );

						for( Map.Entry<String, Object> entry : Requirements.data.get( "killReq" ).get( target.getEntityString() ).entrySet() )
						{
							int level = getLevel( Skill.getSkill( entry.getKey() ), player );

							if( level < (double) entry.getValue() )
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + entry.getKey() ), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "red" ) ), false );
							else
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + entry.getKey() ), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "green" ) ), false );
						}
					}

					event.setAmount( event.getAmount() / (weaponGap + 1) / (slayerGap + 1) );
					damage = event.getAmount();

					float amount = 0;
					float playerHealth = player.getHealth();
					float targetHealth = target.getHealth();
					float targetMaxHealth = target.getMaxHealth();
					float lowHpBonus = 1.0f;

					if( damage > targetHealth )		//no overkill xp
					{
						double normalMaxHp = target.getAttribute( SharedMonsterAttributes.MAX_HEALTH ).getBaseValue();
						damage = targetHealth;
						double scaleMultiplier = ( 1 + ( target.getMaxHealth() - normalMaxHp ) / 10 );

						if( Requirements.data.get( "killXp" ).containsKey( target.getEntityString() ) )
						{
							Map<String, Object> killXp = Requirements.data.get( "killXp" ).get( target.getEntityString() );
							for( Map.Entry<String, Object> entry : killXp.entrySet() )
							{
								awardXp( player, Skill.getSkill( entry.getKey() ), player.getHeldItemMainhand().getDisplayName().toString(), (double) entry.getValue() * scaleMultiplier, false );
							}
 						}
						else if( target instanceof AnimalEntity )
							awardXp( player, Skill.SLAYER, player.getHeldItemMainhand().getDisplayName().toString(), 1D * scaleMultiplier, false );
						else if( target instanceof MobEntity )
                            awardXp( player, Skill.SLAYER, player.getHeldItemMainhand().getDisplayName().toString(), 3D * scaleMultiplier, false );

//						System.out.println( ( target.getMaxHealth() - normalMaxHp ) / 10 );

						if( Requirements.data.get( "mobRareDrop" ).containsKey( target.getEntityString() ) )
						{
							Map<String, Object> dropTable = Requirements.data.get( "mobRareDrop" ).get( target.getEntityString() );

							for( Map.Entry<String, Object> entry : dropTable.entrySet() )
							{
								if( Math.floor( Math.random() * (double) entry.getValue() ) == 0 )
								{
									ItemStack itemStack = new ItemStack( getItem( entry.getKey() ) );
									dropItemStack( itemStack, player.world, target.getPosition() );

									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( textStyle.get( "green" ) ), false );
									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( textStyle.get( "green" ) ), true );
								}
							}
						}
					}

					amount += damage * 3;

					if ( startDmg >= targetHealth )	//kill reduce xp
						amount /= 2;

					if( startDmg >= targetMaxHealth )	//max hp kill reduce xp
						amount /= 1.5;

//					player.setHealth( 1f );

					if( target instanceof AnimalEntity )		//reduce xp if passive mob
						amount /= 2;
					else if( playerHealth <= 10 )				//if aggresive mob and low hp
					{
						lowHpBonus += ( 11 - playerHealth ) / 5;
						if( playerHealth <= 2 )
							lowHpBonus += 1;
					}

					if( event.getSource().damageType.equals( "arrow" ) )
					{
						double distance = event.getEntity().getDistance( player );
						if( distance > 16 )
							distance -= 16;
						else
							distance = 0;

						amount += ( Math.pow( distance, 1.25 ) * ( damage / target.getMaxHealth() ) * ( damage >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp
						amount *= lowHpBonus;

						awardXp( player, Skill.ARCHERY, player.getHeldItemMainhand().getDisplayName().toString(), amount, false );
					}
					else
					{
						amount *= lowHpBonus;
						awardXp( player, Skill.COMBAT, player.getHeldItemMainhand().getDisplayName().toString(), amount, false );
					}

					if( weaponGap > 0 )
						player.getHeldItemMainhand().damageItem( weaponGap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );
				}
			}
		}
	}

	public static void handleJump( LivingJumpEvent event )
	{
		if( event.getEntityLiving() instanceof PlayerEntity && !(event.getEntityLiving() instanceof FakePlayer) )
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();

//			if( !player.world.isRemote )
//				System.out.println( player.getPersistentData() );

			if( !player.isCreative() )
			{
				CompoundNBT prefsTag = getPreferencesTag(player);

				double agilityLevel = 1;
				double jumpBoost = 0;
				double maxJumpBoost = getConfig( "maxJumpBoost" );
				double maxJumpBoostPref = maxJumpBoost;
				int levelsCrouchJumpBoost = (int) Math.floor( getConfig( "levelsCrouchJumpBoost" ) );
				int levelsSprintJumpBoost = (int) Math.floor( getConfig( "levelsSprintJumpBoost" ) );

				agilityLevel = getLevel( Skill.AGILITY, player );

				if (player.isCrouching())
				{
					if( prefsTag.contains( "maxCrouchJumpBoost" ) )
						maxJumpBoostPref = 0.14 * (prefsTag.getDouble( "maxCrouchJumpBoost" ) / 100);
					jumpBoost = -0.011 + agilityLevel * ( 0.14 / levelsCrouchJumpBoost );
				}

				if (player.isSprinting())
				{
					if( prefsTag.contains( "maxSprintJumpBoost" ) )
						maxJumpBoostPref = 0.14 * (prefsTag.getDouble( "maxSprintJumpBoost" ) / 100);
					jumpBoost = -0.013 + agilityLevel * ( 0.14 / levelsSprintJumpBoost );
				}

				if ( jumpBoost > maxJumpBoost )
					jumpBoost = maxJumpBoost;
				if( jumpBoost > maxJumpBoostPref )
					jumpBoost = maxJumpBoostPref;

				if (player.world.isRemote)
				{
					if( jumpBoost > 0 )
						player.setMotion( player.getMotion().x, player.getMotion().y + jumpBoost, player.getMotion().z );
				}
				else if (!player.isInWater())
				{
					float jumpAmp = 0;

					if( player.isPotionActive( Effects.JUMP_BOOST ) )
						jumpAmp = player.getActivePotionEffect( Effects.JUMP_BOOST ).getAmplifier() + 1;

					awardXp( player, Skill.AGILITY, "jumping", (jumpBoost * 10 + 1) * ( 1 + jumpAmp / 4 ), true );
				}
			}
		}
	}

	public static void handleLivingDeath( LivingDeathEvent event )
	{
		if( event.getEntity() instanceof PlayerEntity && !(event.getEntity() instanceof FakePlayer) )
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if( !player.world.isRemote() )
			{
				CompoundNBT skillsTag = getSkillsTag( player );

				for( String key : skillsTag.keySet() )
				{
					double startXp = skillsTag.getDouble( key );
					double floorXp = xpAtLevelDecimal( Math.floor( levelAtXpDecimal( startXp ) ) );
					double diffXp = startXp - floorXp;
					double finalXp = floorXp + diffXp * (1 - deathXpPenaltyMultiplier);

					skillsTag.putDouble( key, finalXp );
					NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getDouble( key ), Skill.getInt( key ), 0, true ), (ServerPlayerEntity) player );
				}
			}
		}
	}

	public static void handleLivingSpawn( LivingSpawnEvent.EnteringChunk event )
	{
		if( event.getEntity() instanceof MobEntity && !(event.getEntity() instanceof AnimalEntity) )
		{
			MobEntity mob = (MobEntity) event.getEntity();
			MinecraftServer server = mob.getServer();
			if( server != null )
			{
				Collection<ServerPlayerEntity> allPlayers = server.getPlayerList().getPlayers();

				Float closestDistance = null;
				float tempDistance;

				for( ServerPlayerEntity player : allPlayers )
				{
					tempDistance = mob.getDistance( player );
					if( closestDistance == null || tempDistance < closestDistance )
						closestDistance = tempDistance;
				}

				if( closestDistance != null )
				{
					float searchRange = closestDistance + 30;
					float powerLevel = 0;
					float playerPowerAverage = 1;

					Collection<ServerPlayerEntity> players = new ArrayList<>();

					for( ServerPlayerEntity player : allPlayers )
					{
						if( mob.getDistance( player ) < searchRange )
							players.add( player );
					}

					for( ServerPlayerEntity player : players )
					{
						powerLevel += getPowerLevel( player );
					}
					powerLevel /= players.size();
					playerPowerAverage = (float) Math.pow(0.5, players.size() - 1 );

					if( playerPowerAverage < 1 )
						powerLevel *= 1 + playerPowerAverage;

					AttributeHandler.updateHP( mob, powerLevel );
					AttributeHandler.updateDamage( mob, powerLevel );
					AttributeHandler.updateSpeed( mob, powerLevel );
				}
			}
		}
	}

	public static float getPowerLevel( PlayerEntity player )
    {
		int powerLevel = getLevel( Skill.ENDURANCE, player );

		int combatLevel = getLevel( Skill.COMBAT, player );
		int archeryLevel = getLevel( Skill.ARCHERY, player );
		int magicLevel = getLevel( Skill.MAGIC, player );

		int maxOffensive = combatLevel;
		if( maxOffensive < archeryLevel )
			maxOffensive = archeryLevel;
		if( maxOffensive < magicLevel )
			maxOffensive = magicLevel;

		return ( powerLevel + (maxOffensive * 1.5f) ) / 50;
    }

	public static void handlePlayerRespawn( PlayerEvent.PlayerRespawnEvent event )
	{
		PlayerEntity player = event.getPlayer();

		AttributeHandler.updateAll( player );
	}

	public static void handleClone( PlayerEvent.Clone event )
	{
		event.getPlayer().getPersistentData().put( "pmmo", event.getOriginal().getPersistentData().getCompound( "pmmo" ) );
	}

	public static void syncPlayerConfig( PlayerEntity player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdateReq( Requirements.localData, "json" ), (ServerPlayerEntity) player );

		NetworkHandler.sendToPlayer( new MessageUpdateNBT( NBTHelper.mapToNBT( localConfig ), "config" ), (ServerPlayerEntity) player );
	}

	public static void syncPlayer( PlayerEntity player )
    {
        CompoundNBT skillsTag = getSkillsTag( player );
        CompoundNBT prefsTag = getPreferencesTag( player );
        Set<String> keySet = new HashSet<>( skillsTag.keySet() );

		syncPlayerConfig( player );

        NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0f, true ), (ServerPlayerEntity) player );
        NetworkHandler.sendToPlayer( new MessageUpdateNBT( prefsTag, "prefs" ), (ServerPlayerEntity) player );
        AttributeHandler.updateAll( player );

        for( String tag : keySet )
        {
            if( Skill.getInt( tag ) == 0 )
            {
                if( Skill.getInt( tag.toLowerCase() ) != 0 )
                    skillsTag.put( tag.toLowerCase(), skillsTag.get(tag) );

                if( tag.toLowerCase().equals( "repairing" ) )
                    skillsTag.put( "smithing", skillsTag.get(tag) );

                System.out.println( "REMOVING INVALID SKILL " + tag );
                skillsTag.remove( tag );
            }
        }

        keySet = new HashSet<>( skillsTag.keySet() );

        for( String tag : keySet )
        {
            NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getDouble( tag ), Skill.getInt( tag ), 0, true ), (ServerPlayerEntity) player );
        }
    }

	public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
	{
		PlayerEntity player = event.getPlayer();
		if( !player.world.isRemote() )
		{
			syncPlayer( player );

            if( lapisDonators.contains( player.getUniqueID() ) )
            {
                player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                {
                    thePlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.text.lapisDonatorWelcome", thePlayer.getDisplayName().getString() ).setStyle( new Style().setColor( TextFormatting.BLUE ) ), false );
                });
            }
            else if( showDonatorWelcome )
            {
                if( dandelionDonators.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.dandelionDonatorWelcome", player.getDisplayName().getString() ).setStyle( textStyle.get( "yellow" ) ), false );
                else if( ironDonators.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.ironDonatorWelcome", player.getDisplayName().getString() ).setStyle( textStyle.get( "grey" ) ), false );
            }

            if( showWelcome )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.welcome" ), false );
		}
	}

	public static boolean checkReq( PlayerEntity player, ResourceLocation res, String type )
	{
		if( res.equals( Items.AIR.getRegistryName() ) )
			return true;

		String registryName = res.toString();
		Map<String, Double> reqMap = new HashMap<>();
		int startLevel;
		boolean failedReq = false;

		switch( type.toLowerCase() )
		{
			case "wear":
				if( Requirements.data.get( "wearReq" ).containsKey( registryName ) )
				{
					for( Map.Entry<String, Object> entry : Requirements.data.get( "wearReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				}
				break;

			case "tool":
				if( Requirements.data.get( "toolReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "toolReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "weapon":
				if( Requirements.data.get( "weaponReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "weaponReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

            case "mob":
                if( Requirements.data.get( "mobReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "mobReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
                break;

            case "use":
                if( Requirements.data.get( "useReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "useReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
                break;

            case "place":
				if( Requirements.data.get( "placeReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "placeReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "break":
				if( Requirements.data.get( "breakReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "breakReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "biome":
				if( Requirements.data.get( "biomeReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : Requirements.data.get( "biomeReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			default:
				failedReq = true;
				break;
		}

		if( !failedReq )
		{
			for( Map.Entry<String, Double> entry : reqMap.entrySet() )
			{
				startLevel = getLevel( Skill.getSkill( entry.getKey() ), player );

				if( startLevel < entry.getValue() )
					failedReq = true;
			}
		}

		return !failedReq;
	}

	public static Item getItem( String resLoc )
	{
		if( resLoc != null )
		{
			Item item = ForgeRegistries.ITEMS.getValue( new ResourceLocation( resLoc ) );
			Item item2 = ForgeRegistries.BLOCKS.getValue( new ResourceLocation( resLoc ) ).asItem();
			if( !item.equals( Items.AIR ) )
				return item;
			else if( !item2.equals( Items.AIR ) )
				return item2;
			else
				return Items.AIR;
		}
		else
			return Items.AIR;
	}

	public static Item getItem( ResourceLocation resLoc )
	{
		return ForgeRegistries.ITEMS.getValue( resLoc );
	}

	public static boolean scanBlock( Block block, int radius, PlayerEntity player )
	{
		Block currBlock;
		BlockPos playerPos = player.getPosition();
		boolean matched = false;

		for( int x = -radius; x <= radius; x++ )
		{
			for( int y = -radius; y <= radius; y++ )
			{
				for( int z = -radius; z <= radius; z++ )
				{
					currBlock = player.world.getBlockState( new BlockPos( playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z ) ).getBlock();
					if( currBlock.equals( block ) )
					{
						matched = true;
						break;
					}
				}
			}
		}

		return matched;
	}

	public static void handleItemUse( PlayerInteractEvent event )
	{
		if( event instanceof RightClickBlock || event instanceof RightClickItem )
		{
			PlayerEntity player = event.getPlayer();
			ItemStack itemStack = event.getItemStack();
			Item item = itemStack.getItem();
			Block goldBlock 	= 	Blocks.GOLD_BLOCK;
			Block smithBlock    =   Blocks.SMITHING_TABLE;
			String regKey = item.getRegistryName().toString();
			int startLevel;
			boolean isRemote = player.world.isRemote();
			boolean matched;

            if( event instanceof RightClickItem )
			{
				if( !player.isCreative() )
				{
					if( Requirements.data.get( "salvageInfo" ).containsKey( regKey ) )
					{
						matched = scanBlock( smithBlock, 1, player );
						if( !matched )
							matched = scanBlock( goldBlock, 1, player );

						if( matched )
						{
							event.setCanceled( true );

//							if( isRemote )
//								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.cannotUseProximity", new TranslationTextComponent( matchedBlock.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
						}
					}
				}
			}

			if( !checkReq( player, item.getRegistryName(), "use" ) && !(item instanceof BlockItem) )
			{
				event.setCanceled( true );

				if( isRemote )
					player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toUse", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
			}

			if( event instanceof RightClickBlock )
			{
                Block block = player.world.getBlockState( event.getPos() ).getBlock();

                if( !checkReq( player, block.getRegistryName(), "use" ) )
                {
                	if( !player.isCreative() )
					{
                    	event.setCanceled( true );
                    	if( isRemote && event.getHand().equals( Hand.MAIN_HAND ) )
						{
							player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
							player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
							for( Map.Entry<String, Object> entry : Requirements.data.get( "useReq" ).get( block.getRegistryName().toString() ).entrySet() )
							{
								startLevel = getLevel( Skill.getSkill( entry.getKey() ), player );

								double entryValue = 1;
								if( entry.getValue() instanceof Double )
									entryValue = (double) entry.getValue();

								if( startLevel < entryValue )
									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + entry.getKey() ), "" + (int) Math.floor( entryValue ) ).setStyle( textStyle.get( "red" ) ), false );
								else
									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", new TranslationTextComponent( "pmmo.text." + entry.getKey() ), "" + (int) Math.floor( entryValue ) ).setStyle( textStyle.get( "green" ) ), false );
							}
						}
                    }
                }
                else
                {
                	event.setCanceled( false );

				    Block anvil 		=	Blocks.ANVIL;
				    Block ironBlock		= 	Blocks.IRON_BLOCK;
				    Block diamondBlock 	= 	Blocks.DIAMOND_BLOCK;

				    int smithingLevel = getLevel( Skill.SMITHING, player );
				    int maxEnchantmentBypass = Config.config.maxEnchantmentBypass.get();
				    int levelsPerOneEnchantBypass = Config.config.levelsPerOneEnchantBypass.get();
				    int maxPlayerBypass = (int) Math.floor( (double) smithingLevel / (double) levelsPerOneEnchantBypass );
				    if( maxPlayerBypass > maxEnchantmentBypass )
				    	maxPlayerBypass = maxEnchantmentBypass;

				    if( player.isCrouching() )
				    {
				    	if( block.equals( ironBlock ) || block.equals( anvil ) )
				    	{
				    		if( event.getHand() == Hand.MAIN_HAND )
				    		{
				    			//Outdated, Replaced by Tooltip
				    		}
				    		else
				    			return;
				    	}

				    	if( ( block.equals( goldBlock ) || block.equals( smithBlock ) ) )
				    	{
							if( Requirements.data.get( "salvageInfo" ).containsKey( regKey ) )
								event.setCanceled( true );

				    		if( isRemote )
				    			return;

				    		if( event.getHand().equals( Hand.OFF_HAND ) )
							{
				    			itemStack = player.getHeldItemOffhand();
				    			item = itemStack.getItem();

				    			if( !item.equals( Items.AIR ) )
								{
				    			if( Requirements.data.get( "salvageInfo" ).containsKey( regKey ) )
				    			{
				    				if( player.getPosition().withinDistance( event.getPos(), 2 ) )
									{
										Map<String, Object> theMap = Requirements.data.get( "salvageInfo" ).get( regKey );
										Item salvageItem = getItem( (String) theMap.get( "salvageItem" ) );
										if( !salvageItem.equals( Items.AIR ) )
										{
											Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( itemStack );
				    						double baseChance = (double) theMap.get( "baseChance" );
											double chancePerLevel = (double) theMap.get( "chancePerLevel" );
											double maxSalvageMaterialChance = (double) theMap.get( "maxChance" );
											int reqLevel = (int) Math.floor( (double) theMap.get( "levelReq" ) );
											int salvageMax = (int) Math.floor( (double) theMap.get( "salvageMax" ) );
											smithingLevel -= reqLevel;
											if( smithingLevel >= 0 )
											{
												double chance = baseChance + ( chancePerLevel * smithingLevel );
				    							double maxSalvageEnchantChance = Config.config.maxSalvageEnchantChance.get();
				    							double enchantSaveChancePerLevel = Config.config.enchantSaveChancePerLevel.get();

				    							if( chance > maxSalvageMaterialChance )
				    								chance = maxSalvageMaterialChance;

				    							double enchantChance = smithingLevel * enchantSaveChancePerLevel;
				    							if( enchantChance > maxSalvageEnchantChance )
				    								enchantChance = maxSalvageEnchantChance;

												double startDmg = itemStack.getDamage();
												double maxDmg = itemStack.getMaxDamage();
				    							double award = 0;
												double displayDurabilityPercent = ( 1.00f - ( startDmg / maxDmg ) ) * 100;
				    							double durabilityPercent = ( 1.00f - ( startDmg / maxDmg ) );

												if( Double.isNaN( durabilityPercent ) )
													durabilityPercent = 1;

				    							int potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );

				    							if( event.getHand() == Hand.OFF_HAND )
				    							{
				    								if( !player.isCreative() )
				    								{
				    									int returnAmount = 0;

				    									for( int i = 0; i < potentialReturnAmount; i++ )
				    									{
				    										if( Math.ceil( Math.random() * 10000 ) <= chance * 100 )
				    											returnAmount++;
				    									}
				    									award += (double) theMap.get( "xpPerItem" ) * returnAmount;

				    									if( returnAmount > 0 )
				    										dropItems( returnAmount, salvageItem, event.getWorld(), event.getPos() );

				    									if( award > 0 )
				    										awardXp( player, Skill.SMITHING, "salvaging " + returnAmount + "/" + salvageMax + " from an item", award, false  );

				    									if( returnAmount == potentialReturnAmount )
				    										NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.text.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
				    									else if( returnAmount > 0 )
				    										NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.text.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 3 ), (ServerPlayerEntity) player );
				    									else
				    										NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.text.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 2 ), (ServerPlayerEntity) player );

				    									if( enchants.size() > 0 )
				    									{
				    										ItemStack salvagedBook = new ItemStack( Items.ENCHANTED_BOOK );
				    										Set<Enchantment> enchantKeys = enchants.keySet();
															Map<Enchantment, Integer> newEnchantMap = new HashMap<>();
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
//				    													i = enchants.get( enchant ) + 1;
				    												}
				    											}
				    											if( enchantLevel > 0 )
																	newEnchantMap.put( enchant, enchantLevel );
				    										}
				    										if( newEnchantMap.size() > 0 )
				    										{
				    											EnchantmentHelper.setEnchantments( newEnchantMap, salvagedBook );
				    											block.spawnAsEntity( event.getWorld(), event.getPos(), salvagedBook );
				    											if( fullEnchants )
				    												player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.savedAllEnchants" ).setStyle( textStyle.get( "green" ) ), false );
				    											else
				    												player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.savedSomeEnchants" ).setStyle( textStyle.get( "yellow" ) ), false );
				    										}
				    									}
				    									player.getHeldItemOffhand().shrink( 1 );
//				    									player.inventory.offHandInventory.set( 0, new ItemStack( Items.AIR, 0 ) );
				    									player.sendBreakAnimation(Hand.OFF_HAND );
				    								}
				    								else
				    									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.survivalOnlyWarning" ).setStyle( textStyle.get( "red" ) ), true );
				    							}
				    							else
				    							{
				    								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.offhandToDiss" ), false );
				    								sendMessage( "_________________________________", false, player );
				    								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.durabilityInfo", item.getTranslationKey(), "" + DP.dp( displayDurabilityPercent ), false, 0 ), (ServerPlayerEntity) player );
				    								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.materialSaveChanceInfo", DP.dp( chance ), potentialReturnAmount ), false );
				    								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.repairInfo", "" + DP.dp( enchantChance ), "" + itemStack.getRepairCost(), false, 0 ), (ServerPlayerEntity) player );
				    								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.enchantmentBypassInfo", "" + maxPlayerBypass ), false );
				    							}
				    						}
											else
												player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.cannotSalvageLackLevelLonger", reqLevel, new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
				    					}
										else
											player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSalvageItem", theMap.get( "salvageItem" ) ).setStyle( textStyle.get( "red" ) ), true );
				    				}
				    				else
									{
										System.out.println( player.getPosition().distanceSq( event.getPos() ) );
										player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.tooFarAwayToSalvage" ).setStyle( textStyle.get( "red" ) ), true );
									}
								}
				    			else
									player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.cannotSalvage", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
								}
							}
				    	}

				    	if( block.equals( diamondBlock ) && event.getHand() == Hand.MAIN_HAND )
				    	{
				    		int agilityLevel = getLevel( Skill.AGILITY, player );
				    		int enduranceLevel = getLevel( Skill.ENDURANCE, player );
				    		int combatLevel = getLevel( Skill.COMBAT, player );
				    		int swimLevel = getLevel( Skill.SMITHING, player );
				    		int nightvisionUnlockLevel = (int) Math.floor( getConfig( "nightvisionUnlockLevel" ) );	//Swimming

				    		double maxFallSaveChance = Config.config.maxFallSaveChance.get();			//Agility
				    		double saveChancePerLevel = Config.config.saveChancePerLevel.get() / 100;
				    		double speedBoostPerLevel = getConfig( "speedBoostPerLevel" );
				    		double maxSpeedBoost = getConfig( "maxSpeedBoost" );

				    		int levelsPerDamage = Config.config.levelsPerDamage.get();					//Combat

				    		double endurancePerLevel = Config.config.endurancePerLevel.get();			//Endurance
				    		double maxEndurance = Config.config.maxEndurance.get();
				    		double endurePercent = (enduranceLevel * endurancePerLevel);
				    		if( endurePercent > maxEndurance )
				    			endurePercent = maxEndurance;

				    		double reach = AttributeHandler.getReach( player );
				    		double agilityChance = agilityLevel * saveChancePerLevel;

				    		double extraDamage = Math.floor( combatLevel / levelsPerDamage );

				    		double speedBonus = agilityLevel * speedBoostPerLevel;

				    		if( agilityChance > maxFallSaveChance )
				    			agilityChance = maxFallSaveChance;

				    		if( speedBonus > maxSpeedBoost )
				    			speedBonus = maxSpeedBoost;

				    		sendMessage( "_________________________________" , false, player );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.buildingInfo", DP.dp( reach ) ), false );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.fallInfo", DP.dp( agilityChance ) ), false );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.enduranceInfo", DP.dp( endurePercent ) ), false );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.combatInfo", DP.dp( extraDamage ) ), false );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.sprintInfo", DP.dp( speedBonus ) ), false );
				    		player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.enchantmentBypassInfo", maxPlayerBypass ), false );

				    		if( swimLevel >= nightvisionUnlockLevel )
				    			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.underwaterNightVisionUnLocked", nightvisionUnlockLevel ), false );
				    		else
				    			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.underwaterNightVisionLocked", nightvisionUnlockLevel ), false );
				    	}
				    }
                }
			}
		}
	}

	public static void handleAnvilRepair( AnvilRepairEvent event )
	{
		try
		{
			PlayerEntity player = event.getPlayer();
			if( !player.world.isRemote )
			{
				boolean bypassEnchantLimit = Config.config.bypassEnchantLimit.get();
				int currLevel = getLevel( Skill.SMITHING, player );
				ItemStack rItem = event.getIngredientInput();		//IGNORED FOR PURPOSE OF REPAIR
				ItemStack lItem = event.getItemInput();
				ItemStack oItem = event.getItemResult();

				if( event.getItemInput().getItem().isDamageable() )
				{
					double anvilCostReductionPerLevel = Config.config.anvilCostReductionPerLevel.get();
					double extraChanceToNotBreakAnvilPerLevel = Config.config.extraChanceToNotBreakAnvilPerLevel.get() / 100;
					double anvilFinalItemBonusRepaired = Config.config.anvilFinalItemBonusRepaired.get() / 100;
					int anvilFinalItemMaxCostToAnvil = Config.config.anvilFinalItemMaxCostToAnvil.get();

					double bonusRepair = anvilFinalItemBonusRepaired * currLevel;
					int maxCost = (int) Math.floor( 50 - ( currLevel * anvilCostReductionPerLevel ) );
					if( maxCost < anvilFinalItemMaxCostToAnvil )
						maxCost = anvilFinalItemMaxCostToAnvil;

					event.setBreakChance( event.getBreakChance() / ( 1f + (float) extraChanceToNotBreakAnvilPerLevel * currLevel ) );

					if( oItem.getRepairCost() > maxCost )
						oItem.setRepairCost( maxCost );

					float repaired = oItem.getDamage() - lItem.getDamage();
					if( repaired < 0 )
						repaired = -repaired;

					oItem.setDamage( (int) Math.floor( oItem.getDamage() - repaired * bonusRepair ) );

					double award = ( ( ( repaired + repaired * bonusRepair * 2.5 ) / 100 ) * ( 1 + lItem.getRepairCost() * 0.025 ) );
					if( Requirements.data.get( "salvageInfo" ).containsKey( oItem.getItem().getRegistryName().toString() ) )
						award *= (double) Requirements.data.get( "salvageInfo" ).get( oItem.getItem().getRegistryName().toString() ).get( "xpPerItem" );

					if( award > 0 )
					{
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.extraRepaired", "" + (int) repaired, "" + (int) ( repaired * bonusRepair ), true, 1 ), (ServerPlayerEntity) player );
						awardXp( player, Skill.SMITHING, "repairing an item by: " + repaired, award, false );
					}
				}

				if( bypassEnchantLimit )
				{
					Map<Enchantment, Integer> lEnchants = EnchantmentHelper.getEnchantments( rItem );
					Map<Enchantment, Integer> rEnchants = EnchantmentHelper.getEnchantments( lItem );

					Map<Enchantment, Integer> newEnchants = mergeEnchants( lEnchants, rEnchants, player, currLevel );

					EnchantmentHelper.setEnchantments( newEnchants, oItem );
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error( "ANVIL FAILED, PLEASE REPORT", e );
			System.out.println( "ANVIL FAILED, PLEASE REPORT" );
		}
	}

	public static Map<Enchantment, Integer> mergeEnchants( Map<Enchantment, Integer> lEnchants, Map<Enchantment, Integer> rEnchants, PlayerEntity player, int currLevel )
	{
		Map<Enchantment, Integer> newEnchants = new HashMap<>();
		double bypassChance = Config.config.upgradeChance.get();
		double failedBypassPenaltyChance = Config.config.failedUpgradeKeepLevelChance.get();
		int levelsPerOneEnchantBypass = Config.config.levelsPerOneEnchantBypass.get();
		int maxEnchantmentBypass = Config.config.maxEnchantmentBypass.get();
		int maxEnchantLevel = Config.config.maxEnchantLevel.get();
		boolean alwaysUseUpgradeChance = Config.config.alwaysUseUpgradeChance.get();
		boolean creative = player.isCreative();

		lEnchants.forEach( ( enchant, startLevel ) ->
		{
			if( newEnchants.containsKey( enchant ) )
			{
				if( newEnchants.get( enchant ) < startLevel )
					newEnchants.replace( enchant, startLevel );
			}
			else
				newEnchants.put( enchant, startLevel );
		});


		rEnchants.forEach( ( enchant, startLevel ) ->
		{
			if( newEnchants.containsKey( enchant ) )
			{
				if( newEnchants.get( enchant ) < startLevel )
					newEnchants.replace( enchant, startLevel );
			}
			else
				newEnchants.put( enchant, startLevel );
		});

		Set<Enchantment> keys = new HashSet<>( newEnchants.keySet() );

		keys.forEach( ( enchant ) ->
		{
			int startLevel = newEnchants.get( enchant );

			int maxPlayerBypass = (int) Math.floor( (double) currLevel / (double) levelsPerOneEnchantBypass );
			if( maxPlayerBypass > maxEnchantmentBypass )
				maxPlayerBypass = maxEnchantmentBypass;

			if( maxEnchantLevel < startLevel && !creative )
			{
				if( maxEnchantLevel > 0 )
					newEnchants.replace( enchant, maxEnchantLevel );
				else
					newEnchants.remove( enchant );
				NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.maxEnchantLevelWarning", enchant.getRegistryName().toString(), "" + maxEnchantLevel, false, 2 ), (ServerPlayerEntity) player );
			}
			else if( enchant.getMaxLevel() + maxPlayerBypass < startLevel && !creative )
			{
				if( enchant.getMaxLevel() + maxPlayerBypass > 0 )
					newEnchants.replace( enchant, enchant.getMaxLevel() + maxPlayerBypass );
				else
					newEnchants.remove( enchant );
				NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.enchantmentDegradedWarning", enchant.getRegistryName().toString(), "" + (enchant.getMaxLevel() + maxPlayerBypass), false, 2 ), (ServerPlayerEntity) player );
			}
			else if( lEnchants.get( enchant ) != null && rEnchants.get( enchant ) != null )
			{
				if( lEnchants.get( enchant ).intValue() == rEnchants.get( enchant ).intValue() ) //same values
				{
					if( startLevel + 1 > maxEnchantLevel && !creative )
					{
						NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.maxEnchantLevelWarning", enchant.getRegistryName().toString(), "" + maxEnchantLevel, false, 2 ), (ServerPlayerEntity) player );
					}
					else if( startLevel + 1 > enchant.getMaxLevel() + maxPlayerBypass && !creative )
					{
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.enchantLackOfLevelWarning", enchant.getRegistryName() ).setStyle( textStyle.get( "red" ) ), false );
					}
					else
					{
						if( ( ( startLevel >= enchant.getMaxLevel() ) || alwaysUseUpgradeChance ) && !creative )
						{
							if( Math.ceil( Math.random() * 100 ) <= bypassChance ) //success
							{
								newEnchants.replace( enchant, startLevel + 1 );
								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.enchantUpgradeSuccess", enchant.getRegistryName().toString(), "" + (startLevel + 1), false, 1 ), (ServerPlayerEntity) player );
							}
							else if( Math.ceil( Math.random() * 100 ) <= failedBypassPenaltyChance ) //fucked up twice
							{
								if( startLevel > 1 )
									newEnchants.replace( enchant, startLevel - 1 );
								else
									newEnchants.remove( enchant );
								NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.text.enchantUpgradeAndSaveFail", enchant.getRegistryName().toString(), "" + bypassChance, "" + failedBypassPenaltyChance, false, 2 ), (ServerPlayerEntity) player );
							}
							else	//only fucked up once
							{
								newEnchants.replace( enchant, startLevel );
								NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.enchantUpgradeFail", enchant.getRegistryName().toString(), "" + bypassChance, false, 3 ), (ServerPlayerEntity) player );
							}
						}
						else
						{
							newEnchants.replace( enchant, startLevel + 1 );
							NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.enchantUpgradeSuccess", enchant.getRegistryName().toString(), "" + (startLevel + 1), false, 1 ), (ServerPlayerEntity) player );
						}
					}
				}
			}
		});

		return newEnchants;
	}

	public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
	{
	    PlayerEntity player = event.getPlayer();
	    if( !player.world.isRemote() )
        {
	        Map<String, Double> award = new HashMap<>();
	        award.put( "crafting", 1D );
	        ItemStack itemStack = event.getCrafting();
	        ResourceLocation resLoc = itemStack.getItem().getRegistryName();

	        if( Requirements.data.get( "xpValueCrafting" ).containsKey( resLoc.toString() ) )
	            addMaps( award, getXpCrafting( resLoc ) );

	        multiplyMap( award, itemStack.getCount() );

            for( String skillName : award.keySet() )
            {
                awardXp( player, Skill.getSkill( skillName ), "crafting", award.get( skillName ), false );
            }
        }
	}

	public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
	{
		PlayerEntity player = event.getPlayer();

		String skill = getSkill( correctHarvestTool( event.getState().getMaterial() ) ).name().toLowerCase();
		double speedBonus = 0;

		int toolGap = getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), "tool" );

		if( toolGap > 0 )
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toUseAsTool", new TranslationTextComponent( player.getHeldItemMainhand().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );

		int startLevel = getLevel( Skill.getSkill( skill ), player );

		switch ( correctHarvestTool( event.getState().getMaterial() ) )
		{
			case "pickaxe":
				float height = event.getPos().getY();
				if (height < 0)
					height = -height;

				double blocksToUnbreakableY = Config.config.blocksToUnbreakableY.get();
				double heightMultiplier = 1 - ( height / blocksToUnbreakableY );

				if ( heightMultiplier < Config.config.minBreakSpeed.get() )
					heightMultiplier = Config.config.minBreakSpeed.get();

				speedBonus = Config.config.miningBonusSpeed.get() / 100;
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) * ( (float) heightMultiplier ) );
				break;

			case "axe":
				speedBonus = Config.config.woodcuttingBonusSpeed.get() / 100;
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
				break;

			case "shovel":
				speedBonus = Config.config.excavationBonusSpeed.get() / 100;
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
				break;

			case "hoe":
				speedBonus = Config.config.farmingBonusSpeed.get() / 100;
				event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
				break;

			default:
				event.setNewSpeed( event.getOriginalSpeed() );
				break;
		}

		event.setNewSpeed( event.getNewSpeed() / (toolGap + 1) );
	}

	public static CompoundNBT getPmmoTag( PlayerEntity player )
	{
		if( player != null )
		{
			CompoundNBT persistTag = player.getPersistentData();
			CompoundNBT pmmoTag = null;

			if( !persistTag.contains( "pmmo" ) )			//if Player doesn't have pmmo tag, make it
			{
				pmmoTag = new CompoundNBT();
				persistTag.put( "pmmo", pmmoTag );
			}
			else
			{
				pmmoTag = persistTag.getCompound( "pmmo" );	//if Player has pmmo tag, use it
			}

			return pmmoTag;
		}
		else
			return new CompoundNBT();
	}

	public static CompoundNBT getPmmoTagElement( PlayerEntity player, String element )
	{
		if( player != null )
		{
			CompoundNBT pmmoTag = getPmmoTag( player );
			CompoundNBT elementTag = null;

			if( !pmmoTag.contains( element ) )					//if Player doesn't have element tag, make it
			{
				elementTag = new CompoundNBT();
				pmmoTag.put( element, elementTag );
			}
			else
			{
				elementTag = pmmoTag.getCompound( element );	//if Player has element tag, use it
			}

			return elementTag;
		}
		else
			return new CompoundNBT();
	}

	public static CompoundNBT getSkillsTag( PlayerEntity player )
	{
		return getPmmoTagElement( player, "skills" );
	}

	public static CompoundNBT getPreferencesTag( PlayerEntity player )
	{
		return getPmmoTagElement( player, "preferences" );
	}

	public static CompoundNBT getAbilitiesTag( PlayerEntity player )
	{
		return getPmmoTagElement( player, "abilities" );
	}

	public static void awardXp( PlayerEntity player, Map<String, Object> map, String sourceName, boolean skip )
	{
		for( Map.Entry<String, Object> entry : map.entrySet() )
		{
			awardXp( player, Skill.getSkill( entry.getKey() ), sourceName, (double) entry.getValue(), skip );
		}
	}

	public static void awardXp(PlayerEntity player, Skill skill, @Nullable String sourceName, double amount, boolean skip )
	{
		double biomePenaltyMultiplier = getConfig( "biomePenaltyMultiplier" );
		double maxXp = getConfig( "maxXp" );

		if( amount <= 0.0f || player.world.isRemote || player instanceof FakePlayer )
			return;

		if( skill == skill.INVALID_SKILL )
		{
			LOGGER.error( "Invalid skill at awardXp" );
			return;
		}

		String skillName = skill.name().toLowerCase();
		double skillMultiplier = 1;
		double difficultyMultiplier = 1;


//		for( char letter : player.getDisplayName().getFormattedText().toCharArray() )
//		{
//			if( !( letter >= 'a' && letter <= 'z' ) && !( letter >= 'A' && letter <= 'Z' ) && !( letter >= '0' && letter <= '9' ) && !( letter == '\u00a7' ) && !( letter == '_' ) )
//				return;
//		}

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

			case SMITHING:
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


		Biome biome = player.world.getBiome( player.getPosition() );
		ResourceLocation resLoc = biome.getRegistryName();
		String biomeKey = resLoc.toString();
		Map<String, Object> biomeMap = Requirements.data.get( "biomeMultiplier" ).get( biomeKey );

		if( !checkReq( player, resLoc, "biome" ) )
			amount *= biomePenaltyMultiplier;
		else if( biomeMap != null && biomeMap.containsKey( skillName ) )
			amount *= (double) biomeMap.get( skillName );

		amount *= globalMultiplier;

		if( amount == 0 )
			return;

		String playerName = player.getDisplayName().getFormattedText();
		int startLevel;
		int currLevel;
		double startXp = 0;

		CompoundNBT skillsTag = getSkillsTag( player );

		if( !skillsTag.contains( skillName ) )
		{
			skillsTag.putDouble( skillName, amount );
			startLevel = 1;
		}
		else
		{
			startLevel = levelAtXp( skillsTag.getDouble( skillName ) );
			startXp = skillsTag.getDouble( skillName );

			if( startXp >= 2000000000 )
				return;

			if( startXp + amount >= 2000000000 )
			{
				sendMessage( skillName + " cap of 2b xp reached, you fucking psycho!", false, player, TextFormatting.LIGHT_PURPLE );
				System.out.println( player.getName() + " " + skillName + " 2b cap reached" );
				amount = 2000000000 - startXp;
			}

			skillsTag.putDouble( skillName, (startXp + amount) );
		}

		currLevel = levelAtXp( skillsTag.getDouble( skillName ) );

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

			if( ModList.get().isLoaded( "compatskills" ) )
			{
				try
				{
					if( !player.world.isRemote )
						player.getServer().getCommandManager().getDispatcher().execute( "reskillable incrementskill " + playerName + " compatskills." + skill + " 1", player.getCommandSource().withFeedbackDisabled() );
				}
				catch( CommandSyntaxException e )
				{
					System.out.println( e );
				}
			}

			System.out.println( playerName + " " + currLevel + " " + skillName + " level up!" );
			if( currLevel % 10 == 0 && broadcastMilestone )
			{
				player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
				{
					if( thePlayer.getUniqueID() != player.getUniqueID() )
						NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.text.milestoneLevelUp", player.getDisplayName().getString(), "" + currLevel , "pmmo.text." + skillName, false, 3 ), thePlayer );
				});
			}
		}

		if( player instanceof ServerPlayerEntity )
			NetworkHandler.sendToPlayer( new MessageXp( startXp, skill.getValue(), amount, skip ), (ServerPlayerEntity) player );

//		if( !skip )
//			System.out.println( playerName + " +" + amount + "xp in "  + skillName + " for " + sourceName + " total xp: " + skillsTag.getDouble( skillName ) );

		if( startXp + amount >= maxXp && startXp < maxXp )
		{
			sendMessage( skillName + " max startLevel reached, you psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			System.out.println( playerName + " " + skillName + " max startLevel reached" );
		}
	}

	private static int getGap( int a, int b )
	{
		int gap = a - b;

		return gap;
	}

	private static int getSkillReqGap( PlayerEntity player, ResourceLocation res, String type )
	{
		int gap = 0;

		if( !checkReq( player, res, type ) )
		{
			Map<String, Object> theMap = null;
			Map<String, Double> reqs = new HashMap<>();
			switch( type )
			{
				case "wear":
					if( Requirements.data.get( "wearReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "wearReq" ).get( res.toString() );
					break;

				case "tool":
					if( Requirements.data.get( "toolReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "toolReq" ).get( res.toString() );
					break;

				case "weapon":
					if( Requirements.data.get( "weaponReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "weaponReq" ).get( res.toString() );
					break;

				case "use":
					if( Requirements.data.get( "useReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "useReq" ).get( res.toString() );
					break;

				case "place":
					if( Requirements.data.get( "placeReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "placeReq" ).get( res.toString() );
					break;

				case "break":
					if( Requirements.data.get( "breakReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "breakReq" ).get( res.toString() );
					break;

				case "biome":
					if( Requirements.data.get( "biomeReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "biomeReq" ).get( res.toString() );
					break;

				case "slayer":
					if( Requirements.data.get( "killReq" ).containsKey( res.toString() ) )
						theMap = Requirements.data.get( "killReq" ).get( res.toString() );
					break;

				default:
					System.out.println( "PLEASE REPORT THIS IF YOU SEE ME" );
					return 0;
			}

			if( theMap != null )
			{
				for( Map.Entry<String, Object> entry : theMap.entrySet() )
				{
					if( entry.getValue() instanceof Double )
						reqs.put( entry.getKey(), (double) entry.getValue() );
				}

				gap = (int) Math.floor( reqs.entrySet().stream()
						.map( entry -> getGap( (int) Math.floor( entry.getValue() ), getLevel( Skill.getSkill( entry.getKey() ), player ) ) )
						.reduce( 0, Math::max ) );
			}
		}

		return gap;
	}

	private static void checkWornLevelReq( PlayerEntity player, Item item )
	{
		if( !checkReq( player, item.getRegistryName(), "wear" ) )
		{
			int gap = getSkillReqGap( player, item.getRegistryName(), "wear" );
			if( gap > 0 )
			    player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toWear", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );

			if( gap > 9 )
				gap = 9;

			player.addPotionEffect( new EffectInstance( Effects.MINING_FATIGUE, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.WEAKNESS, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.SLOWNESS, 75, gap, false, true ) );
		}
	}

	private static void checkBiomeLevelReq( PlayerEntity player )
	{
		Biome biome = player.world.getBiome( player.getPosition() );
		ResourceLocation resLoc = biome.getRegistryName();
		String biomeKey = resLoc.toString();
		UUID playerUUID = player.getUniqueID();
		Map<String, Object> biomeReq = Requirements.data.get( "biomeReq" ).get( biomeKey );
		Map<String, Object> biomeEffect = Requirements.data.get( "biomeEffect" ).get( biomeKey );

		if( !lastBiome.containsKey( playerUUID ) )
			lastBiome.put( playerUUID, "none" );

		if( biomeReq != null && biomeEffect != null )
		{
			if( !checkReq( player, resLoc, "biome" ) )
			{
				for( Map.Entry<String, Object> entry : biomeEffect.entrySet() )
				{
					Effect effect = ForgeRegistries.POTIONS.getValue( new ResourceLocation( entry.getKey() ) );

					if( effect != null )
						player.addPotionEffect( new EffectInstance( effect, 75, (int) Math.floor( (double) entry.getValue() ), false, true ) );
				}

				if( player.world.isRemote() )
				{
					if( !lastBiome.get( playerUUID ).equals( biomeKey ) )
					{
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toSurvive", new TranslationTextComponent( biome.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.toSurvive", new TranslationTextComponent( biome.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
						for( Map.Entry<String, Object> entry : biomeReq.entrySet() )
						{
							int startLevel = getLevel( Skill.getSkill( entry.getKey() ), player );

							if( startLevel < (double) entry.getValue() )
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + entry.getKey() ).getString(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "red" ) ), false );
							else
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + entry.getKey() ).getString(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "green" ) ), false );
						}
					}
				}
			}
		}

		lastBiome.put( playerUUID, biomeKey );
	}

	public static void handlePlayerTick( TickEvent.PlayerTickEvent event )
	{
		PlayerEntity player = event.player;
		boolean crawlingAllowed;
		if( getConfig( "crawlingAllowed" ) == 0 )
			crawlingAllowed = false;
		else
			crawlingAllowed = true;

		if( isCrawling.contains( player.getUniqueID() ) && crawlingAllowed )
			PMMOPoseSetter.setPose( player, Pose.SWIMMING );

		if( !player.isCreative() && player.isAlive() )
		{
			String name = player.getName().getString();
			UUID playerUUID = player.getUniqueID();

			if( player.isSprinting() )
				AttributeHandler.updateSpeed( player );
			else
				AttributeHandler.resetSpeed( player );

			if( !lastAward.containsKey( playerUUID ) )
				lastAward.put( playerUUID, System.currentTimeMillis() );

			long gap = System.currentTimeMillis() - lastAward.get( playerUUID );
			if( gap > 1000 )
			{
				int swimLevel = getLevel( Skill.SWIMMING, player );
				int flyLevel = getLevel( Skill.FLYING, player );
				int agilityLevel = getLevel( Skill.AGILITY, player );
				int nightvisionUnlockLevel = Config.config.nightvisionUnlockLevel.get();
				float swimAmp = EnchantmentHelper.getDepthStriderModifier( player );
				float speedAmp = 0;
				PlayerInventory inv = player.inventory;

				checkBiomeLevelReq( player );

				if( !player.world.isRemote() )
				{
					if( Curios.isLoaded() )
					{
						Curios.getCurios(player).forEach(value ->
						{
							for (int i = 0; i < value.getSlots(); i++)
							{
								checkWornLevelReq( player, value.getStackInSlot(i).getItem() );
							}
						});
					}

					if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
						checkWornLevelReq( player, player.inventory.getStackInSlot( 39 ).getItem() );
					if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
						checkWornLevelReq( player, player.inventory.getStackInSlot( 38 ).getItem() );
					if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
						checkWornLevelReq( player, player.inventory.getStackInSlot( 37 ).getItem() );
					if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
						checkWornLevelReq( player, player.inventory.getStackInSlot( 36 ).getItem() );
				}
////////////////////////////////////////////XP_STUFF//////////////////////////////////////////

				if( player.isPotionActive( Effects.SPEED ) )
					speedAmp = player.getActivePotionEffect( Effects.SPEED ).getAmplifier() + 1;

				float swimAward = ( 3 + swimLevel    / 10.00f ) * ( gap / 1000f ) * ( 1 + swimAmp / 4 );
				float flyAward  = ( 1 + flyLevel     / 30.77f ) * ( gap / 1000f );
				float runAward  = ( 1 + agilityLevel / 30.77f ) * ( gap / 1000f ) * ( 1 + speedAmp / 4);

				lastAward.replace( playerUUID, System.currentTimeMillis() );
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

				if( swimLevel >= nightvisionUnlockLevel && player.isInWater() && waterAbove )
					player.addPotionEffect( new EffectInstance( Effects.NIGHT_VISION, 300, 0, false, false ) );

				if( player.isSprinting() )
				{
					if( player.isInWater() && ( waterAbove || waterBelow ) )
					{


						awardXp( player, Skill.SWIMMING, "swimming fast", swimAward * 1.25f, true );
					}
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
////////////////////////////////////////////ABILITIES//////////////////////////////////////////
//				if( !player.world.isRemote() )
//				{
//					CompoundNBT abilityTag = getAbilitiesTag( player );
//					if( !abilityTag.contains( "excavate" ) )
//						abilityTag.putDouble( "excavate", 0 );
//
//					abilityTag.putDouble( "excavate", abilityTag.getDouble( "excavate" ) + 1 );
//
//					System.out.println( abilityTag.getDouble( "excavate" ) );
//				}
			}
		}
	}

	public static void handleFished( ItemFishedEvent event )
	{
		PlayerEntity player = event.getPlayer();
		int startLevel = getLevel( Skill.FISHING, player );
		int level;
		NonNullList<ItemStack> items = event.getDrops();
		double award = 10D;
		for( ItemStack itemStack : items )
		{
			Map<String, Double> itemXp = getXp( itemStack.getItem().getRegistryName() );
			if( itemXp.containsKey( "fishing" ) )
				award = itemXp.get( "fishing" );
		}
		Map<String, Map<String, Object>> fishPool = Requirements.data.get( "fishPool" );

		if( fishPool != null )
		{
			double fishPoolBaseChance = Config.config.fishPoolBaseChance.get();
			double fishPoolChancePerLevel = Config.config.fishPoolChancePerLevel.get();
			double fishPoolMaxChance = Config.config.fishPoolMaxChance.get();
			double fishPoolChance = fishPoolBaseChance + fishPoolChancePerLevel * startLevel;
			if( fishPoolChance > fishPoolMaxChance )
				fishPoolChance = fishPoolMaxChance;

			if( Math.random() * 10000 < fishPoolChance * 100 )
			{
				String matchKey = null;
				Map<String, Object> match = new HashMap<>();

				double totalWeight = 0;
				double weight;
				double result;
				double currentWeight = 0;
				int count, minCount, maxCount;

				for( Map.Entry<String, Map<String, Object>> entry : fishPool.entrySet() )
				{
					totalWeight += getWeight( startLevel, entry.getValue() );
				}

				result = Math.floor( Math.random() * (totalWeight + 1) );

				for( Map.Entry<String, Map<String, Object>> entry : fishPool.entrySet() )
				{
					weight = getWeight( startLevel, entry.getValue() );

					if( currentWeight < result && currentWeight + weight >= result )
					{
						matchKey = entry.getKey();
						match = new HashMap<>( entry.getValue() );
						break;
					}

					currentWeight += weight;
				}

				Item item = ForgeRegistries.ITEMS.getValue( new ResourceLocation( matchKey ) );

				minCount = (int) Math.floor( (double) match.get( "minCount" ) );
				maxCount = (int) Math.floor( (double) match.get( "maxCount" ) );

				if( maxCount == 1 )
					count = 1;
				else
					count = (int) Math.floor( Math.random() * ( maxCount - minCount ) + minCount + 1 );

				ItemStack itemStack = new ItemStack( item, count );

				if( itemStack.isDamageable() )
					itemStack.setDamage( (int) Math.floor( Math.random() * itemStack.getMaxDamage() ) );

				if( itemStack.isEnchantable() )
				{
					Map<String, Map<String, Object>> enchantMap = Requirements.data.get( "fishEnchantPool" );
					Map<Enchantment, Integer> outEnchants = new HashMap<>();

					for( Map.Entry<String, Map<String, Object>> entry : enchantMap.entrySet() )
					{
						Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue( new ResourceLocation( entry.getKey() ) );
						Map<String, Object> enchantInfo = entry.getValue();

						if( enchant.canApply( itemStack ) )
						{
							int enchantLevelReq = (int) Math.floor( (double) enchantInfo.get( "levelReq" ) );
							int itemLevelReq = (int) Math.floor( (double) match.get( "enchantLevelReq" ) );
							int totalLevelReq = enchantLevelReq + itemLevelReq;
							if( startLevel >= totalLevelReq )
							{
								level = startLevel - totalLevelReq;
								double chancePerLevel = (double) enchantInfo.get( "chancePerLevel" );
								double maxChance = (double) enchantInfo.get( "maxChance" );
								double enchantChance = (chancePerLevel * level );
								if( enchantChance > maxChance )
									enchantChance = maxChance;

								double levelPerLevel = (double) enchantInfo.get( "levelPerLevel" );
								double maxEnchantLevel = (double) enchantInfo.get( "maxLevel" );
								int potentialEnchantLevel;

								if( levelPerLevel > 0 )
									potentialEnchantLevel = (int) Math.floor( level / levelPerLevel );
								else
									potentialEnchantLevel = (int) Math.floor( maxEnchantLevel );

								if( potentialEnchantLevel > maxEnchantLevel )
									potentialEnchantLevel = (int) Math.floor( maxEnchantLevel );

								int enchantLevel = 0;

								for( int i = 0; i < potentialEnchantLevel; i++ )
								{
									if( Math.random() * 10000 < enchantChance * 100 )
									{
										enchantLevel++;
									}
//									else
//										break;
								}

								if( enchantLevel > 0 )
									outEnchants.put( enchant, enchantLevel );
							}
						}
					}

					if( outEnchants.size() > 0 )
						EnchantmentHelper.setEnchantments( outEnchants, itemStack );
				}

				dropItemStack( itemStack, player.world, player.getPosition() );
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.extraFished", count, new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( textStyle.get( "green" ) ), true );

				award += (double) match.get( "xp" ) * count;
			}

			awardXp( player, Skill.FISHING, "catching " + items, award, false );
		}
	}
	
	private static double getWeight( int startLevel, Map<String, Object> fishItem )
	{
		return DP.map( startLevel, (double) fishItem.get( "startLevel" ), (double) fishItem.get( "endLevel" ), (double) fishItem.get( "startWeight" ), (double) fishItem.get( "endWeight" ) );
	}

	public static int levelAtXp( float xp )
	{
		return levelAtXp( (double) xp );
	}
	public static int levelAtXp( double xp )
	{
		double baseXp = getConfig( "baseXp" );
		double xpIncreasePerLevel = getConfig( "xpIncreasePerLevel" );
		int maxLevel = (int) Math.floor( getConfig( "maxLevel" ) );

		int theXp = 0;

		for( int startLevel = 0; ; startLevel++ )
		{
			if( xp < theXp || startLevel >= maxLevel )
			{
				return startLevel;
			}
			theXp += baseXp + startLevel * xpIncreasePerLevel;
		}
	}

	public static float levelAtXpDecimal( float xp )
	{
		return (float) levelAtXpDecimal( (double) xp );
	}
	public static double levelAtXpDecimal( double xp )
	{
		int maxLevel = (int) Math.floor( getConfig( "maxLevel" ) );

		if( levelAtXp( xp ) == maxLevel )
			xp = xpAtLevel( maxLevel );
		int startLevel = levelAtXp( xp );
		double startXp = xpAtLevel( startLevel );
		double goalXp = xpAtLevel( startLevel + 1 );

		if( startXp == goalXp )
			return maxLevel;
		else
			return startLevel + ( (xp - startXp) / (goalXp - startXp) );
	}

	public static double xpAtLevel( float givenLevel )
	{
		return xpAtLevel( (double) givenLevel );
	}
	public static double xpAtLevel( double givenLevel )
	{
		double baseXp = getConfig( "baseXp" );
		double xpIncreasePerLevel = getConfig( "xpIncreasePerLevel" );
		int maxLevel = (int) Math.floor( getConfig( "maxLevel" ) );

		double theXp = 0;
		if( givenLevel > maxLevel )
			givenLevel = maxLevel;

		for( int startLevel = 1; startLevel < givenLevel; startLevel++ )
		{
			theXp += baseXp + (startLevel - 1) * xpIncreasePerLevel;
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

