package harmonised.pmmo.core;

import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Preconditions;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;

public class SkillGates {
	public SkillGates () {}

	private Map<ReqType, Map<ResourceLocation, Map<String, Integer>>> reqData = new HashMap<>();
	private Map<ResourceLocation, Map<Integer, Map<String, Integer>>> enchantmentReqs = new HashMap<>();
	
	//====================REQDATA GETTERS AND SETTERS======================================
	public Map<String, Integer> getObjectSkillMap(ReqType reqType, ResourceLocation objectID) {
		return reqData.computeIfAbsent(reqType, s -> new HashMap<>()).computeIfAbsent(objectID, s -> new HashMap<>());
	}
	
	public Map<String, Integer> getEnchantmentReqs(ResourceLocation enchantID, int enchantLvl) {
		return enchantmentReqs.getOrDefault(enchantID, new HashMap<>()).getOrDefault(enchantLvl, new HashMap<>());
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
	//====================REQDATA UTILITY METHODS======================================
	public boolean doesObjectReqExist(ReqType reqType, ResourceLocation objectID) {
		return reqData.containsKey(reqType) ? reqData.get(reqType).containsKey(objectID) : false;
	}
	
	
}
