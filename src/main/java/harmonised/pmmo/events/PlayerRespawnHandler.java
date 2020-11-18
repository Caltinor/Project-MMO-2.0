package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn( PlayerEvent.PlayerRespawnEvent event )
    {
        EntityPlayer player = event.player;

        AttributeHandler.updateAll( player );
        player.setHealth( player.getMaxHealth() );
    }
}