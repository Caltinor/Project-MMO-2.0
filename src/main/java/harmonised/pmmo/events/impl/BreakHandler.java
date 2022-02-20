package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
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
		boolean serverSide = !event.getPlayer().level.isClientSide;
		if (!core.isBlockActionPermitted(ReqType.BREAK, event.getPos(), event.getPlayer())) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
			return;
		}
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BLOCK_BREAK, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		//Process perks on both sides. note that only server-side perks will affect xp outputs.
		CompoundTag perkDataIn = eventHookOutput;
		//if break data is needed by perks, we can add it here.  this is just default implementation.
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_BREAK, event.getPlayer(), perkDataIn));
		if (serverSide) {
			Map<String, Long> xpAward = calculateXpAward(core, event, perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);
			//update ChunkData to remove the block from the placed map
			ChunkDataHandler.delPos(event.getPlayer().getLevel().dimension().getRegistryName(), event.getPos());
		}
	}
	
	private static Map<String, Long> calculateXpAward(Core core, BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> outMap = core.getBlockExperienceAwards(EventType.BLOCK_BREAK, event.getPos(), event.getPlayer(), dataIn); 
		if (ChunkDataHandler.playerMatchesPos(event.getPlayer(), event.getPos())) {
			double xpModifier = Config.REUSE_PENALTY.get();
			if (xpModifier == 0) return new HashMap<>();
			Map<String, Long> modifiedOutMap = new HashMap<>();
			outMap.forEach((k, v) -> modifiedOutMap.put(k, (long)((double)v * xpModifier)));
			return modifiedOutMap;
		}
		return outMap;
	}
}
