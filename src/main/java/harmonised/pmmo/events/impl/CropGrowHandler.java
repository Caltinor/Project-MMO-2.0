package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent.CropGrowEvent;

public class CropGrowHandler {

	public static void handle(CropGrowEvent.Post event) {
		Level level = (Level) event.getLevel();
		if (!level.isClientSide) {
			Core core = Core.get(level);
			
			//if this block grew to exist because of a cascading block, return the block which spawned it
			BlockPos sourcePos = getParentPos(level, event.getState(), event.getPos());
			System.out.println(level.getBlockState(sourcePos).toString()+" | "+sourcePos.toString()); //TODO remove
			//get the owner of the source block to know who to give the grow XP to.
			ChunkDataHandler cap = (ChunkDataHandler) level.getChunkAt(sourcePos).getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new);
			UUID placerID = cap.checkPos(sourcePos);
			System.out.println(placerID.toString());
			ServerPlayer player = event.getLevel().getServer().getPlayerList().getPlayer(placerID);
			
			//if there is no owning player, return as there is no XP to give
			if (player == null) return;
			
			//this is a redundant call to make sure cascading blocks have their owner set.  for non-cascading it just reassigns the same value
			cap.addPos(event.getPos(), placerID);
			
			//Execute event triggers from addons
			CompoundTag hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.GROW, event, new CompoundTag());
			
			//Execute perks configured by the player
			hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.GROW, player, core.getSide()));

			//Apply experience gains
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.GROW, event.getPos(), level, player, hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange(player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
	
	/**Checks if the block at the provided position is a cascading growth crop.
	 * If true, looks to see if the source of the growth is above or below the
	 * current position and returns that location.  This is used in {@link handle}
	 * to get the owner data from the source block so the new growth can properly
	 * supply XP to the player
	 * 
	 * @param level	ServerLevel object
	 * @param state State of the new growth
	 * @param posIn Location of the new growth
	 * @return source block position
	 */
	private static BlockPos getParentPos(Level level, BlockState state, BlockPos posIn) {
		if (state.is(Reference.CASCADING_BREAKABLES)) 
			return level.getBlockState(posIn.above()).isAir() && level.getBlockState(posIn.below()).is(Reference.CASCADING_BREAKABLES)
					? posIn.below() 
					: level.getBlockState(posIn.below()).isAir() && level.getBlockState(posIn.above()).is(Reference.CASCADING_BREAKABLES)
						? posIn.above()
						: posIn;
		else
			return posIn;
	}
}
