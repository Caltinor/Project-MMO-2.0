package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.setup.Core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

public class BreakSpeedHandler {
	private static Map<UUID, DetailsCache> resultCache = new HashMap<>();
	
	private record DetailsCache(ItemStack item, BlockPos pos, BlockState state, boolean cancelled, float newSpeed) {}
	
	public static void handle(BreakSpeed event) {
		Core core = Core.get(event.getEntity().getLevel());
		System.out.println("Server BreakSpeed: "+event.getNewSpeed());
		//First, check the cache for a repeat event trigger
		if (resultCache.containsKey(event.getPlayer().getUUID())) {
			if (usingCache(event)) return;
		}
		//calculate the event results anew.
		if (!canUseTool(core, event)) {
			event.setCanceled(true);
			event.getPlayer().displayClientMessage(new TextComponent("Unable to use this tool"), false);
			//TODO Notify player of inability to perform.
			//Cache the result for future event occurrences
			resultCache.put(event.getPlayer().getUUID(), 
					new DetailsCache(event.getPlayer().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
			return;
		}
		if (!canPerform(core, event)) {
			event.setCanceled(true);
			event.getPlayer().displayClientMessage(new TextComponent("Unable to break this block"), false);
			//TODO Notify player of inability to perform.
			resultCache.put(event.getPlayer().getUUID(), 
					new DetailsCache(event.getPlayer().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
		}
		else {
			CompoundTag eventHookOutput = getEventHookResults(core, event);
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				resultCache.put(event.getPlayer().getUUID(), 
						new DetailsCache(event.getPlayer().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
			}
			else {
				CompoundTag perkDataIn = eventHookOutput;
				perkDataIn.putFloat(APIUtils.BREAK_SPEED_INPUT_VALUE, event.getOriginalSpeed());
				perkDataIn.putLong(APIUtils.BLOCK_POS, event.getPos().asLong());
				//how am i gonna do gaps?  hmmmm
				CompoundTag perkDataOut = core.getPerkRegistry().executePerk(EventType.BREAK_SPEED, (ServerPlayer) event.getPlayer(), perkDataIn);
				if (perkDataOut.contains(APIUtils.BREAK_SPEED_OUTPUT_VALUE)) {
					float newSpeed = Math.max(0, perkDataOut.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE));
					event.setNewSpeed(newSpeed);
					resultCache.put(event.getPlayer().getUUID(), 
							new DetailsCache(event.getPlayer().getMainHandItem(), event.getPos(), event.getState(), false, event.getNewSpeed()));
				}
			}
		}
	}
	
	private static boolean canUseTool(Core core, BreakSpeed event) {
		ResourceLocation toolID = event.getPlayer().getMainHandItem().getItem().getRegistryName();
		if (core.getPredicateRegistry().predicateExists(toolID, ReqType.TOOL))
			return core.getPredicateRegistry().checkPredicateReq(event.getPlayer(), toolID, ReqType.TOOL);
		else if (core.getSkillGates().doesObjectReqExist(ReqType.TOOL, toolID))
			return core.getSkillGates().doesPlayerMeetReq(ReqType.TOOL, toolID, event.getPlayer().getUUID());
		else if (Config.ENABLE_AUTO_VALUES.get()) {
			Map<String, Integer> requirements = AutoValues.getRequirements(ReqType.TOOL, toolID, ObjectType.BLOCK);
			return core.getSkillGates().doesPlayerMeetReq(ReqType.TOOL, toolID, event.getPlayer().getUUID(), requirements);
		} 
		return true;
	}
	
	private static boolean canPerform(Core core, BreakSpeed event) {		
		ResourceLocation blockID = event.getState().getBlock().getRegistryName();
		if (core.getPredicateRegistry().predicateExists(blockID, ReqType.BREAK)) {
			BlockEntity tile = event.getPlayer().getLevel().getBlockEntity(event.getPos());
			return tile == null ? 
					core.getPredicateRegistry().checkPredicateReq(event.getPlayer(), blockID, ReqType.BREAK):
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
	
	private static CompoundTag getEventHookResults(Core core, BreakSpeed event) {
		return core.getEventTriggerRegistry().executeEventListeners(EventType.BREAK_SPEED, event, new CompoundTag());
	}

	private static boolean usingCache(BreakSpeed event) {
		DetailsCache cachedData = resultCache.get(event.getPlayer().getUUID());
		if (event.getPos().equals(cachedData.pos)
			&& event.getState().equals(cachedData.state)
			&& event.getPlayer().getMainHandItem().equals(cachedData.item, false)) {			
			if (cachedData.cancelled) 
				event.setCanceled(true);
			else 
				event.setNewSpeed(cachedData.newSpeed);
			return true;
		}
		return false;
	}
}
