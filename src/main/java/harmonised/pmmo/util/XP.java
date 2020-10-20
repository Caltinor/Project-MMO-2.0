package harmonised.pmmo.util;

import java.util.*;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.network.*;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.PMMOFireworkEntity;
import harmonised.pmmo.skills.Skill;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class XP
{
	private static Map<Material, String> materialHarvestTool = new HashMap<>();
	private static Map<Skill, Integer> skillColors = new HashMap<>();
	public static Set<UUID> isVeining = new HashSet<>();
	public static Map<Skill, Style> skillStyle = new HashMap<>();
	public static Map<String, Style> textStyle = new HashMap<>();
	public static Map<UUID, String> playerNames = new HashMap<>();
	public static Map<UUID, Map<Skill, Double>> offlineXp = new HashMap<>();
	private static Map<UUID, String> lastBiome = new HashMap<>();
	private static int debugInt = 0;
	private static boolean alwaysDropWornItems = Config.forgeConfig.alwaysDropWornItems.get();

	public static void initValues()
	{
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
		skillColors.put( Skill.SLAYER, 0xffffff );
		skillColors.put( Skill.HUNTER, 0xcf7815 );
		skillColors.put( Skill.FLETCHING, 0xff9700 );
		skillColors.put( Skill.TAMING, 0xffffff );
		skillColors.put( Skill.ENGINEERING, 0xffffff );
		skillColors.put( Skill.COOKING, 0xe69900 );
		skillColors.put( Skill.ALCHEMY, 0xe69900 );

		skillStyle.put( Skill.MINING, new Style().setColor( TextFormatting.AQUA ) );
		skillStyle.put( Skill.BUILDING, new Style().setColor( TextFormatting.AQUA ) );
		skillStyle.put( Skill.EXCAVATION, new Style().setColor( TextFormatting.GOLD ) );
		skillStyle.put( Skill.WOODCUTTING, new Style().setColor( TextFormatting.GOLD ) );
		skillStyle.put( Skill.FARMING, new Style().setColor( TextFormatting.GREEN ) );
		skillStyle.put( Skill.AGILITY, new Style().setColor( TextFormatting.GREEN ) );
		skillStyle.put( Skill.ENDURANCE, new Style().setColor( TextFormatting.DARK_RED ) );
		skillStyle.put( Skill.COMBAT, new Style().setColor( TextFormatting.RED ) );
		skillStyle.put( Skill.ARCHERY, new Style().setColor( TextFormatting.YELLOW ) );
		skillStyle.put( Skill.SMITHING, new Style().setColor( TextFormatting.GRAY ) );
		skillStyle.put( Skill.FLYING, new Style().setColor( TextFormatting.GRAY ) );
		skillStyle.put( Skill.SWIMMING, new Style().setColor( TextFormatting.AQUA ) );
		skillStyle.put( Skill.FISHING, new Style().setColor( TextFormatting.AQUA ) );
		skillStyle.put( Skill.CRAFTING, new Style().setColor( TextFormatting.GOLD ) );
		skillStyle.put( Skill.MAGIC, new Style().setColor( TextFormatting.BLUE ) );
		skillStyle.put( Skill.SLAYER, new Style().setColor( TextFormatting.GRAY ) );
		skillStyle.put( Skill.HUNTER, new Style().setColor( TextFormatting.GOLD ) );
		skillStyle.put( Skill.FLETCHING, new Style().setColor( TextFormatting.DARK_GREEN ) );
		skillStyle.put( Skill.TAMING, new Style().setColor( TextFormatting.WHITE ) );
		skillStyle.put( Skill.ENGINEERING, new Style().setColor( TextFormatting.WHITE ) );
		skillStyle.put( Skill.COOKING, new Style().setColor( TextFormatting.GOLD ) );
		skillStyle.put( Skill.ALCHEMY, new Style().setColor( TextFormatting.GOLD ) );

//		skillStyle.put(Skill.MINING, TextFormatting.AQUA );
//		skillStyle.put( Skill.BUILDING, TextFormatting.AQUA );
//		skillStyle.put( Skill.EXCAVATION, TextFormatting.GOLD );
//		skillStyle.put( Skill.WOODCUTTING, TextFormatting.GOLD );
//		skillStyle.put( Skill.FARMING, TextFormatting.GREEN );
//		skillStyle.put( Skill.AGILITY, TextFormatting.GREEN );
//		skillStyle.put( Skill.ENDURANCE, TextFormatting.DARK_RED );
//		skillStyle.put( Skill.COMBAT, TextFormatting.RED);
//		skillStyle.put( Skill.ARCHERY, TextFormatting.YELLOW );
//		skillStyle.put( Skill.SMITHING, TextFormatting.GRAY );
//		skillStyle.put( Skill.FLYING, TextFormatting.GRAY );
//		skillStyle.put( Skill.SWIMMING, TextFormatting.AQUA );
//		skillStyle.put( Skill.FISHING, TextFormatting.AQUA );
//		skillStyle.put( Skill.CRAFTING, TextFormatting.GOLD );
//		skillStyle.put( Skill.MAGIC, TextFormatting.BLUE );
//		skillStyle.put( Skill.SLAYER, TextFormatting.DARK_GRAY );
//		skillStyle.put( Skill.FLETCHING, TextFormatting.DARK_GREEN );
//		skillStyle.put( Skill.TAMING, TextFormatting.WHITE );
////////////////////////////////////Style//////////////////////////////////////////////
		textStyle.put( "red", new Style().setColor( TextFormatting.RED ) );
		textStyle.put( "green", new Style().setColor( TextFormatting.GREEN ) );
		textStyle.put( "yellow", new Style().setColor( TextFormatting.YELLOW ) );
		textStyle.put( "grey", new Style().setColor( TextFormatting.GRAY ) );
		textStyle.put( "blue", new Style().setColor( TextFormatting.BLUE ) );
////////////////////////////////////PATREONS//////////////////////////////////////////////
		PlayerConnectedHandler.lapisPatreons.add( UUID.fromString( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" ) );		//LUCIFER
		PlayerConnectedHandler.dandelionPatreons.add( UUID.fromString( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" ) );	//TYRIUS
		PlayerConnectedHandler.ironPatreons.add( UUID.fromString( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" ) );		//DIDIS
		PlayerConnectedHandler.ironPatreons.add( UUID.fromString( "0bc51f06-9906-41ea-9fb4-7e9be169c980" ) );		//STRESSINDICATOR
		PlayerConnectedHandler.ironPatreons.add( UUID.fromString( "5bfdb948-7b66-476a-aefe-d45e4778fb2d" ) );		//DADDY_P1G
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

	public static Style getSkillStyle( Skill skill )
	{
		return skillStyle.getOrDefault( skill, new Style() );
	}

	public static Skill getSkill( Material material )
	{
		return getSkillFromTool( XP.correctHarvestTool( material ) );
	}

	public static Skill getSkill( BlockState state )
	{
		return getSkill( state.getMaterial() );
	}

//	public static Skill getSkill( Block block )
//	{
//		if( block.getTags().contains( getResLoc(  "forge:ores") ) )
//			return Skill.MINING;
//		else if( block.getTags().contains( getResLoc( "forge:logs" ) ) )
//			return Skill.WOODCUTTING;
//		else if( block.getTags().contains( getResLoc( "forge:plants" ) ) )
//			return Skill.FARMING;
//		else
//			return Skill.INVALID_SKILL;
//	}

	public static Skill getSkillFromTool( String tool )
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

			case "shears":
				return Skill.CRAFTING;

			default:
				return Skill.INVALID_SKILL;
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
				if( entry.getValue() instanceof Double )
					theMap.put( entry.getKey(), (double) entry.getValue() );
			}
		}

		return theMap;
	}

	public static Integer getSkillColor( Skill skill )
	{
		return skillColors.getOrDefault( skill, 0xffffff );
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
		Skill skill;

		switch( jType )
		{
			case INFO_ORE:
				skill = Skill.MINING;
				break;

			case INFO_LOG:
				skill = Skill.WOODCUTTING;
				break;

			case INFO_PLANT:
				skill = Skill.FARMING;
				break;

			case INFO_SMELT:
				skill = Skill.SMITHING;
				break;

			case INFO_COOK:
				skill = Skill.COOKING;
				break;

			case INFO_BREW:
				skill = Skill.ALCHEMY;
				break;

			default:
				LogHandler.LOGGER.error( "WRONG getExtraChance CHANCE TYPE! PLEASE REPORT!" );
				return 0;
		}

		startLevel = offline ? XP.getOfflineLevel( skill, uuid ) : skill.getLevel( uuid );

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
		int enduranceLevel = Skill.ENDURANCE.getLevel( uuid );

		int combatLevel = Skill.COMBAT.getLevel( uuid );
		int archeryLevel = Skill.ARCHERY.getLevel( uuid );
		int magicLevel = Skill.MAGIC.getLevel( uuid );

		int maxOffensive = combatLevel;
		if( maxOffensive < archeryLevel )
			maxOffensive = archeryLevel;
		if( maxOffensive < magicLevel )
			maxOffensive = magicLevel;

		return ( enduranceLevel + (maxOffensive * 1.5f) ) / 50;
	}

	public static void syncPlayerConfig( PlayerEntity player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.data3ToNbt( JsonConfig.localData ), 4 ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.data4ToNbt( JsonConfig.localData2 ), 5 ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( Config.localConfig ), 2 ), (ServerPlayerEntity) player );
	}


	public static void syncPlayer( PlayerEntity player )
	{
//    	UUID uuid = player.getUniqueID();
//        CompoundNBT xpTag 		 = NBTHelper.mapSkillToNbt ( Config.getXpMap( player ) );
		CompoundNBT prefsTag = NBTHelper.mapStringToNbt( Config.getPreferencesMap( player ) );
		CompoundNBT abilitiesTag = NBTHelper.mapStringToNbt( Config.getAbilitiesMap( player ) );

		syncPlayerConfig( player );
		updateRecipes( (ServerPlayerEntity) player );

		NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0f, true ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( prefsTag, 0 ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( abilitiesTag, 1 ), (ServerPlayerEntity) player );
		AttributeHandler.updateAll( player );

		for( Map.Entry<Skill, Double> entry : Config.getXpMap( player ).entrySet() )
		{
			NetworkHandler.sendToPlayer( new MessageXp( entry.getValue(), entry.getKey().getValue(), 0, true ), (ServerPlayerEntity) player );
		}
	}

	public static boolean checkReq( PlayerEntity player, String res, JType jType )
	{
		return checkReq( player, XP.getResLoc( res ), jType );
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

	public static Map<String, Double> getReqMap( String registryName, JType type )
	{
		Map<String, Map<String, Double>> fullMap = JsonConfig.data.get( type );
		Map<String, Double> map = null;

		if( fullMap != null && fullMap.containsKey( registryName ) )
		{
			map = new HashMap<>();

			for( Map.Entry<String, Double> entry : fullMap.get( registryName ).entrySet() )
			{
				if( entry.getValue() instanceof Double )
					map.put( entry.getKey(), (double) entry.getValue() );
			}
		}

		return map;
	}

	public static boolean checkReq( PlayerEntity player, ResourceLocation res, JType jType )
	{
		if( res == null )
			return true;

		if( res.equals( Items.AIR.getRegistryName() ) || player.isCreative() )
			return true;

		if( JsonConfig.data.get( JType.PLAYER_SPECIFIC ).containsKey( player.getUniqueID().toString() ) )
		{
			if( JsonConfig.data.get( JType.PLAYER_SPECIFIC ).get( player.getUniqueID().toString() ).containsKey( "ignoreReq" ) )
				return true;
		}

		String registryName = res.toString();
		Map<String, Double> reqMap = getReqMap( registryName, jType );
		double startLevel;
		boolean failedReq = false;

//		if( reqMap == null )
//			failedReq = true;

		if( reqMap != null )
		{
			for( Map.Entry<String, Double> entry : reqMap.entrySet() )
			{
				startLevel = Skill.getSkill( entry.getKey() ).getLevel( player );

				if( startLevel < entry.getValue() )
					failedReq = true;
			}
		}

		return !failedReq;
	}

	public static int getHighestReq( String regKey, JType jType )
	{
		int highestReq = 1;

		Map<String, Double> map = XP.getReqMap( regKey, jType );

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
		return ForgeRegistries.ITEMS.getValue( resLoc );
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

	public static double getWornXpBoost( PlayerEntity player, Item item, String skillName )
	{
		double boost = 0;

		if( !item.equals( Items.AIR ) )
		{
			String regName = item.getRegistryName().toString();
			Map<String, Double> itemXpMap = JsonConfig.data.get( JType.XP_BONUS_WORN ).get( regName );

			if( itemXpMap != null && itemXpMap.containsKey( skillName ) )
			{
				if( checkReq( player, item.getRegistryName(), JType.REQ_WEAR ) )
				{
					boost = (double) itemXpMap.get( skillName );
				}
			}
		}

		return boost;
	}

	public static double getGlobalMultiplier( Skill skill )
	{
		return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill.toString(), 1D );
	}

	public static double getDimensionMultiplier(Skill skill, PlayerEntity player )
	{
		String dimensionKey = player.world.getDimension().getType().getRegistryName().toString();
		return JsonConfig.data.get( JType.XP_MULTIPLIER_DIMENSION ).getOrDefault( dimensionKey, new HashMap<>() ).getOrDefault( skill.toString(), 1D );
	}

	public static double getDifficultyMultiplier( PlayerEntity player, Skill skill )
	{
		double difficultyMultiplier = 1;

		if( skill == Skill.COMBAT || skill == Skill.ARCHERY || skill == Skill.ENDURANCE )
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

	public static double getItemBoost( PlayerEntity player, Skill skill )
	{
		if( player.getHeldItemMainhand().getItem().getRegistryName() == null )
			return 0;

		double itemBoost = 0;

		String skillName = skill.toString().toLowerCase();
		String regKey = player.getHeldItemMainhand().getItem().getRegistryName().toString();
		Map<String, Double> heldMap = JsonConfig.data.get( JType.XP_BONUS_HELD ).get( regKey );
		PlayerInventory inv = player.inventory;

		if( heldMap != null )
		{
			if( heldMap.containsKey( skillName ) )
				heldMap.get( skillName );
		}

//		if( Curios.isLoaded() )
//		{
//			Collection<ICurioStacksHandler> curiosItems = Curios.getCurios(player).collect(Collectors.toSet());
//
//			for( ICurioStacksHandler value : curiosItems )
//			{
//				for (int i = 0; i < value.getSlots(); i++)
//				{
//					itemBoost += getWornXpBoost( player, value.getStacks().getStackInSlot(i).getItem(), skillName );
//				}
//			};
//		}
		//COUT

		if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
			itemBoost += getWornXpBoost( player, player.inventory.getStackInSlot( 39 ).getItem(), skillName );
		if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
			itemBoost += getWornXpBoost( player, player.inventory.getStackInSlot( 38 ).getItem(), skillName );
		if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
			itemBoost += getWornXpBoost( player, player.inventory.getStackInSlot( 37 ).getItem(), skillName );
		if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
			itemBoost += getWornXpBoost( player, player.inventory.getStackInSlot( 36 ).getItem(), skillName );
		if( !inv.getStackInSlot( 40 ).isEmpty() )	//Off-Hand
			itemBoost += getWornXpBoost( player, player.inventory.getStackInSlot( 40 ).getItem(), skillName );

		return itemBoost;
	}

	public static double getDimensionBoost( PlayerEntity player, Skill skill )
	{
		String dimensionKey = player.world.getDimension().getType().getRegistryName().toString();
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( dimensionKey, new HashMap<>() ).getOrDefault( skill.toString(), 0D );
	}

	public static double getGlobalBoost( Skill skill )
	{
		return JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).getOrDefault( "all_dimensions", new HashMap<>() ).getOrDefault( skill.toString(), 0D );
	}

	public static CompoundNBT writeUniqueId( UUID uuid )
	{
		CompoundNBT compoundnbt = new CompoundNBT();
		compoundnbt.putLong("M", uuid.getMostSignificantBits());
		compoundnbt.putLong("L", uuid.getLeastSignificantBits());
		return compoundnbt;
	}

	public static double getBiomeBoost( PlayerEntity player, Skill skill )
	{
		double biomeBoost = 0;
		double theBiomeBoost = 0;
		double biomePenaltyMultiplier = Config.getConfig( "biomePenaltyMultiplier" );
		String skillName = skill.toString().toLowerCase();

		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVec() ) );
		ResourceLocation resLoc = biome.getRegistryName();
		if( resLoc == null )
			return 0;
		String biomeKey = resLoc.toString();
		Map<String, Double> biomeMap = JsonConfig.data.get( JType.XP_BONUS_BIOME ).get( biomeKey );

		if( biomeMap != null && biomeMap.containsKey( skillName ) )
			theBiomeBoost = biomeMap.get( skillName );

		if( checkReq( player, resLoc, JType.REQ_BIOME ) )
			biomeBoost = theBiomeBoost;
		else
			biomeBoost = Math.min( theBiomeBoost, -biomePenaltyMultiplier * 100 );

		return biomeBoost;
	}

	public static double getMultiplier( PlayerEntity player, Skill skill )
	{
		double multiplier = Config.forgeConfig.globalMultiplier.get();

		double globalMultiplier = getGlobalMultiplier( skill );
		double dimensionMultiplier = getDimensionMultiplier( skill, player );
		double difficultyMultiplier = getDifficultyMultiplier( player, skill );
		double globalBoost = getGlobalBoost( skill );
		double itemBoost = getItemBoost( player, skill );
		double biomeBoost = getBiomeBoost( player, skill );
		double dimensionBoost = getDimensionBoost( player, skill );
		double additiveMultiplier = 1 + (itemBoost + biomeBoost + dimensionBoost + globalBoost ) / 100;

		multiplier *= globalMultiplier;
		multiplier *= dimensionMultiplier;
		multiplier *= difficultyMultiplier;
		multiplier *= additiveMultiplier;

		return multiplier;
	}

	public static double getHorizontalDistance( Vec3d p1, Vec3d p2 )
	{
		return Math.sqrt( Math.pow( ( p1.getX() - p2.getX() ), 2 ) + Math.pow( ( p1.getZ() - p2.getZ() ), 2 ) );
	}

	public static int getMaxVein( PlayerEntity player, Skill skill )
	{
		int maxVein = 0;
		int level = skill.getLevel( player ) - 1;

		switch( skill )
		{
			case MINING:
				maxVein = level / 5;
				break;

			case WOODCUTTING:
				maxVein = level / 2;
				break;

			case EXCAVATION:
				maxVein = level;
				break;

			case FARMING:
				maxVein = level;
				break;
		}

		return maxVein;
	}

	public static void awardXp(PlayerEntity player, Skill skill, @Nullable String sourceName, double amount, boolean skip, boolean ignoreBonuses )
	{
		if( !(player instanceof ServerPlayerEntity) )
		{
			LogHandler.LOGGER.error( "NOT ServerPlayerEntity PLAYER XP AWARD ATTEMPTED! THIS SHOULD NOT HAPPEN! SOURCE: " + sourceName + ", SKILL: " + skill.name() + ", AMOUNT: " + amount + ", CLASS: " + player.getClass().getName().toString() );
			return;
		}

		if( amount <= 0.0f || player.world.isRemote || player instanceof FakePlayer )
			return;

		if( skill.getValue() == 0 )
		{
			LogHandler.LOGGER.error( "INVALID SKILL AT AWARD XP! SOURCE: " + sourceName + ", AMOUNT: " + amount );
			return;
		}

		String skillName = skill.name().toLowerCase();

		if( !ignoreBonuses )
			amount *= getMultiplier( player, skill );

		if( amount == 0 )
			return;

		UUID uuid = player.getUniqueID();
		String playerName = player.getDisplayName().getString();
		int startLevel = skill.getLevel( uuid );
		double startXp = skill.getXp( uuid );
		double maxXp = Config.getConfig( "maxXp" );

		if( startXp >= 2000000000 )
			return;

		if( startXp + amount >= 2000000000 )
		{
			sendMessage( skillName + " cap of 2b xp reached, you fucking psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LogHandler.LOGGER.info( player.getDisplayName().getString() + " " + skillName + " 2b cap reached" );
			amount = 2000000000 - startXp;
		}

		PmmoSavedData.get( player ).addXp( skill, uuid, amount );

		int currLevel = skill.getLevel( uuid );

		if( startLevel != currLevel ) //Level Up! Or Down?
		{
			AttributeHandler.updateAll( player );
			updateRecipes( (ServerPlayerEntity) player );

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
					LogHandler.LOGGER.error( "PMMO Level Up - compatskills command went wrong! args: " + commandArgs, e );
				}
			}

			if( JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skillName.toLowerCase() ) != null )
			{
				Map<String, Double> commandMap = JsonConfig.data.get( JType.LEVEL_UP_COMMAND ).get( skillName.toLowerCase() );

				for( Map.Entry<String, Double> entry : commandMap.entrySet() )
				{
					int commandLevel = (int) Math.floor( entry.getValue() );
					if( startLevel < commandLevel && currLevel >= commandLevel )
					{
						String command = entry.getKey().replace( ">player<", playerName ).replace( ">level<", "" + commandLevel );
						try
						{
							player.getServer().getCommandManager().getDispatcher().execute( command, player.getServer().getCommandSource() );
							LogHandler.LOGGER.info( "Executing command \"" + command + "\"\nTrigger: " + playerName + " level up from " + startLevel + " to " + currLevel + " in " + skill.name() + ", trigger level " + commandLevel );
						}
						catch( CommandSyntaxException e )
						{
							LogHandler.LOGGER.error( "Invalid level up command \"" + command + "\"", e );
						}
					}
				}
			}
		}

		NetworkHandler.sendToPlayer( new MessageXp( startXp, skill.getValue(), amount, skip ), (ServerPlayerEntity) player );
		if( !skip )
			LogHandler.LOGGER.debug( playerName + " +" + amount + "xp in: "  + skillName + " for: " + sourceName + " total xp: " + skill.getXp( uuid ) );

		if( startXp + amount >= maxXp && startXp < maxXp )
		{
			sendMessage( skillName + " max startLevel reached, you psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LogHandler.LOGGER.info( playerName + " " + skillName + " max startLevel reached" );
		}
	}

	public static void awardXpTrigger( UUID uuid, String triggerKey, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		if( JsonConfig.data.get( JType.XP_VALUE_TRIGGER ).containsKey( triggerKey ) )
		{
			awardXpMap( uuid, JsonConfig.data.get( JType.XP_VALUE_TRIGGER ).get( triggerKey ), sourceName, skip, ignoreBonuses );
		}
		else
			LogHandler.LOGGER.error( "TRIGGER XP AWARD \"" + triggerKey + "\" DOES NOT HAVE ANY VALUES, CANNOT AWARD" );
	}

	public static void awardXpMap( UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			Skill.getSkill( entry.getKey() ).addXp( uuid, (double) entry.getValue(), sourceName, skip, ignoreBonuses );
		}
	}

	public static void awardXpMapDouble( UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses )
	{
		for( Map.Entry<String, Double> entry : map.entrySet() )
		{
			Skill.getSkill( entry.getKey() ).addXp( uuid, entry.getValue(), sourceName, skip, ignoreBonuses );
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

	public static void scanUnlocks( int level, Skill skill )
	{

	}

	private static int getGap( int a, int b )
	{
		return a - b;
	}

	public static int getSkillReqGap(PlayerEntity player, ResourceLocation res, JType jType )
	{
		int gap = 0;

		if( !checkReq( player, res, jType ) )
		{
			Map<String, Double> reqs = getReqMap( res.toString(), jType );

			if( reqs != null )
			{
				gap = (int) Math.floor( reqs.entrySet().stream()
						.map( entry -> getGap( (int) Math.floor( entry.getValue() ), Skill.getSkill( entry.getKey() ).getLevel( player ) ) )
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

	public static void spawnRocket( World world, BlockPos pos, Skill skill )
	{
		spawnRocket( world, new Vec3d( pos.getX(), pos.getY(), pos.getZ() ), skill );
	}

	public static void spawnRocket( World world, Vec3d pos, Skill skill )
	{
		CompoundNBT nbt = new CompoundNBT();
		CompoundNBT fw = new CompoundNBT();
		ListNBT explosion = new ListNBT();
		CompoundNBT l = new CompoundNBT();

		int[] colors = new int[1];
		colors[0] = getSkillColor( skill );
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

		PMMOFireworkEntity fireworkRocketEntity = new PMMOFireworkEntity( world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack );
		world.addEntity( fireworkRocketEntity );
	}

	public static void applyWornPenalty( PlayerEntity player, ItemStack itemStack )
	{
		ResourceLocation resLoc = itemStack.getItem().getRegistryName();

		if( !checkReq( player, resLoc, JType.REQ_WEAR ) )
		{
			int gap = getSkillReqGap( player, resLoc, JType.REQ_WEAR );

			if( gap > 9 )
				gap = 9;

			player.addPotionEffect( new EffectInstance( Effects.MINING_FATIGUE, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.WEAKNESS, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.SLOWNESS, 75, gap, false, true ) );

			if( alwaysDropWornItems || EnchantmentHelper.hasBindingCurse( itemStack ) )
			{
				ItemStack droppedItemStack = itemStack.copy();
				player.dropItem( droppedItemStack, false, false );
				itemStack.setCount( 0 );
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToWearDropped", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToWearDropped", new TranslationTextComponent( droppedItemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
			}
			else
				player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToWear", new TranslationTextComponent( itemStack.getItem().getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
		}
	}

	public static void checkBiomeLevelReq(PlayerEntity player)
	{
		Biome biome = player.world.getBiome( vecToBlock( player.getPositionVec() ) );
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
						Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );

						if( effect != null )
							player.addPotionEffect( new EffectInstance( effect, 75, (int) Math.floor( (double) entry.getValue() ), false, false ) );
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
						player.addPotionEffect( new EffectInstance( effect, 75, (int) Math.floor( (double) entry.getValue() ), false, true ) );
				}
				if( player.world.isRemote() )
				{
					if( !lastBiome.get( playerUUID ).equals( biomeKey ) )
					{
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToSurvive", new TranslationTextComponent( biome.getRegistryName().toString() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToSurvive", new TranslationTextComponent( biome.getRegistryName().toString() ) ).setStyle( textStyle.get( "red" ) ), false );
						for( Map.Entry<String, Double> entry : biomeReq.entrySet() )
						{
							int startLevel = Skill.getSkill( entry.getKey() ).getLevel( player );

							if( startLevel < (double) entry.getValue() )
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + entry.getKey() ).getString(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "red" ) ), false );
							else
								player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + entry.getKey() ).getString(), "" + (int) Math.floor( (double) entry.getValue() ) ).setStyle( textStyle.get( "green" ) ), false );
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

	public static Map<Skill, Double> getOfflineXpMap(UUID uuid)
	{
		if( !offlineXp.containsKey( uuid ) )
			offlineXp.put( uuid, new HashMap<>() );
		return offlineXp.get( uuid );
	}

	public static void setOfflineXpMap( UUID uuid, Map<Skill, Double> newOfflineXp )
	{
		offlineXp.put( uuid, newOfflineXp );
	}

	public static void removeOfflineXpUuid( UUID uuid )
	{
		offlineXp.remove( uuid );
	}

	public static int getOfflineLevel( Skill skill, UUID uuid )
	{
		return levelAtXp( getOfflineXp( skill, uuid ) );
	}

	public static double getOfflineXp( Skill skill, UUID uuid )
	{
		if( skill.equals( Skill.INVALID_SKILL ) )
		{
			LogHandler.LOGGER.error( "Invalid Skill at getOfflineXp" );
			return -1;
		}

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
		int maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );

		double xpIncreasePerLevel = Config.getConfig( "xpIncreasePerLevel" );

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

	public static float levelAtXpDecimal( float xp )
	{
		return (float) levelAtXpDecimal( (double) xp );
	}
	public static double levelAtXpDecimal( double xp )
	{
		int maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );

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

		int maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );
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
}