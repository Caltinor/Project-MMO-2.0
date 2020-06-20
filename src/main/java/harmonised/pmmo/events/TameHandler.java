package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

public class TameHandler
{
    private static final double defaultTamingXp = Config.forgeConfig.defaultTamingXp.get();

    public static void handleAnimalTaming( AnimalTameEvent event )
    {
        String regKey = event.getAnimal().getEntityString();

        if( JsonConfig.data.get( "xpValueTaming" ).containsKey( regKey ) )
        {
            XP.awardXpMap( event.getTamer(), JsonConfig.data.get( "xpValueTaming" ).get( regKey ), "taming", false, false );
        }
		else
			XP.awardXp( event.getTamer(), Skill.TAMING, "taming", defaultTamingXp, false, false );
    }
}
