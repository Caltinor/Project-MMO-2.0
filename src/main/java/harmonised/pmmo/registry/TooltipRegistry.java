package harmonised.pmmo.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TooltipRegistry {
	public TooltipRegistry() {}
	
	private Map<ReqType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Integer>>>> itemReqTooltips = new HashMap<>();
	private Map<ReqType, LinkedListMultimap<ResourceLocation, Function<BlockEntity, Map<String, Integer>>>> blockReqTooltips = new HashMap<>();
	private Map<ReqType, LinkedListMultimap<ResourceLocation, Function<Entity, Map<String, Integer>>>> entityReqTooltips = new HashMap<>();
	
	private Map<EventType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Long>>>> itemXpGainTooltips = new HashMap<>();
	private Map<EventType, LinkedListMultimap<ResourceLocation, Function<BlockEntity, Map<String, Long>>>> blockXpGainTooltips = new HashMap<>();
	private Map<EventType, LinkedListMultimap<ResourceLocation, Function<Entity, Map<String, Long>>>> entityXpGainTooltips = new HashMap<>();
	
	private Map<ModifierDataType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Double>>>> itemBonusTooltips = new HashMap<>();
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public void registerItemRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<ItemStack, Map<String, Integer>> func) 
	{
		if (func == null) {MsLoggy.info("Supplied Function Null"); return;}
		if (reqType == null) {MsLoggy.info("Supplied ReqType Null"); return;}
		if (res == null) {MsLoggy.info("Supplied ResourceLocation Null"); return;}
		
		if (!itemReqTooltips.containsKey(reqType)) {
			MsLoggy.info("New tooltip category created for: "+reqType.toString()+" "+res.toString());
			itemReqTooltips.put(reqType, LinkedListMultimap.create());
		}
		itemReqTooltips.get(reqType).get(res).add(func);
	}
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public void registerBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<BlockEntity, Map<String, Integer>> func) 
	{
		if (func == null) {MsLoggy.info("Supplied Function Null"); return;}
		if (reqType == null) {MsLoggy.info("Supplied ReqType Null"); return;}
		if (res == null) {MsLoggy.info("Supplied ResourceLocation Null"); return;}
		
		if (!blockReqTooltips.containsKey(reqType)) {
			MsLoggy.info("New tooltip category created for: "+reqType.toString()+" "+res.toString());
			blockReqTooltips.put(reqType, LinkedListMultimap.create());
		}
		blockReqTooltips.get(reqType).get(res).add(func);
	}
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public void registerEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<Entity, Map<String, Integer>> func) 
	{
		if (func == null) {MsLoggy.info("Supplied Function Null"); return;}
		if (reqType == null) {MsLoggy.info("Supplied ReqType Null"); return;}
		if (res == null) {MsLoggy.info("Supplied ResourceLocation Null"); return;}
		
		if (!blockReqTooltips.containsKey(reqType)) {
			MsLoggy.info("New tooltip category created for: "+reqType.toString()+" "+res.toString());
			entityReqTooltips.put(reqType, LinkedListMultimap.create());
		}
		entityReqTooltips.get(reqType).get(res).add(func);
	}
	
	/**this is an internal method to check if a function exists for the given conditions
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @return whether or not a function is registered for the parameters
	 */
	public boolean requirementTooltipExists(ResourceLocation res, ReqType reqType)
	{
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(reqType);
		return blockReqTooltips.getOrDefault(reqType, LinkedListMultimap.create()).containsKey(res) ||
				entityReqTooltips.getOrDefault(reqType, LinkedListMultimap.create()).containsKey(res) ||
				itemReqTooltips.getOrDefault(reqType, LinkedListMultimap.create()).containsKey(res);
	}
	
	/**this is executed by PMMO where the required map for tooltips is used.  some PMMO
	 * behavior uses this map before a a requirement check for adding things like dynamic
	 * values. 
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the item.
	 */	
	public Map<String, Integer> getItemRequirementTooltipData(ResourceLocation res, ReqType reqType, ItemStack stack)
	{
		if (requirementTooltipExists(res, reqType)) {
			Map<String, Integer> suppliedData = new HashMap<>();
			List<Map<String, Integer>> rawData = new ArrayList<>();
			for (Function<ItemStack, Map<String, Integer>> func : itemReqTooltips.get(reqType).get(res)) {
				rawData.add(func.apply(stack));
			}
			for (int i = 0; i < rawData.size(); i++) {
				for (Map.Entry<String, Integer> entry : rawData.get(i).entrySet()) {
					suppliedData.merge(entry.getKey(), entry.getValue(), (o,n) -> {
						return o > n ? o : n;
					});
				}
			}
			return suppliedData;
		}	
		return new HashMap<>();
	}	
	/**this is executed by PMMO where the required map for block tooltips are used.  some PMMO
	 * behavior uses this map before a a requirement check for adding things like dynamic
	 * values. 
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the block.
	 */	
	public Map<String, Integer> getBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, BlockEntity tile)
	{
		if (requirementTooltipExists(res, reqType)) {
			Map<String,Integer> suppliedData = new HashMap<>();
			List<Map<String, Integer>> rawData = new ArrayList<>();
			for (Function<BlockEntity, Map<String, Integer>> func : blockReqTooltips.get(reqType).get(res)) {
				rawData.add(func.apply(tile));
			}
			for (int i = 0; i < rawData.size(); i++) {
				for (Map.Entry<String, Integer> entry : rawData.get(i).entrySet()) {
					suppliedData.merge(entry.getKey(), entry.getValue(), (o,n) -> {
						return o > n ? o : n;
					});
				}
			}
			return suppliedData;
		}	
		return new HashMap<>();
	}	
	/**this is executed by PMMO where the required map for entity interactions.  
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the entity.
	 */	
	public Map<String, Integer> getEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Entity entity)
	{
		if (requirementTooltipExists(res, reqType)) {
			Map<String,Integer> suppliedData = new HashMap<>();
			List<Map<String, Integer>> rawData = new ArrayList<>();
			for (Function<Entity, Map<String, Integer>> func : entityReqTooltips.get(reqType).get(res)) {
				rawData.add(func.apply(entity));
			}
			for (int i = 0; i < rawData.size(); i++) {
				for (Map.Entry<String, Integer> entry : rawData.get(i).entrySet()) {
					suppliedData.merge(entry.getKey(), entry.getValue(), (o,n) -> {
						return o > n ? o : n;
					});
				}
			}
			return suppliedData;
		}	
		return new HashMap<>();
	}

	public void registerItemXpGainTooltipData(ResourceLocation res, EventType eventType, Function<ItemStack, Map<String, Long>> func) {
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!itemXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			itemXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		itemXpGainTooltips.get(eventType).get(res).add(func);
	}
	public void registerBlockXpGainTooltipData(ResourceLocation res, EventType eventType, Function<BlockEntity, Map<String, Long>> func) {
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!blockXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			blockXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		blockXpGainTooltips.get(eventType).get(res).add(func);
	}
	public void registerEntityXpGainTooltipData(ResourceLocation res, EventType eventType, Function<Entity, Map<String, Long>> func) {	
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!entityXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			entityXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		entityXpGainTooltips.get(eventType).get(res).add(func);
	}	
	
	public boolean xpGainTooltipExists(ResourceLocation res, EventType eventType) {
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		return blockXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).containsKey(res) ||
				entityXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).containsKey(res) ||
				itemXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).containsKey(res);
	}
	
	public Map<String, Long> getItemXpGainTooltipData(ResourceLocation itemID, EventType eventType, ItemStack stack) {
		List<Map<String, Long>> rawData = new ArrayList<>();
		Map<String, Long> outData = new HashMap<>();
		List<Function<ItemStack, Map<String, Long>>> functions = itemXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).get(itemID);
		for (Function<ItemStack, Map<String, Long>> func : functions) {
			rawData.add(func.apply(stack));
		}
		for (Map<String, Long> map : rawData) {
			for (Map.Entry<String, Long> entry : map.entrySet()) {
				outData.merge(entry.getKey(), entry.getValue(), (o, n) -> o > n ? o : n);
			}
		}
		return outData;
	}	
	public Map<String, Long> getBlockXpGainTooltipData(ResourceLocation blockID, EventType eventType, BlockEntity tile) {
		List<Map<String, Long>> rawData = new ArrayList<>();
		Map<String, Long> outData = new HashMap<>();
		List<Function<BlockEntity, Map<String, Long>>> functions = blockXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).get(blockID);
		for (Function<BlockEntity, Map<String, Long>> func : functions) {
			rawData.add(func.apply(tile));
		}
		for (Map<String, Long> map : rawData) {
			for (Map.Entry<String, Long> entry : map.entrySet()) {
				outData.merge(entry.getKey(), entry.getValue(), (o, n) -> o > n ? o : n);
			}
		}
		return outData;
	}
	public Map<String, Long> getEntityXpGainTooltipData(ResourceLocation entityID, EventType eventType, Entity entity) {
		List<Map<String, Long>> rawData = new ArrayList<>();
		Map<String, Long> outData = new HashMap<>();
		List<Function<Entity, Map<String, Long>>> functions = entityXpGainTooltips.getOrDefault(eventType, LinkedListMultimap.create()).get(entityID);
		for (Function<Entity, Map<String, Long>> func : functions) {
			rawData.add(func.apply(entity));
		}
		for (Map<String, Long> map : rawData) {
			for (Map.Entry<String, Long> entry : map.entrySet()) {
				outData.merge(entry.getKey(), entry.getValue(), (o, n) -> o > n ? o : n);
			}
		}
		return outData;
	}
	
	public void registerItemBonusTooltipData(ResourceLocation res, ModifierDataType type, Function<ItemStack, Map<String, Double>> func) {
		if (func == null) {MsLoggy.info("Supplied Function Null"); return;}
		if (type == null) {MsLoggy.info("Supplied ModifierType Null"); return;}
		if (res == null) {MsLoggy.info("Supplied ResourceLocation Null"); return;}
		if (!itemBonusTooltips.containsKey(type)) {
			MsLoggy.info("New tooltip category created for: "+type.toString()+" "+res.toString());
			itemBonusTooltips.put(type, LinkedListMultimap.create());
		}
		itemBonusTooltips.get(type).get(res).add(func);
	}

	public boolean bonusTooltipExists(ResourceLocation res, ModifierDataType type) {
		if (res == null) return false;
		if (type == null) return false;
		if (itemBonusTooltips.containsKey(type)) {
			return itemBonusTooltips.get(type).containsKey(res);
		}
		return false;
	}
	
	public Map<String, Double> getBonusTooltipData(ResourceLocation res, ModifierDataType type, ItemStack stack) {
		if (bonusTooltipExists(res, type)) {
			Map<String, Double> suppliedData = new HashMap<>();
			List<Map<String, Double>> rawData = new ArrayList<>();
			for (Function<ItemStack, Map<String, Double>> func : itemBonusTooltips.get(type).get(res)) {
				rawData.add(func.apply(stack));
			}
			for (int i = 0; i < rawData.size(); i++) {
				for (Map.Entry<String, Double> entry : rawData.get(i).entrySet()) {
					suppliedData.merge(entry.getKey(), entry.getValue(), (o,n) -> {
						return o > n ? o : n;
					});
				}
			}
			return suppliedData;
		}	
		return new HashMap<>();
	}
}
