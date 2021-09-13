package harmonised.pmmo.party;

import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PartyPendingSystem
{
    public static CompoundTag offlineData = new CompoundTag();

    public static Map<UUID, UUID> pendingInvitations = new HashMap<>();
    public static Map<UUID, Long> invitationDates = new HashMap<>();
    public static final long expirationTime = 5 * 60 * 1000;

    public static UUID getOwnerUUID( UUID inviteeUUID )
    {
        UUID ownerUUID = null;

        if( pendingInvitations.containsKey( inviteeUUID ) )
            ownerUUID = pendingInvitations.get( inviteeUUID );

        return ownerUUID;
    }

    public static int createInvitation( ServerPlayer invitee, UUID ownerUUID )
    {
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party ownerParty = pmmoSavedData.getParty( ownerUUID );
        UUID inviteeUUID = invitee.getUUID();
        if( pendingInvitations.containsKey( inviteeUUID ) && pendingInvitations.get( inviteeUUID ).equals( ownerUUID ) )
            return -3;  //Invitation already pending
        else if( ownerParty == null )
            return -1;  //Owner does not have a party
        else if( pmmoSavedData.getParty( inviteeUUID ) != null )
            return -2;  //Invitee is already in a Party
        else if( ownerParty.getMembersCount() + 1 > Party.getMaxPartyMembers() )
            return -4;  //Party is full
        else    //if invitee has no party, and owner does have a party, create an invitation
        {
            pendingInvitations.put( inviteeUUID, ownerUUID );
            invitationDates.put( inviteeUUID, System.currentTimeMillis() );
            return 0;
        }
    }

    public static int acceptInvitation( ServerPlayer invitee, UUID ownerUUID )
    {
        UUID inviteeUUID = invitee.getUUID();
        int result = -3;    //Invitation doesn't exist: Either expired, or didn't exist
        if( pendingInvitations.containsKey( inviteeUUID ) )
        {
            long createdAgo = invitationDates.get( inviteeUUID );
            invitationDates.remove( inviteeUUID );
            if( System.currentTimeMillis() - createdAgo <= expirationTime )
            {
                PmmoSavedData pmmoSavedData = PmmoSavedData.get();
                result = pmmoSavedData.addToParty( ownerUUID, inviteeUUID );
            }
        }
        pendingInvitations.remove( inviteeUUID );
        return result;
    }

    public static boolean declineInvitation( UUID inviteeUUID )
    {
        boolean success = false;
        if( pendingInvitations.containsKey( inviteeUUID ) )
            success = true;
        pendingInvitations.remove( inviteeUUID );
        return success;
    }

    public static void sendPlayerOfflineData( ServerPlayer player )
    {
        CompoundTag partyData = new CompoundTag();

        Party party = PmmoSavedData.get().getParty( player.getUUID() );
        if( party != null )
        {
            Set<PartyMemberInfo> membersInfo = party.getAllMembersInfo();

            for( PartyMemberInfo partyMemberInfo : membersInfo )
            {
                UUID uuid = partyMemberInfo.uuid;
                ServerPlayer partyMember = XP.getPlayerByUUID( uuid );
                CompoundTag partyMemberData = new CompoundTag();

                if( partyMember != null )
                {
                    partyMemberData.putFloat( "maxHp", partyMember.getMaxHealth() );
                    partyMemberData.putFloat( "hp", partyMember.getHealth() );
                    partyMemberData.putString( "dim", XP.getDimResLoc( partyMember.level ).toString() );
                    Vec3 pos = partyMember.position();
                    partyMemberData.putDouble( "x", pos.x() );
                    partyMemberData.putDouble( "y", pos.y() );
                    partyMemberData.putDouble( "z", pos.z() );
                }

                partyData.put( PmmoSavedData.get().getName( uuid ), partyMemberData );
            }
        }
        NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( partyData, 7 ), player );
    }
}