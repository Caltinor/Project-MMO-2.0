package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
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
    private static double deathXpPenaltyMultiplier = Config.forgeConfig.deathXpPenaltyMultiplier.get();
    private static double passiveMobHunterXp = Config.forgeConfig.passiveMobHunterXp.get();
    private static double aggresiveMobSlayerXp = Config.forgeConfig.aggresiveMobSlayerXp.get();

    public static void handleDeath( LivingDeathEvent event )
    {
        LivingEntity target = event.getEntityLiving();
        Entity source = event.getSource().getTrueSource();

        if( target instanceof PlayerEntity && !( target instanceof FakePlayer) )
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if( !player.world.isRemote() )
            {
                CompoundNBT skillsTag = XP.getSkillsTag( player );

                for( String key : new HashSet<String>( skillsTag.keySet() ) )
                {
                    double startXp = skillsTag.getDouble( key );
                    double floorXp = XP.xpAtLevelDecimal( Math.floor( XP.levelAtXpDecimal( startXp ) ) );
                    double diffXp = startXp - floorXp;
                    double finalXp = floorXp + diffXp * (1 - deathXpPenaltyMultiplier);

                    if( finalXp > 0 )
                        skillsTag.putDouble( key, finalXp );
                    else
                        skillsTag.remove( key );
                }

                XP.syncPlayer( player );
            }
        }
        else if( source instanceof PlayerEntity && !( source instanceof FakePlayer ) )
        {
            PlayerEntity player = (PlayerEntity) source;

            double normalMaxHp = target.getAttribute( SharedMonsterAttributes.MAX_HEALTH ).getBaseValue();
            double scaleMultiplier = ( 1 + ( target.getMaxHealth() - normalMaxHp ) / 10 );

            if( JsonConfig.data.get( "killXp" ).containsKey( target.getEntityString() ) )
            {
                Map<String, Object> killXp = JsonConfig.data.get( "killXp" ).get( target.getEntityString() );
                for( Map.Entry<String, Object> entry : killXp.entrySet() )
                {
                    XP.awardXp( player, Skill.getSkill( entry.getKey() ), player.getHeldItemMainhand().getDisplayName().toString(), (double) entry.getValue() * scaleMultiplier, false );
                }
            }
            else if( target instanceof AnimalEntity)
                XP.awardXp( player, Skill.HUNTER, player.getHeldItemMainhand().getDisplayName().toString(), passiveMobHunterXp * scaleMultiplier, false );
            else if( target instanceof MobEntity)
                XP.awardXp( player, Skill.SLAYER, player.getHeldItemMainhand().getDisplayName().toString(), aggresiveMobSlayerXp * scaleMultiplier, false );

//					System.out.println( ( target.getMaxHealth() - normalMaxHp ) / 10 );

            if( JsonConfig.data.get( "mobRareDrop" ).containsKey( target.getEntityString() ) )
            {
                Map<String, Object> dropTable = JsonConfig.data.get( "mobRareDrop" ).get( target.getEntityString() );
                Collection<PlayerEntity> allPlayers = XP.getNearbyPlayers( target );
                double chance;

                for( Map.Entry<String, Object> entry : dropTable.entrySet() )
                {
                    chance = ( (double) entry.getValue() / allPlayers.size() );

                    System.out.println( entry.getValue() );
                    System.out.println( (double) entry.getValue() / allPlayers.size() );

                    if( Math.floor( Math.random() * chance ) == 0 )
                    {
                        ItemStack itemStack = new ItemStack( XP.getItem( entry.getKey() ) );
                        XP.dropItemStack( itemStack, player.world, target.getPosition() );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.rareDrop", new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ), true );
                    }
                }
            }
        }
    }
}
