package harmonised.pmmo.skills;

import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class PMMOFireworkEntity extends FireworkRocketEntity
{
    private WorldText explosionText;

    public PMMOFireworkEntity(Level worldIn, double x, double y, double z, ItemStack givenItem)
    {
        super(worldIn, x, y, z, givenItem);
        this.life = 0;
        this.setPos(x, y, z);
        int i = 1;
        if (!givenItem.isEmpty() && givenItem.hasTag()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, givenItem.copy());
            i += givenItem.getOrCreateTagElement("Fireworks").getByte("Flight");
        }

        this.setDeltaMovement(this.random.nextGaussian() * 0.001D, 0.05D, this.random.nextGaussian() * 0.001D);
        this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public void setExplosionText(WorldText explosionText)
    {
        this.explosionText = explosionText;
    }

    @Override
    public void explode()
    {
        this.level.broadcastEntityEvent(this, (byte)17);
        if(explosionText != null)
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
        }
//        this.dealExplosionDamage();
        this.remove(RemovalReason.DISCARDED);
    }
}
