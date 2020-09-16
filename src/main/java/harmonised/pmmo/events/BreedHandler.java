package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.Map;

public class BreedHandler
{
    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        if( event.getChild() != null && event.getCausedByPlayer() != null && !(event.getCausedByPlayer() instanceof FakePlayer) )
        {
            double defaultBreedingXp = Config.forgeConfig.defaultBreedingXp.get();
            String regKey = event.getChild().getEntityString();
            Map<String, Double> xpValue = XP.getXp( XP.getResLoc( regKey ), JType.XP_VALUE_BREED );

            if( xpValue.size() > 0 )
                XP.awardXpMap( event.getCausedByPlayer().getUniqueID(), xpValue, "breeding", false, false );
            else
                XP.awardXp( event.getCausedByPlayer(), Skill.FARMING, "breeding", defaultBreedingXp, false, false );
        }
    }
}
