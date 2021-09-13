package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.Map;

public class BreedHandler
{
    public static void handleBreedEvent( BabyEntitySpawnEvent event )
    {
        if( event.getChild() != null && event.getCausedByPlayer() != null && event.getCausedByPlayer() instanceof ServerPlayer && !(event.getCausedByPlayer() instanceof FakePlayer) )
        {
            ServerPlayer causedByPlayer = (ServerPlayer) event.getCausedByPlayer();
            double defaultBreedingXp = Config.forgeConfig.defaultBreedingXp.get();
            String regKey = event.getChild().getEncodeId();
            Vec3 midPos = Util.getMidVec( event.getParentA().position(), event.getParentB().position() );
            Vec3 xpDropPos = new Vec3( midPos.x(), midPos.y() + event.getChild().getEyeHeight() + 0.523, midPos.z() );
            Map<String, Double> award = XP.getXp( event.getChild() , JType.XP_VALUE_BREED );
            if( award.size() == 0 )
                award.put( Skill.FARMING.toString(), defaultBreedingXp );

            if( XP.isHoldingDebugItemInOffhand( causedByPlayer ) )
                causedByPlayer.displayClientMessage( new TextComponent( regKey ), false );

            for( String awardSkillName : award.keySet() )
            {
                WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( causedByPlayer.getLevel() ), xpDropPos.x(), xpDropPos.y(), xpDropPos.z(), 0.5, award.get( awardSkillName ), awardSkillName );
                XP.addWorldXpDrop( xpDrop, causedByPlayer );
                Skill.addXp( awardSkillName, causedByPlayer, award.get( awardSkillName ), "breeding " + regKey, false, false );
            }
        }
    }
}