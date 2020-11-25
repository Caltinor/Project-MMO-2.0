package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

public class DeclinePartyCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        EntityPlayerMP player = (EntityPlayerMP) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID( uuid );
        if( partyOwnerUUID == null )
        {
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
            return 1;
        }
        EntityPlayerMP ownerPlayer = XP.getPlayerByUUID( partyOwnerUUID, player.getServer() );
        boolean result = PartyPendingSystem.declineInvitation( uuid );
        if( result )
        {
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youHaveDeclinedPartyInvitation" ).setStyle(XP.textStyle.get( "yellow" ) ), false );
            if( ownerPlayer != null )
                ownerPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.playerDeclinedYourPartyInvitation", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
        }
        else
        {
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
        }
        return 1;
    }
}
