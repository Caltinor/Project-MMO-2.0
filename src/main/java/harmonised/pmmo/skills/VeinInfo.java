package harmonised.pmmo.skills;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VeinInfo
{
    public World world;
    public BlockState state;
    public BlockPos pos;
    public ItemStack itemStack;
    public Item startItem;

    public VeinInfo( World world, BlockState state, BlockPos pos, ItemStack itemStack )
    {
        this.world = world;
        this.state = state;
        this.pos = pos;
        this.itemStack = itemStack;
        this.startItem = itemStack.getItem();
    }
}
