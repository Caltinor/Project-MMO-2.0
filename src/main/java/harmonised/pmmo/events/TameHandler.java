package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

import java.util.Map;

public class TameHandler
{
    public static void handleAnimalTaming( AnimalTameEvent event )
    {
        if( event.getTamer() instanceof ServerPlayerEntity )
        {
            ServerPlayerEntity tamer = (ServerPlayerEntity) event.getTamer();
            String regKey = event.getAnimal().getEntityString();
            Map<String, Double> xpValue = XP.getXp( XP.getResLoc( regKey ), JType.XP_VALUE_TAME );

            if( xpValue.size() > 0 )
                XP.awardXpMap( tamer.getUniqueID(), xpValue, "taming", false, false );
            else
                XP.awardXp( tamer, Skill.TAMING.toString(), "taming", Config.forgeConfig.defaultTamingXp.get(), false, false, false );
        }
    }
}