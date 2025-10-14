package harmonised.pmmo.events.impl;

import com.mojang.authlib.GameProfile;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.FurnaceBurnEvent;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;


public class FurnaceHandler {

	public static void handle(FurnaceBurnEvent event) {
		//Checkers to exit early for non-applicable conditions
		if (event.getLevel().isClientSide) return;
		UUID pid = event.getLevel().getChunkAt(event.getPos())
				.getData(DataAttachmentTypes.PLACED_MAP.get())
				.getOrDefault(event.getPos(), Reference.NIL);
		if (pid == null) return;
		ServerPlayer player = event.getLevel().getServer().getPlayerList().getPlayer(pid);
		if (player == null) {
			Optional<GameProfile> playerProfile = event.getLevel().getServer().getProfileCache().get(pid);
			if (playerProfile.isEmpty()) 
				return;
			player = new ServerPlayer(event.getLevel().getServer(), (ServerLevel) event.getLevel(), playerProfile.get(), ClientInformation.createDefault());
		}
		
		//core logic
		Core core = Core.get(event.getLevel());
		CompoundTag eventHook = core.getEventTriggerRegistry().executeEventListeners(EventType.SMELT, event, new CompoundTag());
		Map<String, Long> xpAwards = new HashMap<>();

		if(event.getInput() != null) {
			eventHook.putString(APIUtils.STACK, TagUtils.stackTag(event.getInput(), event.getLevel()).getAsString());
			eventHook = TagUtils.mergeTags(eventHook, core.getPerkRegistry().executePerk(EventType.SMELT, player, eventHook));
			core.getExperienceAwards(EventType.SMELT, event.getInput(), player, eventHook).forEach((skill, award) -> xpAwards.merge(skill, award, Long::sum));
		}

		if(event.getOutput() != null) {
			core.getExperienceAwards(EventType.SMELTED, event.getOutput(), player, eventHook).forEach((skill, award) -> xpAwards.merge(skill, award, Long::sum));
		}

		if(!xpAwards.isEmpty()) {
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange(player);
			core.awardXP(partyMembersInRange, xpAwards);
		}
	}
}
