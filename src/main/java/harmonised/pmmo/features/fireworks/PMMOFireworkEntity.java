package harmonised.pmmo.features.fireworks;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PMMOFireworkEntity extends FireworkRocketEntity
{
	//TODO reimplement firework text
    //private WorldText explosionText;

    public PMMOFireworkEntity(Level worldIn, double x, double y, double z, ItemStack givenItem)
    {
        super(worldIn, x, y, z, givenItem);
    }

    /*public void setExplosionText(WorldText explosionText) {
        this.explosionText = explosionText;
    }*/

    @Override
    public void explode()
    {
        this.level.broadcastEntityEvent(this, (byte)17);
        /*if(explosionText != null)
        {
            Vec3 pos = position();
            ResourceLocation dimResLoc = XP.getDimResLoc(level);
            for(ServerPlayer player : PmmoSavedData.getServer().getPlayerList().getPlayers())
            {
                if(level == player.getLevel() && Util.getDistance(pos, player.position()) < 325.1)
                {
                    explosionText.setPos(pos);
                    explosionText.updatePos();
                    XP.addWorldTextRadius(dimResLoc, explosionText, 128);
                }
            }
        }*/
        //this.dealExplosionDamage();
        this.remove(RemovalReason.DISCARDED);
    }
}
