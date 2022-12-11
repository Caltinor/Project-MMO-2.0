package harmonised.pmmo.config.codecs;

import java.util.Map;

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
	public Map<String, Long> getXpValues(EventType type, CompoundTag nbt);
	public void setXpValues(EventType type, Map<String, Long> award);
	public Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt);
	public void setBonuses(ModifierDataType type, Map<String, Double> bonuses);
	public Map<String, Integer> getReqs(ReqType type, CompoundTag nbt);
	public void setReqs(ReqType type, Map<String, Integer> reqs);
	public Map<ResourceLocation, Integer> getNegativeEffect();
	public void setNegativeEffects(Map<ResourceLocation, Integer> neg);
	public Map<ResourceLocation, Integer> getPositiveEffect();
	public void setPositiveEffects(Map<ResourceLocation, Integer> pos);
}
