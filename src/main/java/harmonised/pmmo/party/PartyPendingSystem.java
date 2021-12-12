package harmonised.pmmo.party;

import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

public class PartyPendingSystem
{
    public static CompoundNBT offlineData = new CompoundNBT();

    public static Map<UUID, UUID> pendingInvitations = new HashMap<>();
    public static Map<UUID, Long> invitationDates = new HashMap<>();
    public static final long expirationTime = 5 * 60 * 1000;

    public static UUID getOwnerUUID(UUID inviteeUUID)
    {
        UUID ownerUUID = null;

        if(pendingInvitations.containsKey(inviteeUUID))
            ownerUUID = pendingInvitations.get(inviteeUUID);

        return ownerUUID;
    }

    public static int createInvitation(ServerPlayerEntity invitee, UUID ownerUUID)
    {
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        Party ownerParty = pmmoSavedData.getParty(ownerUUID);
        UUID inviteeUUID = invitee.getUniqueID();
        if(pendingInvitations.containsKey(inviteeUUID) && pendingInvitations.get(inviteeUUID).equals(ownerUUID))
            return -3;  //Invitation already pending
        else if(ownerParty == null)
            return -1;  //Owner does not have a party
        else if(pmmoSavedData.getParty(inviteeUUID) != null)
            return -2;  //Invitee is already in a Party
        else if(ownerParty.getMembersCount() + 1 > Party.getMaxPartyMembers())
            return -4;  //Party is full
        else    //if invitee has no party, and owner does have a party, create an invitation
        {
            pendingInvitations.put(inviteeUUID, ownerUUID);
            invitationDates.put(inviteeUUID, System.currentTimeMillis());
            return 0;
        }
    }

    public static int acceptInvitation(ServerPlayerEntity invitee, UUID ownerUUID)
    {
        UUID inviteeUUID = invitee.getUniqueID();
        int result = -3;    //Invitation doesn't exist: Either expired, or didn't exist
        if(pendingInvitations.containsKey(inviteeUUID))
        {
            long createdAgo = invitationDates.get(inviteeUUID);
            invitationDates.remove(inviteeUUID);
            if(System.currentTimeMillis() - createdAgo <= expirationTime)
            {
                PmmoSavedData pmmoSavedData = PmmoSavedData.get();
                result = pmmoSavedData.addToParty(ownerUUID, inviteeUUID);
            }
        }
        pendingInvitations.remove(inviteeUUID);
        return result;
    }

    public static boolean declineInvitation(UUID inviteeUUID)
    {
        boolean success = false;
        if(pendingInvitations.containsKey(inviteeUUID))
            success = true;
        pendingInvitations.remove(inviteeUUID);
        return success;
    }

    public static void sendPlayerOfflineData(ServerPlayerEntity player)
    {
        CompoundNBT partyData = new CompoundNBT();

        Party party = PmmoSavedData.get().getParty(player.getUniqueID());
        if(party != null)
        {
            Set<PartyMemberInfo> membersInfo = party.getAllMembersInfo();

            for(PartyMemberInfo partyMemberInfo : membersInfo)
            {
                UUID uuid = partyMemberInfo.uuid;
                ServerPlayerEntity partyMember = XP.getPlayerByUUID(uuid);
                CompoundNBT partyMemberData = new CompoundNBT();

                if(partyMember != null)
                {
                    partyMemberData.putFloat("maxHp", partyMember.getMaxHealth());
                    partyMemberData.putFloat("hp", partyMember.getHealth());
                    partyMemberData.putString("dim", XP.getDimResLoc(partyMember.world).toString());
                    Vector3d pos = partyMember.getPositionVec();
                    partyMemberData.putDouble("x", pos.getX());
                    partyMemberData.putDouble("y", pos.getY());
                    partyMemberData.putDouble("z", pos.getZ());
                }

                partyData.put(PmmoSavedData.get().getName(uuid), partyMemberData);
            }
        }
        NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(partyData, 7), player);
    }
}