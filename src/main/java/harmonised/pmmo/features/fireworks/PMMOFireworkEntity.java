package harmonised.pmmo.features.fireworks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PMMOFireworkEntity extends FireworkRocketEntity
{
    public PMMOFireworkEntity(Level worldIn, double x, double y, double z, ItemStack givenItem)
    {
        super(worldIn, x, y, z, givenItem);
    }

    @Override
    public void explode(ServerLevel level)
    {
        this.level().broadcastEntityEvent(this, (byte)17);
        this.remove(RemovalReason.DISCARDED);
    }
}
