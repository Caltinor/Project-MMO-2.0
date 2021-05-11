package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.WebHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerDisconnectedHandler
{
    public static void handlerPlayerDisconnected( PlayerEvent.PlayerLoggedOutEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( player.world.isRemote() )
        {
            WebHandler.updateInfo();
        }
        else
        {
            if( Config.forgeConfig.autoLeavePartyOnDisconnect.get() )
                PmmoSavedData.get().removeFromParty( player.getUniqueID() );
            player.getServer().getPlayerList().getPlayers().forEach( XP::syncPlayersSkills );
        }
    }
}
