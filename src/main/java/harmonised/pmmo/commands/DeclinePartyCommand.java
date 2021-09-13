package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.UUID;

public class DeclinePartyCommand
{
    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        UUID uuid = player.getUUID();
        UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID( uuid );
        if( partyOwnerUUID == null )
        {
            player.displayClientMessage( new TranslatableComponent( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
            return 1;
        }
        ServerPlayer ownerPlayer = XP.getPlayerByUUID( partyOwnerUUID, player.getServer() );
        boolean result = PartyPendingSystem.declineInvitation( uuid );
        if( result )
        {
            player.displayClientMessage( new TranslatableComponent( "pmmo.youHaveDeclinedPartyInvitation" ).setStyle(XP.textStyle.get( "yellow" ) ), false );
            if( ownerPlayer != null )
                ownerPlayer.displayClientMessage( new TranslatableComponent( "pmmo.playerDeclinedYourPartyInvitation", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
        }
        else
        {
            player.displayClientMessage( new TranslatableComponent( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
        }
        return 1;
    }
}
