package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.UUID;

public class LeavePartyCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        UUID uuid = player.getUUID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party party = pmmoSavedData.getParty(uuid);
        int result = pmmoSavedData.removeFromParty(uuid);
        switch(result)
        {
            case -1:
                player.displayClientMessage(new TranslatableComponent("pmmo.youAreNotInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case 0:
            case 1:
                Collection<ServerPlayer> members = party.getOnlineMembers(player.getServer());
                for(ServerPlayer memberPlayer : members)
                {
                    if(!memberPlayer.getUUID().equals(uuid))
                        memberPlayer.displayClientMessage(new TranslatableComponent("pmmo.playerLeftYourParty", player.getDisplayName()).setStyle(XP.textStyle.get("red")), false);
                }
                player.displayClientMessage(new TranslatableComponent("pmmo.youLeftTheParty").setStyle(XP.textStyle.get("green")), false);
                if(result == 1)
                    player.displayClientMessage(new TranslatableComponent("pmmo.disbandedParty").setStyle(XP.textStyle.get("yellow")), false);
                break;
        }

        return 1;
    }
}
