package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        EntityPlayer player = (EntityPlayer) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party party = pmmoSavedData.getParty( uuid );

        if( party == null )
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInAParty" ).setStyle( XP.textStyle.get( "red" ) ), false );
        else
        {
            Set<PartyMemberInfo> membersInfo = party.getAllMembersInfo();
            Set<UUID> membersInRangeUUID = party.getOnlineMembersInRange( (EntityPlayerMP) player ).stream().map( playerToMap -> playerToMap.getUniqueID() ).collect(Collectors.toSet());
            double totalXpGained = party.getTotalXpGained();
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreInAParty" ).setStyle( XP.textStyle.get( "green" ) ), false );
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.totalMembersOutOfMax", party.getPartySize(), Party.getMaxPartyMembers() ).setStyle( XP.textStyle.get( "green" ) ), false );
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.partyTotalXpGained", DP.dpSoft( totalXpGained ) ).setStyle( XP.textStyle.get( "green" ) ), false );
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.partyXpBonus", DP.dpSoft( party.getMultiplier( membersInRangeUUID.size() ) ) ).setStyle( XP.textStyle.get( "green" ) ), false );
            for( PartyMemberInfo memberInfo : membersInfo )
            {
                String xpGainedPercentage = DP.dpSoft( ( memberInfo.xpGained / totalXpGained ) * 100 );
                String color = "yellow";
                if( !memberInfo.uuid.equals( uuid ) )
                    color = membersInRangeUUID.contains( memberInfo.uuid ) ? "green" : "dark_green";

                player.sendStatusMessage( new TextComponentTranslation( "pmmo.partyMemberListEntry", pmmoSavedData.getName( memberInfo.uuid ), DP.dpSoft( memberInfo.xpGained ), totalXpGained == 0 ? "0" : xpGainedPercentage, memberInfo.xpGained ).setStyle( XP.textStyle.get( color ) ), false );
            }
        }
        return 1;
    }
}