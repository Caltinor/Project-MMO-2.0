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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class FurnaceHandler {

	public static void handle(FurnaceBurnEvent event) {
		//Checkers to exit early for non-applicable conditions
		if (event.getLevel().isClientSide()) return;
		UUID pid = event.getLevel().getChunkAt(event.getPos())
				.getData(DataAttachmentTypes.PLACED_MAP.get())
				.getOrDefault(event.getPos(), Reference.NIL);
		if (pid == null) return;
		ServerPlayer player = event.getLevel().getServer().getPlayerList().getPlayer(pid);
		if (player == null) {
			return;
		}
		
		//core logic 
		Core core = Core.get(event.getLevel());
		CompoundTag eventHook = core.getEventTriggerRegistry().executeEventListeners(EventType.SMELT, event, new CompoundTag());
		eventHook.putString(APIUtils.STACK, TagUtils.stackTag(event.getInput(), event.getLevel()).asString().orElse("missing"));
		eventHook = TagUtils.mergeTags(eventHook, core.getPerkRegistry().executePerk(EventType.SMELT, player, eventHook));
		Map<String, Long> xpAwards = core.getExperienceAwards(EventType.SMELT, event.getInput(), player, eventHook);
		core.getExperienceAwards(EventType.SMELTED, event.getOutput(), player, eventHook).forEach((skill, award) -> xpAwards.merge(skill, award, Long::sum));
		List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange(player);
		core.awardXP(partyMembersInRange, xpAwards);
		
	}
}
