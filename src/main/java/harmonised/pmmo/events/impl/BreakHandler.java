package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		if (!canPerform(event))
			event.setCanceled(true);
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
		if (PredicateRegistry.predicateExists(blockID, ReqType.REQ_BREAK)) 
			return PredicateRegistry.checkPredicateReq(event.getPlayer(), blockID, ReqType.REQ_BREAK);
		else if (SkillGates.doesObjectReqExist(ReqType.REQ_BREAK, blockID))
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.REQ_BREAK, blockID, ObjectType.BLOCK);
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID(), requirements);
		}

		return true;
	}
	
	private static CompoundTag getEventHookResults(BreakEvent event) {
		return EventTriggerRegistry.executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
	}
	
	private static Map<String, Long> calculateXpAward(BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> xpGains = dataIn.contains(APIUtils.SERIALIZED_AWARD_MAP) 
				? XpUtils.deserializeAwardMap(dataIn.getList(APIUtils.SERIALIZED_AWARD_MAP, Tag.TAG_COMPOUND))
				: new HashMap<>();
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (TooltipRegistry.xpGainTooltipExists(blockID, EventType.BLOCK_BREAK) || event.getWorld().getBlockEntity(event.getPos()) != null) 
			xpGains = TooltipRegistry.getBlockXpGainTooltipData(blockID, EventType.BLOCK_BREAK, event.getWorld().getBlockEntity(event.getPos()));
		else if (XpUtils.hasXpGainObjectEntry(EventType.BLOCK_BREAK, blockID)) 
			xpGains = XpUtils.getObjectExperienceMap(EventType.BLOCK_BREAK, blockID);
		else if (Config.ENABLE_AUTO_VALUES.get()) 
			xpGains = AutoValues.getExperienceAward(EventType.BLOCK_BREAK, blockID, ObjectType.BLOCK);

		MsLoggy.info("XpGains: "+MsLoggy.mapToString(xpGains));
		xpGains = XpUtils.applyXpModifiers(event.getPlayer(), null, xpGains);
		MsLoggy.info("XpGains (afterMod): "+MsLoggy.mapToString(xpGains));
		xpGains = CheeseTracker.applyAntiCheese(xpGains);
		MsLoggy.info("XpGains (afterCheese): "+MsLoggy.mapToString(xpGains));
		
		return xpGains;
	}
	
	private static void awardXP(List<ServerPlayer> players, Map<String, Long> xpValues) {
		int partyCount = players.size();
		for (int i = 0; i < partyCount; i++) {
			for (Map.Entry<String, Long> award : xpValues.entrySet()) {
				XpEvent xpEvent = new XpEvent(players.get(i), award.getKey(), (award.getValue() / partyCount), new CompoundTag());
				if (!MinecraftForge.EVENT_BUS.post(xpEvent)) {
					XpUtils.setXpDiff(players.get(i).getUUID(), xpEvent.skill, xpEvent.amount);
					XpUtils.sendXpAwardNotifications(players.get(i), award.getKey(), partyCount);
				}
			}
		}
	}
}
