package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;

public class AnvilRepairHandler {

	public static void handle(AnvilRepairEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !event.getPlayer().level.isClientSide; 
		if (serverSide)		
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ANVIL_REPAIR, event, new CompoundTag());
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.ANVIL_REPAIR, event.getPlayer(), eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ANVIL_REPAIR, event.getItemResult(), event.getPlayer(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
