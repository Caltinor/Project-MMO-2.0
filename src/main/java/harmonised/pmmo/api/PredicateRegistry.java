package harmonised.pmmo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class PredicateRegistry {
	
	private static Map<String, Predicate<PlayerEntity>> reqPredicates = new HashMap<>();
	
	public static void registerPredicate(ResourceLocation res, JType jType, Predicate<PlayerEntity> pred) 
	{
		String condition = jType.toString()+";"+res.toString();
		if (pred == null) return;
		reqPredicates.put(condition, pred);
		JsonConfig.data.get(jType).remove(res.toString());
		XP.LOGGER.info("Predicate Registered: "+condition);
	}
	
	public static boolean predicateExists(ResourceLocation res, JType jType) 
	{
		return reqPredicates.containsKey(jType.toString()+";"+res.toString());
	}
	
	public static boolean checkPredicateReq(PlayerEntity player, ResourceLocation res, JType jType) 
	{
		return reqPredicates.get(jType.toString()+";"+res.toString()).test(player); 
	}
}
