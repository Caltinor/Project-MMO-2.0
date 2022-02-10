package harmonised.pmmo.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.DataConfig;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.salvaging.SalvageLogic;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;

public class Core {
	  private static final Map<LogicalSide, Supplier<Core>> INSTANCES = Map.of(LogicalSide.CLIENT, Suppliers.memoize(Core::new)::get, LogicalSide.SERVER, Suppliers.memoize(Core::new)::get);
	  private final XpUtils xp;
	  private final SkillGates gates;
	  private final DataConfig config;
	  private final PredicateRegistry predicates;
	  private final EventTriggerRegistry eventReg;
	  private final TooltipRegistry tooltips;
	  private final PerkRegistry perks;
	  private final SalvageLogic salvageLogic;
	  
	  private Core() {
	    this.xp = new XpUtils();
	    this.gates = new SkillGates();
	    this.config = new DataConfig();
	    this.predicates = new PredicateRegistry();
	    this.eventReg = new EventTriggerRegistry();
	    this.tooltips = new TooltipRegistry();
	    this.perks = new PerkRegistry();
	    this.salvageLogic = new SalvageLogic();
	  }
	  
	  public static Core get(final LogicalSide side) {
	    return INSTANCES.get(side).get();
	  }
	  public static Core get(final Level level) {
	    return get(level.isClientSide()? LogicalSide.CLIENT : LogicalSide.SERVER);
	  }
	  
	  public XpUtils getXpUtils() {return xp;}
	  public SkillGates getSkillGates() {return gates;}
	  public DataConfig getDataConfig() {return config;}
	  public PredicateRegistry getPredicateRegistry() {return predicates;}
	  public EventTriggerRegistry getEventTriggerRegistry() {return eventReg;}
	  public TooltipRegistry getTooltipRegistry() {return tooltips;}
	  public PerkRegistry getPerkRegistry() {return perks;}
	  public SalvageLogic getSalvageLogic() {return salvageLogic;}
	  
	  public boolean isActionPermitted(ReqType type, ItemStack stack, Player player) {
		  if (!Config.reqEnabled(type).get()) return true;
		  ResourceLocation itemID = stack.getItem().getRegistryName();
			if (predicates.predicateExists(itemID, type)) 
				return predicates.checkPredicateReq(player, stack, type);
			else if (gates.doesObjectReqExist(type, itemID))
				return gates.doesPlayerMeetReq(type, itemID, player.getUUID());
			else if (Config.ENABLE_AUTO_VALUES.get()) {
				Map<String, Integer> requirements = AutoValues.getRequirements(type, itemID, ObjectType.ITEM);
				return gates.doesPlayerMeetReq(type, itemID, player.getUUID(), requirements);
			}
		  return true;
	  }
	  public boolean isBlockActionPermitted(ReqType type, BlockPos pos, Player player) {
		  if (!Config.reqEnabled(type).get()) return true;
		  BlockEntity tile = player.getLevel().getBlockEntity(pos);
		  ResourceLocation res = player.getLevel().getBlockState(pos).getBlock().getRegistryName();
		  return tile == null ?
				  isActionPermitted_BypassPredicates(type, res, player, ObjectType.BLOCK) :
				  isActionPermitted(type, tile, player);
	  }
	  private boolean isActionPermitted_BypassPredicates(ReqType type, ResourceLocation res, Player player, ObjectType oType) {
		  if (gates.doesObjectReqExist(type, res))
				return gates.doesPlayerMeetReq(type, res, player.getUUID());
		  else if (Config.ENABLE_AUTO_VALUES.get()) {
			  Map<String, Integer> requirements = AutoValues.getRequirements(type, res, oType);
			  return gates.doesPlayerMeetReq(type, res, player.getUUID(), requirements);
		  }
		  return true;
	  }
	  private boolean isActionPermitted(ReqType type, BlockEntity tile, Player player) {
		  Preconditions.checkNotNull(tile);
		  ResourceLocation blockID = tile.getBlockState().getBlock().getRegistryName();
			if (predicates.predicateExists(blockID, type)) {
				return predicates.checkPredicateReq(player, tile, type);
			}
			else if (gates.doesObjectReqExist(type, blockID))
				return gates.doesPlayerMeetReq(type, blockID, player.getUUID());
			else if (Config.ENABLE_AUTO_VALUES.get()) {
				Map<String, Integer> requirements = AutoValues.getRequirements(type, blockID, ObjectType.BLOCK);
				return gates.doesPlayerMeetReq(type, blockID, player.getUUID(), requirements);
			}
		  return true;
	  }
	  public boolean isActionPermitted(ReqType type, Entity entity, Player player) {
		  if (!Config.reqEnabled(type).get()) return true;
		  ResourceLocation entityID = new ResourceLocation(entity.getEncodeId());
			if (predicates.predicateExists(entityID, type)) 
				return predicates.checkPredicateReq(player, entity, type);
			else if (gates.doesObjectReqExist(type, entityID))
				return gates.doesPlayerMeetReq(type, entityID, player.getUUID());
			else if (Config.ENABLE_AUTO_VALUES.get()) {
				Map<String, Integer> requirements = AutoValues.getRequirements(type, entityID, ObjectType.ENTITY);
				return gates.doesPlayerMeetReq(type, entityID, player.getUUID(), requirements);
			}
		  return true;
	  }
	 
