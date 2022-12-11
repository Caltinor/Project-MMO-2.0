package harmonised.pmmo.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.features.veinmining.VeinDataManager;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.registry.EventTriggerRegistry;
import harmonised.pmmo.registry.LevelRegistry;
import harmonised.pmmo.registry.PerkRegistry;
import harmonised.pmmo.registry.PredicateRegistry;
import harmonised.pmmo.registry.TooltipRegistry;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

/**<p>This class bridges the gap between various systems within Project MMO.
 * Methods within this class connect these distinct systems without 
 * poluting the features themselves with content that is not true to their
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
	private final NBTUtilsLegacy nbt;
	private final VeinDataManager vein;
	private final IDataStorage data;
	private final LogicalSide side;
	  
	private Core(LogicalSide side) {
		this.loader = new CoreLoader();
	    this.predicates = new PredicateRegistry();
	    this.eventReg = new EventTriggerRegistry();
	    this.tooltips = new TooltipRegistry();
	    this.perks = new PerkRegistry();
	    this.lvlProvider = new LevelRegistry();
	    this.nbt = new NBTUtilsLegacy();
	    this.vein = new VeinDataManager();
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
		nbt.reset();
		vein.reset();
		if (side.equals(LogicalSide.SERVER)) {
			PmmoSavedData dataBackend = (PmmoSavedData) data;
			if (dataBackend.getServer() == null) return;
			for (ServerPlayer player : dataBackend.getServer().getPlayerList().getPlayers()) {
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
	public NBTUtilsLegacy getNBTUtils() {return nbt;}
	public VeinDataManager getVeinData() {return vein;}
	public IDataStorage getData() {return data.get();}
	public IDataStorage getData(MinecraftServer server) {return data.get(server);}
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
		new HashMap<>(xpValues).forEach((skill, value) -> {
			xpValues.put(skill, (long)((double)value * Config.SKILL_MODIFIERS.get().getOrDefault(skill, Config.GLOBAL_MODIFIER.get())));
		});
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) instanceof FakePlayer) continue;
			for (Map.Entry<String, Long> award : xpValues.entrySet()) {
				long xpAward = award.getValue();
				if (players.size() > 1)
					xpAward = Double.valueOf((double)xpAward * (Config.PARTY_BONUS.get() * (double)players.size())).longValue();
				getData().setXpDiff(players.get(i).getUUID(), award.getKey(), xpAward/players.size());
			}
		}
	  }

	//======DATA OBTAINING METHODS======
	public Map<String, Long> getExperienceAwards(EventType type, ItemStack stack, Player player, CompoundTag dataIn) {
		ResourceLocation itemID = RegistryUtil.getId(stack);
		dataIn.merge(TagUtils.stackTag(stack));
  
		Map<String, Long> xpGains = tooltips.xpGainTooltipExists(itemID, type)
			? tooltips.getItemXpGainTooltipData(itemID, type, stack)
			: new HashMap<>();
  
		return getCommonXpAwardData(xpGains, type, itemID, player, ObjectType.ITEM, dataIn);
	}
	public Map<String, Long> getExperienceAwards(MobEffectInstance mei, Player player, CompoundTag dataIn) {
		ResourceLocation effectID = RegistryUtil.getId(mei.getEffect());
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
	
	private Map<String, Long> getCommonXpAwardData(Map<String, Long> xpGains, EventType type, ResourceLocation objectID, Player player, ObjectType oType, CompoundTag tag) {
		if (xpGains.isEmpty()) {
			xpGains = CoreUtils.mergeXpMapsWithSummateCondition(				
					tag.contains(APIUtils.SERIALIZED_AWARD_MAP) 
						? CoreUtils.deserializeAwardMap(tag.getCompound(APIUtils.SERIALIZED_AWARD_MAP))
						: new HashMap<>(),
					getObjectExperienceMap(oType, objectID, type, tag));
			
			if (AutoValueConfig.ENABLE_AUTO_VALUES.get() && xpGains.isEmpty()) 
				xpGains = AutoValues.getExperienceAward(type, objectID, oType);
			
		}
		MsLoggy.INFO.log(LOG_CODE.XP, "XpGains: "+MsLoggy.mapToString(xpGains));
		
		if (player != null && !(player instanceof FakePlayer))
			CoreUtils.applyXpModifiers(xpGains, getConsolidatedModifierMap(player));
		MsLoggy.INFO.log(LOG_CODE.XP, "XpGains (afterMod): "+MsLoggy.mapToString(xpGains));
		
		CheeseTracker.applyAntiCheese(xpGains);
		MsLoggy.INFO.log(LOG_CODE.XP, "XpGains (afterCheese): "+MsLoggy.mapToString(xpGains));
		
		CoreUtils.processSkillGroupXP(xpGains);
		return xpGains;
	}	
	
	//======DATA OBTAINING UTILITY METHODS=======
	public Map<String, Long> getObjectExperienceMap(ObjectType type, ResourceLocation objectID, EventType eventType, CompoundTag tag) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? data.getXpValues(eventType, tag) : new HashMap<>());
	}
	public Map<String, Double> getObjectModifierMap(ObjectType type, ResourceLocation objectID, ModifierDataType modifierType, CompoundTag tag) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? data.getBonuses(modifierType, tag) : new HashMap<>());
	}
	public Map<String, Double> getConsolidatedModifierMap(Player player) {
		Map<String, Double> mapOut = new HashMap<>();
		if (player instanceof FakePlayer) return mapOut;
		for (ModifierDataType type : ModifierDataType.values()) {
			Map<String, Double> modifiers = new HashMap<>();
			switch (type) {
			case BIOME: {
				ResourceLocation biomeID = RegistryUtil.getId(player.level.getBiome(player.blockPosition()).value());
				modifiers = getObjectModifierMap(ObjectType.BIOME, biomeID, type, new CompoundTag());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
				}
				break;
			}
			case HELD: {
				ItemStack offhandStack = player.getOffhandItem();
				ItemStack mainhandStack = player.getMainHandItem();
				ResourceLocation offhandID = RegistryUtil.getId(offhandStack);
				modifiers = tooltips.bonusTooltipExists(offhandID, type) 
						? tooltips.getBonusTooltipData(offhandID, type, offhandStack) 
						: getObjectModifierMap(ObjectType.ITEM, offhandID, type
								, offhandStack.getTag() == null 
									? new CompoundTag()
									: offhandStack.getTag());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
				}				
				ResourceLocation mainhandID = RegistryUtil.getId(mainhandStack);				
				modifiers = tooltips.bonusTooltipExists(mainhandID, type) ?
						tooltips.getBonusTooltipData(mainhandID, null, mainhandStack) :
						getObjectModifierMap(ObjectType.ITEM, mainhandID, type
								, mainhandStack.getTag() == null
									? new CompoundTag()
									: mainhandStack.getTag());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
				}				
				break;
			}
			case WORN: {
				player.getArmorSlots().forEach((stack) -> {
					ResourceLocation itemID = RegistryUtil.getId(stack);
					Map<String, Double> modifers = tooltips.bonusTooltipExists(itemID, type) ?
							tooltips.getBonusTooltipData(itemID, type, stack):
							getObjectModifierMap(ObjectType.ITEM, itemID, type
									, stack.getTag() == null
										? new CompoundTag()
										: stack.getTag());
					for (Map.Entry<String, Double> modMap : modifers.entrySet()) {
						mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
					}
				});
				break;
			}
			case DIMENSION: {
				ResourceLocation dimensionID = player.level.dimension().location();
				modifiers = getObjectModifierMap(ObjectType.DIMENSION, dimensionID, type, new CompoundTag());
				for (Map.Entry<String, Double> modMap : modifiers.entrySet()) {
					mapOut.merge(modMap.getKey(), modMap.getValue(), (o, n) -> {return o + (n-1);});
				}
				break;
			}
			default: {}
			}
			
		}
		return loader.PLAYER_LOADER.getData(new ResourceLocation(player.getUUID().toString()))
				.mergeWithPlayerBonuses(CoreUtils.processSkillGroupBonus(mapOut));
	}
	
	//============================================================================================
	/*			REQUIREMENTS LOGIC
	 * 
	 * 		This section contains methods and logic for limiting player actions
	*/
	//============================================================================================
  	
	public boolean isActionPermitted(ReqType type, ItemStack stack, Player player) {
		if (player instanceof FakePlayer || !Config.reqEnabled(type).get()) return true;
		ResourceLocation itemID = RegistryUtil.getId(stack.getItem());
		
		return (predicates.predicateExists(itemID, type)) 
			? predicates.checkPredicateReq(player, stack, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, stack));
	}
	public boolean isActionPermitted(ReqType type, BlockPos pos, Player player) {
		if (player instanceof FakePlayer || !Config.reqEnabled(type).get()) return true;
		BlockEntity tile = player.getLevel().getBlockEntity(pos);
		ResourceLocation res = RegistryUtil.getId(player.getLevel().getBlockState(pos));
		return tile != null && predicates.predicateExists(res, type)
			? predicates.checkPredicateReq(player, tile, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, pos, player.level));
	}
	public boolean isActionPermitted(ReqType type, Entity entity, Player player) {
		if (player instanceof FakePlayer || !Config.reqEnabled(type).get()) return true;
		ResourceLocation entityID = entity.getType().equals(EntityType.PLAYER) ? playerID : RegistryUtil.getId(entity);
		return (predicates.predicateExists(entityID, type))
			? predicates.checkPredicateReq(player, entity, type)
			: doesPlayerMeetReq(player.getUUID(), getReqMap(type, entity));
	}
	public boolean isActionPermitted(ReqType type, Biome biome, Player player) {
		if (type != ReqType.TRAVEL) return false;
		return doesPlayerMeetReq(player.getUUID(), 
				getObjectSkillMap(ObjectType.BIOME, RegistryUtil.getId(biome), type, new CompoundTag()));
	}
	public boolean isActionPermitted(ReqType type, ResourceKey<Level> dimension, Player player) {
		if (type != ReqType.TRAVEL) return false;
		return doesPlayerMeetReq(player.getUUID(),
				getObjectSkillMap(ObjectType.DIMENSION, dimension.location(), type, new CompoundTag()));
	}
	
	public boolean doesPlayerMeetReq(UUID playerID, Map<String, Integer> requirements) {
		//convert skill groups which do not use total levels into constituent skills
		CoreUtils.processSkillGroupReqs(requirements);
		for (Map.Entry<String, Integer> req : requirements.entrySet()) {
			int skillLevel = getData().getPlayerSkillLevel(req.getKey(), playerID);
			if (SkillsConfig.SKILLS.get().getOrDefault(req.getKey(), SkillData.Builder.getDefault()).isSkillGroup()) {	
				SkillData skillData = SkillsConfig.SKILLS.get().get(req.getKey());
				if (skillData.getUseTotalLevels()) {
					int total = skillData.getGroup().keySet().stream().map(skill-> getData().getPlayerSkillLevel(skill, playerID)).collect(Collectors.summingInt(Integer::intValue));
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
	
	//======DATA OBTAINING METHODS=======
	public Map<String, Integer> getObjectSkillMap(ObjectType type, ResourceLocation objectID, ReqType reqType, CompoundTag nbt) {
		DataSource<?> data = loader.getLoader(type).getData().get(objectID);
		return new HashMap<>(data != null ? data.getReqs(reqType, nbt) : new HashMap<>()); 
	}
	public Map<String, Integer> getReqMap(ReqType reqType, ItemStack stack) {
		ResourceLocation itemID = RegistryUtil.getId(stack);
		Map<String, Integer> reqMap = (reqType == ReqType.USE_ENCHANTMENT)
			? getEnchantReqs(stack)
			: new HashMap<>();
		if (tooltips.requirementTooltipExists(itemID, reqType)) 
			tooltips.getItemRequirementTooltipData(itemID, reqType, stack).forEach((skill, lvl) -> {
				reqMap.merge(skill, lvl, (o, n) -> o > n ? o : n);
			});;
		return getCommonReqData(reqMap, ObjectType.ITEM, itemID, reqType, TagUtils.stackTag(stack));
	}
	public Map<String, Integer> getEnchantReqs(ItemStack stack) {
		Map<String, Integer> outMap = new HashMap<>();
		if (!stack.isEnchanted() || !Config.reqEnabled(ReqType.USE_ENCHANTMENT).get()) return outMap;
		for (Map.Entry<Enchantment, Integer> enchant : EnchantmentHelper.getEnchantments(stack).entrySet()) {			
			getEnchantmentReqs(RegistryUtil.getId(enchant.getKey()), enchant.getValue()).forEach((skill, level) -> {
				outMap.merge(skill, level, (o, n) -> o > n ? o : n);
			});
		}
		return outMap;
	}
	public Map<String, Integer> getReqMap(ReqType reqType, Entity entity) {
		ResourceLocation entityID = entity.getType().equals(EntityType.PLAYER) ? new ResourceLocation("minecraft:player") : RegistryUtil.getId(entity);
		Map<String, Integer> reqMap = tooltips.requirementTooltipExists(entityID, reqType)
			? tooltips.getEntityRequirementTooltipData(entityID, reqType, entity)
			: new HashMap<>();
		return getCommonReqData(reqMap, ObjectType.ENTITY, entityID, reqType, TagUtils.entityTag(entity));
	}	
	public Map<String, Integer> getReqMap(ReqType reqType, BlockPos pos, Level level) {
		BlockEntity tile = level.getBlockEntity(pos);
		ResourceLocation blockID = RegistryUtil.getId(level.getBlockState(pos));
		Map<String, Integer> reqMap = (tile != null && tooltips.requirementTooltipExists(blockID, reqType))
			? tooltips.getBlockRequirementTooltipData(blockID, reqType, tile)
			: new HashMap<>();
		return getCommonReqData(reqMap, ObjectType.BLOCK, blockID, reqType, TagUtils.tileTag(tile));
	}
	
	private Map<String, Integer> getCommonReqData(Map<String, Integer> reqsIn, ObjectType oType, ResourceLocation objectID, ReqType type, CompoundTag tag) {
		if (reqsIn.isEmpty()) {
			reqsIn = getObjectSkillMap(oType, objectID, type, tag);
			if (AutoValueConfig.ENABLE_AUTO_VALUES.get() && reqsIn.isEmpty())
				reqsIn = AutoValues.getRequirements(type, objectID, oType);
		}
		return CoreUtils.processSkillGroupReqs(reqsIn);
	}
	//======DATA OBTAINING UTILITY METHODS======
	private ResourceLocation playerID = new ResourceLocation("player");
	
	public Map<String, Integer> getEnchantmentReqs(ResourceLocation enchantID, int enchantLvl) {
		return ((EnhancementsData) loader.getLoader(ObjectType.ENCHANTMENT).getData(enchantID)).skillArray().getOrDefault(enchantLvl, new HashMap<>());
	}
	
	//============================================================================================
	/*			SALVAGE LOGIC
	 * 
	 * 		This section contains methods for interacting with salvage data
	*/ 
	//============================================================================================
	public void getSalvage(ServerPlayer player) {
		ItemStack salvageItem = player.getMainHandItem().isEmpty() 
				? player.getOffhandItem().isEmpty() 
						? ItemStack.EMPTY 
						: player.getOffhandItem()
				: player.getMainHandItem();
		boolean salvageMainHand = !player.getMainHandItem().isEmpty();
		boolean salvageOffHand = !salvageMainHand && !player.getOffhandItem().isEmpty();
		if (!loader.ITEM_LOADER.getData().containsKey(RegistryUtil.getId(salvageItem))) return;
		Map<String, Long> playerXp = getData().getXpMap(player.getUUID());
		
		Map<String, Long> xpAwards = new HashMap<>();
		for (Map.Entry<ResourceLocation, SalvageData> result : loader.ITEM_LOADER.getData(RegistryUtil.getId(salvageItem)).salvage().entrySet()) {
			//First look for any skills that do not meet the req and continue to the next output 
			//item if the req is not met. 
			for (Map.Entry<String, Integer> skill : result.getValue().levelReq().entrySet()) {
				if (skill.getValue() > Core.get(LogicalSide.SERVER).getData().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l))) continue;
			}
			
			//get the base calculation values including the bonuses from skills
			double base = result.getValue().baseChance();
			double max = result.getValue().maxChance();
			double bonus = 0d;
			for (Map.Entry<String, Double> skill : result.getValue().chancePerLevel().entrySet()) {
				bonus += skill.getValue() * Core.get(LogicalSide.SERVER).getData().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l));
			}
			
			//conduct random check for the total count possible and add each succcess to the output
			for (int i = 0; i < result.getValue().salvageMax(); i++) {
				if (player.getRandom().nextDouble() < Math.min(max, base + bonus)) {
					player.drop(new ItemStack(ForgeRegistries.ITEMS.getValue(result.getKey())), false, true);
					for (Map.Entry<String, Long> award : result.getValue().xpAward().entrySet()) {
						xpAwards.merge(award.getKey(), award.getValue(), (o, n) -> o + n);
					}
				}
			}
		}
		if (salvageMainHand) player.getMainHandItem().shrink(1);
		if (salvageOffHand) player.getOffhandItem().shrink(1);
		List<ServerPlayer> party = PartyUtils.getPartyMembersInRange(player);
		awardXP(party, xpAwards);
	}
	

	
	  
	/** This method registers applies PMMO's NBT logic to the values that are 
	   *  configured.  This should be fired after data is loaded or else it will
	   *  register nothing.
	   */
	public void registerNBT() {		
		//QOL maybe change the loops to use the enum applicability arrays
		//==============REGISTER REQUIREMENT LOGIC=============================== 
		for (Map.Entry<ReqType, HashMultimap<ResourceLocation, LogicEntry>> entry : nbt.itemReqLogic().entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(ReqType.BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				BiPredicate<Player, ItemStack> pred = (player, stack) -> doesPlayerMeetReq(player.getUUID(), nbt.getReqMap(entry.getKey(), stack));
				predicates.registerPredicate(rl, entry.getKey(), pred);
				Function<ItemStack, Map<String, Integer>> func = (stack) -> nbt.getReqMap(entry.getKey(), stack);
				tooltips.registerItemRequirementTooltipData(rl, entry.getKey(), func);
			});			
		}
		nbt.blockReqLogic().getOrDefault(ReqType.BREAK, HashMultimap.create()).forEach((rl, logic) -> {
			BiPredicate<Player, BlockEntity> pred = (player, tile) -> doesPlayerMeetReq(player.getUUID(), nbt.getReqMap(ReqType.BREAK, tile));
			predicates.registerBreakPredicate(rl, ReqType.BREAK, pred);
			Function<BlockEntity, Map<String, Integer>> func = (tile) -> nbt.getReqMap(ReqType.BREAK, tile);
			tooltips.registerBlockRequirementTooltipData(rl, ReqType.BREAK, func);
		});
		for (Map.Entry<ReqType, HashMultimap<ResourceLocation, LogicEntry>> entry : nbt.entityReqLogic().entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(ReqType.BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				BiPredicate<Player, Entity> pred = (player, entity) -> doesPlayerMeetReq(player.getUUID(), nbt.getReqMap(entry.getKey(), entity));
				predicates.registerEntityPredicate(rl, entry.getKey(), pred);
				Function<Entity, Map<String, Integer>> func = (entity) -> nbt.getReqMap(entry.getKey(), entity);
				tooltips.registerEntityRequirementTooltipData(rl, entry.getKey(), func);
			});
		}
		
		//==============REGISTER XP GAIN LOGIC=====================================
		for (Map.Entry<EventType, HashMultimap<ResourceLocation, LogicEntry>> entry : nbt.itemXpGainLogic().entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(EventType.BLOCK_BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				Function<ItemStack, Map<String, Long>> func = (stack) -> nbt.getXpMap(entry.getKey(), stack);
				tooltips.registerItemXpGainTooltipData(rl, entry.getKey(), func);
			});
		}
		nbt.blockXpGainLogic().getOrDefault(ReqType.BREAK, HashMultimap.create()).forEach((rl, logic) -> {
			Function<BlockEntity, Map<String, Long>> func = (tile) -> nbt.getXpMap(EventType.BLOCK_BREAK, tile);
			tooltips.registerBlockXpGainTooltipData(rl, EventType.BLOCK_BREAK, func);
		});
		for (Map.Entry<EventType, HashMultimap<ResourceLocation, LogicEntry>> entry : nbt.entityXpGainLogic().entrySet()) {
			//bypass this req for items since it is not applicable
			if (entry.getKey().equals(EventType.BLOCK_BREAK)) continue;
			//register remaining items and cases
			entry.getValue().forEach((rl, logic) -> {
				Function<Entity, Map<String, Long>> func = (entity) -> nbt.getXpMap(entry.getKey(), entity);
				tooltips.registerEntityXpGainTooltipData(rl, entry.getKey(), func);
			});
		}
		
		//==============REGISTER BONUSES LOGIC=====================================
		MsLoggy.DEBUG.log(LOG_CODE.API, "Bonus Logic entrySet size: "+nbt.bonusLogic().size());
		for (Map.Entry<ModifierDataType, HashMultimap<ResourceLocation, LogicEntry>> entry : nbt.bonusLogic().entrySet()) {
			MsLoggy.DEBUG.log(LOG_CODE.API, "Bonus Logic Element Size: "+entry.getKey().name()+" "+entry.getValue().size());
			entry.getValue().forEach((rl, logic) -> {
				MsLoggy.DEBUG.log(LOG_CODE.API, "Bonus Logic Detail: "+rl.toString()+" "+logic.toString());
				Function<ItemStack, Map<String, Double>> func = (stack) -> nbt.getBonusMap(entry.getKey(), stack);
				tooltips.registerItemBonusTooltipData(rl, entry.getKey(), func);
			});
		}
	}
}
