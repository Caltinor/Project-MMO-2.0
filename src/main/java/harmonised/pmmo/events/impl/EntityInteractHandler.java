package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;

public class EntityInteractHandler {
	
	public static void handle(EntityInteract event) {
		Core core = Core.get(event.getPlayer().level);
		if (!core.isActionPermitted(ReqType.ENTITY_INTERACT, event.getTarget(), event.getPlayer())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
			//TODO notify player of inability to perform
		}
		else if (!event.getPlayer().level.isClientSide){
			CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENTITY, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.FAIL);
			}
			else {
				//proecess perks
				CompoundTag perkDataIn = eventHookOutput;
				//if interact data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.ENTITY, (ServerPlayer) event.getPlayer(), perkDataIn));
				Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENTITY, event.getTarget(), event.getPlayer(), perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
}
