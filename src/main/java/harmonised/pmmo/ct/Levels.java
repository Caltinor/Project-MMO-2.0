package harmonised.pmmo.ct;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.entity.player.MCEntityPlayer;
import harmonised.pmmo.skills.Skill;
import net.minecraft.client.entity.player.ClientEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    public static final Logger LOGGER = LogManager.getLogger();

    @ZenCodeType.Method("checkLevels")
    public static boolean checkLevels( MapData args, MCEntityPlayer mcPlayer )
    {
        EntityPlayer player = mcPlayer.getInternal();
        NBTTagCompound reqLevels = args.getInternal();
        Skill skill;
        for( String key : reqLevels.getKeySet() )
        {
            skill = Skill.getSkill( key );
            if( skill.equals( Skill.INVALID_SKILL ) )
                LOGGER.info( "ZenScript -> PMMO -> checkLevels -> Invalid Skill Provided! \"" + key + "\"" );
            if( reqLevels.getDouble( key ) > skill.getLevelDecimal( player ) )
                return false;
        }
        return true;
    }

    @ZenCodeType.Method("awardXp")
    public static void awardXp( MapData args, MCEntityPlayer mcPlayer, boolean ignoreBonuses )
    {
        EntityPlayer player = mcPlayer.getInternal();
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.getKeySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LOGGER.info( "ZenScript -> PMMO -> awardXp -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.addXp( (EntityPlayerMP) player, xpAwards.getDouble( key ), "CraftTweaker", false, ignoreBonuses );
            }
        }
        else if( player instanceof ClientEntityPlayer )
            LOGGER.info( "ZenScript -> PMMO -> awardXp -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> awardXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("awardLevels")
    public static void awardLevels( MapData args, MCEntityPlayer mcPlayer )
    {
        EntityPlayer player = mcPlayer.getInternal();
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.getKeySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LOGGER.info( "ZenScript -> PMMO -> awardLevels -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.addLevel( (EntityPlayerMP) player, xpAwards.getDouble( key ), "CraftTweaker", false, true );
            }
        }
        else if( player instanceof ClientEntityPlayer )
            LOGGER.info( "ZenScript -> PMMO -> awardLevels -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> awardLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("setXp")
    public static void setXp( MapData args, MCEntityPlayer mcPlayer )
    {
        EntityPlayer player = mcPlayer.getInternal();
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.getKeySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LOGGER.info( "ZenScript -> PMMO -> setXp -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.setXp( (EntityPlayerMP) player, xpAwards.getDouble( key ) );
            }
        }
        else if( player instanceof ClientEntityPlayer )
            LOGGER.info( "ZenScript -> PMMO -> setXp -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> setXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenCodeType.Method("setLevels")
    public static void setLevels( MapData args, MCEntityPlayer mcPlayer )
    {
        EntityPlayer player = mcPlayer.getInternal();
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = args.getInternal();
            Skill skill;
            for( String key : xpAwards.getKeySet() )
            {
                skill = Skill.getSkill( key );
                if( skill.equals( Skill.INVALID_SKILL ) )
                    LOGGER.info( "ZenScript -> PMMO -> setLevels -> Invalid Skill Provided! \"" + key + "\"" );
                else
                    skill.setLevel( (EntityPlayerMP) player, xpAwards.getDouble( key ) );
            }
        }
        else if( player instanceof ClientEntityPlayer )
            LOGGER.info( "ZenScript -> PMMO -> setLevels -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> setLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }
}
