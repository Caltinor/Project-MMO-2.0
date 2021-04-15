package harmonised.pmmo.util;

import java.util.*;
import java.util.stream.Collectors;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.gui.WorldRenderHandler;
import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.*;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.CheeseTracker;
import harmonised.pmmo.skills.PMMOFireworkEntity;
import harmonised.pmmo.skills.Skill;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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

	public static Style getColorStyle( int color )
	{
		return Style.EMPTY.setColor( Color.fromInt( color ) );
	}

	public static void initValues()
	{
////////////////////////////////////Style//////////////////////////////////////////////
		WorldText.init();
		textStyle.put( "red", 			Style.EMPTY.applyFormatting( TextFormatting.RED ) );
		textStyle.put( "green", 		Style.EMPTY.applyFormatting( TextFormatting.GREEN ) );
		textStyle.put( "dark_green", 	Style.EMPTY.applyFormatting( TextFormatting.DARK_GREEN ) );
		textStyle.put( "yellow", 		Style.EMPTY.applyFormatting( TextFormatting.YELLOW ) );
		textStyle.put( "grey", 			Style.EMPTY.applyFormatting( TextFormatting.GRAY ) );
		textStyle.put( "cyan", 			Style.EMPTY.applyFormatting( TextFormatting.AQUA ) );
		textStyle.put( "blue", 			Style.EMPTY.applyFormatting( TextFormatting.BLUE ) );
		textStyle.put( "dark_blue", 	Style.EMPTY.applyFormatting( TextFormatting.DARK_BLUE ) );
		textStyle.put( "pink", 			Style.EMPTY.applyFormatting( TextFormatting.LIGHT_PURPLE ) );
		textStyle.put( "dark_purple", 	Style.EMPTY.applyFormatting( TextFormatting.DARK_PURPLE ) );
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
		materialHarvestTool.put( Material.SHULKER, "pickaxe" );
		materialHarvestTool.put( Material.BARRIER, "pickaxe" );
		materialHarvestTool.put( Material.MISCELLANEOUS, "pickaxe" );

		materialHarvestTool.put( Material.WOOD, "axe" );			//AXE
		materialHarvestTool.put( Material.LEAVES, "axe" );
		materialHarvestTool.put( Material.GOURD, "axe" );

		materialHarvestTool.put( Material.CLAY, "shovel" );			//SHOVEL
		materialHarvestTool.put( Material.EARTH, "shovel" );
		materialHarvestTool.put( Material.SAND, "shovel" );
		materialHarvestTool.put( Material.SNOW, "shovel" );
		materialHarvestTool.put( Material.SEA_GRASS, "shovel" );
		materialHarvestTool.put( Material.SNOW_BLOCK, "shovel" );

		materialHarvestTool.put( Material.PLANTS, "hoe" );			//HOE
		materialHarvestTool.put( Material.OCEAN_PLANT, "hoe" );
		materialHarvestTool.put( Material.CACTUS, "hoe" );
		materialHarvestTool.put( Material.CORAL, "hoe" );
		materialHarvestTool.put( Material.TALL_PLANTS, "hoe" );
		materialHarvestTool.put( Material.BAMBOO, "hoe" );
		materialHarvestTool.put( Material.BAMBOO_SAPLING, "hoe" );
		materialHarvestTool.put( Material.ORGANIC, "hoe" );

		materialHarvestTool.put( Material.WOOL, "shears" );			//SHEARS (Crafting)
		materialHarvestTool.put( Material.CARPET, "shears" );
		materialHarvestTool.put( Material.TNT, "shears" );
		materialHarvestTool.put( Material.DRAGON_EGG, "shears" );
		materialHarvestTool.put( Material.WEB, "shears" );
		materialHarvestTool.put( Material.CAKE, "shears" );
		materialHarvestTool.put( Material.SPONGE, "shears" );
	}

	public static String getSkill( BlockState state )
	{
		String skill = getSkillFromTool( getHarvestTool( state ) );
		if( skill.equals( Skill.INVALID_SKILL.toString() ) )
			return getSkill( state.getBlock() );
		else
			return skill;
	}

	public static String getSkill( Block block )
	{
		if( block.getTags().contains( getResLoc(  "forge:ores") ) )
			return Skill.MINING.toString();
		else if( block.getTags().contains( getResLoc( "forge:logs" ) ) )
			return Skill.WOODCUTTING.toString();
		else if( block.getTags().contains( getResLoc( "forge:plants" ) ) )
			return Skill.FARMING.toString();
		else
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

	public static ResourceLocation getBiomeResLoc( World world, Biome biome )
	{
		return world.func_241828_r().func_230521_a_( Registry.BIOME_KEY ).get().getKey( biome );
	}

	public static ResourceLocation getBiomeResLoc( World world, BlockPos pos )
	{
		return world.func_242406_i( pos ).get().getRegistryName();
	}

	public static ResourceLocation getDimResLoc( World world )
	{
		return world.getDimensionKey().getLocation();
//		ResourceLocation dimResLoc = world.getDimensionKey().getLocation();
//		if( dimResLoc.toString().equals( "minecraft:dimension" ) )
//			return world.func_241828_r().func_230520_a_().getKey( world.getDimensionType() );
//		else
//			return dimResLoc;
	}

	public static String getHarvestTool( BlockState state )
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

	public static void sendMessage( String msg, boolean bar, PlayerEntity player )
	{
		player.sendStatusMessage( new StringTextComponent( msg ), bar );
	}

	public static void sendMessage( String msg, boolean bar, PlayerEntity player, TextFormatting format )
	{
		player.sendStatusMessage( new StringTextComponent( msg ).setStyle( Style.EMPTY.applyFormatting( format ) ), bar );
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

	private static int doubleDoubleToInt( Double object )
	{
		return (int) Math.floor( object );
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
			highestReq = JsonConfig.data.get( JType.REQ_BREAK ).get( resLoc.toString() ).entrySet().stream().map(a -> doubleDoubleToInt( a.getValue() ) ).reduce( 0, Math::max );
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
				LOGGER.error( "WRONG getExtraChance CHANCE TYPE! PLEASE REPORT!" );
				return 0;
		}

		startLevel = offline ? XP.getOfflineLevel( skill, uuid ) : Skill.getLevel( skill, uuid );

		if( JsonConfig.data.get( jType ).containsKey( regKey ) && JsonConfig.data.get( jType ).get( regKey ).containsKey( "extraChance" ) )
			if( JsonConfig.data.get(jType).get(regKey).get("extraChance") != null )
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

	public static void dropItemStack( ItemStack itemStack, World world, Vector3d pos )
	{
		dropItemStack( itemStack, world, new BlockPos( pos ) );
	}

	public static void dropItemStack( ItemStack itemStack, World world, BlockPos pos )
	{
		Block.spawnAsEntity( world, pos, itemStack );
	}

	public static boolean isPlayerSurvival( PlayerEntity player )
	{
		if( player.isCreative() || player.isSpectator() )
			return false;
		else
			return true;
	}

	public static Collection<PlayerEntity> getNearbyPlayers( Entity mob )
	{
		World world = mob.getEntityWorld();
		List<? extends PlayerEntity> allPlayers = world.getPlayers();
		Collection<PlayerEntity> nearbyPlayers = new ArrayList<>();

		Float closestDistance = null;
		float tempDistance;

		for( PlayerEntity player : allPlayers )
		{
			tempDistance = mob.getDistance( player );
			if( closestDistance == null || tempDistance < closestDistance )
				closestDistance = tempDistance;
		}

		if( closestDistance != null )
		{
			float searchRange = closestDistance + 30;

			for( PlayerEntity player : allPlayers )
			{
				if( mob.getDistance( player ) < searchRange )
					nearbyPlayers.add( player );
			}
		}

		return nearbyPlayers;
	}

	public static float getPowerLevel( UUID uuid )
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

	public static ServerPlayerEntity getPlayerByUUID( UUID uuid )
	{
		return getPlayerByUUID( uuid, PmmoSavedData.getServer() );
	}

	public static ServerPlayerEntity getPlayerByUUID( UUID uuid, MinecraftServer server )
	{
		ServerPlayerEntity matchedPlayer = null;

		for( ServerPlayerEntity player : server.getPlayerList().getPlayers() )
		{
			if( player.getUniqueID().equals( uuid ) )
			{
				matchedPlayer = player;
				break;
			}
		}

		return matchedPlayer;
	}

	public static double getDistance( Vector3d a, Vector3d b )
	{
		return Math.sqrt( Math.pow( a.getX() - b.getX(), 2 ) + Math.pow( a.getY() - b.getY(), 2 ) + Math.pow( a.getZ() - b.getZ(), 2 ) );
	}

	public static <T extends Entity> Set<T> getEntitiesInRange( Vector3d origin, Set<T> entities, double range )
	{
		Set<T> withinRange = new HashSet<>();
		Vector3d pos;
		for( T entity : entities )
		{
			pos = entity.getPositionVec();
			double distance = getDistance( origin, pos );
			if( distance <= range )
				withinRange.add( entity );
		}

		return withinRange;
	}

	public static void syncPlayerDataAndConfig( PlayerEntity player )
	{
		syncPlayerData3( player );
		syncPlayerData4( player );
		syncPlayerXpBoost( player );
		syncPlayersSkills( player );
		NetworkHandler.sendToPlayer( new MessageUpdateBoolean( true, 1 ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( Config.localConfig ), 2 ), (ServerPlayerEntity) player );
	}

	public static void syncPlayersSkills( PlayerEntity player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.xpMapsToNbt( PmmoSavedData.get().getAllXpMaps() ), 3 ), (ServerPlayerEntity) player );
	}

	public static void syncPlayerXpBoost( PlayerEntity player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringMapStringToNbt( Config.getXpBoostsMap( player ) ), 6 ), (ServerPlayerEntity) player );
	}

	public static void syncPlayerData3( PlayerEntity player )
	{
		CompoundNBT fullData = NBTHelper.data3ToNbt( JsonConfig.localData );
		CompoundNBT dataChunk = new CompoundNBT();
		dataChunk.putBoolean( "wipe", true );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (ServerPlayerEntity) player );
		dataChunk = new CompoundNBT();

		int i = 0;
		for( String key3 : fullData.keySet() )
		{
			if( !dataChunk.contains( key3 ) )
				dataChunk.put( key3, new CompoundNBT() );

			for( String key2 : fullData.getCompound( key3 ).keySet() )
			{
				if( i >= 1000 )	//if any single JType reaches 1000 Elements, send chunk reset count
				{
					i = 0;
					NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (ServerPlayerEntity) player );
					dataChunk = new CompoundNBT();
					dataChunk.put( key3, new CompoundNBT() );
				}

				dataChunk.getCompound( key3 ).put( key2, fullData.getCompound( key3 ).getCompound( key2 ) );
				i++;
			}
		}

		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 4 ), (ServerPlayerEntity) player );
	}

	public static void syncPlayerData4( PlayerEntity player )
	{
		CompoundNBT fullData = NBTHelper.data4ToNbt( JsonConfig.localData2 );
		CompoundNBT dataChunk = new CompoundNBT();
		dataChunk.putBoolean( "wipe", true );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (ServerPlayerEntity) player );
		dataChunk = new CompoundNBT();

		int i = 0;
		for( String key4 : fullData.keySet() )
		{
			if( !dataChunk.contains( key4 ) )
				dataChunk.put( key4, new CompoundNBT() );

			for( String key3 : fullData.getCompound( key4 ).keySet() )
			{
				if( !dataChunk.getCompound( key4 ).contains( key3 ) )
					dataChunk.getCompound( key4 ).put( key3, new CompoundNBT() );

				for( String key2 : fullData.getCompound( key4 ).getCompound( key3 ).keySet() )
				{
					if( i >= 1000 )	//if any single JType reaches 1000 Elements, send chunk reset count
					{
						i = 0;
						NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (ServerPlayerEntity) player );
						dataChunk = new CompoundNBT();
						dataChunk.put( key4, new CompoundNBT() );
						dataChunk.getCompound( key4 ).put( key3, new CompoundNBT() );
					}

					dataChunk.getCompound( key4 ).getCompound( key3 ).put( key2, fullData.getCompound( key4 ).getCompound( key3 ).getCompound( key2 ) );
					i++;
				}
			}
		}

		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( dataChunk, 5 ), (ServerPlayerEntity) player );
	}

	public static void syncPlayer( PlayerEntity player )
    {
//    	UUID uuid = player.getUniqueID();
//        CompoundNBT xpTag 		 = NBTHelper.mapStringToNbt ( Config.getXpMap( player ) );
        CompoundNBT prefsTag 	 = NBTHelper.mapStringToNbt( Config.getPreferencesMap( player ) );
		CompoundNBT abilitiesTag = NBTHelper.mapStringToNbt( Config.getAbilitiesMap( player ) );

		syncPlayerDataAndConfig( player );
		updateRecipes( (ServerPlayerEntity) player );

        NetworkHandler.sendToPlayer( new MessageXp( 0f, "42069", 0f, true ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( prefsTag, 0 ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( abilitiesTag, 1 ), (ServerPlayerEntity) player );
		AttributeHandler.updateAll( player );

        for( Map.Entry<String, Double> entry : Config.getXpMap( player ).entrySet() )
        {
            NetworkHandler.sendToPlayer( new MessageXp( entry.getValue(), entry.getKey(), 0, true ), (ServerPlayerEntity) player );
        }
    }

	public static ResourceLocation getResLoc( String regKey )
	{
		try
		{
			return new ResourceLocation( regKey );
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

	public static boolean checkReq( PlayerEntity player, String res, JType jType )
	{
		return checkReq( player, XP.getResLoc( res ), jType );
	}

	public static boolean checkReq( PlayerEntity player, ResourceLocation res, JType jType )
	{
		if( res == null )
			return true;

		if( res.equals( Items.AIR.getRegistryName() ) || player.isCreative() )
			return true;

		return checkReq( player, getJsonMap( res.toString(), jType ) );
	}

	public static boolean checkReq( PlayerEntity player, Map<String, Double> reqMap )
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

	public static int getHighestReq( String regKey, JType jType )
	{
		int highestReq = 1;

		Map<String, Double> map = XP.getJsonMap( regKey, jType );

		if( map != null )
		{
			for( Map.Entry<String, Double> entry : map.entrySet() )
			{
				if( highestReq < entry.getValue() )
					highestReq = (int) (double) entry.getValue();
			}
		}

		return highestReq;
	}

	public static Set<String> getElementsFromTag( String tag )
	{
		Set<String> results = new HashSet<>();

		for( ITag.INamedTag<Item> namedTag : ItemTags.getAllTags() )
		{
			if( namedTag.getName().toString().equals( tag ) )
			{
				for( Item element : namedTag.getAllElements() )
				{
					try
					{
						results.add( element.getRegistryName().toString() );
					} catch( Exception e ){ /* Failed, don't care */ };
				}
			}
		}

		for( ITag.INamedTag<Block> namedTag : BlockTags.getAllTags() )
		{
			if( namedTag.getName().toString().equals( tag ) )
			{
				for( Block element : namedTag.getAllElements() )
				{
					try
					{
						results.add( element.getRegistryName().toString() );
					} catch( Exception e ){ /* Failed, don't care */ };
				}
			}
		}

		for( ITag.INamedTag<Fluid> namedTag : FluidTags.getAllTags() )
		{
			if( namedTag.getName().toString().equals( tag ) )
			{
				for( Fluid element : namedTag.getAllElements() )
				{
					try
					{
						results.add( element.getRegistryName().toString() );
					} catch( Exception e ){ /* Failed, don't care */ };
				}
			}
		}

		for( ITag.INamedTag<EntityType<?>> namedTag : EntityTypeTags.getAllTags() )
		{
			if( namedTag.getName().toString().equals( tag ) )
			{
				for( EntityType<?> element : namedTag.getAllElements() )
				{
					try
					{
						results.add( element.getRegistryName().toString() );
					} catch( Exception e ){ /* Failed, don't care */ };
				}
			}
		}

		return results;
	}

	public static Block getBlock( String regKey )
	{
		ResourceLocation resLoc = getResLoc( regKey );
		Block block = ForgeRegistries.BLOCKS.getValue( resLoc );
		return block == null ? Blocks.AIR : block;
	}

	public static Item getItem( String regKey )
	{
		ResourceLocation resLoc = getResLoc( regKey );
		Item item = ForgeRegistries.ITEMS.getValue( resLoc );
		if( item != null && !item.equals( Items.AIR ) )
			return item;
		else
		{
			Block block = ForgeRegistries.BLOCKS.getValue( resLoc );
			if( block != null )
				return block.asItem();
		}
		return Items.AIR;
	}

	public static Item getItem( ResourceLocation resLoc )
	{
		return getItem( resLoc.toString() );
	}

	public static boolean scanBlock( Block block, int radius, PlayerEntity player )
	{
		Block currBlock;
		BlockPos playerPos = vecToBlock( player.getPositionVec() );
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

//	public static CompoundNBT getPmmoTag( PlayerEntity player )
//	{
//		if( player != null )
//		{
//			CompoundNBT persistTag = player.getPersistentData();
//			CompoundNBT pmmoTag = null;
//
//			if( !persistTag.contains( Reference.MOD_ID ) )			//if Player doesn't have pmmo tag, make it
//			{
//				pmmoTag = new CompoundNBT();
//				persistTag.put( Reference.MOD_ID, pmmoTag );
//			}
//			else
//			{
//				pmmoTag = persistTag.getCompound( Reference.MOD_ID );	//if Player has pmmo tag, use it
//			}
//
//			return pmmoTag;
//		}
//		else
//			return new CompoundNBT();
//	}
//
//	public static CompoundNBT getPmmoTagElement( PlayerEntity player, String element )
//	{
//		if( player != null )
//		{
//			CompoundNBT pmmoTag = getPmmoTag( player );
//			CompoundNBT elementTag = null;
//
//			if( !pmmoTag.contains( element ) )					//if Player doesn't have element tag, make it
//			{
//				elementTag = new CompoundNBT();
//				pmmoTag.put( element, elementTag );
//			}
//			else
//			{
//				elementTag = pmmoTag.getCompound( element );	//if Player has element tag, use it
//			}
//
//			return elementTag;
//		}
//		else
//			return new CompoundNBT();
//	}
//
//	public static CompoundNBT getxpMap( PlayerEntity player )
//	{
//		return getPmmoTagElement( player, "skills" );
//	}
//
//	public static CompoundNBT getPreferencesTag( PlayerEntity player )
//	{
//		return getPmmoTagElement( player, "preferences" );
//	}
//
//	public static CompoundNBT getabilitiesMap( PlayerEntity player )
//	{
//		return getPmmoTagElement( player, "abilities" );
//	}

	public static double getMaxLevel()
	{
		double serverMaxLevel = Config.getConfig( "maxLevel" );
		return serverMaxLevel <= 1 ? Config.forgeConfig.maxLevel.get() : serverMaxLevel;
	}

	public static double getXpBoostDurabilityMultiplier( ItemStack itemStack )
	{
		double scale = 1;
		if( itemStack.isDamageable() )
		{
			double durabilityPercentage = 1 - itemStack.getDamage() / (double) itemStack.getMaxDamage();
			double scaleStart = Config.getConfig( "scaleXpBoostByDurabilityStart" ) / 100D;
			double scaleEnd = Math.max( scaleStart, Config.getConfig( "scaleXpBoostByDurabilityEnd" ) / 100D );
			scale = Util.mapCapped( durabilityPercentage, scaleStart, scaleEnd, 0, 1 );
		}
		return scale;
	}

	public static Map<String, Double> getStackXpBoosts( ItemStack itemStack, boolean type /*false = worn, true = held*/ )
	{
		Item item = itemStack.getItem();
		JType jType = type ? JType.XP_BONUS_HELD : JType.XP_BONUS_WORN;
		String regName = item.getRegistryName().toString();
		Map<String, Double> itemXpMap = new HashMap<>( JsonConfig.data.get( jType ).getOrDefault( regName, new HashMap<>() ) );
		if( Config.getConfig( "scaleXpBoostByDurability" ) != 0 )
			multiplyMapAnyDouble( itemXpMap, getXpBoostDurabilityMultiplier( itemStack ) );
		return itemXpMap;
	}

	public static double getStackXpBoost(PlayerEntity player, ItemStack itemStack, String skill, boolean type /*false = worn, true = held*/ )
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
				if( Config.getConfig( "scaleXpBoostByDurability" ) != 0 && itemStack.isDamageable() )
					boost *= getXpBoostDurabilityMultiplier( itemStack );
			}
		}

		return boost;
	}

	public static double getGlobalMultiplier( String skill )
	{
		return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill, 1D );
	}

	public static double getDimensionMultiplier( String skill, PlayerEntity player )
	{
		try
		{
			String dimensionKey = XP.getDimResLoc( player.world ).toString();
			return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( dimensionKey, new HashMap<>() ).getOrDefault( skill, 1D );
		}
		catch( Exception e )
		{
			return 1D;
		}
	}

	public static double getDifficultyMultiplier( PlayerEntity player, String skill )
	{
		double difficultyMultiplier = 1;

		if( skill.equals( Skill.COMBAT.toString() ) || skill.equals( Skill.ARCHERY.toString() ) || skill.equals( Skill.ENDURANCE.toString() ) )
		{
			switch( player.world.getDifficulty() )
			{
				case PEACEFUL:
					difficultyMultiplier = Config.forgeConfig.peacefulMultiplier.get();
					break;

				case EASY:
					difficultyMultiplier = Config.forgeConfig.easyMultiplier.get();
					break;

				case NORMAL:
					difficultyMultiplier = Config.forgeConfig.normalMultiplier.get();
					break;

				case HARD:
					difficultyMultiplier = Config.forgeConfig.hardMultiplier.get();
					break;

				default:
					break;
			}
		}

		return difficultyMultiplier;
	}

	public static double getItemBoost( PlayerEntity player, String skill )
	{
		if( player.getHeldItemMainhand().getItem().getRegistryName() == null )
			return 0;

		double itemBoost = 0;

		skill = skill.toLowerCase();
		PlayerInventory inv = player.inventory;

		if( Curios.isLoaded() )
		{
			Collection<ICurioStacksHandler> curiosItems = Curios.getCurios(player).collect( Collectors.toSet() );

			for( ICurioStacksHandler value : curiosItems )
			{
				for (int i = 0; i < value.getSlots(); i++)
				{
					itemBoost += getStackXpBoost( player, value.getStacks().getStackInSlot(i), skill, false );
				}
			};
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
		if( !inv.getStackInSlot( 40 ).isEmpty() )	//Off-Hand
			itemBoost += getStackXpBoost( player, inv.getStackInSlot( 40 ), skill, false );

		return itemBoost;
	}

	public static Map<String, Double> getDimensionBoosts( String dimKey )
	{
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( dimKey, new HashMap<>() );
	}

	public static double getDimensionBoost( PlayerEntity player, String skill )
	{
		try
		{
			String dimensionKey = XP.getDimResLoc( player.world ).toString();
			return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( dimensionKey, new HashMap<>() ).getOrDefault( skill, 0D );
		}
		catch( NullPointerException e )
		{
			return 0;
		}
	}

	public static double getGlobalBoost( String skill )
	{
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill, 0D );
	}

	public static CompoundNBT writeUniqueId( UUID uuid )
	{
		CompoundNBT compoundnbt = new CompoundNBT();
		compoundnbt.putLong("M", uuid.getMostSignificantBits());
		compoundnbt.putLong("L", uuid.getLeastSignificantBits());
		return compoundnbt;
	}

	public static Map<String, Double> getBiomeBoosts( PlayerEntity player )
	{
		Map<String, Double> biomeBoosts = new HashMap<>();
		double biomePenaltyMultiplier = Config.getConfig( "biomePenaltyMultiplier" );

		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVec() ) );
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

	public static double getMultiplier( PlayerEntity player, String skill )
	{
		double multiplier = Config.forgeConfig.globalMultiplier.get();

		double globalMultiplier = getGlobalMultiplier( skill );
		double dimensionMultiplier = getDimensionMultiplier( skill, player );
		double difficultyMultiplier = getDifficultyMultiplier( player, skill );

		double globalBoost = getGlobalBoost( skill );
		double itemBoost = getItemBoost( player, skill );
		double biomeBoost = getBiomeBoosts( player ).getOrDefault( skill, 0D );
		double dimensionBoost = getDimensionBoost( player, skill );
		double playerBoost = PmmoSavedData.get().getPlayerXpBoost( player.getUniqueID(), skill );

		double additiveMultiplier = 1 + (itemBoost + biomeBoost + dimensionBoost + globalBoost + playerBoost ) / 100;

		multiplier *= globalMultiplier;
		multiplier *= dimensionMultiplier;
		multiplier *= difficultyMultiplier;
		multiplier *= additiveMultiplier;
		multiplier *= CheeseTracker.getLazyMultiplier( player.getUniqueID(), skill );

		return Math.max( 0, multiplier );
	}

	public static double getHorizontalDistance( Vector3d p1, Vector3d p2 )
	{
		return Math.sqrt( Math.pow( ( p1.getX() - p2.getX() ), 2 ) + Math.pow( ( p1.getZ() - p2.getZ() ), 2 ) );
	}

	public static void awardXp( ServerPlayerEntity player, String skill, @Nullable String sourceName, double amount, boolean skip, boolean ignoreBonuses, boolean causedByParty )
	{
//		if( !(player instanceof ServerPlayerEntity) )
//		{
//			LOGGER.error( "NOT ServerPlayerEntity PLAYER XP AWARD ATTEMPTED! THIS SHOULD NOT HAPPEN! SOURCE: " + sourceName + ", SKILL: " + skill + ", AMOUNT: " + amount + ", CLASS: " + player.getClass().getName().toString() );
//			return;
//		}

		if( player.world.isRemote || Double.isNaN( amount ) || player instanceof FakePlayer )
			return;

		PmmoSavedData pmmoSavedData = PmmoSavedData.get();
		UUID uuid = player.getUniqueID();

		if( !causedByParty )
		{
			Party party = pmmoSavedData.getParty( uuid );
			if( party != null )
			{
				Set<ServerPlayerEntity> membersInRange = party.getOnlineMembersInRange( player );
				int membersInRangeSize = membersInRange.size();
				double partyMultiplier = party.getMultiplier( membersInRangeSize );
				amount *= partyMultiplier;
				party.submitXpGained( uuid, amount );
				amount /= membersInRangeSize + 1D;
				for( ServerPlayerEntity partyMember : membersInRange )
				{
					awardXp( partyMember, skill, sourceName, amount, true, ignoreBonuses, true );
				}
			}
		}

		if( !ignoreBonuses )
			amount *= getMultiplier( player, skill );

		String playerName = player.getDisplayName().getString();
		int startLevel = Skill.getLevel( skill, uuid );
		double startXp = Skill.getXp( skill, uuid );
		double maxXp = Config.getConfig( "maxXp" );

		if( amount == 0 || startXp >= 2000000000 )
			return;

		if( startXp + amount >= 2000000000 )
		{
			sendMessage( skill + " cap of 2b xp reached, you fucking psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LOGGER.info( player.getDisplayName().getString() + " " + skill + " 2b cap reached" );
			amount = 2000000000 - startXp;
		}

		pmmoSavedData.addXp( skill, uuid, amount );

		int currLevel = Skill.getLevel( skill, uuid );

		if( startLevel != currLevel ) //Level Up! Or Down?
		{
			AttributeHandler.updateAll( player );
			updateRecipes( player );

			if( ModList.get().isLoaded( "compatskills" ) )
			{
				String commandArgs = "reskillable incrementskill " + playerName + " compatskills." + skill + " 1";

				try
				{
					if( !player.world.isRemote )
						player.getServer().getCommandManager().getDispatcher().execute( commandArgs, player.getCommandSource().withFeedbackDisabled() );
				}
				catch( CommandSyntaxException e )
				{
					LOGGER.error( "PMMO Level Up - compatskills command went wrong! args: " + commandArgs, e );
				}
			}

			if( JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skill.toLowerCase() ) != null )
			{
				Map<String, Double> commandMap = JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skill.toLowerCase() );

				for( Map.Entry<String, Double> entry : commandMap.entrySet() )
				{
					int commandLevel = (int) Math.floor( entry.getValue() );
					if( startLevel < commandLevel && currLevel >= commandLevel )
					{
						String command = entry.getKey().replace( ">player<", playerName ).replace( ">level<", "" + commandLevel );
						try
						{
							player.getServer().getCommandManager().getDispatcher().execute( command, player.getServer().getCommandSource() );
							LOGGER.info( "Executing command \"" + command + "\"\nTrigger: " + playerName + " level up from " + startLevel + " to " + currLevel + " in " + skill + ", trigger level " + commandLevel );
						}
						catch( CommandSyntaxException e )
						{
							LOGGER.error( "Invalid level up command \"" + command + "\"", e );
						}
					}
				}
			}
		}

		NetworkHandler.sendToPlayer( new MessageXp( startXp, skill, amount, skip ), player );
		if( !skip && Config.forgeConfig.logXpGainedInDebugLog.get() )
			LOGGER.debug( playerName + " +" + amount + "xp in: "  + skill + " for: " + sourceName + " total xp: " + Skill.getXp( skill, uuid ) );

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
			LOGGER.error( "TRIGGER XP AWARD \"" + triggerKey + "\" DOES NOT HAVE ANY VALUES, CANNOT AWARD" );
	}

	public static void awardXpMap(UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			Skill.addXp( entry.getKey(), uuid, entry.getValue(), sourceName, skip, ignoreBonuses );
		}
	}

	public static void updateRecipes( ServerPlayerEntity player )
	{
		if( Config.forgeConfig.craftReqEnabled.get() )
		{
			Collection<IRecipe<?>> allRecipes = player.getServer().getRecipeManager().getRecipes();
			Collection<IRecipe<?>> removeRecipes = new HashSet<>();
			Collection<IRecipe<?>> newRecipes = new HashSet<>();

			for( IRecipe<?> recipe : allRecipes )
			{
				if( XP.checkReq( player, recipe.getRecipeOutput().getItem().getRegistryName(), JType.REQ_CRAFT ) )
					newRecipes.add( recipe );
				else
					removeRecipes.add( recipe );
			}

			player.getRecipeBook().remove( removeRecipes, player );
			player.getRecipeBook().add( newRecipes, player );
		}
	}

	public static void scanUnlocks( int level, String skill )
	{

	}

	private static int getGap( int a, int b )
	{
		return a - b;
	}

	public static int getSkillReqGap( PlayerEntity player, ResourceLocation res, JType jType )
	{
		Map<String, Double> reqs = getJsonMap( res.toString(), jType );

		if( reqs == null )
			return 0;

		return getSkillReqGap( player, reqs );
	}

	public static int getSkillReqGap(PlayerEntity player, Map<String, Double> reqs )
	{
		int gap = 0;
		if( !checkReq( player, reqs ) )
		{
			if( reqs != null )
			{
				gap = (int) Math.floor( reqs.entrySet().stream()
						.map( entry -> (int) Math.floor( entry.getValue() ) -  Skill.getLevel( entry.getKey(), player ) )
						.reduce( 0, Math::max ) );
			}
		}
		return gap;
	}

	public static BlockPos vecToBlock( Vector3d pos )
	{
		return new BlockPos( pos );
	}

	public static Vector3d blockToVec( BlockPos pos )
	{
		return new Vector3d( pos.getX(), pos.getY(), pos.getZ() );
	}

	public static Vector3d blockToMiddleVec( BlockPos pos )
	{
		return new Vector3d( pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D );
	}

	public static void spawnRocket( World world, BlockPos pos, String skill )
	{
		spawnRocket( world, new Vector3d( pos.getX(), pos.getY(), pos.getZ() ), skill );
	}

	public static void spawnRocket( World world, Vector3d pos, String skill )
	{
		CompoundNBT nbt = new CompoundNBT();
		CompoundNBT fw = new CompoundNBT();
		ListNBT explosion = new ListNBT();
		CompoundNBT l = new CompoundNBT();

		int[] colors = new int[1];
		colors[0] = Skill.getSkillColor( skill );
//		int[] fadeColors = {0xff0000, 0x00ff00, 0x0000ff};

		l.putInt( "Flicker", 1 );
		l.putInt( "Trail", 0 );
		l.putInt( "Type", 1 );
		l.put( "Colors", new IntArrayNBT( colors ) );
//		l.put( "FadeColors", new IntArrayNBT( fadeColors ) );
		explosion.add(l);

		fw.put( "Explosions", explosion );
		fw.putInt( "Flight", 0 );
		nbt.put( "Fireworks", fw );

		ItemStack itemStack = new ItemStack( Items.FIREWORK_ROCKET );
		itemStack.setTag( nbt );

		FireworkRocketEntity fireworkRocketEntity = new PMMOFireworkEntity( world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack );
		world.addEntity( fireworkRocketEntity );
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

		if( Config.forgeConfig.enchantUseReqAutoScaleEnabled.get() && enchantLevel > highestSpecifiedLevel )
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

	public static void applyWornPenalty( PlayerEntity player, ItemStack itemStack )
	{
		if( Config.getConfig( "wearReqEnabled" ) == 0 )
			return;
		ResourceLocation resLoc = itemStack.getItem().getRegistryName();
		Map<String, Double> wearReq = XP.getJsonMap( resLoc, JType.REQ_WEAR );
		String wearReqSkill = Config.forgeConfig.autoGenerateWearReqAsCombat.get() ? Skill.COMBAT.toString() : Skill.ENDURANCE.toString();
		if( !wearReq.containsKey( wearReqSkill ) && Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateWearReqDynamicallyEnabled" ) != 0 )
			wearReq.put( wearReqSkill, AutoValues.getWearReqFromStack( itemStack ) );

		if( !checkReq( player, wearReq ) )
		{
			int gap = getSkillReqGap( player, resLoc, JType.REQ_WEAR );

			if( gap > 9 )
				gap = 9;

			player.addPotionEffect( new EffectInstance( Effects.MINING_FATIGUE, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.WEAKNESS, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.SLOWNESS, 75, gap, false, true ) );

			if( Config.forgeConfig.strictReqWear.get() || EnchantmentHelper.hasBindingCurse( itemStack ) )
			{
				ItemStack droppedItemStack = itemStack.copy();
				player.dropItem( droppedItemStack, false, false );
				itemStack.setCount( 0 );
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.gotTooHotDroppedItem", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.gotTooHotDroppedItem", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
			}
			else
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToWear", new TranslationTextComponent( itemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
		}

		applyEnchantmentUsePenalty( player, itemStack );
	}

	public static void applyEnchantmentUsePenalty( PlayerEntity player, ItemStack itemStack )
	{
		if( Config.getConfig( "enchantUseReqEnabled" ) == 0 )
			return;
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

				player.addPotionEffect( new EffectInstance( Effects.MINING_FATIGUE, 75, gap, false, true ) );
				player.addPotionEffect( new EffectInstance( Effects.WEAKNESS, 75, gap, false, true ) );
				player.addPotionEffect( new EffectInstance( Effects.SLOWNESS, 75, gap, false, true ) );

				if( Config.forgeConfig.strictReqUseEnchantment.get() || EnchantmentHelper.hasBindingCurse( itemStack ) )
				{
					ItemStack droppedItemStack = itemStack.copy();
					player.dropItem( droppedItemStack, false, false );
					itemStack.setCount( 0 );
					player.sendStatusMessage( new TranslationTextComponent( "pmmo.gotTooHotDroppedItem", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
					player.sendStatusMessage( new TranslationTextComponent( "pmmo.gotTooHotDroppedItem", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
				}
				else
					player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUseEnchantment", new TranslationTextComponent( entry.getKey().getName() ), new TranslationTextComponent( itemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
			}
		}
	}

	public static void checkBiomeLevelReq( PlayerEntity player )
	{
		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVec() ) );
		ResourceLocation resLoc = XP.getBiomeResLoc( player.world, biome );
		if( resLoc == null )
			return;
		String biomeKey = resLoc.toString();
		UUID playerUUID = player.getUniqueID();
		if( !JsonConfig.data.containsKey( JType.REQ_BIOME ) )
			return;
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
						Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );

						if( effect != null )
							player.addPotionEffect( new EffectInstance( effect, 75, (int) Math.floor( entry.getValue() ), false, false ) );
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
					Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );

					if( effect != null )
						player.addPotionEffect( new EffectInstance( effect, 75, (int) Math.floor( entry.getValue() ), false, true ) );
				}
				if( player.world.isRemote() )
				{
					if( !lastBiome.get( playerUUID ).equals( biomeKey ) )
					{
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToSurvive", new TranslationTextComponent( getBiomeResLoc( player.world, biome ).toString() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToSurvive", new TranslationTextComponent( getBiomeResLoc( player.world, biome ).toString() ) ).setStyle( textStyle.get( "red" ) ), false );
						for( Map.Entry<String, Double> entry : biomeReq.entrySet() )
						{
							int startLevel = Skill.getLevel( entry.getKey(), player );

							if( startLevel < entry.getValue() )
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + entry.getKey() ).getString(), "" + (int) Math.floor( entry.getValue() ) ).setStyle( textStyle.get( "red" ) ), false );
							else
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + entry.getKey() ).getString(), "" + (int) Math.floor( entry.getValue() ) ).setStyle( textStyle.get( "green" ) ), false );
						}
					}
				}
			}
		}

		lastBiome.put( playerUUID, biomeKey );
	}

	public static double getWeight( int startLevel, Map<String, Double> fishItem )
	{
		return Util.mapCapped( startLevel, fishItem.get( "startLevel" ), fishItem.get( "endLevel" ), fishItem.get( "startWeight" ), fishItem.get( "endWeight" ) );
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

	public static <T> int getTotalXpFromMap( Map<T, Double> input )
	{
		int sum = 0;

		for( double xp : input.values() )
		{
			sum += xp;
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

	public static int levelAtXp( float xp )
	{
		return levelAtXp( (double) xp );
	}
	public static int levelAtXp( double xp )
	{
		boolean useExponentialFormula = Config.getConfig("useExponentialFormula") != 0;
		double baseXp = Config.getConfig( "baseXp" );
		double exponentialBaseXp = Config.getConfig( "exponentialBaseXp" );
		double exponentialBase = Config.getConfig( "exponentialBase" );
		double exponentialRate = Config.getConfig( "exponentialRate" );
		int maxLevel = (int) Math.floor( XP.getMaxLevel() );
		double xpIncreasePerLevel = Config.getConfig( "xpIncreasePerLevel" );

		int theXp = 0;

		for( int level = 0; ; level++ )
		{
			if( xp < theXp || level >= maxLevel )
				return level;

			if( useExponentialFormula )
				theXp += exponentialBaseXp * Math.pow( exponentialBase, exponentialRate * ( level ) );
			else
				theXp += baseXp + level * xpIncreasePerLevel;
		}
	}

	public static float levelAtXpDecimal( float xp )
	{
		return (float) levelAtXpDecimal( (double) xp );
	}
	public static double levelAtXpDecimal( double xp )
	{
		int maxLevel = (int) Math.floor( XP.getMaxLevel() );

		if( levelAtXp( xp ) >= maxLevel )
			return maxLevel;
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
	public static double xpAtLevel( float givenLevel )
	{
		return xpAtLevel( (double) givenLevel );
	}
	public static double xpAtLevel( double givenLevel )
	{
		boolean useExponentialFormula = Config.getConfig("useExponentialFormula") != 0;

		double baseXp = Config.getConfig( "baseXp" );
		double exponentialBaseXp = Config.getConfig( "exponentialBaseXp" );
		double exponentialBase = Config.getConfig( "exponentialBase" );
		double exponentialRate = Config.getConfig( "exponentialRate" );

		int maxLevel = (int) Math.floor( XP.getMaxLevel() );
		if( givenLevel > maxLevel )
			givenLevel = maxLevel;
		double theXp = 0;

		double xpIncreasePerLevel = Config.getConfig( "xpIncreasePerLevel" );

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

	public static boolean isHoldingDebugItemInOffhand( PlayerEntity player )
	{
		return isItemDebugItem( player.getHeldItemOffhand().getItem() );
	}

	public static boolean isItemDebugItem( Item item )
	{
		return Items.MUSIC_DISC_CAT.equals( item );
	}

	public static boolean isNightvisionUnlocked( PlayerEntity player )
	{
		return Skill.getLevel( Skill.SWIMMING.toString(), player ) >= Config.getConfig( "nightvisionUnlockLevel" );
	}

	public static void addWorldXpDrop( WorldXpDrop xpDrop, ServerPlayerEntity player )
	{
//        System.out.println( "xp drop added at " + xpDrop.getPos() );
		xpDrop.startXp = xpDrop.startXp * (float) XP.getMultiplier( player, xpDrop.getSkill() );
		if( Config.getPreferencesMap( player ).getOrDefault( "worldXpDropsEnabled", 1D ) != 0 )
			NetworkHandler.sendToPlayer( new MessageWorldXp( xpDrop ), player );
		UUID uuid = player.getUniqueID();
		for( ServerPlayerEntity otherPlayer : PmmoSavedData.getServer().getPlayerList().getPlayers() )
		{
			double distance = Util.getDistance( xpDrop.getPos(), otherPlayer.getPositionVec() );
			if ( distance < 64D && !uuid.equals( otherPlayer.getUniqueID() ) && Config.getPreferencesMap( otherPlayer ).getOrDefault( "showOthersWorldXpDrops", 0D ) != 0 )
				NetworkHandler.sendToPlayer( new MessageWorldXp( xpDrop ), otherPlayer );
		}
	}

	public static void addWorldXpDrop( WorldXpDrop xpDrop, UUID uuid )
	{
		ServerPlayerEntity player = PmmoSavedData.getServer().getPlayerList().getPlayerByUUID( uuid );
		if( player != null )
			addWorldXpDrop( xpDrop, player );
	}

	public static void addWorldXpDropOffline( WorldXpDrop xpDrop )
	{
		WorldRenderHandler.addWorldXpDropOffline( xpDrop );
	}

	public static void addWorldText( WorldText worldText, UUID uuid )
	{
		ServerPlayerEntity player = PmmoSavedData.getServer().getPlayerList().getPlayerByUUID( uuid );
		if( player != null )
			addWorldText( worldText, player );
	}

	public static void addWorldTextRadius( WorldText worldText, double radius )
	{
		worldText.updatePos();
		for( ServerPlayerEntity otherPlayer : PmmoSavedData.getServer().getPlayerList().getPlayers() )
		{
			double distance = Util.getDistance( worldText.getPos(), otherPlayer.getPositionVec() );
			if ( distance < radius )
				NetworkHandler.sendToPlayer( new MessageWorldText( worldText ), otherPlayer );
		}
	}

	public static void addWorldText( WorldText worldText, ServerPlayerEntity player )
	{
		worldText.updatePos();
		NetworkHandler.sendToPlayer( new MessageWorldText( worldText ), player );
	}

	public static void addWorldTextOffline( WorldText worldText )
	{
		WorldRenderHandler.addWorldTextOffline( worldText );
	}
}