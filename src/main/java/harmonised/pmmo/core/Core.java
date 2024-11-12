package harmonised.pmmo.core;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.registry.EventTriggerRegistry;
import harmonised.pmmo.registry.LevelRegistry;
import harmonised.pmmo.registry.PerkRegistry;
import harmonised.pmmo.registry.PredicateRegistry;
import harmonised.pmmo.registry.TooltipRegistry;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**<p>This class bridges the gap between various systems within Project MMO.
 * Methods within this class connect these distinct systems without 
 * polluting the features themselves with content that is not true to their
 * purpose.</p>  
 * <p>This class also allows for client and server to have their own copies
 * of both the data itself and the logic.  Using this approach Core can
 * be invoked in side-sensitive contexts and not violate any cross-side
 * boundaries.</p> 
 * 
 * @author Caltinor
 *
 */
public class Core {
	private static final Map<LogicalSide, Function<LogicalSide, Core>> INSTANCES = Map.of(LogicalSide.CLIENT, Functions.memoize(Core::new), LogicalSide.SERVER, Functions.memoize(Core::new));
	private final CoreLoader loader;
	private final PredicateRegistry predicates;
	private final EventTriggerRegistry eventReg;
	private final TooltipRegistry tooltips;
	private final PerkRegistry perks;
	private final LevelRegistry lvlProvider;
	private final IDataStorage data;
	private final LogicalSide side;
	  
	private Core(LogicalSide side) {
		this.loader = new CoreLoader();
	    this.predicates = new PredicateRegistry();
	    this.eventReg = new EventTriggerRegistry();
	    this.tooltips = new TooltipRegistry();
	    this.perks = new PerkRegistry();
	    this.lvlProvider = new LevelRegistry();
	    data = side.equals(LogicalSide.SERVER) ? new PmmoSavedData() : new DataMirror();
	    this.side = side;
	}
	  
	public static Core get(final LogicalSide side) {
	    return INSTANCES.get(side).apply(side);
	}
	public static Core get(final Level level) {
	    return get(level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
	}
	
	public void resetDataForReload() {
		tooltips.clearRegistry();
		if (side.equals(LogicalSide.SERVER)) {
			if (ServerLifecycleHooks.getCurrentServer() == null) return;
			for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				Networking.sendToClient(new CP_ClearData(), player);
			}
		}
	}
	  
	public CoreLoader getLoader() {return loader;}
	public PredicateRegistry getPredicateRegistry() {return predicates;}
	public EventTriggerRegistry getEventTriggerRegistry() {return eventReg;}
	public TooltipRegistry getTooltipRegistry() {return tooltips;}
	public PerkRegistry getPerkRegistry() {return perks;}
	public LevelRegistry getLevelProvider() {return lvlProvider;}
	public IDataStorage getData() {return data.get();}
	public LogicalSide getSide() {return side;}
	
	//============================================================================================
	/*			EXPERIENCE GAINING LOGIC
	 * 
	 * 		This section contains methods and logic for how players gain experience
	*/
	//============================================================================================
	
