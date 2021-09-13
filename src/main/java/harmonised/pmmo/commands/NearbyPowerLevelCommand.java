package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NearbyPowerLevelCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        String[] args = context.getInput().split( " " );
        Player sender = null;
        Player target = null;

        try
        {
            sender = context.getSource().getPlayerOrException();
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
            LOGGER.error( "PMMO NearbyPowerLevel Command: Sender not player, and target is invalid!" );
        else
        {
            if( target == null )
                target = sender;

            double totalPowerLevel = 0;

            for( Player player : XP.getNearbyPlayers( target ) )
            {
                totalPowerLevel += XP.getPowerLevel( player.getUUID() );
            }

            LOGGER.info( "PMMO NearbyPowerLevel Command Output: " + totalPowerLevel );

            if( sender != null )
                sender.displayClientMessage( new TextComponent( totalPowerLevel + " " + new TranslatableComponent( "pmmo.power" ).getString() ), false );
        }

        return 1;
    }
}
