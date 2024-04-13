package harmonised.pmmo.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class SalvageEvent extends PlayerEvent implements ICancellableEvent
{
    BlockPos blockPos;
    public SalvageEvent(Player player, BlockPos blockPos)
    {
        super(player);
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos()
    {
        return blockPos;
    }
}
