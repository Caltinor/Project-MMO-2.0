package harmonised.pmmo.impl;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PredicateRegistry {
	private static LinkedListMultimap<String, Predicate<Player>> reqPredicates = LinkedListMultimap.create();
	private static LinkedListMultimap<String, BiPredicate<Player, BlockEntity>> reqBreakPredicates = LinkedListMultimap.create();
	
	/** registers a predicate to be used in determining if a given player is permitted
	 * to perform a particular action. [Except for break action.  see registerBreakPredicate.
	 * The ResouceLocation and JType parameters are 
	 * conditions for when this check should be applied and are used by PMMO to know
	 * which predicates apply in which contexts.  The predicate itself links to the 
	 * compat mod's logic which handles the external behavior.
	 * 
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param pred what executes to determine if player is permitted to perform the action
	 */
	public static void registerPredicate(ResourceLocation res, ReqType jType, Predicate<Player> pred) 
	{
		String condition = jType.toString()+";"+res.toString();
		if (pred == null) 
			return;
		reqPredicates.get(condition).add(pred);
		MsLoggy.info("Predicate Registered: "+condition);
	}
	
	/** registers a predicate to be used in determining if a given player is permitted
	 * to break a block.  The ResouceLocation and JType parameters are 
	 * conditions for when this check should be applied and are used by PMMO to know
	 * which predicates apply in which contexts.  The predicate itself links to the 
	 * compat mod's logic which handles the external behavior.
	 * 
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param pred what executes to determine if player is permitted to perform the action
	 */
	public static void registerBreakPredicate(ResourceLocation res, ReqType jType, BiPredicate<Player, BlockEntity> pred) 
	{
		String condition = jType.toString()+";"+res.toString();
		if (pred == null) 
			return;
		reqBreakPredicates.get(condition).add(pred);
		MsLoggy.info("Predicate Registered: "+condition);
	}
	
	/**this is an internal method to check if a predicate exists for the given conditions
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether or not a predicate is registered for the parameters
	 */
	public static boolean predicateExists(ResourceLocation res, ReqType jType) 
	{
		if (jType.equals(ReqType.BREAK))
			return reqBreakPredicates.containsKey(jType.toString()+";"+res.toString());
		return reqPredicates.containsKey(jType.toString()+";"+res.toString());
	}
	
	/**this is executed by PMMO logic to determine if the player is permitted to perform
	 * the action according to the object and type contexts.  
	 * 
	 * @param player the player performing the action
	 * @param res res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether the player is permitted to do the action (true if yes)
	 */
	public static boolean checkPredicateReq(Player player, ResourceLocation res, ReqType jType) 
	{
		if (!predicateExists(res, jType)) 
			return false;
		boolean outcome = true;
		for (Predicate<Player> pred : reqPredicates.get(jType.toString()+";"+res.toString())) {
			if (!pred.test(player)) return false;
		}
		return outcome;
	}
	
	/**this is executed by PMMO logic to determine if the player is permitted to break
	 * the block according to the object and type contexts.  
	 * 
	 * @param player the player performing the action
	 * @param res res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether the player is permitted to do the action (true if yes)
	 */
	public static boolean checkPredicateReq(Player player, BlockEntity tile, ReqType jType) 
	{
		ResourceLocation res = tile.getBlockState().getBlock().getRegistryName();
		if (!predicateExists(res, jType)) 
			return false;
		boolean outcome = true;
		for (BiPredicate<Player, BlockEntity> pred : reqBreakPredicates.get(jType.toString()+";"+res.toString())) {
			if (!pred.test(player, tile)) return false;
		}
		return outcome;
	}
}
