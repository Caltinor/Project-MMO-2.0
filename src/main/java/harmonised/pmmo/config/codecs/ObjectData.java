package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.NBTUtils;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public record ObjectData(
		boolean override,
		Set<String> tagValues,
		Map<ReqType, Map<String, Long>> reqs,
		Map<ReqType, List<LogicEntry>> nbtReqs,
		Map<Identifier, Integer> negativeEffects,
		Map<EventType, Map<String, Long>> xpValues,
		Map<EventType, Map<String, Map<String, Long>>> damageXpValues,
		Map<EventType, Map<String, List<LogicEntry>>> nbtDamageValues,
		Map<EventType, List<LogicEntry>> nbtXpValues,
		Map<ModifierDataType, Map<String, Double>> bonuses,
		Map<ModifierDataType, List<LogicEntry>> nbtBonuses,
		Map<Identifier, SalvageData> salvage,
		VeinData veinData) implements DataSource<ObjectData>{
		public ObjectData(boolean override) {this(override, new HashSet<>(), new HashMap<>(), new HashMap<>(),
			new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new HashMap<>(), new HashMap<>(), VeinData.EMPTY);}
		public ObjectData() {this(false, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new HashMap<>(), VeinData.EMPTY);
		}

		public Map<Identifier, SalvageData> salvage() {
			return salvage.entrySet().stream()
					.filter(entry -> !BuiltInRegistries.ITEM.get(entry.getKey()).equals(Items.AIR))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		@Override
		public Map<String, Long> getXpValues(EventType type, CompoundTag nbt) {
			boolean isDamage = EventType.is(EventType.DAMAGE_TYPES, type);
			if (nbtXpValues().get(type) == null && nbtDamageValues().get(type) == null) {
				return isDamage
					? damageXpValues()
						.getOrDefault(type, new HashMap<>())
						//TODO iterate over the damage values in the xp map and check if the damage type is a tag member
						.getOrDefault(nbt.getString(APIUtils.DAMAGE_TYPE), new HashMap<>())
					: xpValues().getOrDefault(type, new HashMap<>());
			}
			else return isDamage
					? NBTUtils.getExperienceAward(nbtDamageValues()
						.getOrDefault(type, new HashMap<>())
						.getOrDefault(nbt.getString(APIUtils.DAMAGE_TYPE), new ArrayList<>()), nbt)
					: NBTUtils.getExperienceAward(nbtXpValues().getOrDefault(type, new ArrayList<>()), nbt);

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
		public Map<String, Long> getReqs(ReqType type, CompoundTag nbt) {
			return nbtReqs().get(type) == null
					? reqs().getOrDefault(type, new HashMap<>())
					: NBTUtils.getRequirement(nbtReqs().getOrDefault(type, new ArrayList<>()), nbt);
		}
		@Override
		public void setReqs(ReqType type, Map<String, Long> reqs) {
			reqs().put(type, reqs);
		}
		@Override
		public Map<Identifier, Integer> getNegativeEffect() {
			return negativeEffects();
		}
		@Override
		public void setNegativeEffects(Map<Identifier, Integer> neg) {
			negativeEffects().clear();
			negativeEffects().putAll(neg);
		}
		@Override
		public Set<String> getTagValues() {return tagValues();}
		public static final MapCodec<ObjectData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("override").forGetter(od -> Optional.of(od.override())),
				Codec.STRING.listOf().optionalFieldOf("isTagFor").forGetter(od -> Optional.of(new ArrayList<>(od.tagValues))),
				Codec.optionalField("requirements", 
					Codec.simpleMap(ReqType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(ReqType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.reqs())),
				Codec.optionalField("nbt_requirements",
					Codec.simpleMap(ReqType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ReqType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.nbtReqs())),
				Codec.unboundedMap(Identifier.CODEC, Codec.INT)
					.optionalFieldOf("negative_effect")					
					.forGetter(od -> Optional.of(od.negativeEffects())),
				Codec.optionalField("xp_values",
					Codec.simpleMap(EventType.CODEC, CodecTypes.LONG_CODEC, StringRepresentable.keys(EventType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.xpValues())),
				Codec.optionalField("nbt_xp_values",
					Codec.simpleMap(EventType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(EventType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.nbtXpValues())),
				Codec.optionalField("damage_type_xp",
					Codec.simpleMap(EventType.CODEC, Codec.unboundedMap(Codec.STRING, CodecTypes.LONG_CODEC), StringRepresentable.keys(EventType.DAMAGE_TYPES)).codec(), false)
					.forGetter(od -> Optional.of(od.damageXpValues())),
				Codec.optionalField("nbt_damage_type_xp",
					Codec.simpleMap(EventType.CODEC, Codec.unboundedMap(Codec.STRING, LogicEntry.CODEC.listOf()), StringRepresentable.keys(EventType.DAMAGE_TYPES)).codec(), false)
					.forGetter(od -> Optional.of(od.nbtDamageValues())),
				Codec.optionalField("bonuses",
					Codec.simpleMap(ModifierDataType.CODEC, CodecTypes.DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.bonuses())),
				Codec.optionalField("nbt_bonuses",
					Codec.simpleMap(ModifierDataType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ModifierDataType.values())).codec(), false)
					.forGetter(od -> Optional.of(od.nbtBonuses())),
				Codec.unboundedMap(Identifier.CODEC, CodecTypes.SALVAGE_CODEC).optionalFieldOf("salvage").forGetter(od -> Optional.of(od.salvage())),
				VeinData.VEIN_DATA_CODEC.optionalFieldOf(VeinMiningLogic.VEIN_DATA).forGetter(od -> Optional.of(od.veinData()))
				).apply(instance, (override, tags, reqs, nbtreqs, effects, xp, nbtXp, dmg, nbtdmg, bonus, nbtbonus, salvage, vein) ->
					new ObjectData(
						override.orElse(false),
						new HashSet<>(tags.orElse(List.of())),
						DataSource.clearEmptyValues(reqs.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(nbtreqs.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(effects.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(xp.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(dmg.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(nbtdmg.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(nbtXp.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(bonus.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(nbtbonus.orElse(new HashMap<>())),
						DataSource.clearEmptyValues(salvage.orElse(new HashMap<>())),
						vein.orElse(VeinData.EMPTY))
				));
		
		@Override
		public ObjectData combine(ObjectData two) {
			Set<String> tagValues = new HashSet<>();
			Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
			Map<EventType, List<LogicEntry>> nbtXp = new HashMap<>();
			Map<EventType, Map<String, Map<String, Long>>> damageXP = new HashMap<>();
			Map<EventType, Map<String, List<LogicEntry>>> nbtDamageXP = new HashMap<>();
			Map<ModifierDataType, Map<String, Double>> bonuses = new HashMap<>();
			Map<ModifierDataType, List<LogicEntry>> nbtBonus = new HashMap<>();
			Map<ReqType, Map<String, Long>> reqs = new HashMap<>();
			Map<ReqType, List<LogicEntry>> nbtReq = new HashMap<>();
			Map<Identifier, Integer> reqEffects = new HashMap<>();
			Map<Identifier, SalvageData> salvage = new HashMap<>();
			VeinData[] combinedVein = {this.veinData()};
			
			BiConsumer<ObjectData, ObjectData> bothOrNeither = (o, t) -> {
				//combine NBT settings
				nbtXp.putAll(o.nbtXpValues());
				t.nbtXpValues().forEach((event, logic) -> nbtXp.merge(event, logic, (a, b) -> {var list = new ArrayList<>(a); list.addAll(b); return list;}));
				nbtDamageXP.putAll(o.nbtDamageValues());
				t.nbtDamageValues().forEach((event, map) ->
					map.forEach((dmg, logic) ->
						nbtDamageXP.computeIfAbsent(event, e -> new HashMap<>()).merge(dmg, logic, (oLogic, nLogic) ->
							{var list = new ArrayList<>(oLogic); list.addAll(nLogic); return list;})
					)
				);
				nbtBonus.putAll(o.nbtBonuses());
				t.nbtBonuses().forEach((modifier, logic) -> nbtBonus.merge(modifier, logic, (a, b) -> {var list = new ArrayList<>(a); list.addAll(b); return list;}));
				nbtReq.putAll(o.nbtReqs());
				t.nbtReqs().forEach((req, logic) -> nbtReq.merge(req, logic, (a, b) -> {var list = new ArrayList<>(a); list.addAll(b); return list;}));

				//merge all other settings
				tagValues.addAll(o.tagValues());
                tagValues.addAll(t.tagValues());
				xpValues.putAll(o.xpValues());
				t.xpValues().forEach((event, map) -> {
					xpValues.merge(event, map, (oMap, nMap) -> {
						Map<String, Long> mergedMap = new HashMap<>(oMap);
						nMap.forEach((k, v) -> mergedMap.merge(k, v, (o1, n1) -> o1 > n1 ? o1 : n1));
						return mergedMap;
					});
				});
				damageXP.putAll(o.damageXpValues());
				t.damageXpValues().forEach((event, map) -> {
					map.forEach((dmg, xp) -> {
						damageXP.computeIfAbsent(event, e -> new HashMap<>()).merge(dmg, xp, (oMap, nMap) -> {
							Map<String, Long> mergedMap = new HashMap<>(oMap);
							nMap.forEach((k, v) -> mergedMap.merge(k, v, Long::max));
							return mergedMap;
						});
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
						Map<String, Long> mergedMap = new HashMap<>(oMap);
						nMap.forEach((k, v) -> mergedMap.merge(k, v, Long::max));
						return mergedMap;
					});
				});
				reqEffects.putAll(o.negativeEffects());	
				t.negativeEffects().forEach((skill, level) -> {
					reqEffects.merge(skill, level, Integer::max);
				});
				salvage.putAll(o.salvage());
				t.salvage().forEach((rl, data) -> {
					salvage.merge(rl, data, (oD, nD) -> {
						return SalvageData.combine(oD, nD, o.override(), t.override());
					});
				});
				
				combinedVein[0] = combinedVein[0].combine(t.veinData());
			};
			Functions.biPermutation(this, two, this.override(), two.override(), (o, t) -> {
				tagValues.addAll(o.tagValues().isEmpty() ? t.tagValues() : o.tagValues());
				xpValues.putAll(o.xpValues().isEmpty() ? t.xpValues() : o.xpValues());
				nbtXp.putAll(o.nbtXpValues().isEmpty() ? t.nbtXpValues() : o.nbtXpValues());
				damageXP.putAll(o.damageXpValues().isEmpty() ? t.damageXpValues() : o.damageXpValues());
				nbtDamageXP.putAll(o.nbtDamageValues().isEmpty() ? t.nbtDamageValues() : o.nbtDamageValues());
				bonuses.putAll(o.bonuses().isEmpty() ? t.bonuses() : o.bonuses());
				nbtBonus.putAll(o.nbtBonuses().isEmpty() ? t.nbtBonuses() : o.nbtBonuses());
				reqs.putAll(o.reqs().isEmpty() ? t.reqs() : o.reqs());
				nbtReq.putAll(o.nbtReqs().isEmpty() ? t.nbtReqs() : o.nbtReqs());
				reqEffects.putAll(o.negativeEffects().isEmpty() ? t.negativeEffects() : o.negativeEffects());
				salvage.putAll(o.salvage().isEmpty() ? t.salvage() : o.salvage());
				combinedVein[0] = o.veinData().isUnconfigured() ? t.veinData() : o.veinData();
			}, 
			bothOrNeither, 
			bothOrNeither);
			
			return new ObjectData(this.override() || two.override(), tagValues, reqs, nbtReq, reqEffects
					, xpValues, damageXP, nbtDamageXP, nbtXp, bonuses, nbtBonus, salvage, combinedVein[0]);
		}
		
		@Override
		public boolean isUnconfigured() {
			return reqs().values().stream().allMatch(Map::isEmpty)
					&& nbtReqs().values().stream().allMatch(List::isEmpty)
					&& negativeEffects().isEmpty()
					&& xpValues().values().stream().allMatch(Map::isEmpty)
					&& nbtXpValues().values().stream().allMatch(List::isEmpty)
					&& damageXpValues().values().stream().allMatch(Map::isEmpty)
					&& nbtDamageValues().values().stream().allMatch(Map::isEmpty)
					&& bonuses().values().stream().allMatch(Map::isEmpty)
					&& nbtBonuses().values().stream().allMatch(List::isEmpty)
					&& salvage().keySet().stream().allMatch(rl -> rl.equals(Reference.mc("item")))
					&& veinData().isUnconfigured();
		}

		public static Builder build() {return new Builder();}
		public static class Builder {
			boolean override = false;
			Set<String> tagValues = new HashSet<>();
			Map<ReqType, Map<String, Long>> reqs = new HashMap<>();
			Map<ReqType, List<LogicEntry>> nbtReqs = new HashMap<>();
			Map<Identifier, Integer> negativeEffects = new HashMap<>();
			Map<EventType, Map<String, Long>> xpValues = new HashMap<>();
			Map<EventType, Map<String, Map<String, Long>>> damageXpValues = new HashMap<>();
			Map<EventType, Map<String, List<LogicEntry>>> nbtDamageXpValues = new HashMap<>();
			Map<EventType, List<LogicEntry>> nbtXpValues = new HashMap<>();
			Map<ModifierDataType, Map<String, Double>> bonuses = new HashMap<>();
			Map<ModifierDataType, List<LogicEntry>> nbtBonuses = new HashMap<>();
			Map<Identifier, SalvageData> salvage = new HashMap<>();
			public Optional<Integer> chargeCap = Optional.empty();
			public Optional<Double> chargeRate = Optional.empty();
			public Optional<Integer> consumeAmount = Optional.empty();
			public Builder() {}
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
			public Builder addReq(ReqType type, Map<String, Long> req) {
				this.reqs.put(type, req);
				return this;
			}
			public Builder addNBTReq(ReqType type, List<LogicEntry> req) {
				this.nbtReqs.put(type, req);
				return this;
			}
			public Builder addNegativeEffect(Identifier id, int level) {
				this.negativeEffects.put(id, level);
				return this;
			}
			public Builder addXpValues(EventType type, Map<String, Long> awards) {
				this.xpValues.put(type, awards);
				return this;
			}
			public Builder addDamageXp(EventType type, String damageID, Map<String, Long> award) {
				this.damageXpValues.computeIfAbsent(type, t -> new HashMap<>()).put(damageID, award);
				return this;
			}
			public Builder addNbtDamageXp(EventType type, String damageID, List<LogicEntry> logic) {
				this.nbtDamageXpValues.computeIfAbsent(type, t -> new HashMap<>()).put(damageID, logic);
				return this;
			}
			public Builder addNBTXp(EventType type, List<LogicEntry> logic) {
				this.nbtXpValues.put(type, logic);
				return this;
			}
			public Builder addBonus(ModifierDataType type, Map<String, Double> bonus) {
				this.bonuses.put(type, bonus);
				return this;
			}
			public Builder addNBTBonus(ModifierDataType type, List<LogicEntry> bonus) {
				this.nbtBonuses.put(type, bonus);
				return this;
			}
			public Builder addSalvage(Identifier outputID, SalvageData details) {
				this.salvage.put(outputID, details);
				return this;
			}
			public Builder setVeinRate(double rate) {
				this.chargeRate = Optional.of(rate);
				return this;
			}
			public Builder setVeinCap(int cap) {
				this.chargeCap = Optional.of(cap);
				return this;
			}
			public Builder setVeinConsume(int consumed) {
				this.consumeAmount = Optional.of(consumed);
				return this;
			}
			public ObjectData end() {
				return new ObjectData(override, tagValues, reqs, nbtReqs, negativeEffects, xpValues, damageXpValues,
						nbtDamageXpValues, nbtXpValues, bonuses, nbtBonuses, salvage,
						new VeinData(chargeCap, chargeRate, consumeAmount));
			}
		}
}
