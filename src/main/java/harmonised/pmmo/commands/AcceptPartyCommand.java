package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.UUID;

public class AcceptPartyCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        UUID uuid = player.getUUID();
        UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID(uuid);
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        if(partyOwnerUUID == null)
        {
            //Invitation doesn't exist
            player.displayClientMessage(new TranslatableComponent("pmmo.youAreNotInvitedToAnyParty").setStyle(XP.textStyle.get("red")), false);
            return 1;
        }
        int result = PartyPendingSystem.acceptInvitation(player, partyOwnerUUID);
        switch(result)
        {
            case -4:
                //Party is full
                player.displayClientMessage(new TranslatableComponent("pmmo.thePartyIsFull").setStyle(XP.textStyle.get("red")), false);
                break;

            case -3:
                //Invitation doesn't exist: Either expired, or didn't exist
                player.displayClientMessage(new TranslatableComponent("pmmo.youAreNotInvitedToAnyParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case -2:
                //You are already in a Party
                player.displayClientMessage(new TranslatableComponent("pmmo.youAreAlreadyInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case -1:
                //Owner does not have a party
                player.displayClientMessage(new TranslatableComponent("pmmo.playerDoesNotHaveAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case 0:
                //You have joined the party
                player.displayClientMessage(new TranslatableComponent("pmmo.youJoinedAParty").setStyle(XP.textStyle.get("green")), false);

                Party party = pmmoSavedData.getParty(uuid);
                Collection<ServerPlayer> members = party.getOnlineMembers(player.getServer());
                for(ServerPlayer memberPlayer : members)
                {
                    if(!memberPlayer.getUUID().equals(uuid))
                        memberPlayer.displayClientMessage(new TranslatableComponent("pmmo.playerJoinedYourParty", player.getDisplayName()).setStyle(XP.textStyle.get("green")), false);
                }
                break;
        }
        return 1;
    }
}