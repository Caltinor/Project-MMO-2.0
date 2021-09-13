package harmonised.pmmo.skills;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class VeinInfo
{
    public Level world;
    public BlockState state;
    public BlockPos pos;
    public ItemStack itemStack;
    public Item startItem;

    public VeinInfo( Level world, BlockState state, BlockPos pos, ItemStack itemStack )
    {
        this.world = world;
        this.state = state;
        this.pos = pos;
        this.itemStack = itemStack;
        this.startItem = itemStack.getItem();
    }
}
