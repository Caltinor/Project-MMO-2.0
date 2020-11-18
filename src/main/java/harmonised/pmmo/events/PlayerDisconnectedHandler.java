package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        if( Config.forgeConfig.autoLeavePartyOnDisconnect.get() )
            PmmoSavedData.get().removeFromParty( event.player.getUniqueID() );
    }
}