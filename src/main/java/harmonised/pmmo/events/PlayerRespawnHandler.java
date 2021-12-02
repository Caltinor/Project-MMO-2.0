package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();

        AttributeHandler.updateAll(player);
        player.setHealth(player.getMaxHealth());
    }
}