package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

public class DataConfig {
	private static Map<CoreType, Map<ResourceLocation, Map<String, Integer>>> coreData = new HashMap<>();
	
	public static void setCoreDataMap(CoreType coreType, ResourceLocation objectID, Map<String, Integer> skillMap) {
		Preconditions.checkNotNull(coreType);
		Preconditions.checkNotNull(objectID);
		Preconditions.checkNotNull(skillMap);
		coreData.computeIfAbsent(coreType, s -> new HashMap<>()).put(objectID, skillMap);
	}
	
	//==================UTILITY METHODS==============================
	private static final String COLOR_KEY = "color";
	
	public static int getSkillColor(String skill) {
		return coreData.computeIfAbsent(CoreType.SKILLS,s -> new HashMap<>())
				.computeIfAbsent(new ResourceLocation(Reference.MOD_ID, skill),s -> new HashMap<>())
				.getOrDefault(COLOR_KEY, 16777215);
	}
}
