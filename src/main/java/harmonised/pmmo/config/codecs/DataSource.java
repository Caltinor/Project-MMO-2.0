package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

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
	public default Map<String, Integer> getReqs(ReqType type, CompoundTag nbt) {
		return new HashMap<>();
	};
	public default void setReqs(ReqType type, Map<String, Integer> reqs) {}
	public default Map<ResourceLocation, Integer> getNegativeEffect() {
		return new HashMap<>();
	};
	public default void setNegativeEffects(Map<ResourceLocation, Integer> neg) {}
	public default Map<ResourceLocation, Integer> getPositiveEffect() {
		return new HashMap<>();
	};
	public default void setPositiveEffects(Map<ResourceLocation, Integer> pos) {}
}
