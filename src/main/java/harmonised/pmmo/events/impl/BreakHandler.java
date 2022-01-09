package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		/* DESIGN NOTES
		 * 
		 * this should factor in the following features
		 *  - Event Granularity (nested Experience checks)
		 *  	- Event API hooks
		 *  - Perk Triggers
		 *  - AutoValues
		 *  - Effects/Penalties
		 *  - Party
		 *  - Anti-Cheese
		 *  - Unlocks
		 *  	-NBT Checks included
		 *  - XP Awards
		 *  
		 *  Sequence should be 
		 *  - Requirement (in case cancelled)
		 *  	-get item req 
		 *  		-get NBT req
		 *  			-if not get default req
		 *  				-if not get/generate AutoValue
		 *  - Event Triggers
		 *  	-Event API hooks
		 *  	-Perk Triggers
		 *  - XP calculations
		 *  	-get xp values from NBT
		 *  		-if not get default value
		 *  			-if not get/generate AutoValue
		 *  	-penalties/effects
		 *  		-anti-cheese
		 *  	-party xp distribution
		 *  -Award XP
		 *  	-send XP Event to Event Bus
		 *  	-award XP and notify
		 *  
		 * 
		 */

		if (!canPerform(event))
			event.setCanceled(true);
		else {
			CompoundTag eventHookOutput = getEventHookResults(event);
			if (!eventHookOutput.getBoolean(EventTriggerRegistry.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				//TODO un-comment these calls and replace the new tag with the perk execution
				//CompoundTag perkDataIn = TagBuilder.start().build();
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, new CompoundTag()); //PerkRegistry.executePerk(EventType.BLOCK_BREAK, event.getPlayer(), perkDataIn);
				Map<String, Long> xpAward = calculateXpAward(event, perkOutput);
				awardXP(event, xpAward);
			}
		}
		
		
		
		/*XpUtils.setXpDiff(event.getPlayer().getUUID(), "testing", 20);
		long xpNow = XpUtils.getLevelFromXP(XpUtils.getPlayerXpRaw(event.getPlayer().getUUID(), "testing"));
		event.getPlayer().sendMessage(new TextComponent("Testing: " + xpNow), event.getPlayer().getUUID());*/
	}
	
	private static boolean canPerform(BreakEvent event) {
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (PredicateRegistry.predicateExists(blockID, ReqType.REQ_BREAK)) 
			return PredicateRegistry.checkPredicateReq(event.getPlayer(), blockID, ReqType.REQ_BREAK);
		else if (SkillGates.doesObjectReqExist(ReqType.REQ_BREAK, blockID))
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.REQ_BREAK, event.getState());
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID(), requirements);
		}

		return true;
	}
	
	private static CompoundTag getEventHookResults(BreakEvent event) {
		return EventTriggerRegistry.executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
	}
	
	private static Map<String, Long> calculateXpAward(BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> xpGains = new HashMap<>();
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (TooltipRegistry.xpGainTooltipExists(blockID, EventType.BLOCK_BREAK) || event.getWorld().getBlockEntity(event.getPos()) != null)
			xpGains = TooltipRegistry.getBlockXpGainTooltipData(blockID, EventType.BLOCK_BREAK, event.getWorld().getBlockEntity(event.getPos()));
		else if (XpUtils.hasXpGainObjectEntry(EventType.BLOCK_BREAK, blockID))
			xpGains = XpUtils.getObjectExperienceMap(EventType.BLOCK_BREAK, blockID);
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			xpGains = AutoValues.getExperienceAward(EventType.BLOCK_BREAK, event.getState());
		}

		//Apply bonuses and penalties
		//	-apply anti-cheese 
		//Distribute Party XP
		
		return xpGains;
	}
	
	private static void awardXP(BreakEvent event, Map<String, Long> xpValues) {
		//Post to event bus
		//award xp and notify
		for (Map.Entry<String, Long> award : xpValues.entrySet()) {
			XpEvent xpEvent = new XpEvent((ServerPlayer) event.getPlayer(), award.getKey(), award.getValue(), new CompoundTag());
			if (!MinecraftForge.EVENT_BUS.post(xpEvent))
				XpUtils.setXpDiff(event.getPlayer().getUUID(), xpEvent.skill, xpEvent.amount);
		}
	}
}
