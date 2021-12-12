package harmonised.pmmo.api.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SalvageEvent extends PlayerEvent
{
    BlockPos blockPos;
    public SalvageEvent(PlayerEntity player, BlockPos blockPos)
    {
        super(player);
        this.blockPos = blockPos;
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
}
