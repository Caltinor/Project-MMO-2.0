package harmonised.pmmo.events;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.XP;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

public class TamingHandler
{
    public static void handleAnimalTaming( AnimalTameEvent event )
    {
        String regKey = event.getAnimal().getEntityString();

        if( JsonConfig.data.get( "xpValueTaming" ).containsKey( regKey ) )
        {
            XP.awardXp( event.getTamer(), JsonConfig.data.get( "xpValueTaming" ).get( regKey ), "taming", true );
        }
//		else
//			XP.awardXp( event.getTamer(), Skill.TAMING, "taming", 10.0D, true );
    }
}
