package harmonised.pmmo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TooltipSupplier {
	public static final Logger LOGGER = LogManager.getLogger();
	private static Map<JType, Map<ResourceLocation, Function<ItemStack, Map<String, Double>>>> tooltips = new HashMap<>();
	
	public static void registerTooltipData(ResourceLocation res, JType jType, Function<ItemStack, Map<String, Double>> func) 
	{
		if (func == null) {LOGGER.info("Supplied Function Null"); return;}
		if (jType == null) {LOGGER.info("Supplied JType Null"); return;}
		if (res == null) {LOGGER.info("Supplied ResourceLocation Null"); return;}
		
		if (!tooltips.containsKey(jType)) {
			LOGGER.info("New tooltip category created for: "+jType.toString()+" "+res.toString());
			tooltips.put(jType, new HashMap<ResourceLocation, Function<ItemStack, Map<String, Double>>>());
		}
		if (tooltipExists(res, jType)) return;
		//TODO implement existing function checker/logger though might not be needed
		tooltips.get(jType).put(res, func);
	}
	
	public static boolean tooltipExists(ResourceLocation res, JType jType)
	{
		if (jType == null) return false;
		if (res == null) return false;
		if (!tooltips.containsKey(jType)) return false;
		return tooltips.get(jType).containsKey(res);
	}
	
	public static Map<String, Double> getTooltipData(ResourceLocation res, JType jType, ItemStack stack)
	{
		if (tooltipExists(res, jType)) {
			return tooltips.get(jType).get(res).apply(stack);
		}				
		return JsonConfig.data.get(jType).get(res.toString());
	}
}
