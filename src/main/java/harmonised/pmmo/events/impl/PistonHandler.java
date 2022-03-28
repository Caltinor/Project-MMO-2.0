package harmonised.pmmo.events.impl;

import java.util.UUID;

import harmonised.pmmo.storage.ChunkDataHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.event.world.PistonEvent;

public class PistonHandler {
	
	 public static void handle(PistonEvent event)
	    {
	        if(!event.getWorld().isClientSide())
	        {
	            Level world = (Level) event.getWorld();
	            BlockPos pistonPos = event.getPos();
	            Direction direction = event.getDirection();
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
	        }
	    }
}
