package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.DP;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckBiomeCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "checkBiome";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return new ArrayList<>();
    }

    @Override
    public List<String> getAliases()
    {
        return new ArrayList<>();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        EntityPlayerMP player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch( PlayerNotFoundException e )
        {
            LOGGER.info( "CheckBiome command fired not from player " + args, e );
            return;
        }

        String biomeKey = player.world.getBiome( player.getPosition() ).getRegistryName().toString();
        String biomeName = player.world.getBiome( player.getPosition() ).getBiomeName();
        Map<String, Double> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );

        String damageBonus = "100";
        String hpBonus = "100";
        String speedBonus = "100";

        if( theMap != null )
        {
            if( theMap.containsKey( "damageBonus" ) )
                damageBonus = DP.dp( theMap.get( "damageBonus" ) * 100 );
            if( theMap.containsKey( "hpBonus" ) )
                hpBonus = DP.dp( theMap.get( "hpBonus" ) * 100 );
            if( theMap.containsKey( "damageBonus" ) )
                speedBonus = DP.dp( theMap.get( "damageBonus" ) * 100 );
        }

        player.sendStatusMessage( new TextComponentTranslation( "pmmo.mobDamageBoost", new TextComponentTranslation( damageBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( biomeName ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        player.sendStatusMessage( new TextComponentTranslation( "pmmo.mobHpBoost", new TextComponentTranslation( hpBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( biomeName ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        player.sendStatusMessage( new TextComponentTranslation( "pmmo.mobSpeedBoost", new TextComponentTranslation( speedBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( biomeName ).setStyle( XP.textStyle.get( "grey" ) ) ), false );

        return;
    }
}
