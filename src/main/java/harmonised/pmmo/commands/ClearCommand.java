package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClearCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "clear";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        List<String> completions = new ArrayList<String>();
        completions.add( "iagreetothetermsandconditions" );
        return completions;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        try
        {
            Collection<EntityPlayerMP> players = EntityArgument.getPlayers( context, "target" );

            for( EntityPlayerMP player : players )
            {
                String playerName = player.getDisplayName().getUnformattedText();
                AttributeHandler.updateAll( player );
                XP.updateRecipes( player );

                Map<Skill, Double> xpMap = PmmoSavedData.get().getXpMap( player.getUniqueID() );
                for( Skill skill : new HashSet<>( xpMap.keySet() ) )
                {
                    xpMap.remove( skill );
                }
                NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), player );
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsCleared" ), false );

                LOGGER.info( "PMMO Command Clear: " + playerName + " has had their stats wiped!" );
            }
        }
        catch( Exception e )
        {
            LOGGER.info( "Clear Command Failed to get Players [" + Arrays.toString(args) + "]", e );
        }
    }
}
