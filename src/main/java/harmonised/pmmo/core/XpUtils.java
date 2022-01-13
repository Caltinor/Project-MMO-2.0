package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.readers.XpValueDataType;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class XpUtils {
	
	private static Map<EventType, Map<ResourceLocation, Map<String, Long>>> xpGainData = new HashMap<>();
	private static Map<XpValueDataType, Map<ResourceLocation, Map<String, Double>>> xpModifierData = new HashMap<>();
	
	//===================XP INTERACTION METHODS=======================================
	public static void setXpDiff(UUID playerID, String skillName, long change) {
		long newValue = PmmoSavedData.get().getXpRaw(playerID, skillName) + change;
		PmmoSavedData.get().setXpRaw(playerID, skillName, newValue);
	}
	
	public static long getPlayerXpRaw(UUID playerID, String skill) {
		return PmmoSavedData.get().getXpRaw(playerID, skill);
	}
	
	public static boolean hasXpGainObjectEntry(EventType eventType, ResourceLocation objectID) {
		if (!xpGainData.containsKey(eventType))
			return false;
		return xpGainData.get(eventType).containsKey(objectID);
	}
	
	public static Map<String, Long> getObjectExperienceMap(EventType EventType, ResourceLocation objectID) {
		return xpGainData.computeIfAbsent(EventType, s -> new HashMap<>()).getOrDefault(objectID, new HashMap<>());
	}
	
	public static void setObjectXpGainMap(EventType eventType, ResourceLocation objectID, Map<String, Long> xpMap) {
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpGainData.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	public static void setObjectXpModifierMap(XpValueDataType XpValueDataType, ResourceLocation objectID, Map<String, Double> xpMap) {
		Preconditions.checkNotNull(XpValueDataType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpModifierData.computeIfAbsent(XpValueDataType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	//====================UTILITY METHODS==============================================
	public static int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	
	public static long getXpGainFromMap(Map<String, Long> sourceMap, UUID playerID) {
		//TODO generate
		return 0l;
	}
	
	public static Map<String, Long> deserializeAwardMap(ListTag nbt) {
		Map<String, Long> map = new HashMap<>();
		if (nbt.getElementType() != Tag.TAG_COMPOUND) {
			MsLoggy.error("An API method passed an invalid award map.  This may not have negative effects on gameplay," + 
							"but may cause the source implementation to behave unexpectedly");
			return map;
		}
		for (int i = 0; i < nbt.size(); i++) {
			map.put(nbt.getCompound(i).getString(Reference.API_MAP_SERIALIZER_KEY)
				   ,nbt.getCompound(i).getLong(Reference.API_MAP_SERIALIZER_VALUE));
		}
		return map;
	}
	
	public static Map<String, Long> applyXpModifiers(Player player, @Nullable Entity targetEntity, Map<String, Long> mapIn) {
		Map<String, Long> mapOut = new HashMap<>();
		Map<String, Double> modifiers = getConsolidatedModifierMap(player, targetEntity);
		for (Map.Entry<String, Long> award : mapIn.entrySet()) {
			if (modifiers.containsKey(award.getKey()))
				mapOut.put(award.getKey(), (long)(award.getValue() * modifiers.get(award.getKey())));
			else
				mapOut.put(award.getKey(), award.getValue());
		}
		return mapOut;
	}
	//====================LOGICAL METHODS==============================================
	
	private static List<Long> levelCache = new ArrayList<>();
	
	public static void computeLevelsForCache() {
		boolean exponential = Config.USE_EXPONENTIAL_FORUMULA.get();
		
		long linearBase = Config.LINEAR_BASE_XP.get();
		double linearPer = Config.LINEAR_PER_LEVEL.get();
		
		int exponentialBase = Config.EXPONENTIAL_BASE_XP.get();
		double exponentialRoot = Config.EXPONENTIAL_POWER_BASE.get();
		double exponentialRate = Config.EXPONENTIAL_LEVEL_MOD.get();
		
		long current = 0;
		for (int i = 1; i <= Config.MAX_LEVEL.get(); i++) {
			current += exponential?
					exponentialBase * Math.pow(exponentialRoot, exponentialRate * (i)) :
					linearBase + (i) * linearPer;
			levelCache.add(current);
		}
	}
	
	private static Map<String, Double> getConsolidatedModifierMap(Player player, @Nullable Entity entity) {
		Map<String, Double> mapOut = new HashMap<>();
		for (XpValueDataType type : XpValueDataType.modifierTypes) {
			Map<String, Double> modifiers = new HashMap<>();
			switch (type) {
			case BONUS_BIOME: {
				ResourceLocation biomeID = player.level.getBiome(player.blockPosition()).getRegistryName();
				modifiers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(biomeID, new HashMap<>());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
				}
				break;
			}
			case BONUS_HELD: {
				ItemStack offhandStack = player.getOffhandItem();
				ItemStack mainhandStack = player.getMainHandItem();
				//TODO get NBT based API data for this
				ResourceLocation offhandID = offhandStack.getItem().getRegistryName();
				modifiers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(offhandID, new HashMap<>());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
				}				
				ResourceLocation mainhandID = mainhandStack.getItem().getRegistryName();				
				modifiers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(mainhandID, new HashMap<>());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
				}				
				break;
			}
			case BONUS_WORN: {
				player.getArmorSlots().forEach((stack) -> {
					//TODO get NBT based API data for this
					ResourceLocation itemID = stack.getItem().getRegistryName();
					Map<String, Double> modifers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(itemID, new HashMap<>());
					for (Map.Entry<String, Double> modMap : modifers.entrySet()) {
						mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
					}
				});
				break;
			}
			case BONUS_DIMENSION: {
				ResourceLocation dimensionID = player.level.dimension().getRegistryName();
				modifiers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(dimensionID, new HashMap<>());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
				}
				break;
			}
			case MULTIPLIER_ENTITY: {
				if (entity == null) break;
				ResourceLocation dimensionID = new ResourceLocation(entity.getEncodeId());
				modifiers = xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(dimensionID, new HashMap<>());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (n, o) -> {return n * o;});
				}
				break;
			}
			default: {}
			}
			
		}
		MsLoggy.info("Consolidated Modifier Map: "+MsLoggy.mapToString(mapOut));
		return mapOut;
	}
}
