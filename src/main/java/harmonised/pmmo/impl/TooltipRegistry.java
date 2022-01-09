package harmonised.pmmo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TooltipRegistry {
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Integer>>>> itemReqTooltips = new HashMap<>();
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, Function<BlockEntity, Map<String, Integer>>>> blockReqTooltips = new HashMap<>();
	private static Map<ReqType, LinkedListMultimap<ResourceLocation, Function<Entity, Map<String, Integer>>>> entityReqTooltips = new HashMap<>();
	
	private static Map<EventType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Long>>>> itemXpGainTooltips = new HashMap<>();
	private static Map<EventType, LinkedListMultimap<ResourceLocation, Function<BlockEntity, Map<String, Long>>>> blockXpGainTooltips = new HashMap<>();
	private static Map<EventType, LinkedListMultimap<ResourceLocation, Function<Entity, Map<String, Long>>>> entityXpGainTooltips = new HashMap<>();
	
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
	public static void registerItemRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<ItemStack, Map<String, Integer>> func) 
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
	public static void registerBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<BlockEntity, Map<String, Integer>> func) 
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
	public static void registerEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<Entity, Map<String, Integer>> func) 
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
	public static boolean requirementTooltipExists(ResourceLocation res, ReqType reqType)
	{
		if (reqType == null) return false;
		if (res == null) return false;
		
		if (reqType.equals(ReqType.REQ_BREAK) || reqType.equals(ReqType.REQ_PLACE))
		{
			if (!blockReqTooltips.containsKey(reqType))
				return false;
			return blockReqTooltips.get(reqType).containsKey(res);
		}
		else if (reqType.equals(ReqType.REQ_KILL) || reqType.equals(ReqType.REQ_ENTITY_INTERACT))
		{
			if (!entityReqTooltips.containsKey(reqType))
				return false;
			return entityReqTooltips.get(reqType).containsKey(res);
		}

		else if (!itemReqTooltips.containsKey(reqType)) 
			return false;
		
		return itemReqTooltips.get(reqType).containsKey(res);
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
	public static Map<String, Integer> getItemRequirementTooltipData(ResourceLocation res, ReqType reqType, ItemStack stack)
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
		return SkillGates.getObjectSkillMap(reqType, res);
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
	public static Map<String, Integer> getBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, BlockEntity tile)
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
		return SkillGates.getObjectSkillMap(reqType, res);
	}
	
	/**this is executed by PMMO where the required map for entity interactions.  
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param reqType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the entity.
	 */	
	public static Map<String, Integer> getEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Entity entity)
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
		return SkillGates.getObjectSkillMap(reqType, res);
	}

	public static void registerItemXpGainTooltipData(ResourceLocation res, EventType eventType, Function<ItemStack, Map<String, Long>> func) {
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!itemXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			itemXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		itemXpGainTooltips.get(eventType).get(res).add(func);
	}
	
	public static void registerBlockXpGainTooltipData(ResourceLocation res, EventType eventType, Function<BlockEntity, Map<String, Long>> func) {
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!blockXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			blockXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		blockXpGainTooltips.get(eventType).get(res).add(func);
	}
	
	public static void registerEntityXpGainTooltipData(ResourceLocation res, EventType eventType, Function<Entity, Map<String, Long>> func) {	
		Preconditions.checkNotNull(res);
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(func);
		
		if (!entityXpGainTooltips.containsKey(eventType)) {
			MsLoggy.info("New tooltip category created for: "+eventType.toString()+" "+res.toString());
			entityXpGainTooltips.put(eventType, LinkedListMultimap.create());
		}
		entityXpGainTooltips.get(eventType).get(res).add(func);
	}
	
	//TODO implement all the below methods for the XpGain maps
	public static boolean xpGainTooltipExists(ResourceLocation res, EventType eventType) {
		return false;
	}
	
	public static Map<String, Long> getItemXpGainTooltipData(ResourceLocation itemID, EventType eventType, ItemStack stack) {
		return new HashMap<>();
	}
	
	public static Map<String, Long> getBlockXpGainTooltipData(ResourceLocation itemID, EventType eventType, BlockEntity stack) {
		return new HashMap<>();
	}
	
	public static Map<String, Long> getEntityXpGainTooltipData(ResourceLocation itemID, EventType eventType, Entity stack) {
		return new HashMap<>();
	}
}
