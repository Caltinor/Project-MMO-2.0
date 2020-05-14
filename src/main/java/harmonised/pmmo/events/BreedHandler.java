package harmonised.pmmo.events;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class BreedHandler
{
    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        String regKey = event.getChild().getEntityString();

        if( JsonConfig.data.get( "xpValueBreeding" ).containsKey( regKey ) )
        {
            XP.awardXp( event.getCausedByPlayer(), JsonConfig.data.get( "xpValueBreeding" ).get( regKey ), "breeding", true );
        }
        else
            XP.awardXp( event.getCausedByPlayer(), Skill.FARMING, "breeding", 10.0D, true );
    }
}
