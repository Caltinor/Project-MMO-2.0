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
	public SkillGates () {}

	private Map<ReqType, Map<ResourceLocation, Map<String, Integer>>> reqData = new HashMap<>();
	private Map<ResourceLocation, Map<Integer, Map<String, Integer>>> enchantmentReqs = new HashMap<>();
	
	//====================REQDATA GETTERS AND SETTERS======================================
	public Map<String, Integer> getObjectSkillMap(ReqType reqType, ResourceLocation objectID) {
		//TODO get autoValues if data not present
		return reqData.computeIfAbsent(reqType, s -> new HashMap<>()).computeIfAbsent(objectID, s -> new HashMap<>());
	}
	
	public int getObjectSkillLevel(ReqType reqType, ResourceLocation objectID, String skillName) {
		return reqData.computeIfAbsent(reqType, s -> new HashMap<>()).computeIfAbsent(objectID, s -> new HashMap<>()).getOrDefault(skillName, 0);
	}
	
	public void setObjectSkillMap(ReqType reqType, ResourceLocation objectID, Map<String, Integer> skillMap) {
		Preconditions.checkNotNull(reqType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(skillMap);
		reqData.computeIfAbsent(reqType, s -> new HashMap<>()).put(objectID, skillMap);
	}
	
	public void setEnchantmentReqs(ResourceLocation enchantmentID, Map<Integer, Map<String, Integer>> data) {
		Preconditions.checkNotNull(enchantmentID);
		Preconditions.checkNotNull(data);
		enchantmentReqs.put(enchantmentID, data);
	}
	
	public List<String> getUsedSkills() {
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
	public boolean doesObjectReqExist(ReqType reqType, ResourceLocation objectID) {
		return reqData.containsKey(reqType) ? reqData.get(reqType).containsKey(objectID) : false;
	}
	
	public boolean doesPlayerMeetReq(ReqType reqType, ResourceLocation objectID, UUID playerID) {
		Map<String, Integer> requirements = getObjectSkillMap(reqType, objectID);
		return doesPlayerMeetReq(playerID, requirements);	
	}
	
	public boolean doesPlayerMeetReq(UUID playerID, Map<String, Integer> requirements) {
		boolean meetsReq = true;
		for (Map.Entry<String, Integer> req : requirements.entrySet()) {
			int skillLevel = PmmoSavedData.get().getLevelFromXP(PmmoSavedData.get().getXpRaw(playerID, req.getKey()));
			if (req.getValue() > skillLevel)
				return false;
		}
		return meetsReq;
	}
}
