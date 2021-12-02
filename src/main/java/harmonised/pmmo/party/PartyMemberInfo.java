package harmonised.pmmo.party;

import java.util.UUID;

public class PartyMemberInfo
{
    public final UUID uuid;
    public final long joinDate;
    public double xpGained;

    public PartyMemberInfo(UUID uuid, long joinDate, double xpGained)
    {
        this.uuid = uuid;
        this.joinDate = joinDate;
        this.xpGained = xpGained;
    }
}
