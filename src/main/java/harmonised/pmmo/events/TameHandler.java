package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;

import java.util.Map;

public class TameHandler
{
    public static void handleAnimalTaming( AnimalTameEvent event )
    {
        if( event.getTamer() instanceof ServerPlayer )
        {
            ServerPlayer tamer = (ServerPlayer) event.getTamer();
            String regKey = event.getAnimal().getEncodeId();
            Map<String, Double> award = XP.getXp( event.getAnimal() , JType.XP_VALUE_TAME );

            if( award.size() == 0 )
                award.put( Config.forgeConfig.defaultTamingXpFarming.get() ? Skill.FARMING.toString() : Skill.TAMING.toString(), Config.forgeConfig.defaultTamingXp.get() );

            if( XP.isHoldingDebugItemInOffhand( tamer ) )
                tamer.displayClientMessage( new TextComponent( regKey ), false );

            for( String awardSkillName : award.keySet() )
            {
                Vec3 xpDropPos = event.getAnimal().position();
                WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( tamer.getLevel() ), xpDropPos.x(), xpDropPos.y() + event.getAnimal().getEyeHeight() + 0.523, xpDropPos.z(), 0.5, award.get( awardSkillName ), awardSkillName );
                xpDrop.setDecaySpeed( 0.35 );
                XP.addWorldXpDrop( xpDrop, tamer );
                Skill.addXp( awardSkillName, tamer, award.get( awardSkillName ), "taming " + regKey, false, false );
            }
        }
    }
}