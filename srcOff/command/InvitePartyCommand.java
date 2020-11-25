package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class InvitePartyCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        EntityPlayer player = (EntityPlayer) context.getSource().getEntity();
        EntityPlayerMP targetPlayer;
        UUID uuid = player.getUniqueID();

        try
        {
            targetPlayer = EntityArgument.getPlayer( context, "target" );
        }
        catch( CommandSyntaxException err )
        {
            LOGGER.info( "PMMO Invite Party Command Error: Target player does not exist. How..?" );
            return 1;
        }

        int result = PartyPendingSystem.createInvitation( targetPlayer, uuid );
        switch( result )
        {
            case -4:
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.yourPartyIsFull" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -3:
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAlreadyInvitedPlayerToYourParty", targetPlayer.getDisplayName() ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -2:
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.playerAlreadyInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case -1:
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                break;

            case 0:
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.youHaveInvitedAPlayerToYourParty", targetPlayer.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                targetPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.playerInvitedYouToAParty", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                break;
        }

        return 1;
    }
}