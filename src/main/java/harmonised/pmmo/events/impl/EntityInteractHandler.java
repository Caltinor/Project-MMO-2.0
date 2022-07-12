package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;

public class EntityInteractHandler {
	
	public static void handle(EntityInteract event) {
		Core core = Core.get(event.getEntity().level);
		if (!core.isActionPermitted(ReqType.ENTITY_INTERACT, event.getTarget(), event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
			Messenger.sendDenialMsg(ReqType.ENTITY_INTERACT, event.getEntity(), event.getTarget().getName());
			return;
		}
		boolean serverSide = !event.getEntity().level.isClientSide;
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENTITY, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.FAIL);
				return;
			}
		}
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.ENTITY,  event.getEntity(), eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENTITY, event.getTarget(), event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
