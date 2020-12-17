package harmonised.pmmo.ct;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import harmonised.pmmo.skills.Skill;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stanhebben.zenscript.annotations.ZenClass;

@ZenClass("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    public static final Logger LOGGER = LogManager.getLogger();

    @ZenClass("checkLevels")
    public static boolean checkLevels( IData args, IPlayer mcPlayer )
    {
        EntityPlayer player = CraftTweakerMC.getPlayer( mcPlayer );
        NBTTagCompound reqLevels = CraftTweakerMC.getNBTCompound( args );
        for( String key : reqLevels.getKeySet() )
        {
            if( reqLevels.getDouble( key ) > Skill.getLevelDecimal( key, player ) )
                return false;
        }
        return true;
    }

    @ZenClass("awardXp")
    public static void awardXp( IData args, IPlayer mcPlayer, boolean ignoreBonuses )
    {
        EntityPlayer player = CraftTweakerMC.getPlayer( mcPlayer );
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = CraftTweakerMC.getNBTCompound( args );
            for( String key : xpAwards.getKeySet() )
            {
                Skill.addXp( key, (EntityPlayerMP) player, xpAwards.getDouble( key ), "CraftTweaker", false, ignoreBonuses );
            }
        }
        else if( player instanceof EntityPlayerSP )
            LOGGER.info( "ZenScript -> PMMO -> awardXp -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> awardXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenClass("awardLevels")
    public static void awardLevels( IData args, IPlayer mcPlayer )
    {
        EntityPlayer player = CraftTweakerMC.getPlayer( mcPlayer );
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = CraftTweakerMC.getNBTCompound( args );
            for( String key : xpAwards.getKeySet() )
            {
                Skill.addLevel( key, (EntityPlayerMP) player, xpAwards.getDouble( key ), "CraftTweaker", false, true );
            }
        }
        else if( player instanceof EntityPlayerSP )
            LOGGER.info( "ZenScript -> PMMO -> awardLevels -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> awardLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenClass("setXp")
    public static void setXp( IData args, IPlayer mcPlayer )
    {
        EntityPlayer player = CraftTweakerMC.getPlayer( mcPlayer );
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = CraftTweakerMC.getNBTCompound( args );
            for( String key : xpAwards.getKeySet() )
            {
                Skill.setXp( key, (EntityPlayerMP) player, xpAwards.getDouble( key ) );
            }
        }
        else if( player instanceof EntityPlayerSP )
            LOGGER.info( "ZenScript -> PMMO -> setXp -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> setXp -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }

    @ZenClass("setLevels")
    public static void setLevels( IData args, IPlayer mcPlayer )
    {
        EntityPlayer player = CraftTweakerMC.getPlayer( mcPlayer );
        if( player instanceof EntityPlayerMP )
        {
            NBTTagCompound xpAwards = CraftTweakerMC.getNBTCompound( args );
            for( String key : xpAwards.getKeySet() )
            {
                Skill.setLevel( key, (EntityPlayerMP) player, xpAwards.getDouble( key ) );
            }
        }
        else if( player instanceof EntityPlayerSP )
            LOGGER.info( "ZenScript -> PMMO -> setLevels -> Called from Client!" );
        else
            LOGGER.info( "ZenScript -> PMMO -> setLevels -> Invalid Player Class! \"" + player.getClass().getName() + "\"" );
    }
}