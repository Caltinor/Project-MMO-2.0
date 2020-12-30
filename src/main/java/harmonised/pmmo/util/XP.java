package harmonised.pmmo.util;

import java.util.*;

import harmonised.pmmo.baubles.BaublesHandler;
import harmonised.pmmo.config.AutoValues;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.network.*;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

public class XP
{
	public static final Logger LOGGER = LogManager.getLogger();

	private static Map<Material, String> materialHarvestTool = new HashMap<>();
	public static Set<UUID> isVeining = new HashSet<>();
	public static Map<String, Style> textStyle = new HashMap<>();
	public static Map<UUID, String> playerNames = new HashMap<>();
	public static Map<String, UUID> playerUUIDs = new HashMap<>();
	public static Map<UUID, Map<String, Double>> offlineXp = new HashMap<>();
	private static Map<UUID, String> lastBiome = new HashMap<>();
	private static int debugInt = 0;

	public static void initValues()
	{
////////////////////////////////////COLOR_VALUES///////////////////////////////////////////////////
		Skill.setSkillStyle( Skill.MINING.toString(), new Style().setColor( TextFormatting.AQUA ) );
		Skill.setSkillStyle( Skill.BUILDING.toString(), new Style().setColor( TextFormatting.AQUA ) );
		Skill.setSkillStyle( Skill.EXCAVATION.toString(), new Style().setColor( TextFormatting.GOLD ) );
		Skill.setSkillStyle( Skill.WOODCUTTING.toString(), new Style().setColor( TextFormatting.GOLD ) );
		Skill.setSkillStyle( Skill.FARMING.toString(), new Style().setColor( TextFormatting.GREEN ) );
		Skill.setSkillStyle( Skill.AGILITY.toString(), new Style().setColor( TextFormatting.GREEN ) );
		Skill.setSkillStyle( Skill.ENDURANCE.toString(), new Style().setColor( TextFormatting.DARK_RED ) );
		Skill.setSkillStyle( Skill.COMBAT.toString(), new Style().setColor( TextFormatting.RED ) );
		Skill.setSkillStyle( Skill.ARCHERY.toString(), new Style().setColor( TextFormatting.YELLOW ) );
		Skill.setSkillStyle( Skill.SMITHING.toString(), new Style().setColor( TextFormatting.GRAY ) );
		Skill.setSkillStyle( Skill.FLYING.toString(), new Style().setColor( TextFormatting.GRAY ) );
		Skill.setSkillStyle( Skill.SWIMMING.toString(), new Style().setColor( TextFormatting.AQUA ) );
		Skill.setSkillStyle( Skill.FISHING.toString(), new Style().setColor( TextFormatting.AQUA ) );
		Skill.setSkillStyle( Skill.CRAFTING.toString(), new Style().setColor( TextFormatting.GOLD ) );
		Skill.setSkillStyle( Skill.MAGIC.toString(), new Style().setColor( TextFormatting.BLUE ) );
		Skill.setSkillStyle( Skill.SLAYER.toString(), new Style().setColor( TextFormatting.GRAY ) );
		Skill.setSkillStyle( Skill.HUNTER.toString(), new Style().setColor( TextFormatting.GOLD ) );
		Skill.setSkillStyle( Skill.TAMING.toString(), new Style().setColor( TextFormatting.WHITE ) );
		Skill.setSkillStyle( Skill.COOKING.toString(), new Style().setColor( TextFormatting.GOLD ) );
		Skill.setSkillStyle( Skill.ALCHEMY.toString(), new Style().setColor( TextFormatting.GOLD ) );

//		skillStyle.put(Skill.MINING.toString(), TextFormatting.AQUA );
//		skillStyle.put( Skill.BUILDING.toString(), TextFormatting.AQUA );
//		skillStyle.put( Skill.EXCAVATION.toString(), TextFormatting.GOLD );
//		skillStyle.put( Skill.WOODCUTTING.toString(), TextFormatting.GOLD );
//		skillStyle.put( Skill.FARMING.toString(), TextFormatting.GREEN );
//		skillStyle.put( Skill.AGILITY.toString(), TextFormatting.GREEN );
//		skillStyle.put( Skill.ENDURANCE.toString(), TextFormatting.DARK_RED );
//		skillStyle.put( Skill.COMBAT.toString(), TextFormatting.RED);
//		skillStyle.put( Skill.ARCHERY.toString(), TextFormatting.YELLOW );
//		skillStyle.put( Skill.SMITHING.toString(), TextFormatting.GRAY );
//		skillStyle.put( Skill.FLYING.toString(), TextFormatting.GRAY );
//		skillStyle.put( Skill.SWIMMING.toString(), TextFormatting.AQUA );
//		skillStyle.put( Skill.FISHING.toString(), TextFormatting.AQUA );
//		skillStyle.put( Skill.CRAFTING.toString(), TextFormatting.GOLD );
//		skillStyle.put( Skill.MAGIC.toString(), TextFormatting.BLUE );
//		skillStyle.put( Skill.SLAYER, TextFormatting.DARK_GRAY );
//		skillStyle.put( Skill.FLETCHING, TextFormatting.DARK_GREEN );
//		skillStyle.put( Skill.TAMING, TextFormatting.WHITE );
////////////////////////////////////Style//////////////////////////////////////////////
		textStyle.put( "red", 			new Style().setColor( TextFormatting.RED ) );
		textStyle.put( "green", 		new Style().setColor( TextFormatting.GREEN ) );
		textStyle.put( "dark_green", 	new Style().setColor( TextFormatting.DARK_GREEN ) );
		textStyle.put( "yellow", 		new Style().setColor( TextFormatting.YELLOW ) );
		textStyle.put( "grey", 			new Style().setColor( TextFormatting.GRAY ) );
		textStyle.put( "cyan", 			new Style().setColor( TextFormatting.AQUA ) );
		textStyle.put( "blue", 			new Style().setColor( TextFormatting.BLUE ) );
		textStyle.put( "dark_blue", 	new Style().setColor( TextFormatting.DARK_BLUE ) );
		textStyle.put( "pink", 			new Style().setColor( TextFormatting.LIGHT_PURPLE ) );
		textStyle.put( "dark_purple", 	new Style().setColor( TextFormatting.DARK_PURPLE ) );
////////////////////////////////////PATREONS//////////////////////////////////////////////
		PlayerConnectedHandler.lapisPatreons.add( 		UUID.fromString( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" ) );	//LUCIFER

		PlayerConnectedHandler.dandelionPatreons.add( 	UUID.fromString( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" ) );	//TYRIUS
		PlayerConnectedHandler.dandelionPatreons.add( 	UUID.fromString( "554b53b8-d0fa-409e-ab87-2a34bf83e506" ) );	//JOERKIG
		PlayerConnectedHandler.dandelionPatreons.add( 	UUID.fromString( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" ) );	//DIDIS

		PlayerConnectedHandler.dandelionPatreons.add( 	UUID.fromString( "21bb554a-f339-48ef-80f7-9a5083172892" ) );	//JUDICIUS
		PlayerConnectedHandler.muteList.add( UUID.fromString( "21bb554a-f339-48ef-80f7-9a5083172892" ) );

		PlayerConnectedHandler.ironPatreons.add( 		UUID.fromString( "0bc51f06-9906-41ea-9fb4-7e9be169c980" ) );	//STRESSINDICATOR
		PlayerConnectedHandler.ironPatreons.add( 		UUID.fromString( "5bfdb948-7b66-476a-aefe-d45e4778fb2d" ) );	//DADDY_P1G
		PlayerConnectedHandler.ironPatreons.add( 		UUID.fromString( "edafb5eb-9ccb-4121-bef7-e7ffded64ee3" ) );	//LEWDCINA
////////////////////////////////////MATERIAL_HARVEST_TOOLS/////////////////////////////////////////
		materialHarvestTool.put( Material.ANVIL, "pickaxe" );		//PICKAXE
		materialHarvestTool.put( Material.GLASS, "pickaxe" );
		materialHarvestTool.put( Material.ICE, "pickaxe" );
		materialHarvestTool.put( Material.IRON, "pickaxe" );
		materialHarvestTool.put( Material.PACKED_ICE, "pickaxe" );
		materialHarvestTool.put( Material.PISTON, "pickaxe" );
		materialHarvestTool.put( Material.REDSTONE_LIGHT, "pickaxe" );
		materialHarvestTool.put( Material.ROCK, "pickaxe" );
//		materialHarvestTool.put( Material.SHULKER, "pickaxe" );
		materialHarvestTool.put( Material.BARRIER, "pickaxe" );
//		materialHarvestTool.put( Material.MISCELLANEOUS, "pickaxe" );

		materialHarvestTool.put( Material.WOOD, "axe" );			//AXE
		materialHarvestTool.put( Material.LEAVES, "axe" );
		materialHarvestTool.put( Material.GOURD, "axe" );

		materialHarvestTool.put( Material.CLAY, "shovel" );			//SHOVEL
//		materialHarvestTool.put( Material.EARTH, "shovel" );
		materialHarvestTool.put( Material.SAND, "shovel" );
		materialHarvestTool.put( Material.SNOW, "shovel" );
		materialHarvestTool.put( Material.GRASS, "shovel" );
		materialHarvestTool.put( Material.GROUND, "shovel" );
		materialHarvestTool.put( Material.CRAFTED_SNOW, "shovel" );
//		materialHarvestTool.put( Material.SEA_GRASS, "shovel" );
//		materialHarvestTool.put( Material.SNOW_BLOCK, "shovel" );

		materialHarvestTool.put( Material.PLANTS, "hoe" );			//HOE
//		materialHarvestTool.put( Material.OCEAN_PLANT, "hoe" );
		materialHarvestTool.put( Material.CACTUS, "hoe" );
		materialHarvestTool.put( Material.CORAL, "hoe" );
		materialHarvestTool.put( Material.VINE, "hoe" );
//		materialHarvestTool.put( Material.TALL_PLANTS, "hoe" );
//		materialHarvestTool.put( Material.BAMBOO, "hoe" );
//		materialHarvestTool.put( Material.BAMBOO_SAPLING, "hoe" );
//		materialHarvestTool.put( Material.ORGANIC, "hoe" );

//		materialHarvestTool.put( Material.WOOL, "shears" );			//SHEARS (Crafting)
		materialHarvestTool.put( Material.CARPET, "shears" );
		materialHarvestTool.put( Material.TNT, "shears" );
		materialHarvestTool.put( Material.DRAGON_EGG, "shears" );
		materialHarvestTool.put( Material.WEB, "shears" );
		materialHarvestTool.put( Material.CAKE, "shears" );
		materialHarvestTool.put( Material.SPONGE, "shears" );
	}

	public static Item getBlockAsItem( Block block )
	{
		Item item = ForgeRegistries.ITEMS.getValue( block.getRegistryName() );
		return item == null ? Items.AIR : item;
	}

	public static String getSkill( IBlockState state )
	{
		String skill = getSkillFromTool( getHarvestTool( state ) );
		if( skill.equals( Skill.INVALID_SKILL.toString() ) )
			return getSkill( state.getBlock() );
		else
			return skill;
	}

	public static String getSkill( Block block )
	{
		String[] oreNames = OreDictionary.getOreNames();
		int[] oreIDs = OreDictionary.getOreIDs( new ItemStack( block ) );
		for( int id : oreIDs )
		{
			String tagName = oreNames[ id ];
			if( tagName.startsWith( "stone" ) || tagName.startsWith( "ore" ) )
			{
				return Skill.MINING.toString();
			}
			else if( tagName.startsWith( "log" ) || tagName.startsWith( "wood" ) )
			{
				return Skill.WOODCUTTING.toString();
			}
			else if( tagName.startsWith( "crop" ) )
			{
				return Skill.FARMING.toString();
			}
		}
		return Skill.INVALID_SKILL.toString();
	}

	public static String getSkillFromTool( String tool )
	{
		if( tool == null )
			return Skill.INVALID_SKILL.toString();

		switch( tool )
		{
			case "pickaxe":
				return Skill.MINING.toString();

			case "shovel":
				return Skill.EXCAVATION.toString();

			case "axe":
				return Skill.WOODCUTTING.toString();

			case "hoe":
				return Skill.FARMING.toString();

			case "shears":
				return Skill.CRAFTING.toString();

			default:
				return Skill.INVALID_SKILL.toString();
		}
	}

	public static Map<String, Double> getXp( String registryName, JType jType )
	{
		return getXp( new ResourceLocation( registryName ), jType );
	}

	public static Map<String, Double> getXp( ResourceLocation registryName, JType jType )
	{
		Map<String, Double> theMap = new HashMap<>();

		if( JsonConfig.data.get( jType ).containsKey( registryName.toString() ) )
		{
			for( Map.Entry<String, Double> entry : JsonConfig.data.get( jType ).get( registryName.toString() ).entrySet() )
			{
				theMap.put( entry.getKey(), entry.getValue() );
			}
		}

		return theMap;
	}

	public static String getHarvestTool( IBlockState state )
	{
		String correctTool = materialHarvestTool.getOrDefault( state.getMaterial(), "none" );

		if( correctTool.equals( "none" ) )
		{
			double pickDestroySpeed   = new ItemStack( Items.DIAMOND_PICKAXE ).getDestroySpeed( state );
			double axeDestroySpeed    = new ItemStack( Items.DIAMOND_AXE ).getDestroySpeed( state );
			double shovelDestroySpeed = new ItemStack( Items.DIAMOND_SHOVEL ).getDestroySpeed( state );
			double swordDestroySpeed = new ItemStack( Items.DIAMOND_SWORD ).getDestroySpeed( state );

			double highestDestroySpeed = pickDestroySpeed;
			correctTool = "pickaxe";

			if( highestDestroySpeed < axeDestroySpeed )
			{
				highestDestroySpeed = axeDestroySpeed;
				correctTool = "axe";
			}
			if( highestDestroySpeed < shovelDestroySpeed )
			{
				highestDestroySpeed = shovelDestroySpeed;
				correctTool = "shovel";
			}
			if( highestDestroySpeed < swordDestroySpeed )
			{
//				highestDestroySpeed = swordDestroySpeed;
				correctTool = "shears";
			}
		}

		return correctTool;
	}

//	public static String checkMaterial( Material material )
//	{
//		if( material.equals( Material.AIR ) )
//			return "AIR";
//		else if( material.equals( Material.STRUCTURE_VOID ) )
//			return "STRUCTURE_VOID";
//		else if( material.equals( Material.PORTAL ) )
//			return "PORTAL";
//		else if( material.equals( Material.CARPET ) )
//			return "CARPET";
//		else if( material.equals( Material.PLANTS ) )
//			return "PLANTS";
//		else if( material.equals( Material.OCEAN_PLANT ) )
//			return "OCEAN_PLANT";
//		else if( material.equals( Material.TALL_PLANTS ) )
//			return "TALL_PLANTS";
//		else if( material.equals( Material.SEA_GRASS ) )
//			return "SEA_GRASS";
//		else if( material.equals( Material.WATER ) )
//			return "WATER";
//		else if( material.equals( Material.BUBBLE_COLUMN ) )
//			return "BUBBLE_COLUMN";
//		else if( material.equals( Material.LAVA ) )
//			return "LAVA";
//		else if( material.equals( Material.SNOW ) )
//			return "SNOW";
//		else if( material.equals( Material.FIRE ) )
//			return "FIRE";
//		else if( material.equals( Material.MISCELLANEOUS ) )
//			return "MISCELLANEOUS";
//		else if( material.equals( Material.WEB ) )
//			return "WEB";
//		else if( material.equals( Material.REDSTONE_LIGHT ) )
//			return "REDSTONE_LIGHT";
//		else if( material.equals( Material.CLAY ) )
//			return "CLAY";
//		else if( material.equals( Material.EARTH ) )
//			return "EARTH";
//		else if( material.equals( Material.ORGANIC ) )
//			return "ORGANIC";
//		else if( material.equals( Material.PACKED_ICE ) )
//			return "PACKED_ICE";
//		else if( material.equals( Material.SAND ) )
//			return "SAND";
//		else if( material.equals( Material.SPONGE ) )
//			return "SPONGE";
//		else if( material.equals( Material.SHULKER ) )
//			return "SHULKER";
//		else if( material.equals( Material.WOOD ) )
//			return "WOOD";
//		else if( material.equals( Material.BAMBOO_SAPLING ) )
//			return "BAMBOO_SAPLING";
//		else if( material.equals( Material.BAMBOO ) )
//			return "BAMBOO";
//		else if( material.equals( Material.WOOL ) )
//			return "WOOL";
//		else if( material.equals( Material.TNT ) )
//			return "TNT";
//		else if( material.equals( Material.LEAVES ) )
//			return "LEAVES";
//		else if( material.equals( Material.GLASS ) )
//			return "GLASS";
//		else if( material.equals( Material.ICE ) )
//			return "ICE";
//		else if( material.equals( Material.CACTUS ) )
//			return "CACTUS";
//		else if( material.equals( Material.ROCK ) )
//			return "ROCK";
//		else if( material.equals( Material.IRON ) )
//			return "IRON";
//		else if( material.equals( Material.SNOW_BLOCK ) )
//			return "SNOW_BLOCK";
//		else if( material.equals( Material.ANVIL ) )
//			return "ANVIL";
//		else if( material.equals( Material.BARRIER ) )
//			return "BARRIER";
//		else if( material.equals( Material.PISTON ) )
//			return "PISTON";
//		else if( material.equals( Material.CORAL ) )
//			return "CORAL";
//		else if( material.equals( Material.GOURD ) )
//			return "GOURD";
//		else if( material.equals( Material.DRAGON_EGG ) )
//			return "DRAGON_EGG";
//		else if( material.equals( Material.CAKE ) )
//			return "CAKE";
//		else
//			return "UNKNOWN";
//	}

	public static void sendMessage( String msg, boolean bar, EntityPlayer player )
	{
		player.sendStatusMessage( new TextComponentString( msg ), bar );
	}

	public static void sendMessage( String msg, boolean bar, EntityPlayer player, TextFormatting format )
	{
		player.sendStatusMessage( new TextComponentString( msg ).setStyle( new Style().setColor( format ) ), bar );
	}

	public static <T> Map<T, Double> addMapsAnyDouble(Map<T, Double> mapOne, Map<T, Double> mapTwo )
	{
		for( T key : mapTwo.keySet() )
		{
			if( mapOne.containsKey( key ) )
				mapOne.replace( key, mapOne.get( key ) + mapTwo.get( key ) );
			else
				mapOne.put( key, mapTwo.get( key ) );

		}

		return mapOne;
	}

	private static int doubleObjectToInt( Object object )
	{
		return (int) Math.floor( (double) object );
	}


	public static double getExtraChance( UUID uuid, String resLoc, JType jType, boolean offline )
	{
		return getExtraChance( uuid, XP.getResLoc( resLoc ), jType, offline );
	}

	public static double getExtraChance( UUID uuid, ResourceLocation resLoc, JType jType, boolean offline )
	{
		String regKey = resLoc.toString();
		double extraChancePerLevel = 0;
		double extraChance;
		int highestReq = 1;
		if( JsonConfig.data.get( JType.REQ_BREAK ).containsKey( resLoc.toString() ) )
			highestReq = JsonConfig.data.get( JType.REQ_BREAK ).get( resLoc.toString() ).entrySet().stream().map(a -> doubleObjectToInt( a.getValue() ) ).reduce( 0, Math::max );
		int startLevel;
		String skill;

		switch( jType )
		{
			case INFO_ORE:
				skill = Skill.MINING.toString();
				break;

			case INFO_LOG:
				skill = Skill.WOODCUTTING.toString();
				break;

			case INFO_PLANT:
				skill = Skill.FARMING.toString();
				break;

			case INFO_SMELT:
				skill = Skill.SMITHING.toString();
				break;

			case INFO_COOK:
				skill = Skill.COOKING.toString();
				break;

			case INFO_BREW:
				skill = Skill.ALCHEMY.toString();
				break;

			default:
				LOGGER.info( "WRONG getExtraChance CHANCE TYPE! PLEASE REPORT!" );
				return 0;
		}

		startLevel = offline ? XP.getOfflineLevel( skill, uuid ) : Skill.getLevel( skill, uuid );

		if( JsonConfig.data.get( jType ).containsKey( regKey ) && JsonConfig.data.get( jType ).get( regKey ).containsKey( "extraChance" ) )
			extraChancePerLevel = JsonConfig.data.get( jType ).get( regKey ).get( "extraChance" );

		extraChance = (startLevel - highestReq) * extraChancePerLevel;
		if( extraChance < 0 )
			extraChance = 0;

		return extraChance;
	}

	public static boolean hasElement( ResourceLocation key, JType jType )
	{
		return hasElement( key.toString(), jType );
	}

	public static boolean hasElement( String key, JType jType )
	{
		return JsonConfig.data.get( jType ).containsKey( key );
	}

	public static boolean rollChance( double extraChance )
	{
		return Math.random() < extraChance;
	}

	public static void dropItems(int dropsLeft, Item item, World world, BlockPos pos)
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

	public static void dropItemStack( ItemStack itemStack, World world, Vec3d pos )
	{
		dropItemStack( itemStack, world, new BlockPos( pos ) );
	}

	public static void dropItemStack( ItemStack itemStack, World world, BlockPos pos )
	{
		Block.spawnAsEntity( world, pos, itemStack );
	}

	public static boolean isPlayerSurvival( EntityPlayer player )
	{
		return !player.isCreative() && !player.isSpectator();
	}

	public static Collection<EntityPlayer> getNearbyPlayers( Entity mob )
	{
		World world = mob.getEntityWorld();
		List<? extends EntityPlayer> allPlayers = world.getMinecraftServer().getPlayerList().getPlayers();
		Collection<EntityPlayer> nearbyPlayers = new ArrayList<>();

		Double closestDistance = null;
		double tempDistance;

		for( EntityPlayer player : allPlayers )
		{
			tempDistance = mob.getDistance( player );
			if( closestDistance == null || tempDistance < closestDistance )
				closestDistance = tempDistance;
		}

		if( closestDistance != null )
		{
			double searchRange = closestDistance + 30;

			for( EntityPlayer player : allPlayers )
			{
				if( mob.getDistance( player ) < searchRange )
					nearbyPlayers.add( player );
			}
		}

		return nearbyPlayers;
	}

	public static double getPowerLevel( UUID uuid )
	{
		int enduranceLevel = Skill.getLevel( Skill.ENDURANCE.toString(), uuid );

		int combatLevel = Skill.getLevel( Skill.COMBAT.toString(), uuid );
		int archeryLevel = Skill.getLevel( Skill.ARCHERY.toString(), uuid );
		int magicLevel = Skill.getLevel( Skill.MAGIC.toString(), uuid );

		int maxOffensive = combatLevel;
		if( maxOffensive < archeryLevel )
			maxOffensive = archeryLevel;
		if( maxOffensive < magicLevel )
			maxOffensive = magicLevel;

		return ( enduranceLevel + (maxOffensive * 1.5f) ) / 50;
	}

	public static EntityPlayerMP getPlayerByUUID( UUID uuid )
	{
		return getPlayerByUUID( uuid, PmmoSavedData.getServer() );
	}

	public static EntityPlayerMP getPlayerByUUID(UUID uuid, MinecraftServer server )
	{
		EntityPlayerMP matchedPlayer = null;

		for( EntityPlayerMP player : server.getPlayerList().getPlayers() )
		{
			if( player.getUniqueID().equals( uuid ) )
			{
				matchedPlayer = player;
				break;
			}
		}

		return matchedPlayer;
	}

	public static double getDistance( Vec3d a, Vec3d b )
	{
		return Math.sqrt( Math.pow( a.x - b.x, 2 ) + Math.pow( a.y - b.y, 2 ) + Math.pow( a.z - b.z, 2 ) );
	}

	public static <T extends Entity> Set<T> getEntitiesInRange( Vec3d origin, Set<T> entities, double range )
	{
		Set<T> withinRange = new HashSet<>();
		Vec3d pos;
		for( T entity : entities )
		{
			pos = entity.getPositionVector();
			double distance = getDistance( origin, pos );
			if( distance <= range )
				withinRange.add( entity );
		}

		return withinRange;
	}

	public static void syncPlayerDataAndConfig( EntityPlayer player )
	{
		syncPlayerData3( player );
		syncPlayerData4( player );
		syncPlayerXpBoost( player );
		syncPlayersSkills( player );
		NetworkHandler.sendToPlayer( new MessageUpdateBoolean( true, 1 ), (EntityPlayerMP) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( FConfig.localConfig ), 2 ), (EntityPlayerMP) player );
	}

	public static void syncPlayersSkills( EntityPlayer player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.xpMapsToNbt( PmmoSavedData.get().getAllXpMaps() ), 3 ), (EntityPlayerMP) player );
	}

	public static void syncPlayerXpBoost( EntityPlayer player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringMapStringToNbt( FConfig.getXpBoostsMap( player ) ), 6 ), (EntityPlayerMP) player );
	}

	public static void syncPlayerData3( EntityPlayer player )
	{
		NBTTagCompound fullData = NBTHelper.data3ToNbt( JsonConfig.localData );
		NBTTagCompound dataChunk = new NBTTagCompound();
		dataChunk.setBoolean( "wipe", true );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (EntityPlayerMP) player );
		dataChunk = new NBTTagCompound();

		int i = 0;
		for( String key3 : fullData.getKeySet() )
		{
			if( !dataChunk.hasKey( key3 ) )
				dataChunk.setTag( key3, new NBTTagCompound() );

			for( String key2 : fullData.getCompoundTag( key3 ).getKeySet() )
			{
				if( i >= 1000 )	//if any single JType reaches 1000 Elements, send chunk reset count
				{
					i = 0;
					NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (EntityPlayerMP) player );
					dataChunk = new NBTTagCompound();
					dataChunk.setTag( key3, new NBTTagCompound() );
				}

				dataChunk.getCompoundTag( key3 ).setTag( key2, fullData.getCompoundTag( key3 ).getCompoundTag( key2 ) );
				i++;
			}
		}

		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (EntityPlayerMP) player );
	}

	public static void syncPlayerData4( EntityPlayer player )
	{
		NBTTagCompound fullData = NBTHelper.data4ToNbt( JsonConfig.localData2 );
		NBTTagCompound dataChunk = new NBTTagCompound();
		dataChunk.setBoolean( "wipe", true );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (EntityPlayerMP) player );
		dataChunk = new NBTTagCompound();

		int i = 0;
		for( String key4 : fullData.getKeySet() )
		{
			if( !dataChunk.hasKey( key4 ) )
				dataChunk.setTag( key4, new NBTTagCompound() );

			for( String key3 : fullData.getCompoundTag( key4 ).getKeySet() )
			{
				if( !dataChunk.getCompoundTag( key4 ).hasKey( key3 ) )
					dataChunk.getCompoundTag( key4 ).setTag( key3, new NBTTagCompound() );

				for( String key2 : fullData.getCompoundTag( key4 ).getCompoundTag( key3 ).getKeySet() )
				{
					if( i >= 1000 )	//if any single JType reaches 1000 Elements, send chunk reset count
					{
						i = 0;
						NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (EntityPlayerMP) player );
						dataChunk = new NBTTagCompound();
						dataChunk.setTag( key4, new NBTTagCompound() );
						dataChunk.getCompoundTag( key4 ).setTag( key3, new NBTTagCompound() );
					}

					dataChunk.getCompoundTag( key4 ).getCompoundTag( key3 ).setTag( key2, fullData.getCompoundTag( key4 ).getCompoundTag( key3 ).getCompoundTag( key2 ) );
					i++;
				}
			}
		}

		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (EntityPlayerMP) player );
	}

	public static void syncPlayer( EntityPlayer player )
	{
//    	UUID uuid = player.getUniqueID();
//        NBTTagCompound xpTag 		 = NBTHelper.mapSkillToNbt ( FConfig.getXpMap( player ) );
		NBTTagCompound prefsTag = NBTHelper.mapStringToNbt( FConfig.getPreferencesMap( player ) );
		NBTTagCompound abilitiesTag = NBTHelper.mapStringToNbt( FConfig.getAbilitiesMap( player ) );

		syncPlayerDataAndConfig( player );
		syncPlayerXpBoost( player );
		updateRecipes( (EntityPlayerMP) player );

		NetworkHandler.sendToPlayer( new MessageXp( 0f, "42069", 0f, true ), (EntityPlayerMP) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( prefsTag, 0 ), (EntityPlayerMP) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( abilitiesTag, 1 ), (EntityPlayerMP) player );
		AttributeHandler.updateAll( player );

		for( Map.Entry<String, Double> entry : FConfig.getXpMap( player ).entrySet() )
		{
			NetworkHandler.sendToPlayer( new MessageXp( entry.getValue(), entry.getKey(), 0, true ), (EntityPlayerMP) player );
		}
	}

	public static boolean checkReq( EntityPlayer player, String res, JType jType )
	{
		return checkReq( player, XP.getResLoc( res ), jType );
	}

	public static boolean checkReq( EntityPlayer player, ResourceLocation res, JType jType )
	{
		if( res == null )
			return true;

		if( res.equals( Items.AIR.getRegistryName() ) || player.isCreative() )
			return true;

		return checkReq( player, getJsonMap( res.toString(), jType ) );
	}

	public static boolean checkReq( EntityPlayer player, Map<String, Double> reqMap )
	{
		boolean failedReq = false;

		try
		{
			if( JsonConfig.data.get( JType.PLAYER_SPECIFIC ).containsKey( player.getUniqueID().toString() ) )
			{
				if( JsonConfig.data.get( JType.PLAYER_SPECIFIC ).get( player.getUniqueID().toString() ).containsKey( "ignoreReq" ) )
					return true;
			}
			double startLevel;

//		if( reqMap == null )
//			failedReq = true;

			if( reqMap != null )
			{
				for( Map.Entry<String, Double> entry : reqMap.entrySet() )
				{
					startLevel = Skill.getLevel( entry.getKey(), player );

					if( startLevel < entry.getValue() )
						failedReq = true;
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error( e );
		}

		return !failedReq;
	}

	public static ResourceLocation getResLoc( String regKey )
	{
		try
		{
			return new ResourceLocation( regKey.replaceAll( " ", "" ).trim() );
		}
		catch( Exception e )
		{
			return new ResourceLocation( "" );
		}
	}

	public static ResourceLocation getResLoc( String firstPart, String secondPart )
	{
		try
		{
			return new ResourceLocation( firstPart, secondPart );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public static Map<String, Double> getJsonMap( ResourceLocation registryName, JType type )
	{
		return getJsonMap( registryName.toString(), type );
	}

	public static Map<String, Double> getJsonMap( String registryName, JType type )
	{
		return JsonConfig.data.getOrDefault( type, new HashMap<>() ).getOrDefault( registryName, new HashMap<>() );
	}

	public static int getHighestReq( String regKey, JType jType )
	{
		int highestReq = 1;

		Map<String, Double> map = XP.getJsonMap( regKey, jType );

		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			if( highestReq < entry.getValue() )
				highestReq = (int) (double) entry.getValue();
		}

		return highestReq;
	}

	public static Item getItem( String regKey )
	{
		ResourceLocation resLoc = getResLoc( regKey );

		Item item = ForgeRegistries.ITEMS.getValue( resLoc );

		if( item != null )
			return item;

		return Items.AIR;
	}

	public static Item getItem( ResourceLocation resLoc )
	{
		return getItem( resLoc.toString() );
	}

	public static boolean scanBlock( Block block, int radius, EntityPlayer player )
	{
		Block currBlock;
		BlockPos playerPos = vecToBlock( player.getPositionVector() );
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

//	public static NBTTagCompound getPmmoTag( EntityPlayer player )
//	{
//		if( player != null )
//		{
//			NBTTagCompound persistTag = player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG );
//			NBTTagCompound pmmoTag = null;
//
//			if( !persistTag.contains( Reference.MOD_ID ) )			//if Player doesn't have pmmo tag, make it
//			{
//				pmmoTag = new NBTTagCompound();
//				persistTag.setTag( Reference.MOD_ID, pmmoTag );
//			}
//			else
//			{
//				pmmoTag = persistTag.getCompoundTag( Reference.MOD_ID );	//if Player has pmmo tag, use it
//			}
//
//			return pmmoTag;
//		}
//		else
//			return new NBTTagCompound();
//	}
//
//	public static NBTTagCompound getPmmoTagElement( EntityPlayer player, String element )
//	{
//		if( player != null )
//		{
//			NBTTagCompound pmmoTag = getPmmoTag( player );
//			NBTTagCompound elementTag = null;
//
//			if( !pmmoTag.contains( element ) )					//if Player doesn't have element tag, make it
//			{
//				elementTag = new NBTTagCompound();
//				pmmoTag.setTag( element, elementTag );
//			}
//			else
//			{
//				elementTag = pmmoTag.getCompoundTag( element );	//if Player has element tag, use it
//			}
//
//			return elementTag;
//		}
//		else
//			return new NBTTagCompound();
//	}
//
//	public static NBTTagCompound getxpMap( EntityPlayer player )
//	{
//		return getPmmoTagElement( player, "skills" );
//	}
//
//	public static NBTTagCompound getPreferencesTag( EntityPlayer player )
//	{
//		return getPmmoTagElement( player, "preferences" );
//	}
//
//	public static NBTTagCompound getabilitiesMap( EntityPlayer player )
//	{
//		return getPmmoTagElement( player, "abilities" );
//	}

	public static double getXpBoostDurabilityMultiplier( ItemStack itemStack )
	{
		double scale = 1;
		if( itemStack.isItemStackDamageable() )
		{
			double durabilityPercentage = 1 - itemStack.getItemDamage() / (double) itemStack.getMaxDamage();
			double scaleStart = FConfig.getConfig( "scaleXpBoostByDurabilityStart" ) / 100D;
			double scaleEnd = Math.max( scaleStart, FConfig.getConfig( "scaleXpBoostByDurabilityEnd" ) / 100D );
			scale = DP.mapCapped( durabilityPercentage, scaleStart, scaleEnd, 0, 1 );
		}
		return scale;
	}

	public static Map<String, Double> getStackXpBoosts( ItemStack itemStack, boolean type /*false = worn, true = held*/ )
	{
		Item item = itemStack.getItem();
		JType jType = type ? JType.XP_BONUS_HELD : JType.XP_BONUS_WORN;
		String regName = item.getRegistryName().toString();
		Map<String, Double> itemXpMap = JsonConfig.data.get( jType ).getOrDefault( regName, new HashMap<>() );
		if( FConfig.getConfig( "scaleXpBoostByDurability" ) != 0 )
			multiplyMapAnyDouble( itemXpMap, getXpBoostDurabilityMultiplier( itemStack ) );
		return itemXpMap;
	}

	public static double getStackXpBoost( EntityPlayer player, ItemStack itemStack, String skill, boolean type /*false = worn, true = held*/ )
	{
		if( itemStack == null || itemStack.isEmpty() )
			return 0;

		Item item = itemStack.getItem();
		JType jType = type ? JType.XP_BONUS_HELD : JType.XP_BONUS_WORN;
		double boost = 0;

		String regName = item.getRegistryName().toString();
		Map<String, Double> itemXpMap = JsonConfig.data.get( jType ).get( regName );

		if( itemXpMap != null && itemXpMap.containsKey( skill ) )
		{
			if( type || checkReq( player, item.getRegistryName(), JType.REQ_WEAR ) )
			{
				boost = itemXpMap.get( skill );
				if( FConfig.getConfig( "scaleXpBoostByDurability" ) != 0 && itemStack.isItemStackDamageable() )
					boost *= getXpBoostDurabilityMultiplier( itemStack );
			}
		}

		return boost;
	}

	public static double getGlobalMultiplier( String skill )
	{
		return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill.toString(), 1D );
	}

	public static double getDimensionMultiplier(String skill, EntityPlayer player )
	{
		try
		{
			String dimensionId = Integer.toString( player.world.getWorldType().getId() );
			return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( dimensionId, new HashMap<>() ).getOrDefault( skill.toString(), 1D );
		}
		catch( Exception e )
		{
			return 1D;
		}
	}

	public static double getDifficultyMultiplier( EntityPlayer player, String skill )
	{
		double difficultyMultiplier = 1;

		if( Skill.COMBAT.equals( skill ) || Skill.ARCHERY.equals( skill ) || Skill.ENDURANCE.equals( skill ) )
		{
			switch( player.world.getDifficulty() )
			{
				case PEACEFUL:
					difficultyMultiplier = FConfig.peacefulMultiplier;
					break;

				case EASY:
					difficultyMultiplier = FConfig.easyMultiplier;
					break;

				case NORMAL:
					difficultyMultiplier = FConfig.normalMultiplier;
					break;

				case HARD:
					difficultyMultiplier = FConfig.hardMultiplier;
					break;

				default:
					break;
			}
		}

		return difficultyMultiplier;
	}

	public static double getItemBoost( EntityPlayer player, String skill )
	{
		if( player.getHeldItemMainhand().getItem().getRegistryName() == null )
			return 0;

		double itemBoost = 0;
		InventoryPlayer inv = player.inventory;

		if( BaublesHandler.isLoaded() )
		{
			for( ItemStack stack : BaublesHandler.getBaublesItems( player ) )
			{
				itemBoost += getStackXpBoost( player, stack, skill, false );
			}
		}

		itemBoost += getStackXpBoost( player, player.getHeldItemMainhand(), skill, true );

		if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
			itemBoost += getStackXpBoost( player, inv.getStackInSlot( 39 ), skill, false );
		if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
			itemBoost += getStackXpBoost( player, inv.getStackInSlot( 38 ), skill, false );
		if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
			itemBoost += getStackXpBoost( player, inv.getStackInSlot( 37 ), skill, false );
		if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
			itemBoost += getStackXpBoost( player, inv.getStackInSlot( 36 ), skill, false );
		if( !inv.getStackInSlot( 40 ).isEmpty() )	//Off-Hand 40
			itemBoost += getStackXpBoost( player, player.getHeldItemOffhand(), skill, false );

		return itemBoost;
	}

	public static Map<String, Double> getDimensionBoosts( String dimKey )
	{
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( dimKey, new HashMap<>() );
	}

	public static double getDimensionBoost( EntityPlayer player, String skill )
	{
		String dimensionId = Integer.toString( player.world.getWorldType().getId() );
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( dimensionId, new HashMap<>() ).getOrDefault( skill.toString(), 0D );
	}

	public static double getGlobalBoost( String skill )
	{
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill.toString(), 0D );
	}

	public static NBTTagCompound writeUniqueId( UUID uuid )
	{
		NBTTagCompound NBTTagCompound = new NBTTagCompound();
		NBTTagCompound.setLong("M", uuid.getMostSignificantBits());
		NBTTagCompound.setLong("L", uuid.getLeastSignificantBits());
		return NBTTagCompound;
	}

	public static Map<String, Double> getBiomeBoosts( EntityPlayer player )
	{
		Map<String, Double> biomeBoosts = new HashMap<>();
		double biomePenaltyMultiplier = FConfig.getConfig( "biomePenaltyMultiplier" );

		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVector() ) );
		ResourceLocation resLoc = biome.getRegistryName();
		if( resLoc == null )
			return new HashMap<>();
		String biomeKey = resLoc.toString();
		Map<String, Double> biomeMap = getJsonMap( biomeKey, JType.XP_BONUS_BIOME );

		if( biomeMap != null )
		{
			boolean metReq = checkReq( player, resLoc, JType.REQ_BIOME );
			for( Map.Entry<String, Double> entry : biomeMap.entrySet() )
			{
				biomeBoosts.put( entry.getKey(), metReq ? entry.getValue() : Math.min( entry.getValue(), -biomePenaltyMultiplier * 100 ) );
			}
		}

		return biomeBoosts;
	}

	public static double getMultiplier( EntityPlayer player, String skill )
	{
		double multiplier = FConfig.globalMultiplier;

		double globalMultiplier = getGlobalMultiplier( skill );
		double dimensionMultiplier = getDimensionMultiplier( skill, player );
		double difficultyMultiplier = getDifficultyMultiplier( player, skill );
		double globalBoost = getGlobalBoost( skill );
		double itemBoost = getItemBoost( player, skill );
		double biomeBoost = getBiomeBoosts( player ).getOrDefault( skill.toString(), 0D );
		double dimensionBoost = getDimensionBoost( player, skill );
		double playerBoost = PmmoSavedData.get().getPlayerXpBoost( player.getUniqueID(), skill );
		double additiveMultiplier = 1 + (itemBoost + biomeBoost + dimensionBoost + globalBoost + playerBoost ) / 100;

		multiplier *= globalMultiplier;
		multiplier *= dimensionMultiplier;
		multiplier *= difficultyMultiplier;
		multiplier *= additiveMultiplier;

		return Math.max( 0, multiplier );
	}

	public static double getHorizontalDistance( Vec3d p1, Vec3d p2 )
	{
		return Math.sqrt( Math.pow( ( p1.x - p2.x ), 2 ) + Math.pow( ( p1.z - p2.z ), 2 ) );
	}

	public static int getMaxVein( EntityPlayer player, String skill )
	{
		int maxVein = 0;
		int level = Skill.getLevel( skill, player ) - 1;

		switch( skill )
		{
			case "mining":
				maxVein = level / 5;
				break;

			case "woodcutting":
				maxVein = level / 2;
				break;

			case "excavation":
				maxVein = level;
				break;

			case "farming":
				maxVein = level;
				break;
		}

		return maxVein;
	}

	public static void awardXp(EntityPlayerMP player, String skill, @Nullable String sourceName, double amount, boolean skip, boolean ignoreBonuses, boolean causedByParty )
	{
//		if( !(player instanceof EntityPlayerMP) )
//		{
//			LOGGER.info( "NOT EntityPlayerMP PLAYER XP AWARD ATTEMPTED! THIS SHOULD NOT HAPPEN! SOURCE: " + sourceName + ", SKILL: " + skill + ", AMOUNT: " + amount + ", CLASS: " + player.getClass().getName().toString() );
//			return;
//		}

		if( player.world.isRemote || player instanceof FakePlayer )
			return;

		PmmoSavedData pmmoSavedData = PmmoSavedData.get();
		UUID uuid = player.getUniqueID();

		if( !causedByParty )
		{
			Party party = pmmoSavedData.getParty( uuid );
			if( party != null )
			{
				Set<EntityPlayerMP> membersInRange = party.getOnlineMembersInRange( player );
				int membersInRangeSize = membersInRange.size();
				double partyMultiplier = party.getMultiplier( membersInRangeSize );
				amount *= partyMultiplier;
				party.submitXpGained( uuid, amount );
				amount /= membersInRangeSize + 1D;
				for( EntityPlayerMP partyMember : membersInRange )
				{
					awardXp( partyMember, skill, sourceName, amount, true, ignoreBonuses, true );
				}
			}
		}

		if( !ignoreBonuses )
			amount *= getMultiplier( player, skill );

		String playerName = player.getDisplayName().getUnformattedText();
		int startLevel = Skill.getLevel( skill, uuid );
		double startXp = Skill.getXp( skill, uuid );
		double maxXp = FConfig.getConfig( "maxXp" );

		if( amount == 0 || startXp >= 2000000000 )
			return;

		if( startXp + amount >= 2000000000 )
		{
			sendMessage( skill + " cap of 2b xp reached, you fucking psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LOGGER.info( player.getDisplayName().getUnformattedText() + " " + skill + " 2b cap reached" );
			amount = 2000000000 - startXp;
		}

		pmmoSavedData.addXp( skill, uuid, amount );

		int currLevel = Skill.getLevel( skill, uuid );

		if( startLevel != currLevel ) //Level Up!
		{
			AttributeHandler.updateAll( player );
			updateRecipes( player );

			if( Loader.isModLoaded( "compatskills" ) )
			{
				if( !player.world.isRemote )
				{
					if( skill.equals( Skill.MINING.toString() ) || skill.equals( Skill.BUILDING.toString() ) || skill.equals( Skill.FARMING.toString() ) || skill.equals( Skill.AGILITY.toString() ) )
						player.getServer().commandManager.executeCommand( player, "/reskillable incrementskill " + playerName + " reskillable." + skill.toString() + " 1" );
					else
						player.getServer().commandManager.executeCommand( player, "/reskillable incrementskill " + playerName + " compatskills." + skill.toString() + " 1" );
				}
			}

			if( JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skill.toString() ) != null )
			{
				Map<String, Double> commandMap = JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skill.toString() );

				for( Map.Entry<String, Double> entry : commandMap.entrySet() )
				{
					int commandLevel = (int) Math.floor( entry.getValue() );
					if( startLevel < commandLevel && currLevel >= commandLevel )
					{
						String command = entry.getKey().replace( ">player<", playerName ).replace( ">level<", "" + commandLevel );
						try
						{
							player.getServer().commandManager.executeCommand( player, command );
							LOGGER.info( "Executing command \"" + command + "\"\nTrigger: " + playerName + " level up from " + startLevel + " to " + currLevel + " in " + skill + ", trigger level " + commandLevel );
						}
						catch( Exception e )
						{
							LOGGER.info( "Invalid level up command \"" + command + "\"" + e.toString() );
						}
					}
				}
			}
		}

		NetworkHandler.sendToPlayer( new MessageXp( startXp, skill, amount, skip ), (EntityPlayerMP) player );
		if( !skip )
			LOGGER.info( playerName + " +" + amount + "xp in: "  + skill + " for: " + sourceName + " total xp: " + Skill.getXp( skill, uuid ) );

		if( startXp + amount >= maxXp && startXp < maxXp )
		{
			sendMessage( skill + " max startLevel reached, you psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LOGGER.info( playerName + " " + skill + " max startLevel reached" );
		}
	}

	public static void awardXpTrigger( UUID uuid, String triggerKey, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		if( JsonConfig.data.get( JType.XP_VALUE_TRIGGER ).containsKey( triggerKey ) )
		{
			awardXpMap( uuid, JsonConfig.data.get( JType.XP_VALUE_TRIGGER ).get( triggerKey ), sourceName, skip, ignoreBonuses );
		}
		else
			LOGGER.info( "TRIGGER XP AWARD \"" + triggerKey + "\" DOES NOT HAVE ANY VALUES, CANNOT AWARD" );
	}

	public static void awardXpMap(UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			Skill.addXp( entry.getKey(), uuid, (double) entry.getValue(), sourceName, skip, ignoreBonuses );
		}
	}

	public static void awardXpMapDouble( UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			Skill.addXp( entry.getKey(), uuid, entry.getValue(), sourceName, skip, ignoreBonuses );
		}
	}

	public static void updateRecipes( EntityPlayerMP player )
	{
//		if( FConfig.craftReqEnabled.get() )
//		{
//			Collection<IRecipe<?>> allRecipes = player.getServer().getRecipeManager().getRecipes();
//			Collection<IRecipe<?>> removeRecipes = new HashSet<>();
//			Collection<IRecipe<?>> newRecipes = new HashSet<>();
//
//			for( IRecipe<?> recipe : allRecipes )
//			{
//				if( XP.checkReq( player, recipe.getRecipeOutput().getItem().getRegistryName(), JType.REQ_CRAFT ) )
//					newRecipes.add( recipe );
//				else
//					removeRecipes.add( recipe );
//			}
//
//			player.getRecipeBook().remove( removeRecipes, player );
//			player.getRecipeBook().add( newRecipes, player );
//		}
	}
	//COUT RECIPE LOCK

	public static void scanUnlocks( int level, String skill )
	{

	}

	private static int getGap( int a, int b )
	{
		return a - b;
	}

	public static int getSkillReqGap( EntityPlayer player, ResourceLocation res, JType jType )
	{
		Map<String, Double> reqs = getJsonMap( res.toString(), jType );

		if( reqs == null )
			return 0;

		return getSkillReqGap( player, reqs );
	}

	public static int getSkillReqGap( EntityPlayer player, Map<String, Double> reqs )
	{
		int gap = 0;

		if( !checkReq( player, reqs ) )
		{
			if( reqs != null )
			{
				gap = (int) Math.floor( reqs.entrySet().stream()
						.map( entry -> getGap( (int) Math.floor( entry.getValue() ), Skill.getLevel( entry.getKey(), player ) ) )
						.reduce( 0, Math::max ) );
			}
		}

		return gap;
	}

	public static BlockPos vecToBlock( Vec3d pos )
	{
		return new BlockPos( pos );
	}

	public static Vec3d blockToVec( BlockPos pos )
	{
		return new Vec3d( pos.getX(), pos.getY(), pos.getZ() );
	}

	public static Vec3d blockToMiddleVec( BlockPos pos )
	{
		return new Vec3d( pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D );
	}

	public static void spawnRocket( World world, BlockPos pos, String skill )
	{
		spawnRocket( world, new Vec3d( pos.getX(), pos.getY(), pos.getZ() ), skill );
	}

	public static void spawnRocket( World world, Vec3d pos, String skill )
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag( "Fireworks", new NBTTagCompound() );
		NBTTagCompound fwTag = tag.getCompoundTag( "Fireworks" );
		NBTTagList expList = new NBTTagList();
		fwTag.setTag( "Explosions", expList );

		NBTTagCompound expTag = fwTag.getCompoundTag( "Explosions" );

		int[] colors = new int[1];
		colors[0] = Skill.getSkillColor( skill );

		fwTag.setInteger( "Flicker", 1 );
		fwTag.setInteger( "Trail", 1 );
		fwTag.setInteger( "Type", 1 );
		expTag.setTag( "Colors", new NBTTagIntArray( colors ) );

		expList.appendTag( expTag );

		ItemStack itemStack = new ItemStack( Items.FIREWORKS );
		itemStack.setTagCompound( tag );

		EntityFireworkRocket fireworkRocketEntity = new EntityFireworkRocket( world, pos.x + 0.5D, pos.y + 0.5D, pos.z + 0.5D, itemStack );
		world.spawnEntity( fireworkRocketEntity );
	}

	public static <T> Map<T, Double> ceilMapAnyDouble( Map<T, Double> input )
	{
		for( Map.Entry<T, Double> entry : input.entrySet() )
		{
			input.replace( entry.getKey(), Math.ceil( entry.getValue() ) );
		}

		return input;
	}

	public static <T> Map<T, Double> multiplyMapAnyDouble( Map<T, Double> input, double multiplier )
	{
		for( Map.Entry<T, Double> entry : input.entrySet() )
		{
			input.put( entry.getKey(), entry.getValue() * multiplier );
		}

		return input;
	}

	public static <T> Map<T, Double> mergeMapHighestValue( Map<T, Double> map1, Map<T, Double> map2 )
	{
		for( Map.Entry<T, Double> entry : map2.entrySet() )
		{
			if( !map1.containsKey( entry.getKey() ) )
				map1.put( entry.getKey(), entry.getValue() );
			else if( map1.get( entry.getKey() ) < entry.getValue() )
				map1.put( entry.getKey(), entry.getValue() );
		}

		return map1;
	}

	public static Map<String, Double> getEnchantUseReq( ResourceLocation resLoc, int enchantLevel )
	{
		Map<String, Double> reqs = new HashMap<>();
		String skill;
		int highestSpecifiedLevel = 0;

		if( JsonConfig.data2.getOrDefault( JType.REQ_USE_ENCHANTMENT, new HashMap<>() ).containsKey( resLoc.toString() ) )
		{
			Map<String, Map<String, Double>> levelMaps = JsonConfig.data2.get( JType.REQ_USE_ENCHANTMENT ).get( resLoc.toString() );
			List<Integer> levels = new ArrayList<>();

			for( String levelKey : levelMaps.keySet() )
			{
				levels.add( Integer.parseInt( levelKey ) );
			}

			levels.sort( Comparator.comparingInt( ( a -> a ) ) );

			for( int level : levels )
			{
				if( level > enchantLevel )
					break;
				highestSpecifiedLevel = level;

				Map<String, Double> skillMap = levelMaps.get( String.valueOf( level ) );
				for( Map.Entry<String, Double> skillElement : skillMap.entrySet() )
				{
					skill = skillElement.getKey();
					if( !reqs.containsKey( skill ) )
						reqs.put( skill, skillElement.getValue() );
					else if( reqs.get( skill ) < skillElement.getValue() )
						reqs.put( skill, skillElement.getValue() );
				}
			}
		}

		if( FConfig.enchantUseReqAutoScaleEnabled && enchantLevel > highestSpecifiedLevel )
			multiplyMapAnyDouble( reqs, enchantLevel / (double) highestSpecifiedLevel );

		return reqs;
	}

	public static Map<String, Double> getEnchantsUseReq( ItemStack itemStack )
	{
		Map<String, Double> reqs = new HashMap<>();
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments( itemStack );

		for( Map.Entry<Enchantment, Integer> entry : enchantments.entrySet() )
		{
			mergeMapHighestValue( reqs, getEnchantUseReq( entry.getKey().getRegistryName(), entry.getValue() ) );
		}

		return reqs;
	}

	public static void applyWornPenalty( EntityPlayer player, ItemStack itemStack )
	{
		ResourceLocation resLoc = itemStack.getItem().getRegistryName();
		Map<String, Double> wearReq = XP.getJsonMap( resLoc, JType.REQ_WEAR );
		if( !wearReq.containsKey( Skill.ENDURANCE.toString() ) && FConfig.getConfig( "autoGenerateValuesEnabled" ) != 0 && FConfig.getConfig( "autoGenerateWearReqDynamicallyEnabled" ) != 0 )
			wearReq.put( Skill.ENDURANCE.toString(), AutoValues.getWearReqFromStack( itemStack ) );

		if( !checkReq( player, wearReq ) )
		{
			int gap = getSkillReqGap( player, resLoc, JType.REQ_WEAR );

			if( gap > 9 )
				gap = 9;

			player.addPotionEffect( new PotionEffect( MobEffects.MINING_FATIGUE, 75, gap, false, true ) );
			player.addPotionEffect( new PotionEffect( MobEffects.WEAKNESS, 75, gap, false, true ) );
			player.addPotionEffect( new PotionEffect( MobEffects.SLOWNESS, 75, gap, false, true ) );

			if( FConfig.strictReqWear || EnchantmentHelper.hasBindingCurse( itemStack ) )
			{
				ItemStack droppedItemStack = itemStack.copy();
				player.dropItem( droppedItemStack, false, false );
				itemStack.setCount( 0 );
				player.sendStatusMessage( new TextComponentTranslation( "pmmo.gotTooHotDroppedItem", new TextComponentTranslation( droppedItemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), true );
				player.sendStatusMessage( new TextComponentTranslation( "pmmo.gotTooHotDroppedItem", new TextComponentTranslation( droppedItemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), false );
			}
			else
				player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToWear", new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), true );
		}

		applyEnchantmentUsePenalty( player, itemStack );
	}

	public static void applyEnchantmentUsePenalty( EntityPlayer player, ItemStack itemStack )
	{
		ResourceLocation resLoc = itemStack.getItem().getRegistryName(), enchantResLoc;
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments( itemStack );

		for( Map.Entry<Enchantment, Integer> entry : enchantments.entrySet() )
		{
			enchantResLoc = entry.getKey().getRegistryName();
			if( !checkReq( player, getEnchantUseReq( enchantResLoc, entry.getValue() ) ) )
			{
				int gap = getSkillReqGap( player, resLoc, JType.REQ_USE_ENCHANTMENT );

				if( gap > 9 )
					gap = 9;

				player.addPotionEffect( new PotionEffect( MobEffects.MINING_FATIGUE, 75, gap, false, true ) );
				player.addPotionEffect( new PotionEffect( MobEffects.WEAKNESS, 75, gap, false, true ) );
				player.addPotionEffect( new PotionEffect( MobEffects.SLOWNESS, 75, gap, false, true ) );

				if( FConfig.strictReqUseEnchantment || EnchantmentHelper.hasBindingCurse( itemStack ) )
				{
					ItemStack droppedItemStack = itemStack.copy();
					player.dropItem( droppedItemStack, false, false );
					itemStack.setCount( 0 );
					player.sendStatusMessage( new TextComponentTranslation( "pmmo.gotTooHotDroppedItem", new TextComponentTranslation( droppedItemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), true );
					player.sendStatusMessage( new TextComponentTranslation( "pmmo.gotTooHotDroppedItem", new TextComponentTranslation( droppedItemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), false );
				}
				else
					player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToUseEnchantment", new TextComponentTranslation( entry.getKey().getName() ), new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( textStyle.get( "red" ) ), true );
			}
		}
	}

	public static void checkBiomeLevelReq(EntityPlayer player)
	{
		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVector() ) );
		ResourceLocation resLoc = biome.getRegistryName();
		if( resLoc == null )
			return;
		String biomeKey = resLoc.toString();
		UUID playerUUID = player.getUniqueID();
		Map<String, Double> biomeReq = JsonConfig.data.get( JType.REQ_BIOME ).get( biomeKey );
		Map<String, Map<String, Double>> negativeEffects = JsonConfig.data.get( JType.BIOME_EFFECT_NEGATIVE );
		Map<String, Map<String, Double>> positiveEffects = JsonConfig.data.get( JType.BIOME_EFFECT_POSITIVE );

		if( !lastBiome.containsKey( playerUUID ) )
			lastBiome.put( playerUUID, "none" );

		if( checkReq( player, resLoc, JType.REQ_BIOME ) )
		{
			if( positiveEffects != null )
			{
				Map<String, Double> positiveEffect = positiveEffects.get( biomeKey );
				if( positiveEffect != null )
				{
					for( Map.Entry<String, Double> entry : positiveEffect.entrySet() )
					{
						Potion effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );

						if( effect != null )
							player.addPotionEffect( new PotionEffect( effect, 75, (int) Math.floor( (double) entry.getValue() ), false, false ) );
					}
				}
			}
		}
		else if( negativeEffects != null )
		{
			Map<String, Double> negativeEffect = negativeEffects.get( biomeKey );
			if( negativeEffect != null )
			{
				for( Map.Entry<String, Double> entry : negativeEffect.entrySet() )
				{
					Potion effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );

					if( effect != null )
						player.addPotionEffect( new PotionEffect( effect, 75, (int) Math.floor( (double) entry.getValue() ), false, true ) );
				}
				if( player.world.isRemote )
				{
					if( !lastBiome.get( playerUUID ).equals( biomeKey ) )
					{
						player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToSurvive", new TextComponentTranslation( biome.getRegistryName().toString() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToSurvive", new TextComponentTranslation( biome.getRegistryName().toString() ) ).setStyle( textStyle.get( "red" ) ), false );
						for( Map.Entry<String, Double> entry : biomeReq.entrySet() )
						{
							int startLevel = Skill.getLevel( entry.getKey(), player );

							if( startLevel < entry.getValue() )
								player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelDisplay", " " + new TextComponentTranslation( "pmmo." + entry.getKey() ).getUnformattedText(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "red" ) ), false );
							else
								player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelDisplay", " " + new TextComponentTranslation( "pmmo." + entry.getKey() ).getUnformattedText(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "green" ) ), false );
						}
					}
				}
			}
		}

		lastBiome.put( playerUUID, biomeKey );
	}

	public static double getWeight( int startLevel, Map<String, Double> fishItem )
	{
		return DP.mapCapped( startLevel, fishItem.get( "startLevel" ), fishItem.get( "endLevel" ), fishItem.get( "startWeight" ), fishItem.get( "endWeight" ) );
	}

	public static <T> int getTotalLevelFromMap( Map<T, Double> input )
	{
		int sum = 0;

		for( double xp : input.values() )
		{
			sum += levelAtXp( xp );
		}

		return sum;
	}

	public static Map<String, Double> getOfflineXpMap( UUID uuid )
	{
		if( !offlineXp.containsKey( uuid ) )
			offlineXp.put( uuid, new HashMap<>() );
		return offlineXp.get( uuid );
	}

	public static void setOfflineXpMaps( Map<UUID, Map<String, Double>> newOfflineXp )
	{
		offlineXp = new HashMap<>( newOfflineXp );
	}

	public static void setOfflineXpMap( UUID uuid, Map<String, Double> newOfflineXp )
	{
		offlineXp.put( uuid, newOfflineXp );
	}

	public static void removeOfflineXpUuid( UUID uuid )
	{
		offlineXp.remove( uuid );
	}

	public static int getOfflineLevel( String skill, UUID uuid )
	{
		return levelAtXp( getOfflineXp( skill, uuid ) );
	}

	public static double getOfflineXp( String skill, UUID uuid )
	{
		return offlineXp.getOrDefault( uuid, new HashMap<>() ).getOrDefault( skill, 0D );
	}

	public static double logBase( double base, double goal )
	{
		return Math.log( goal ) / Math.log( base );
	}

	public static int levelAtXp( double xp )
	{
		boolean useExponentialFormula = FConfig.getConfig("useExponentialFormula") != 0;
		double baseXp = FConfig.getConfig( "baseXp" );
		double exponentialBaseXp = FConfig.getConfig( "exponentialBaseXp" );
		double exponentialBase = FConfig.getConfig( "exponentialBase" );
		double exponentialRate = FConfig.getConfig( "exponentialRate" );
		int maxLevel = (int) Math.floor( FConfig.getConfig( "maxLevel" ) );

		double xpIncreasePerLevel = FConfig.getConfig( "xpIncreasePerLevel" );

		int theXp = 0;

		for( int startLevel = 0; ; startLevel++ )
		{
			if( xp < theXp || startLevel >= maxLevel )
			{
				return startLevel;
			}

			if( useExponentialFormula )
				theXp += exponentialBaseXp * Math.pow( exponentialBase, exponentialRate * ( startLevel ) );
			else
				theXp += baseXp + startLevel * xpIncreasePerLevel;
		}
	}

	public static double levelAtXpDecimal( double xp )
	{
		int maxLevel = (int) Math.floor( FConfig.getConfig( "maxLevel" ) );

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

	public static double xpAtLevel( int givenLevel )
	{
		return xpAtLevel( (double) givenLevel );
	}
	public static double xpAtLevel( double givenLevel )
	{
		boolean useExponentialFormula = FConfig.getConfig("useExponentialFormula") != 0;

		double baseXp = FConfig.getConfig( "baseXp" );
		double exponentialBaseXp = FConfig.getConfig( "exponentialBaseXp" );
		double exponentialBase = FConfig.getConfig( "exponentialBase" );
		double exponentialRate = FConfig.getConfig( "exponentialRate" );

		int maxLevel = (int) Math.floor( FConfig.getConfig( "maxLevel" ) );
		if( givenLevel > maxLevel )
			givenLevel = maxLevel;
		double theXp = 0;

		double xpIncreasePerLevel = FConfig.getConfig( "xpIncreasePerLevel" );

		for( int startLevel = 1; startLevel < givenLevel; startLevel++ )
		{
			if( useExponentialFormula )
				theXp += exponentialBaseXp * Math.pow( exponentialBase, exponentialRate * ( startLevel - 1 ) );
			else
				theXp += baseXp + ( startLevel - 1 ) * xpIncreasePerLevel;
		}

		return theXp;
	}

	public static double xpAtLevelDecimal( double givenLevel )
	{
		double startXp = xpAtLevel( Math.floor( givenLevel ) );
		double endXp   = xpAtLevel( Math.floor( givenLevel + 1 ) );
		double pos = givenLevel - Math.floor( givenLevel );

		return startXp + ( ( endXp - startXp ) * pos );
	}
}