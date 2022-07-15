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
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		boolean serverSide = !event.getPlayer().level.isClientSide;
		if (!core.isBlockActionPermitted(ReqType.BREAK, event.getPos(), event.getPlayer())) {
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.BREAK, event.getPlayer(), event.getState().getBlock().getName());
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
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_BREAK, event.getPlayer(), eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = calculateXpAward(core, event, perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);
			//update ChunkData to remove the block from the placed map
			//if a cascading breaking crop block, add the breaker value.
			LevelChunk chunk = (LevelChunk) event.getLevel().getChunk(event.getPos());
			chunk.getCapability(ChunkDataProvider.CHUNK_CAP).ifPresent(cap -> {
				cap.delPos(event.getPos());
				if (event.getLevel().getBlockState(event.getPos()).is(Reference.CASCADING_BREAKABLES))
					cap.setBreaker(event.getPos(), event.getPlayer().getUUID());
			});;
			chunk.setUnsaved(true);
		}
		//==============Process Vein Miner Logic==================
		if (core.getVeinData().getMarkedPos(event.getPlayer().getUUID()).equals(event.getPos())) {
			BlockState block = event.getLevel().getBlockState(event.getPos());
			if (event.getPlayer().getMainHandItem().getItem() instanceof TieredItem) {
				TieredItem item = (TieredItem) event.getPlayer().getMainHandItem().getItem();
				if (item.isCorrectToolForDrops(event.getPlayer().getMainHandItem(), block)) 
					VeinMiningLogic.applyVein((ServerPlayer) event.getPlayer(), event.getPos());
			}
		}
	}
	
	private static Map<String, Long> calculateXpAward(Core core, BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> outMap = core.getBlockExperienceAwards(EventType.BLOCK_BREAK, event.getPos(), (Level)event.getLevel(), event.getPlayer(), dataIn); 
		LevelChunk chunk = (LevelChunk) event.getLevel().getChunk(event.getPos());
		IChunkData cap = chunk.getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new);
		if (cap.playerMatchesPos(event.getPlayer(), event.getPos())) {
			//Do not apply self-place penalty to crops
			BlockState cropState = event.getLevel().getBlockState(event.getPos()); 
			if (cropState.getBlock() instanceof CropBlock && ((CropBlock)cropState.getBlock()).isMaxAge(cropState))
				return outMap;
			double xpModifier = Config.REUSE_PENALTY.get();
			if (xpModifier == 0) return new HashMap<>();
			Map<String, Long> modifiedOutMap = new HashMap<>();
			outMap.forEach((k, v) -> modifiedOutMap.put(k, (long)((double)v * xpModifier)));
			return modifiedOutMap;
		}
		return outMap;
	}
}
