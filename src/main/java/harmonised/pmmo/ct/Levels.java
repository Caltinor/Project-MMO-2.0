package harmonised.pmmo.ct;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.entity.player.MCPlayerEntity;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.FakePlayer;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    @ZenCodeType.Method("checkLevels")
    public static boolean checkLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        CompoundNBT reqLevels = args.getInternal();
        Skill skill;
        for( String key : reqLevels.keySet() )
        {
            skill = Skill.getSkill( key );
            if( skill.equals( Skill.INVALID_SKILL ) )
                LogHandler.LOGGER.error( "ZenScript -> PMMO -> checkLevels -> Invalid Skill Provided! \"" + key + "\"" );
            if( reqLevels.getDouble( key ) > skill.getLevelDecimal( player ) )
                return false;
        }
        return true;
    }

    @ZenCodeType.Method("awardXp")
    public static void awardXp( MapData args, MCPlayerEntity mcPlayer, boolean ignoreBonuses )
    {
        PlayerEntity player = mcPlayer.getInternal();
        if( player instanceof ServerPlayerEntity )
        {
            CompoundNBT xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.keySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardXp -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.addXp( (ServerPlayerEntity) player, xpAwards.getDouble( key ), ignoreBonuses );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardXp -> Called from Client!" );
        else
            LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("awardLevels")
    public static void awardLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        if( player instanceof ServerPlayerEntity )
        {
            CompoundNBT xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.keySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardLevels -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.addLevel( (ServerPlayerEntity) player, xpAwards.getDouble( key ), true );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardLevels -> Called from Client!" );
        else
            LogHandler.LOGGER.error( "ZenScript -> PMMO -> awardLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }
}
