package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FurnaceHandler {

	public static void handle(ItemStack input, Level level, BlockPos pos) {
		//Checkers to exit early for non-applicable conditions
		if (level.isClientSide) return;
		UUID pid = ChunkDataHandler.checkPos(level, pos);
		if (pid == null) return;
		ServerPlayer player = level.getServer().getPlayerList().getPlayer(pid);
		if (player == null) {
			GameProfile playerProfile = level.getServer().getProfileCache().get(pid).orElseGet(null);
			if (playerProfile == null) return;
			player = new ServerPlayer(level.getServer(), (ServerLevel) level, playerProfile);
		}
		
		//core logic 
		Core core = Core.get(level);
		CompoundTag eventHook = core.getEventTriggerRegistry().executeEventListeners(EventType.SMELT, null, new CompoundTag());
		eventHook.putString(APIUtils.STACK, input.serializeNBT().getAsString());
		eventHook = TagUtils.mergeTags(eventHook, core.getPerkRegistry().executePerk(EventType.SMELT, player, eventHook, core.getSide()));
		Map<String, Long> xpAwards = core.getExperienceAwards(EventType.SMELT, input, player, eventHook);
		List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange(player);
		core.awardXP(partyMembersInRange, xpAwards);
		
	}
}
