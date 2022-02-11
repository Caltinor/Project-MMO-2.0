package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.GlobalsData;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.LogicEntry.Case;
import harmonised.pmmo.core.nbt.LogicEntry.Criteria;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.core.nbt.PathReader;
import harmonised.pmmo.core.nbt.Result;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;

public class NBTUtils {
	public NBTUtils() {}
	
	private Map<String, String> globalPaths = new HashMap<>();
	private Map<String, String> globalConst = new HashMap<>();
	private Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> itemReqLogic = new HashMap<>();
	private Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> blockReqLogic = new HashMap<>();
	private Map<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> entityReqLogic = new HashMap<>();
	private Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> itemXpGainLogic = new HashMap<>();
	private Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> blockXpGainLogic = new HashMap<>();
	private Map<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> entityXpGainLogic = new HashMap<>();
	private Map<ModifierDataType, LinkedListMultimap<ResourceLocation, LogicEntry>> bonusLogic = new HashMap<>();
	
	//======================SETTERS=================================
	public void setGlobals(GlobalsData data) {
		MsLoggy.info("GLOBAL PATHS: "+MsLoggy.mapToString(data.paths()));
		globalPaths = data.paths();
		MsLoggy.info("GLOBAL CONSTANTS"+MsLoggy.mapToString(data.constants()));
		globalConst = data.constants();
	}
	public void setItemReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		itemReqLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setBlockReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		blockReqLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setEntityReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		entityReqLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setItemXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		itemXpGainLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setBlockXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		blockXpGainLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setEntityXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		entityXpGainLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	public void setBonuses(ModifierDataType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		bonusLogic.computeIfAbsent(type, s -> LinkedListMultimap.create()).putAll(id, logic);
	}
	
	//======================GETTERS=================================
	public Map<String, Integer> getReqMap(ReqType reqType, ItemStack stack) {
		return translateToInt(evaluateEntries(stack.getTag(), itemReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(stack.getItem().getRegistryName())));
	}
	public Map<String, Integer> getReqMap(ReqType reqType, BlockEntity tile) {
		return translateToInt(evaluateEntries(tile.getTileData(), blockReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(tile.getBlockState().getBlock().getRegistryName())));
	}
	public Map<String, Integer> getReqMap(ReqType reqType, Entity entity) {
		//TODO verify this NBT getter is correct for our purposes
		return translateToInt(evaluateEntries(entity.getPersistentData(), entityReqLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(new ResourceLocation(entity.getEncodeId()))));
	}
	public Map<String, Long> getXpMap(EventType reqType, ItemStack stack) {
		return translateToLong(evaluateEntries(stack.getTag(), itemXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(stack.getItem().getRegistryName())));
	}
	public Map<String, Long> getXpMap(EventType reqType, BlockEntity tile) {
		return translateToLong(evaluateEntries(tile.getTileData(), blockXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(tile.getBlockState().getBlock().getRegistryName())));
	}
	public Map<String, Long> getXpMap(EventType reqType, Entity entity) {
		//TODO verify this NBT getter is correct for our purposes
		return translateToLong(evaluateEntries(entity.getPersistentData(), entityXpGainLogic.getOrDefault(reqType, LinkedListMultimap.create()).get(new ResourceLocation(entity.getEncodeId()))));
	}
	public Map<String, Double> getBonusMap(ModifierDataType type, ItemStack stack) {
		return evaluateEntries(stack.getTag(), bonusLogic.getOrDefault(type, LinkedListMultimap.create()).get(stack.getItem().getRegistryName()));
	}
	
	//======================INTERNAL GETTERS========================
	private String getActualPath(String key) {
		return key.contains("#") ? globalPaths.getOrDefault(key.replace("#", ""), "") : key;
	}
	private String getActualConstant(String key) {
		return key.contains("#") ? globalConst.getOrDefault(key.replace("#", ""), "") : key;
	}
	
	//======================LOGICAL METHODS=========================
	private record LogicTier (BehaviorToPrevious behavior, boolean isSummative, List<Result> results) {}
	
