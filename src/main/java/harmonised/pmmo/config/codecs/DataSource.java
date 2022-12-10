package harmonised.pmmo.config.codecs;

import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.nbt.CompoundTag;

public interface DataSource<T> {
	public T combine(T two);
	public boolean isUnconfigured();
	public Map<String, Long> getXpValues(EventType type, CompoundTag nbt);
	public void setXpValues(EventType type, Map<String, Long> award);
	public Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt);
	public void setBonuses(ModifierDataType type, Map<String, Double> bonuses);
	public Map<String, Integer> getReqs(ReqType type, CompoundTag nbt);
	public void setReqs(ReqType type, Map<String, Integer> reqs);
}