	/**<p>provides the supplied players the xp from the supplied map.  Global
	 * modifiers are applied to the map at this stage.
	 * <p>NOTE: internally, multiple players passed into the player argument
	 * are assumed to be in a party.  As such the xp is divided among party
	 * members according to the configuration</p>
	 * 
	 * 
	 * @param players all party members
	 * @param xpValues the map of pre-global-modifier xp awards
	 */
	public void awardXP(List<ServerPlayer> players, Map<String, Long> xpValues) {
		if (players.size() > 1)
			xpValues.replaceAll((skill, value) -> Double.valueOf((double)value * Config.server().party().bonus()).longValue());
		CoreUtils.processSkillGroupXP(xpValues);

		new HashMap<>(xpValues).forEach((skill, value) -> {
			xpValues.put(skill, (long)((double)value * Config.server().levels().skillModifiers().getOrDefault(skill, Config.server().levels().globalModifier())));
		});
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) instanceof FakePlayer || players.get(i).isDeadOrDying()) continue;
			for (Map.Entry<String, Long> award : xpValues.entrySet()) {
				long xpAward = award.getValue();
				if (players.size() > 1)
					xpAward = Double.valueOf((double)xpAward * (Config.server().party().bonus() * (double)players.size())).longValue();
				getData().addXp(players.get(i).getUUID(), award.getKey(), xpAward/players.size());
			}
		}
	  }

	//======DATA OBTAINING METHODS======
	public Map<String, Long> getExperienceAwards(EventType type, ItemStack stack, Player player, CompoundTag dataIn) {
		ResourceLocation itemID = RegistryUtil.getId(stack);
		dataIn.merge(TagUtils.stackTag(stack, player.level()));
  
		Map<String, Long> xpGains = tooltips.xpGainTooltipExists(itemID, type)
			? tooltips.getItemXpGainTooltipData(itemID, type, stack)
			: new HashMap<>();
  
		return getCommonXpAwardData(xpGains, type, itemID, player, ObjectType.ITEM, dataIn);
	}
	public Map<String, Long> getExperienceAwards(MobEffectInstance mei, Player player, CompoundTag dataIn) {
		ResourceLocation effectID = RegistryUtil.getId(mei.getEffect().value());
		EnhancementsData data = (EnhancementsData) loader.getLoader(ObjectType.EFFECT).getData().get(effectID);
		Map<String, Long> xpGains = new HashMap<>();
		if (data != null) {
			data.skillArray().getOrDefault(mei.getAmplifier()+1, new HashMap<>()).forEach((skill, value) -> {
				xpGains.put(skill, value.longValue());
			});
		}			
		return getCommonXpAwardData(xpGains, EventType.EFFECT, effectID, player, ObjectType.EFFECT, dataIn);
	}
	public Map<String, Long> getExperienceAwards(EventType type, BlockPos pos, Level level, Player player, CompoundTag dataIn) {
		ResourceLocation res = RegistryUtil.getId(level.getBlockState(pos));
		BlockEntity tile = level.getBlockEntity(pos);
		dataIn.merge(TagUtils.tileTag(tile));
		dataIn.put("state", TagUtils.stateTag(level.getBlockState(pos)));

		Map<String, Long> xpGains = (tile != null && tooltips.xpGainTooltipExists(res, type))
			? tooltips.getBlockXpGainTooltipData(res, type, tile)
			: new HashMap<>();
		
		return getCommonXpAwardData(xpGains, type, res, player, ObjectType.BLOCK, dataIn);
	}
	public Map<String, Long> getExperienceAwards(EventType type, Entity entity, Player player, CompoundTag dataIn) {
		ResourceLocation entityID = entity.getType().equals(EntityType.PLAYER) ? playerID : RegistryUtil.getId(entity);
		dataIn.merge(TagUtils.entityTag(entity));
		  
		Map<String, Long> xpGains = tooltips.xpGainTooltipExists(entityID, type)
			? tooltips.getEntityXpGainTooltipData(entityID, type, entity)
			: new HashMap<>();
		
		return getCommonXpAwardData(xpGains, type, entityID, player, ObjectType.ENTITY, dataIn);
	}
	
	public Map<String, Long> getCommonXpAwardData(Map<String, Long> xpGains, EventType type, ResourceLocation objectID, Player player, ObjectType oType, CompoundTag tag) {
		String[] loggables = new String[4];
		if (xpGains.isEmpty()) {
			xpGains = CoreUtils.mergeXpMapsWithSummateCondition(				
					tag.contains(APIUtils.SERIALIZED_AWARD_MAP) 
						? CoreUtils.deserializeAwardMap(tag.getCompound(APIUtils.SERIALIZED_AWARD_MAP))
						: new HashMap<>(),
					getObjectExperienceMap(oType, objectID, type, tag));
			
			if (Config.autovalue().enabled() && xpGains.isEmpty())
				xpGains = AutoValues.getExperienceAward(type, objectID, oType);
			
		}
		loggables[0] = type.name();
		loggables[1] = MsLoggy.mapToString(xpGains);
		
		if (player != null && !(player instanceof FakePlayer))
			CoreUtils.applyXpModifiers(xpGains, getConsolidatedModifierMap(player));
		loggables[2] = MsLoggy.mapToString(xpGains);
		List<ResourceLocation> source = new ArrayList<>();
		source.add(objectID);
		if (tag.contains(APIUtils.DAMAGE_TYPE)) source.add(Reference.of(tag.getString(APIUtils.DAMAGE_TYPE)));
		CheeseTracker.applyAntiCheese(type, source, player, xpGains);

		loggables[3] = MsLoggy.mapToString(xpGains);
		MsLoggy.INFO.log(LOG_CODE.XP, "XP: {} base:{}, afterBonus:{}, afterAntiCheese:{}", loggables);

		return xpGains;
	}	
	
	//======DATA OBTAINING UTILITY METHODS=======
	public Map<String, Long> getObjectExperienceMap(ObjectType type, ResourceLocation objectID, EventType eventType, CompoundTag tag) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? MsLoggy.DEBUG.logAndReturn(data.getXpValues(eventType, tag), LOG_CODE.DATA, "getObjectExperienceMap= {}") : new HashMap<>());
	}
	public Map<String, Double> getObjectModifierMap(ObjectType type, ResourceLocation objectID, ModifierDataType modifierType, CompoundTag tag) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? data.getBonuses(modifierType, tag) : new HashMap<>());
	}
	public Map<String, Double> getConsolidatedModifierMap(Player player) {
		Map<String, Double> mapOut = new HashMap<>();
		if (player instanceof FakePlayer) return mapOut;		
		
		//BIOME Modification
		ResourceLocation biomeID = RegistryUtil.getId(player.level().getBiome(player.blockPosition()));
		for (Map.Entry<String, Double> modMap : getObjectModifierMap(ObjectType.BIOME, biomeID, ModifierDataType.BIOME, new CompoundTag()).entrySet()) {
			mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
		}
		
		//DIMENSION Modification
		ResourceLocation dimensionID = player.level().dimension().location();
		for (Map.Entry<String, Double> modMap : getObjectModifierMap(ObjectType.DIMENSION, dimensionID, ModifierDataType.DIMENSION, new CompoundTag()).entrySet()) {
			mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
		}
				
		Map<String, Double> modifiers = new HashMap<>();
		//HELD Modification
		for (ItemStack stack : List.of(player.getOffhandItem(), player.getMainHandItem())) {
			ResourceLocation itemID = RegistryUtil.getId(stack);
			modifiers = tooltips.bonusTooltipExists(itemID, ModifierDataType.HELD) 
					? tooltips.getBonusTooltipData(itemID, ModifierDataType.HELD, stack) 
					: getObjectModifierMap(ObjectType.ITEM, itemID, ModifierDataType.HELD, TagUtils.stackTag(stack, player.level()));
			for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
				mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
			}		
		}
		
		//WORN Modification
		List<ItemStack> wornItems = new ArrayList<>();
		player.getArmorSlots().forEach(wornItems::add);
		wornItems.forEach((stack) -> {
			ResourceLocation itemID = RegistryUtil.getId(stack);
			Map<String, Double> modifers = tooltips.bonusTooltipExists(itemID, ModifierDataType.WORN) ?
					tooltips.getBonusTooltipData(itemID, ModifierDataType.WORN, stack):
					getObjectModifierMap(ObjectType.ITEM, itemID, ModifierDataType.WORN, TagUtils.stackTag(stack ,player.level()));
			for (Map.Entry<String, Double> modMap : modifers.entrySet()) {
				mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
			}
		});
		
		MsLoggy.DEBUG.log(LOG_CODE.XP, "Modifier Map: {}", MsLoggy.mapToString(mapOut));
		return loader.PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString()))
				.mergeWithPlayerBonuses(CoreUtils.processSkillGroupBonus(mapOut));
	}
	
	//============================================================================================
	/*			REQUIREMENTS LOGIC
	 * 
	 * 		This section contains methods and logic for limiting player actions
	*/
	//============================================================================================
  	
	public boolean isActionPermitted(ReqType type, ItemStack stack, Player player) {
		if (player instanceof FakePlayer
			|| !Config.server().requirements().isEnabled(type)
			|| getLoader().PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString())).ignoreReq()) return true;
		ResourceLocation itemID = RegistryUtil.getId(stack.getItem());
		
		return (predicates.predicateExists(itemID, type)) 
			? predicates.checkPredicateReq(player, stack, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, stack, player.level(), false));
	}
	public boolean isActionPermitted(ReqType type, BlockPos pos, Player player) {
		if (player instanceof FakePlayer
				|| !Config.server().requirements().isEnabled(type)
				|| getLoader().PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString())).ignoreReq()) return true;
		BlockEntity tile = player.level().getBlockEntity(pos);
		ResourceLocation res = RegistryUtil.getId(player.level().getBlockState(pos));
		return tile != null && predicates.predicateExists(res, type)
			? predicates.checkPredicateReq(player, tile, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, pos, player.level()));
	}
	public boolean isActionPermitted(ReqType type, Entity entity, Player player) {
		if (player instanceof FakePlayer
				|| !Config.server().requirements().isEnabled(type)
				|| getLoader().PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString())).ignoreReq()) return true;
		ResourceLocation entityID = entity.getType().equals(EntityType.PLAYER) ? playerID : RegistryUtil.getId(entity);
		return (predicates.predicateExists(entityID, type))
			? predicates.checkPredicateReq(player, entity, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, entity));
	}
	public boolean isActionPermitted(ReqType type, Holder<Biome> biome, Player player) {
		if (type != ReqType.TRAVEL) return false;
		if (player instanceof FakePlayer
				|| !Config.server().requirements().isEnabled(type)
				|| getLoader().PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString())).ignoreReq()) return true;
		return doesPlayerMeetReq(player.getUUID(), 
				getObjectSkillMap(ObjectType.BIOME, RegistryUtil.getId(biome), type, new CompoundTag()));
	}
	public boolean isActionPermitted(ReqType type, ResourceKey<Level> dimension, Player player) {
		if (type != ReqType.TRAVEL) return false;
		if (player instanceof FakePlayer
				|| !Config.server().requirements().isEnabled(type)
				|| getLoader().PLAYER_LOADER.getData(Reference.mc(player.getUUID().toString())).ignoreReq()) return true;
		return doesPlayerMeetReq(player.getUUID(),
				getObjectSkillMap(ObjectType.DIMENSION, dimension.location(), type, new CompoundTag()));
	}
	
	public boolean doesPlayerMeetReq(UUID playerID, Map<String, Long> requirements) {
		//convert skill groups which do not use total levels into constituent skills
		CoreUtils.processSkillGroupReqs(requirements);
		for (Map.Entry<String, Long> req : requirements.entrySet()) {
			long skillLevel = getData().getLevel(req.getKey(), playerID);
			if (Config.skills().get(req.getKey()).isSkillGroup()) {
				SkillData skillData = Config.skills().get(req.getKey());
				if (skillData.getUseTotalLevels()) {
					long total = skillData.getGroup().keySet().stream().map(skill -> getData().getLevel(skill, playerID)).mapToLong(Long::longValue).sum();
					if (req.getValue() > total) {
						return false;
					}
				}
			}
			else if (req.getValue() > skillLevel)
				return false;
		}
		return true;
	}

	//======DATA OBTAINING METHODS=======
	public Map<String, Long> getObjectSkillMap(ObjectType type, ResourceLocation objectID, ReqType reqType, CompoundTag nbt) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? data.getReqs(reqType, nbt) : new HashMap<>()); 
	}
	public Map<String, Long> getReqMap(ReqType reqType, ItemStack stack, Level level, boolean ignoreEnchants) {
		ResourceLocation itemID = RegistryUtil.getId(stack);
		Map<String, Long> reqMap = ignoreEnchants ? new HashMap<>() : getEnchantReqs(stack);
		if (tooltips.requirementTooltipExists(itemID, reqType)) 
			tooltips.getItemRequirementTooltipData(itemID, reqType, stack).forEach((skill, lvl) -> {
				reqMap.merge(skill, lvl, Long::max);
			});;
		return getCommonReqData(reqMap, ObjectType.ITEM, itemID, reqType, TagUtils.stackTag(stack, level));
	}
	public Map<String, Long> getEnchantReqs(ItemStack stack) {
		Map<String, Long> outMap = new HashMap<>();
		if (!stack.isEnchanted() || !Config.server().requirements().isEnabled(ReqType.USE_ENCHANTMENT)) return outMap;
		for (var enchant : stack.getEnchantments().entrySet()) {
			getEnchantmentReqs(enchant.getKey().unwrapKey().get().location(), enchant.getValue()-1).forEach((skill, level) -> {
				outMap.merge(skill, level, Long::max);
			});
		}
		return outMap;
	}
	public Map<String, Long> getReqMap(ReqType reqType, Entity entity) {
		ResourceLocation entityID = entity.getType().equals(EntityType.PLAYER) ? Reference.mc("player") : RegistryUtil.getId(entity);
		Map<String, Long> reqMap = tooltips.requirementTooltipExists(entityID, reqType)
			? tooltips.getEntityRequirementTooltipData(entityID, reqType, entity)
			: new HashMap<>();
		return getCommonReqData(reqMap, ObjectType.ENTITY, entityID, reqType, TagUtils.entityTag(entity));
	}	
	public Map<String, Long> getReqMap(ReqType reqType, BlockPos pos, Level level) {
		BlockEntity tile = level.getBlockEntity(pos);
		ResourceLocation blockID = RegistryUtil.getId(level.getBlockState(pos));
		Map<String, Long> reqMap = (tile != null && tooltips.requirementTooltipExists(blockID, reqType))
			? tooltips.getBlockRequirementTooltipData(blockID, reqType, tile)
			: new HashMap<>();
		CompoundTag dataIn = TagUtils.tileTag(tile);
		dataIn.put("state", TagUtils.stateTag(level.getBlockState(pos)));
		return getCommonReqData(reqMap, ObjectType.BLOCK, blockID, reqType, dataIn);
	}
	
	/**gets the default and autovalue data for the provided object.
	 * 
	 * @param reqsIn reqs from previous step
	 * @param oType object type
	 * @param objectID 
	 * @param type req type
	 * @param tag object's tag if present
	 * @return a map of skills and levels required to perform the action
	 */
	public Map<String, Long> getCommonReqData(Map<String, Long> reqsIn, ObjectType oType, ResourceLocation objectID, ReqType type, CompoundTag tag) {
		if (reqsIn.isEmpty()) {
			reqsIn = getObjectSkillMap(oType, objectID, type, tag);
			if (Config.autovalue().enabled() && reqsIn.isEmpty())
				reqsIn = AutoValues.getRequirements(type, objectID, oType);
		}
		return CoreUtils.processSkillGroupReqs(reqsIn);
	}
	//======DATA OBTAINING UTILITY METHODS======
	private ResourceLocation playerID = Reference.mc("player");
	
	public Map<String, Long> getEnchantmentReqs(ResourceLocation enchantID, int enchantLvl) {
		return ((EnhancementsData) loader.getLoader(ObjectType.ENCHANTMENT).getData(enchantID)).skillArray().getOrDefault(enchantLvl, new HashMap<>());
	}
	
	//============================================================================================
	/*			FEATURE LOGIC
	 * 
	 * 		This section contains methods for interacting with various features
	*/ 
	//============================================================================================
	public void getSalvage(ServerPlayer player) {
		boolean salvageMainHand = !player.getMainHandItem().isEmpty();
		boolean salvageOffHand = !salvageMainHand && !player.getOffhandItem().isEmpty();
		ItemStack salvageItem = salvageMainHand 
				? player.getMainHandItem() 
				: salvageOffHand 
					? player.getOffhandItem()
					: ItemStack.EMPTY ;
		if (salvageItem == ItemStack.EMPTY || salvageItem.is(Items.AIR))
			return;
		if (!loader.ITEM_LOADER.getData().containsKey(RegistryUtil.getId(salvageItem))) return;
		Map<String, Experience> playerXp = getData().getXpMap(player.getUUID());
		
		Map<String, Long> xpAwards = new HashMap<>();
		boolean validAttempt = false;
		entry:
		for (Map.Entry<ResourceLocation, SalvageData> result : loader.ITEM_LOADER.getData(RegistryUtil.getId(salvageItem)).salvage().entrySet()) {
			//First look for any skills that do not meet the req and continue to the next output 
			//item if the req is not met. 
			for (Map.Entry<String, Long> skill : result.getValue().levelReq().entrySet()) {
				if (skill.getValue() > playerXp.getOrDefault(skill.getKey(), new Experience()).getLevel().getLevel()) continue entry;
			}
			//ensures that only salvage where the reqs have been met AND the item has entries result in item consumption
			validAttempt = true;
			//get the base calculation values including the bonuses from skills
			double base = result.getValue().baseChance();
			double max = result.getValue().maxChance();
			double bonus = 0d;
			for (Map.Entry<String, Double> skill : result.getValue().chancePerLevel().entrySet()) {
				bonus += skill.getValue() * playerXp.getOrDefault(skill.getKey(), new Experience()).getLevel().getLevel();
			}
			
			//conduct random check for the total count possible and add each succcess to the output
			for (int i = 0; i < result.getValue().salvageMax(); i++) {
				if (player.getRandom().nextDouble() < Math.min(max, base + bonus)) {
					player.drop(new ItemStack(BuiltInRegistries.ITEM.get(result.getKey())), false, true);
					for (Map.Entry<String, Long> award : result.getValue().xpAward().entrySet()) {
						xpAwards.merge(award.getKey(), award.getValue(), (o, n) -> o + n);
					}
				}
			}
		}
		if (validAttempt) {
			if (salvageMainHand) player.getMainHandItem().shrink(1);
			if (salvageOffHand) player.getOffhandItem().shrink(1);
			List<ServerPlayer> party = PartyUtils.getPartyMembersInRange(player);
			awardXP(party, xpAwards);
		}	
		else
			player.sendSystemMessage(LangProvider.DENIAL_SALVAGE.asComponent(salvageItem.getDisplayName()));
	}
	
	/**stores the designated block marked for each player*/
	private final Map<UUID, BlockPos> markers = new HashMap<>();
	
	public void setMarkedPos(UUID playerID, BlockPos pos) {
		BlockPos finalPos = pos.equals(getMarkedPos(playerID)) ? BlockPos.ZERO : pos;
		markers.put(playerID, finalPos);
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Player "+playerID.toString()+" Marked Pos: "+finalPos.toString());
	}
	
	public BlockPos getMarkedPos(UUID playerID) {
		return markers.getOrDefault(playerID, BlockPos.ZERO);
	}

	public int getBlockConsume(Block block) {
		return loader.BLOCK_LOADER.getData(RegistryUtil.getId(block)).veinData().consumeAmount.orElseGet(() -> {
			return Config.server().veinMiner().requireSettings() ? -1 : Config.server().veinMiner().defaultConsume();
		});
	}
}
