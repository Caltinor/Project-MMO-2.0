package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BreakHandler {
	
	@SuppressWarnings("resource")
	public static void handle(BlockEvent.BreakEvent event) {
		Core core = Core.get(event.getPlayer().level());
		boolean serverSide = !event.getPlayer().level().isClientSide;
		if (!core.isActionPermitted(ReqType.BREAK, event.getPos(), event.getPlayer())) {
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
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BLOCK_BREAK, event.getPlayer(), eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = calculateXpAward(core, event, perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);
			//update ChunkData to remove the block from the placed map
			//if a cascading breaking crop block, add the breaker value.
			LevelChunk chunk = (LevelChunk) event.getLevel().getChunk(event.getPos());
			chunk.getData(DataAttachmentTypes.PLACED_MAP.get()).remove(event.getPos());
			if (event.getLevel().getBlockState(event.getPos()).is(Reference.CASCADING_BREAKABLES))
				chunk.getData(DataAttachmentTypes.BREAK_MAP.get()).put(event.getPos(), event.getPlayer().getUUID());
			chunk.setUnsaved(true);
		}
		//==============Process Vein Miner Logic==================
		if (core.getMarkedPos(event.getPlayer().getUUID()).equals(event.getPos())) {
			BlockState block = event.getLevel().getBlockState(event.getPos());
			Item tool = event.getPlayer().getMainHandItem().getItem();
			if (!Config.server().veinMiner().blacklist().contains(RegistryUtil.getId(tool))) {
				VeinMiningLogic.applyVein((ServerPlayer) event.getPlayer(), event.getPos());
			}
		}
	}
	
	private static Map<String, Long> calculateXpAward(Core core, BlockEvent.BreakEvent event, CompoundTag dataIn) {
		Map<String, Long> outMap = core.getExperienceAwards(EventType.BLOCK_BREAK, event.getPos(), (Level)event.getLevel(), event.getPlayer(), dataIn); 
		LevelChunk chunk = (LevelChunk) event.getLevel().getChunk(event.getPos());
		var placeMap = chunk.getData(DataAttachmentTypes.PLACED_MAP);
		if (placeMap.containsKey(event.getPos()) && placeMap.get(event.getPos()).equals(event.getPlayer().getUUID())) {
			//Do not apply self-place penalty to crops
			BlockState cropState = event.getLevel().getBlockState(event.getPos()); 
			if (cropState.getBlock() instanceof CropBlock && ((CropBlock)cropState.getBlock()).isMaxAge(cropState))
				return outMap;
			double xpModifier = Config.server().xpGains().reusePenalty();
			if (xpModifier == 0) return new HashMap<>();
			Map<String, Long> modifiedOutMap = new HashMap<>();
			outMap.forEach((k, v) -> modifiedOutMap.put(k, (long)((double)v * xpModifier)));
			return modifiedOutMap;
		}
		return outMap;
	}
}
