package harmonised.pmmo.skills;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PMMOFireworkEntity extends FireworkRocketEntity
{

    public PMMOFireworkEntity(EntityType<? extends FireworkRocketEntity> p_i50164_1_, World p_i50164_2_)
    {
        super(p_i50164_1_, p_i50164_2_);
    }

    public PMMOFireworkEntity(World worldIn, double x, double y, double z, ItemStack givenItem) {
        super(EntityType.FIREWORK_ROCKET, worldIn);
        this.fireworkAge = 0;
        this.setPosition(x, y, z);
        int i = 1;
        if (!givenItem.isEmpty() && givenItem.hasTag()) {
            this.dataManager.set(FIREWORK_ITEM, givenItem.copy());
            i += givenItem.getOrCreateChildTag("Fireworks").getByte("Flight");
        }

        this.setMotion(this.rand.nextGaussian() * 0.001D, 0.05D, this.rand.nextGaussian() * 0.001D);
        this.lifetime = 10 * i + this.rand.nextInt(6) + this.rand.nextInt(7);
    }

    @Override
    public void func_213893_k()
    {
        this.world.setEntityState(this, (byte)17);
//        this.dealExplosionDamage();
        this.remove();
    }
}
