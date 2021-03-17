package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.Map;

public class BreedHandler
{
    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        if( event.getChild() != null && event.getCausedByPlayer() != null && event.getCausedByPlayer() instanceof ServerPlayerEntity && !(event.getCausedByPlayer() instanceof FakePlayer) )
        {
            ServerPlayerEntity causedByPlayer = (ServerPlayerEntity) event.getCausedByPlayer();
            double defaultBreedingXp = Config.forgeConfig.defaultBreedingXp.get();
            String regKey = event.getChild().getEntityString();
            Vector3d midPos = Util.getMidVec( event.getParentA().getPositionVec(), event.getParentB().getPositionVec() );
            Vector3d xpDropPos = new Vector3d( midPos.getX(), midPos.getY() + event.getChild().getEyeHeight() + 0.523, midPos.getZ() );
            Map<String, Double> award = XP.getXp( XP.getResLoc( regKey ), JType.XP_VALUE_BREED );
            if( award.size() == 0 )
                award.put( Skill.FARMING.toString(), defaultBreedingXp );

            if( XP.isHoldingDebugItemInOffhand( causedByPlayer ) )
                causedByPlayer.sendStatusMessage( new StringTextComponent( regKey ), false );

            for( String awardSkillName : award.keySet() )
            {
                WorldXpDrop xpDrop = new WorldXpDrop( xpDropPos.getX(), xpDropPos.getY(), xpDropPos.getZ(), 0.5, award.get( awardSkillName ), awardSkillName );
                WorldRenderHandler.addWorldXpDrop( xpDrop );
                Skill.addXp( awardSkillName, causedByPlayer, award.get( awardSkillName ), "breeding " + regKey, false, false );
            }
        }
    }
}