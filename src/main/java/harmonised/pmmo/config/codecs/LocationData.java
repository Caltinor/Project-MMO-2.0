package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attributes;

public record LocationData(
		boolean override,
		Set<String> tagValues,
		Map<ModifierDataType, Map<String, Double>> bonusMap,
		Map<ResourceLocation, Integer> positive,
		Map<ResourceLocation, Integer> negative,
		List<ResourceLocation> veinBlacklist,
		Map<String, Integer> travelReq,
		List<MobModifier> globalMobModifiers,
		Map<ResourceLocation, List<MobModifier>> mobModifiers) implements DataSource<LocationData>{
	
	public LocationData() {this(
			false, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>());}

	@Override
	public Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt) {
		return bonusMap().getOrDefault(type, new HashMap<>());
	}
	@Override
	public void setBonuses(ModifierDataType type, Map<String, Double> bonuses) {
		bonusMap().put(type, bonuses);
	}
	@Override
	public Map<String, Integer> getReqs(ReqType type, CompoundTag nbt) {
		return travelReq();
	}
	@Override
	public void setReqs(ReqType type, Map<String, Integer> reqs) {
		travelReq().clear();
		travelReq().putAll(reqs);
	}
	@Override
	public Map<ResourceLocation, Integer> getNegativeEffect() {
		return negative();
	}
	@Override
	public void setNegativeEffects(Map<ResourceLocation, Integer> neg) {
		negative().clear();
		negative().putAll(neg);
	}
	@Override
	public Map<ResourceLocation, Integer> getPositiveEffect() {
		return positive();
	}
	@Override
	public void setPositiveEffects(Map<ResourceLocation, Integer> pos) {
		positive().clear();
		positive().putAll(pos);
	}
	@Override
	public Set<String> getTagValues() {return tagValues();}
	
	public static final Codec<LocationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(ld -> Optional.of(ld.override())),
			Codec.list(Codec.STRING).optionalFieldOf("isTagFor").forGetter(ld -> Optional.of(new ArrayList<>(ld.tagValues()))),
			Codec.optionalField("bonus", 
				Codec.simpleMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).codec())
				.forGetter(ld -> Optional.of(ld.bonusMap())),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("positive_effect").forGetter(ld -> Optional.of(ld.positive())),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("negative_effect").forGetter(ld -> Optional.of(ld.negative())),
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("vein_blacklist").forGetter(ld -> Optional.of(ld.veinBlacklist())),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("travel_req").forGetter(ld -> Optional.of(ld.travelReq())),
			Codec.list(MobModifier.CODEC).optionalFieldOf("global_mob_modifiers").forGetter(ld -> Optional.of(ld.globalMobModifiers())),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.list(MobModifier.CODEC)).optionalFieldOf("mob_modifier").forGetter(ld -> Optional.of(ld.mobModifiers()))
			).apply(instance, (override, tags, bonus, pos, neg, vein, req, dimMobScaling, mobs) ->
				new LocationData(
						override.orElse(false),
						new HashSet<>(tags.orElse(List.of())),
						DataSource.clearEmptyValues(bonus.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(pos.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(neg.orElse(new HashMap<>())),
						new ArrayList<>(vein.orElse(new ArrayList<>())),
						DataSource.clearEmptyValues(req.orElse(new HashMap<>())),
						new ArrayList<>(dimMobScaling.orElse(new ArrayList<>())),
						DataSource.clearEmptyValues(mobs.orElse(new HashMap<>())))
			));	
	
	@Override
	public LocationData combine(LocationData two) {
		Set<String> tagValues = new HashSet<>();
		Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>();
		Map<ResourceLocation, Integer> positive = new HashMap<>();
		Map<ResourceLocation, Integer> negative = new HashMap<>();
		List<ResourceLocation> veinBlacklist = new ArrayList<>();
		Map<String, Integer> travelReq = new HashMap<>();
		List<MobModifier> globalMobModifiers = new ArrayList<>();
		Map<ResourceLocation, List<MobModifier>> mobModifiers = new HashMap<>();
		
		BiConsumer<LocationData, LocationData> bothOrNeither = (o, t) -> {
			tagValues.addAll(o.tagValues());
			t.tagValues().forEach((rl) -> {
				if (!tagValues.contains(rl))
					tagValues.add(rl);
			});		
			bonusMap.putAll(o.bonusMap());
			t.bonusMap().forEach((key, value) -> {
				bonusMap.merge(key, value, (oldV, newV) -> {
					Map<String, Double> mergedMap = new HashMap<>(oldV);
					newV.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
					return mergedMap;
				});
			});		
			positive.putAll(o.positive());
			t.positive().forEach((key, value) -> positive.merge(key, value, (o1, n1) -> o1 > n1 ? o1 : n1));			
			negative.putAll(o.negative());
			t.negative().forEach((key, value) -> negative.merge(key, value, (o1, n1) -> o1 > n1 ? o1 : n1));			
			veinBlacklist.addAll(o.veinBlacklist());
			t.veinBlacklist().forEach((rl) -> {
				if (!veinBlacklist.contains(rl)) 
					veinBlacklist.add(rl);
			});		
			travelReq.putAll(o.travelReq());
			t.travelReq().forEach((key, value) -> travelReq.merge(key, value, (o1, n1) -> o1 > n1 ? o1 : n1));
			globalMobModifiers.addAll(o.globalMobModifiers());
			t.globalMobModifiers().forEach((am) -> {
				if (!globalMobModifiers.contains(am))
					globalMobModifiers.add(am);
			});
			mobModifiers.putAll(o.mobModifiers());
			t.mobModifiers().forEach((key, value) -> {
				mobModifiers.merge(key, value, (oldV, newV) -> {
					List<MobModifier> mergedList = new ArrayList<>(oldV);
                    mergedList.addAll(newV);
					return mergedList;
				});
			});	
		};
		Functions.biPermutation(this, two, this.override(), two.override(), 
		(o, t) -> {
			tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
			bonusMap.putAll(o.bonusMap().isEmpty() ? t.bonusMap() : o.bonusMap());
			positive.putAll(o.positive().isEmpty() ? t.positive() : o.positive());
			negative.putAll(o.negative().isEmpty() ? t.negative() : o.negative());
			veinBlacklist.addAll(o.veinBlacklist().isEmpty() ? t.veinBlacklist(): o.veinBlacklist());
			travelReq.putAll(o.travelReq().isEmpty() ? t.travelReq() : o.travelReq());
			globalMobModifiers.addAll(o.globalMobModifiers().isEmpty() ? t.globalMobModifiers() : o.globalMobModifiers());
			mobModifiers.putAll(o.mobModifiers().isEmpty() ? t.mobModifiers() : o.mobModifiers());
		}, 
		bothOrNeither,
		bothOrNeither);
		
		return new LocationData(this.override() || two.override(), tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, globalMobModifiers, mobModifiers);
	}

	@Override
	public boolean isUnconfigured() {
		return bonusMap.values().stream().allMatch(map -> map.isEmpty())
				&& positive.isEmpty() && negative.isEmpty()
				&& veinBlacklist.isEmpty() && travelReq.isEmpty()
				&& globalMobModifiers.isEmpty()	&& mobModifiers.isEmpty();
	}
}
