package harmonised.pmmo.skills;

import java.util.*;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.*;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.stream.Collectors;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class XP
{
	private static Map<Material, String> materialHarvestTool = new HashMap<>();
	private static Map<Skill, Integer> skillColors = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger();
	public static Set<UUID> isCrawling = new HashSet<>();
	public static Map<Skill, TextFormatting> skillTextFormat = new HashMap<>();
	public static Map<String, Style> textStyle = new HashMap<>();
	private static Map<UUID, String> lastBiome = new HashMap<>();
	private static double globalMultiplier = Config.forgeConfig.globalMultiplier.get();
	private static boolean broadcastMilestone = Config.forgeConfig.broadcastMilestone.get();
	private static int debugInt = 0;
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

		skillTextFormat.put( Skill.MINING, TextFormatting.AQUA );
		skillTextFormat.put( Skill.BUILDING, TextFormatting.AQUA );
		skillTextFormat.put( Skill.EXCAVATION, TextFormatting.GOLD );
		skillTextFormat.put( Skill.WOODCUTTING, TextFormatting.GOLD );
		skillTextFormat.put( Skill.FARMING, TextFormatting.GREEN );
		skillTextFormat.put( Skill.AGILITY, TextFormatting.GREEN );
		skillTextFormat.put( Skill.ENDURANCE, TextFormatting.DARK_RED );
		skillTextFormat.put( Skill.COMBAT, TextFormatting.RED);
		skillTextFormat.put( Skill.ARCHERY, TextFormatting.YELLOW );
		skillTextFormat.put( Skill.SMITHING, TextFormatting.GRAY );
		skillTextFormat.put( Skill.FLYING, TextFormatting.GRAY );
		skillTextFormat.put( Skill.SWIMMING, TextFormatting.AQUA );
		skillTextFormat.put( Skill.FISHING, TextFormatting.AQUA );
		skillTextFormat.put( Skill.CRAFTING, TextFormatting.GOLD );
		skillTextFormat.put( Skill.MAGIC, TextFormatting.BLUE );
		skillTextFormat.put( Skill.SLAYER, TextFormatting.DARK_GRAY );
		skillTextFormat.put( Skill.HUNTER, TextFormatting.GOLD );
		skillTextFormat.put( Skill.FLETCHING, TextFormatting.DARK_GREEN );
		skillTextFormat.put( Skill.TAMING, TextFormatting.WHITE );

//		skillTextFormat.put(Skill.MINING, TextFormatting.AQUA );
//		skillTextFormat.put( Skill.BUILDING, TextFormatting.AQUA );
//		skillTextFormat.put( Skill.EXCAVATION, TextFormatting.GOLD );
//		skillTextFormat.put( Skill.WOODCUTTING, TextFormatting.GOLD );
//		skillTextFormat.put( Skill.FARMING, TextFormatting.GREEN );
//		skillTextFormat.put( Skill.AGILITY, TextFormatting.GREEN );
//		skillTextFormat.put( Skill.ENDURANCE, TextFormatting.DARK_RED );
//		skillTextFormat.put( Skill.COMBAT, TextFormatting.RED);
//		skillTextFormat.put( Skill.ARCHERY, TextFormatting.YELLOW );
//		skillTextFormat.put( Skill.SMITHING, TextFormatting.GRAY );
//		skillTextFormat.put( Skill.FLYING, TextFormatting.GRAY );
//		skillTextFormat.put( Skill.SWIMMING, TextFormatting.AQUA );
//		skillTextFormat.put( Skill.FISHING, TextFormatting.AQUA );
//		skillTextFormat.put( Skill.CRAFTING, TextFormatting.GOLD );
//		skillTextFormat.put( Skill.MAGIC, TextFormatting.BLUE );
//		skillTextFormat.put( Skill.SLAYER, TextFormatting.DARK_GRAY );
//		skillTextFormat.put( Skill.FLETCHING, TextFormatting.DARK_GREEN );
//		skillTextFormat.put( Skill.TAMING, TextFormatting.WHITE );
////////////////////////////////////Style//////////////////////////////////////////////
		textStyle.put( "red", new Style().setColor( TextFormatting.RED ) );
		textStyle.put( "green", new Style().setColor( TextFormatting.GREEN ) );
		textStyle.put( "yellow", new Style().setColor( TextFormatting.YELLOW ) );
		textStyle.put( "grey", new Style().setColor( TextFormatting.GRAY ) );
////////////////////////////////////DONATORS//////////////////////////////////////////////
		PlayerConnectedHandler.lapisDonators.add( UUID.fromString( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" ) );
		PlayerConnectedHandler.dandelionDonators.add( UUID.fromString( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" ) );
		PlayerConnectedHandler.ironDonators.add( UUID.fromString( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" ) );
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

	public static Skill getSkill( Material material )
	{
		return getSkillFromTool( XP.correctHarvestTool( material ) );
	}

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

			default:
				return Skill.INVALID_SKILL;
		}
	}

	public static Map<String, Double> getXp(ResourceLocation registryName)
	{
		Map<String, Double> theMap = new HashMap<>();

		if( JsonConfig.data.get( "xpValue" ).containsKey( registryName.toString() ) )
		{
			for( Map.Entry<String, Object> entry : JsonConfig.data.get( "xpValue" ).get( registryName.toString() ).entrySet() )
			{
				if( entry.getValue() instanceof Double )
					theMap.put( entry.getKey(), (double) entry.getValue() );
			}
		}

		return theMap;
	}

    public static Map<String, Double> getXpCrafting(ResourceLocation registryName)
    {
        Map<String, Double> theMap = new HashMap<>();

        if( JsonConfig.data.get( "xpValueCrafting" ).containsKey( registryName.toString() ) )
        {
            for( Map.Entry<String, Object> entry : JsonConfig.data.get( "xpValueCrafting" ).get( registryName.toString() ).entrySet() )
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
		if( JsonConfig.data.get( "breakReq" ).containsKey( resLoc.toString() ) )
			highestReq = JsonConfig.data.get( "breakReq" ).get( resLoc.toString() ).entrySet().stream().map(a -> doubleObjectToInt( a.getValue() ) ).reduce( 0, Math::max );
		int startLevel = 1;

		switch( type )
		{
			case "ore":
				if( JsonConfig.data.get( "oreInfo" ).containsKey( regKey ) && JsonConfig.data.get( "oreInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( JsonConfig.data.get( "oreInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) JsonConfig.data.get( "oreInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.MINING, player );
				break;

			case "log":
				if( JsonConfig.data.get( "logInfo" ).containsKey( regKey ) && JsonConfig.data.get( "logInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( JsonConfig.data.get( "logInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) JsonConfig.data.get( "logInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.WOODCUTTING, player );
				break;

			case "plant":
				if( JsonConfig.data.get( "plantInfo" ).containsKey( regKey ) && JsonConfig.data.get( "plantInfo" ).get( regKey ).containsKey( "extraChance" ) )
					if( JsonConfig.data.get( "plantInfo" ).get( regKey ).get( "extraChance" ) instanceof Double )
						extraChancePerLevel = (double) JsonConfig.data.get( "plantInfo" ).get( regKey ).get( "extraChance" );
				startLevel = getLevel( Skill.FARMING, player );
				break;

			default:
				LogHandler.LOGGER.error( "WRONG getExtraChance CHANCE TYPE! PLEASE REPORT!" );
				return 0;
		}

		extraChance = (startLevel - highestReq) * extraChancePerLevel;
		if( extraChance < 0 )
			extraChance = 0;

		return extraChance;
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

	public static void dropItemStack(ItemStack itemStack, World world, BlockPos pos)
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

	public static void syncPlayerConfig( PlayerEntity player )
	{
		NetworkHandler.sendToPlayer( new MessageUpdateReq( JsonConfig.localData, "json" ), (ServerPlayerEntity) player );
		NetworkHandler.sendToPlayer( new MessageUpdateNBT( NBTHelper.mapToNBT( Config.localConfig ), "config" ), (ServerPlayerEntity) player );
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

                LogHandler.LOGGER.info( "REMOVING INVALID SKILL " + tag + " FROM PLAYER " + player.getDisplayName().getString() );
                skillsTag.remove( tag );
            }
        }

        keySet = new HashSet<>( skillsTag.keySet() );

        for( String tag : keySet )
        {
            NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getDouble( tag ), Skill.getInt( tag ), 0, true ), (ServerPlayerEntity) player );
        }
    }

	public static boolean checkReq( PlayerEntity player, ResourceLocation res, String type )
	{
		if( res.equals( Items.AIR.getRegistryName() ) )
			return true;

		if( JsonConfig.data.get( "playerSpecific" ).containsKey( player.getUniqueID().toString() ) )
		{
			if( JsonConfig.data.get( "playerSpecific" ).get( player.getUniqueID().toString() ).containsKey( "ignoreReq" ) )
				return true;
		}

		String registryName = res.toString();
		Map<String, Double> reqMap = new HashMap<>();
		int startLevel;
		boolean failedReq = false;

		switch( type.toLowerCase() )
		{
			case "wear":
				if( JsonConfig.data.get( "wearReq" ).containsKey( registryName ) )
				{
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "wearReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				}
				break;

			case "tool":
				if( JsonConfig.data.get( "toolReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "toolReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "weapon":
				if( JsonConfig.data.get( "weaponReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "weaponReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

            case "mob":
                if( JsonConfig.data.get( "mobReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "mobReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
                break;

            case "use":
                if( JsonConfig.data.get( "useReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "useReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
                break;

            case "place":
				if( JsonConfig.data.get( "placeReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "placeReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "break":
				if( JsonConfig.data.get( "breakReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "breakReq" ).get( registryName ).entrySet() )
					{
						if( entry.getValue() instanceof Double )
							reqMap.put( entry.getKey(), (double) entry.getValue() );
					}
				break;

			case "biome":
				if( JsonConfig.data.get( "biomeReq" ).containsKey( registryName ) )
					for( Map.Entry<String, Object> entry : JsonConfig.data.get( "biomeReq" ).get( registryName ).entrySet() )
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

	public static double getWornXpBoost( PlayerEntity player, Item item, String skillName )
	{
		double boost = 0;

		if( !item.equals( Items.AIR ) )
		{
			String regName = item.getRegistryName().toString();
			Map<String, Object> itemXpMap = JsonConfig.data.get( "wornItemXpBoost" ).get( regName );

			if( itemXpMap != null && itemXpMap.containsKey( skillName ) )
			{
				if( checkReq( player, item.getRegistryName(), "wear" ) )
				{
					boost = (double) itemXpMap.get( skillName );
				}
			}
		}

		return boost;
	}

	public static double getSkillMultiplier( PlayerEntity player, Skill skill )
	{
		double skillMultiplier = 1;

		switch( skill )
		{
			case MINING:
				skillMultiplier = Config.forgeConfig.miningMultiplier.get();
				break;

			case BUILDING:
				skillMultiplier = Config.forgeConfig.buildingMultiplier.get();
				break;

			case EXCAVATION:
				skillMultiplier = Config.forgeConfig.excavationMultiplier.get();
				break;

			case WOODCUTTING:
				skillMultiplier = Config.forgeConfig.woodcuttingMultiplier.get();
				break;

			case FARMING:
				skillMultiplier = Config.forgeConfig.farmingMultiplier.get();
				break;

			case AGILITY:
				skillMultiplier = Config.forgeConfig.agilityMultiplier.get();
				break;

			case ENDURANCE:
				skillMultiplier = Config.forgeConfig.enduranceMultiplier.get();
				break;

			case COMBAT:
				skillMultiplier = Config.forgeConfig.combatMultiplier.get();
				break;

			case ARCHERY:
				skillMultiplier = Config.forgeConfig.archeryMultiplier.get();
				break;

			case SMITHING:
				skillMultiplier = Config.forgeConfig.repairingMultiplier.get();
				break;

			case FLYING:
				skillMultiplier = Config.forgeConfig.flyingMultiplier.get();
				break;

			case SWIMMING:
				skillMultiplier = Config.forgeConfig.swimmingMultiplier.get();
				break;

			case FISHING:
				skillMultiplier = Config.forgeConfig.fishingMultiplier.get();
				break;

			case CRAFTING:
				skillMultiplier = Config.forgeConfig.craftingMultiplier.get();
				break;

			default:
				break;
		}

		return skillMultiplier;
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
		double itemBoost = 0;

		String skillName = skill.toString().toLowerCase();
		String regKey = player.getHeldItemMainhand().getItem().getRegistryName().toString();
		Map<String, Object> heldMap = JsonConfig.data.get( "heldItemXpBoost" ).get( regKey );
		PlayerInventory inv = player.inventory;

		if( heldMap != null )
		{
			if( heldMap.containsKey( skillName ) )
				itemBoost += (double) heldMap.get( skillName );
		}

		if( Curios.isLoaded() )
		{
			Collection<IItemHandler> curiosItems = Curios.getCurios(player).collect(Collectors.toSet());

			for( IItemHandler value : curiosItems )
			{
				for (int i = 0; i < value.getSlots(); i++)
				{
					itemBoost += getWornXpBoost( player, value.getStackInSlot(i).getItem(), skillName );
				}
			};
		}

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

	public static double getBiomeBoost( PlayerEntity player, Skill skill )
	{
		double biomeBoost = 0;
		double theBiomeBoost = 0;
		double biomePenaltyMultiplier = Config.getConfig( "biomePenaltyMultiplier" );
		String skillName = skill.toString().toLowerCase();

		Biome biome = player.world.getBiome( player.getPosition() );
		ResourceLocation resLoc = biome.getRegistryName();
		String biomeKey = resLoc.toString();
		Map<String, Object> biomeMap = JsonConfig.data.get( "biomeXpBonus" ).get( biomeKey );

		if( biomeMap != null && biomeMap.containsKey( skillName ) )
		{
			theBiomeBoost = (double) biomeMap.get( skillName );

			if( !checkReq( player, resLoc, "biome" ) )
			{
				if( theBiomeBoost > -biomePenaltyMultiplier * 100 )
					biomeBoost = -biomePenaltyMultiplier * 100;
				else
					biomeBoost = theBiomeBoost;
			}
			else
				biomeBoost = theBiomeBoost;
		}

		return biomeBoost;
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
		double maxXp = Config.getConfig( "maxXp" );

		if( amount <= 0.0f || player.world.isRemote || player instanceof FakePlayer )
			return;

		if( skill == skill.INVALID_SKILL )
		{
			LogHandler.LOGGER.error( "Invalid skill at awardXp" );
			return;
		}

		String skillName = skill.name().toLowerCase();
		double skillMultiplier = getSkillMultiplier( player, skill );
		double difficultyMultiplier = getDifficultyMultiplier( player, skill );

		double itemBoost = getItemBoost( player, skill );
		double biomeBoost = getBiomeBoost( player, skill );
		double additiveMultiplier = 1 + (itemBoost + biomeBoost) / 100;

		amount *= skillMultiplier;
		amount *= difficultyMultiplier;
		amount *= additiveMultiplier;

 		amount *= globalMultiplier;

		if( amount == 0 )
			return;

		String playerName = player.getDisplayName().getString();
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
				LogHandler.LOGGER.info( player.getDisplayName().getString() + " " + skillName + " 2b cap reached" );
				amount = 2000000000 - startXp;
			}

			skillsTag.putDouble( skillName, (startXp + amount) );
		}

		currLevel = levelAtXp( skillsTag.getDouble( skillName ) );

		if( startLevel != currLevel )
		{
			AttributeHandler.updateAll( player );

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

			LogHandler.LOGGER.info( playerName + " " + currLevel + " " + skillName + " level up!" );

			if( currLevel % 10 == 0 && broadcastMilestone )
			{
				player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
				{
					if( thePlayer.getUniqueID() != player.getUniqueID() )
						NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.milestoneLevelUp", player.getDisplayName().getString(), "" + currLevel , "pmmo." + skillName, false, 3 ), thePlayer );
				});
			}

			if( JsonConfig.data.get( "levelUpCommand" ).get( skillName.toLowerCase() ) != null )
			{
				Map<String, Object> commandMap = JsonConfig.data.get( "levelUpCommand" ).get( skillName.toLowerCase() );

				for( Map.Entry<String, Object> entry : commandMap.entrySet() )
				{
					int commandLevel = (int) Math.floor( (double) entry.getValue() );
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

		if( player instanceof ServerPlayerEntity )
			NetworkHandler.sendToPlayer( new MessageXp( startXp, skill.getValue(), amount, skip ), (ServerPlayerEntity) player );

		if( !skip )
			LogHandler.LOGGER.debug( ( playerName + " +" + amount + "xp in "  + skillName + " for " + sourceName + " total xp: " + skillsTag.getDouble( skillName ) ) );

		if( startXp + amount >= maxXp && startXp < maxXp )
		{
			sendMessage( skillName + " max startLevel reached, you psycho!", false, player, TextFormatting.LIGHT_PURPLE );
			LogHandler.LOGGER.info( playerName + " " + skillName + " max startLevel reached" );
		}
	}

	private static int getGap( int a, int b )
	{
		int gap = a - b;

		return gap;
	}

	public static int getSkillReqGap(PlayerEntity player, ResourceLocation res, String type)
	{
		int gap = 0;

		if( !checkReq( player, res, type ) )
		{
			Map<String, Object> theMap = null;
			Map<String, Double> reqs = new HashMap<>();
			switch( type )
			{
				case "wear":
					if( JsonConfig.data.get( "wearReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "wearReq" ).get( res.toString() );
					break;

				case "tool":
					if( JsonConfig.data.get( "toolReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "toolReq" ).get( res.toString() );
					break;

				case "weapon":
					if( JsonConfig.data.get( "weaponReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "weaponReq" ).get( res.toString() );
					break;

				case "use":
					if( JsonConfig.data.get( "useReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "useReq" ).get( res.toString() );
					break;

				case "place":
					if( JsonConfig.data.get( "placeReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "placeReq" ).get( res.toString() );
					break;

				case "break":
					if( JsonConfig.data.get( "breakReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "breakReq" ).get( res.toString() );
					break;

				case "biome":
					if( JsonConfig.data.get( "biomeReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "biomeReq" ).get( res.toString() );
					break;

				case "slayer":
					if( JsonConfig.data.get( "killReq" ).containsKey( res.toString() ) )
						theMap = JsonConfig.data.get( "killReq" ).get( res.toString() );
					break;

				default:
					LogHandler.LOGGER.error( "ERROR getSkillReqGap wrong type" );
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

	public static void applyWornPenalty(PlayerEntity player, Item item)
	{
		if( !checkReq( player, item.getRegistryName(), "wear" ) )
		{
			int gap = getSkillReqGap( player, item.getRegistryName(), "wear" );
			if( gap > 0 )
			    player.sendStatusMessage( new TranslationTextComponent( "pmmo.toWear", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );

			if( gap > 9 )
				gap = 9;

			player.addPotionEffect( new EffectInstance( Effects.MINING_FATIGUE, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.WEAKNESS, 75, gap, false, true ) );
			player.addPotionEffect( new EffectInstance( Effects.SLOWNESS, 75, gap, false, true ) );
		}
	}

	public static void checkBiomeLevelReq(PlayerEntity player)
	{
		Biome biome = player.world.getBiome( player.getPosition() );
		ResourceLocation resLoc = biome.getRegistryName();
		String biomeKey = resLoc.toString();
		UUID playerUUID = player.getUniqueID();
		Map<String, Object> biomeReq = JsonConfig.data.get( "biomeReq" ).get( biomeKey );
		Map<String, Object> biomeEffect = JsonConfig.data.get( "biomeEffect" ).get( biomeKey );

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
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.toSurvive", new TranslationTextComponent( biome.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), true );
						player.sendStatusMessage( new TranslationTextComponent( "pmmo.toSurvive", new TranslationTextComponent( biome.getTranslationKey() ) ).setStyle( textStyle.get( "red" ) ), false );
						for( Map.Entry<String, Object> entry : biomeReq.entrySet() )
						{
							int startLevel = getLevel( Skill.getSkill( entry.getKey() ), player );

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
	
	public static double getWeight(int startLevel, Map<String, Object> fishItem)
	{
		return DP.map( startLevel, (double) fishItem.get( "startLevel" ), (double) fishItem.get( "endLevel" ), (double) fishItem.get( "startWeight" ), (double) fishItem.get( "endWeight" ) );
	}

	public static int levelAtXp( float xp )
	{
		return levelAtXp( (double) xp );
	}
	public static int levelAtXp( double xp )
	{
		double baseXp = Config.getConfig( "baseXp" );
		double xpIncreasePerLevel = Config.getConfig( "xpIncreasePerLevel" );
		int maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );

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

	public static double xpAtLevel( float givenLevel )
	{
		return xpAtLevel( (double) givenLevel );
	}
	public static double xpAtLevel( double givenLevel )
	{
		double baseXp = Config.getConfig( "baseXp" );
		double xpIncreasePerLevel = Config.getConfig( "xpIncreasePerLevel" );
		int maxLevel = (int) Math.floor( Config.getConfig( "maxLevel" ) );

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

