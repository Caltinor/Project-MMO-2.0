package harmonised.pmmo.compat.crafttweaker;

//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiFunction;
//import java.util.function.BiPredicate;
//import java.util.stream.Collectors;
//
//import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.data.MapData;
//import com.blamejared.crafttweaker.api.data.converter.tag.TagToDataConverter;
//import com.blamejared.crafttweaker_annotations.annotations.Document;
//
//import harmonised.pmmo.api.perks.Perk;
//import harmonised.pmmo.config.codecs.MobModifier;
//import harmonised.pmmo.util.Reference;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.contents.PlainTextContents;
//import net.neoforged.fml.LogicalSide;
//import org.apache.commons.lang3.function.TriFunction;
//import org.openzen.zencode.java.ZenCodeType;
//import harmonised.pmmo.api.APIUtils;
//import harmonised.pmmo.api.enums.EventType;
//import harmonised.pmmo.api.enums.ModifierDataType;
//import harmonised.pmmo.api.enums.ObjectType;
//import harmonised.pmmo.api.enums.PerkSide;
//import harmonised.pmmo.api.enums.ReqType;
//import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
//import harmonised.pmmo.config.codecs.EnhancementsData;
//import harmonised.pmmo.config.codecs.LocationData;
//import harmonised.pmmo.config.codecs.ObjectData;
//import harmonised.pmmo.core.Core;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Player;
//
//@ZenRegister
//@Document("mods/PMMO/CTUtils")
//@ZenCodeType.Name("mods.pmmo.CTUtils")
public class CTUtils {//implements IRuntimeAction{
//	@Override
//	public void apply() {}
//	@Override
//	public String describe() {return "Functionality for using ZenCode to access PMMO's API.";}
//
//    @Override
//    public String systemName() {
//        return Reference.MOD_ID;
//    }
//
//    /**sets a configuration setting for a requirement to perform
//	 * and action for the specified item.
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param objectID the key for the item being configured
//	 * @param type the requirement category
//	 * @param requirements a map of skills and levels needed to perform the action
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam objectID <resource:namespace:path>
//	 * @docParam type <constant:pmmo:reqtype:value>
//	 * @docParam requirements {skillname: 00 as int?, otherskillname: 00 as int?}
//	 */
//	@ZenCodeType.Method
//	public static void setReq(ObjectType objectType, ResourceLocation objectID, ReqType type, Map<String, Long> requirements) {
//		switch (objectType) {
//		case ITEM -> {Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).reqs().put(type, requirements);}
//		case BLOCK -> {Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).reqs().put(type, requirements);}
//		case ENTITY -> {Core.get(LogicalSide.SERVER).getLoader().ENTITY_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).reqs().put(type, requirements);}
//		case DIMENSION -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.travelReq().clear();
//			data.travelReq().putAll(requirements);
//		}
//		case BIOME -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.travelReq().clear();
//			data.travelReq().putAll(requirements);
//		}
//		default -> {}}
//	}
//	/**sets the requirements for a given enchantment and enchantment level
//	 *
//	 * @param enchantID the key for the enchantment
//	 * @param enchantLevel the level of the enchantment
//	 * @param reqs a map of the skills and levels needed to use this enchantment
//	 *
//	 * @docParam enchantID <resource:namespace:path>
//	 * @docParam enchantLevel 1
//	 * @docParam reqs {skillname: 00 as int?, otherskillname: 00 as int?}
//	 */
//	@ZenCodeType.Method
//	public static void setEnchantment(ResourceLocation enchantID, int enchantLevel, Map<String, Long> reqs) {
//		var data = Core.get(LogicalSide.SERVER).getLoader().ENCHANTMENT_LOADER.getData().computeIfAbsent(enchantID, rl -> new EnhancementsData());
//		data.skillArray().clear();
//		data.skillArray().put(enchantLevel, reqs);
//	}
//	/**registers a configuration setting for experience that should be awarded
//	 * to a player for performing an action with/on a specific object.
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param objectID the key for the item being configured
//	 * @param type the event which awards the experience
//	 * @param award a map of skills and experience values to be awarded
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam objectID <resource:namespace:path>
//	 * @docParam type <constant:pmmo:eventtype:value>
//	 * @docParam award {skillname: 00 as long?, otherskillname: 00 as long?}
//	 */
//	@ZenCodeType.Method
//	public static void setXpAward(ObjectType objectType, ResourceLocation objectID, EventType type, Map<String, Long> award) {
//		switch (objectType) {
//		case ITEM -> {Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).xpValues().put(type, award);}
//		case BLOCK -> {Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).xpValues().put(type, award);}
//		case ENTITY -> {Core.get(LogicalSide.SERVER).getLoader().ENTITY_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).xpValues().put(type, award);}
//		default -> {}}
//	}
//	/**Registers a configuration setting for xp gained from active effects
//	 *
//	 * @param effectID the key for the effect
//	 * @param effectLevel the level of the effect
//	 * @param xpGains a map of the skills and xp awarded when this effect is active
//	 *
//	 * @docParam effectID <resource:namespace:path>
//	 * @docParam enchantLevel 1
//	 * @docParam xpGains {skillname: 00 as int?, otherskillname: 00 as int?}
//	 */
//	@ZenCodeType.Method
//	public static void setEffectXp(ResourceLocation effectID, int effectLevel, Map<String, Long> xpGains) {
//		var data = Core.get(LogicalSide.SERVER).getLoader().EFFECT_LOADER.getData().computeIfAbsent(effectID, rl -> new EnhancementsData());
//		data.skillArray().clear();
//		data.skillArray().put(effectLevel, xpGains);
//	}
//	/**registers a configuration setting for bonuses to xp gains.
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param objectID the object linked to the bonus
//	 * @param type the relation to the object which predicates the bonus
//	 * @param bonus a map of skills and multipliers (1.0 = no bonus)
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam objectID <resource:namespace:path>
//	 * @docParam type <constant:pmmo:modifierdatatype:value>
//	 * @docParam bonus {skillname: 0.0 as double?, otherskillname: 0.0 as double?}
//	 */
//	@ZenCodeType.Method
//	public static void setBonus(ObjectType objectType, ResourceLocation objectID, ModifierDataType type, Map<String, Double> bonus) {
//		switch (objectType) {
//		case ITEM -> {Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData()).bonuses().put(type, bonus);}
//		case DIMENSION -> {Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData()).bonusMap().put(type, bonus);}
//		case BIOME -> {Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData()).bonusMap().put(type, bonus);}
//		default -> {}}
//	}
//	/**registers a configuration setting for what status effects should be applied to the player
//	 * if they attempt to wear/hold and item they are not skilled enough to use.
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param objectID the key for the item being configured
//	 * @param effects a map of effect ids and levels
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam objectID <resource:namespace:path>
//	 * @docParam effects {<resource:namespace:path>: 00 as int?, <resource:othernamespace:otherpath>: 00 as int?}
//	 */
//	@ZenCodeType.Method
//	public static void setNegativeEffect(ObjectType objectType, ResourceLocation objectID, Map<ResourceLocation, Integer> effects) {
//		switch (objectType) {
//		case ITEM -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.getData().computeIfAbsent(objectID, rl -> new ObjectData());
//			data.negativeEffects().clear();
//			data.negativeEffects().putAll(effects.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
//		}
//		case DIMENSION -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.negative().clear();
//			data.negative().putAll(effects);
//		}
//		case BIOME -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.negative().clear();
//			data.negative().putAll(effects);
//		}
//		default ->{}}
//	}
//	/**registers a configuration setting for what status effects should be applied to the player
//	 * based on their meeting or not meeting the requirements for the specified location.
//	 * <p>Note: a "negative" effect on a dimension will have no use in-game</p>
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param objectID the key for the dimension or biome being configured
//	 * @param effects a map of effect ids and levels
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam objectID <resource:namespace:path>
//	 * @docParam effects {<resource:namespace:path>: 00 as int?, <resource:othernamespace:otherpath>: 00 as int?}
//	 */
//	@ZenCodeType.Method
//	public static void setPositiveEffect(ObjectType objectType, ResourceLocation objectID, Map<ResourceLocation, Integer> effects) {
//		switch (objectType) {
//		case DIMENSION -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.positive().clear();
//			data.positive().putAll(effects);
//		}
//		case BIOME -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.getData().computeIfAbsent(objectID, rl -> new LocationData());
//			data.positive().clear();
//			data.positive().putAll(effects);
//		}
//		default -> {}}
//	}
//	/**registers a configuration setting for items which can be obtained
//	 * via salvage from the item supplied.
//	 * <p>This class provides {@link CTUtils.SalvageBuilder} as a means to construct
//	 * the salvage settings for each output object</p>
//	 *
//	 * @param item a key for the item to be consumed by the salvage operation
//	 * @param salvage a map of output item keys and the conditions for salvage
//	 *
//	 * @docParam item <resource:namespace:path>
//	 * @docParam salvage {<resource:namespace:path>:builderInstance, <resource:othernamespace:otherpath>:otherbuilderInstance}
//	 */
//	@ZenCodeType.Method
//	public static void setSalvage(ResourceLocation item, Map<ResourceLocation, SalvageBuilder> salvage) {
//		var data = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.getData().computeIfAbsent(item, rl -> new ObjectData());
//		data.salvage().clear();
//		data.salvage().putAll(salvage.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().build())));
//	}
//
//	public static final String MOB_HEALTH = "health";
//	public static final String MOB_SPEED = "speed";
//	public static final String MOB_DAMAGE = "damage";
//	/**registers a configuration setting for mob modifiers to a biome or dimension.
//	 *
//	 * <p>Attribute types for the inner map of mob_modifiers can be referenced
//	 * using the static strings in this class prefixed with "MOB_"</p>
//	 *
//	 * @param objectType a value of [item, block, entity, dimension, or biome]
//	 * @param locationID the biome or dimension key
//	 * @param mobID the key for the mob being set
//	 * @param modifiers a map of attributes (health, speed, or damage) and modifiers
//	 *
//	 * @docParam objectType <constant:pmmo:objecttype:value>
//	 * @docParam locationID <resource:namespace:path>
//	 * @docParam mobID <resource:namespace:path>
//	 * @docParam effects {skillname: 0.0 as double?, otherskillname: 0.0 as double?}
//	 */
//	@ZenCodeType.Method
//	public static void setMobModifier(ObjectType objectType, ResourceLocation locationID, ResourceLocation mobID, List<MobModifier> modifiers) {
//		switch (objectType) {
//		case DIMENSION -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.getData().computeIfAbsent(locationID, rl -> new LocationData()).mobModifiers();
//			data.clear();
//			data.put(mobID, modifiers);
//		}
//		case BIOME -> {
//			var data = Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.getData().computeIfAbsent(locationID, rl -> new LocationData()).mobModifiers();
//			data.clear();
//			data.put(mobID, modifiers);
//		}
//		default -> {}}
//	}
//
//	/**A builder class used to create a Salvage Data for use as a parameter.
//	 *
//	 * @author Caltinor
//	 *
//	 */
//	@ZenRegister
//	@Document("mods/PMMO/SalvageBuilder")
//	@ZenCodeType.Name("mods.pmmo.SalvageBuilder")
//	public static class SalvageBuilder {
//		private Map<String, Double> chancePerLevel = new HashMap<>();
//		private Map<String, Long> levelReq = new HashMap<>();
//		private Map<String, Long> xpAward = new HashMap<>();
//		private int salvageMax = 1;
//		private double baseChance = 0.0;
//		private double maxChance = 1.0;
//
//		private SalvageBuilder() {}
//		/**@return a new salvage builder
//		 */
//		@ZenCodeType.Method
//		public static SalvageBuilder start() {return new SalvageBuilder();}
//		/**A map of skill names and chances.  Salvage logic will take the chance
//		 * value and multiply it by the player's level in the skill used in the key
//		 * to increase the player's odds.  This is done for all pairs in the map.
//		 * <p>default = no extra chance</p>
//		 * @param chancePerLevel the increase in chance per level in skill
//		 *
//		 * @docParam chancePerLevel {skillname: 0.0 as double?, otherskillname: 0.0 as double?}
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setChancePerLevel(Map<String, Double> chancePerLevel) {
//			this.chancePerLevel = chancePerLevel;
//			return this;
//		}
//		/**The required levels to have this specific salvage item checked
//		 * against the chance attributes.
//		 * <p>default = no requirements</p>
//		 * @param levelReq the requirements to attempt this result
//		 *
//		 * @docParam levelReq {skillname: 0.0 as int?, otherskillname: 0.0 as int?}
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setLevelReq(Map<String, Long> levelReq) {
//			this.levelReq = levelReq;
//			return this;
//		}
//		/**The xp that should be awarded when this item is successfully
//		 * obtained from salvage
//		 * <p>default = no xp awarded</p>
//		 * @param xpAward a map of skills and their associated xp awards
//		 *
//		 * @docParam xpAward {skillname: 0.0 as long?, otherskillname: 0.0 as long?}
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setXpAward(Map<String, Long> xpAward) {
//			this.xpAward = xpAward;
//			return this;
//		}
//		/**The most of this item that can be obtained from salvage
//		 * <p>default = 1</p>
//		 * @param max the maximum output of this item
//		 *
//		 * @docParam max 1
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setSalvageMax(int max) {
//			this.salvageMax = max;
//			return this;
//		}
//		/**The default chance irregardless of skills that this
//		 * salvage will be obtained.
//		 * <p>default = 0.0</p>
//		 * @param chance chance before skill based chances are added
//		 *
//		 * @docParam chance 0.0
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setBaseChance(double chance) {
//			this.baseChance = chance;
//			return this;
//		}
//		/**The maximum chance to obtain this item.  This acts as
//		 * a ceiling for obtaining items where skills levels may
//		 * create a 100% chance case that is not desired
//		 * <p>default = 1.0</p>
//		 * @param chance chance ceiling for this item
//		 *
//		 * @docParam chance 1.0
//		 */
//		@ZenCodeType.Method
//		public SalvageBuilder setMaxChance(double chance) {
//			this.maxChance = chance;
//			return this;
//		}
//		/**<b>INTERNAL USE ONLY</b>.
//		 * @return a constructed SalvageData object
//		 */
//		public SalvageData build() {
//			return new SalvageData(chancePerLevel, levelReq, xpAward, salvageMax, baseChance, maxChance);
//		}
//	}
//
//	/**Registers a perk for use in pmmo-Perks.toml.
//	 *
//	 * @param perkID the Perk ID
//	 * @param defaults the default settings for your perk.  These
//	 * are provided to your execution if no manual user configuration
//	 * is present.  This allows you to ignore null checks and
//	 * displays to users what settings are valid for your perk.
//	 * <i>(NOTE: you are not required to add settings here for them
//	 * to work.  It is permitted to have "hidden" settings.)</i>
//	 * @param customConditions used to check for conditions in which
//	 * a perk is allowed to execute in addition to pmmo's default
//	 * checks.  For example, if the player is the correct dimension.
//	 * @param onStart the logic executed by this perk when it is triggered
//     * @param onTick logic executed over the "duration" of the perk, if duration is set
//	 * @param onStop the logic executed after the start behavior and all ticking has concluded
//     * @param description a short explanation of what the perk does.
//	 * @param side which logical side this fires on CLIENT=0, SERVER=1, BOTH=2
//	 *
//	 * @docparam perkID <resource:namespace:path>
//	 * @docparam defaults {"settingKey": "value", "otherSetting": 0} as MapData
//	 * @docparam customConditions (player, data, level) => {return true;}
//	 * @docparam onStart (player, data) => { logic; return new MapData();}
//     * @docparam onTick (player, data) => { logic; return new MapData();}
//	 * @docparam onStop (player, data) => { logic; return new MapData();}
//	 * @docparam side 0
//	 */
//	@ZenCodeType.Method
//	public static void registerPerk(
//			ResourceLocation perkID,
//			MapData defaults,
//			CTPerkPredicate customConditions,
//			CTPerkFunction onStart,
//            CTTickFunction onTick,
//			CTPerkFunction onStop,
//            PlainTextContents.LiteralContents description,
//            CTDescriptionFunction status,
//			int side) {
//		BiPredicate<Player, CompoundTag> conditions = (p, c) -> customConditions.test(p, (MapData) TagToDataConverter.convert(c));
//		BiFunction<Player, CompoundTag, CompoundTag> execute = (p, c) -> onStart.apply(p,(MapData) TagToDataConverter.convert(c)).getInternal();
//        TriFunction<Player, CompoundTag, Integer, CompoundTag> tick = (p, c, t) -> onTick.apply(p,(MapData) TagToDataConverter.convert(c), t).getInternal();
//		BiFunction<Player, CompoundTag, CompoundTag> conclude = (p, c) -> onStop.apply(p, (MapData) TagToDataConverter.convert(c)).getInternal();
//        BiFunction<Player, CompoundTag, List<MutableComponent>> statusOut = (p, c) -> status.apply(p, ((MapData) TagToDataConverter.convert(c))).stream().map(MutableComponent::create).toList();
//		PerkSide perkSide = PerkSide.values()[side > 2 ? 2 : side];
//        Perk perk = new Perk(conditions, defaults.getInternal(), execute, tick, conclude, MutableComponent.create(description), statusOut);
//		APIUtils.registerPerk(perkID, perk, perkSide);
//	}
}
