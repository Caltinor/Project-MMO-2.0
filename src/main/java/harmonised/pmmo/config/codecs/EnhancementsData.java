package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.Functions;
import net.minecraft.nbt.CompoundTag;

public record EnhancementsData(
		boolean override,
		Map<Integer, Map<String, Integer>> skillArray) implements DataSource<EnhancementsData>{
	
	public EnhancementsData() {this(false, new HashMap<>());}
	
	@Override
	public Map<String, Long> getXpValues(EventType type, CompoundTag nbt) {
		return new HashMap<>();
	}
	@Override
	public void setXpValues(EventType type, Map<String, Long> award) {}
	@Override
	public Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt) {
		return new HashMap<>();
		}
	@Override
	public void setBonuses(ModifierDataType type, Map<String, Double> bonuses) {}
	@Override
	public Map<String, Integer> getReqs(ReqType type, CompoundTag nbt) {
		return new HashMap<>();
	}
	@Override
	public void setReqs(ReqType type, Map<String, Integer> reqs) {}
	
	public static final Codec<EnhancementsData> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
			Codec.BOOL.optionalFieldOf("override").forGetter(cme -> Optional.of(cme.override())),
			CodecTypes.INTEGER_CODEC.listOf().xmap(list -> {
				Map<Integer, Map<String, Integer>> dataOut = new HashMap<>();
				for (int i = 0; i < list.size(); i++) {
					dataOut.put(i, list.get(i));
				}
				return dataOut;
			}, map -> {
				List<Map<String, Integer>> dataOut = new ArrayList<>();
				for (int i = 0; i <= map.keySet().stream().max(Integer::compare).get(); i++) {
					dataOut.add(map.getOrDefault(i, new HashMap<>()));
				}
				return dataOut;
			}).fieldOf("levels").forGetter(EnhancementsData::skillArray)
		).apply(instance, (o, map) -> new EnhancementsData(o.orElse(false), new HashMap<>(map))));
	
	@Override
	public EnhancementsData combine(EnhancementsData two) {
		Map<Integer, Map<String, Integer>> skillArray = new HashMap<>();
		
		BiConsumer<EnhancementsData, EnhancementsData> bothOrNeither = (o, t) -> {
			Map<Integer, Map<String, Integer>> combinedMap = new HashMap<>(o.skillArray());
			t.skillArray().forEach((lvl, map) -> {
				combinedMap.merge(lvl, map, (oldMap, newMap) -> {
					newMap.forEach((skill, level) -> {
						oldMap.merge(skill, lvl, (oldValue, newValue) -> oldValue > newValue ? oldValue : newValue);
					});
					return oldMap;
				});
			});
		};
		Functions.biPermutation(this, two, this.override(), two.override(), 
				(o, t) -> {
					skillArray.clear();
					skillArray.putAll(o.skillArray().isEmpty() ? t.skillArray() : o.skillArray());
				}, 
				bothOrNeither, 
				bothOrNeither);
		
		return new EnhancementsData(this.override() || two.override(), skillArray);
	}

	@Override
	public boolean isUnconfigured() {
		return skillArray().isEmpty();
	}


	

	
}
