package harmonised.pmmo.events;

import harmonised.pmmo.util.XP;
import net.minecraft.block.BlockState;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.PistonEvent;

import java.util.UUID;

public class PistonEventHandler
{
    public static void handlePistonPush(PistonEvent event)
    {
        if(!event.getWorld().isRemote())
        {
            World world = (World) event.getWorld();
            BlockPos pistonPos = event.getPos();
            Direction direction = event.getDirection();
            UUID uuid;
            if(event.getPistonMoveType().equals(PistonEvent.PistonMoveType.EXTEND))
            {
                uuid = ChunkDataHandler.checkPos(world, pistonPos.offset(direction));
                if(uuid != null)
                {
                    ChunkDataHandler.addPos(XP.getDimResLoc(world), pistonPos.offset(direction, 2), uuid);
                    ChunkDataHandler.delPos(XP.getDimResLoc(world), pistonPos.offset(direction));
                }
            }
            else
            {
                BlockState state = world.getBlockState(pistonPos);
                if(state.hasProperty(MovingPistonBlock.TYPE) && state.get(MovingPistonBlock.TYPE).equals(PistonType.STICKY))
                {
                    uuid = UUID.fromString("80008135-1337-3251-1523-852369874125");
                    ChunkDataHandler.addPos(XP.getDimResLoc(world), pistonPos.offset(direction), uuid);
                    ChunkDataHandler.delPos(XP.getDimResLoc(world), pistonPos.offset(direction, 2));
                }
            }
        }
    }
}
