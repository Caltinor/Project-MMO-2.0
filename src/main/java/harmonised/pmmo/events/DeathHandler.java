package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class DeathHandler
{
    public static void handleDeath( LivingDeathEvent event )
    {
        LivingEntity target = event.getEntityLiving();
        Entity source = event.getSource().getTrueSource();
        double deathXpPenaltyMultiplier = Config.forgeConfig.deathXpPenaltyMultiplier.get();
        double passiveMobHunterXp = Config.forgeConfig.passiveMobHunterXp.get();
        double aggresiveMobSlayerXp = Config.forgeConfig.aggresiveMobSlayerXp.get();

        if( target instanceof PlayerEntity && !( target instanceof FakePlayer) )
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if( !player.world.isRemote() )
            {
                CompoundNBT skillsTag = XP.getSkillsTag( player );
                CompoundNBT prefsTag = XP.getPreferencesTag( player );
                double totalLost = 0;
                boolean wipeAllSkills = Config.forgeConfig.wipeAllSkillsUponDeathPermanently.get();
                if( prefsTag.contains( "wipeAllSkillsUponDeathPermanently" ) && prefsTag.getDouble( "wipeAllSkillsUponDeathPermanently" ) != 0 )
                    wipeAllSkills = true;

                if( wipeAllSkills )
                {
                    for( String key : new HashSet<String>( skillsTag.keySet() ) )
                    {
                        totalLost += skillsTag.getDouble( key );
                        skillsTag.remove( key );
                    }
                }
                else
                {
                    for( String key : new HashSet<String>( skillsTag.keySet() ) )
                    {
                        double startXp = skillsTag.getDouble( key );
                        double floorXp = XP.xpAtLevelDecimal( Math.floor( XP.levelAtXpDecimal( startXp ) ) );
                        double diffXp = startXp - floorXp;
                        double lostXp = diffXp * deathXpPenaltyMultiplier;
                        double finalXp = startXp - lostXp;
                        totalLost += lostXp;

                        if( finalXp > 0 )
                            skillsTag.putDouble( key, finalXp );
                        else
                            skillsTag.remove( key );
                    }
                }

                if( totalLost > 0 )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.lostXp", DP.dprefix( totalLost ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                
                XP.syncPlayer( player );
            }
        }
        else if( source instanceof PlayerEntity && !( source instanceof FakePlayer ) )
        {
            PlayerEntity player = (PlayerEntity) source;
            Collection<PlayerEntity> nearbyPlayers = XP.getNearbyPlayers( target );
            double scaleValue = 0;

            for( PlayerEntity thePlayer : nearbyPlayers )
            {
                if( XP.getPowerLevel( player ) > 1 )
                    scaleValue += 1;
                else
                    scaleValue += XP.getPowerLevel( thePlayer );
            }

            scaleValue /= 5;

            if( scaleValue < 1 )
                scaleValue = 1;

            if( scaleValue > 10 )
                scaleValue = 10;

//            double normalMaxHp = target.getAttribute( Attributes.GENERIC_MAX_HEALTH ).getBaseValue();
//            double scaleMultiplier = ( 1 + ( target.getMaxHealth() - normalMaxHp ) / 10 );

            if( JsonConfig.data.get( JType.XP_VALUE_KILL ).containsKey( target.getEntityString() ) )
            {
                Map<String, Object> killXp = JsonConfig.data.get( JType.XP_VALUE_KILL ).get( target.getEntityString() );
                for( Map.Entry<String, Object> entry : killXp.entrySet() )
                {
                    XP.awardXp( player, Skill.getSkill( entry.getKey() ), player.getHeldItemMainhand().getDisplayName().toString(), (double) entry.getValue() * scaleValue, false, false );
                }
            }
            else if( target instanceof AnimalEntity)
                XP.awardXp( player, Skill.HUNTER, player.getHeldItemMainhand().getDisplayName().toString(), passiveMobHunterXp * scaleValue, false, false );
            else if( target instanceof MobEntity)
                XP.awardXp( player, Skill.SLAYER, player.getHeldItemMainhand().getDisplayName().toString(), aggresiveMobSlayerXp * scaleValue, false, false );

            if( JsonConfig.data.get( JType.MOB_RARE_DROP ).containsKey( target.getEntityString() ) )
            {
                Map<String, Object> dropTable = JsonConfig.data.get( JType.MOB_RARE_DROP ).get( target.getEntityString() );

                double chance;

                for( Map.Entry<String, Object> entry : dropTable.entrySet() )
                {
                    chance = (double) entry.getValue();
                    chance /= scaleValue;

                    if( Math.floor( Math.random() * chance ) == 0 )
                    {
                        ItemStack itemStack = new ItemStack( XP.getItem( entry.getKey() ) );
                        XP.dropItemStack( itemStack, player.world, target.getPositionVec() );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ), true );
                    }
                }
            }
        }
    }
}
