package harmonised.pmmo.commands;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;

import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NearbyPowerLevelCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(  ) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        EntityPlayer sender = null;
        EntityPlayer target = null;

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch( CommandSyntaxException e )
        {
            //no sender, it's fine
        }

        if( args.length > 3 )
        {
            try
            {
                target = EntityArgument.getPlayer( context, "target" );
            }
            catch( CommandSyntaxException e )
            {
                //no target, target will be sender
            }
        }

        if( sender == null && target == null )
            LOGGER.info( "PMMO NearbyPowerLevel Command: Sender not player, and target is invalid!" );
        else
        {
            if( target == null )
                target = sender;

            double totalPowerLevel = 0;

            for( EntityPlayer player : XP.getNearbyPlayers( target ) )
            {
                totalPowerLevel += XP.getPowerLevel( player.getUniqueID() );
            }

            LOGGER.info( "PMMO NearbyPowerLevel Command Output: " + totalPowerLevel );

            if( sender != null )
                sender.sendStatusMessage( new TextComponentString( totalPowerLevel + " " + new TextComponentTranslation( "pmmo.power" ).getUnformattedText() ), false );
        }

        return 1;
    }
}
