package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn( PlayerEvent.PlayerRespawnEvent event )
    {
        EntityPlayer player = event.getPlayer();

        AttributeHandler.updateAll( player );
        player.setHealth( player.getMaxHealth() );
    }
}
