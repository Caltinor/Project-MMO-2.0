package harmonised.pmmo.api.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;

public class TreasureEvent extends PlayerEvent
{
    BlockPos blockPos;
    ItemStack itemStack;
    Map<String, Double> award;

    public TreasureEvent( PlayerEntity player, BlockPos blockPos, ItemStack itemStack, Map<String, Double> award )
    {
        super( player );
        this.blockPos = blockPos;
        this.itemStack = itemStack;
        this.award = award;
    }

    @Override
    public boolean isCancelable()
    {
        return true;
    }

    public BlockPos getBlockPos()
    {
        return blockPos;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Map<String, Double> getAward()
    {
        return award;
    }

    public void setAward(Map<String, Double> award)
    {
        this.award = award;
    }
}
