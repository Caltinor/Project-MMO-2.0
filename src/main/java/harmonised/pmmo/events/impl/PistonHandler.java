package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.storage.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.world.PistonEvent;

public class PistonHandler {
	
	 public static void handle(PistonEvent.Pre event) {
		 if (event.getWorld().isClientSide()) return;
		 
		 
		 Level level = (Level) event.getWorld();
		 PistonStructureResolver structure = event.getStructureHelper();
		 structure.resolve();

		 for (BlockPos destroyed : structure.getToDestroy()) {
			 LevelChunk ck = level.getChunkAt(destroyed);
			 ck.getCapability(ChunkDataProvider.CHUNK_CAP).ifPresent(cap -> {
				 cap.delPos(destroyed);
			 });
			 ck.setUnsaved(true);
		 }		 
		 Map<BlockPos, UUID> updateToMap = new HashMap<>();
		 for (BlockPos moved : structure.getToPush()) {
			 LevelChunk oldCK = level.getChunkAt(moved);			 
			 UUID currentID = oldCK.getCapability(ChunkDataProvider.CHUNK_CAP).map(cap -> cap.checkPos(moved)).get();
			 oldCK.getCapability(ChunkDataProvider.CHUNK_CAP).ifPresent(cap -> {
				 cap.delPos(moved);
			 });
			 updateToMap.put( moved.relative(event.getStructureHelper().getPushDirection()), currentID);
			 oldCK.setUnsaved(true);
		 }
		 for (Map.Entry<BlockPos, UUID> map : updateToMap.entrySet()) {
			 LevelChunk toCK = level.getChunkAt(map.getKey());
			 toCK.getCapability(ChunkDataProvider.CHUNK_CAP).ifPresent(cap -> {
				 cap.addPos(map.getKey(), map.getValue());
			 });
			 toCK.setUnsaved(true);
		 }
	 }
}
