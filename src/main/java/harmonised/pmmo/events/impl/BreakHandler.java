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
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
import harmonised.pmmo.setup.Core;
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
		if (!canPerform(event)) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
		}
		else {
			CompoundTag eventHookOutput = getEventHookResults(event);
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				CompoundTag perkDataIn = eventHookOutput;
				//if break data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, PerkRegistry.executePerk(EventType.BLOCK_BREAK, (ServerPlayer) event.getPlayer(), perkDataIn));
				Map<String, Long> xpAward = calculateXpAward(event, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
				awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static boolean canPerform(BreakEvent event) {
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (PredicateRegistry.predicateExists(blockID, ReqType.BREAK)) {
			BlockEntity tile = event.getPlayer().getLevel().getBlockEntity(event.getPos());
			return tile == null ? 
					PredicateRegistry.checkPredicateReq(event.getPlayer(), blockID, ReqType.BREAK) :
					PredicateRegistry.checkPredicateReq(event.getPlayer(), tile, ReqType.BREAK);
		}
		else if (Core.get(event.getPlayer().getLevel()).getSkillGates().doesObjectReqExist(ReqType.BREAK, blockID))
			return Core.get(event.getPlayer().getLevel()).getSkillGates().doesPlayerMeetReq(ReqType.BREAK, blockID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.BREAK, blockID, ObjectType.BLOCK);
			return Core.get(event.getPlayer().getLevel()).getSkillGates().doesPlayerMeetReq(ReqType.BREAK, blockID, event.getPlayer().getUUID(), requirements);
		}

		return true;
	}
	
	private static CompoundTag getEventHookResults(BreakEvent event) {
		return EventTriggerRegistry.executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
	}
	
	private static Map<String, Long> calculateXpAward(BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
				? Core.get(event.getPlayer().getLevel()).getXpUtils().deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
				: new HashMap<>();
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (TooltipRegistry.xpGainTooltipExists(blockID, EventType.BLOCK_BREAK) || event.getWorld().getBlockEntity(event.getPos()) != null) 
			xpGains = TooltipRegistry.getBlockXpGainTooltipData(blockID, EventType.BLOCK_BREAK, event.getWorld().getBlockEntity(event.getPos()));
		else if (Core.get(event.getPlayer().getLevel()).getXpUtils().hasXpGainObjectEntry(EventType.BLOCK_BREAK, blockID)) 
			xpGains = Core.get(event.getPlayer().getLevel()).getXpUtils().getObjectExperienceMap(EventType.BLOCK_BREAK, blockID);
		else if (Config.ENABLE_AUTO_VALUES.get()) 
			xpGains = AutoValues.getExperienceAward(EventType.BLOCK_BREAK, blockID, ObjectType.BLOCK);

		MsLoggy.info("XpGains: "+MsLoggy.mapToString(xpGains));
		xpGains = Core.get(event.getPlayer().getLevel()).getXpUtils().applyXpModifiers(event.getPlayer(), null, xpGains);
		MsLoggy.info("XpGains (afterMod): "+MsLoggy.mapToString(xpGains));
		xpGains = CheeseTracker.applyAntiCheese(xpGains);
		MsLoggy.info("XpGains (afterCheese): "+MsLoggy.mapToString(xpGains));
		
		return xpGains;
	}
	
	private static void awardXP(List<ServerPlayer> players, Map<String, Long> xpValues) {
		int partyCount = players.size();
		for (int i = 0; i < partyCount; i++) {
			for (Map.Entry<String, Long> award : xpValues.entrySet()) {
				if (Core.get(players.get(i).getLevel()).getXpUtils().setXpDiff(players.get(i).getUUID(), award.getKey(), award.getValue())) {
					Core.get(players.get(i).getLevel()).getXpUtils().sendXpAwardNotifications(players.get(i), award.getKey(), award.getValue());
				}
			}
		}
	}
}