	  public Map<String, Long> getExperienceAwards(EventType type, ItemStack stack, Player player, CompoundTag dataIn) {
		  Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
					? xp.deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
					: new HashMap<>();
		  boolean tooltipsUsed = false;
		  ResourceLocation itemID = stack.getItem().getRegistryName();
		  if (tooltipsUsed = tooltips.xpGainTooltipExists(itemID, type))
			  xpGains = xp.mergeXpMapsWithSummateCondition(xpGains, tooltips.getItemXpGainTooltipData(itemID, type, stack));
		  return getCommonXpAwardData(xpGains, type, itemID, player, ObjectType.ITEM, tooltipsUsed);
	  }
	  public Map<String, Long> getBlockExperienceAwards(EventType type, BlockPos pos, Player player, CompoundTag dataIn) {
		  Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
					? xp.deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
					: new HashMap<>();
		  BlockEntity tile = player.getLevel().getBlockEntity(pos);
		  ResourceLocation res = player.getLevel().getBlockState(pos).getBlock().getRegistryName();
		  return tile == null ?
				  xp.mergeXpMapsWithSummateCondition(xpGains, getCommonXpAwardData(xpGains, type, res, player, ObjectType.BLOCK, false)) :
				  xp.mergeXpMapsWithSummateCondition(xpGains, getExperienceAwards(xpGains, type, tile, player));
	  }
	  private Map<String, Long> getExperienceAwards(Map<String, Long> xpGains, EventType type, BlockEntity tile, Player player) {		  
			ResourceLocation blockID = tile.getBlockState().getBlock().getRegistryName();
			boolean tooltipsUsed = false;
			if (tooltipsUsed = tooltips.xpGainTooltipExists(blockID, type)) 
				xpGains = xp.mergeXpMapsWithSummateCondition(xpGains, tooltips.getBlockXpGainTooltipData(blockID, type, tile));
		  return getCommonXpAwardData(xpGains, type, blockID, player, ObjectType.BLOCK, tooltipsUsed);
	  }	  
	  public Map<String, Long> getExperienceAwards(EventType type, Entity entity, Player player, CompoundTag dataIn) {
		  Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
					? xp.deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
					: new HashMap<>();
		  boolean tooltipsUsed = false;
		  ResourceLocation entityID = new ResourceLocation(entity.getEncodeId());
		  if (tooltipsUsed = tooltips.xpGainTooltipExists(entityID, type))
			  xpGains = xp.mergeXpMapsWithSummateCondition(xpGains, tooltips.getEntityXpGainTooltipData(entityID, type, entity));
		  return getCommonXpAwardData(xpGains, type, entityID, player, ObjectType.ENTITY, tooltipsUsed);
	  }
	  private Map<String, Long> getCommonXpAwardData(Map<String, Long> inMap, EventType type, ResourceLocation objectID, Player player, ObjectType oType, boolean predicateUsed) {
		  if (!predicateUsed) {
			  if (xp.hasXpGainObjectEntry(type, objectID)) 
				  inMap = xp.mergeXpMapsWithSummateCondition(inMap, xp.getObjectExperienceMap(type, objectID));
			  else if (Config.ENABLE_AUTO_VALUES.get()) 
				  inMap = xp.mergeXpMapsWithSummateCondition(inMap, AutoValues.getExperienceAward(type, objectID, oType));
		  }
		  MsLoggy.info("XpGains: "+MsLoggy.mapToString(inMap));
			inMap = xp.applyXpModifiers(player, inMap);
			MsLoggy.info("XpGains (afterMod): "+MsLoggy.mapToString(inMap));
			inMap = CheeseTracker.applyAntiCheese(inMap);
			MsLoggy.info("XpGains (afterCheese): "+MsLoggy.mapToString(inMap));			
		  return inMap;
	  }

	  public void awardXP(List<ServerPlayer> players, Map<String, Long> xpValues) {
		  int partyCount = players.size();
			for (int i = 0; i < partyCount; i++) {
				for (Map.Entry<String, Long> award : xpValues.entrySet()) {
					if (PmmoSavedData.get().setXpDiff(players.get(i).getUUID(), award.getKey(), award.getValue())) {
						xp.sendXpAwardNotifications(players.get(i), award.getKey(), award.getValue());
					}
				}
			}
	  }
}
