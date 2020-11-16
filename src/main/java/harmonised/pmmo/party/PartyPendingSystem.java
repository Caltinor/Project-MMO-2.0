package harmonised.pmmo.party;

import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.*;

public class PartyPendingSystem
{
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

    public static int createInvitation( EntityPlayerMP invitee, UUID ownerUUID )
    {
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party ownerParty = pmmoSavedData.getParty( ownerUUID );
        UUID inviteeUUID = invitee.getUniqueID();
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

    public static int acceptInvitation( EntityPlayerMP invitee, UUID ownerUUID )
    {
        UUID inviteeUUID = invitee.getUniqueID();
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
}