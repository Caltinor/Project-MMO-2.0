package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
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
//				System.out.println( player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG ) );

            if( XP.isPlayerSurvival( player ) )
            {
                double jumpBoost = 0;
                if ( player.isSneaking() )
                    jumpBoost = getCrouchJumpBoost( player );
                else if (player.isSprinting())
                    jumpBoost = getSprintJumpBoost( player );

                if ( player.world.isRemote )
                {
                    if( jumpBoost > 0 )
                        player.addVelocity( 0, jumpBoost, 0 );
                }
                else if (!player.isInWater())
                {
                    double jumpAmp = 0;

                    if( player.isPotionActive( MobEffects.JUMP_BOOST ) )
                        jumpAmp = player.getActivePotionEffect( MobEffects.JUMP_BOOST ).getAmplifier() + 1;

                    XP.awardXp( (EntityPlayerMP) player, Skill.AGILITY, "jumping", Math.max( (jumpBoost * 10 + 1) * ( 1 + jumpAmp / 4 ), 1 ), true, false, false );
                }
            }
        }
    }

    public static double getCrouchJumpBoost( EntityPlayer player )
    {
        Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
        double agilityLevel;
        double jumpBoost;
        double maxJumpBoost = FConfig.getConfig( "maxJumpBoost" );
        double maxJumpBoostPref = maxJumpBoost;
        int levelsPerCrouchJumpBoost = (int) Math.floor( FConfig.getConfig( "levelsPerCrouchJumpBoost" ) );
        agilityLevel = Skill.AGILITY.getLevel( player );
        if( prefsMap.containsKey( "maxCrouchJumpBoost" ) )
            maxJumpBoostPref = 0.14 * ( prefsMap.get( "maxCrouchJumpBoost" ) / 100);
        jumpBoost = -0.011 + agilityLevel * ( 0.14 / levelsPerCrouchJumpBoost );
        jumpBoost = Math.min( maxJumpBoostPref, Math.min( maxJumpBoost, jumpBoost ) );
        return jumpBoost;
    }

    public static double getSprintJumpBoost( EntityPlayer player )
    {
        Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
        double agilityLevel;
        double jumpBoost;
        double maxJumpBoost = FConfig.getConfig( "maxJumpBoost" );
        double maxJumpBoostPref = maxJumpBoost;
        int levelsPerSprintJumpBoost = (int) Math.floor( FConfig.getConfig( "levelsPerSprintJumpBoost" ) );
        agilityLevel = Skill.AGILITY.getLevel( player );
        if( prefsMap.containsKey( "maxSprintJumpBoost" ) )
            maxJumpBoostPref = 0.14 * ( prefsMap.get( "maxSprintJumpBoost" ) / 100);
        jumpBoost = -0.013 + agilityLevel * ( 0.14 / levelsPerSprintJumpBoost );
        jumpBoost = Math.min( maxJumpBoostPref, Math.min( maxJumpBoost, jumpBoost ) );
        return jumpBoost;
    }
}
