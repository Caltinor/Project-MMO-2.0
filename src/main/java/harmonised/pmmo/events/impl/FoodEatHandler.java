package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.EatFoodEvent;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class FoodEatHandler {

	@SuppressWarnings("resource")
	public static void handle(EatFoodEvent event) {
		Core core = Core.get(event.getEntity().level());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !event.getEntity().level().isClientSide; 
		if (serverSide)		
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.CONSUME, event, new CompoundTag());
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.CONSUME, event.getEntity(), eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.CONSUME, event.getFood(), event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
