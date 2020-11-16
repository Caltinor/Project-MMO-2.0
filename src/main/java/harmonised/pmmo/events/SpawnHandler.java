package harmonised.pmmo.events;

import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import java.util.Collection;

public class SpawnHandler
{
    public static void handleSpawn( LivingSpawnEvent.EnteringChunk event )
    {
        if( event.getEntity() instanceof MobEntity && !(event.getEntity() instanceof AnimalEntity) )
        {
            MobEntity mob = (MobEntity) event.getEntity();
            MinecraftServer server = mob.getServer();
            if( server != null )
            {
                float powerLevel = 0;
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
