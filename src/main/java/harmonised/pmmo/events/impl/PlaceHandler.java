package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;

public class PlaceHandler {
	public static void handle(EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		Core core = Core.get(event.getEntity().getLevel());
		if (!core.isBlockActionPermitted(ReqType.PLACE, event.getPos(), player)) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
		}
		else if (!player.level.isClientSide){
			CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BLOCK_PLACE, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				CompoundTag perkDataIn = eventHookOutput;
				//if break data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_PLACE, (ServerPlayer) event.getEntity(), perkDataIn));
				Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.BLOCK_PLACE, event.getPos(), (Player) event.getEntity(), perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
				//Add the newly placed block to the ChunkDataHandler
				ChunkDataHandler.addPos(player.getLevel().dimension().getRegistryName(), event.getPos(), player.getUUID());
			}
		}
	}
}
