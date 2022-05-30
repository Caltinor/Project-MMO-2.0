package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import net.minecraft.resources.ResourceLocation;

public record CodecMapObject (
	Optional<List<ResourceLocation>> tagValues,
	Optional<EventData> xpValuesMap,
	Optional<ModifierData> bonusMap,
	Optional<ReqData> reqMap,
	Optional<NBTReqData> nbtReqMap,
	Optional<Map<String, Integer>> reqNegativeEffect,
	Optional<NBTXpGainData> nbtXpMap,
	Optional<NBTBonusData> nbtBonusMap,
	Optional<Map<ResourceLocation, SalvageData>> salvageMap,
	Optional<VeinData> veinData){
	
	public static final Codec<CodecMapObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("isTagFor").forGetter(CodecMapObject::tagValues),
			CodecTypes.EVENT_CODEC.optionalFieldOf("xp_values").forGetter(CodecMapObject::xpValuesMap),
			CodecTypes.MODIFIER_CODEC.optionalFieldOf("bonuses").forGetter(CodecMapObject::bonusMap),
			CodecTypes.REQ_CODEC.optionalFieldOf("requirements").forGetter(CodecMapObject::reqMap),
			CodecTypes.NBT_REQ_CODEC.optionalFieldOf("nbt_requirements").forGetter(CodecMapObject::nbtReqMap),
			CodecTypes.INTEGER_CODEC.optionalFieldOf("negative_effect").forGetter(CodecMapObject::reqNegativeEffect),
			CodecTypes.NBT_XPGAIN_CODEC.optionalFieldOf("nbt_xp_values").forGetter(CodecMapObject::nbtXpMap),
			CodecTypes.NBT_BONUS_CODEC.optionalFieldOf("nbt_bonuses").forGetter(CodecMapObject::nbtBonusMap),
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.SALVAGE_CODEC).optionalFieldOf("salvage").forGetter(CodecMapObject::salvageMap),
			VeinData.VEIN_DATA_CODEC.optionalFieldOf(VeinMiningLogic.VEIN_DATA).forGetter(CodecMapObject::veinData)
			).apply(instance, CodecMapObject::new));
	
	public static record ObjectMapContainer(
		List<ResourceLocation> tagValues,
		Map<EventType, Map<String, Long>> xpValues,
		Map<ModifierDataType, Map<String, Double>> modifiers,
		Map<ReqType, Map<String, Integer>> reqs,
		NBTReqData nbtReqs,
		Map<String, Integer> reqNegativeEffect,
		NBTXpGainData nbtXpGains,
		NBTBonusData nbtBonuses,
		Map<ResourceLocation, SalvageData> salvage,
		VeinData veinData) {
		
		public ObjectMapContainer(CodecMapObject src) {
			this(src.tagValues().isPresent() ? src.tagValues().get() : new ArrayList<>(),
				src.xpValuesMap().isPresent() ? src.xpValuesMap().get().obj() : new HashMap<>(),
				src.bonusMap().isPresent() ? src.bonusMap().get().obj(): new HashMap<>(),
				src.reqMap().isPresent() ? src.reqMap().get().obj() : new HashMap<>(),
				src.nbtReqMap().isPresent() ? src.nbtReqMap().get() : new NBTReqData(),
				src.reqNegativeEffect().isPresent() ? src.reqNegativeEffect().get() : new HashMap<>(),
				src.nbtXpMap().isPresent() ? src.nbtXpMap().get() : new NBTXpGainData(),
				src.nbtBonusMap().isPresent() ? src.nbtBonusMap().get() : new NBTBonusData(),
				src.salvageMap().isPresent() ? src.salvageMap().get() : new HashMap<>(),
				src.veinData().isPresent() ? src.veinData().get() : VeinData.EMPTY);
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
			Map<String, Integer> reqEffects = new HashMap<>(one.reqNegativeEffect());
			reqEffects.putAll(two.reqNegativeEffect());
			
			Map<ResourceLocation, SalvageData> salvage = new HashMap<>(one.salvage);
			salvage.putAll(two.salvage);
			VeinData combinedVein = one.veinData().combineWith(two.veinData());
			return new ObjectMapContainer(tagValues, xpValues, modifiers, reqs, two.nbtReqs(), reqEffects, two.nbtXpGains(), two.nbtBonuses(), salvage, combinedVein);
		}
		public ObjectMapContainer() {
			this(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new NBTReqData(), new HashMap<>(), new NBTXpGainData(), new NBTBonusData(), new HashMap<>(), VeinData.EMPTY);
		}
		
		public static final Codec<ObjectMapContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.listOf().fieldOf("tagValues").forGetter(ObjectMapContainer::tagValues),
				Codec.unboundedMap(EventType.CODEC, CodecTypes.LONG_CODEC).fieldOf("xpValues").forGetter(ObjectMapContainer::xpValues),
				Codec.unboundedMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf("modifiers").forGetter(ObjectMapContainer::modifiers),
				Codec.unboundedMap(ReqType.CODEC, CodecTypes.INTEGER_CODEC).fieldOf("reqs").forGetter(ObjectMapContainer::reqs),
				CodecTypes.NBT_REQ_CODEC.fieldOf("nbt_requirements").forGetter(ObjectMapContainer::nbtReqs),
				CodecTypes.INTEGER_CODEC.fieldOf("req_effects").forGetter(ObjectMapContainer::reqNegativeEffect),
				CodecTypes.NBT_XPGAIN_CODEC.fieldOf("nbt_xp_values").forGetter(ObjectMapContainer::nbtXpGains),
				CodecTypes.NBT_BONUS_CODEC.fieldOf("nbt_bonuses").forGetter(ObjectMapContainer::nbtBonuses),
				Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.SALVAGE_CODEC).fieldOf("salvage").forGetter(ObjectMapContainer::salvage),
				VeinData.VEIN_DATA_CODEC.fieldOf(VeinMiningLogic.VEIN_DATA).forGetter(ObjectMapContainer::veinData)
				).apply(instance, ObjectMapContainer::new));
	}
}
