package harmonised.pmmo.events;

import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCloneHandler
{
    public static void handleClone( PlayerEvent.Clone event )
    {
        event.getPlayer().getPersistentData().put( "pmmo", event.getOriginal().getPersistentData().getCompound( "pmmo" ) );
    }
}
