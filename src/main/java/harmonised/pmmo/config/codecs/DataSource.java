package harmonised.pmmo.config.codecs;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface DataSource<T> {
	/**Combines the current instance with the supplied instance and
	 * returns a new object.  Neither original object are modified
	 * by this method.
	 * 
	 * @param two the object to be integrated into this one
	 * @return a new combined object
	 */
	public T combine(T two);
	/**Used by data loaders to identify objects that have been 
	 * constructed with no customized values.  This is used by
	 * {@link harmonised.pmmo.config.readers.CoreLoader CoreLoader}
	 * to bypass objects that add no value and would be burdensome
	 * to send as packets to clients.
	 * 
	 * @return if the object contains only empty/default configurations
	 */
	public boolean isUnconfigured();
	
	//======SHARED METHODS=========================
	public default Set<String> getTagValues() {return Set.of();}
	public default Map<String, Long> getXpValues(EventType type, CompoundTag nbt) {
		return new HashMap<>();
	};
	public default void setXpValues(EventType type, Map<String, Long> award) {}
	public default Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt) {
		return new HashMap<>();
	};
	public default void setBonuses(ModifierDataType type, Map<String, Double> bonuses) {}
	public default Map<String, Long> getReqs(ReqType type, CompoundTag nbt) {
		return new HashMap<>();
	};
	public default void setReqs(ReqType type, Map<String, Long> reqs) {}
	public default Map<ResourceLocation, Integer> getNegativeEffect() {
		return new HashMap<>();
	};
	public default void setNegativeEffects(Map<ResourceLocation, Integer> neg) {}
	public default Map<ResourceLocation, Integer> getPositiveEffect() {
		return new HashMap<>();
	};
	public default void setPositiveEffects(Map<ResourceLocation, Integer> pos) {}
	
	//======SHARED CODEC UTILITIES===================
	public static <K, V> HashMap<K,V> clearEmptyValues(Map<K,V> map) {
		HashMap<K, V> outMap = new HashMap<>();
		map.forEach((key, value) -> {
			boolean isEmpty = false;
			if (value instanceof Collection)
				isEmpty = ((Collection<?>)value).isEmpty();
			else if (value instanceof Map)
				isEmpty = ((Map<?,?>)value).isEmpty();
			
			if (!isEmpty)
				outMap.put(key, value);
		});
		return outMap;
	}
}
