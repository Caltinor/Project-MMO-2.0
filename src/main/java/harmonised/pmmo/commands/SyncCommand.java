package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;

public class SyncCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSourceStack> context, @Nullable Collection<ServerPlayer> players ) throws CommandRuntimeException
    {
        if( players != null )
        {
            for( ServerPlayer player : players )
            {
                XP.syncPlayer( player );
                player.displayClientMessage( new TranslatableComponent( "pmmo.skillsResynced" ), false );
            }
        }
        else
        {
            try
            {
                Player player = context.getSource().getPlayerOrException();
                XP.syncPlayer( player );
                player.displayClientMessage( new TranslatableComponent( "pmmo.skillsResynced" ), false );
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.error( "Sync command fired not from player " + context.getInput(), e );
            }
        }

        return 1;
    }
}
