package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        if( FConfig.autoLeavePartyOnDisconnect )
            PmmoSavedData.get().removeFromParty( event.player.getUniqueID() );
    }
}