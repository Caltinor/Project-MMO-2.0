package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn( PlayerEvent.PlayerRespawnEvent event )
    {
        PlayerEntity player = event.getPlayer();

        AttributeHandler.updateAll( player );
        player.setHealth( player.getMaxHealth() );
    }
}