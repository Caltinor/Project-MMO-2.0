package harmonised.pmmo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.mojang.datafixers.util.Pair;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.LogicEntry.Case;
import harmonised.pmmo.core.nbt.LogicEntry.Criteria;
import harmonised.pmmo.network.clientpackets.CP_RegisterNBT;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.core.nbt.PathReader;
import harmonised.pmmo.core.nbt.Result;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

public class NBTUtils {
	public NBTUtils() {}
	
	private Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> itemReqLogic = new HashMap<>();
	private Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> blockReqLogic = new HashMap<>();
	private Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> entityReqLogic = new HashMap<>();
	private Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> itemXpGainLogic = new HashMap<>();
	private Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> blockXpGainLogic = new HashMap<>();
	private Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> entityXpGainLogic = new HashMap<>();
	private Map<ModifierDataType, HashMultimap<ResourceLocation, LogicEntry>> bonusLogic = new HashMap<>();
	
	public void reset() {
		itemReqLogic = new HashMap<>();
		blockReqLogic = new HashMap<>();
		entityReqLogic = new HashMap<>();
		itemXpGainLogic = new HashMap<>();
		blockXpGainLogic = new HashMap<>();
		entityXpGainLogic = new HashMap<>();
		bonusLogic = new HashMap<>();
		cache.clear();
	}
	
	//======================SETTERS=================================
	public void setItemReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		itemReqLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setBlockReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		blockReqLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setEntityReq(ReqType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		entityReqLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setItemXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		itemXpGainLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setBlockXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		blockXpGainLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setEntityXpGains(EventType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		entityXpGainLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	public void setBonuses(ModifierDataType type, ResourceLocation id, List<LogicEntry> logic) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(logic);
		bonusLogic.computeIfAbsent(type, s -> HashMultimap.create()).putAll(id, logic);
	}
	
	//======================GETTERS=================================
	public Map<String, Integer> getReqMap(ReqType reqType, ItemStack stack) {
		return translateToInt(evaluateEntries(stack.getTag(), itemReqLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(stack))));
	}
	public Map<String, Integer> getReqMap(ReqType reqType, BlockEntity tile) {
		return translateToInt(evaluateEntries(tile.getPersistentData(), blockReqLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(tile.getBlockState()))));
	}
	public Map<String, Integer> getReqMap(ReqType reqType, Entity entity) {
		return translateToInt(evaluateEntries(entity.getPersistentData(), entityReqLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(entity))));
	}
	public Map<String, Long> getXpMap(EventType reqType, ItemStack stack) {
		return translateToLong(evaluateEntries(stack.getTag(), itemXpGainLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(stack))));
	}
	public Map<String, Long> getXpMap(EventType reqType, BlockEntity tile) {
		return translateToLong(evaluateEntries(tile.getPersistentData(), blockXpGainLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(tile.getBlockState()))));
	}
	public Map<String, Long> getXpMap(EventType reqType, Entity entity) {
		return translateToLong(evaluateEntries(entity.getPersistentData(), entityXpGainLogic.getOrDefault(reqType, HashMultimap.create()).get(RegistryUtil.getId(entity))));
	}
	public Map<String, Double> getBonusMap(ModifierDataType type, ItemStack stack) {
		return evaluateEntries(stack.getTag(), bonusLogic.getOrDefault(type, HashMultimap.create()).get(RegistryUtil.getId(stack)));
	}
	
	public Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> itemReqLogic() {return new HashMap<>(itemReqLogic);}
	public Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> blockReqLogic() {return new HashMap<>(blockReqLogic);}
	public Map<ReqType, HashMultimap<ResourceLocation, LogicEntry>> entityReqLogic() {return new HashMap<>(entityReqLogic);}
	public Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> itemXpGainLogic() {return new HashMap<>(itemXpGainLogic);}
	public Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> blockXpGainLogic() {return new HashMap<>(blockXpGainLogic);}
	public Map<EventType, HashMultimap<ResourceLocation, LogicEntry>> entityXpGainLogic() {return new HashMap<>(entityXpGainLogic);}
	public Map<ModifierDataType, HashMultimap<ResourceLocation, LogicEntry>> bonusLogic() {return new HashMap<>(bonusLogic);}
	
	//======================INTERNAL GETTERS========================
	private String getActualPath(String key) {
		return key.contains("#") ? GlobalsConfig.PATHS.get().getOrDefault(key.replace("#", ""), "") : key;
	}
	private String getActualConstant(String key) {
		return key.contains("#") ? GlobalsConfig.CONSTANTS.get().getOrDefault(key.replace("#", ""), "") : key;
	}
	
	//======================LOGICAL METHODS=========================
	private record LogicTier (BehaviorToPrevious behavior, boolean isSummative, List<Result> results) {}
	
	private final Map<Pair<CompoundTag, Set<LogicEntry>>, Map<String, Double>> cache = new HashMap<>();
	
	private Map<String, Double> evaluateEntries(CompoundTag nbt, Set<LogicEntry> logic) {		
		Map<String, Double> output = new HashMap<>();
		//cancels the evaluation if NBT has no data or never existed
		if (nbt == null || nbt.isEmpty()) return output;
		//if the compound matches a cached result, return that instead
		if (cache.containsKey(Pair.of(nbt, logic))) return MsLoggy.DEBUG.logAndReturn(cache.get(Pair.of(nbt, logic)), LOG_CODE.DATA, "NBT Cache Used");
		//this section cycles through the logic and generates usable result objects
		List<LogicTier> logicSequence = new ArrayList<>();
		for (LogicEntry entry : logic) {
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
		cache.put(Pair.of(nbt, logic), output);
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
	
	//======================REGISTER SETTINGS======================
	/**Used by {@link harmonised.pmmo.network.Networking#registerDataSyncPackets()}
	 * to send a packet to the player during various stages of the reload
	 * process to trigger client side application of the configurations.
	 * 
	 * @param channel the network channel the packet is being sent through
	 */	
	public static Consumer<OnDatapackSyncEvent> onDataReload(SimpleChannel channel) {
		return event -> {
			ServerPlayer player = event.getPlayer();
			PacketTarget target = player == null
					? PacketDistributor.ALL.noArg()
					: PacketDistributor.PLAYER.with(() -> player);
			channel.send(target, new CP_RegisterNBT());
		};
	}
	
}
