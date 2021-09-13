package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.WebHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        Player player = event.getPlayer();
        if( player.level.isClientSide() )
        {
            WebHandler.updateInfo();
        }
        else
        {
            if( Config.forgeConfig.autoLeavePartyOnDisconnect.get() )
                PmmoSavedData.get().removeFromParty( player.getUUID() );
            player.getServer().getPlayerList().getPlayers().forEach( XP::syncPlayersSkills );
        }
    }
}
