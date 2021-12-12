package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.UUID;

public class AcceptPartyCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID(uuid);
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        if(partyOwnerUUID == null)
        {
            //Invitation doesn't exist
            player.sendStatusMessage(new TranslationTextComponent("pmmo.youAreNotInvitedToAnyParty").setStyle(XP.textStyle.get("red")), false);
            return 1;
        }
        int result = PartyPendingSystem.acceptInvitation(player, partyOwnerUUID);
        switch(result)
        {
            case -4:
                //Party is full
                player.sendStatusMessage(new TranslationTextComponent("pmmo.thePartyIsFull").setStyle(XP.textStyle.get("red")), false);
                break;

            case -3:
                //Invitation doesn't exist: Either expired, or didn't exist
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youAreNotInvitedToAnyParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case -2:
                //You are already in a Party
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youAreAlreadyInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case -1:
                //Owner does not have a party
                player.sendStatusMessage(new TranslationTextComponent("pmmo.playerDoesNotHaveAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case 0:
                //You have joined the party
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youJoinedAParty").setStyle(XP.textStyle.get("green")), false);

                Party party = pmmoSavedData.getParty(uuid);
                Collection<ServerPlayerEntity> members = party.getOnlineMembers(player.getServer());
                for(ServerPlayerEntity memberPlayer : members)
                {
                    if(!memberPlayer.getUniqueID().equals(uuid))
                        memberPlayer.sendStatusMessage(new TranslationTextComponent("pmmo.playerJoinedYourParty", player.getDisplayName()).setStyle(XP.textStyle.get("green")), false);
                }
                break;
        }
        return 1;
    }
}