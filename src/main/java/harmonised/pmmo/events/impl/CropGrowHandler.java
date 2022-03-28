package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;

public class CropGrowHandler {

	public static void handle(CropGrowEvent.Post event) {
		Level level = (Level) event.getWorld();
		Core core = Core.get(level);
		UUID placerID = level.getChunkAt(event.getPos()).getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new).checkPos(event.getPos());
		ServerPlayer player = event.getWorld().getServer().getPlayerList().getPlayer(placerID);
		CompoundTag hookOutput = new CompoundTag();
		if (!level.isClientSide)
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.GROW, event, new CompoundTag());
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.GROW, player, core.getSide()));
		if (!level.isClientSide && placerID != null) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.GROW, event.getPos(), level, player, hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
