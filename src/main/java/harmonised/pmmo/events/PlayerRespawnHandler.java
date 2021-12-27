package harmonised.pmmo.events;

import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.skills.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerRespawnHandler
{
    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
    	if (!event.getPlayer().level.isClientSide) {
	        ServerPlayer player = (ServerPlayer) event.getPlayer();
	
	        for (Map.Entry<String, Integer> skill : Skill.getSkills().entrySet()) {
	    		int skillLevel = APIUtils.getLevel(skill.getKey(), player);
	    		PerkRegistry.executePerk(PerkTrigger.SKILL_UP, player, skillLevel);
	        }
	        player.setHealth(player.getMaxHealth());
    	}
    }
}