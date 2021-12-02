package harmonised.pmmo.party;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
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

    public Set<ServerPlayer> getOnlineMembers(MinecraftServer server)
    {
        Set<ServerPlayer> onlineMembers = new HashSet<>();
        for(ServerPlayer onlinePlayer : server.getPlayerList().getPlayers())
        {
            if(getMemberInfo(onlinePlayer.getUUID()) != null)
                onlineMembers.add(onlinePlayer);
        }
        return onlineMembers;
    }

    public Set<ServerPlayer> getOnlineMembersInRange(ServerPlayer originPlayer)
    {
        Set<ServerPlayer> membersInRange = XP.getEntitiesInRange(originPlayer.position(), getOnlineMembers(originPlayer.getServer()), Config.forgeConfig.partyRange.get());
        membersInRange.remove(originPlayer);
        for(ServerPlayer memberInRange : new HashSet<>(membersInRange))
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

    public Set<PartyMemberInfo> getMembersInfo(Set<ServerPlayer> membersInRange)
    {
        Set<PartyMemberInfo> onlineMembersInfo = new HashSet<>();
        for(ServerPlayer player : membersInRange)
        {
            onlineMembersInfo.add(getMemberInfo(player.getUUID()));
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