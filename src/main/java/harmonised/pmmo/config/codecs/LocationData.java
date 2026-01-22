package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.Functions;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public record LocationData(
		boolean override,
		Set<String> tagValues,
		Map<ModifierDataType, Map<String, Double>> bonusMap,
		Map<Identifier, Integer> positive,
		Map<Identifier, Integer> negative,
		List<Identifier> veinBlacklist,
		Map<String, Long> travelReq,
		List<MobModifier> globalModifiers,
		Map<Identifier, List<MobModifier>> mobModifiers) implements DataSource<LocationData>{
	public LocationData(boolean override) {this(override, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>());}
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
	public Map<String, Long> getReqs(ReqType type, CompoundTag nbt) {
		return travelReq();
	}
	@Override
	public void setReqs(ReqType type, Map<String, Long> reqs) {
		travelReq().clear();
		travelReq().putAll(reqs);
	}
	@Override
	public Map<Identifier, Integer> getNegativeEffect() {
		return negative();
	}
	@Override
	public void setNegativeEffects(Map<Identifier, Integer> neg) {
		negative().clear();
		negative().putAll(neg);
	}
	@Override
	public Map<Identifier, Integer> getPositiveEffect() {
		return positive();
	}
	@Override
	public void setPositiveEffects(Map<Identifier, Integer> pos) {
		positive().clear();
		positive().putAll(pos);
	}
	@Override
	public Set<String> getTagValues() {return tagValues();}
	
	public static final MapCodec<LocationData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(ld -> Optional.of(ld.override())),
			Codec.list(Codec.STRING).optionalFieldOf("isTagFor").forGetter(ld -> Optional.of(new ArrayList<>(ld.tagValues()))),
			Codec.optionalField("bonus", 
				Codec.simpleMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).codec(), false)
				.forGetter(ld -> Optional.of(ld.bonusMap())),
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("positive_effect").forGetter(ld -> Optional.of(ld.positive())),
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("negative_effect").forGetter(ld -> Optional.of(ld.negative())),
			Codec.list(Identifier.CODEC).optionalFieldOf("vein_blacklist").forGetter(ld -> Optional.of(ld.veinBlacklist())),
			Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("travel_req").forGetter(ld -> Optional.of(ld.travelReq())),
			MobModifier.CODEC.listOf().optionalFieldOf("global_mob_modifier").forGetter(ld -> Optional.of(ld.globalModifiers)),
			Codec.unboundedMap(Identifier.CODEC, MobModifier.CODEC.listOf()).optionalFieldOf("mob_modifier").forGetter(ld -> Optional.of(ld.mobModifiers()))
			).apply(instance, (override, tags, bonus, pos, neg, vein, req, global, mobs) ->
				new LocationData(
						override.orElse(false),
						new HashSet<>(tags.orElse(List.of())),
						DataSource.clearEmptyValues(bonus.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(pos.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(neg.orElse(new HashMap<>())),
						new ArrayList<>(vein.orElse(new ArrayList<>())),
						DataSource.clearEmptyValues(req.orElse(new HashMap<>())),
						new ArrayList<>(global.orElse(new ArrayList<>())),
						DataSource.clearEmptyValues(mobs.orElse(new HashMap<>())))
			));	
	
	@Override
	public LocationData combine(LocationData two) {
		Set<String> tagValues = new HashSet<>();
		Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>();
		Map<Identifier, Integer> positive = new HashMap<>();
		Map<Identifier, Integer> negative = new HashMap<>();
		List<Identifier> veinBlacklist = new ArrayList<>();
		Map<String, Long> travelReq = new HashMap<>();
		List<MobModifier> globalModifiers = new ArrayList<>();
		Map<Identifier, List<MobModifier>> mobModifiers = new HashMap<>();
		
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
			globalModifiers.addAll(o.globalModifiers());
			t.globalModifiers().forEach(modifier -> {if (!globalModifiers.contains(modifier)) globalModifiers.add(modifier);});
			mobModifiers.putAll(o.mobModifiers());
			t.mobModifiers().forEach((key, value) -> mobModifiers.computeIfAbsent(key, id -> new ArrayList<>()).addAll(value));
		};
		Functions.biPermutation(this, two, this.override(), two.override(), 
		(o, t) -> {
			tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
			bonusMap.putAll(o.bonusMap().isEmpty() ? t.bonusMap() : o.bonusMap());
			positive.putAll(o.positive().isEmpty() ? t.positive() : o.positive());
			negative.putAll(o.negative().isEmpty() ? t.negative() : o.negative());
			veinBlacklist.addAll(o.veinBlacklist().isEmpty() ? t.veinBlacklist(): o.veinBlacklist());
			travelReq.putAll(o.travelReq().isEmpty() ? t.travelReq() : o.travelReq());
			globalModifiers.addAll(o.globalModifiers().isEmpty() ? t.globalModifiers() : o.globalModifiers());
			mobModifiers.putAll(o.mobModifiers().isEmpty() ? t.mobModifiers() : o.mobModifiers());
		}, 
		bothOrNeither,
		bothOrNeither);
		
		return new LocationData(this.override() || two.override(), tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, globalModifiers, mobModifiers);
	}

	@Override
	public boolean isUnconfigured() {
		return bonusMap.values().stream().allMatch(map -> map.isEmpty())
				&& positive.isEmpty() && negative.isEmpty()
				&& veinBlacklist.isEmpty() && travelReq.isEmpty()
				&& mobModifiers.isEmpty();
	}

	public static Builder build() {return new Builder();}
	public static class Builder {
		boolean override = false;
		Set<String> tagValues = new HashSet<>();
		Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>();
		Map<Identifier, Integer> positive = new HashMap<>();
		Map<Identifier, Integer> negative = new HashMap<>();
		List<Identifier> veinBlacklist = new ArrayList<>();
		Map<String, Long> travelReq = new HashMap<>();
		List<MobModifier> globalModifiers = new ArrayList<>();
		Map<Identifier, List<MobModifier>> mobModifiers = new HashMap<>();

		protected Builder() {}
		public Builder setOverride(boolean override) {
			this.override = override;
			return this;
		}
		public Builder addTag(String... id) {
			this.tagValues.addAll(Arrays.asList(id));
			return this;
		}
		public Builder addTag(TagKey<?>...id) {
			var ids = Arrays.stream(id).map(key -> "#"+key.location()).toList();
			this.tagValues.addAll(ids);
			return this;
		}
		public Builder addReq(Map<String, Long> req) {
			this.travelReq.putAll(req);
			return this;
		}
		public Builder addNegativeEffect(Holder<MobEffect> id, int level) {
			this.negative.put(id.unwrapKey().get().identifier(), level);
			return this;
		}
		public Builder addPositiveEffect(Holder<MobEffect> id, int level) {
			this.positive.put(id.unwrapKey().get().identifier(), level);
			return this;
		}
		public Builder addBonus(ModifierDataType type, Map<String, Double> bonus) {
			this.bonusMap.put(type, bonus);
			return this;
		}
		public Builder addVeinBlacklist(Identifier... id) {
			this.veinBlacklist.addAll(Arrays.stream(id).toList());
			return this;
		}
		public Builder addGlobalModifier(MobModifier...modifiers) {
			this.globalModifiers.addAll(Arrays.stream(modifiers).toList());
			return this;
		}
		public Builder addMobModifier(Identifier mobId, MobModifier modifier) {
			this.mobModifiers.computeIfAbsent(mobId, id -> new ArrayList<>()).add(modifier);
			return this;
		}
		public LocationData end() {return
			new LocationData(override, tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, globalModifiers, mobModifiers);
		}
	}
}
