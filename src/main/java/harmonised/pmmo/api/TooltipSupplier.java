package harmonised.pmmo.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.*;

import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;

public class TooltipSupplier {
	public static final Logger LOGGER = LogManager.getLogger();
	private static Map<JType, LinkedListMultimap<ResourceLocation, Function<ItemStack, Map<String, Double>>>> tooltips = new HashMap<>();
	private static Map<JType, LinkedListMultimap<ResourceLocation, Function<BlockEntity, Map<String, Double>>>> breakTooltips = new HashMap<>();
	private static Map<JType, LinkedListMultimap<ResourceLocation, Function<Entity, Map<String, Double>>>> entityTooltips = new HashMap<>();
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and JType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public static void registerTooltipData(ResourceLocation res, JType jType, Function<ItemStack, Map<String, Double>> func) 
	{
		if (func == null) {LOGGER.info("Supplied Function Null"); return;}
		if (jType == null) {LOGGER.info("Supplied JType Null"); return;}
		if (res == null) {LOGGER.info("Supplied ResourceLocation Null"); return;}
		
		if (!tooltips.containsKey(jType)) {
			LOGGER.info("New tooltip category created for: "+jType.toString()+" "+res.toString());
			tooltips.put(jType, LinkedListMultimap.create());
		}
		if (tooltipExists(res, jType)) return;
		tooltips.get(jType).get(res).add(func);
	}
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and JType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public static void registerBreakTooltipData(ResourceLocation res, JType jType, Function<BlockEntity, Map<String, Double>> func) 
	{
		if (func == null) {LOGGER.info("Supplied Function Null"); return;}
		if (jType == null) {LOGGER.info("Supplied JType Null"); return;}
		if (res == null) {LOGGER.info("Supplied ResourceLocation Null"); return;}
		
		if (!breakTooltips.containsKey(jType)) {
			LOGGER.info("New tooltip category created for: "+jType.toString()+" "+res.toString());
			breakTooltips.put(jType, LinkedListMultimap.create());
		}
		if (tooltipExists(res, jType)) return;
		breakTooltips.get(jType).get(res).add(func);
	}
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and JType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public static void registerEntityTooltipData(ResourceLocation res, JType jType, Function<Entity, Map<String, Double>> func) 
	{
		if (func == null) {LOGGER.info("Supplied Function Null"); return;}
		if (jType == null) {LOGGER.info("Supplied JType Null"); return;}
		if (res == null) {LOGGER.info("Supplied ResourceLocation Null"); return;}
		
		if (!breakTooltips.containsKey(jType)) {
			LOGGER.info("New tooltip category created for: "+jType.toString()+" "+res.toString());
			entityTooltips.put(jType, LinkedListMultimap.create());
		}
		if (tooltipExists(res, jType)) return;
		entityTooltips.get(jType).get(res).add(func);
	}
	
	/**this is an internal method to check if a function exists for the given conditions
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether or not a function is registered for the parameters
	 */
	public static boolean tooltipExists(ResourceLocation res, JType jType)
	{
		if (jType == null) return false;
		if (res == null) return false;
		
		if (jType.equals(JType.REQ_BREAK) || jType.equals(JType.XP_VALUE_BREAK))
		{
			if (!breakTooltips.containsKey(jType))
				return false;
			return breakTooltips.get(jType).containsKey(res);
		}
		else if (jType.equals(JType.XP_VALUE_BREED) || jType.equals(JType.XP_VALUE_TAME) || jType.equals(JType.XP_VALUE_KILL))
		{
			if (!entityTooltips.containsKey(jType))
				return false;
			return entityTooltips.get(jType).containsKey(res);
		}

		else if (!tooltips.containsKey(jType)) 
			return false;
		
		return tooltips.get(jType).containsKey(res);
	}
	
	/**this is executed by PMMO where the required map for tooltips is used.  some PMMO
	 * behavior uses this map before a a requirement check for adding things like dynamic
	 * values. 
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the item.
	 */	
	public static Map<String, Double> getTooltipData(ResourceLocation res, JType jType, ItemStack stack)
	{
		if (tooltipExists(res, jType)) {
			Map<String,Double> suppliedData = new HashMap<>();
			List<Map<String, Double>> rawData = new ArrayList<>();
			for (Function<ItemStack, Map<String, Double>> func : tooltips.get(jType).get(res)) {
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
		return XP.getXpBypass(res, jType);
	}
	
	/**this is executed by PMMO where the required map for block tooltips are used.  some PMMO
	 * behavior uses this map before a a requirement check for adding things like dynamic
	 * values. 
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the block.
	 */	
	public static Map<String, Double> getTooltipData(ResourceLocation res, JType jType, BlockEntity tile)
	{
		if (tooltipExists(res, jType)) {
			Map<String,Double> suppliedData = new HashMap<>();
			List<Map<String, Double>> rawData = new ArrayList<>();
			for (Function<BlockEntity, Map<String, Double>> func : breakTooltips.get(jType).get(res)) {
				rawData.add(func.apply(tile));
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
		return XP.getXpBypass(res, jType);
	}
	
	/**this is executed by PMMO where the required map for entity interactions.  
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill map of the entity.
	 */	
	public static Map<String, Double> getTooltipData(ResourceLocation res, JType jType, Entity entity)
	{
		if (tooltipExists(res, jType)) {
			Map<String,Double> suppliedData = new HashMap<>();
			List<Map<String, Double>> rawData = new ArrayList<>();
			for (Function<Entity, Map<String, Double>> func : entityTooltips.get(jType).get(res)) {
				rawData.add(func.apply(entity));
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
		return XP.getXpBypass(res, jType);
	}
}
