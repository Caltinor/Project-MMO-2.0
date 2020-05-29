package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import javax.annotation.Nullable;

public class BreedHandler
{
    private static final double defaultBreedingXp = Config.forgeConfig.defaultBreedingXp.get();

    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        if( event.getChild() != null )
        {
            String regKey = event.getChild().getEntityString();

            if( JsonConfig.data.get( "xpValueBreeding" ).containsKey( regKey ) )
            {
                XP.awardXpMap( event.getCausedByPlayer(), JsonConfig.data.get( "xpValueBreeding" ).get( regKey ), "breeding", false, false );
            }
            else
                XP.awardXp( event.getCausedByPlayer(), Skill.FARMING, "breeding", defaultBreedingXp, false, false );
        }
    }
}
