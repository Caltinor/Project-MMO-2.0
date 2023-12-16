package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.PistonEvent;


public class PistonHandler {
	
	 public static void handle(PistonEvent.Pre event) {
		 if (event.getLevel().isClientSide()) return;
		 
		 
		 Level level = (Level) event.getLevel();
		 PistonStructureResolver structure = event.getStructureHelper();
		 structure.resolve();

		 for (BlockPos destroyed : structure.getToDestroy()) {
			 LevelChunk ck = level.getChunkAt(destroyed);
			 ck.getData(DataAttachmentTypes.PLACED_MAP.get()).remove(destroyed);
			 ck.setUnsaved(true);
		 }		 
		 Map<BlockPos, UUID> updateToMap = new HashMap<>();
		 for (BlockPos moved : structure.getToPush()) {
			 LevelChunk oldCK = level.getChunkAt(moved);
			 var placedData = oldCK.getData(DataAttachmentTypes.PLACED_MAP.get());
			 UUID currentID = placedData.getOrDefault(moved, Reference.NIL);
			 if (currentID.equals(Reference.NIL)) continue;
			 placedData.remove(moved);
			 updateToMap.put( moved.relative(event.getStructureHelper().getPushDirection()), currentID);
			 oldCK.setUnsaved(true);
		 }
		 for (Map.Entry<BlockPos, UUID> map : updateToMap.entrySet()) {
			 LevelChunk toCK = level.getChunkAt(map.getKey());
			 toCK.getData(DataAttachmentTypes.PLACED_MAP.get()).put(map.getKey(), map.getValue());
			 toCK.setUnsaved(true);
		 }
	 }
}
