package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

public class EntityInteractHandler {
	
	@SuppressWarnings("resource")
	public static void handle(PlayerInteractEvent.EntityInteract event) {
		Core core = Core.get(event.getEntity().level());
		if (!core.isActionPermitted(ReqType.ENTITY_INTERACT, event.getTarget(), event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
			Messenger.sendDenialMsg(ReqType.ENTITY_INTERACT, event.getEntity(), event.getTarget().getName());
			return;
		}
		boolean serverSide = !event.getEntity().level().isClientSide();
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENTITY, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED).get()) {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.FAIL);
				return;
			}
		}
		//Process perks
		eventHookOutput.putString(APIUtils.TARGET, RegistryUtil.getId(event.getTarget()).toString());
		eventHookOutput.putInt(APIUtils.ENTITY_ID, event.getTarget().getId());
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.ENTITY,  event.getEntity(), eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENTITY, event.getTarget(), event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
