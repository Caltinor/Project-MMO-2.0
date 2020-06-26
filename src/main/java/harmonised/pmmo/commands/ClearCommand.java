package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;

public class ClearCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        String[] args = context.getInput().split( " " );

        try
        {
            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

            for( ServerPlayerEntity player : players )
            {
                String playerName = player.getDisplayName().getString();
                AttributeHandler.updateAll( player );
                XP.updateRecipes( player );

                NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), player );
                player.getPersistentData().getCompound( "pmmo" ).put( "skills", new CompoundNBT() );
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.skillsCleared" ), false );

                LogHandler.LOGGER.info( "PMMO Command Clear: " + playerName + " has had their stats wiped!" );
            }
        }
        catch( CommandSyntaxException e )
        {
            LogHandler.LOGGER.error( "Clear Command Failed to get Players [" + Arrays.toString(args) + "]", e );
        }

        return 1;
    }
}