	private Map<String, Double> evaluateEntries(CompoundTag nbt, List<LogicEntry> logic) {
		Map<String, Double> output = new HashMap<>();
		//cancels the evaluation if NBT has no data or never existed
		if (nbt.isEmpty() || nbt == null) return output;
		//this section cycles through the logic and generates usable result objects
		List<LogicTier> logicSequence = new ArrayList<>();
		for (int i = 0; i < logic.size(); i++) {
			LogicEntry entry = logic.get(i);
			logicSequence.add(new LogicTier(entry.behavior(), entry.addCases(), processCases(entry.cases(), nbt)));
		}
		//This section iterates through the logical tiers and processes the summative attribute
		List<Map<String, Double>> interMap = new ArrayList<>();
		for (int i = 0; i < logicSequence.size(); i++) {
			Map<String, Double> combinedMap = new HashMap<>();
			List<Result> data = logicSequence.get(i).results;
			boolean isSummative = logicSequence.get(i).isSummative;
			for (Result r : data) {
				if (r == null) continue;
				if (!r.compares()) continue;
				Map<String, Double> value = r.values();					
				for (Map.Entry<String, Double> val : value.entrySet()) {
					combinedMap.merge(val.getKey(), val.getValue(), (in1, in2) -> {
						return isSummative ? (in1 + in2)
								: (in1 > in2 ? in1 : in2);});
				}
			}
			interMap.add(combinedMap);
		}
		//this section iterates through the logical tiers and processes the relational attribute
		for (int i = 0; i < logicSequence.size(); i++) {
			switch (logicSequence.get(i).behavior()) {
			case SUB_FROM: {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					if (output.getOrDefault(value.getKey(), 0d) - value.getValue() <= 0) output.remove(value.getKey());
					else 
						output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue - newValue);
				}
				break;
			}
			case HIGHEST: {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue > newValue ? oldValue : newValue);
				}
				break;
			}
			case REPLACE: {
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.put(value.getKey(), value.getValue());
				}
				break;
			}
			case ADD_TO: default:{
				for (Map.Entry<String, Double> value : interMap.get(i).entrySet()) {
					output.merge(value.getKey(), value.getValue(), (oldValue, newValue) -> oldValue + newValue);
				}
				break;
			}
			}
		}
		return output;
	}
	
	private List<Result> processCases(List<Case> cases, CompoundTag nbt) {
		List<Result> results = new ArrayList<>();
		for (int i = 0; i < cases.size(); i++) {
			Case caseIterant = cases.get(i);
			List<String> paths = caseIterant.paths();
			List<Criteria> criteria = caseIterant.criteria();
			for (int p = 0; p < paths.size(); p++) {
				for (int c = 0; c < criteria.size(); c++) {
					Criteria critObj = criteria.get(c);
					Map<String, Double> values = critObj.skillMap();
					Operator operator = critObj.operator();					
					List<String> comparison = PathReader.getNBTValues(getActualPath(paths.get(p)), nbt);
					for (int j = 0; j < comparison.size(); j++) {
						List<String> comparators = new ArrayList<>();
						if (!operator.equals(Operator.EXISTS)) {
							comparators = critObj.comparators().orElseGet(() -> new ArrayList<>());
							for (int l = 0; l < comparators.size(); l++) {
								String comparator = getActualConstant(comparators.get(l));
								results.add(new Result(operator, comparator, comparison.get(j), values));
							}
						}
						else results.add(new Result(operator, "", comparison.get(j), values));
					}
					
				}
			}
		}
		return results;
	}
	
	private Map<String, Long> translateToLong(Map<String, Double> src) {
		Map<String, Long> output = new HashMap<>();
		src.forEach((k, v) -> output.put(k, v.longValue()));
		return output;
	}
	private Map<String, Integer> translateToInt(Map<String, Double> src) {
		Map<String, Integer> output = new HashMap<>();
		src.forEach((k, v) -> output.put(k, v.intValue()));
		return output;
	}
	
	//======================REGISTER DEFAULTS======================
	public void registerNBT(LogicalSide side) {
		Core core = Core.get(side);
		
		//==============REGISTER REQUIREMENT LOGIC=============================== 
		for (Map.Entry<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> entry : itemReqLogic.entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(ReqType.BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				BiPredicate<Player, ItemStack> pred = (player, stack) -> core.getSkillGates().doesPlayerMeetReq(player.getUUID(), getReqMap(entry.getKey(), stack));
				core.getPredicateRegistry().registerPredicate(rl, entry.getKey(), pred);
				Function<ItemStack, Map<String, Integer>> func = (stack) -> getReqMap(entry.getKey(), stack);
				core.getTooltipRegistry().registerItemRequirementTooltipData(rl, entry.getKey(), func);
			});			
		}
		blockReqLogic.getOrDefault(ReqType.BREAK, LinkedListMultimap.create()).forEach((rl, logic) -> {
			BiPredicate<Player, BlockEntity> pred = (player, tile) -> core.getSkillGates().doesPlayerMeetReq(player.getUUID(), getReqMap(ReqType.BREAK, tile));
			core.getPredicateRegistry().registerBreakPredicate(rl, ReqType.BREAK, pred);
			Function<BlockEntity, Map<String, Integer>> func = (tile) -> getReqMap(ReqType.BREAK, tile);
			core.getTooltipRegistry().registerBlockRequirementTooltipData(rl, ReqType.BREAK, func);
		});
		for (Map.Entry<ReqType, LinkedListMultimap<ResourceLocation, LogicEntry>> entry : entityReqLogic.entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(ReqType.BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				BiPredicate<Player, Entity> pred = (player, entity) -> core.getSkillGates().doesPlayerMeetReq(player.getUUID(), getReqMap(entry.getKey(), entity));
				core.getPredicateRegistry().registerEntityPredicate(rl, entry.getKey(), pred);
				Function<Entity, Map<String, Integer>> func = (entity) -> getReqMap(entry.getKey(), entity);
				core.getTooltipRegistry().registerEntityRequirementTooltipData(rl, entry.getKey(), func);
			});
		}
		
		//==============REGISTER XP GAIN LOGIC=====================================
		for (Map.Entry<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> entry : itemXpGainLogic.entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(EventType.BLOCK_BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				Function<ItemStack, Map<String, Long>> func = (stack) -> getXpMap(entry.getKey(), stack);
				core.getTooltipRegistry().registerItemXpGainTooltipData(rl, entry.getKey(), func);
			});
		}
		blockXpGainLogic.getOrDefault(ReqType.BREAK, LinkedListMultimap.create()).forEach((rl, logic) -> {
			Function<BlockEntity, Map<String, Long>> func = (tile) -> getXpMap(EventType.BLOCK_BREAK, tile);
			core.getTooltipRegistry().registerBlockXpGainTooltipData(rl, EventType.BLOCK_BREAK, func);
		});
		for (Map.Entry<EventType, LinkedListMultimap<ResourceLocation, LogicEntry>> entry : entityXpGainLogic.entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(EventType.BLOCK_BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				Function<Entity, Map<String, Long>> func = (entity) -> getXpMap(entry.getKey(), entity);
				core.getTooltipRegistry().registerEntityXpGainTooltipData(rl, entry.getKey(), func);
			});
		}
		
		//==============REGISTER BONUSES LOGIC=====================================
		for (Map.Entry<ModifierDataType, LinkedListMultimap<ResourceLocation, LogicEntry>> entry : bonusLogic.entrySet()) {
			entry.getValue().forEach((rl, logic) -> {
				Function<ItemStack, Map<String, Double>> func = (stack) -> getBonusMap(entry.getKey(), stack);
				core.getTooltipRegistry().registerItemBonusTooltipData(rl, entry.getKey(), func);
			});
		}
	}
}
