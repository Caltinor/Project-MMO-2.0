package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.ibm.icu.text.Collator;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.resources.ResourceLocation;

public class SkillGates {

	private static Map<ReqType, Map<ResourceLocation, Map<String, Integer>>> reqData = new HashMap<>();
	
	//====================REQDATA GETTERS AND SETTERS======================================
	public static Map<String, Integer> getObjectSkillMap(ReqType reqType, ResourceLocation objectID) {
		return reqData.computeIfAbsent(reqType, s -> new HashMap<>()).computeIfAbsent(objectID, s -> new HashMap<>());
	}
	
	public static int getObjectSkillLevel(ReqType reqType, ResourceLocation objectID, String skillName) {
		return reqData.computeIfAbsent(reqType, s -> new HashMap<>()).computeIfAbsent(objectID, s -> new HashMap<>()).getOrDefault(skillName, 0);
	}
	
	public static void setObjectSkillMap(ReqType reqType, ResourceLocation objectID, Map<String, Integer> skillMap) {
		Preconditions.checkNotNull(reqType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(skillMap);
		reqData.computeIfAbsent(reqType, s -> new HashMap<>()).put(objectID, skillMap);
	}
	
	public static List<String> getUsedSkills() {
		List<String> out = new ArrayList<>();
		for (Map.Entry<ReqType, Map<ResourceLocation, Map<String, Integer>>> entries : reqData.entrySet()) {
			for (Map.Entry<ResourceLocation, Map<String,Integer>> entry : entries.getValue().entrySet()) {
				for (Map.Entry<String, Integer> map : entry.getValue().entrySet()) {
					if (!out.contains(map.getKey()))
						out.add(map.getKey());
				}
			}
		}
		out.sort(Collator.getInstance());
		return out;
	}
	//====================REQDATA UTILITY METHODS======================================
	public static boolean doesObjectReqExist(ReqType reqType, ResourceLocation objectID) {
		return reqData.containsKey(reqType) ? reqData.get(reqType).containsKey(objectID) : false;
	}
	
	public static boolean doesPlayerMeetReq(ReqType reqType, ResourceLocation objectID, UUID playerID) {
		Map<String, Integer> requirements = getObjectSkillMap(reqType, objectID);
		return doesPlayerMeetReq(reqType, objectID, playerID, requirements);	
	}
	
	public static boolean doesPlayerMeetReq(ReqType reqType, ResourceLocation objectID, UUID playerID, Map<String, Integer> requirements) {
		boolean meetsReq = true;
		for (Map.Entry<String, Integer> req : requirements.entrySet()) {
			int skillLevel = XpUtils.getLevelFromXP(PmmoSavedData.get().getXpRaw(playerID, req.getKey()));
			if (req.getValue() > skillLevel)
				return false;
		}
		return meetsReq;
	}
}
