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
import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.Functions;
import net.minecraft.resources.ResourceLocation;

public record CodecMapObject (
	Optional<Boolean> override,
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
			Codec.BOOL.optionalFieldOf("override").forGetter(CodecMapObject::override),
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
		boolean override,
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
			this(src.override().orElse(false),
				src.tagValues().isPresent() ? src.tagValues().get() : new ArrayList<>(),
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
			List<ResourceLocation> tagValues = new ArrayList<>();
			Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
			Map<ModifierDataType, Map<String, Double>> modifiers = new HashMap<>();
			Map<ReqType, Map<String, Integer>> reqs = new HashMap<>();
			Map<String, Integer> reqEffects = new HashMap<>();
			Map<ResourceLocation, SalvageData> salvage = new HashMap<>();
			VeinData[] combinedVein = {one.veinData()};
			
			BiConsumer<ObjectMapContainer, ObjectMapContainer> bothOrNeither = (o, t) -> {
				tagValues.addAll(o.tagValues());
				t.tagValues.forEach((rl) -> {
					if (!tagValues.contains(rl))
						tagValues.add(rl);
				});			
				xpValues.putAll(o.xpValues());
				t.xpValues().forEach((event, map) -> {
					xpValues.merge(event, map, (oMap, nMap) -> {
						Map<String, Long> mergedMap = new HashMap<>(oMap);
						nMap.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
						return mergedMap;
					});
				});
				modifiers.putAll(o.modifiers());	
				t.modifiers().forEach((event, map) -> {
					modifiers.merge(event, map, (oMap, nMap) -> {
						Map<String, Double> mergedMap = new HashMap<>(oMap);
						nMap.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
						return mergedMap;
					});
				});
				reqs.putAll(o.reqs());	
				t.reqs().forEach((event, map) -> {
					reqs.merge(event, map, (oMap, nMap) -> {
						Map<String, Integer> mergedMap = new HashMap<>(oMap);
						nMap.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
						return mergedMap;
					});
				});
				reqEffects.putAll(o.reqNegativeEffect());	
				t.reqNegativeEffect().forEach((skill, level) -> {
					reqEffects.merge(skill, level, (o1, n1) -> o1 > n1 ? o1 : n1);
				});
				salvage.putAll(o.salvage());
				t.salvage().forEach((rl, data) -> {
					salvage.merge(rl, data, (oD, nD) -> {
						return SalvageData.combine(oD, nD, o.override(), t.override());
					});
				});
				
				combinedVein[0] = combinedVein[0].combineWith(t.veinData());
			};
			Functions.biPermutation(one, two, one.override(), two.override(), (o, t) -> {
				tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
				xpValues.putAll(o.xpValues().isEmpty() ? t.xpValues() : o.xpValues());
				modifiers.putAll(o.modifiers().isEmpty() ? t.modifiers() : o.modifiers());
				reqs.putAll(o.reqs().isEmpty() ? t.reqs() : o.reqs());
				reqEffects.putAll(o.reqNegativeEffect().isEmpty() ? t.reqNegativeEffect() : o.reqNegativeEffect());
				salvage.putAll(o.salvage().isEmpty() ? t.salvage() : o.salvage());
			}, 
			bothOrNeither, 
			bothOrNeither);
			
			return new ObjectMapContainer(one.override() || two.override(), tagValues, xpValues, modifiers, reqs, two.nbtReqs(), reqEffects, two.nbtXpGains(), two.nbtBonuses(), salvage, combinedVein[0]);
		}
		public ObjectMapContainer() {
			this(false, new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new NBTReqData(), new HashMap<>(), new NBTXpGainData(), new NBTBonusData(), new HashMap<>(), VeinData.EMPTY);
		}
		
		public static final Codec<ObjectMapContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("override").forGetter(ObjectMapContainer::override),
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

	public static class Builder {
		boolean override = false;
		List<ResourceLocation> tagValues = new ArrayList<>();
		Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
		Map<ModifierDataType, Map<String, Double>> modifiers = new HashMap<>();
		Map<ReqType, Map<String, Integer>> reqs = new HashMap<>();
		Map<String, Integer> reqNegativeEffect = new HashMap<>();
		Map<ResourceLocation, SalvageData> salvage = new HashMap<>();
		private Builder() {}
		public static Builder start() {return new Builder();}
		public Builder override(boolean bool) {override = bool; return this;}
		public Builder isTagFor(List<ResourceLocation> tags) {tagValues = tags; return this;}
		public Builder xpValues(Map<EventType, Map<String, Long>> xp) {xpValues = xp; return this;}
		public Builder bonus(Map<ModifierDataType, Map<String, Double>> bonuses) {modifiers = bonuses; return this;}
		public Builder reqs(Map<ReqType, Map<String, Integer>> requirements) {reqs = requirements; return this;}
		public Builder penalty(Map<String, Integer> penalty) {reqNegativeEffect = penalty; return this;}
		public Builder salvage(Map<ResourceLocation, SalvageData> scrap) {salvage = scrap; return this;}
		public ObjectMapContainer build() {
			return new ObjectMapContainer(override, tagValues, xpValues, modifiers, reqs, 
					new NBTReqData(), reqNegativeEffect, new NBTXpGainData(), new NBTBonusData(),
					salvage, VeinData.EMPTY);
		}
	}
}
