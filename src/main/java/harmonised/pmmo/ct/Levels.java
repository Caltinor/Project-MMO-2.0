package harmonised.pmmo.ct;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.entity.player.MCPlayerEntity;
import harmonised.pmmo.skills.Skill;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    public static final Logger LOGGER = LogManager.getLogger();

    @ZenCodeType.Method("checkLevels")
    public static boolean checkLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        CompoundNBT reqLevels = args.getInternal();
        for( String skill : reqLevels.keySet() )
        {
            if( reqLevels.getDouble( skill ) > Skill.getLevelDecimal( skill, player ) )
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
            for( String skill : xpAwards.keySet() )
            {
                Skill.addXp( skill, (ServerPlayerEntity) player, xpAwards.getDouble( skill ), "CraftTweaker", false, ignoreBonuses );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LOGGER.error( "ZenScript -> PMMO -> awardXp -> Called from Client!" );
        else
            LOGGER.error( "ZenScript -> PMMO -> awardXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("awardLevels")
    public static void awardLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        if( player instanceof ServerPlayerEntity )
        {
            CompoundNBT xpAwards = args.getInternal();
            for( String skill : xpAwards.keySet() )
            {
                Skill.addLevel( skill, (ServerPlayerEntity) player, xpAwards.getDouble( skill ), "CraftTweaker", false, true );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LOGGER.error( "ZenScript -> PMMO -> awardLevels -> Called from Client!" );
        else
            LOGGER.error( "ZenScript -> PMMO -> awardLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("setXp")
    public static void setXp( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        if( player instanceof ServerPlayerEntity )
        {
            CompoundNBT xpAwards = args.getInternal();
            for( String skill : xpAwards.keySet() )
            {
                Skill.setXp( skill, (ServerPlayerEntity) player, xpAwards.getDouble( skill ) );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LOGGER.error( "ZenScript -> PMMO -> setXp -> Called from Client!" );
        else
            LOGGER.error( "ZenScript -> PMMO -> setXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("setLevels")
    public static void setLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        if( player instanceof ServerPlayerEntity )
        {
            CompoundNBT xpAwards = args.getInternal();
            for( String skill : xpAwards.keySet() )
            {
                Skill.setLevel( skill, (ServerPlayerEntity) player, xpAwards.getDouble( skill ) );
            }
        }
        else if( player instanceof ClientPlayerEntity )
            LOGGER.error( "ZenScript -> PMMO -> setLevels -> Called from Client!" );
        else
            LOGGER.error( "ZenScript -> PMMO -> setLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }
}
