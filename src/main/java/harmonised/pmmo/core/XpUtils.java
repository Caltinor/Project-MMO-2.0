package harmonised.pmmo.core;

import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Preconditions;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class XpUtils {
	public XpUtils() {}
	
	private  Map<EventType, Map<ResourceLocation, Map<String, Long>>> xpGainData = new HashMap<>();
	private  Map<ModifierDataType, Map<ResourceLocation, Map<String, Double>>> xpModifierData = new HashMap<>();
	
	public void reset() {
		xpGainData = new HashMap<>();
		xpModifierData = new HashMap<>();
	}
	
	//===================XP INTERACTION METHODS=======================================
	
	public boolean hasXpGainObjectEntry(EventType eventType, ResourceLocation objectID) {
		if (!xpGainData.containsKey(eventType))
			return false;
		return xpGainData.get(eventType).containsKey(objectID);
	}
	
	public Map<String, Long> getObjectExperienceMap(EventType EventType, ResourceLocation objectID) {
		return xpGainData.computeIfAbsent(EventType, s -> new HashMap<>()).getOrDefault(objectID, new HashMap<>());
	}
	
	public void setObjectXpGainMap(EventType eventType, ResourceLocation objectID, Map<String, Long> xpMap) {
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpGainData.computeIfAbsent(eventType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	public void setObjectXpModifierMap(ModifierDataType XpValueDataType, ResourceLocation objectID, Map<String, Double> xpMap) {
		Preconditions.checkNotNull(XpValueDataType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(xpMap);
		xpModifierData.computeIfAbsent(XpValueDataType, s -> new HashMap<>()).put(objectID, xpMap);
	}
	
	public boolean hasModifierObjectEntry(ModifierDataType type, ResourceLocation objectID) {
		if (!xpModifierData.containsKey(type))
			return false;
		return xpModifierData.get(type).containsKey(objectID);
	}
	
	public Map<String, Double> getObjectModifierMap(ModifierDataType type, ResourceLocation objectID) {
		return xpModifierData.computeIfAbsent(type, s -> new HashMap<>()).getOrDefault(objectID, new HashMap<>());
	}
	//====================UTILITY METHODS==============================================
	public Map<String, Long> deserializeAwardMap(ListTag nbt) {
		Map<String, Long> map = new HashMap<>();
		if (nbt.getElementType() != Tag.TAG_COMPOUND) {
			MsLoggy.ERROR.log(LOG_CODE.API, "An API method passed an invalid award map.  This may not have negative effects on gameplay," + 
							"but may cause the source implementation to behave unexpectedly");
			return map;
		}
		for (int i = 0; i < nbt.size(); i++) {
			map.put(nbt.getCompound(i).getString(Reference.API_MAP_SERIALIZER_KEY)
				   ,nbt.getCompound(i).getLong(Reference.API_MAP_SERIALIZER_VALUE));
		}
		return map;
	}	
	
	public Map<String, Long> applyXpModifiers(Player player, Map<String, Long> mapIn) {
		Map<String, Long> mapOut = new HashMap<>();
		Map<String, Double> modifiers = Core.get(player.level).getConsolidatedModifierMap(player);
		for (Map.Entry<String, Long> award : mapIn.entrySet()) {
			if (modifiers.containsKey(award.getKey()))
				mapOut.put(award.getKey(), (long)(award.getValue() * modifiers.get(award.getKey())));
			else
				mapOut.put(award.getKey(), award.getValue());
		}
		return mapOut;
	}
	
	public void sendXpAwardNotifications(ServerPlayer player, String skillName, long amount) {
		//drop XP into world.
		//player.sendMessage(new TranslatableComponent("pmmo."+skillName).append(": "+String.valueOf(amount)), player.getUUID());
	}
	
	public Map<String, Long> mergeXpMapsWithSummateCondition(Map<String, Long> ogMap, Map<String, Long> newMap) {
		boolean summate = Config.SUMMATED_MAPS.get();
		if (!summate) return newMap;
		for (Map.Entry<String, Long> entry : newMap.entrySet()) {
			ogMap.merge(entry.getKey(), entry.getValue(), (a, b) -> a > b ? a : b);
		}
		return ogMap;
	}
}
