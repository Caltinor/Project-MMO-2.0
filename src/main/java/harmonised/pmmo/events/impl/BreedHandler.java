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
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class BreedHandler {

	public static void handle(BabyEntitySpawnEvent event) {
		Player player = event.getCausedByPlayer();
		//This catches if animals are bred by fake players or TEs
		if (player == null || event.getChild() == null) return;
		Core core = Core.get(player.getLevel());

		if (!core.isActionPermitted(ReqType.BREED, event.getChild(), player)) {
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.BREED, player, event.getChild().getName());
			return;
		}
		boolean serverSide = !player.level.isClientSide;
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BREED, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
				event.setCanceled(true);
				return;
			}
		}
		//populate with dataIn if needed
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BREED, player, eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.BREED, event.getChild(), player, perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
