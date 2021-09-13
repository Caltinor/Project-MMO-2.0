package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Map;

public class JumpHandler
{
    public static void handleJump( LivingEvent.LivingJumpEvent event )
    {
        if( event.getEntityLiving() instanceof Player && !(event.getEntityLiving() instanceof FakePlayer) )
        {
            Player player = (Player) event.getEntityLiving();

//			if( !player.world.isRemote )
//				System.out.println( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ) );

            if( XP.isPlayerSurvival( player ) )
            {
                double jumpBoost = 0;
                if ( player.isShiftKeyDown() )
                    jumpBoost = getCrouchJumpBoost( player );
                else if (player.isSprinting())
                    jumpBoost = getSprintJumpBoost( player );

                if ( player.level.isClientSide )
                {
                    if( jumpBoost > 0 )
                        player.push( 0, jumpBoost, 0 );
                }
                else if (!player.isInWater())
                {
                    double jumpAmp = 0;

                    if( player.hasEffect( MobEffects.JUMP ) )
                        jumpAmp = player.getEffect( MobEffects.JUMP ).getAmplifier() + 1;

                    Vec3 xpDropPos = player.position();
                    double award = Math.max( (jumpBoost * 10 + 1) * ( 1 + jumpAmp / 4 ), 1 );
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( player.getCommandSenderWorld() ), xpDropPos.x(), xpDropPos.y() + 0.523, xpDropPos.z(), 0.15, award, Skill.AGILITY.toString() );
                    XP.addWorldXpDrop( xpDrop, (ServerPlayer) player );
                    XP.awardXp( (ServerPlayer) player, Skill.AGILITY.toString(), "jumping", award, true, false, false );
                }
            }
        }
    }

    public static double getCrouchJumpBoost( Player player )
    {
        Map<String, Double> prefsMap = Config.getPreferencesMap( player );
        double agilityLevel;
        double jumpBoost;
        double maxJumpBoost = Config.getConfig( "maxJumpBoost" );
        double maxJumpBoostPref = maxJumpBoost;
        int levelsPerCrouchJumpBoost = (int) Math.floor( Config.getConfig( "levelsPerCrouchJumpBoost" ) );
        agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
        if( prefsMap.containsKey( "maxCrouchJumpBoost" ) )
            maxJumpBoostPref = prefsMap.get( "maxCrouchJumpBoost" );
        jumpBoost = -0.011 + agilityLevel * ( 0.14 / levelsPerCrouchJumpBoost );
        jumpBoost = Math.min( maxJumpBoostPref, Math.min( maxJumpBoost, jumpBoost ) );
        return jumpBoost;
    }

    public static double getSprintJumpBoost( Player player )
    {
        Map<String, Double> prefsMap = Config.getPreferencesMap( player );
        double agilityLevel;
        double jumpBoost;
        double maxJumpBoost = Config.getConfig( "maxJumpBoost" );
        double maxJumpBoostPref = maxJumpBoost;
        int levelsPerSprintJumpBoost = (int) Math.floor( Config.getConfig( "levelsPerSprintJumpBoost" ) );
        agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
        if( prefsMap.containsKey( "maxSprintJumpBoost" ) )
            maxJumpBoostPref = prefsMap.get( "maxSprintJumpBoost" );
        jumpBoost = -0.013 + agilityLevel * ( 0.14 / levelsPerSprintJumpBoost );
        jumpBoost = Math.min( maxJumpBoostPref, Math.min( maxJumpBoost, jumpBoost ) );
        return jumpBoost;
    }
}