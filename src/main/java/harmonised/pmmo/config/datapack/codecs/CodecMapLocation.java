package harmonised.pmmo.config.datapack.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.readers.XpValueDataType;
import net.minecraft.resources.ResourceLocation;

public class CodecMapLocation {
	private final Optional<CodecTypeModifier> bonusMap;
	private final Optional<Map<ResourceLocation, Integer>> positive;
	private final Optional<Map<ResourceLocation, Integer>> negative;
	private final Optional<List<ResourceLocation>> veinBlacklist;
	private final Optional<Map<String, Integer>> travelReq;
	private final Optional<CodecTypeMobMultiplier> mobModifiers;
	
	public static final Codec<CodecMapLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecTypeModifier.CODEC.optionalFieldOf("bonus").forGetter(CodecMapLocation::getBonusMap),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("positive_effect").forGetter(CodecMapLocation::getPositive),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("negative_effect").forGetter(CodecMapLocation::getPositive),
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("vein_blacklist").forGetter(CodecMapLocation::getVeinBlacklist),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("travel_req").forGetter(CodecMapLocation::getTravelReq),
			CodecTypeMobMultiplier.CODEC.optionalFieldOf("mob_multiplier").forGetter(CodecMapLocation::getMobModifiers)
			).apply(instance, CodecMapLocation::new));
	
	public CodecMapLocation(
			Optional<CodecTypeModifier> bonusMap,
			Optional<Map<ResourceLocation, Integer>> positive,
			Optional<Map<ResourceLocation, Integer>> negative,
			Optional<List<ResourceLocation>> veinBlacklist,
			Optional<Map<String, Integer>> travelReq,
			Optional<CodecTypeMobMultiplier> mobModifiers) {
		this.bonusMap = bonusMap;
		this.positive = positive;
		this.negative = negative;
		this.veinBlacklist = veinBlacklist;
		this.travelReq = travelReq;
		this.mobModifiers = mobModifiers;
	}
	public Optional<CodecTypeModifier> getBonusMap() {return bonusMap;}
	public Optional<Map<ResourceLocation, Integer>> getPositive() {return positive;}
	public Optional<Map<ResourceLocation, Integer>> getNegative() {return negative;}
	public Optional<List<ResourceLocation>> getVeinBlacklist() {return veinBlacklist;}
	public Optional<Map<String, Integer>> getTravelReq() {return travelReq;}
	public Optional<CodecTypeMobMultiplier> getMobModifiers() {return mobModifiers;}
	
	public static class LocationMapContainer {
		public Map<XpValueDataType, Map<String, Double>> bonusMap = new HashMap<>();
		public Map<ResourceLocation, Integer> positive = new HashMap<>();
		public Map<ResourceLocation, Integer> negative = new HashMap<>();
		public List<ResourceLocation> veinBlacklist = new ArrayList<>();
		public Map<String, Integer> travelReq = new HashMap<>();
		public  Map<ResourceLocation, Map<String, Double>> mobModifiers = new HashMap<>();
		
		public LocationMapContainer(CodecMapLocation src) {
			bonusMap = src.getBonusMap().isPresent() ? src.getBonusMap().get().getMap() : new HashMap<>();
			positive = src.getPositive().orElseGet(() -> new HashMap<>());
			negative = src.getNegative().orElseGet(() -> new HashMap<>());
			veinBlacklist = src.getVeinBlacklist().orElseGet(() -> new ArrayList<>());
			travelReq = src.getTravelReq().orElseGet(() -> new HashMap<>());
			mobModifiers = src.getMobModifiers().isPresent() ? src.getMobModifiers().get().getMap() : new HashMap<>();
		}
		public LocationMapContainer() {}
		
		public static LocationMapContainer combine(LocationMapContainer one, LocationMapContainer two) {
			one.bonusMap.putAll(two.bonusMap);
			one.positive.putAll(two.positive);
			one.negative.putAll(two.negative);
			two.veinBlacklist.forEach((rl) -> {
				if (!one.veinBlacklist.contains(rl)) 
					one.veinBlacklist.add(rl);
			});
			one.travelReq.putAll(two.travelReq);
			one.mobModifiers.putAll(two.mobModifiers);
			return one;
		}
	}
}
