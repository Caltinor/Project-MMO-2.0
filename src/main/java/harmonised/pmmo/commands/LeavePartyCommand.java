package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.UUID;

public class LeavePartyCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party party = pmmoSavedData.getParty(uuid);
        int result = pmmoSavedData.removeFromParty(uuid);
        switch(result)
        {
            case -1:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youAreNotInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case 0:
            case 1:
                Collection<ServerPlayerEntity> members = party.getOnlineMembers(player.getServer());
                for(ServerPlayerEntity memberPlayer : members)
                {
                    if(!memberPlayer.getUniqueID().equals(uuid))
                        memberPlayer.sendStatusMessage(new TranslationTextComponent("pmmo.playerLeftYourParty", player.getDisplayName()).setStyle(XP.textStyle.get("red")), false);
                }
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youLeftTheParty").setStyle(XP.textStyle.get("green")), false);
                if(result == 1)
                    player.sendStatusMessage(new TranslationTextComponent("pmmo.disbandedParty").setStyle(XP.textStyle.get("yellow")), false);
                break;
        }

        return 1;
    }
}
