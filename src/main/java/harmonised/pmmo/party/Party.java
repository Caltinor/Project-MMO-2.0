package harmonised.pmmo.party;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party
{
    private final long creationDate;
    private final Set<PartyMemberInfo> membersInfo;

    public Party(long creationDate, Set<PartyMemberInfo> members)
    {
        this.creationDate = creationDate;
        this.membersInfo = members;
    }

    public int getPartySize()
    {
        return membersInfo.size();
    }

    public PartyMemberInfo getMemberInfo(UUID uuid)
    {
        PartyMemberInfo info = null;
        for(PartyMemberInfo memberInfo : membersInfo)
        {
            if(memberInfo.uuid.equals(uuid))
            {
                info = memberInfo;
                break;
            }
        }
        return info;
    }

    public void addMember(UUID uuid)
    {
        membersInfo.add(new PartyMemberInfo(uuid, System.currentTimeMillis(), 0));
    }

    public boolean removeMember(UUID uuid)
    {
        PartyMemberInfo memberInfo = getMemberInfo(uuid);
        if(memberInfo == null)
            return false;
        else
        {
            membersInfo.remove(memberInfo);
            return true;
        }
    }

    public long getCreationDate()
    {
        return creationDate;
    }

    public Set<PartyMemberInfo> getAllMembersInfo()
    {
        return membersInfo;
    }

    public Set<ServerPlayerEntity> getOnlineMembers(MinecraftServer server)
    {
        Set<ServerPlayerEntity> onlineMembers = new HashSet<>();
        for(ServerPlayerEntity onlinePlayer : server.getPlayerList().getPlayers())
        {
            if(getMemberInfo(onlinePlayer.getUniqueID()) != null)
                onlineMembers.add(onlinePlayer);
        }
        return onlineMembers;
    }

    public Set<ServerPlayerEntity> getOnlineMembersInRange(ServerPlayerEntity originPlayer)
    {
        Set<ServerPlayerEntity> membersInRange = XP.getEntitiesInRange(originPlayer.getPositionVec(), getOnlineMembers(originPlayer.getServer()), Config.forgeConfig.partyRange.get());
        membersInRange.remove(originPlayer);
        for(ServerPlayerEntity memberInRange : new HashSet<>(membersInRange))
        {
            if(!XP.isPlayerSurvival(memberInRange))
                membersInRange.remove(memberInRange);
        }
        return membersInRange;
    }

    public int getMembersCount()
    {
        return membersInfo.size();
    }

    public Set<PartyMemberInfo> getMembersInfo(Set<ServerPlayerEntity> membersInRange)
    {
        Set<PartyMemberInfo> onlineMembersInfo = new HashSet<>();
        for(ServerPlayerEntity player : membersInRange)
        {
            onlineMembersInfo.add(getMemberInfo(player.getUniqueID()));
        }
        return onlineMembersInfo;
    }

    public double getTotalXpGained()
    {
        double totalXpGained = 0;

        for(PartyMemberInfo memberInfo : membersInfo)
        {
            totalXpGained += memberInfo.xpGained;
        }

        return totalXpGained;
    }

    public double getMultiplier(int membersInRange)   //membersInRange SHOULD ONLY INCLUDE OTHER PLAYERS
    {
        return 1 + Math.min(membersInRange * Config.forgeConfig.partyXpIncreasePerPlayer.get() / 100D, Config.forgeConfig.maxPartyXpBonus.get() / 100D);
    }

    public void submitXpGained(UUID uuid, double xpGained)
    {
        PartyMemberInfo memberInfo = getMemberInfo(uuid);
        if(memberInfo != null)
            memberInfo.xpGained += xpGained;
    }

    public static int getMaxPartyMembers()
    {
        return Config.forgeConfig.partyMaxMembers.get();
    }
}