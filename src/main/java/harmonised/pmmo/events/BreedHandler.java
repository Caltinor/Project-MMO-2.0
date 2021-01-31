package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.Map;

public class BreedHandler
{
    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        if( event.getChild() != null && event.getCausedByPlayer() != null && event.getCausedByPlayer() instanceof EntityPlayerMP && !(event.getCausedByPlayer() instanceof FakePlayer) )
        {
            EntityPlayerMP causedByPlayer = (EntityPlayerMP) event.getCausedByPlayer();
            double defaultBreedingXp = FConfig.defaultBreedingXp;
            String regKey = event.getChild().getName();
            Map<String, Double> xpValue = XP.getXp( XP.getResLoc( regKey ), JType.XP_VALUE_BREED );

            if( XP.isHoldingDebugItemInOffhand( causedByPlayer ) )
                causedByPlayer.sendStatusMessage( new TextComponentString( regKey ), false );

            if( xpValue.size() > 0 )
                XP.awardXpMap( event.getCausedByPlayer().getUniqueID(), xpValue, "breeding", false, false );
            else
                XP.awardXp( causedByPlayer, Skill.FARMING.toString(), "breeding", defaultBreedingXp, false, false, false );
        }
    }
}