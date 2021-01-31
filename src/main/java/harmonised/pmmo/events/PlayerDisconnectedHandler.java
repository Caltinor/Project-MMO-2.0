package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        EntityPlayer player = event.player;
        if( player.world.isRemote )
        {

        }
        else
        {
            if( FConfig.autoLeavePartyOnDisconnect )
                PmmoSavedData.get().removeFromParty( player.getUniqueID() );
            player.getServer().getPlayerList().getPlayers().forEach( XP::syncPlayersSkills );
        }    }
}