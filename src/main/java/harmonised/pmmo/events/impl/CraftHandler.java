package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;

public class CraftHandler {
	public static void handle(ItemCraftedEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !event.getPlayer().level.isClientSide; 
		if (serverSide)		
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.CRAFT, event, new CompoundTag());
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.CRAFT, event.getPlayer(), eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.CRAFT, event.getCrafting(), event.getPlayer(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
