package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.FurnaceBurnEvent;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


public class FurnaceHandler {

	@SuppressWarnings("resource")
	public static void handle(FurnaceBurnEvent event) {
		//Checkers to exit early for non-applicable conditions
		if (event.getLevel().isClientSide) return;
		IChunkData cap = event.getLevel().getChunkAt(event.getPos()).getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new);
		UUID pid = cap.checkPos(event.getPos());
		if (pid == null) return;
		ServerPlayer player = event.getLevel().getServer().getPlayerList().getPlayer(pid);
		if (player == null) {
			Optional<GameProfile> playerProfile = event.getLevel().getServer().getProfileCache().get(pid);
			if (playerProfile.isEmpty()) 
				return;
			player = new ServerPlayer(event.getLevel().getServer(), (ServerLevel) event.getLevel(), playerProfile.get(), null);
		}
		
		//core logic 
		Core core = Core.get(event.getLevel());
		CompoundTag eventHook = core.getEventTriggerRegistry().executeEventListeners(EventType.SMELT, event, new CompoundTag());
		eventHook.putString(APIUtils.STACK, event.getInput().serializeNBT().getAsString());
		eventHook = TagUtils.mergeTags(eventHook, core.getPerkRegistry().executePerk(EventType.SMELT, player, eventHook, core.getSide()));
		Map<String, Long> xpAwards = core.getExperienceAwards(EventType.SMELT, event.getInput(), player, eventHook);
		List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange(player);
		core.awardXP(partyMembersInRange, xpAwards);
		
	}
}
