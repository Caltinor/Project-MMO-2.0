package harmonised.pmmo.core;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class CoreUtils {

	/**Deserializes an award map provided by Perks.  The provided
	 * {@link net.minecraft.nbt.ListTag ListTag} should have been
	 * constructed using the API helper method to conform to specification.
	 * 
	 * @param nbt a ListTag serialized using {@link harmonised.pmmo.api.APIUtils#serializeAwardMap(Map) APIUtils.serializeAwardMap}
	 * @return a deserialized map
	 */
	public static Map<String, Long> deserializeAwardMap(CompoundTag nbt) {
		return CodecTypes.LONG_CODEC
				.parse(NbtOps.INSTANCE, nbt)
				.resultOrPartial(str -> MsLoggy.ERROR.log(LOG_CODE.API, "Error Deserializing Award Map from API: {}",str))
				.orElse(new HashMap<>());
	}
	
	/**If the configuration to summate maps is true, this function will combine
	 * the values of the two maps and output the final map.  
	 * 
	 * @param ogMap the map discarded if summate is false
	 * @param newMap the map to be used and/or merged
	 * @return an award map
	 */
	public static Map<String, Long> mergeXpMapsWithSummateCondition(Map<String, Long> ogMap, Map<String, Long> newMap) {
		boolean summate = Config.SUMMATED_MAPS.get();
		if (!summate) return newMap;
		for (Map.Entry<String, Long> entry : newMap.entrySet()) {
			ogMap.merge(entry.getKey(), entry.getValue(), (a, b) -> a > b ? a : b);
		}
		return ogMap;
	}
	
	/**Applies the bonuses to the provided award map
	 * 
	 * @param mapIn the original award map
	 * @param modifiers the bonuses
	 */
	public static void applyXpModifiers(Map<String, Long> mapIn, Map<String, Double> modifiers) {
		modifiers.forEach((skill, modifier) -> {
			mapIn.computeIfPresent(skill, (key, xp) -> (long)(xp * modifier));
		});
	}
	
	/**converts any skills in the map which are skill groups
	 * into their respective member skills and merges the values
	 * with the existing map members
	 * 
	 * @param map the base xp map to be modified
	 */
	public static void processSkillGroupXP(Map<String, Long> map) {
		new HashMap<>(map).forEach((skill, level) -> {
			SkillData data = SkillData.Builder.getDefault();
			if ((data = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault())).isSkillGroup()) {
				map.remove(skill);
				data.getGroupXP(level).forEach((member, xp) -> {
					map.merge(member, level, (o, n) -> o + n);
				});																					
			}
		});
	}
}
