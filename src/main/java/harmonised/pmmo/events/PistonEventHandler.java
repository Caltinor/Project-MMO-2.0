package harmonised.pmmo.events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.PistonEvent;

import java.util.UUID;

public class PistonEventHandler
{
    public static void handlePistonPush( PistonEvent event )
    {
        if( !event.getWorld().isRemote() )
        {
            World world = event.getWorld().getWorld();
            BlockPos pistonPos = event.getPos();
            Direction direction = event.getDirection();
            UUID uuid;
            if( event.getPistonMoveType().equals( PistonEvent.PistonMoveType.EXTEND ) )
            {
                uuid = ChunkDataHandler.checkPos( world.getDimension().getType().getRegistryName(), pistonPos.offset( direction ) );
                if( uuid != null )
                {
                    ChunkDataHandler.addPos( world.getDimension().getType().getRegistryName(), pistonPos.offset( direction, 2 ), uuid );
                    ChunkDataHandler.delPos( world.getDimension().getType().getRegistryName(), pistonPos.offset( direction ) );
                }
            }
            else
            {
                BlockState state = world.getBlockState( pistonPos );
                if( state.has( MovingPistonBlock.TYPE ) && state.get( MovingPistonBlock.TYPE ).equals( PistonType.STICKY ) )
                {
                    uuid = UUID.fromString( "80008135-1337-3251-1523-852369874125" );
                    ChunkDataHandler.addPos( world.getDimension().getType().getRegistryName(), pistonPos.offset( direction ), uuid );
                    ChunkDataHandler.delPos( world.getDimension().getType().getRegistryName(), pistonPos.offset( direction, 2 ) );
                }
            }
        }
    }
}
