package harmonised.pmmo.events;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;

public class RemoveThisClass
{
    public static void doStuff(BlockEvent.BreakEvent event)
    {
        World world = (World) event.getWorld();
        if(!world.isRemote())
        {
            BlockPos pos = event.getPos().up(2);
            world.setBlockState(pos.down(), Blocks.GRASS_BLOCK.getDefaultState());
            world.setBlockState(pos, Blocks.BIRCH_SAPLING.getDefaultState());
            BlockState saplingState = world.getBlockState(pos).func_235896_a_(BlockStateProperties.STAGE_0_1);
            SaplingBlock sapling = (SaplingBlock) saplingState.getBlock();
            sapling.grow((ServerWorld) world, world.getRandom(), pos, saplingState);
        }
    }
}
