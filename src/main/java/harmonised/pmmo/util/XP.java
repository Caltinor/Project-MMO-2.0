package harmonised.pmmo.util;

import java.util.*;
import java.util.stream.Collectors;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.PredicateRegistry;
import harmonised.pmmo.api.TooltipSupplier;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.api.events.XpEvent;
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
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.*;
//import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class XP
{
	public static final Logger LOGGER = LogManager.getLogger();

	private static final Map<Material, String> materialHarvestTool = new HashMap<Material, String>()
	{{
		put(Material.HEAVY_METAL, "pickaxe");		//PICKAXE
		put(Material.GLASS, "pickaxe");
		put(Material.ICE, "pickaxe");
		put(Material.METAL, "pickaxe");
		put(Material.ICE_SOLID, "pickaxe");
		put(Material.PISTON, "pickaxe");
		put(Material.BUILDABLE_GLASS, "pickaxe");
		put(Material.STONE, "pickaxe");
		put(Material.SHULKER_SHELL, "pickaxe");
		put(Material.BARRIER, "pickaxe");
		put(Material.DECORATION, "pickaxe");
		put(Material.WOOD, "axe");			//AXE
		put(Material.LEAVES, "axe");
		put(Material.VEGETABLE, "axe");
		put(Material.CLAY, "shovel");			//SHOVEL
		put(Material.DIRT, "shovel");
		put(Material.SAND, "shovel");
		put(Material.TOP_SNOW, "shovel");
		put(Material.REPLACEABLE_WATER_PLANT, "shovel");
		put(Material.SNOW, "shovel");
		put(Material.PLANT, "hoe");			//HOE
		put(Material.WATER_PLANT, "hoe");
		put(Material.CACTUS, "hoe");
		put(Material.REPLACEABLE_PLANT, "hoe");
		put(Material.BAMBOO, "hoe");
		put(Material.BAMBOO_SAPLING, "hoe");
		put(Material.GRASS, "hoe");
		put(Material.WOOL, "shears");			//SHEARS (Crafting)
		put(Material.CLOTH_DECORATION, "shears");
		put(Material.EXPLOSIVE, "shears");
		put(Material.EGG, "shears");
		put(Material.WEB, "shears");
		put(Material.CAKE, "shears");
		put(Material.SPONGE, "shears");
	}};
	public static Set<UUID> isVeining = new HashSet<>();
	public static Map<String, Style> textStyle = new HashMap<String, Style>()
	{{
		put("red", 		Style.EMPTY.applyFormat(ChatFormatting.RED));
		put("green", 		Style.EMPTY.applyFormat(ChatFormatting.GREEN));
		put("dark_green", 	Style.EMPTY.applyFormat(ChatFormatting.DARK_GREEN));
		put("yellow", 		Style.EMPTY.applyFormat(ChatFormatting.YELLOW));
		put("grey", 		Style.EMPTY.applyFormat(ChatFormatting.GRAY));
		put("cyan", 		Style.EMPTY.applyFormat(ChatFormatting.AQUA));
		put("blue", 		Style.EMPTY.applyFormat(ChatFormatting.BLUE));
		put("dark_blue", 	Style.EMPTY.applyFormat(ChatFormatting.DARK_BLUE));
		put("pink", 		Style.EMPTY.applyFormat(ChatFormatting.LIGHT_PURPLE));
		put("dark_purple", Style.EMPTY.applyFormat(ChatFormatting.DARK_PURPLE));
	}};
	public static Map<UUID, String> playerNames = new HashMap<>();
	public static Map<String, UUID> playerUUIDs = new HashMap<>();
	public static Map<UUID, Map<String, Double>> offlineXp = new HashMap<>();
	private static Map<UUID, String> lastBiome = new HashMap<>();
	//private static int debugInt = 0;

	public static Style getColorStyle(int color)
	{
		return Style.EMPTY.withColor(TextColor.fromRgb(color));
	}

	public static void initValues()
	{
////////////////////////////////////Style//////////////////////////////////////////////
		WorldText.init();
	}

	public static String getSkill(BlockState state)
	{
		String skill = getSkillFromTool(getHarvestTool(state));
		if(skill.equals(Skill.INVALID_SKILL.toString()))
			return getSkill(state.getBlock());
		else
			return skill;
	}

	public static String getSkill(Block block)
	{
		if(block.getTags().contains(getResLoc( "forge:ores")))
			return Skill.MINING.toString();
		else if(block.getTags().contains(getResLoc("forge:logs")))
			return Skill.WOODCUTTING.toString();
		else if(block.getTags().contains(getResLoc("forge:plants")))
			return Skill.FARMING.toString();
		else
			return Skill.INVALID_SKILL.toString();
	}

	public static String getSkillFromTool(String tool)
	{
		if(tool == null)
			return Skill.INVALID_SKILL.toString();

		switch(tool)
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

	public static Map<String, Double> getXp(BlockEntity tile, JType jType)
	{
		ResourceLocation res = tile.getBlockState().getBlock().getRegistryName();
		
		if (TooltipSupplier.tooltipExists(res, jType))
			return TooltipSupplier.getTooltipData(res, jType, tile);
		
		return getXp(res, jType);
	}
	
	public static Map<String, Double> getXp(ItemStack stack, JType jType)
	{
		ResourceLocation res = stack.getItem().getRegistryName();
		
		if (TooltipSupplier.tooltipExists(res, jType))
			return TooltipSupplier.getTooltipData(res, jType, stack);
		
		return getXp(res, jType);
	}
	
	public static Map<String, Double> getXp(Entity entity, JType jType)
	{
		ResourceLocation res = entity.getType().getRegistryName();
		
		if (TooltipSupplier.tooltipExists(res, jType))
			return TooltipSupplier.getTooltipData(res, jType, entity);
		
		return getXp(res, jType);
	}
	
	/**	This method is called to deliberately bypass the public getXP methods
	 * this should only be used when an action does not require an API check.
	 * 
	 * @param registryName the object resourcelocation being passed
	 * @param jType the JType being passed.
	 * @return a map of the skill and xp value
	 */
	public static Map<String, Double> getXpBypass(ResourceLocation registryName, JType jType)
	{
		return getXp(registryName, jType);
	}

	public static Map<String, Double> getXpBypass(String registryName, JType jType)
	{
		return getXp(registryName, jType);
	}

	private static Map<String, Double> getXp(ResourceLocation registryName, JType jType)
	{
		return getXp(registryName.toString(), jType);
	}

	private static Map<String, Double> getXp(String registryName, JType jType)
	{
		return new HashMap<>(JsonConfig.data.get(jType).getOrDefault(registryName, new HashMap<>()));
	}

	public static ResourceLocation getBiomeResLoc(Level world, Biome biome)
	{
		return world.registryAccess().registry(Registry.BIOME_REGISTRY).get().getKey(biome);
	}

	public static ResourceLocation getBiomeResLoc(Level world, BlockPos pos)
	{
		return world.getBiomeName(pos).get().getRegistryName();
	}

	public static ResourceLocation getDimResLoc(Level world)
	{
		return world.dimension().location();
//		ResourceLocation dimResLoc = world.getDimensionKey().getLocation();
//		if(dimResLoc.toString().equals("minecraft:dimension"))
//			return world.registryAccess().dimensionTypes().getKey(world.getDimensionType());
//		else
//			return dimResLoc;
	}

	public static String getHarvestTool(BlockState state)
	{
		String correctTool = materialHarvestTool.getOrDefault(state.getMaterial(), "none");

		if(correctTool.equals("none"))
		{
			double pickDestroySpeed   = new ItemStack(Items.DIAMOND_PICKAXE).getDestroySpeed(state);
			double axeDestroySpeed    = new ItemStack(Items.DIAMOND_AXE).getDestroySpeed(state);
			double shovelDestroySpeed = new ItemStack(Items.DIAMOND_SHOVEL).getDestroySpeed(state);
			double swordDestroySpeed = new ItemStack(Items.DIAMOND_SWORD).getDestroySpeed(state);

			double highestDestroySpeed = pickDestroySpeed;
			correctTool = "pickaxe";

			if(highestDestroySpeed < axeDestroySpeed)
			{
				highestDestroySpeed = axeDestroySpeed;
				correctTool = "axe";
			}
			if(highestDestroySpeed < shovelDestroySpeed)
			{
				highestDestroySpeed = shovelDestroySpeed;
				correctTool = "shovel";
			}
			if(highestDestroySpeed < swordDestroySpeed)
			{
//				highestDestroySpeed = swordDestroySpeed;
				correctTool = "shears";
			}
		}

		return correctTool;
	}

	public static String checkMaterial(Material material)
	{
		if(material.equals(Material.AIR))
			return "AIR";
		else if(material.equals(Material.STRUCTURAL_AIR))
			return "STRUCTURE_VOID";
		else if(material.equals(Material.PORTAL))
			return "PORTAL";
		else if(material.equals(Material.CLOTH_DECORATION))
			return "CARPET";
		else if(material.equals(Material.PLANT))
			return "PLANTS";
		else if(material.equals(Material.WATER_PLANT))
			return "OCEAN_PLANT";
		else if(material.equals(Material.REPLACEABLE_PLANT))
			return "TALL_PLANTS";
		else if(material.equals(Material.REPLACEABLE_WATER_PLANT))
			return "SEA_GRASS";
		else if(material.equals(Material.WATER))
			return "WATER";
		else if(material.equals(Material.BUBBLE_COLUMN))
			return "BUBBLE_COLUMN";
		else if(material.equals(Material.LAVA))
			return "LAVA";
		else if(material.equals(Material.TOP_SNOW))
			return "SNOW";
		else if(material.equals(Material.FIRE))
			return "FIRE";
		else if(material.equals(Material.DECORATION))
			return "MISCELLANEOUS";
		else if(material.equals(Material.WEB))
			return "WEB";
		else if(material.equals(Material.BUILDABLE_GLASS))
			return "REDSTONE_LIGHT";
		else if(material.equals(Material.CLAY))
			return "CLAY";
		else if(material.equals(Material.DIRT))
			return "EARTH";
		else if(material.equals(Material.GRASS))
			return "ORGANIC";
		else if(material.equals(Material.ICE_SOLID))
			return "PACKED_ICE";
		else if(material.equals(Material.SAND))
			return "SAND";
		else if(material.equals(Material.SPONGE))
			return "SPONGE";
		else if(material.equals(Material.SHULKER_SHELL))
			return "SHULKER";
		else if(material.equals(Material.WOOD))
			return "WOOD";
		else if(material.equals(Material.BAMBOO_SAPLING))
			return "BAMBOO_SAPLING";
		else if(material.equals(Material.BAMBOO))
			return "BAMBOO";
		else if(material.equals(Material.WOOL))
			return "WOOL";
		else if(material.equals(Material.EXPLOSIVE))
			return "TNT";
		else if(material.equals(Material.LEAVES))
			return "LEAVES";
		else if(material.equals(Material.GLASS))
			return "GLASS";
		else if(material.equals(Material.ICE))
			return "ICE";
		else if(material.equals(Material.CACTUS))
			return "CACTUS";
		else if(material.equals(Material.STONE))
			return "ROCK";
		else if(material.equals(Material.METAL))
			return "IRON";
		else if(material.equals(Material.SNOW))
			return "SNOW_BLOCK";
		else if(material.equals(Material.HEAVY_METAL))
			return "ANVIL";
		else if(material.equals(Material.BARRIER))
			return "BARRIER";
		else if(material.equals(Material.PISTON))
			return "PISTON";
		else if(material.equals(Material.VEGETABLE))
			return "GOURD";
		else if(material.equals(Material.EGG))
			return "DRAGON_EGG";
		else if(material.equals(Material.CAKE))
			return "CAKE";
		else
			return "UNKNOWN";
	}

	public static void sendMessage(String msg, boolean bar, Player player)
	{
		player.displayClientMessage(new TextComponent(msg), bar);
	}

	public static void sendMessage(String msg, boolean bar, Player player, ChatFormatting format)
	{
		player.displayClientMessage(new TextComponent(msg).setStyle(Style.EMPTY.applyFormat(format)), bar);
	}

	public static <T> Map<T, Double> addMapsAnyDouble(Map<T, Double> mapOne, Map<T, Double> mapTwo)
	{
		for(T key : mapTwo.keySet())
		{
			if(mapOne.containsKey(key))
				mapOne.replace(key, mapOne.get(key) + mapTwo.get(key));
			else
				mapOne.put(key, mapTwo.get(key));

		}

		return mapOne;
	}

	private static int doubleDoubleToInt(Double object)
	{
		return (int) Math.floor(object);
	}

	public static double getExtraChance(UUID uuid, String resLoc, JType jType, boolean offline)
	{
		return getExtraChance(uuid, XP.getResLoc(resLoc), jType, offline);
	}

	public static double getExtraChance(UUID uuid, ResourceLocation resLoc, JType jType, boolean offline)
	{
		String regKey = resLoc.toString();
		double extraChancePerLevel = 0;
		double extraChance;
		int highestReq = 1;
		if(JsonConfig.data.get(JType.REQ_BREAK).containsKey(resLoc.toString()))
			highestReq = JsonConfig.data.get(JType.REQ_BREAK).get(resLoc.toString()).entrySet().stream().map(a -> doubleDoubleToInt(a.getValue())).reduce(0, Math::max);
		int startLevel;
		String skill;

		switch(jType)
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
				LOGGER.error("WRONG getExtraChance CHANCE TYPE! PLEASE REPORT!");
				return 0;
		}

		startLevel = offline ? XP.getOfflineLevel(skill, uuid) : Skill.getLevel(skill, uuid);

		if(JsonConfig.data.get(jType).containsKey(regKey) && JsonConfig.data.get(jType).get(regKey).containsKey("extraChance"))
			if(JsonConfig.data.get(jType).get(regKey).get("extraChance") != null)
				extraChancePerLevel = JsonConfig.data.get(jType).get(regKey).get("extraChance");

		extraChance = (startLevel - highestReq) * extraChancePerLevel;
		if(extraChance < 0)
			extraChance = 0;

		return extraChance;
	}

	public static boolean hasElement(ResourceLocation key, JType jType)
	{
		return hasElement(key.toString(), jType);
	}

	public static boolean hasElement(String key, JType jType)
	{
		return JsonConfig.data.get(jType).containsKey(key);
	}

	public static boolean rollChance(double extraChance)
	{
		return Math.random() < extraChance;
	}

	public static void dropItems(int dropsLeft, Item item, Level world, BlockPos pos)
	{
		if(dropsLeft > 0)
		{
			while(dropsLeft > 64)
			{
				Block.popResource(world, pos, new ItemStack(item, 64));
				dropsLeft -= 64;
			}
			Block.popResource(world, pos, new ItemStack(item, dropsLeft));
		}
	}

	public static void dropItemStack(ItemStack itemStack, Level world, Vec3 pos)
	{
		dropItemStack(itemStack, world, new BlockPos(pos));
	}

	public static void dropItemStack(ItemStack itemStack, Level world, BlockPos pos)
	{
		Block.popResource(world, pos, itemStack);
	}

	public static boolean isPlayerSurvival(Player player)
	{
		if(player.isCreative() || player.isSpectator())
			return false;
		else
			return true;
	}

	public static Collection<Player> getNearbyPlayers(Entity mob)
	{
		Level world = mob.getCommandSenderWorld();
		List<? extends Player> allPlayers = world.players();
		Collection<Player> nearbyPlayers = new ArrayList<>();

		Float closestDistance = null;
		float tempDistance;

		for(Player player : allPlayers)
		{
			tempDistance = mob.distanceTo(player);
			if(closestDistance == null || tempDistance < closestDistance)
				closestDistance = tempDistance;
		}

		if(closestDistance != null)
		{
			float searchRange = closestDistance + 30;

			for(Player player : allPlayers)
			{
				if(mob.distanceTo(player) < searchRange)
					nearbyPlayers.add(player);
			}
		}

		return nearbyPlayers;
	}

	public static float getPowerLevel(UUID uuid)
    {
		int enduranceLevel = Skill.getLevel(Skill.ENDURANCE.toString(), uuid);

		int combatLevel = Skill.getLevel(Skill.COMBAT.toString(), uuid);
		int archeryLevel = Skill.getLevel(Skill.ARCHERY.toString(), uuid);
		int magicLevel = Skill.getLevel(Skill.MAGIC.toString(), uuid);
		int gunslingLevel = Skill.getLevel(Skill.GUNSLINGING.toString(), uuid);

		int maxOffensive = combatLevel;
		if(maxOffensive < archeryLevel)
			maxOffensive = archeryLevel;
		if(maxOffensive < magicLevel)
			maxOffensive = magicLevel;
		if(maxOffensive < gunslingLevel)
			maxOffensive = gunslingLevel;

		return (enduranceLevel + (maxOffensive * 1.5f)) / 50;
    }

	public static ServerPlayer getPlayerByUUID(UUID uuid)
	{
		return getPlayerByUUID(uuid, PmmoSavedData.getServer());
	}

	public static ServerPlayer getPlayerByUUID(UUID uuid, MinecraftServer server)
	{
		ServerPlayer matchedPlayer = null;

		for(ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if(player.getUUID().equals(uuid))
			{
				matchedPlayer = player;
				break;
			}
		}

		return matchedPlayer;
	}

	public static double getDistance(Vec3 a, Vec3 b)
	{
		return Math.sqrt(Math.pow(a.x() - b.x(), 2) + Math.pow(a.y() - b.y(), 2) + Math.pow(a.z() - b.z(), 2));
	}

	public static <T extends Entity> Set<T> getEntitiesInRange(Vec3 origin, Set<T> entities, double range)
	{
		Set<T> withinRange = new HashSet<>();
		Vec3 pos;
		for(T entity : entities)
		{
			pos = entity.position();
			double distance = getDistance(origin, pos);
			if(distance <= range)
				withinRange.add(entity);
		}

		return withinRange;
	}

	public static void syncPlayerDataAndConfig(Player player)
	{
		syncPlayerData3(player);
		syncPlayerData4(player);
		syncPlayerXpBoost(player);
		syncPlayersSkills(player);
		NetworkHandler.sendToPlayer(new MessageUpdateBoolean(true, 1), (ServerPlayer) player);
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.mapStringToNbt(Config.localConfig), 2), (ServerPlayer) player);
	}

	public static void syncPlayersSkills(Player player)
	{
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.xpMapsToNbt(PmmoSavedData.get().getAllXpMaps()), 3), (ServerPlayer) player);
	}

	public static void syncPlayerXpBoost(Player player)
	{
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.mapStringMapStringToNbt(APIUtils.getXpBoostsMap(player)), 6), (ServerPlayer) player);
	}

	public static void syncPlayerXpBoost(UUID uuid)
	{
		ServerPlayer player = getPlayerByUUID(uuid, PmmoSavedData.getServer());
		if(player != null)
			NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.mapStringMapStringToNbt(APIUtils.getXpBoostsMap(player)), 6), (ServerPlayer) player);
	}

	public static void syncPlayerData3(Player player)
	{
		CompoundTag fullData = NBTHelper.data3ToNbt(JsonConfig.localData);
		CompoundTag dataChunk = new CompoundTag();
		dataChunk.putBoolean("wipe", true);
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 4), (ServerPlayer) player);
		dataChunk = new CompoundTag();

		int i = 0;
		for(String key3 : fullData.getAllKeys())
		{
			if(!dataChunk.contains(key3))
				dataChunk.put(key3, new CompoundTag());

			for(String key2 : fullData.getCompound(key3).getAllKeys())
			{
				if(i >= 1000)	//if any single JType reaches 1000 Elements, send chunk reset count
				{
					i = 0;
					NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 4), (ServerPlayer) player);
					dataChunk = new CompoundTag();
					dataChunk.put(key3, new CompoundTag());
				}

				dataChunk.getCompound(key3).put(key2, fullData.getCompound(key3).getCompound(key2));
				i++;
			}
		}

		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 4), (ServerPlayer) player);
	}

	public static void syncPlayerData4(Player player)
	{
		CompoundTag fullData = NBTHelper.data4ToNbt(JsonConfig.localData2);
		CompoundTag dataChunk = new CompoundTag();
		dataChunk.putBoolean("wipe", true);
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 5), (ServerPlayer) player);
		dataChunk = new CompoundTag();

		int i = 0;
		for(String key4 : fullData.getAllKeys())
		{
			if(!dataChunk.contains(key4))
				dataChunk.put(key4, new CompoundTag());

			for(String key3 : fullData.getCompound(key4).getAllKeys())
			{
				if(!dataChunk.getCompound(key4).contains(key3))
					dataChunk.getCompound(key4).put(key3, new CompoundTag());

				for(String key2 : fullData.getCompound(key4).getCompound(key3).getAllKeys())
				{
					if(i >= 1000)	//if any single JType reaches 1000 Elements, send chunk reset count
					{
						i = 0;
						NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 5), (ServerPlayer) player);
						dataChunk = new CompoundTag();
						dataChunk.put(key4, new CompoundTag());
						dataChunk.getCompound(key4).put(key3, new CompoundTag());
					}

					dataChunk.getCompound(key4).getCompound(key3).put(key2, fullData.getCompound(key4).getCompound(key3).getCompound(key2));
					i++;
				}
			}
		}

		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(dataChunk, 5), (ServerPlayer) player);
	}

	public static void syncPlayer(Player player)
    {
//    	UUID uuid = player.getUniqueID();
//        CompoundNBT xpTag 		 = NBTHelper.mapStringToNbt (Config.getXpMap(player));
        CompoundTag prefsTag 	 = NBTHelper.mapStringToNbt(Config.getPreferencesMap(player));
		CompoundTag abilitiesTag = NBTHelper.mapStringToNbt(Config.getAbilitiesMap(player));

		syncPlayerDataAndConfig(player);
		updateRecipes((ServerPlayer) player);

        NetworkHandler.sendToPlayer(new MessageXp(0f, "42069", 0f, true), (ServerPlayer) player);
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(prefsTag, 0), (ServerPlayer) player);
		NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(abilitiesTag, 1), (ServerPlayer) player);
		AttributeHandler.updateAll(player);

        for(Map.Entry<String, Double> entry : Config.getXpMap(player).entrySet())
        {
            NetworkHandler.sendToPlayer(new MessageXp(entry.getValue(), entry.getKey(), 0, true), (ServerPlayer) player);
        }
    }

	public static ResourceLocation getResLoc(String regKey)
	{
		try
		{
			return new ResourceLocation(regKey);
		}
		catch(Exception e)
		{
			return new ResourceLocation("");
		}
	}

	public static ResourceLocation getResLoc(String firstPart, String secondPart)
	{
		try
		{
			return new ResourceLocation(firstPart, secondPart);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static Map<String, Double> getJsonMap(ResourceLocation registryName, JType type)
	{
		return getJsonMap(registryName.toString(), type);
	}

	public static Map<String, Double> getJsonMap(String registryName, JType type)
	{
		return JsonConfig.data.getOrDefault(type, new HashMap<>()).getOrDefault(registryName, new HashMap<>());
	}

	public static boolean checkReq(Player player, String res, JType jType)
	{
		return checkReq(player, XP.getResLoc(res), jType);
	}
	
	@SuppressWarnings("deprecation")
	public static boolean checkReq(Player player, BlockEntity tile, JType jType)
	{
		if(tile == null)
			return true;
		
		if(tile.getBlockState().isAir())
			return true;
		
		if(PredicateRegistry.predicateExists(tile.getBlockState().getBlock().getRegistryName(), jType))
			return PredicateRegistry.checkPredicateReq(player, tile, jType);
		
		return checkReq(player, getJsonMap(tile.getBlockState().getBlock().getRegistryName().toString(), jType));
	}

	public static boolean checkReq(Player player, ResourceLocation res, JType jType)
	{
		if(res == null)
			return true;

		if(res.equals(Items.AIR.getRegistryName()) || player.isCreative())
			return true;
		
		if(PredicateRegistry.predicateExists(res, jType)) 
			return PredicateRegistry.checkPredicateReq(player, res, jType);
		
		return checkReq(player, getJsonMap(res.toString(), jType));
	}

	public static boolean checkReq(Player player, Map<String, Double> reqMap)
	{
		boolean failedReq = false;

		try
		{
			if(JsonConfig.data.get(JType.PLAYER_SPECIFIC).containsKey(player.getUUID().toString()))
			{
				if(JsonConfig.data.get(JType.PLAYER_SPECIFIC).get(player.getUUID().toString()).containsKey("ignoreReq"))
					return true;
			}
			double startLevel;

//		if(reqMap == null)
//			failedReq = true;

			if(reqMap != null)
			{
				for(Map.Entry<String, Double> entry : reqMap.entrySet())
				{
					startLevel = Skill.getLevel(entry.getKey(), player);

					if(startLevel < entry.getValue())
						failedReq = true;
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
		}

		return !failedReq;
	}

	public static int getHighestReq(String regKey, JType jType)
	{
		int highestReq = 1;

		Map<String, Double> map = XP.getJsonMap(regKey, jType);

		if(map != null)
		{
			for(Map.Entry<String, Double> entry : map.entrySet())
			{
				if(highestReq < entry.getValue())
					highestReq = (int) (double) entry.getValue();
			}
		}

		return highestReq;
	}

	public static Set<String> getElementsFromTag(String tag)
	{
		Set<String> results = new HashSet<>();

		for(Map.Entry<ResourceLocation, Tag<Item>> namedTag : ItemTags.getAllTags().getAllTags().entrySet())
		{
			if(namedTag.getKey().toString().startsWith(tag))
			{
				for(Item element : namedTag.getValue().getValues())
				{
					try
					{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<Block>> namedTag : BlockTags.getAllTags().getAllTags().entrySet())
		{
			if(namedTag.getKey().toString().equals(tag))
			{
				for(Block element : namedTag.getValue().getValues())
				{
					try
					{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<Fluid>> namedTag : FluidTags.getAllTags().getAllTags().entrySet())
		{
			if(namedTag.getKey().toString().equals(tag))
			{
				for(Fluid element : namedTag.getValue().getValues())
				{
					try
					{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		for(Map.Entry<ResourceLocation, Tag<EntityType<?>>> namedTag : EntityTypeTags.getAllTags().getAllTags().entrySet())
		{
			if(namedTag.getKey().toString().equals(tag))
			{
				for(EntityType<?> element : namedTag.getValue().getValues())
				{
					try
					{
						results.add(element.getRegistryName().toString());
					} catch(Exception e){ /* Failed, don't care */ };
				}
			}
		}

		return results;
	}

	public static Block getBlock(String regKey)
	{
		ResourceLocation resLoc = getResLoc(regKey);
		Block block = ForgeRegistries.BLOCKS.getValue(resLoc);
		return block == null ? Blocks.AIR : block;
	}

	public static Item getItem(String regKey)
	{
		ResourceLocation resLoc = getResLoc(regKey);
		Item item = ForgeRegistries.ITEMS.getValue(resLoc);
		if(item != null && !item.equals(Items.AIR))
			return item;
		else
		{
			Block block = ForgeRegistries.BLOCKS.getValue(resLoc);
			if(block != null)
				return block.asItem();
		}
		return Items.AIR;
	}

	public static Item getItem(ResourceLocation resLoc)
	{
		return getItem(resLoc.toString());
	}

	public static boolean scanBlock(Block block, int radius, Player player)
	{
		Block currBlock;
		BlockPos playerPos = vecToBlock(player.position());
		boolean matched = false;

		for(int x = -radius; x <= radius; x++)
		{
			for(int y = -radius; y <= radius; y++)
			{
				for(int z = -radius; z <= radius; z++)
				{
					currBlock = player.level.getBlockState(new BlockPos(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z)).getBlock();
					if(currBlock.equals(block))
					{
						matched = true;
						break;
					}
				}
			}
		}

		return matched;
	}

/*	public static CompoundNBT getPmmoTag(PlayerEntity player)
	{
		if(player != null)
		{
			CompoundNBT persistTag = player.getPersistentData();
			CompoundNBT pmmoTag = null;

			if(!persistTag.contains(Reference.MOD_ID))			//if Player doesn't have pmmo tag, make it
			{
				pmmoTag = new CompoundNBT();
				persistTag.put(Reference.MOD_ID, pmmoTag);
			}
			else
			{
				pmmoTag = persistTag.getCompound(Reference.MOD_ID);	//if Player has pmmo tag, use it
			}

			return pmmoTag;
		}
		else
			return new CompoundNBT();
	}
*/
/*	public static CompoundNBT getPmmoTagElement(PlayerEntity player, String element)
	{
		if(player != null)
		{
			CompoundNBT pmmoTag = getPmmoTag(player);
			CompoundNBT elementTag = null;

			if(!pmmoTag.contains(element))					//if Player doesn't have element tag, make it
			{
				elementTag = new CompoundNBT();
				pmmoTag.put(element, elementTag);
			}
			else
			{
				elementTag = pmmoTag.getCompound(element);	//if Player has element tag, use it
			}

			return elementTag;
		}
		else
			return new CompoundNBT();
	}
*/
/*	public static CompoundNBT getxpMap(PlayerEntity player)
	{
		return getPmmoTagElement(player, "skills");
	}
*/
/*	public static CompoundNBT getPreferencesTag(PlayerEntity player)
	{
		return getPmmoTagElement(player, "preferences");
	}
*/
/*	public static CompoundNBT getabilitiesMap(PlayerEntity player)
	{
		return getPmmoTagElement(player, "abilities");
	}
*/
	public static double getMaxLevel()
	{
		double serverMaxLevel = Config.getConfig("maxLevel");
		return serverMaxLevel <= 1 ? Config.forgeConfig.maxLevel.get() : serverMaxLevel;
	}

	public static double getXpBoostDurabilityMultiplier(ItemStack itemStack)
	{
		double scale = 1;
		if(itemStack.isDamageableItem())
		{
			double durabilityPercentage = 1 - itemStack.getDamageValue() / (double) itemStack.getMaxDamage();
			double scaleStart = Config.getConfig("scaleXpBoostByDurabilityStart") / 100D;
			double scaleEnd = Math.max(scaleStart, Config.getConfig("scaleXpBoostByDurabilityEnd") / 100D);
			scale = Util.mapCapped(durabilityPercentage, scaleStart, scaleEnd, 0, 1);
		}
		return scale;
	}

	public static Map<String, Double> getStackXpBoosts(ItemStack itemStack, boolean type /*false = worn, true = held*/)
	{
		Item item = itemStack.getItem();
		JType jType = type ? JType.XP_BONUS_HELD : JType.XP_BONUS_WORN;
		String regName = item.getRegistryName().toString();
		Map<String, Double> itemXpMap = PredicateRegistry.predicateExists(item.getRegistryName(), jType) 
				? TooltipSupplier.getTooltipData(item.getRegistryName(), jType, itemStack)
				: JsonConfig.data.get(jType).getOrDefault(regName, new HashMap<>());
		itemXpMap = new HashMap<>(itemXpMap);
		if(Config.getConfig("scaleXpBoostByDurability") != 0)
			multiplyMapAnyDouble(itemXpMap, getXpBoostDurabilityMultiplier(itemStack));
		return itemXpMap;
	}

	public static double getStackXpBoost(Player player, ItemStack itemStack, String skill, boolean type /*false = worn, true = held*/)
	{
		if(itemStack == null || itemStack.isEmpty())
			return 0;

		Item item = itemStack.getItem();
		JType jType = type ? JType.XP_BONUS_HELD : JType.XP_BONUS_WORN;
		double boost = 0;

		String regName = item.getRegistryName().toString();
		Map<String, Double> itemXpMap = PredicateRegistry.predicateExists(item.getRegistryName(), jType) 
				? TooltipSupplier.getTooltipData(item.getRegistryName(), jType, itemStack) 
				: JsonConfig.data.get(jType).getOrDefault(regName, new HashMap<>());

		if(itemXpMap != null && itemXpMap.containsKey(skill))
		{
			if(type || checkReq(player, item.getRegistryName(), JType.REQ_WEAR))
			{
				boost = itemXpMap.get(skill);
				if(Config.getConfig("scaleXpBoostByDurability") != 0 && itemStack.isDamageableItem())
					boost *= getXpBoostDurabilityMultiplier(itemStack);
			}
		}

		return boost;
	}

	public static double getGlobalMultiplier(String skill)
	{
		return JsonConfig.data.get(JType.XP_MULTIPLIER_DIMENSION).getOrDefault("all_dimensions", new HashMap<>()).getOrDefault(skill, 1D);
	}

	public static double getDimensionMultiplier(String skill, Player player)
	{
		try
		{
			String dimensionKey = XP.getDimResLoc(player.level).toString();
			return JsonConfig.data.get(JType.XP_MULTIPLIER_DIMENSION).getOrDefault(dimensionKey, new HashMap<>()).getOrDefault(skill, 1D);
		}
		catch(Exception e)
		{
			return 1D;
		}
	}

	public static double getDifficultyMultiplier(Player player, String skill)
	{
		double difficultyMultiplier = 1;

		if(skill.equals(Skill.COMBAT.toString()) || skill.equals(Skill.ARCHERY.toString()) || skill.equals(Skill.GUNSLINGING.toString()) || skill.equals(Skill.ENDURANCE.toString()))
		{
			switch(player.level.getDifficulty())
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

	public static double getItemBoost(Player player, String skill)
	{
		if(player.getMainHandItem().getItem().getRegistryName() == null)
			return 0;

		double itemBoost = 0;

		skill = skill.toLowerCase();
		Inventory inv = player.getInventory();

		/*if(Curios.isLoaded())
		{
			Collection<ICurioStacksHandler> curiosItems = Curios.getCurios(player).collect(Collectors.toSet());

			for(ICurioStacksHandler value : curiosItems)
			{
				for (int i = 0; i < value.getSlots(); i++)
				{
					itemBoost += getStackXpBoost(player, value.getStacks().getStackInSlot(i), skill, false);
				}
			};
		}*/

		itemBoost += getStackXpBoost(player, player.getMainHandItem(), skill, true);

		if(!inv.getItem(39).isEmpty())	//Helm
			itemBoost += getStackXpBoost(player, inv.getItem(39), skill, false);
		if(!inv.getItem(38).isEmpty())	//Chest
			itemBoost += getStackXpBoost(player, inv.getItem(38), skill, false);
		if(!inv.getItem(37).isEmpty())	//Legs
			itemBoost += getStackXpBoost(player, inv.getItem(37), skill, false);
		if(!inv.getItem(36).isEmpty())	//Boots
			itemBoost += getStackXpBoost(player, inv.getItem(36), skill, false);
		if(!inv.getItem(40).isEmpty())	//Off-Hand
			itemBoost += getStackXpBoost(player, inv.getItem(40), skill, false);

		return itemBoost;
	}

	public static Map<String, Double> getDimensionBoosts(String dimKey)
	{
		return JsonConfig.data.get(JType.XP_BONUS_DIMENSION).getOrDefault(dimKey, new HashMap<>());
	}

	public static double getDimensionBoost(Player player, String skill)
	{
		try
		{
			String dimensionKey = XP.getDimResLoc(player.level).toString();
			return JsonConfig.data.get(JType.XP_BONUS_DIMENSION).getOrDefault(dimensionKey, new HashMap<>()).getOrDefault(skill, 0D);
		}
		catch(NullPointerException e)
		{
			return 0;
		}
	}

	public static double getGlobalBoost(String skill)
	{
		return JsonConfig.data.get(JType.XP_BONUS_DIMENSION).getOrDefault("all_dimensions", new HashMap<>()).getOrDefault(skill, 0D);
	}

	public static CompoundTag writeUniqueId(UUID uuid)
	{
		CompoundTag compoundnbt = new CompoundTag();
		compoundnbt.putLong("M", uuid.getMostSignificantBits());
		compoundnbt.putLong("L", uuid.getLeastSignificantBits());
		return compoundnbt;
	}

	public static Map<String, Double> getBiomeBoosts(Player player)
	{
		Map<String, Double> biomeBoosts = new HashMap<>();
		double biomePenaltyMultiplier = Config.getConfig("biomePenaltyMultiplier");

		Biome biome = player.level.getBiome(vecToBlock(player.position()));
		ResourceLocation resLoc = biome.getRegistryName();
		if(resLoc == null)
			return new HashMap<>();
		String biomeKey = resLoc.toString();
		Map<String, Double> biomeMap = getJsonMap(biomeKey, JType.XP_BONUS_BIOME);

		if(biomeMap != null)
		{
			boolean metReq = checkReq(player, resLoc, JType.REQ_BIOME);
			for(Map.Entry<String, Double> entry : biomeMap.entrySet())
			{
				biomeBoosts.put(entry.getKey(), metReq ? entry.getValue() : Math.min(entry.getValue(), -biomePenaltyMultiplier * 100));
			}
		}

		return biomeBoosts;
	}

	public static double getMultiplier(Player player, String skill)
	{
		double multiplier = Config.forgeConfig.globalMultiplier.get();

		double globalMultiplier = getGlobalMultiplier(skill);
		double dimensionMultiplier = getDimensionMultiplier(skill, player);
		double difficultyMultiplier = getDifficultyMultiplier(player, skill);

		double globalBoost = getGlobalBoost(skill);
		double itemBoost = getItemBoost(player, skill);
		double biomeBoost = getBiomeBoosts(player).getOrDefault(skill, 0D);
		double dimensionBoost = getDimensionBoost(player, skill);
		double playerBoost = PmmoSavedData.get().getPlayerXpBoost(player.getUUID(), skill);

		double additiveMultiplier = 1 + (itemBoost + biomeBoost + dimensionBoost + globalBoost + playerBoost) / 100;

		multiplier *= globalMultiplier;
		multiplier *= dimensionMultiplier;
		multiplier *= difficultyMultiplier;
		multiplier *= additiveMultiplier;
		multiplier *= CheeseTracker.getLazyMultiplier(player.getUUID(), skill);

		return Math.max(0, multiplier);
	}

	public static double getHorizontalDistance(Vec3 p1, Vec3 p2)
	{
		return Math.sqrt(Math.pow((p1.x() - p2.x()), 2) + Math.pow((p1.z() - p2.z()), 2));
	}

	public static void awardXp(ServerPlayer player, String skill, @Nullable String sourceName, double amount, boolean skip, boolean ignoreBonuses, boolean causedByParty)
	{
//		if(!(player instanceof ServerPlayerEntity))
//		{
//			LOGGER.error("NOT ServerPlayerEntity PLAYER XP AWARD ATTEMPTED! THIS SHOULD NOT HAPPEN! SOURCE: " + sourceName + ", SKILL: " + skill + ", AMOUNT: " + amount + ", CLASS: " + player.getClass().getName().toString());
//			return;
//		}

		if(player.level.isClientSide || Double.isNaN(amount) || player instanceof FakePlayer)
			return;

		PmmoSavedData pmmoSavedData = PmmoSavedData.get();
		UUID uuid = player.getUUID();

		XpEvent xpEvent = new XpEvent(player, skill, sourceName, amount, skip, ignoreBonuses, causedByParty);
		if(MinecraftForge.EVENT_BUS.post(xpEvent))
			return;

		skill = xpEvent.getSkill();
		sourceName = xpEvent.getSourceName();
		amount = xpEvent.getAmount();
		skip = xpEvent.isSkip();
		ignoreBonuses = xpEvent.isIgnoreBonuses();
		causedByParty = xpEvent.isCausedByParty();

		if(!ignoreBonuses && !causedByParty)
			amount *= getMultiplier(player, skill);

		String playerName = player.getDisplayName().getString();
		int startLevel = Skill.getLevel(skill, uuid);
		double startXp = Skill.getXp(skill, uuid);
		double maxXp = Config.getConfig("maxXp");

		pmmoSavedData.addXp(skill, uuid, amount);

		if(!causedByParty)
		{
			Party party = pmmoSavedData.getParty(uuid);
			if(party != null)
			{
				Set<ServerPlayer> membersInRange = party.getOnlineMembersInRange(player);
				int membersInRangeSize = membersInRange.size();
				double partyMultiplier = party.getMultiplier(membersInRangeSize);
				amount *= partyMultiplier;
				party.submitXpGained(uuid, amount);
				amount /= membersInRangeSize + 1D;
				for(ServerPlayer partyMember : membersInRange)
				{
					awardXp(partyMember, skill, sourceName, amount, true, ignoreBonuses, true);
				}
			}
		}

		if(amount == 0 /* || startXp >= 2000000000 */)
			return;

//		if(startXp + amount >= 2000000000)
//		{
//			sendMessage(skill + " cap of 2b xp reached, you fucking psycho!", false, player, TextFormatting.LIGHT_PURPLE);
//			LOGGER.info(player.getDisplayName().getString() + " " + skill + " 2b cap reached");
//			amount = 2000000000 - startXp;
//		}

		int currLevel = Skill.getLevel(skill, uuid);

		if(startLevel != currLevel) //Level Up! Or Down?
		{
			AttributeHandler.updateAll(player);
			updateRecipes(player);

//			if(ModList.get().isLoaded("compatskills"))
//			{
//				String commandArgs = "reskillable incrementskill " + playerName + " compatskills." + skill + " 1";
//
//				try
//				{
//					if(!player.level.isClientSide)
//						player.getServer().getCommandManager().getDispatcher().execute(commandArgs, player.getCommandSource().withFeedbackDisabled());
//				}
//				catch(CommandSyntaxException e)
//				{
//					LOGGER.error("PMMO Level Up - compatskills command went wrong! args: " + commandArgs, e);
//				}
//			}

			if(JsonConfig.data.get(JType.LEVEL_UP_COMMAND).get(skill.toLowerCase()) != null)
			{
				Map<String, Double> commandMap = JsonConfig.data.get(JType.LEVEL_UP_COMMAND).get(skill.toLowerCase());

				for(Map.Entry<String, Double> entry : commandMap.entrySet())
				{
					int commandLevel = (int) Math.floor(entry.getValue());
					if(startLevel < commandLevel && currLevel >= commandLevel)
					{
						String command = entry.getKey().replace(">player<", playerName).replace(">level<", "" + commandLevel);
						try
						{
							player.getServer().getCommands().getDispatcher().execute(command, player.getServer().createCommandSourceStack());
							LOGGER.info("Executing command \"" + command + "\"\nTrigger: " + playerName + " level up from " + startLevel + " to " + currLevel + " in " + skill + ", trigger level " + commandLevel);
						}
						catch(CommandSyntaxException e)
						{
							LOGGER.error("Invalid level up command \"" + command + "\"", e);
						}
					}
				}
			}
		}

		NetworkHandler.sendToPlayer(new MessageXp(startXp, skill, amount, skip), player);
		if(!skip && Config.forgeConfig.logXpGainedInDebugLog.get())
			LOGGER.debug(playerName + " +" + amount + "xp in: "  + skill + " for: " + sourceName + " total xp: " + Skill.getXp(skill, uuid));

		if(startXp + amount >= maxXp && startXp < maxXp)
		{
			sendMessage(skill + " max startLevel reached, you psycho!", false, player, ChatFormatting.LIGHT_PURPLE);
			LOGGER.info(playerName + " " + skill + " max startLevel reached");
		}
	}
	
	/**
	 * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#awardXpTrigger(UUID, String, String, boolean, boolean) APIUtils.awardTrigger}
	 */
	@Deprecated
	public static void awardXpTrigger(UUID uuid, String triggerKey, @Nullable String sourceName, boolean skip, boolean ignoreBonuses)
	{
		if(JsonConfig.data.get(JType.XP_VALUE_TRIGGER).containsKey(triggerKey))
		{
			awardXpMap(uuid, JsonConfig.data.get(JType.XP_VALUE_TRIGGER).get(triggerKey), sourceName, skip, ignoreBonuses);
		}
		else
			LOGGER.error("TRIGGER XP AWARD \"" + triggerKey + "\" DOES NOT HAVE ANY VALUES, CANNOT AWARD");
	}

	public static void awardXpMap(UUID uuid, Map<String, Double> map, @Nullable String sourceName, boolean skip, boolean ignoreBonuses)
	{
		for(Map.Entry<String, Double> entry : map.entrySet())
		{
			Skill.addXp(entry.getKey(), uuid, entry.getValue(), sourceName, skip, ignoreBonuses);
		}
	}

	public static void updateRecipes(ServerPlayer player)
	{
		if(Config.forgeConfig.craftReqEnabled.get())
		{
			Collection<Recipe<?>> allRecipes = player.getServer().getRecipeManager().getRecipes();
			Collection<Recipe<?>> removeRecipes = new HashSet<>();
			Collection<Recipe<?>> newRecipes = new HashSet<>();

			for(Recipe<?> recipe : allRecipes)
			{
				if(recipe == null)
					continue;
				if(XP.checkReq(player, recipe.getResultItem().getItem().getRegistryName(), JType.REQ_CRAFT))
					newRecipes.add(recipe);
				else
					removeRecipes.add(recipe);
			}

			player.getRecipeBook().removeRecipes(removeRecipes, player);
			player.getRecipeBook().addRecipes(newRecipes, player);
		}
	}

	public static void scanUnlocks(int level, String skill)
	{

	}

	/*private static int getGap(int a, int b)
	{
		return a - b;
	}*/

	public static int getSkillReqGap(Player player, ResourceLocation res, JType jType)
	{
		Map<String, Double> reqs = getJsonMap(res.toString(), jType);

		if(reqs == null)
			return 0;

		return getSkillReqGap(player, reqs);
	}

	public static int getSkillReqGap(Player player, Map<String, Double> reqs)
	{
		int gap = 0;
		if(!checkReq(player, reqs))
		{
			if(reqs != null)
			{
				gap = (int) Math.floor(reqs.entrySet().stream()
						.map(entry -> (int) Math.floor(entry.getValue()) -  Skill.getLevel(entry.getKey(), player))
						.reduce(0, Math::max));
			}
		}
		return gap;
	}

	public static BlockPos vecToBlock(Vec3 pos)
	{
		return new BlockPos(pos);
	}

	public static Vec3 blockToVec(BlockPos pos)
	{
		return new Vec3(pos.getX(), pos.getY(), pos.getZ());
	}

	public static Vec3 blockToMiddleVec(BlockPos pos)
	{
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	public static void spawnRocket(Level world, BlockPos pos, String skill, @Nullable WorldText explosionText)
	{
		spawnRocket(world, new Vec3(pos.getX(), pos.getY(), pos.getZ()), skill, explosionText);
	}

	public static void spawnRocket(Level world, Vec3 pos, String skill, @Nullable WorldText explosionText)
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag fw = new CompoundTag();
		ListTag explosion = new ListTag();
		CompoundTag l = new CompoundTag();

		int[] colors = new int[1];
		colors[0] = Skill.getSkillColor(skill);
//		int[] fadeColors = {0xff0000, 0x00ff00, 0x0000ff};

		l.putInt("Flicker", 1);
		l.putInt("Trail", 0);
		l.putInt("Type", 1);
		l.put("Colors", new IntArrayTag(colors));
//		l.put("FadeColors", new IntArrayNBT(fadeColors));
		explosion.add(l);

		fw.put("Explosions", explosion);
		fw.putInt("Flight", 0);
		nbt.put("Fireworks", fw);

		ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
		itemStack.setTag(nbt);

		PMMOFireworkEntity fireworkRocketEntity = new PMMOFireworkEntity(world, pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D, itemStack);
		if(explosionText != null)
			fireworkRocketEntity.setExplosionText(explosionText);
		world.addFreshEntity(fireworkRocketEntity);
	}

	public static <T> Map<T, Double> ceilMapAnyDouble(Map<T, Double> input)
	{
		for(Map.Entry<T, Double> entry : input.entrySet())
		{
			input.replace(entry.getKey(), Math.ceil(entry.getValue()));
		}

		return input;
	}

	public static <T> Map<T, Double> multiplyMapAnyDouble(Map<T, Double> input, double multiplier)
	{
		for(Map.Entry<T, Double> entry : input.entrySet())
		{
			input.put(entry.getKey(), entry.getValue() * multiplier);
		}

		return input;
	}

	public static <T> Map<T, Double> mergeMapHighestValue(Map<T, Double> map1, Map<T, Double> map2)
	{
		for(Map.Entry<T, Double> entry : map2.entrySet())
		{
			if(!map1.containsKey(entry.getKey()))
				map1.put(entry.getKey(), entry.getValue());
			else if(map1.get(entry.getKey()) < entry.getValue())
				map1.put(entry.getKey(), entry.getValue());
		}

		return map1;
	}

	public static Map<String, Double> getEnchantUseReq(ResourceLocation resLoc, int enchantLevel)
	{
		Map<String, Double> reqs = new HashMap<>();
		String skill;
		int highestSpecifiedLevel = 0;

		if(JsonConfig.data2.getOrDefault(JType.REQ_USE_ENCHANTMENT, new HashMap<>()).containsKey(resLoc.toString()))
		{
			Map<String, Map<String, Double>> levelMaps = JsonConfig.data2.get(JType.REQ_USE_ENCHANTMENT).get(resLoc.toString());
			List<Integer> levels = new ArrayList<>();

			for(String levelKey : levelMaps.keySet())
			{
				levels.add(Integer.parseInt(levelKey));
			}

			levels.sort(Comparator.comparingInt((a -> a)));

			for(int level : levels)
			{
				if(level > enchantLevel)
					break;
				highestSpecifiedLevel = level;

				Map<String, Double> skillMap = levelMaps.get(String.valueOf(level));
				for(Map.Entry<String, Double> skillElement : skillMap.entrySet())
				{
					skill = skillElement.getKey();

					if(!reqs.containsKey(skill))
						reqs.put(skill, skillElement.getValue());
					else if(reqs.get(skill) < skillElement.getValue())
						reqs.put(skill, skillElement.getValue());
				}
			}
		}

		if(Config.forgeConfig.enchantUseReqAutoScaleEnabled.get() && enchantLevel > highestSpecifiedLevel)
			multiplyMapAnyDouble(reqs, enchantLevel / (double) highestSpecifiedLevel);

		return reqs;
	}

	public static Map<String, Double> getEnchantsUseReq(ItemStack itemStack)
	{
		Map<String, Double> reqs = new HashMap<>();
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);

		for(Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
		{
			mergeMapHighestValue(reqs, getEnchantUseReq(entry.getKey().getRegistryName(), entry.getValue()));
		}

		return reqs;
	}

	public static void applyWornPenalty(Player player, ItemStack itemStack)
	{
		if(Config.getConfig("wearReqEnabled") == 0)
			return;
		ResourceLocation resLoc = itemStack.getItem().getRegistryName();
		Map<String, Double> wearReq = XP.getJsonMap(resLoc, JType.REQ_WEAR);
		String wearReqSkill = Config.forgeConfig.autoGenerateWearReqAsCombat.get() ? Skill.COMBAT.toString() : Skill.ENDURANCE.toString();
		if(!wearReq.containsKey(wearReqSkill) && Config.getConfig("autoGenerateValuesEnabled") != 0 && Config.getConfig("autoGenerateWearReqDynamicallyEnabled") != 0)
			wearReq.put(wearReqSkill, AutoValues.getWearReqFromStack(itemStack));

		if(!checkReq(player, wearReq))
		{
			int gap = getSkillReqGap(player, resLoc, JType.REQ_WEAR);

			if(gap > 9)
				gap = 9;

			player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 75, gap, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 75, gap, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, gap, false, true));

			if(Config.forgeConfig.strictReqWear.get() || EnchantmentHelper.hasBindingCurse(itemStack))
			{
				ItemStack droppedItemStack = itemStack.copy();
				player.drop(droppedItemStack, false, false);
				itemStack.setCount(0);
				player.displayClientMessage(new TranslatableComponent("pmmo.gotTooHotDroppedItem", new TranslatableComponent(droppedItemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), true);
				player.displayClientMessage(new TranslatableComponent("pmmo.gotTooHotDroppedItem", new TranslatableComponent(droppedItemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), false);
			}
			else
				player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToWear", new TranslatableComponent(itemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), true);
		}

		applyEnchantmentUsePenalty(player, itemStack);
	}

	public static void applyEnchantmentUsePenalty(Player player, ItemStack itemStack)
	{
		if(Config.getConfig("enchantUseReqEnabled") == 0)
			return;
		ResourceLocation resLoc = itemStack.getItem().getRegistryName(), enchantResLoc;
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);

		for(Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
		{
			enchantResLoc = entry.getKey().getRegistryName();
			if(!checkReq(player, getEnchantUseReq(enchantResLoc, entry.getValue())))
			{
				int gap = getSkillReqGap(player, resLoc, JType.REQ_USE_ENCHANTMENT);

				if(gap > 9)
					gap = 9;

				player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 75, gap, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 75, gap, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, gap, false, true));

				if(Config.forgeConfig.strictReqUseEnchantment.get() || EnchantmentHelper.hasBindingCurse(itemStack))
				{
					ItemStack droppedItemStack = itemStack.copy();
					player.drop(droppedItemStack, false, false);
					itemStack.setCount(0);
					player.displayClientMessage(new TranslatableComponent("pmmo.gotTooHotDroppedItem", new TranslatableComponent(droppedItemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), true);
					player.displayClientMessage(new TranslatableComponent("pmmo.gotTooHotDroppedItem", new TranslatableComponent(droppedItemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), false);
				}
				else
					player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToUseEnchantment", new TranslatableComponent(entry.getKey().getDescriptionId()), new TranslatableComponent(itemStack.getItem().getDescriptionId())).setStyle(textStyle.get("red")), true);
			}
		}
	}

	public static void checkBiomeLevelReq(Player player)
	{
		Biome biome = player.level.getBiome(vecToBlock(player.position()));
		ResourceLocation resLoc = XP.getBiomeResLoc(player.level, biome);
		if(resLoc == null)
			return;
		String biomeKey = resLoc.toString();
		UUID playerUUID = player.getUUID();
		if(!JsonConfig.data.containsKey(JType.REQ_BIOME))
			return;
		Map<String, Double> biomeReq = JsonConfig.data.get(JType.REQ_BIOME).get(biomeKey);
		Map<String, Map<String, Double>> negativeEffects = JsonConfig.data.get(JType.BIOME_EFFECT_NEGATIVE);
		Map<String, Map<String, Double>> positiveEffects = JsonConfig.data.get(JType.BIOME_EFFECT_POSITIVE);

		if(!lastBiome.containsKey(playerUUID))
			lastBiome.put(playerUUID, "none");

		if(checkReq(player, resLoc, JType.REQ_BIOME))
		{
			if(positiveEffects != null)
			{
				Map<String, Double> positiveEffect = positiveEffects.get(biomeKey);
				if(positiveEffect != null)
				{
					for(Map.Entry<String, Double> entry : positiveEffect.entrySet())
					{
						Potion effect = ForgeRegistries.POTIONS.getValue(XP.getResLoc(entry.getKey()));

						if(effect != null)
						{
							for(MobEffectInstance instance : effect.getEffects())
							{
								player.addEffect(instance);
							}
						}
//							player.addEffect(new MobEffectInstance(effect, 75, (int) Math.floor(entry.getValue()), false, false));
					}
				}
			}
		}
		else if(negativeEffects != null)
		{
			Map<String, Double> negativeEffect = negativeEffects.get(biomeKey);
			if(negativeEffect != null)
			{
				for(Map.Entry<String, Double> entry : negativeEffect.entrySet())
				{
					Potion potion = ForgeRegistries.POTIONS.getValue(XP.getResLoc(entry.getKey()));

					if(potion != null)
					{
						for(MobEffectInstance instance : potion.getEffects())
						{
							player.addEffect(instance);
						}
					}
//						player.addEffect(new MobEffectInstance(, 75, (int) Math.floor(entry.getValue()), false, true));
				}
				if(player.level.isClientSide())
				{
					if(!lastBiome.get(playerUUID).equals(biomeKey))
					{
						player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToSurvive", new TranslatableComponent(getBiomeResLoc(player.level, biome).toString())).setStyle(textStyle.get("red")), true);
						player.displayClientMessage(new TranslatableComponent("pmmo.notSkilledEnoughToSurvive", new TranslatableComponent(getBiomeResLoc(player.level, biome).toString())).setStyle(textStyle.get("red")), false);
						sendPlayerSkillList(player, biomeReq);
					}
				}
			}
		}

		lastBiome.put(playerUUID, biomeKey);
	}

	public static int getTotalLevelFromUUID(UUID uuid)
	{
		return getTotalLevelFromMap(Config.getXpMap(uuid));
	}

	public static <T> int getTotalLevelFromMap(Map<T, Double> input)
	{
		int sum = 0;

		for(double xp : input.values())
		{
			sum += levelAtXp(xp);
		}

		return sum;
	}

	public static <T> int getTotalXpFromMap(Map<T, Double> input)
	{
		int sum = 0;

		for(double xp : input.values())
		{
			sum += xp;
		}

		return sum;
	}

	public static Map<String, Double> getOfflineXpMap(UUID uuid)
	{
		if(!offlineXp.containsKey(uuid))
			offlineXp.put(uuid, new HashMap<>());
		return offlineXp.get(uuid);
	}

	public static void setOfflineXpMaps(Map<UUID, Map<String, Double>> newOfflineXp)
	{
		offlineXp = new HashMap<>(newOfflineXp);
	}

	public static void removeOfflineXpUuid(UUID uuid)
	{
		offlineXp.remove(uuid);
	}

	public static double getOfflineLevelDecimal(String skill, UUID uuid)
	{
		if(skill.equals("totalLevel"))
			return getTotalLevelFromMap(XP.getOfflineXpMap(uuid));
		else
			return levelAtXpDecimal(XP.getOfflineXp(skill, uuid));
	}

	public static int getOfflineLevel(String skill, UUID uuid)
	{
		if(skill.equals("totalLevel"))
			return getTotalLevelFromMap(XP.getOfflineXpMap(uuid));
		else
			return levelAtXp(getOfflineXp(skill, uuid));
	}

	public static double getOfflineXp(String skill, UUID uuid)
	{
		return offlineXp.getOrDefault(uuid, new HashMap<>()).getOrDefault(skill, 0D);
	}

	public static double logBase(double base, double goal)
	{
		return Math.log(goal) / Math.log(base);
	}

	public static int levelAtXp(float xp)
	{
		return levelAtXp((double) xp);
	}
	public static int levelAtXp(double xp)
	{
		boolean useExponentialFormula = Config.getConfig("useExponentialFormula") != 0;
		double baseXp = Config.getConfig("baseXp");
		double exponentialBaseXp = Config.getConfig("exponentialBaseXp");
		double exponentialBase = Config.getConfig("exponentialBase");
		double exponentialRate = Config.getConfig("exponentialRate");
		int maxLevel = (int) Math.floor(XP.getMaxLevel());
		double xpIncreasePerLevel = Config.getConfig("xpIncreasePerLevel");

		double theXp = 0;

		for(int level = 0; ; level++)
		{
			if(xp < theXp || level >= maxLevel)
				return level;

			if(useExponentialFormula)
				theXp += exponentialBaseXp * Math.pow(exponentialBase, exponentialRate * (level));
			else
				theXp += baseXp + level * xpIncreasePerLevel;
		}
	}

	public static float levelAtXpDecimal(float xp)
	{
		return (float) levelAtXpDecimal((double) xp);
	}
	public static double levelAtXpDecimal(double xp)
	{
		int maxLevel = (int) Math.floor(XP.getMaxLevel());

		if(levelAtXp(xp) >= maxLevel)
			return maxLevel;
		int startLevel = levelAtXp(xp);
		double startXp = xpAtLevel(startLevel);
		double goalXp = xpAtLevel(startLevel + 1);

		if(startXp == goalXp)
			return maxLevel;
		else
			return startLevel + ((xp - startXp) / (goalXp - startXp));
	}

	public static double xpAtLevel(int givenLevel)
	{
		return xpAtLevel((double) givenLevel);
	}
	public static double xpAtLevel(float givenLevel)
	{
		return xpAtLevel((double) givenLevel);
	}
	public static double xpAtLevel(double givenLevel)
	{
		boolean useExponentialFormula = Config.getConfig("useExponentialFormula") != 0;

		double baseXp = Config.getConfig("baseXp");
		double exponentialBaseXp = Config.getConfig("exponentialBaseXp");
		double exponentialBase = Config.getConfig("exponentialBase");
		double exponentialRate = Config.getConfig("exponentialRate");

		int maxLevel = (int) Math.floor(XP.getMaxLevel());
		if(givenLevel > maxLevel)
			givenLevel = maxLevel;
		double theXp = 0;

		double xpIncreasePerLevel = Config.getConfig("xpIncreasePerLevel");

		for(int startLevel = 1; startLevel < givenLevel; startLevel++)
		{
			if(useExponentialFormula)
				theXp += exponentialBaseXp * Math.pow(exponentialBase, exponentialRate * (startLevel - 1));
			else
				theXp += baseXp + (startLevel - 1) * xpIncreasePerLevel;
		}

		return theXp;
	}

	public static double xpAtLevelDecimal(double givenLevel)
	{
		double startXp = xpAtLevel(Math.floor(givenLevel));
		double endXp   = xpAtLevel(Math.floor(givenLevel + 1));
		double pos = givenLevel - Math.floor(givenLevel);

		return startXp + ((endXp - startXp) * pos);
	}

	public static boolean isHoldingDebugItemInOffhand(Player player)
	{
		return isItemDebugItem(player.getOffhandItem().getItem());
	}

	public static boolean isItemDebugItem(Item item)
	{
		return Items.MUSIC_DISC_CAT.equals(item);
	}

	public static boolean isNightvisionUnlocked(Player player)
	{
		return Skill.getLevel(Skill.SWIMMING.toString(), player) >= Config.getConfig("nightvisionUnlockLevel");
	}

	public static void addWorldXpDrop(WorldXpDrop xpDrop, ServerPlayer player)
	{
//        System.out.println("xp drop added at " + xpDrop.getPos());
		xpDrop.startXp = xpDrop.startXp * (float) XP.getMultiplier(player, xpDrop.getSkill());
		if(Config.getPreferencesMap(player).getOrDefault("worldXpDropsEnabled", 1D) != 0)
			NetworkHandler.sendToPlayer(new MessageWorldXp(xpDrop), player);
		UUID uuid = player.getUUID();
		for(ServerPlayer otherPlayer : PmmoSavedData.getServer().getPlayerList().getPlayers())
		{
			double distance = Util.getDistance(xpDrop.getPos(), otherPlayer.position());
			if (distance < 64D && !uuid.equals(otherPlayer.getUUID()) && Config.getPreferencesMap(otherPlayer).getOrDefault("showOthersWorldXpDrops", 0D) != 0)
				NetworkHandler.sendToPlayer(new MessageWorldXp(xpDrop), otherPlayer);
		}
	}

	public static void addWorldXpDrop(WorldXpDrop xpDrop, UUID uuid)
	{
		ServerPlayer player = PmmoSavedData.getServer().getPlayerList().getPlayer(uuid);
		if(player != null)
			addWorldXpDrop(xpDrop, player);
	}

	public static void addWorldXpDropOffline(WorldXpDrop xpDrop)
	{
		WorldRenderHandler.addWorldXpDropOffline(xpDrop);
	}

	public static void addWorldText(WorldText worldText, UUID uuid)
	{
		ServerPlayer player = PmmoSavedData.getServer().getPlayerList().getPlayer(uuid);
		if(player != null)
			addWorldText(worldText, player);
	}

	public static void addWorldTextRadius(ResourceLocation dimResLoc, WorldText worldText, double radius)
	{
		worldText.updatePos();
		for(ServerPlayer otherPlayer : PmmoSavedData.getServer().getPlayerList().getPlayers())
		{
			if(dimResLoc == getDimResLoc(otherPlayer.getCommandSenderWorld()))
			{
				double distance = Util.getDistance(worldText.getPos(), otherPlayer.position());
				if (distance < radius)
					NetworkHandler.sendToPlayer(new MessageWorldText(worldText), otherPlayer);
			}
		}
	}

	public static void addWorldText(WorldText worldText, ServerPlayer player)
	{
		worldText.updatePos();
		NetworkHandler.sendToPlayer(new MessageWorldText(worldText), player);
	}

	public static void addWorldTextOffline(WorldText worldText)
	{
		WorldRenderHandler.addWorldTextOffline(worldText);
	}

	public static void sendPlayerSkillList(Player player, Map<String, Double> skills)
	{
		for(Map.Entry<String, Double> entry : skills.entrySet())
		{
			int level = Skill.getLevel(entry.getKey(), player);

			if(level < entry.getValue())
				player.displayClientMessage(new TranslatableComponent("pmmo.levelDisplay", new TranslatableComponent("pmmo." + entry.getKey()), "" + (int) Math.floor(entry.getValue())).setStyle(XP.textStyle.get("red")), false);
			else
				player.displayClientMessage(new TranslatableComponent("pmmo.levelDisplay", new TranslatableComponent("pmmo." + entry.getKey()), "" + (int) Math.floor(entry.getValue())).setStyle(XP.textStyle.get("green")), false);
		}
	}
}