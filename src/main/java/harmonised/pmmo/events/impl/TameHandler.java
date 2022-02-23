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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

public class TameHandler {

	public static void handle(AnimalTameEvent event) {
		Core core = Core.get(event.getTamer().level);
		Player player = event.getTamer();
		Animal target = event.getAnimal();

		if (!core.isActionPermitted(ReqType.TAME, target, player)) {
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.TAME, player, target.getName());
		}
		boolean serverSide = !player.level.isClientSide;
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.DEATH, event, new CompoundTag());
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
		}
		//Process perks
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.DEATH, player, hookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.DEATH, target, player, hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
