package harmonised.pmmo.config.datapack.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ModifierDataType;
import net.minecraft.resources.ResourceLocation;

public class CodecMapObject {
	private final Optional<List<ResourceLocation>> tagValues;
	private final Optional<CodecTypeEvent> xpValuesMap;
	private final Optional<CodecTypeModifier> bonusMap;
	private final Optional<CodecTypeReq> reqMap;
	//private final Map<ReqType, List<LogicEntry>> nbtReqMap;
	//private final Map<ReqType, List<LogicEntry>> nbtXpMap;
	private final Optional<Map<ResourceLocation, CodecTypeSalvage>> salvageMap;
	
	public static final Codec<CodecMapObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("isTagFor").forGetter(CodecMapObject::getTagValues),
			CodecTypeEvent.CODEC.optionalFieldOf("xp_values").forGetter(CodecMapObject::getXpValuesMap),
			CodecTypeModifier.CODEC.optionalFieldOf("bonuses").forGetter(CodecMapObject::getBonusMap),
			CodecTypeReq.CODEC.optionalFieldOf("requirements").forGetter(CodecMapObject::getReqMap),
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypeSalvage.CODEC).optionalFieldOf("salvage").forGetter(CodecMapObject::getSalvageMap)
			).apply(instance, CodecMapObject::new));
	
	public CodecMapObject(
			Optional<List<ResourceLocation>> tagValues,
			Optional<CodecTypeEvent> xpValuesMap, 
			Optional<CodecTypeModifier> bonusMap, 
			Optional<CodecTypeReq> reqMap, 
			//Map<ReqType, JsonObject> nbtReqMap,
			Optional<Map<ResourceLocation, CodecTypeSalvage>> salvageMap) {
		this.tagValues = tagValues;
		this.xpValuesMap = xpValuesMap;
		this.bonusMap = bonusMap;
		this.reqMap = reqMap;
		//this.nbtReqMap = nbtReqMap;
		this.salvageMap = salvageMap;
	}
	public Optional<List<ResourceLocation>> getTagValues() {return tagValues;}
	public Optional<CodecTypeEvent> getXpValuesMap() {return xpValuesMap;}
	public Optional<CodecTypeModifier> getBonusMap() {return bonusMap;}
	public Optional<CodecTypeReq> getReqMap() {return reqMap;}
	//public Map<ReqType, JsonObject> getNBTReqMap() {return nbtReqMap;}
	public Optional<Map<ResourceLocation, CodecTypeSalvage>> getSalvageMap() {return salvageMap;}
	
	public static record ObjectMapContainer(
		List<ResourceLocation> tagValues,
		Map<EventType, Map<String, Long>> xpValues,
		Map<ModifierDataType, Map<String, Double>> modifiers,
		Map<ReqType, Map<String, Integer>> reqs,
		Map<ResourceLocation, CodecTypeSalvage> salvage) {
		
		public ObjectMapContainer(CodecMapObject src) {
			this(src.getTagValues().isPresent() ? src.getTagValues().get() : new ArrayList<>(),
				src.getXpValuesMap().isPresent() ? src.getXpValuesMap().get().getMap() : new HashMap<>(),
				src.getBonusMap().isPresent() ? src.getBonusMap().get().getMap(): new HashMap<>(),
				src.getReqMap().isPresent() ? src.getReqMap().get().getMap() : new HashMap<>(),
				src.getSalvageMap().isPresent() ? src.getSalvageMap().get() : new HashMap<>());
		}
		
		public static ObjectMapContainer combine(ObjectMapContainer one, ObjectMapContainer two) {
			List<ResourceLocation> tagValues = new ArrayList<>(one.tagValues);
			two.tagValues.forEach((rl) -> {
				if (!tagValues.contains(rl))
					tagValues.add(rl);
			});
			Map<EventType, Map<String, Long>> xpValues = new HashMap<>(one.xpValues);
			xpValues.putAll(two.xpValues);
			Map<ModifierDataType, Map<String, Double>> modifiers = new HashMap<>(one.modifiers);
			modifiers.putAll(two.modifiers);
			Map<ReqType, Map<String, Integer>> reqs = new HashMap<>(one.reqs);
			reqs.putAll(two.reqs);
			Map<ResourceLocation, CodecTypeSalvage> salvage = new HashMap<>(one.salvage);
			salvage.putAll(two.salvage);
			return new ObjectMapContainer(tagValues, xpValues, modifiers, reqs, salvage);
		}
		public ObjectMapContainer() {
			this(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		}
		
		public static final Codec<ObjectMapContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.listOf().fieldOf("tagValues").forGetter(ObjectMapContainer::tagValues),
				Codec.unboundedMap(EventType.CODEC, CodecTypeEvent.LONG_CODEC).fieldOf("xpValues").forGetter(ObjectMapContainer::xpValues),
				Codec.unboundedMap(ModifierDataType.CODEC, CodecTypeModifier.DOUBLE_CODEC).fieldOf("modifiers").forGetter(ObjectMapContainer::modifiers),
				Codec.unboundedMap(ReqType.CODEC, CodecTypeReq.INTEGER_CODEC).fieldOf("reqs").forGetter(ObjectMapContainer::reqs),
				Codec.unboundedMap(ResourceLocation.CODEC, CodecTypeSalvage.CODEC).fieldOf("salvage").forGetter(ObjectMapContainer::salvage)
				).apply(instance, ObjectMapContainer::new));
	}
}
