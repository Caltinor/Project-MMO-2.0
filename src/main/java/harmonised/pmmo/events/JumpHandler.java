package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

public class JumpHandler
{
    public static void handleJump( LivingEvent.LivingJumpEvent event )
    {
        if( event.getEntityLiving() instanceof PlayerEntity && !(event.getEntityLiving() instanceof FakePlayer) )
        {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

//			if( !player.world.isRemote )
//				System.out.println( player.getPersistentData() );

            if( XP.isPlayerSurvival( player ) )
            {
                CompoundNBT prefsTag = XP.getPreferencesTag(player);

                double agilityLevel = 1;
                double jumpBoost = 0;
                double maxJumpBoost = Config.getConfig( "maxJumpBoost" );
                double maxJumpBoostPref = maxJumpBoost;
                int levelsCrouchJumpBoost = (int) Math.floor( Config.getConfig( "levelsCrouchJumpBoost" ) );
                int levelsSprintJumpBoost = (int) Math.floor( Config.getConfig( "levelsSprintJumpBoost" ) );

                agilityLevel = XP.getLevel( Skill.AGILITY, player );

                if (player.isCrouching())
                {
                    if( prefsTag.contains( "maxCrouchJumpBoost" ) )
                        maxJumpBoostPref = 0.14 * (prefsTag.getDouble( "maxCrouchJumpBoost" ) / 100);
                    jumpBoost = -0.011 + agilityLevel * ( 0.14 / levelsCrouchJumpBoost );
                }

                if (player.isSprinting())
                {
                    if( prefsTag.contains( "maxSprintJumpBoost" ) )
                        maxJumpBoostPref = 0.14 * (prefsTag.getDouble( "maxSprintJumpBoost" ) / 100);
                    jumpBoost = -0.013 + agilityLevel * ( 0.14 / levelsSprintJumpBoost );
                }

                if ( jumpBoost > maxJumpBoost )
                    jumpBoost = maxJumpBoost;
                if( jumpBoost > maxJumpBoostPref )
                    jumpBoost = maxJumpBoostPref;

                if (player.world.isRemote)
                {
                    if( jumpBoost > 0 )
                        player.setMotion( player.getMotion().x, player.getMotion().y + jumpBoost, player.getMotion().z );
                }
                else if (!player.isInWater())
                {
                    float jumpAmp = 0;

                    if( player.isPotionActive( Effects.JUMP_BOOST ) )
                        jumpAmp = player.getActivePotionEffect( Effects.JUMP_BOOST ).getAmplifier() + 1;

                    XP.awardXp( player, Skill.AGILITY, "jumping", (jumpBoost * 10 + 1) * ( 1 + jumpAmp / 4 ), true );
                }
            }
        }
    }
}
