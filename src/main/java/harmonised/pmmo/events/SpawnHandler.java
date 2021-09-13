package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import java.util.Collection;

public class SpawnHandler
{
    public static void handleSpawn( LivingSpawnEvent.SpecialSpawn event )
    {
        if( event.getEntity() instanceof Mob && !(event.getEntity() instanceof Animal) )
        {
            Mob mob = (Mob) event.getEntity();
            MinecraftServer server = mob.getServer();
            if( server != null )
            {
                int powerLevelContributorCount = 0;
                float powerLevel = 0;

                Collection<Player> allPlayers = XP.getNearbyPlayers( mob );

                for( Player player : allPlayers )
                {
                    if( XP.isPlayerSurvival( player ) )
                    {
                        powerLevel += XP.getPowerLevel( player.getUUID() );
                        powerLevelContributorCount++;
                    }
                }

                if( powerLevelContributorCount > 1 )
                    powerLevel /= powerLevelContributorCount;

                AttributeHandler.updateHP( mob, powerLevel );
                AttributeHandler.updateDamage( mob, powerLevel );
                AttributeHandler.updateSpeed( mob, powerLevel );
            }
        }
    }
}
