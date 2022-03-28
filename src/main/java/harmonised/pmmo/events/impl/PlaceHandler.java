package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;

public class PlaceHandler {
	public static void handle(EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		Core core = Core.get(event.getEntity().getLevel());
		if (!core.isBlockActionPermitted(ReqType.PLACE, event.getPos(), player)) {
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.PLACE, player, event.getPlacedBlock().getBlock().getName());
			return;
		}
		boolean serverSide = !player.level.isClientSide;
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BLOCK_PLACE, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
				event.setCanceled(true);
				return;
			}
		}
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_PLACE, player, eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.BLOCK_PLACE, event.getPos(),(Level) event.getWorld(), (Player) event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
			//Add the newly placed block to the ChunkDataHandler
			ChunkDataHandler.addPos(player.getLevel().dimension(), event.getPos(), player.getUUID());
			event.getWorld().getChunk(event.getPos()).setUnsaved(true);
		}
	}
}
