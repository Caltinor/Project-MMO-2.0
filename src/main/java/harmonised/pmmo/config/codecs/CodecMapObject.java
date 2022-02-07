package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.config.readers.ModifierDataType;
import net.minecraft.resources.ResourceLocation;

public record CodecMapObject (
	Optional<List<ResourceLocation>> tagValues,
	Optional<EventData> xpValuesMap,
	Optional<ModifierData> bonusMap,
	Optional<ReqData> reqMap,
	//Optional<NBTReqData> nbtReqMap;
	//Optional<NBTXpGainData> nbtXpMap;
	Optional<Map<ResourceLocation, SalvageData>> salvageMap){
	
	public static final Codec<CodecMapObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("isTagFor").forGetter(CodecMapObject::tagValues),
			CodecTypes.EVENT_CODEC.optionalFieldOf("xp_values").forGetter(CodecMapObject::xpValuesMap),
			CodecTypes.MODIFIER_CODEC.optionalFieldOf("bonuses").forGetter(CodecMapObject::bonusMap),
			CodecTypes.REQ_CODEC.optionalFieldOf("requirements").forGetter(CodecMapObject::reqMap),
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.SALVAGE_CODEC).optionalFieldOf("salvage").forGetter(CodecMapObject::salvageMap)
			).apply(instance, CodecMapObject::new));
	
	public static record ObjectMapContainer(
		List<ResourceLocation> tagValues,
		Map<EventType, Map<String, Long>> xpValues,
		Map<ModifierDataType, Map<String, Double>> modifiers,
		Map<ReqType, Map<String, Integer>> reqs,
		Map<ResourceLocation, SalvageData> salvage) {
		
		public ObjectMapContainer(CodecMapObject src) {
			this(src.tagValues().isPresent() ? src.tagValues().get() : new ArrayList<>(),
				src.xpValuesMap().isPresent() ? src.xpValuesMap().get().obj() : new HashMap<>(),
				src.bonusMap().isPresent() ? src.bonusMap().get().obj(): new HashMap<>(),
				src.reqMap().isPresent() ? src.reqMap().get().obj() : new HashMap<>(),
				src.salvageMap().isPresent() ? src.salvageMap().get() : new HashMap<>());
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
			Map<ResourceLocation, SalvageData> salvage = new HashMap<>(one.salvage);
			salvage.putAll(two.salvage);
			return new ObjectMapContainer(tagValues, xpValues, modifiers, reqs, salvage);
		}
		public ObjectMapContainer() {
			this(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		}
		
		public static final Codec<ObjectMapContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.listOf().fieldOf("tagValues").forGetter(ObjectMapContainer::tagValues),
				Codec.unboundedMap(EventType.CODEC, CodecTypes.LONG_CODEC).fieldOf("xpValues").forGetter(ObjectMapContainer::xpValues),
				Codec.unboundedMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf("modifiers").forGetter(ObjectMapContainer::modifiers),
				Codec.unboundedMap(ReqType.CODEC, CodecTypes.INTEGER_CODEC).fieldOf("reqs").forGetter(ObjectMapContainer::reqs),
				Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.SALVAGE_CODEC).fieldOf("salvage").forGetter(ObjectMapContainer::salvage)
				).apply(instance, ObjectMapContainer::new));
	}
}
