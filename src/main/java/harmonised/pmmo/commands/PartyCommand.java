package harmonised.pmmo.commands;

import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PartyCommand extends CommandBase
{
    @Override
    public String getName()
    {
        return "party";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        List<String> completions = new ArrayList<>();

        if( args.length == 0 )
        {
            completions.add( "create" );
            completions.add( "invite" );
            completions.add( "accept" );
            completions.add( "decline" );
            completions.add( "leave" );
        }
        else if( args.length == 1 && args[0].toLowerCase().equals( "invite" ) )
            return getListOfStringsMatchingLastWord( args, server.getOnlinePlayerNames() );
        return completions;
    }

    @Override
    public List<String> getAliases()
    {
        return new ArrayList<>();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        EntityPlayerMP player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch (PlayerNotFoundException e)
        {
            System.out.println( "ERROR: Party Command called not by a player!" );
            return;
        }
        UUID uuid = player.getUniqueID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party party = pmmoSavedData.getParty( uuid );

        System.out.println( args );
        if( args.length > 0 )
        {
            switch( args[0].toLowerCase() )
            {
                case "accept":
                {
                    UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID( uuid );
                    if( partyOwnerUUID == null )
                    {
                        //Invitation doesn't exist
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInvitedToAnyParty" ).setStyle( XP.textStyle.get( "red" ) ), false );
                        return;
                    }
                    int result = PartyPendingSystem.acceptInvitation( player, partyOwnerUUID );
                    switch( result )
                    {
                        case -4:
                            //Party is full
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.thePartyIsFull" ).setStyle( XP.textStyle.get( "red" ) ), false );
                            break;

                        case -3:
                            //Invitation doesn't exist: Either expired, or didn't exist
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInvitedToAnyParty" ).setStyle( XP.textStyle.get( "red" ) ), false );
                            break;

                        case -2:
                            //You are already in a Party
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreAlreadyInAParty" ).setStyle( XP.textStyle.get( "red" ) ), false );
                            break;

                        case -1:
                            //Owner does not have a party
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.playerDoesNotHaveAParty", "" ).setStyle( XP.textStyle.get( "red" ) ), false );
                            break;

                        case 0:
                            //You have joined the party
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youJoinedAParty" ).setStyle( XP.textStyle.get( "green" ) ), false );

                            Collection<EntityPlayerMP> members = party.getOnlineMembers( player.getServer() );
                            for( EntityPlayerMP memberPlayer : members )
                            {
                                if( !memberPlayer.getUniqueID().equals( uuid ) )
                                    memberPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.playerJoinedYourParty", player.getDisplayName() ).setStyle( XP.textStyle.get( "green" ) ), false );
                            }
                            break;
                    }
                }
                    return;

                case "create":
                {
                    boolean result = pmmoSavedData.makeParty( uuid );

                    if( result )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.partyCreated" ).setStyle(XP.textStyle.get( "green" ) ), false );
                    else
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreAlreadyInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                }
                    return;

                case "decline":
                {
                    UUID partyOwnerUUID = PartyPendingSystem.getOwnerUUID( uuid );
                    if( partyOwnerUUID == null )
                    {
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInvitedToAnyParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                        return;
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
                }
                    return;

                case "invite":
                {
                    if( args.length > 1 )
                    {
                        EntityPlayerMP targetPlayer = getPlayer(server, sender, args[1]);

                        if( targetPlayer != null )
                        {
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
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.playerAlreadyInAParty", "" ).setStyle(XP.textStyle.get( "red" ) ), false );
                                    break;

                                case -1:
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                                    break;

                                case 0:
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.youHaveInvitedAPlayerToYourParty", targetPlayer.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                                    targetPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.playerInvitedYouToAParty", player.getDisplayName() ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                                    break;
                            }
                        }
                        else
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidPlayer", args[1] ).setStyle( XP.skillStyle.get( "red" ) ), false );

                    }
                    else
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ), false );
                }
                    return;

                case "leave":
                {
                    int result = pmmoSavedData.removeFromParty( uuid );
                    switch( result )
                    {
                        case -1:
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );
                            break;

                        case 0:
                        case 1:
                            Collection<EntityPlayerMP> members = party.getOnlineMembers( player.getServer() );
                            for( EntityPlayerMP memberPlayer : members )
                            {
                                if( !memberPlayer.getUniqueID().equals( uuid ) )
                                    memberPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.playerLeftYourParty", player.getDisplayName() ).setStyle( XP.textStyle.get( "red" ) ), false );
                            }
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youLeftTheParty" ).setStyle(XP.textStyle.get( "green" ) ), false );
                            if( result == 1 )
                                player.sendStatusMessage( new TextComponentTranslation( "pmmo.disbandedParty" ).setStyle(XP.textStyle.get( "yellow" ) ), false );
                            break;
                    }
                }
                    return;

                default:
                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidChoice", args[0] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    return;
            }
        }
        else
        {
            if( party == null )
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreNotInAParty" ).setStyle( XP.textStyle.get( "red" ) ), false );
            else
            {
                Set<PartyMemberInfo> membersInfo = party.getAllMembersInfo();
                Set<UUID> membersInRangeUUID = party.getOnlineMembersInRange(player).stream().map(playerToMap -> playerToMap.getUniqueID() ).collect(Collectors.toSet());
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
        }
    }
}