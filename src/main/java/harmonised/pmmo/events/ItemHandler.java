package harmonised.pmmo.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ItemHandler
{
    public static void handleItemEntityPickup( EntityItemPickupEvent event )
    {
        PlayerTickHandler.tagOwnership( event.getItem().getItem(), event.getPlayer().getUniqueID() );
    }
}
