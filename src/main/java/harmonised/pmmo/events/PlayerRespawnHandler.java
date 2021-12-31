package harmonised.pmmo.events;

import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
    	if (!event.getPlayer().level.isClientSide) {
	        ServerPlayer player = (ServerPlayer) event.getPlayer();	
	        PerkRegistry.executePerk(PerkTrigger.SKILL_UP, player);
	        player.setHealth(player.getMaxHealth());
    	}
    }
}