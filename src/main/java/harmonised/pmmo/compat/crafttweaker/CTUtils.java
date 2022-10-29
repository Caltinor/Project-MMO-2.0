package harmonised.pmmo.compat.crafttweaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapLocation.LocationMapContainer;
import harmonised.pmmo.config.codecs.CodecMapObject.ObjectMapContainer;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.registry.ConfigurationRegistry;
import net.minecraft.resources.ResourceLocation;

@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTUtils")
public class CTUtils implements IRuntimeAction{
	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**registers a configuration setting for a requirement to perform
	 * and action for the specified item.
	 * 
	 * @param objectID the key for the item being configured
	 * @param type the requirement category
	 * @param requirements a map of skills and levels needed to perform the action
	 * @param asOverride should this apply after datapacks as an override
	 */	
	@ZenCodeType.Method
	public static void setReq(ObjectType objectType, ResourceLocation objectID, ReqType type, Map<String, Integer> requirements) {
		//Map<String, Integer> requirements = reqs.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Integer.valueOf(entry.getValue())));
		switch (objectType) {
		case ITEM -> {CoreParser.ITEM_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectMapContainer()).reqs().put(type, requirements);}
		case BLOCK -> {CoreParser.BLOCK_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectMapContainer()).reqs().put(type, requirements);}
		case ENTITY -> {CoreParser.ENTITY_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectMapContainer()).reqs().put(type, requirements);}
		case DIMENSION -> {
			var data = CoreParser.DIMENSION_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationMapContainer());
			data.travelReq().clear();
			data.travelReq().putAll(requirements);
		}
		case BIOME -> {
			var data = CoreParser.BIOME_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationMapContainer());
			data.travelReq().clear();
			data.travelReq().putAll(requirements);
		}
		//TODO enchantments separately
		/*case ENCHANTMENT -> {
			CoreParser.ENCHANTMENT_LOADER.getData(objectID).clear();
			CoreParser.ENCHANTMENT_LOADER.getData(objectID).putAll(null);
		}*/
		default -> {}
		}
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
}
