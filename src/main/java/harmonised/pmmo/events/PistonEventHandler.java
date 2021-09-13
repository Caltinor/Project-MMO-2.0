package harmonised.pmmo.events;

import harmonised.pmmo.util.XP;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.PistonEvent;

import java.util.UUID;

public class PistonEventHandler
{
    public static void handlePistonPush( PistonEvent event )
    {
        if( !event.getWorld().isClientSide() )
        {
            Level world = (Level) event.getWorld();
            BlockPos pistonPos = event.getPos();
            Direction direction = event.getDirection();
            UUID uuid;
            if( event.getPistonMoveType().equals( PistonEvent.PistonMoveType.EXTEND ) )
            {
                uuid = ChunkDataHandler.checkPos( world, pistonPos.relative( direction ) );
                if( uuid != null )
                {
                    ChunkDataHandler.addPos( XP.getDimResLoc( world ), pistonPos.relative( direction, 2 ), uuid );
                    ChunkDataHandler.delPos( XP.getDimResLoc( world ), pistonPos.relative( direction ) );
                }
            }
            else
            {
                BlockState state = world.getBlockState( pistonPos );
                if( state.hasProperty( MovingPistonBlock.TYPE ) && state.getValue( MovingPistonBlock.TYPE ).equals( PistonType.STICKY ) )
                {
                    uuid = UUID.fromString( "80008135-1337-3251-1523-852369874125" );
                    ChunkDataHandler.addPos( XP.getDimResLoc( world ), pistonPos.relative( direction ), uuid );
                    ChunkDataHandler.delPos( XP.getDimResLoc( world ), pistonPos.relative( direction, 2 ) );
                }
            }
        }
    }
}
