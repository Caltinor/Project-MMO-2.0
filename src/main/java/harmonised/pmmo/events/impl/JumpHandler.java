package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;

public class JumpHandler {

	public static void handle(LivingJumpEvent event) {
		if (!(event.getEntityLiving() instanceof Player)) return;
		Player player = (Player) event.getEntityLiving();
		
		EventType type = EventType.JUMP;
		if (player.isSprinting()) type = EventType.SPRINT_JUMP;
		else if (player.isCrouching()) type = EventType.CROUCH_JUMP;
		
		Core core = Core.get(player.getLevel());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !player.level.isClientSide; 
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, player, eventHookOutput, core.getSide()));
		if (serverSide) {
			//NOTE a default value of 0.4 is consistent with the base jump amount of a player without any vanilla/modded jump modifiers
			double jumpXpBase = perkOutput.contains(APIUtils.JUMP_OUT) ? Math.max(0.4, perkOutput.getDouble(APIUtils.JUMP_OUT)) : player.getDeltaMovement().y;
			Map<String, Long> xpAward = new HashMap<>();
			Map<String, Double> ratios = getRatioMap(type);
			ratios.keySet().forEach((skill) -> {
				Double xpValue = ratios.getOrDefault(skill, 2.5) * jumpXpBase;
				xpAward.put(skill, xpValue.longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
	
	private static Map<String, Double> getRatioMap(EventType type) {
		return type.equals(EventType.JUMP) ? Config.JUMP_XP.get() 
				: type.equals(EventType.SPRINT_JUMP) ? Config.SPRINT_JUMP_XP.get() 
						: type.equals(EventType.CROUCH_JUMP) ? Config.CROUCH_JUMP_XP.get() : new HashMap<>();  
	}
}
