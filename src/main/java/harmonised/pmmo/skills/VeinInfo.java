package harmonised.pmmo.skills;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VeinInfo
{
    public World world;
    public BlockState state;
    public BlockPos pos;
    public ItemStack item;

    public VeinInfo( World world, BlockState state, BlockPos pos, ItemStack item )
    {
        this.world = world;
        this.state = state;
        this.pos = pos;
        this.item = item;
    }
}
