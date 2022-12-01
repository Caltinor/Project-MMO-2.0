package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.util.Functions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record CodecMapLocation (
	Optional<Boolean> override,
	Optional<List<ResourceLocation>> tagValues,
	Optional<ModifierData> bonusMap,
	Optional<Map<ResourceLocation, Integer>> positive,
	Optional<Map<ResourceLocation, Integer>> negative,
	Optional<List<ResourceLocation>> veinBlacklist,
	Optional<Map<String, Integer>> travelReq,
	Optional<Map<ResourceLocation, Map<String, Double>>> mobModifiers) {
	
	public static final Codec<CodecMapLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(CodecMapLocation::override),
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("isTagFor").forGetter(CodecMapLocation::tagValues),
			CodecTypes.MODIFIER_CODEC.optionalFieldOf("bonus").forGetter(CodecMapLocation::bonusMap),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("positive_effect").forGetter(CodecMapLocation::positive),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("negative_effect").forGetter(CodecMapLocation::negative),
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("vein_blacklist").forGetter(CodecMapLocation::veinBlacklist),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("travel_req").forGetter(CodecMapLocation::travelReq),
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.DOUBLE_CODEC).optionalFieldOf("mob_multiplier").forGetter(CodecMapLocation::mobModifiers)
			).apply(instance, CodecMapLocation::new));
	
	public static class Builder {
		boolean override = false;
		List<ResourceLocation> tagValues = new ArrayList<>();
		Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>();
		Map<ResourceLocation, Integer> positive  = new HashMap<>();
		Map<ResourceLocation, Integer> negative  = new HashMap<>();
		List<ResourceLocation> veinBlacklist = new ArrayList<>();
		Map<String, Integer> travelReq = new HashMap<>();
		Map<ResourceLocation, Map<String, Double>> mobModifiers = new HashMap<>();
		private Builder() {}
		public static Builder start() {return new Builder();}
		public Builder override(boolean bool) {this.override = bool; return this;}
		public Builder isTagFor(List<ResourceLocation> tags) {this.tagValues = tags; return this;}
		public Builder bonus(Map<ModifierDataType, Map<String, Double>> bonuses) {this.bonusMap = bonuses; return this;}
		public Builder positive(Map<ResourceLocation, Integer> pos) {this.positive = pos; return this;}
		public Builder negative(Map<ResourceLocation, Integer> neg) {this.negative = neg; return this;}
		public Builder veinBlacklist(List<ResourceLocation> blacklist) {this.veinBlacklist = blacklist; return this;}
		public Builder req(Map<String, Integer> reqs) {this.travelReq = reqs; return this;}
		public Builder mobifiers(Map<ResourceLocation, Map<String, Double>> mobification) {this.mobModifiers = mobification; return this;}
		public LocationMapContainer build() {
			return new LocationMapContainer(override, tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, mobModifiers);
		}
	}
	
	public static record LocationMapContainer (
		boolean override,
		List<ResourceLocation> tagValues,
		Map<ModifierDataType, Map<String, Double>> bonusMap,
		Map<ResourceLocation, Integer> positive,
		Map<ResourceLocation, Integer> negative,
		List<ResourceLocation> veinBlacklist ,
		Map<String, Integer> travelReq,
		Map<ResourceLocation, Map<String, Double>> mobModifiers) {
		
		public LocationMapContainer(CodecMapLocation src) {
			this(src.override().orElse(false),
			src.tagValues().orElse(new ArrayList<>()),
			src.bonusMap().isPresent() ? src.bonusMap().get().obj() : new HashMap<>(),
			src.positive().orElse(new HashMap<>()),
			src.negative().orElse(new HashMap<>()),
			src.veinBlacklist().orElseGet(() -> new ArrayList<>()),
			src.travelReq().orElse(new HashMap<>()),
			src.mobModifiers().orElse(new HashMap<>()));
		}
		public LocationMapContainer() {
			this(false, new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
		}
		
		public static LocationMapContainer combine(LocationMapContainer one, LocationMapContainer two) {
			List<ResourceLocation> tagValues = new ArrayList<>();
			Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>();
			Map<ResourceLocation, Integer> positive = new HashMap<>();
			Map<ResourceLocation, Integer> negative = new HashMap<>();
			List<ResourceLocation> veinBlacklist = new ArrayList<>();
			Map<String, Integer> travelReq = new HashMap<>();
			Map<ResourceLocation, Map<String, Double>> mobModifiers = new HashMap<>();
			
			BiConsumer<LocationMapContainer, LocationMapContainer> bothOrNeither = (o, t) -> {
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
				mobModifiers.putAll(o.mobModifiers());
				t.mobModifiers().forEach((key, value) -> {
					mobModifiers.merge(key, value, (oldV, newV) -> {
						Map<String, Double> mergedMap = new HashMap<>(oldV);
						newV.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
						return mergedMap;
					});
				});	
			};
			Functions.biPermutation(one, two, one.override(), two.override(), 
			(o, t) -> {
				tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
				bonusMap.putAll(o.bonusMap().isEmpty() ? t.bonusMap() : o.bonusMap());
				positive.putAll(o.positive().isEmpty() ? t.positive() : o.positive());
				negative.putAll(o.negative().isEmpty() ? t.negative() : o.negative());
				veinBlacklist.addAll(o.veinBlacklist().isEmpty() ? t.veinBlacklist(): o.veinBlacklist());
				travelReq.putAll(o.travelReq().isEmpty() ? t.travelReq() : o.travelReq());
				mobModifiers.putAll(o.mobModifiers().isEmpty() ? t.mobModifiers() : o.mobModifiers());
			}, 
			bothOrNeither,
			bothOrNeither);
			
			return new LocationMapContainer(one.override() || two.override(), tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, mobModifiers);
		}
		
		public boolean isUnconfigured() {
			return bonusMap.values().stream().allMatch(map -> map.isEmpty())
					&& positive.isEmpty() && negative.isEmpty()
					&& veinBlacklist.isEmpty() && travelReq.isEmpty()
					&& mobModifiers.isEmpty();
		}
		
		public static final Codec<LocationMapContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("override").forGetter(LocationMapContainer::override),
				Codec.list(ResourceLocation.CODEC).fieldOf("isTagFor").forGetter(LocationMapContainer::tagValues),
				Codec.simpleMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).fieldOf("bonus").forGetter(LocationMapContainer::bonusMap),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("positive_effect").forGetter(LocationMapContainer::positive),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("negative_effect").forGetter(LocationMapContainer::negative),
				Codec.list(ResourceLocation.CODEC).fieldOf("vein_blacklist").forGetter(LocationMapContainer::veinBlacklist),
				CodecTypes.INTEGER_CODEC.fieldOf("travel_req").forGetter(LocationMapContainer::travelReq),
				Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf("mob_multiplier").forGetter(LocationMapContainer::mobModifiers)
				).apply(instance, LocationMapContainer::new));
	}
}
