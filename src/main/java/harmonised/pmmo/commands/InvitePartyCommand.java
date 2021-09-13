package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class InvitePartyCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        ServerPlayer targetPlayer;
        UUID uuid = player.getUUID();

        try
        {
            targetPlayer = EntityArgument.getPlayer( context, "target" );
        }
        catch( CommandSyntaxException err )
        {
            LOGGER.error( "PMMO Invite Party Command Error: Target player does not exist. How..?" );
            return 1;
        }

        int result = PartyPendingSystem.createInvitation( targetPlayer, uuid );
        switch( result )
        {
            case -4:
                player.displayClientMessage( new TranslatableComponent( "pmmo.yourPartyIsFull" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -3:
                player.displayClientMessage( new TranslatableComponent( "pmmo.youAlreadyInvitedPlayerToYourParty", targetPlayer.getDisplayName() ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -2:
                player.displayClientMessage( new TranslatableComponent( "pmmo.playerAlreadyInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -1:
                player.displayClientMessage( new TranslatableComponent( "pmmo.youAreNotInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case 0:
                player.displayClientMessage( new TranslatableComponent( "pmmo.youHaveInvitedAPlayerToYourParty", targetPlayer.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                targetPlayer.displayClientMessage( new TranslatableComponent( "pmmo.playerInvitedYouToAParty", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                break;
        }

        return 1;
    }
}