package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
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
            Map<String, Double> award = XP.getXp( event.getAnimal() , JType.XP_VALUE_TAME );

            if( award.size() == 0 )
                award.put( Skill.TAMING.toString(), Config.forgeConfig.defaultTamingXp.get() );

            if( XP.isHoldingDebugItemInOffhand( tamer ) )
                tamer.sendStatusMessage( new StringTextComponent( regKey ), false );

            for( String awardSkillName : award.keySet() )
            {
                Vector3d xpDropPos = event.getAnimal().getPositionVec();
                WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( tamer.getServerWorld() ), xpDropPos.getX(), xpDropPos.getY() + event.getAnimal().getEyeHeight() + 0.523, xpDropPos.getZ(), 0.5, award.get( awardSkillName ), awardSkillName );
                xpDrop.setDecaySpeed( 0.35 );
                XP.addWorldXpDrop( xpDrop, tamer );
                Skill.addXp( awardSkillName, tamer, award.get( awardSkillName ), "taming " + regKey, false, false );
            }
        }
    }
}