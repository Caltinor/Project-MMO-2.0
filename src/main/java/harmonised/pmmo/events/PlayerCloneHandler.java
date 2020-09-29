package harmonised.pmmo.events;

import harmonised.pmmo.util.Reference;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCloneHandler
{
    public static void handleClone( PlayerEvent.Clone event )
    {
        event.getPlayer().getPersistentData().put( Reference.MOD_ID, event.getOriginal().getPersistentData().getCompound( Reference.MOD_ID ) );
    }
}
