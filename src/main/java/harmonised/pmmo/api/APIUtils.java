package harmonised.pmmo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriFunction;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.registry.ConfigurationRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class APIUtils {
	/* NOTES
	 * 
	 * - Add methods to modify the configuration while live
	 * - Add subjective methods for creating individualized req and xp behaviors
	 */
	//===============CORE HOOKS======================================
	/**get the player's current level in the skill provided
	 * 
	 * @param skill skill name.  Skills are case sensitive and usually all lowercase
	 * @param player the player whose skills are being obtained.
	 * @return the current skill level of the player
	 */
	public static int getLevel(String skill, Player player) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Core.get(player.level).getData().getPlayerSkillLevel(skill, player.getUUID());
	}
	
	/**Sets the player's current level in the skill provided
	 * 
	 * @param skill skill's name.  skills are case sensitive and usually all lowercase
	 * @param player the player whose skills are being set
	 * @param level the new level being applied to the skill
	 */
	public static void setLevel(String skill, Player player, int level) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		Core.get(player.level).getData().setPlayerSkillLevel(skill, player.getUUID(), level);
	}
	
	/**changes the player's level in the specified skill by a specific amount.
	 * providing a negative value in @{link levelChange} will reduce the player's
	 * level.
	 * 
	 * @param skill skill name.  Skills are case sensitive and usually lowercase.
	 * @param player the player whose level is being changed
	 * @param levelChange the number of levels being changed by.  negative values will reduce the player level.
	 * @return true if the level was in fact changed.
	 */
	public static boolean addLevel(String skill, Player player, int levelChange) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Core.get(player.level).getData().changePlayerSkillLevel(skill, player.getUUID(), levelChange);
	}
	
	/**Gets the raw xp value associated with the specified skill and player.
	 * 
	 * @param skill skill name.  Skills are case senstivive and usually lowercase.
	 * @param player the player whose experience is being sought.
	 * @return the raw experience earned in the specified skill.
	 */
	public static long getXp(String skill, Player player) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Core.get(player.level).getData().getXpRaw(player.getUUID(), skill);
	}
	
	/**Sets the raw XP value for the player in the skill specified.
	 * 
	 * @param skill skill name.  Skills are case senstivie and usually lowercase.
	 * @param player the player whose skill is being set.
	 * @param xpRaw the new experience amount to be set for this skill
	 */
	public static void setXp(String skill, Player player, long xpRaw) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		Core.get(player.level).getData().setXpRaw(player.getUUID(), skill, xpRaw);
	}
	
	/**Changes the player's current experience in the specified skill by the amount.
	 * Negative values will reduce current experience.
	 * 
	 * @param skill skill name. Skills are case sensitive and usually lowercase.
	 * @param player the player whose experience is being changed.
	 * @param change the amount being changed by.  Negative values reduce experience.
	 * @return true if the modification was successful.
	 */
	public static boolean addXp(String skill, Player player, long change) {
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Core.get(player.level).getData().setXpDiff(player.getUUID(), skill, change);
	}
	
	/**Obtians a map of the skills and experience amount that would be awarded for the provided
	 * item and event.  The logical side argument allows you to specify if you want to obtain 
	 * this information from the server data or from the client clone.  Which side used will 
	 * depend entirely on the sidedeness context of your implementation. <br><br>
	 * <b>NOTE:</b><i>This is a raw map from the configuration settings and does not necessarily
	 * reflect final values during gameplay.  Many events make changes to experience values
	 * before committing them to the player's data.</i>
	 * 
	 * @param item the itemstack being queried.
	 * @param type the event type the configuration is being specified for
	 * @param side the logical side this should be querying.
	 * @return a skill and experience map for the event type and item.
	 */
	public static Map<String, Long> getXpAwardMap(ItemStack item, EventType type, LogicalSide side) {
		Preconditions.checkNotNull(item);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		return Core.get(side).getExperienceAwards(type, item, null, new CompoundTag());
	}
	
	/**Obtians a map of the skills and experience amount that would be awarded for the provided
	 * block and event.  The level argument allows you to specify if you want to obtain 
	 * this information from the server data or from the client clone.  Which side used will 
	 * depend entirely on the sidedeness context of your implementation. <br><br>
	 * <b>NOTE:</b><i>This is a raw map from the configuration settings and does not necessarily
	 * reflect final values during gameplay.  Many events make changes to experience values
	 * before committing them to the player's data.</i>
	 * 
	 * @param level the level object associated with the block. Using a ClientLevel will use the client's mirror of the server data.
	 * @param pos the block location. This is used to obtain the TileEntity if one exists
	 * @param type the event type for the configuration
	 * @return a skill and experience map for the event type and block
	 */
	public static Map<String, Long> getXpAwardMap(Level level, BlockPos pos, EventType type) {
		Preconditions.checkNotNull(level);
		Preconditions.checkNotNull(pos);
		Preconditions.checkNotNull(type);
		return Core.get(level).getBlockExperienceAwards(type, pos, level, null, new CompoundTag());
	}
	
	/**Obtians a map of the skills and experience amount that would be awarded for the provided
	 * entity and event.  The logical side argument allows you to specify if you want to obtain 
	 * this information from the server data or from the client clone.  Which side used will 
	 * depend entirely on the sidedeness context of your implementation. <br><br>
	 * <b>NOTE:</b><i>This is a raw map from the configuration settings and does not necessarily
	 * reflect final values during gameplay.  Many events make changes to experience values
	 * before committing them to the player's data.</i>
	 * 
	 * @param entity the entity whose configuration is being sought
	 * @param type the event type for the configuration sought
	 * @param side the logical side the information is being obtained from
	 * @return a skill and experience map for the event tyep and block
	 */
	public static Map<String, Long> getXpAwardMap(Entity entity, EventType type, LogicalSide side) {
		Preconditions.checkNotNull(entity);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);;
		return Core.get(side).getExperienceAwards(type, entity, null, new CompoundTag());
	}
	
	/**Returns a skill-level map for the requirements of the item and the requirement type passed.
	 * Note that registered item predicates do not have to conform to the level system in determining
	 * whether an item is permitted or not.  Because of this, a missing or innacurate tooltip 
	 * registration may not reflect the outcome of a predicate check during gameplay.
	 * 
	 * @param item the item being queried
	 * @param type the requirement type
	 * @param side the side being queried.  this can be the actual setting on the server or the client mirror setting.
	 * @return a map containing the skills and their required levels for this item and requirement type.
	 */
	public static Map<String, Integer> getRequirementMap(ItemStack item, ReqType type, LogicalSide side) {
		Preconditions.checkNotNull(item);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		return Core.get(side).getReqMap(type, item);
	}
	
	/**Returns a skill-level map for the requirements of the block and the requirement type passed.
	 * Note that registered block predicates do not have to conform to the level system in determining
	 * whether an block action is permitted or not.  Because of this, a missing or innacurate tooltip 
	 * registration may not reflect the outcome of a predicate check during gameplay.
	 * 
	 * @param pos the location of the block or block entity being queried
	 * @param level the Level this block is located (this is also used for client/server sidedness checks)
	 * @param type the requirement type
	 * @return a map containing the skills and their required levels for this block and requirement type.
	 */
	public static Map<String, Integer> getRequirementMap(BlockPos pos, Level level, ReqType type) {
		Preconditions.checkNotNull(pos);
		Preconditions.checkNotNull(level);
		Preconditions.checkNotNull(type);
		return Core.get(level).getReqMap(null, pos, level);
	}
	
	/**Returns a skill-level map for the requirements of the entity and the requirement type passed.
	 * Note that registered entity predicates do not have to conform to the level system in determining
	 * whether an block action is permitted or not.  Because of this, a missing or innacurate tooltip 
	 * registration may not reflect the outcome of a predicate check during gameplay.
	 * 
	 * @param entity the entity being queried
	 * @param type the requirement type
	 * @param side the side being queried.  this can be the actual setting on the server or the client mirror setting.
	 * @return a map containing the skills and their required levels for this entity and requirement type.
	 */
	public static Map<String, Integer> getRequirementMap(Entity entity, ReqType type, LogicalSide side) {
		Preconditions.checkNotNull(entity);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(side);
		return Core.get(side).getReqMap(type, entity);
	}
	
	//===============CONFIGURATION SUPPLIERS=========================
	/**registers a configuration setting for a requirement to perform
	 * and action for the specified item.
	 * 
	 * @param objectID the key for the item being configured
	 * @param type the requirement category
	 * @param requirements a map of skills and levels needed to perform the action
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerRequirement(ResourceLocation objectID, ReqType type, Map<String, Integer> requirements, boolean asOverride) {
		registerConfiguration(asOverride, core -> {
				MsLoggy.INFO.log(LOG_CODE.DATA, "{} Requirement Configuration Applied: {}: {} {}", asOverride ? "Override": "Default", objectID.toString(), type.name(), MsLoggy.mapToString(requirements));
				core.getSkillGates().setObjectSkillMap(type, objectID, requirements);
			});
	}
	/**registers a configuration setting for experience that should be awarded
	 * to a player for performing an action with/on a specific object.
	 * 
	 * @param objectID the key for the item being configured
	 * @param type the event which awards the experience
	 * @param award a map of skills and experience values to be awarded
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerXpAward(ResourceLocation objectID, EventType type, Map<String, Long> award, boolean asOverride) {
		registerConfiguration(asOverride, core -> core.getXpUtils().setObjectXpGainMap(type, objectID, award));
	}
	/**registers a configuration setting for bonuses to xp gains.
	 * 
	 * @param objectID the object linked to the bonus
	 * @param type the relation to the object which predicates the bonus
	 * @param bonus a map of skills and multipliers (1.0 = no bonus)
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerBonus(ResourceLocation objectID, ModifierDataType type, Map<String, Double> bonus, boolean asOverride) {
		registerConfiguration(asOverride, core -> core.getXpUtils().setObjectXpModifierMap(type, objectID, bonus));
	}
	/**registers a configuration setting for what status effects should be applied to the player
	 * if they attempt to wear/hold and item they are not skilled enough to use.
	 * 
	 * @param item the key for the item being configured
	 * @param effects a map of effect ids and levels
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerNegativeEffect(ResourceLocation item, Map<ResourceLocation, Integer> effects, boolean asOverride) {
		registerConfiguration(asOverride, core -> effects.forEach((id, level) -> core.getDataConfig().setReqEffectData(item, id, level)));
	}
	/**registers a configuration setting for what status effects should be applied to the player
	 * based on their meeting or not meeting the requirements for the specified location.
	 * <p>Note: a "negative" effect on a dimension will have no use in-game</p>
	 * 
	 * @param locationID the key for the dimension or biome being configured
	 * @param effects a map of effect ids and levels
	 * @param isPositive is this for when a player gets a bonus (true) or as a penalty (false)
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerLocationEffect(ResourceLocation locationID, Map<ResourceLocation, Integer> effects, boolean isPositive, boolean asOverride) {
		registerConfiguration(asOverride, core -> core.getDataConfig().setLocationEffectData(isPositive, locationID, effects));
	}
	/**registers a configuration setting for items which can be obtained 
	 * via salvage from the item supplied.
	 * <p>This class provides {@link SalvageBuilder} as a means to construct
	 * the salvage settings for each output object</p>
	 * @param item a key for the item to be consumed by the salvage operation
	 * @param salvage a map of output item keys and the conditions for salvage
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerSalvage(ResourceLocation item, Map<ResourceLocation, SalvageBuilder> salvage, boolean asOverride) {
		registerConfiguration(asOverride, core -> salvage.forEach((id, data) ->core.getSalvageLogic().setSalvageData(item, id, data.build())));
	}
	/**registers vein information for the specified block or item.  Items 
	 * give the player ability charge rate and capacity.  blocks use the 
	 * consume amount when being veined.
	 * 
	 * @param objectID the item/block associated with this vein info
	 * @param chargeCap optional value for vein ability capacity
	 * @param chargeRate optional value for vein recharge rate
	 * @param consumeAmount optional value (only used on blocks) for vein consumed when broken
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerVeinData(ResourceLocation objectID, Optional<Integer> chargeCap, Optional<Double> chargeRate, Optional<Integer> consumeAmount, boolean asOverride) {
		VeinData data = new VeinData(chargeCap, chargeRate, consumeAmount);
		registerConfiguration(asOverride, core -> core.getVeinData().setVeinData(objectID, data));
	}
	
	public static final String MOB_HEALTH = "health";
	public static final String MOB_SPEED = "speed";
	public static final String MOB_DAMAGE = "damage";
	/**registers a configuration setting for mob modifiers to a biome or dimension.
	 * 
	 * <p>Attribute types for the inner map of mob_modifiers can be referenced
	 * using the static strings in this class prefixed with "MOB_"</p>
	 * 
	 * @param locationID the biome or dimension key
	 * @param mob_modifiers a map of mob keys with a value map of attribute types and values
	 * @param asOverride should this apply after datapacks as an override
	 */
	public static void registerMobModifier(ResourceLocation locationID, Map<ResourceLocation, Map<String, Double>> mob_modifiers, boolean asOverride) {
		registerConfiguration(asOverride, core -> mob_modifiers.forEach((id, map) -> core.getDataConfig().setMobModifierData(locationID, id, map)));		
	}

	/**<b>INTERNAL USE ONLY.</b> Utility method for registering custom configurations
	 * 
	 * @param asOverride should this apply after datapacks as an override
	 * @param consumer execution for applying the configuration
	 */
	private static void registerConfiguration(boolean asOverride, Consumer<Core> consumer) {
		if (asOverride)
			ConfigurationRegistry.get().registerOverride(consumer);
		else
			ConfigurationRegistry.get().registerDefault(consumer);
	}
	
	/**A builder class used to create a {@link harmonised.pmmo.config.codecs.CodecTypes SalvageData}
	 * for use in {@link APIUtils#registerSalvage(ResourceLocation, Map, boolean) registerSalvage}
	 * 
	 * @author Caltinor
	 *
	 */
	public static class SalvageBuilder {
		private Map<String, Double> chancePerLevel = new HashMap<>();
		private Map<String, Integer> levelReq = new HashMap<>();
		private Map<String, Long> xpAward = new HashMap<>();
		private int salvageMax = 1;
		private double baseChance = 0.0; 
		private double maxChance = 1.0;
		
		private SalvageBuilder() {}
		/**@return a new salvage builder
		 */
		public static SalvageBuilder start() {return new SalvageBuilder();}
		/**A map of skill names and chances.  Salvage logic will take the chance
		 * value and multiply it by the player's level in the skill used in the key
		 * to increase the player's odds.  This is done for all pairs in the map.
		 * <p>default = no extra chance</p>
		 * @param chancePerLevel the increase in chance per level in skill
		 */
		public SalvageBuilder setChancePerLevel(Map<String, Double> chancePerLevel) {
			this.chancePerLevel = chancePerLevel; 
			return this;
		}
		/**The required levels to have this specific salvage item checked
		 * against the chance attributes.
		 * <p>default = no requirements</p>
		 * @param levelReq the requirements to attempt this result
		 */
		public SalvageBuilder setLevelReq(Map<String, Integer> levelReq) {
			this.levelReq = levelReq;
			return this;
		}
		/**The xp that should be awarded when this item is successfully 
		 * obtained from salvage
		 * <p>default = no xp awarded</p>
		 * @param xpAward a map of skills and their associated xp awards
		 */
		public SalvageBuilder setXpAward(Map<String, Long> xpAward) {
			this.xpAward = xpAward;
			return this;
		}
		/**The most of this item that can be obtained from salvage
		 * <p>default = 1</p>
		 * @param max the maximum output of this item
		 */
		public SalvageBuilder setSalvageMax(int max) {
			this.salvageMax = max;
			return this;
		}
		/**The default chance irregardless of skills that this
		 * salvage will be obtained.
		 * <p>default = 0.0</p>
		 * @param chance chance before skill based chances are added
		 */
		public SalvageBuilder setBaseChance(double chance) {
			this.baseChance = chance;
			return this;
		}
		/**The maximum chance to obtain this item.  This acts as
		 * a ceiling for obtaining items where skills levels may
		 * create a 100% chance case that is not desired
		 * <p>default = 1.0</p>
		 * @param chance chance ceiling for this item
		 */
		public SalvageBuilder setMaxChance(double chance) {
			this.maxChance = chance;
			return this;
		}
		/**<b>INTERNAL USE ONLY</b>. 
		 * @return a constructed SalvageData object
		 */
		public SalvageData build() {
			return new SalvageData(chancePerLevel, levelReq, xpAward, salvageMax, baseChance, maxChance);
		}
	}
	
	//===============REQ AND TOOLTIP REFERENCES======================
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
	public static void registerActionPredicate(ResourceLocation res, ReqType reqType, BiPredicate<Player, ItemStack> pred) {
		Core.get(LogicalSide.SERVER).getPredicateRegistry().registerPredicate(res, reqType, pred);
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
	public static void registerBreakPredicate(ResourceLocation res, ReqType reqType, BiPredicate<Player, BlockEntity> pred) {
		Core.get(LogicalSide.SERVER).getPredicateRegistry().registerBreakPredicate(res, reqType, pred);
	}
	
	/** registers a predicate to be used in determining if a given player is permitted
	 * to perform a particular action related to an entity.
	 * The ResouceLocation and ReqType parameters are 
	 * conditions for when this check should be applied and are used by PMMO to know
	 * which predicates apply in which contexts.
	 * 
	 * @param res the entity registrykey
	 * @param reqType the requirement type
	 * @param pred what executes to determine if player is permitted to perform the action
	 */
	public static void registerEntityPredicate(ResourceLocation res, ReqType reqType, BiPredicate<Player, Entity> pred) {
		Core.get(LogicalSide.SERVER).getPredicateRegistry().registerEntityPredicate(res, reqType, pred);
	}
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.
	 *  
	 * @param res the item registrykey
	 * @param reqType the requirement type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerItemRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<ItemStack, Map<String, Integer>> func)  {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemRequirementTooltipData(res, reqType, func);
	}
	
	/**registers a Function to be used in providing the requirements for specific block
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.
	 *  
	 * @param res the block registrykey
	 * @param reqType the PMMO behavior type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerBlockRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<BlockEntity, Map<String, Integer>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerBlockRequirementTooltipData(res, reqType, func);
	}
	
	/**registers a Function to be used in providing the requirements for specific entity
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and ReqType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.
	 *  
	 * @param res the entity registrykey
	 * @param reqType the requirement type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerEntityRequirementTooltipData(ResourceLocation res, ReqType reqType, Function<Entity, Map<String, Integer>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerEntityRequirementTooltipData(res, reqType, func);
	}
	
	/**registers a Function to be used in providing the experience gains for specific item
	 * and event. The map consists of skill name and experience value pairs.  
	 * The ResouceLocation and EventType parameters are conditions for when the function 
	 * should be applied.
	 *  
	 * @param res the item registrykey
	 * @param reqType the event type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerItemXpGainTooltipData(ResourceLocation res, EventType eventType, Function<ItemStack, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemXpGainTooltipData(res, eventType, func);
	}
	
	/**registers a Function to be used in providing the experience gains for specific block
	 * and event. The map consists of skill name and experience value pairs.  
	 * The ResouceLocation and EventType parameters are conditions for when the function 
	 * should be applied.
	 *  
	 * @param res the block registrykey
	 * @param reqType the event type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerBlockXpGainTooltipData(ResourceLocation res, EventType eventType, Function<BlockEntity, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerBlockXpGainTooltipData(res, eventType, func);
	}
	
	/**registers a Function to be used in providing the experience gains for specific entity
	 * and event. The map consists of skill name and experience value pairs.  
	 * The ResouceLocation and EventType parameters are conditions for when the function 
	 * should be applied.
	 *  
	 * @param res the entity registrykey
	 * @param reqType the event type
	 * @param func returns a map of skills and required levels to pmmo on apply.
	 */
	public static void registerEntityXpGainTooltipData(ResourceLocation res, EventType eventType, Function<Entity, Map<String, Long>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerEntityXpGainTooltipData(res, eventType, func);
	}
	
	/**Registers a function to be used in supplying custom bonuses for the item ID,
	 * and modifier type provided.  This supercedes the configured settings and will
	 * override and negate them.
	 * 
	 * @param res the item ID
	 * @param type the modifier type (HELD or WORN)
	 * @param func the custom logic outputting the resulting modifier map
	 */
	public static void registerItemBonusData(ResourceLocation res, ModifierDataType type, Function<ItemStack, Map<String, Double>> func) {
		Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemBonusTooltipData(res, type, func);
	}
	
	/**<p>Registers a function which receives the level and skill that pmmo would provide
	 * by default and allows for modification of that value.  This is a completely replace
	 * function so failure to provide a default value, the original value, or using zero
	 * as a placeholder is ill advised.</p>
	 * <p>Providers are registered and executed in sequence.  The level value supplied to
	 * your function may be the product of a previous implementation.  
	 * 
	 * @param provider the funtion providing the new player level
	 * @param event required to ensure event process correctly during lifecycle
	 */
	public static void registerLevelProvider(BiFunction<String, Integer, Integer> provider, FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			Core.get(LogicalSide.SERVER).getLevelProvider().registerLevelProvider(provider);
			Core.get(LogicalSide.CLIENT).getLevelProvider().registerLevelProvider(provider);
		});
	}
	
	//===============EVENT TRIGGER REFERENCES========================
	/**Used as a map key for signalling to PMMO that an event should be cancelled*/
	public static final String IS_CANCELLED = "is_cancelled";
	/**Used as a map key for signalling to PMMO that an item interaction should be denied*/
	public static final String DENY_ITEM_USE = "deny_item";
	/**Used as a map key for signalling to PMMO that a block interaction should be denied*/
	public static final String DENY_BLOCK_USE = "deny_block";
	
	/**Registers a custome event listener inside PMMO's listeners.  All registered
	 * listeners execute after requirement checks have succeeded but before perks
	 * and xp awards.  The outputs of these functions are used provided to perks and
	 * used in modify xp awards, and also to cancel event behavior before perks and
	 * xp awards execute.
	 * 
	 * @param listenerID a unique ID for this perk for usin debugging.
	 * @param eventType the PMMO event this should execute for.  Reference the API docs on the PMMO wiki to make sure your impelmentation uses the correct forge event.
	 * @param executeOnTrigger the fucntion to execute when conditions are met to trigger custom listeners.
	 */
	public static void registerListener(
			@NonNull ResourceLocation listenerID, 
			@NonNull EventType eventType, 
			@NonNull BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger) {
		Core.get(LogicalSide.SERVER).getEventTriggerRegistry().registerListener(listenerID, eventType, executeOnTrigger);
	}
	
	//===============PERK REFERENCES=================================
	public static final String PER_LEVEL = "per_level";
	public static final String MAX_BOOST = "max_boost";
	public static final String RATIO = "ratio";
	public static final String MODIFIER = "modifier";
	public static final String MIN_LEVEL = "min_level";
	public static final String MAX_LEVEL = "max_level";
	public static final String MILESTONES = "milestones";
	public static final String MODULUS = "per_x_level";
	public static final String CHANCE = "chance";
	public static final String COOLDOWN = "cooldown";
	public static final String DURATION = "duration";
	
	public static final String BLOCK_POS = "block_pos";
	public static final String SKILLNAME = "skill";
	
	public static final String BREAK_SPEED_INPUT_VALUE = "speedIn";
	public static final String BREAK_SPEED_OUTPUT_VALUE = "speed";
	
	public static final String DAMAGE_IN = "damageIn";
	public static final String DAMAGE_OUT ="damage";
	
	public static final String JUMP_OUT = "jump_boost_output";
	
	public static final String STACK = "stack";
	public static final String PLAYER_ID = "player_id";
	
	public static final String ENCHANT_LEVEL = "enchant_level";
	public static final String ENCHANT_NAME = "enchant_name";
	
	public static final String AMBIENT = "ambient";
	public static final String VISIBLE = "visible";
	
	/**Called during common setup, this method is used to register custom perks
	 * to PMMO so that players can use them in their configurations.  It is 
	 * strongly recommended that you document your perks so that users have a
	 * full understanding of how to use it. This includes inputs and outputs, 
	 * reasonable triggers, and sidedness.
	 * 
	 * @param perkID a custom id for your perk that can be used in perks.json to reference this perk
	 * @param propertyDefaults keys used by your perks and default values to supply if omitted
	 * @param customConditions a predicate for checks outside the standard built in checks
	 * @param onExecute the function executing the behavior of this perk when triggered
	 * @param onConclude the function executing the behavior of this perk when expected to end
	 * @param side the logical sides this perk should exeute on.  Your implementation should factor in sidedness to avoid crashes.
	 */
	public static void registerPerk(
			@NonNull ResourceLocation perkID,
			@NonNull CompoundTag propertyDefaults,
			@NonNull TriPredicate<Player, CompoundTag, Integer> customConditions,
			@NonNull TriFunction<Player, CompoundTag, Integer, CompoundTag> onExecute, 
			@NonNull TriFunction<Player, CompoundTag, Integer, CompoundTag> onConclude,
			@NonNull PerkSide side) {
		if (side.equals(PerkSide.SERVER) || side.equals(PerkSide.BOTH)) {
			Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(perkID, propertyDefaults, customConditions, onExecute, onConclude);
			Core.get(LogicalSide.CLIENT).getPerkRegistry().registerProperties(perkID, propertyDefaults);
		}
		if (side.equals(PerkSide.CLIENT) || side.equals(PerkSide.BOTH))
			Core.get(LogicalSide.CLIENT).getPerkRegistry().registerPerk(perkID, propertyDefaults, customConditions, onExecute, onConclude);
	}	
	
	//===============UTILITY METHODS=================================
	/** A standard key for use in providing PMMO with custom award maps
	 */
	public static final String SERIALIZED_AWARD_MAP = "serialized_award_map";
	
	/** Both Perks and Event Triggers can be used to provide custom XP award maps
	 *  to events. When returning the {@link net.minecraft.nbt.CompoundTag CompoundTag} in  {@link onExecute} 
	 *  and {@link onConclude}, use the key {@link APIUtils.SERIALIZED_AWARD_MAP SERIALIZED_AWARD_MAP} and use 
	 *  this method to convert your award map into a universally serializable
	 *  object that PMMO can understand and utilize when processing rewards.
	 * 
	 * @param awardMap a map of skillnames and xp values to be provided to the xp award logic 
	 * @return a serialized {@link net.minecraft.nbt.ListTag ListTag} that can be deserialized consistently 
	 */
	public static ListTag serializeAwardMap(Map<String, Long> awardMap) {
		ListTag out = new ListTag();
		for (Map.Entry<String, Long> entry : awardMap.entrySet()) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString(Reference.API_MAP_SERIALIZER_KEY, entry.getKey());
			nbt.putLong(Reference.API_MAP_SERIALIZER_VALUE, entry.getValue());
			out.add(nbt);
		}
		return out;
	}
}
