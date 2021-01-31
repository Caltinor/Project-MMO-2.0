package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class DeclinePartyCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID( uuid );
        if( partyOwnerUUID == null )
        {
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
            return 1;
        }
        ServerPlayerEntity ownerPlayer = XP.getPlayerByUUID( partyOwnerUUID, player.getServer() );
        boolean result = PartyPendingSystem.declineInvitation( uuid );
        if( result )
        {
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.youHaveDeclinedPartyInvitation" ).setStyle(XP.textStyle.get( "yellow" ) ), false );
            if( ownerPlayer != null )
                ownerPlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.playerDeclinedYourPartyInvitation", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
        }
        else
        {
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
        }
        return 1;
    }
}
