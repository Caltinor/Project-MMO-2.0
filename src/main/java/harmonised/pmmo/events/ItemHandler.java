package harmonised.pmmo.events;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemHandler
{
    public static void handleItemEntityPickup( EntityItemPickupEvent event )
    {
        PlayerTickHandler.tagOwnership( event.getItem().getItem(), event.getPlayer().getUniqueID() );
    }
}
