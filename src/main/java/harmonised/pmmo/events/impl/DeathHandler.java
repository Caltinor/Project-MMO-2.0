package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;

public class DeathHandler {

	@SuppressWarnings("resource")
	public static void handle(LivingDeathEvent event) {
		//Check the source entity isn't null.  This should also reduce
		//the number of events processed.
		if (event.getSource().getEntity() == null) return;
		
		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player player) {
			LivingEntity target = event.getEntity();
			//Confirm our target is a living entity
			if (target == null) return;

			if (target.equals(player))
				return;
			Core core = Core.get(player.level());			
			
			//===========================DEFAULT LOGIC===================================
			if (!core.isActionPermitted(ReqType.WEAPON, player.getMainHandItem(), player)) {
				event.setCanceled(true);
				Messenger.sendDenialMsg(ReqType.WEAPON, player, player.getMainHandItem().getDisplayName());
				return;
			}
			if (!core.isActionPermitted(ReqType.KILL, target, player)) {
				event.setCanceled(true);
				Messenger.sendDenialMsg(ReqType.KILL, player, target.getName());
				return;
			}
			boolean serverSide = !player.level().isClientSide;
			CompoundTag hookOutput = new CompoundTag();
			if (serverSide) {
				hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.DEATH, event, new CompoundTag());
				if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
					event.setCanceled(true);
					return;
				}
			}
			//Process perks
			hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.DEATH, player, hookOutput));
			if (serverSide) {
				Map<String, Long> xpAward = core.getExperienceAwards(EventType.DEATH, target, player, hookOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
}
