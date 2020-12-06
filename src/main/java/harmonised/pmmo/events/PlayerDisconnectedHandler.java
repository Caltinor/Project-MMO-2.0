package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( player.world.isRemote() )
        {

        }
        else
        {
            if( Config.forgeConfig.autoLeavePartyOnDisconnect.get() )
                PmmoSavedData.get().removeFromParty( player.getUniqueID() );
            player.getServer().getPlayerList().getPlayers().forEach( XP::syncPlayersSkills );
        }
    }
}
