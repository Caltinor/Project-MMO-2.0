package harmonised.pmmo.client.utils;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.core.XpUtils;

/**This class serves as a run-time cache of data that the
 * client will utiliize for various rendering tasks.
 * 
 * @author Caltinor
 *
 */
public class DataMirror {
	private static Map<String, Long> mySkills = new HashMap<>();
	
	public static int getSkillLevel(String skill) {
		return XpUtils.getLevelFromXP(mySkills.getOrDefault(skill, 0l));
	}

}
