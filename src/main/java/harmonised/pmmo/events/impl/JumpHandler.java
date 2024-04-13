package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JumpHandler {

	@SuppressWarnings("resource")
	public static void handle(LivingEvent.LivingJumpEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		
		EventType type = EventType.JUMP;
		if (player.isSprinting()) type = EventType.SPRINT_JUMP;
		else if (player.isCrouching()) type = EventType.CROUCH_JUMP;
		
		Core core = Core.get(player.level());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !player.level().isClientSide; 
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		}
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, player, eventHookOutput));
		if (serverSide) {
			//NOTE a default value of 0.4 is consistent with the base jump amount of a player without any vanilla/modded jump modifiers
			double jumpXpBase = perkOutput.contains(APIUtils.JUMP_OUT) ? Math.max(0.4, perkOutput.getDouble(APIUtils.JUMP_OUT)) : player.getDeltaMovement().y;
			Map<String, Long> xpAward = new HashMap<>();
			Map<String, Double> ratios = Config.server().xpGains().playerXp(type);
			ratios.keySet().forEach((skill) -> {
				Double xpValue = ratios.getOrDefault(skill, 2.5) * jumpXpBase * core.getConsolidatedModifierMap(player).getOrDefault(skill, 1d);
				xpAward.put(skill, xpValue.longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
