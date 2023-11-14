package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.PerksImpl;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;

public class TameHandler {

	@SuppressWarnings("resource")
	public static void handle(AnimalTameEvent event) {
		Core core = Core.get(event.getTamer().level());
		Player player = event.getTamer();
		Animal target = event.getAnimal();

		if (!core.isActionPermitted(ReqType.TAME, target, player)) {
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.TAME, player, target.getName());
			return;
		}
		boolean serverSide = !player.level().isClientSide;
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.TAMING, event, new CompoundTag());
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		//Process perks
		hookOutput.putUUID(PerksImpl.ANIMAL_ID, event.getAnimal().getUUID());
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.TAMING, player, hookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.TAMING, target, player, hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
