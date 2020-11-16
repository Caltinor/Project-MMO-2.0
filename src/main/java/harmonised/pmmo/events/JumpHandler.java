package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Map;

public class JumpHandler
{
    public static void handleJump( LivingEvent.LivingJumpEvent event )
    {
        if( event.getEntityLiving() instanceof EntityPlayer && !(event.getEntityLiving() instanceof FakePlayer) )
        {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

//			if( !player.world.isRemote )
//				System.out.println( player.getPersistentData() );

            if( XP.isPlayerSurvival( player ) )
            {
                Map<String, Double> prefsMap;

                prefsMap = Config.getPreferencesMap( player );

                double agilityLevel = 1;
                double jumpBoost = 0;
                double maxJumpBoost = Config.getConfig( "maxJumpBoost" );
                double maxJumpBoostPref = maxJumpBoost;
                int levelsPerCrouchJumpBoost = (int) Math.floor( Config.getConfig( "levelsPerCrouchJumpBoost" ) );
                int levelsPerSprintJumpBoost = (int) Math.floor( Config.getConfig( "levelsPerSprintJumpBoost" ) );

                agilityLevel = Skill.AGILITY.getLevel( player );

                if ( player.isSneaking() )
                {
                    if( prefsMap.containsKey( "maxCrouchJumpBoost" ) )
                        maxJumpBoostPref = 0.14 * ( prefsMap.get( "maxCrouchJumpBoost" ) / 100);
                    jumpBoost = -0.011 + agilityLevel * ( 0.14 / levelsPerCrouchJumpBoost );
                }

                if (player.isSprinting())
                {
                    if( prefsMap.containsKey( "maxSprintJumpBoost" ) )
                        maxJumpBoostPref = 0.14 * ( prefsMap.get( "maxSprintJumpBoost" ) / 100);
                    jumpBoost = -0.013 + agilityLevel * ( 0.14 / levelsPerSprintJumpBoost );
                }

                if ( jumpBoost > maxJumpBoost )
                    jumpBoost = maxJumpBoost;
                if( jumpBoost > maxJumpBoostPref )
                    jumpBoost = maxJumpBoostPref;

                if (player.world.isRemote)
                {
                    if( jumpBoost > 0 )
                        player.setVelocity( player.getMotion().x, player.getMotion().y + jumpBoost, player.getMotion().z );
                }
                else if (!player.isInWater())
                {
                    float jumpAmp = 0;

                    if( player.isPotionActive( MobEffects.JUMP_BOOST ) )
                        jumpAmp = player.getActivePotionEffect( MobEffects.JUMP_BOOST ).getAmplifier() + 1;

                    XP.awardXp( (EntityPlayerMP) player, Skill.AGILITY, "jumping", Math.max( (jumpBoost * 10 + 1) * ( 1 + jumpAmp / 4 ), 1 ), true, false, false );
                }
            }
        }
    }
}
