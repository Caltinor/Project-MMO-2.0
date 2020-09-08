package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

import java.util.Map;

public class TameHandler
{
    private static final double defaultTamingXp = Config.forgeConfig.defaultTamingXp.get();

    public static void handleAnimalTaming( AnimalTameEvent event )
    {
        String regKey = event.getAnimal().getEntityString();
        Map<String, Double> xpValue = XP.getXp( XP.getResLoc( regKey ), JType.XP_VALUE_TAME );

        if( xpValue.size() > 0 )
            XP.awardXpMapDouble( event.getTamer().getUniqueID(), xpValue, "taming", false, false );
		else
			XP.awardXp( event.getTamer(), Skill.TAMING, "taming", defaultTamingXp, false, false );
    }
}
