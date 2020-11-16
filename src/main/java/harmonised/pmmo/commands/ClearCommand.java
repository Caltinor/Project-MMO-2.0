package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ClearCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        String[] args = context.getInput().split( " " );

        try
        {
            Collection<EntityPlayerMP> players = EntityArgument.getPlayers( context, "target" );

            for( EntityPlayerMP player : players )
            {
                String playerName = player.getDisplayName().getUnformattedText();
                AttributeHandler.updateAll( player );
                XP.updateRecipes( player );

                Map<Skill, Double> xpMap = PmmoSavedData.get().getXpMap( player.getUniqueID() );
                for( Skill skill : new HashSet<>( xpMap.getKeySet() ) )
                {
                    xpMap.remove( skill );
                }
                NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), player );
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsCleared" ), false );

                LOGGER.info( "PMMO Command Clear: " + playerName + " has had their stats wiped!" );
            }
        }
        catch( CommandSyntaxException e )
        {
            LOGGER.info( "Clear Command Failed to get Players [" + Arrays.toString(args) + "]", e );
        }

        return 1;
    }
}
