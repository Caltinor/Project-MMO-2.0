package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        UUID uuid = player.getUUID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party party = pmmoSavedData.getParty(uuid);

        if(party == null)
            player.displayClientMessage(new TranslatableComponent("pmmo.youAreNotInAParty").setStyle(XP.textStyle.get("red")), false);
        else
        {
            Set<PartyMemberInfo> membersInfo = party.getAllMembersInfo();
            Set<UUID> membersInRangeUUID = party.getOnlineMembersInRange((ServerPlayer) player).stream().map(playerToMap -> playerToMap.getUUID()).collect(Collectors.toSet());
            double totalXpGained = party.getTotalXpGained();
            player.displayClientMessage(new TranslatableComponent("pmmo.youAreInAParty").setStyle(XP.textStyle.get("green")), false);
            player.displayClientMessage(new TranslatableComponent("pmmo.totalMembersOutOfMax", party.getPartySize(), Party.getMaxPartyMembers()).setStyle(XP.textStyle.get("green")), false);
            player.displayClientMessage(new TranslatableComponent("pmmo.partyTotalXpGained", DP.dpSoft(totalXpGained)).setStyle(XP.textStyle.get("green")), false);
            player.displayClientMessage(new TranslatableComponent("pmmo.partyXpBonus", DP.dpSoft((party.getMultiplier(membersInRangeUUID.size()) - 1) * 100)).setStyle(XP.textStyle.get("green")), false);
            for(PartyMemberInfo memberInfo : membersInfo)
            {
                String xpGainedPercentage = DP.dpSoft((memberInfo.xpGained / totalXpGained) * 100);
                String color = "yellow";
                if(!memberInfo.uuid.equals(uuid))
                    color = membersInRangeUUID.contains(memberInfo.uuid) ? "green" : "dark_green";

                player.displayClientMessage(new TranslatableComponent("pmmo.partyMemberListEntry", pmmoSavedData.getName(memberInfo.uuid), DP.dpSoft(memberInfo.xpGained), totalXpGained == 0 ? "0" : xpGainedPercentage, memberInfo.xpGained).setStyle(XP.textStyle.get(color)), false);
            }
        }
        return 1;
    }
}