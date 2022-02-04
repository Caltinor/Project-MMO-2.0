package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.setup.Core;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		if (!canPerform(core, event)) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
		}
		else {
			CompoundTag eventHookOutput = getEventHookResults(core, event);
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				CompoundTag perkDataIn = eventHookOutput;
				//if break data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_BREAK, (ServerPlayer) event.getPlayer(), perkDataIn));
				Map<String, Long> xpAward = calculateXpAward(core, event, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
				awardXP(core, partyMembersInRange, xpAward);
			}
		}
	}
	
	private static boolean canPerform(Core core, BreakEvent event) {
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (core.getPredicateRegistry().predicateExists(blockID, ReqType.BREAK)) {
			BlockEntity tile = event.getPlayer().getLevel().getBlockEntity(event.getPos());
			return tile == null ? 
					core.getPredicateRegistry().checkPredicateReq(event.getPlayer(), blockID, ReqType.BREAK) :
						core.getPredicateRegistry().checkPredicateReq(event.getPlayer(), tile, ReqType.BREAK);
		}
		else if (core.getSkillGates().doesObjectReqExist(ReqType.BREAK, blockID))
			return core.getSkillGates().doesPlayerMeetReq(ReqType.BREAK, blockID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.BREAK, blockID, ObjectType.BLOCK);
			return core.getSkillGates().doesPlayerMeetReq(ReqType.BREAK, blockID, event.getPlayer().getUUID(), requirements);
		}

		return true;
	}
	
	private static CompoundTag getEventHookResults(Core core, BreakEvent event) {
		return core.getEventTriggerRegistry().executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
	}
	
	private static Map<String, Long> calculateXpAward(Core core, BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
				? core.getXpUtils().deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
				: new HashMap<>();
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (core.getTooltipRegistry().xpGainTooltipExists(blockID, EventType.BLOCK_BREAK) || event.getWorld().getBlockEntity(event.getPos()) != null) 
			xpGains = core.getTooltipRegistry().getBlockXpGainTooltipData(blockID, EventType.BLOCK_BREAK, event.getWorld().getBlockEntity(event.getPos()));
		else if (core.getXpUtils().hasXpGainObjectEntry(EventType.BLOCK_BREAK, blockID)) 
			xpGains = core.getXpUtils().getObjectExperienceMap(EventType.BLOCK_BREAK, blockID);
		else if (Config.ENABLE_AUTO_VALUES.get()) 
			xpGains = AutoValues.getExperienceAward(EventType.BLOCK_BREAK, blockID, ObjectType.BLOCK);

		MsLoggy.info("XpGains: "+MsLoggy.mapToString(xpGains));
		xpGains = core.getXpUtils().applyXpModifiers(event.getPlayer(), null, xpGains);
		MsLoggy.info("XpGains (afterMod): "+MsLoggy.mapToString(xpGains));
		xpGains = CheeseTracker.applyAntiCheese(xpGains);
		MsLoggy.info("XpGains (afterCheese): "+MsLoggy.mapToString(xpGains));
		
		return xpGains;
	}
	
	private static void awardXP(Core core, List<ServerPlayer> players, Map<String, Long> xpValues) {
		int partyCount = players.size();
		for (int i = 0; i < partyCount; i++) {
			for (Map.Entry<String, Long> award : xpValues.entrySet()) {
				if (PmmoSavedData.get().setXpDiff(players.get(i).getUUID(), award.getKey(), award.getValue())) {
					core.getXpUtils().sendXpAwardNotifications(players.get(i), award.getKey(), award.getValue());
				}
			}
		}
	}
}
