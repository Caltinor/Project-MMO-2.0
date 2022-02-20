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
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
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
		CompoundTag perkDataIn = eventHookOutput; //TODO this could just be passed as is in all cases.
		//if break data is needed by perks, we can add it here.  this is just default implementation.
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, player, perkDataIn));
		if (serverSide) {
			double jumpXpBase = perkOutput.contains(APIUtils.JUMP_OUT) ? perkOutput.getDouble(APIUtils.JUMP_OUT) : player.getDeltaMovement().y;
			Double xpValue = jumpModifier(type) * jumpXpBase;
			Map<String, Long> xpAward = new HashMap<>();
			jumpSkills(type).get().forEach((skill) -> {
				xpAward.put(skill, xpValue.longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
	
	private static double jumpModifier(EventType type) {
		return type.equals(EventType.JUMP) ? Config.JUMP_MODIFIER.get() 
				: type.equals(EventType.SPRINT_JUMP) ? Config.SPRINT_JUMP_MODIFIER.get() 
						: type.equals(EventType.CROUCH_JUMP) ? Config.CROUCH_JUMP_MODIFIER.get() : 2.5;  
	}
	
	private static ConfigValue<List<? extends String>> jumpSkills(EventType type) {
		return type.equals(EventType.JUMP) ? Config.JUMP_SKILLS 
				: type.equals(EventType.SPRINT_JUMP) ? Config.SPRINT_JUMP_SKILLS : Config.CROUCH_JUMP_SKILLS;
	}
}
