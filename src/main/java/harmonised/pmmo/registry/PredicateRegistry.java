package harmonised.pmmo.registry;

import java.util.function.BiPredicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PredicateRegistry {
	public PredicateRegistry() {}
	
	private LinkedListMultimap<String, BiPredicate<Player, ItemStack>> reqPredicates = LinkedListMultimap.create();
	private LinkedListMultimap<String, BiPredicate<Player, BlockEntity>> reqBreakPredicates = LinkedListMultimap.create();
	private LinkedListMultimap<String, BiPredicate<Player, Entity>> reqEntityPredicates = LinkedListMultimap.create();
	
	/** registers a predicate to be used in determining if a given player is permitted
	 * to perform a particular action. [Except for break action.  see {@link APIUtils#registerBreakPredicate registerBreakPredicate}.
	 * The ResouceLocation and ReqType parameters are 
	 * conditions for when this check should be applied and are used by PMMO to know
	 * which predicates apply in which contexts.
	 * 
	 * @param res the item registrykey
	 * @param reqType the requirement type
	 * @param pred what executes to determine if player is permitted to perform the action
	 */
	public void registerPredicate(ResourceLocation res, ReqType reqType, BiPredicate<Player, ItemStack> pred) {
		Preconditions.checkNotNull(pred);
		String condition = reqType.toString()+";"+res.toString();
		reqPredicates.get(condition).add(pred);
		MsLoggy.INFO.log(LOG_CODE.API, "Predicate Registered: "+condition);
	}
	
	/** registers a predicate to be used in determining if a given player is permitted
	 * to break a block.  The ResouceLocation and ReqType parameters are 
	 * conditions for when this check should be applied and are used by PMMO to know
	 * which predicates apply in which contexts.
	 * 
	 * @param res the block registrykey
	 * @param reqType the requirement type
	 * @param pred what executes to determine if player is permitted to perform the action
	 */
	public void registerBreakPredicate(ResourceLocation res, ReqType reqType, BiPredicate<Player, BlockEntity> pred) {
		Preconditions.checkNotNull(pred);
		String condition = reqType.toString()+";"+res.toString();
		reqBreakPredicates.get(condition).add(pred);
		MsLoggy.INFO.log(LOG_CODE.API, "Predicate Registered: "+condition);
	}
	
	public void registerEntityPredicate(ResourceLocation res, ReqType type, BiPredicate<Player, Entity> pred) {
		Preconditions.checkNotNull(pred);
		String condition = type.toString()+";"+res.toString();
		reqEntityPredicates.get(condition).add(pred);
		MsLoggy.INFO.log(LOG_CODE.API, "Entity Predicate Regsitered: "+condition);
	}
	
	/**this is an internal method to check if a predicate exists for the given conditions
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether or not a predicate is registered for the parameters
	 */
	public boolean predicateExists(ResourceLocation res, ReqType type) 
	{
		String key = type.toString()+";"+res.toString();
		return reqPredicates.containsKey(key) ||
				reqBreakPredicates.containsKey(key) ||
				reqEntityPredicates.containsKey(key);
	}
	
	/**this is executed by PMMO logic to determine if the player is permitted to perform
	 * the action according to the object and type contexts.  
	 * 
	 * @param player the player performing the action
	 * @param res res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether the player is permitted to do the action (true if yes)
	 */
	public boolean checkPredicateReq(Player player, ItemStack stack, ReqType jType) 
	{
		if (!predicateExists(RegistryUtil.getId(stack), jType)) 
			return false;
		for (BiPredicate<Player, ItemStack> pred : reqPredicates.get(jType.toString()+";"+RegistryUtil.getId(stack).toString())) {
			if (!pred.test(player, stack)) return false;
		}
		return true;
	}
	
	/**this is executed by PMMO logic to determine if the player is permitted to break
	 * the block according to the object and type contexts.  
	 * 
	 * @param player the player performing the action
	 * @param res res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether the player is permitted to do the action (true if yes)
	 */
	public boolean checkPredicateReq(Player player, BlockEntity tile, ReqType jType) 
	{
		ResourceLocation res = RegistryUtil.getId(tile.getBlockState());
		if (!predicateExists(res, jType)) 
			return false;
		for (BiPredicate<Player, BlockEntity> pred : reqBreakPredicates.get(jType.toString()+";"+res.toString())) {
			if (!pred.test(player, tile)) return false;
		}
		return true;
	}
	
	public boolean checkPredicateReq(Player player, Entity entity, ReqType type) {
		ResourceLocation res = RegistryUtil.getId(entity);
		if (!predicateExists(res, type))
			return false;
		for (BiPredicate<Player, Entity> pred : reqEntityPredicates.get(type.toString()+";"+res.toString())) {
			if (!pred.test(player, entity)) return false;
		}
		return true;
	}
}
