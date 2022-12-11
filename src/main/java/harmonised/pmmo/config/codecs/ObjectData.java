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

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.NBTUtils;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record ObjectData(
		boolean override,
		Set<String> tagValues,
		Map<ReqType, Map<String, Integer>> reqs,		
		Map<ReqType, List<LogicEntry>> nbtReqs,
		Map<ResourceLocation, Integer> negativeEffects,
		Map<EventType, Map<String, Long>> xpValues,
		Map<EventType, List<LogicEntry>> nbtXpValues,
		Map<ModifierDataType, Map<String, Double>> bonuses,
		Map<ModifierDataType, List<LogicEntry>> nbtBonuses,
		Map<ResourceLocation, SalvageData> salvage,
		VeinData veinData) implements DataSource<ObjectData>{
	
		public ObjectData() {
			this(false, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 
					new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), VeinData.EMPTY);
		}
		
		@Override
		public Map<String, Long> getXpValues(EventType type, CompoundTag nbt) {
			return nbtXpValues().get(type) == null
					? xpValues().getOrDefault(type, new HashMap<>())
					: NBTUtils.getExperienceAward(nbtXpValues().get(type), nbt);
		}
		@Override
		public void setXpValues(EventType type, Map<String, Long> award) {
			xpValues().put(type, award);
		}
		@Override
		public Map<String, Double> getBonuses(ModifierDataType type, CompoundTag nbt) {
			return nbtBonuses().get(type) == null
					? bonuses().getOrDefault(type, new HashMap<>())
					: NBTUtils.getBonuses(nbtBonuses().get(type), nbt);
			}
		@Override
		public void setBonuses(ModifierDataType type, Map<String, Double> bonuses) {
			bonuses().put(type, bonuses);
		}
		@Override
		public Map<String, Integer> getReqs(ReqType type, CompoundTag nbt) {
			return nbtReqs().get(type) == null
					? reqs().getOrDefault(type, new HashMap<>())
					: NBTUtils.getRequirement(nbtReqs().get(type), nbt);
		}

		@Override
		public void setReqs(ReqType type, Map<String, Integer> reqs) {
			reqs().put(type, reqs);
		}
		@Override
		public Map<ResourceLocation, Integer> getNegativeEffect() {
			return negativeEffects();
		}
		@Override
		public void setNegativeEffects(Map<ResourceLocation, Integer> neg) {
			negativeEffects().clear();
			negativeEffects().putAll(neg);
		}
		@Override
		public Map<ResourceLocation, Integer> getPositiveEffect() {
			return new HashMap<>();
		}
		@Override
		public void setPositiveEffects(Map<ResourceLocation, Integer> pos) {}

		public static final Codec<ObjectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("override").forGetter(od -> Optional.of(od.override())),
				Codec.STRING.listOf().optionalFieldOf("isTagFor").forGetter(od -> Optional.of(new ArrayList<>(od.tagValues))),
				Codec.optionalField("requirements", 
					Codec.simpleMap(ReqType.CODEC, CodecTypes.INTEGER_CODEC, StringRepresentable.keys(ReqType.values())).codec())
					.forGetter(od -> Optional.of(od.reqs())),
				Codec.optionalField("nbt_requirements",
					Codec.simpleMap(ReqType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ReqType.values())).codec())
					.forGetter(od -> Optional.of(od.nbtReqs())),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
					.optionalFieldOf("negative_effect")					
					.forGetter(od -> Optional.of(od.negativeEffects())),
				Codec.optionalField("xp_values",
					Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).codec())
					.forGetter(od -> Optional.of(od.xpValues())),
				Codec.optionalField("nbt_xp_values",
					Codec.simpleMap(EventType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(EventType.values())).codec())
					.forGetter(od -> Optional.of(od.nbtXpValues())),
				Codec.optionalField("bonuses",
					Codec.simpleMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).codec())
					.forGetter(od -> Optional.of(od.bonuses())),
				Codec.optionalField("nbt_bonuses",
					Codec.simpleMap(ModifierDataType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ModifierDataType.values())).codec())
					.forGetter(od -> Optional.of(od.nbtBonuses())),
				Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.SALVAGE_CODEC).optionalFieldOf("salvage").forGetter(od -> Optional.of(od.salvage())),
				VeinData.VEIN_DATA_CODEC.optionalFieldOf(VeinMiningLogic.VEIN_DATA).forGetter(od -> Optional.of(od.veinData()))
				).apply(instance, (override, tags, reqs, nbtreqs, effects, xp, nbtXp, bonus, nbtbonus, salvage, vein) -> 
					new ObjectData(
						override.orElse(false),
						new HashSet<>(tags.orElse(List.of())),
						new HashMap<>(reqs.orElse(new HashMap<>())),
						new HashMap<>(nbtreqs.orElse(new HashMap<>())),
						new HashMap<>(effects.orElse(new HashMap<>())),
						new HashMap<>(xp.orElse(new HashMap<>())),
						new HashMap<>(nbtXp.orElse(new HashMap<>())),
						new HashMap<>(bonus.orElse(new HashMap<>())),
						new HashMap<>(nbtbonus.orElse(new HashMap<>())),
						new HashMap<>(salvage.orElse(new HashMap<>())),
						vein.orElse(VeinData.EMPTY))
				));
		
		@Override
		public ObjectData combine(ObjectData two) {
			Set<String> tagValues = new HashSet<>();
			Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
			Map<ModifierDataType, Map<String, Double>> bonuses = new HashMap<>();
			Map<ReqType, Map<String, Integer>> reqs = new HashMap<>();
			Map<ResourceLocation, Integer> reqEffects = new HashMap<>();
			Map<ResourceLocation, SalvageData> salvage = new HashMap<>();
			VeinData[] combinedVein = {this.veinData()};
			
			BiConsumer<ObjectData, ObjectData> bothOrNeither = (o, t) -> {
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
				bonuses.putAll(o.bonuses());	
				t.bonuses().forEach((event, map) -> {
					bonuses.merge(event, map, (oMap, nMap) -> {
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
				reqEffects.putAll(o.negativeEffects());	
				t.negativeEffects().forEach((skill, level) -> {
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
			Functions.biPermutation(this, two, this.override(), two.override(), (o, t) -> {
				tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
				xpValues.putAll(o.xpValues().isEmpty() ? t.xpValues() : o.xpValues());
				bonuses.putAll(o.bonuses().isEmpty() ? t.bonuses() : o.bonuses());
				reqs.putAll(o.reqs().isEmpty() ? t.reqs() : o.reqs());
				reqEffects.putAll(o.negativeEffects().isEmpty() ? t.negativeEffects() : o.negativeEffects());
				salvage.putAll(o.salvage().isEmpty() ? t.salvage() : o.salvage());
			}, 
			bothOrNeither, 
			bothOrNeither);
			
			return new ObjectData(this.override() || two.override(), tagValues, reqs, two.nbtReqs(), reqEffects, xpValues, two.nbtXpValues(), bonuses, two.nbtBonuses(), salvage, combinedVein[0]);
		}
		
		@Override
		public boolean isUnconfigured() {
			return reqs().values().stream().allMatch(map -> map.isEmpty()) 
					&& nbtReqs().values().stream().allMatch(map -> map.isEmpty()) 
					&& negativeEffects.isEmpty()
					&& xpValues.values().stream().allMatch(map -> map.isEmpty()) 
					&& nbtXpValues.values().stream().allMatch(map -> map.isEmpty())
					&& bonuses.values().stream().allMatch(map -> map.isEmpty()) 
					&& nbtBonuses.values().stream().allMatch(map -> map.isEmpty())
					&& salvage.keySet().stream().allMatch(rl -> rl.equals(new ResourceLocation("item"))) 
					&& veinData.isUnconfigured();
		}
}
