package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NearbyPowerLevelCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        PlayerEntity sender = null;
        PlayerEntity target = null;

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
            LogHandler.LOGGER.error( "PMMO NearbyPowerLevel Command: Sender not player, and target is invalid!" );
        else
        {
            if( target == null )
                target = sender;

            double totalPowerLevel = 0;

            for( PlayerEntity player : XP.getNearbyPlayers( target ) )
            {
                totalPowerLevel += XP.getPowerLevel( player );
            }

            LogHandler.LOGGER.info( "PMMO NearbyPowerLevel Command Output: " + totalPowerLevel );

            if( sender != null )
                sender.sendStatusMessage( new StringTextComponent( totalPowerLevel + " " + new TranslationTextComponent( "pmmo.power" ).getString() ), false );
        }

        return 1;
    }
}
