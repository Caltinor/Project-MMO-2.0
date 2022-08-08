package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Messenger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

public class BreakSpeedHandler {
	private static Map<UUID, DetailsCache> resultCache = new HashMap<>();
	
	private record DetailsCache(ItemStack item, BlockPos pos, BlockState state, boolean cancelled, float newSpeed) {}
	
	public static void handle(BreakSpeed event) {
		Core core = Core.get(event.getEntity().getLevel());
		//First, check the cache for a repeat event trigger
		if (resultCache.containsKey(event.getEntity().getUUID())) {
			if (usingCache(event)) return;
		}
		//calculate the event results anew.
		if (!core.isActionPermitted(ReqType.TOOL, event.getEntity().getMainHandItem(), event.getEntity())) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.literal("Unable to use this tool"), false);
			Messenger.sendDenialMsg(ReqType.TOOL, event.getEntity(), event.getEntity().getMainHandItem().getDisplayName());
			//Cache the result for future event occurrences
			resultCache.put(event.getEntity().getUUID(), 
					new DetailsCache(event.getEntity().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
			return;
		}
		if (!core.isBlockActionPermitted(ReqType.BREAK, event.getPos(), event.getEntity())) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.literal("Unable to break this block"), false);
			Messenger.sendDenialMsg(ReqType.BREAK, event.getEntity(), event.getState().getBlock().getName());
			resultCache.put(event.getEntity().getUUID(), 
					new DetailsCache(event.getEntity().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
			return;
		}
		CompoundTag eventHookOutput = new CompoundTag();
		if (!event.getEntity().level.isClientSide){
			eventHookOutput = getEventHookResults(core, event);
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				resultCache.put(event.getEntity().getUUID(), 
						new DetailsCache(event.getEntity().getMainHandItem(), event.getPos(), event.getState(), true, event.getOriginalSpeed()));
				return;
			}
		}
		CompoundTag perkDataIn = eventHookOutput;
		perkDataIn.putFloat(APIUtils.BREAK_SPEED_INPUT_VALUE, event.getOriginalSpeed());
		perkDataIn.putLong(APIUtils.BLOCK_POS, event.getPos().asLong());

		CompoundTag perkDataOut = core.getPerkRegistry().executePerk(EventType.BREAK_SPEED, event.getEntity(), perkDataIn, core.getSide());
		if (perkDataOut.contains(APIUtils.BREAK_SPEED_OUTPUT_VALUE)) {
			float newSpeed = Math.max(0, perkDataOut.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE));
			event.setNewSpeed(newSpeed);
			resultCache.put(event.getEntity().getUUID(), 
					new DetailsCache(event.getEntity().getMainHandItem(), event.getPos(), event.getState(), false, newSpeed));
		}			
	}
	
	private static CompoundTag getEventHookResults(Core core, BreakSpeed event) {
		return core.getEventTriggerRegistry().executeEventListeners(EventType.BREAK_SPEED, event, new CompoundTag());
	}

	private static boolean usingCache(BreakSpeed event) {
		DetailsCache cachedData = resultCache.get(event.getEntity().getUUID());
		if (event.getPos().equals(cachedData.pos)
			&& event.getState().equals(cachedData.state)
			&& event.getEntity().getMainHandItem().equals(cachedData.item, false)) {			
			if (cachedData.cancelled) 
				event.setCanceled(true);
			else 
				event.setNewSpeed(cachedData.newSpeed);
			return true;
		}
		return false;
	}
}
