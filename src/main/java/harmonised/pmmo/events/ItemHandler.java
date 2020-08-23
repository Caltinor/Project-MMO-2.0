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
        ItemStack itemStack = event.getItem().getItem();
        PlayerEntity player = event.getPlayer();

        if( itemStack.getTag() == null )
            itemStack.setTag( new CompoundNBT() );

        itemStack.getTag().putUniqueId( "lastOwner", player.getUniqueID() );

    }
}
