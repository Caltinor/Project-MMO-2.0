package harmonised.pmmo.skills;

import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PMMOFireworkEntity extends FireworkRocketEntity
{
    private WorldText explosionText;

    public PMMOFireworkEntity(World worldIn, double x, double y, double z, ItemStack givenItem)
    {
        super(worldIn, x, y, z, givenItem);
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

    public void setExplosionText( WorldText explosionText )
    {
        this.explosionText = explosionText;
    }

    @Override
    public void func_213893_k()
    {
        this.world.setEntityState(this, (byte)17);
        if( explosionText != null )
        {
            Vector3d pos = getPositionVec();
            ResourceLocation dimResLoc = XP.getDimResLoc( world );
            for( ServerPlayerEntity player : PmmoSavedData.getServer().getPlayerList().getPlayers() )
            {
                if( world == player.getServerWorld() && Util.getDistance( pos, player.getPositionVec() ) < 325.1 )
                {
                    explosionText.setPos( pos );
                    explosionText.updatePos();
                    XP.addWorldTextRadius( dimResLoc, explosionText, 128 );
                }
            }
        }
//        this.dealExplosionDamage();
        this.remove();
    }
}
