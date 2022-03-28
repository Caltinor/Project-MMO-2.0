package harmonised.pmmo.events.impl;

import net.minecraftforge.event.world.PistonEvent;

public class PistonHandler {
	
	 public static void handle(PistonEvent event) {
		 /* Design:
		  * grab all of the blocks that will be pushed using the structure helper
		  * as well as those being destroyed.
		  * 
		  * Then, iterater through each one and get the capability for the chunk
		  * moved from (and delPos) and for the chunk moving to (and addpos). if
		  * being destroyed, do nothing as the break event should fire for those.
		  * 
		  * This design should capture both extension and retraction since we are
		  * using the offset direction position which provides an ultimate destination
		  * and is agnostic to the push behavior.
		  * 
	        if(!event.getWorld().isClientSide())
	        {
	            Level world = (Level) event.getWorld();
	            BlockPos pistonPos = event.getPos();
	            Direction direction = event.getDirection();
	            event.getStructureHelper().getToPush()
	            UUID uuid;
	            if(event.getPistonMoveType().equals(PistonEvent.PistonMoveType.EXTEND))
	            {
	                uuid = ChunkDataHandler.checkPos(world, pistonPos.relative(direction));
	                if(uuid != null)
	                {
	                    ChunkDataHandler.addPos(world.dimension(), pistonPos.relative(direction, 2), uuid);
	                    ChunkDataHandler.delPos(world.dimension(), pistonPos.relative(direction));
	                }
	            }
	            else
	            {
	                BlockState state = world.getBlockState(pistonPos);
	                if(state.hasProperty(MovingPistonBlock.TYPE) && state.getValue(MovingPistonBlock.TYPE).equals(PistonType.STICKY))
	                {
	                	//TODO grab a potentially pulled block UUID and make sure to update.
	                    uuid = UUID.fromString("80008135-1337-3251-1523-852369874125");
	                    ChunkDataHandler.addPos(world.dimension(), pistonPos.relative(direction), uuid);
	                    ChunkDataHandler.delPos(world.dimension(), pistonPos.relative(direction, 2));
	                }
	            }
	        }*/
	    }
}
