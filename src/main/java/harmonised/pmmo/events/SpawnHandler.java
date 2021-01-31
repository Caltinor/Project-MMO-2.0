package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import java.util.Collection;

public class SpawnHandler
{
    public static void handleSpawn( LivingSpawnEvent.EnteringChunk event )
    {
        if( event.getEntity() instanceof EntityMob && !(event.getEntity() instanceof EntityAnimal) )
        {
                EntityMob mob = (EntityMob) event.getEntity();
            MinecraftServer server = mob.getServer();
            if( server != null )
            {
                double powerLevel = 0;
                Collection<EntityPlayer> allPlayers = XP.getNearbyPlayers( mob );

                for( EntityPlayer player : allPlayers )
                {
                    if( XP.isPlayerSurvival( player ) )
                        powerLevel += XP.getPowerLevel( player.getUniqueID() );
                }

                AttributeHandler.updateHP( mob, powerLevel );
                AttributeHandler.updateDamage( mob, powerLevel );
                AttributeHandler.updateSpeed( mob, powerLevel );
            }
        }
    }
}
