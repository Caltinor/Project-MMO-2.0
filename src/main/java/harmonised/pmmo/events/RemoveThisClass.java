package harmonised.pmmo.events;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.world.BlockEvent;

public class RemoveThisClass
{
    public static void doStuff( BlockEvent.BreakEvent event )
    {
        Level world = (Level) event.getWorld();
        if( !world.isClientSide() )
        {
            BlockPos pos = event.getPos().above( 2 );
            world.setBlockAndUpdate( pos.below(), Blocks.GRASS_BLOCK.defaultBlockState() );
            world.setBlockAndUpdate( pos, Blocks.BIRCH_SAPLING.defaultBlockState() );
            BlockState saplingState = world.getBlockState( pos ).cycle( BlockStateProperties.STAGE );
            SaplingBlock sapling = (SaplingBlock) saplingState.getBlock();
            sapling.performBonemeal( (ServerLevel) world, world.getRandom(), pos, saplingState );
        }
    }
}
