package harmonised.pmmo.events.impl;

import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

public class BreakSpeedHandler {
	public static void handle(BreakSpeed event) {
		if (!canUseTool(event)) {
			event.setCanceled(true);
			event.getPlayer().displayClientMessage(new TextComponent("Unable to use this tool"), false);
			//TODO Notify player of inability to perform.
			return;
		}
		if (!canPerform(event)) {
			event.setCanceled(true);
			event.getPlayer().displayClientMessage(new TextComponent("Unable to break this block"), false);
			//TODO Notify player of inability to perform.
		}
		else {
			CompoundTag eventHookOutput = getEventHookResults(event);
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				CompoundTag perkDataIn = eventHookOutput;
				perkDataIn.putFloat(APIUtils.BREAK_SPEED_INPUT_VALUE, event.getOriginalSpeed());
				perkDataIn.putLong(APIUtils.BLOCK_POS, event.getPos().asLong());
				//how am i gonna do gaps?  hmmmm
				CompoundTag perkDataOut = PerkRegistry.executePerk(EventType.BREAK_SPEED, (ServerPlayer) event.getPlayer(), perkDataIn);
				if (perkDataOut.contains(APIUtils.BREAK_SPEED_OUTPUT_VALUE)) {
					event.setNewSpeed(perkDataOut.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE));
				}
			}
		}
	}
	
	private static boolean canUseTool(BreakSpeed event) {
		ResourceLocation toolID = event.getPlayer().getMainHandItem().getItem().getRegistryName();
		if (PredicateRegistry.predicateExists(toolID, ReqType.REQ_TOOL))
			return PredicateRegistry.checkPredicateReq(event.getPlayer(), toolID, ReqType.REQ_TOOL);
		else if (SkillGates.doesObjectReqExist(ReqType.REQ_TOOL, toolID))
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_TOOL, toolID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.REQ_TOOL, toolID, ObjectType.BLOCK);
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_TOOL, toolID, event.getPlayer().getUUID(), requirements);
		} 
		return true;
	}
	
	private static boolean canPerform(BreakSpeed event) {		
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (PredicateRegistry.predicateExists(blockID, ReqType.REQ_BREAK)) {
			BlockEntity tile = event.getPlayer().getLevel().getBlockEntity(event.getPos());
			return tile == null ? 
					PredicateRegistry.checkPredicateReq(event.getPlayer(), blockID, ReqType.REQ_BREAK):
					PredicateRegistry.checkPredicateReq(event.getPlayer(), tile, ReqType.REQ_BREAK);
		}
		else if (SkillGates.doesObjectReqExist(ReqType.REQ_BREAK, blockID))
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.REQ_BREAK, blockID, ObjectType.BLOCK);
			return SkillGates.doesPlayerMeetReq(ReqType.REQ_BREAK, blockID, event.getPlayer().getUUID(), requirements);
		}

		return true;
	}
	
	private static CompoundTag getEventHookResults(BreakSpeed event) {
		return EventTriggerRegistry.executeEventListeners(EventType.BREAK_SPEED, event, new CompoundTag());
	}
}
