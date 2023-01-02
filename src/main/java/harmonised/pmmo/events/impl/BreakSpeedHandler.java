package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

public class BreakSpeedHandler {
	private static Map<UUID, DetailsCache> resultCache = new HashMap<>();
	
	private record DetailsCache(ItemStack item, BlockPos pos, BlockState state, boolean isPlayerStanding, boolean cancelled, float newSpeed) {
		public DetailsCache(ItemStack item, BlockPos pos, BlockState state, boolean cancelled, float newSpeed) {
			this(item, pos, state, true, cancelled, newSpeed);
		}
	}
	
	public static void handle(BreakSpeed event) {
        Core core = Core.get(event.getEntity().getLevel());
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        //First, check the cache for a repeat event trigger
        if (resultCache.containsKey(player.getUUID())) {
            if (usingCache(event)) {
                MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Cache Used. Supplied: {}", event.getNewSpeed());
                return;
            }
        }
        //calculate the event results anew.
        if (!core.isActionPermitted(ReqType.TOOL, player.getMainHandItem(), player)) {
            event.setCanceled(true);
            player.displayClientMessage(new TextComponent("Unable to use this tool"), false);
            Messenger.sendDenialMsg(ReqType.TOOL, player, player.getMainHandItem().getDisplayName());
            //Cache the result for future event occurrences
            resultCache.put(player.getUUID(), 
                    new DetailsCache(player.getMainHandItem(), event.getPos(), event.getState(), true, event.getNewSpeed()));
            return;
        }
        if (!core.isActionPermitted(ReqType.BREAK, event.getPos(), player)) {
            event.setCanceled(true);
            player.displayClientMessage(new TextComponent("Unable to break this block"), false);
            Messenger.sendDenialMsg(ReqType.BREAK, player, event.getState().getBlock().getName());
            resultCache.put(player.getUUID(), 
                    new DetailsCache(player.getMainHandItem(), event.getPos(), event.getState(), true, event.getNewSpeed()));
            return;
        }
        CompoundTag eventHookOutput = new CompoundTag();
        if (!player.level.isClientSide){
            eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BREAK_SPEED, event, new CompoundTag());
            if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
                event.setCanceled(true);
                resultCache.put(player.getUUID(), 
                        new DetailsCache(player.getMainHandItem(), event.getPos(), event.getState(), true, event.getNewSpeed()));
                return;
            }
        }
        CompoundTag perkDataIn = eventHookOutput;
        perkDataIn.putFloat(APIUtils.BREAK_SPEED_INPUT_VALUE, event.getNewSpeed());
        perkDataIn.putLong(APIUtils.BLOCK_POS, event.getPos().asLong());

        CompoundTag perkDataOut = core.getPerkRegistry().executePerk(EventType.BREAK_SPEED, player, perkDataIn, core.getSide());
        if (perkDataOut.contains(APIUtils.BREAK_SPEED_OUTPUT_VALUE)) {
            float newSpeed = Math.max(0, perkDataOut.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE));
            event.setNewSpeed(newSpeed);
            resultCache.put(player.getUUID(), 
                    new DetailsCache(player.getMainHandItem(), event.getPos(), event.getState(), player.isOnGround(), false, newSpeed));
        }           
    }

    private static boolean usingCache(BreakSpeed event) {
        DetailsCache cachedData = resultCache.get(event.getEntity().getUUID());
        if (event.getEntity().isOnGround() == cachedData.isPlayerStanding()
            &&event.getPos().equals(cachedData.pos)
            && event.getState().equals(cachedData.state)
            && ((Player)event.getEntity()).getMainHandItem().equals(cachedData.item, false)) {          
            if (cachedData.cancelled) 
                event.setCanceled(true);
            else 
                event.setNewSpeed(cachedData.newSpeed);
            return true;
        }
        return false;
    }
}
