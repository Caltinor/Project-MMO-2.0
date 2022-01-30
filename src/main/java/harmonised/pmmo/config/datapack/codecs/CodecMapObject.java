package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.XpValueDataType;
import net.minecraft.resources.ResourceLocation;

public class CodecMapObject {
	private final Optional<CodecTypeEvent> xpValuesMap;
	private final Optional<CodecTypeModifier> bonusMap;
	private final Optional<CodecTypeReq> reqMap;
	//private final Map<ReqType, JsonObject> nbtReqMap;
	private final Optional<Map<ResourceLocation, CodecTypeSalvage>> salvageMap;
	
	public static final Codec<CodecMapObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecTypeEvent.CODEC.optionalFieldOf("xp_values").forGetter(CodecMapObject::getXpValuesMap),
			CodecTypeModifier.CODEC.optionalFieldOf("bonuses").forGetter(CodecMapObject::getBonusMap),
			CodecTypeReq.CODEC.optionalFieldOf("requirements").forGetter(CodecMapObject::getReqMap),
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypeSalvage.CODEC).optionalFieldOf("salvage").forGetter(CodecMapObject::getSalvageMap)
			).apply(instance, CodecMapObject::new));
	
	public CodecMapObject(
			Optional<CodecTypeEvent> xpValuesMap, 
			Optional<CodecTypeModifier> bonusMap, 
			Optional<CodecTypeReq> reqMap, 
			//Map<ReqType, JsonObject> nbtReqMap,
			Optional<Map<ResourceLocation, CodecTypeSalvage>> salvageMap) {
		this.xpValuesMap = xpValuesMap;
		this.bonusMap = bonusMap;
		this.reqMap = reqMap;
		//this.nbtReqMap = nbtReqMap;
		this.salvageMap = salvageMap;
	}
	
	public Optional<CodecTypeEvent> getXpValuesMap() {return xpValuesMap;}
	public Optional<CodecTypeModifier> getBonusMap() {return bonusMap;}
	public Optional<CodecTypeReq> getReqMap() {return reqMap;}
	//public Map<ReqType, JsonObject> getNBTReqMap() {return nbtReqMap;}
	public Optional<Map<ResourceLocation, CodecTypeSalvage>> getSalvageMap() {return salvageMap;}
	
	public static class ObjectMapContainer {
		public Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
		public Map<XpValueDataType, Map<String, Double>> modifiers = new HashMap<>();
		public Map<ReqType, Map<String, Integer>> reqs = new HashMap<>();
		public Map<ResourceLocation, CodecTypeSalvage.SalvageData> salvage = new HashMap<>();
		
		public ObjectMapContainer(CodecMapObject src) {
			xpValues = src.getXpValuesMap().isPresent() ? src.getXpValuesMap().get().getMap() : new HashMap<>();
			modifiers = src.getBonusMap().isPresent() ? src.getBonusMap().get().getMap(): new HashMap<>();
			reqs = src.getReqMap().isPresent() ? src.getReqMap().get().getMap() : new HashMap<>();
			Map<ResourceLocation, CodecTypeSalvage> salvageRaw = src.getSalvageMap().isPresent() ? src.getSalvageMap().get() : new HashMap<>();
			salvageRaw.forEach((rl, cts) -> {salvage.put(rl, new CodecTypeSalvage.SalvageData(cts));});
		}
		public ObjectMapContainer() {}
		
		public static ObjectMapContainer combine(ObjectMapContainer one, ObjectMapContainer two) {
			one.xpValues.putAll(two.xpValues);
			one.modifiers.putAll(two.modifiers);
			one.reqs.putAll(two.reqs);
			one.salvage.putAll(two.salvage);
			return one;
		}
	}
}
