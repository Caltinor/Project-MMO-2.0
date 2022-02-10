package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		if (!core.isBlockActionPermitted(ReqType.BREAK, event.getPos(), event.getPlayer())) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
		}
		else if (!event.getPlayer().level.isClientSide){
			CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				//proecess perks
				CompoundTag perkDataIn = eventHookOutput;
				//if break data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_BREAK, (ServerPlayer) event.getPlayer(), perkDataIn));
				Map<String, Long> xpAward = calculateXpAward(core, event, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
				core.awardXP(partyMembersInRange, xpAward);
				//update ChunkData to remove the block from the placed map
				ChunkDataHandler.delPos(event.getPlayer().getLevel().dimension().getRegistryName(), event.getPos());
			}
		}
	}
	
	private static Map<String, Long> calculateXpAward(Core core, BreakEvent event, CompoundTag dataIn) {
		//TODO decided if this should be NO XP as is, or reduced XP
		if (ChunkDataHandler.playerMatchesPos(event.getPlayer(), event.getPos()))
			return new HashMap<>();		
		return core.getBlockExperienceAwards(EventType.BLOCK_BREAK, event.getPos(), event.getPlayer(), dataIn);
	}
}
