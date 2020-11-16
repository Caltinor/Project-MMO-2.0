package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;

public class SyncCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSource> context, @Nullable Collection<EntityPlayerMP> players ) throws CommandException
    {
        if( players != null )
        {
            for( EntityPlayerMP player : players )
            {
                XP.syncPlayer( player );
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsResynced" ), false );
            }
        }
        else
        {
            try
            {
                EntityPlayer player = context.getSource().asPlayer();
                XP.syncPlayer( player );
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsResynced" ), false );
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.info( "Sync command fired not from player " + context.getInput(), e );
            }
        }

        return 1;
    }
}
